package cn.game.core.zookeeper;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.zookeeper.codec.ValueCodec;
import cn.game.core.zookeeper.merge.MapMergePolicy;
import cn.game.core.zookeeper.merge.MergePolicy;
import cn.game.core.zookeeper.merge.ReplacePolicy;
import cn.game.core.zookeeper.merge.SetUnionPolicy;
import cn.game.util.ZkHelper;

/**
 * 通用的“ZK 驱动的本地缓存”，带强类型值 T、外部键类型 K：
 * - 内部统一用字符串键做存储与路径拼接，外部通过 KeyAdapter<K> 进行键转换。
 * - 生命周期：warmup() 先全量加载，start() 注册监听；或 startAndWarmup()。
 * - 只读 API：getByKey、getAll、containsKey、size。
 * - 监听：addListener，事件为后置回调。
 * - 写入辅助：createMissing、upsert、deleteByKey；upsert 支持 MergePolicy。
 *
 * 合并策略（优先级）：
 * 1) 显式传入的 mergePolicy（最高优先级）
 * 2) 默认策略推断：
 *    - 若 T 实现 Set -> SetUnionPolicy
 *    - 若 T 实现 Map -> MapMergePolicy（新值覆盖旧值）
 *    - 其他类型 -> ReplacePolicy
 */
