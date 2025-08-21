package cn.game.core.zookeeper;

import cn.game.util.ZkHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 通用的“ZK 驱动的本地只读缓存”，带强类型值 T、外部键类型 K：
 * - 内部统一用字符串键做存储与路径拼接，外部通过 KeyAdapter<K> 进行键转换。
 * - 启动：warmup() 先全量加载快照，再 start() 注册监听，实时更新。
 * - 提供只读 API：getByKey、getAll、containsKey、size。
 * - 支持变更监听：先更新本地缓存，再回调监听。
 * - 提供基础写入辅助（非并发、无版本控制）：createMissing、upsert、deleteByKey。
 */
public class ZkBackedCache<K, T> implements Closeable {

    private final CuratorFramework client;
    private final PathPolicy pathPolicy;
    private final ValueCodec<T> codec;
    private final IdExtractor<T> idExtractor; // 返回业务侧键（K 或其可表示形式）
    private final KeyAdapter<K> keyAdapter;

    // 内部存储：字符串键 -> 值
    private final Map<String, T> cache = new ConcurrentHashMap<>();
    private final List<CacheChangeListener<T>> listeners = new CopyOnWriteArrayList<>();

    private volatile CuratorCache curatorCache;
    private final AtomicBoolean warmedUp = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);

    private ZkBackedCache(Builder<K, T> b) {
        this.client = Objects.requireNonNull(b.client, "client");
        this.pathPolicy = Objects.requireNonNull(b.pathPolicy, "pathPolicy");
        this.codec = Objects.requireNonNull(b.codec, "codec");
        this.idExtractor = Objects.requireNonNull(b.idExtractor, "idExtractor");
        this.keyAdapter = Objects.requireNonNull(b.keyAdapter, "keyAdapter");
    }

    // ============ 生命周期 ============

    public void start() throws Exception {
        if (started.compareAndSet(false, true)) {
            ensureRoot();
            this.curatorCache = CuratorCache.build(client, pathPolicy.basePath());
            CuratorCacheListener l = CuratorCacheListener.builder()
                    .forCreates(cd -> handleCreateOrChange(cd.getPath(), cd.getData(), ChangeType.NODE_CREATED))
                    .forChanges((od, nd) -> {
                        if (nd != null) handleCreateOrChange(nd.getPath(), nd.getData(), ChangeType.NODE_CHANGED);
                    })
                    .forDeletes(cd -> {
                        String stringKey = pathPolicy.idFromPath(cd.getPath());
                        if (stringKey != null) {
                            cache.remove(stringKey);
                            fire(ChangeType.NODE_DELETED, stringKey, null);
                        }
                    })
                    .build();
            this.curatorCache.listenable().addListener(l);
            this.curatorCache.start();
        }
    }

    public void warmup() throws Exception {
        ensureRoot();
        List<String> children;
        try {
            children = client.getChildren().forPath(pathPolicy.basePath());
        } catch (KeeperException.NoNodeException e) {
            children = Collections.emptyList();
        }
        Map<String, T> tmp = new HashMap<>();
        for (String child : children) {
            String p = pathPolicy.pathForId(child);
            try {
                byte[] data = client.getData().forPath(p);
                T val = safeDecode(data);
                if (val != null) {
                    Object idObj = idExtractor.getId(val);
                    @SuppressWarnings("unchecked")
                    String stringKey = keyAdapter.toStringKey((K) idObj);
                    if (stringKey != null) tmp.put(stringKey, val);
                }
            } catch (KeeperException.NoNodeException ignore) {
                // between list and read, node removed
            }
        }
        cache.clear();
        cache.putAll(tmp);
        warmedUp.set(true);
    }

    /** 先 warmup 后 start，保证“先快照、后增量”。 */
    public void startAndWarmup() throws Exception {
        warmup();
        start();
    }

    public boolean isWarmedUp() {
        return warmedUp.get();
    }

    public boolean isStarted() {
        return started.get();
    }

    public void stop() {
        CuratorCache c = this.curatorCache;
        if (c != null) {
            try {
                c.close();
            } finally {
                this.curatorCache = null;
            }
        }
        started.set(false);
    }

    @Override
    public void close() {
        stop();
    }

    // ============ 只读查询 API ============

    public Optional<T> getByKey(K key) {
        if (key == null) return Optional.empty();
        String k = keyAdapter.toStringKey(key);
        return Optional.ofNullable(cache.get(k));
    }

    public List<T> getAll() {
        return new ArrayList<>(cache.values());
    }

    public boolean containsKey(K key) {
        if (key == null) return false;
        return cache.containsKey(keyAdapter.toStringKey(key));
    }

    public int size() {
        return cache.size();
    }

    public void addListener(CacheChangeListener<T> listener) {
        if (listener != null) listeners.add(listener);
    }

    public void removeListener(CacheChangeListener<T> listener) {
        listeners.remove(listener);
    }

    // ============ 基础写入辅助（无并发版本控制） ============

    /**
     * 仅创建缺失节点；已存在则跳过。
     * 返回成功创建的 key（字符串形式）列表。
     */
    public List<String> createMissing(Collection<T> values) throws Exception {
        Objects.requireNonNull(values, "values");
        ensureRoot();
        List<String> created = new ArrayList<>();
        for (T v : values) {
            Object idObj = idExtractor.getId(v);
            @SuppressWarnings("unchecked")
            String stringKey = keyAdapter.toStringKey((K) idObj);
            String path = pathPolicy.pathForId(stringKey);
            if (exists(path)) continue;
            try {
                client.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(path, codec.encode(v));
                cache.put(stringKey, v);
                created.add(stringKey);
                fire(ChangeType.NODE_CREATED, stringKey, v);
            } catch (KeeperException.NodeExistsException ignore) {
            }
        }
        return created;
    }

    /**
     * 简单 upsert：存在则 setData，不存在则 create。
     */
    public void upsert(T value) throws Exception {
        Objects.requireNonNull(value, "value");
        Object idObj = idExtractor.getId(value);
        @SuppressWarnings("unchecked")
        String stringKey = keyAdapter.toStringKey((K) idObj);
        String path = pathPolicy.pathForId(stringKey);
        byte[] data = codec.encode(value);
        if (exists(path)) {
            client.setData().forPath(path, data);
            cache.put(stringKey, value);
            fire(ChangeType.NODE_CHANGED, stringKey, value);
        } else {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data);
            cache.put(stringKey, value);
            fire(ChangeType.NODE_CREATED, stringKey, value);
        }
    }

    /**
     * 删除对应 key 的节点（不存在则跳过）。
     * 返回是否确实删除了一个节点。
     */
    public boolean deleteByKey(K key) throws Exception {
        Objects.requireNonNull(key, "key");
        String stringKey = keyAdapter.toStringKey(key);
        String path = pathPolicy.pathForId(stringKey);
        try {
            client.delete().forPath(path);
            cache.remove(stringKey);
            fire(ChangeType.NODE_DELETED, stringKey, null);
            return true;
        } catch (KeeperException.NoNodeException e) {
            return false;
        }
    }

    // ============ 内部工具 ============

    private void ensureRoot() {
        try {
            if (client.checkExists().forPath(pathPolicy.basePath()) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(pathPolicy.basePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure root: " + pathPolicy.basePath(), e);
        }
    }

    private boolean exists(String path) throws Exception {
        return client.checkExists().forPath(path) != null;
    }

    private void handleCreateOrChange(String path, byte[] data, ChangeType type) {
        T value = safeDecode(data);
        if (value == null) return;
        Object idObj = idExtractor.getId(value);
        if (idObj == null) return;
        @SuppressWarnings("unchecked")
        String stringKey = keyAdapter.toStringKey((K) idObj);
        if (stringKey == null) return;
        cache.put(stringKey, value);
        fire(type, stringKey, value);
    }

    private T safeDecode(byte[] data) {
        if (data == null || data.length == 0) return null;
        try {
            return codec.decode(data);
        } catch (RuntimeException ex) {
            // 可在此处接入日志系统
            return null;
        }
    }

    private void fire(ChangeType type, String stringKey, T value) {
        for (CacheChangeListener<T> l : listeners) {
            try {
                // 对外回调 key 使用最贴近业务的类型：这里回调 String（更通用）
                l.onChange(type, stringKey, value);
            } catch (Throwable ignore) {
            }
        }
    }

    // ============ Builder ============

    public static <K, T> Builder<K, T> builder(Class<K> keyType, Class<T> valueType) {
        return new Builder<>();
    }

    public static final class Builder<K, T> {
        private CuratorFramework client;
        private PathPolicy pathPolicy;
        private ValueCodec<T> codec;
        private IdExtractor<T> idExtractor;
        private KeyAdapter<K> keyAdapter;

        private Builder() {}

        public Builder<K, T> client(CuratorFramework client) {
            this.client = client;
            return this;
        }

        public Builder<K, T> pathPolicy(PathPolicy pathPolicy) {
            this.pathPolicy = pathPolicy;
            return this;
        }

        public Builder<K, T> codec(ValueCodec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<K, T> idExtractor(IdExtractor<T> idExtractor) {
            this.idExtractor = idExtractor;
            return this;
        }

        public Builder<K, T> keyAdapter(KeyAdapter<K> keyAdapter) {
            this.keyAdapter = keyAdapter;
            return this;
        }

        public ZkBackedCache<K, T> build() {
            if (this.client == null) {
                // 默认复用 ZkHelper.curator（若你的项目有该单例）
                this.client = ZkHelper.curator;
            }
            if (this.keyAdapter == null) {
                // 默认用字符串键
                @SuppressWarnings("unchecked")
                KeyAdapter<K> def = (KeyAdapter<K>) KeyAdapter.stringKey();
                this.keyAdapter = def;
            }
            return new ZkBackedCache<>(this);
        }
    }
}

