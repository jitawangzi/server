package cn.game.core.base;

import cn.game.protocol.generated.config.VirtualServerConfig;
import cn.game.util.JsonUtil;
import cn.game.util.ZkHelper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 虚拟分服配置注册表
 * - 复用外部 CuratorFramework（ZkHelper.curator）
 * - 数据存储在 ZK 路径 basePath/{ID}，内容为 JSON（VirtualServerView）
 * - 提供本地缓存 cacheMap，并在 ZK 变更时自动同步
 * - 支持一次性初始化、仅补齐缺失节点、CAS 部分更新等能力
 */
public class VirtualServerRegistry implements Closeable {

    private static volatile VirtualServerRegistry INSTANCE;

    // 注意：这是 basePath，相对 Curator 的 namespace 生效
    private final String basePath;
    private final CuratorFramework client;
    private CuratorCache cache;

    /** 本地缓存：serverId -> VirtualServerView（线程安全） */
    private final Map<String, VirtualServerView> cacheMap = new ConcurrentHashMap<>();
    /** 缓存是否已完成首轮预热（快照加载） */
    private final AtomicBoolean cacheWarmedUp = new AtomicBoolean(false);

    public static VirtualServerRegistry getInstance() {
        VirtualServerRegistry inst = INSTANCE;
        if (inst == null) {
            synchronized (VirtualServerRegistry.class) {
                inst = INSTANCE;
                if (inst == null) {
                    inst = new VirtualServerRegistry(ZkHelper.curator, "/server/login/game/virtual-servers");
                    INSTANCE = inst;
                }
            }
        }
        return inst;
    }

    private VirtualServerRegistry(CuratorFramework externalClient, String basePath) {
        if (externalClient == null) throw new IllegalArgumentException("CuratorFramework cannot be null");
        this.client = externalClient;
        this.basePath = normalizeBasePath(basePath);
        ensureRoot();
    }

    private static String normalizeBasePath(String p) {
        if (p == null || p.isEmpty()) return "/server/login/game/virtual-servers";
        return p.startsWith("/") ? p : "/" + p;
    }