public class ZkBackedCache<K, T> implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZkBackedCache.class);
	private final CuratorFramework client;
	private final PathPolicy pathPolicy;
	private final ValueCodec<T> codec;
	private final IdExtractor<T> idExtractor; // 返回业务侧键（K 或其可表示形式）
	private final KeyAdapter<K> keyAdapter;
	private final MergePolicy<T> mergePolicyOpt; // 可能为空，表示使用默认推断策略

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
		this.mergePolicyOpt = b.mergePolicy;
	}

	// ============ 生命周期 ============

	public void start() throws Exception {
		if (started.compareAndSet(false, true)) {
			ensureRoot();
			this.curatorCache = CuratorCache.build(client, pathPolicy.basePath());
			CuratorCacheListener l = CuratorCacheListener.builder()
					// 仅处理 basePath 的“直接子节点”创建事件；忽略 basePath 自身及更深层级
					.forCreates(cd -> {
						String p = cd.getPath();
						if (!isDirectChild(p)) {
							LOGGER.debug("skip create (not direct child): path={}, data={}", p,
									cd.getData() == null ? null : new String(cd.getData(), StandardCharsets.UTF_8));
							return;
						}
						handleCreateOrChange(p, cd.getData(), NodeChangeType.NODE_CREATED);
					})
					// 仅处理 basePath 的“直接子节点”变更事件
					.forChanges((od, nd) -> {
						if (nd != null) {
							String p = nd.getPath();
							if (!isDirectChild(p)) {
								LOGGER.debug("skip change (not direct child): path={}", p);
								return;
							}
							handleCreateOrChange(p, nd.getData(), NodeChangeType.NODE_CHANGED);
						}
					})
					// 仅处理 basePath 的“直接子节点”删除事件
					.forDeletes(cd -> {
						String p = cd.getPath();
						if (!isDirectChild(p)) {
							LOGGER.debug("skip delete (not direct child): path={}", p);
							return;
						}
						String stringKey = pathPolicy.idFromPath(p);
						if (stringKey != null) {
							cache.remove(stringKey);
							fire(NodeChangeType.NODE_DELETED, stringKey, null);
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
					if (stringKey != null)
						tmp.put(stringKey, val);
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
		if (key == null)
			return Optional.empty();
		String k = keyAdapter.toStringKey(key);
		return Optional.ofNullable(cache.get(k));
	}

	public List<T> getAll() {
		return new ArrayList<>(cache.values());
	}

	public boolean containsKey(K key) {
		if (key == null)
			return false;
		return cache.containsKey(keyAdapter.toStringKey(key));
	}

	public int size() {
		return cache.size();
	}

	public void addListener(CacheChangeListener<T> listener) {
		if (listener != null)
			listeners.add(listener);
	}

	public void removeListener(CacheChangeListener<T> listener) {
		listeners.remove(listener);
	}

	// ============ 写入辅助（支持合并策略） ============

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
			if (exists(path))
				continue;
			try {
				T merged = applyMerge(null, v);
				client.create()
						.creatingParentsIfNeeded()
						.withMode(CreateMode.PERSISTENT)
						.forPath(path, selectPolicy().encodeForWrite(merged, codec));
				cache.put(stringKey, merged);
				created.add(stringKey);
				fire(NodeChangeType.NODE_CREATED, stringKey, merged);
			} catch (KeeperException.NodeExistsException ignore) {
			}
		}
		return created;
	}

	/**
	 * upsert：存在则 setData（可能合并），不存在则 create。
	 */
	public void upsert(T value) throws Exception {
		Objects.requireNonNull(value, "value");
		Object idObj = idExtractor.getId(value);
		@SuppressWarnings("unchecked")
		String stringKey = keyAdapter.toStringKey((K) idObj);
		String path = pathPolicy.pathForId(stringKey);

		T current = cache.get(stringKey);
		T merged = applyMerge(current, value);
		byte[] data = selectPolicy().encodeForWrite(merged, codec);

		if (exists(path)) {
			client.setData().forPath(path, data);
			cache.put(stringKey, merged);
			fire(NodeChangeType.NODE_CHANGED, stringKey, merged);
		} else {
			client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data);
			cache.put(stringKey, merged);
			fire(NodeChangeType.NODE_CREATED, stringKey, merged);
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
			fire(NodeChangeType.NODE_DELETED, stringKey, null);
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

	private void handleCreateOrChange(String path, byte[] data, NodeChangeType type) {
		// 只关心 basePath 的“直接子节点”。额外兜底过滤，避免误处理 basePath 或更深层节点。
		if (!isDirectChild(path)) {
			return;
		}

		T incoming = safeDecode(data);
		if (incoming == null)
			return;

		Object idObj = idExtractor.getId(incoming);
		if (idObj == null)
			return;
		@SuppressWarnings("unchecked")
		String stringKey = keyAdapter.toStringKey((K) idObj);
		if (stringKey == null)
			return;

		// 监听回调的值来自 ZK 的完整新值；若你期望在内存中做“增量合并”，也可应用策略
		T old = cache.get(stringKey);
		T merged = applyMerge(old, incoming);
		cache.put(stringKey, merged);
		fire(type, stringKey, merged);
	}

	private T safeDecode(byte[] data) {
		if (data == null || data.length == 0)
			return null;
		try {
			return codec.decode(data);
		} catch (RuntimeException ex) {
			// 打印内容预览便于排查
			try {
				LOGGER.error("Failed to decode data: {}", new String(data, StandardCharsets.UTF_8), ex);
			} catch (Throwable ignore) {
				LOGGER.error("Failed to decode data: <non-text bytes>", ex);
			}
			return null;
		}
	}

	private void fire(NodeChangeType type, String stringKey, T value) {
		for (CacheChangeListener<T> l : listeners) {
			try {
				l.onChange(type, stringKey, value);
			} catch (Throwable ignore) {
			}
		}
	}

	private T applyMerge(T oldValue, T newValue) {
		return selectPolicy().merge(oldValue, newValue);
	}

	private MergePolicy<T> selectPolicy() {
		if (mergePolicyOpt != null)
			return mergePolicyOpt;
		// 默认策略推断
		// 小心类型擦除：我们用运行时对象判断
		// 尽量不对 cache 中的旧值做 instanceof 判断（可能为 null），改从 newValue 或泛型习惯入手。
		return new MergePolicy<T>() {
			private MergePolicy<T> delegate;

			private MergePolicy<T> ensure() {
				if (delegate != null)
					return delegate;
				delegate = inferDefaultPolicy();
				return delegate;
			}

			@Override
			public T merge(T oldValue, T newValue) {
				return ensure().merge(oldValue, newValue);
			}

			@Override
			public byte[] encodeForWrite(T merged, ValueCodec<T> codec) {
				return ensure().encodeForWrite(merged, codec);
			}
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private MergePolicy<T> inferDefaultPolicy() {
		// 尝试通过缓存中已有值或空集合创建判断
		// 优先：若缓存中已有任意值可判断类型
		T any = null;
		for (T v : cache.values()) {
			any = v;
			if (any != null)
				break;
		}

		if (any instanceof Set) {
			return (MergePolicy<T>) new SetUnionPolicy<>();
		}
		if (any instanceof Map) {
			return (MergePolicy<T>) new MapMergePolicy<>();
		}
		// 若缓存还空，根据“预期值类型”也许无法判断。我们按保守策略：
		// 在实际 merge 调用时，若 newValue 是集合或映射，再做一次判断。
		return new MergePolicy<T>() {
			private MergePolicy<T> delegate;

			@Override
			public T merge(T oldValue, T newValue) {
				if (delegate == null) {
					if (newValue instanceof Set) {
						delegate = (MergePolicy<T>) new SetUnionPolicy<>();
					} else if (newValue instanceof Map) {
						delegate = (MergePolicy<T>) new MapMergePolicy<>();
					} else {
						delegate = new ReplacePolicy<>();
					}
				}
				return delegate.merge(oldValue, newValue);
			}

			@Override
			public byte[] encodeForWrite(T merged, ValueCodec<T> codec) {
				if (delegate == null) {
					// 合并时已确定；这里兜底
					return codec.encode(merged);
				}
				return delegate.encodeForWrite(merged, codec);
			}
		};
	}

	// ============ 仅监听“直接子节点”的辅助方法 ============

	/**
	 * 仅当 path 形如 basePath + "/" + child（且 child 中不再包含 "/"）时返回 true。
	 * 用于限制监听范围到“一层子节点”。
	 */
	private boolean isDirectChild(String path) {
		if (path == null)
			return false;
		String bp = pathPolicy.basePath();
		if (bp == null || bp.isEmpty())
			return false;

		// 规范化：去掉 basePath 尾部的 "/"
		if (bp.length() > 1 && bp.endsWith("/")) {
			bp = bp.substring(0, bp.length() - 1);
		}
		if (!path.startsWith(bp)) {
			return false;
		}
		if (path.length() == bp.length()) {
			// path == basePath -> 不是子节点
			return false;
		}
		if (path.charAt(bp.length()) != '/') {
			// 例如 bp=/a/b 但 path=/a/bb...（前缀碰撞）
			return false;
		}
		// remain 形如 child 或 child/xxx
		String remain = path.substring(bp.length() + 1);
		return remain.length() > 0 && !remain.contains("/");
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
		private MergePolicy<T> mergePolicy; // 可选，显式策略优先

		public Builder() {
		}

		public CuratorFramework getClient() {
			return client;
		}

		public PathPolicy getPathPolicy() {
			return pathPolicy;
		}

		public ValueCodec<T> getCodec() {
			return codec;
		}

		public IdExtractor<T> getIdExtractor() {
			return idExtractor;
		}

		public KeyAdapter<K> getKeyAdapter() {
			return keyAdapter;
		}

		public MergePolicy<T> getMergePolicy() {
			return mergePolicy;
		}

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

		public Builder<K, T> mergePolicy(MergePolicy<T> mergePolicy) {
			this.mergePolicy = mergePolicy;
			return this;
		}

		public ZkBackedCache<K, T> build() {
			if (this.client == null) {
				// 默认复用 ZkHelper.curator
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