    private void ensureRoot() {
        try {
            if (client.checkExists().forPath(basePath) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(basePath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure root path: " + basePath, e);
        }
    }

    private String pathFor(String serverId) {
        return basePath + "/" + serverId;
    }

    // ============ 视图序列化 ============

    private static byte[] toBytes(VirtualServerView view) {
        String json = JsonUtil.toJsonString(view);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    private static VirtualServerView fromBytes(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);
        return JsonUtil.parseObject(json, VirtualServerView.class);
    }

    private static VirtualServerView toView(VirtualServerConfig cfg) {
        return new VirtualServerView(
                cfg.ID,
                cfg.name,
                cfg.playerMaxCount,
                cfg.seq,
                cfg.openTime == null ? null : cfg.openTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    // ============ 本地缓存：预热与读取 ============

    /** 从 ZK 全量加载一次，写入本地缓存（幂等，可重复调用） */
    public void loadSnapshotIntoCache() throws Exception {
        ensureRoot();
        Map<String, VirtualServerView> tmp = new HashMap<>();
        List<String> children;
        try {
            children = client.getChildren().forPath(basePath);
        } catch (KeeperException.NoNodeException e) {
            children = Collections.emptyList();
        }
        for (String child : children) {
            String path = pathFor(child);
            try {
                byte[] data = client.getData().forPath(path);
                VirtualServerView view = fromBytes(data);
                if (view != null && view.ID != null) {
                    tmp.put(view.ID, view);
                }
            } catch (KeeperException.NoNodeException ignore) {
                // 子节点在遍历和读取之间被删除，忽略
            }
        }
        cacheMap.clear();
        cacheMap.putAll(tmp);
        cacheWarmedUp.set(true);
    }

    /** 从本地缓存读取一个视图（不触发网络 IO） */
    public Optional<VirtualServerView> getFromCache(String serverId) {
        if (serverId == null) return Optional.empty();
        return Optional.ofNullable(cacheMap.get(serverId));
    }

    /** 获取本地缓存的所有视图快照（浅拷贝） */
    public List<VirtualServerView> getAllFromCache() {
        return new ArrayList<>(cacheMap.values());
    }

    /** 是否完成了首次快照加载 */
    public boolean isCacheWarmedUp() {
        return cacheWarmedUp.get();
    }

    // ============ CRUD ============

    public void upsert(VirtualServerConfig config) throws Exception {
        Objects.requireNonNull(config);
        Objects.requireNonNull(config.ID, "id required");
        upsertView(toView(config));
    }

    public void upsertView(VirtualServerView view) throws Exception {
        Objects.requireNonNull(view);
        Objects.requireNonNull(view.ID, "id required");
        String path = pathFor(view.ID);
        byte[] data = toBytes(view);

        Stat stat = client.checkExists().forPath(path);
        if (stat == null) {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data);
        } else {
            client.setData().forPath(path, data);
        }
        // 本地缓存立即更新
        cacheMap.put(view.ID, view);
    }

    // 仅当 basePath 为空时，按 configs 初始化一次
    public boolean initOnceFromConfigs(List<VirtualServerConfig> configs) throws Exception {
        Objects.requireNonNull(configs, "configs");
        List<VirtualServerView> views = new ArrayList<>(configs.size());
        for (VirtualServerConfig c : configs) {
            Objects.requireNonNull(c);
            Objects.requireNonNull(c.ID, "config.ID required");
            views.add(toView(c));
        }
        return initOnceFromViews(views);
    }

    /**
     * 仅当 basePath 下无任何子节点时，批量创建所有视图。
     * 返回值：true 表示完成初始化写入；false 表示已存在数据而跳过。
     */
    public boolean initOnceFromViews(List<VirtualServerView> views) throws Exception {
        Objects.requireNonNull(views, "views");

        // 1) 如果根不存在，先创建根
        ensureRoot();

        // 2) 快速检查是否已有任何子节点
        List<String> existingChildren;
        try {
            existingChildren = client.getChildren().forPath(basePath);
            if (existingChildren != null && !existingChildren.isEmpty()) {
                return false; // 已有数据，跳过初始化
            }
        } catch (KeeperException.NoNodeException e) {
            // 理论上 ensureRoot 后不会走到这里
            ensureRoot();
        }

        // 3) 再次稳妥检查：若任何目标节点已存在，也放弃初始化（避免并发启动重复写）
        for (VirtualServerView v : views) {
            Objects.requireNonNull(v);
            Objects.requireNonNull(v.ID, "view.ID required");
            String path = pathFor(v.ID);
            if (client.checkExists().forPath(path) != null) {
                return false; // 检测到已有节点，认为已初始化，跳过
            }
        }

        // 4) 使用事务原子创建全部节点
        CuratorTransactionFinal tx = client.inTransaction().check().forPath(basePath).and();
        for (VirtualServerView v : views) {
            String path = pathFor(v.ID);
            byte[] data = toBytes(v);
            tx = tx.create().forPath(path, data).and();
        }
        tx.commit();

        // 事务成功，刷新本地缓存
        for (VirtualServerView v : views) {
            cacheMap.put(v.ID, v);
        }
        return true;
    }

    /**
     * 根据业务配置，仅创建缺失节点；已存在的不修改（半幂等）。
     * @param configs 业务 VirtualServerConfig 列表
     * @return 实际新建的节点ID列表
     */
    public List<String> initMissingFromConfigs(List<VirtualServerConfig> configs) throws Exception {
        Objects.requireNonNull(configs, "configs");
        List<VirtualServerView> views = new ArrayList<>(configs.size());
        for (VirtualServerConfig c : configs) {
            Objects.requireNonNull(c);
            Objects.requireNonNull(c.ID, "config.ID required");
            views.add(toView(c));
        }
        return initMissingFromViews(views);
    }

    /**
     * 仅创建缺失节点；已存在的不修改（半幂等）。
     * @param views 视图列表
     * @return 实际新建的节点ID列表
     */
    public List<String> initMissingFromViews(List<VirtualServerView> views) throws Exception {
        Objects.requireNonNull(views, "views");
        ensureRoot();

        List<String> created = new ArrayList<>();
        for (VirtualServerView v : views) {
            Objects.requireNonNull(v);
            Objects.requireNonNull(v.ID, "view.ID required");
            String path = pathFor(v.ID);

            Stat stat = client.checkExists().forPath(path);
            if (stat != null) {
                // 已存在：跳过，不覆盖
                continue;
            }

            try {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(path, toBytes(v));
                created.add(v.ID);
                cacheMap.put(v.ID, v); // 创建成功即写入本地缓存
            } catch (KeeperException.NodeExistsException e) {
                // 并发下被他人抢先创建，按“已存在”处理（由监听同步进来）
            }
        }
        return created;
    }

    public Optional<Versioned<VirtualServerView>> getView(String serverId) throws Exception {
        String path = pathFor(serverId);
        Stat stat = new Stat();
        byte[] data;
        try {
            data = client.getData().storingStatIn(stat).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return Optional.empty();
        }
        return Optional.of(new Versioned<>(fromBytes(data), stat.getVersion()));
    }

    public List<VirtualServerView> listAllViews() throws Exception {
        List<VirtualServerView> result = new ArrayList<>();
        List<String> children;
        try {
            children = client.getChildren().forPath(basePath);
        } catch (KeeperException.NoNodeException e) {
            return result;
        }
        for (String child : children) {
            Optional<Versioned<VirtualServerView>> v = getView(child);
            v.ifPresent(x -> result.add(x.value()));
        }
        return result;
    }

    public boolean delete(String serverId) throws Exception {
        String path = pathFor(serverId);
        try {
            client.delete().forPath(path);
            cacheMap.remove(serverId); // 成功时移除本地缓存
            return true;
        } catch (KeeperException.NoNodeException e) {
            return false;
        }
    }

    public void upsertBatchViews(List<VirtualServerView> views) throws Exception {
        CuratorTransactionFinal tx = client.inTransaction().check().forPath(basePath).and();
        for (VirtualServerView v : views) {
            String path = pathFor(v.ID);
            byte[] data = toBytes(v);
            Stat stat = client.checkExists().forPath(path);
            if (stat == null) {
                tx = tx.create().forPath(path, data).and();
            } else {
                tx = tx.setData().forPath(path, data).and();
            }
        }
        tx.commit();
        // 本地缓存批量更新（这里简单覆盖）
        for (VirtualServerView v : views) {
            cacheMap.put(v.ID, v);
        }
    }

    // ============ 部分更新（CAS） ============

    public boolean updateName(String serverId, String name, int expectedVersion) throws Exception {
        return updateField(serverId, expectedVersion, v -> v.name = name);
    }

    public boolean updatePlayerMaxCount(String serverId, Integer playerMaxCount, int expectedVersion) throws Exception {
        return updateField(serverId, expectedVersion, v -> v.playerMaxCount = playerMaxCount);
    }

    public boolean updateSeq(String serverId, Integer seq, int expectedVersion) throws Exception {
        return updateField(serverId, expectedVersion, v -> v.seq = seq);
    }

    public boolean updateOpenTime(String serverId, LocalDateTime openTime, int expectedVersion) throws Exception {
        return updateField(serverId, expectedVersion, v -> v.openTime = openTime == null ? null : openTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    public boolean updateField(String serverId, int expectedVersion, ViewMutator mutator) throws Exception {
        String path = pathFor(serverId);
        Stat stat = new Stat();
        byte[] oldData;
        try {
            oldData = client.getData().storingStatIn(stat).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return false;
        }

        VirtualServerView view = fromBytes(oldData);
        mutator.mutate(view);
        byte[] newData = toBytes(view);

        try {
            if (expectedVersion >= 0) {
                client.setData().withVersion(expectedVersion).forPath(path, newData);
            } else {
                client.setData().forPath(path, newData);
            }
            // 本地缓存更新
            cacheMap.put(serverId, view);
            return true;
        } catch (KeeperException.BadVersionException e) {
            return false;
        }
    }

    // ============ 监听 ============

    @FunctionalInterface
    public interface ChangeListener {
        void onEvent(EventType type, String path, byte[] data);
    }

    public enum EventType { NODE_CREATED, NODE_CHANGED, NODE_DELETED }

    /** 安全解析，避免异常打断监听线程 */
    private VirtualServerView safeParse(byte[] data) {
        if (data == null || data.length == 0) return null;
        try {
            return fromBytes(data);
        } catch (RuntimeException ex) {
            // 可在此处记录日志
            return null;
        }
    }

    /** 从路径获取 serverId：基于 basePath 的最后一级 */
    private String extractServerIdFromPath(String path) {
        if (path == null) return null;
        int idx = path.lastIndexOf('/');
        return idx >= 0 && idx < path.length() - 1 ? path.substring(idx + 1) : null;
    }

    public void startListening(ChangeListener listener) {
        if (this.cache != null) return;
        this.cache = CuratorCache.build(client, basePath);
        CuratorCacheListener l = CuratorCacheListener.builder()
                .forCreates(cd -> {
                    VirtualServerView v = safeParse(cd.getData());
                    if (v != null && v.ID != null) {
                        cacheMap.put(v.ID, v);
                    }
                    if (listener != null) listener.onEvent(EventType.NODE_CREATED, cd.getPath(), cd.getData());
                })
                .forChanges((od, nd) -> {
                    VirtualServerView v = safeParse(nd != null ? nd.getData() : null);
                    if (v != null && v.ID != null) {
                        cacheMap.put(v.ID, v);
                    }
                    if (listener != null) {
                        String path = nd != null ? nd.getPath() : (od != null ? od.getPath() : null);
                        byte[] data = nd != null ? nd.getData() : null;
                        listener.onEvent(EventType.NODE_CHANGED, path, data);
                    }
                })
                .forDeletes(cd -> {
                    String serverId = extractServerIdFromPath(cd.getPath());
                    if (serverId != null) {
                        cacheMap.remove(serverId);
                    }
                    if (listener != null) listener.onEvent(EventType.NODE_DELETED, cd.getPath(), null);
                })
                .build();
        this.cache.listenable().addListener(l);
        this.cache.start();
    }

    /** 先预热缓存，再启动监听（推荐在应用启动时调用） */
    public void startAndWarmup(ChangeListener listener) throws Exception {
        loadSnapshotIntoCache();
        startListening(listener);
    }

    public void stopListening() {
        if (this.cache != null) {
            this.cache.close();
            this.cache = null;
        }
    }

    @Override
    public void close() {
        // 不关闭外部 CuratorFramework
        stopListening();
    }

    // ============ 辅助类型 ============

    @FunctionalInterface
    public interface ViewMutator {
        void mutate(VirtualServerView v);
    }

    public record Versioned<T>(T value, int version) {}

    public static class VirtualServerView {
        public String ID;
        public String name;
        public Integer playerMaxCount;
        public Integer seq;
        public String openTime;

        public VirtualServerView() {}
        public VirtualServerView(String ID, String name, Integer playerMaxCount, Integer seq, String openTime) {
            this.ID = ID;
            this.name = name;
            this.playerMaxCount = playerMaxCount;
            this.seq = seq;
            this.openTime = openTime;
        }
        @Override public String toString() { return JsonUtil.toJsonString(this); }
    }
}