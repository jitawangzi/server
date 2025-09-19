package cn.game.core.cache;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import cn.game.core.cache.CacheDataType.CacheBackend;
import cn.game.core.util.AsyncUtils;
import cn.game.util.RedisUtil;
import io.vertx.core.Future;

/**
 * 统一的本地缓存管理器（基于 Caffeine）。
 * - 每个 CacheDataType 一个 AsyncCache（分片）
 * - 单 key/批量 key 的同步与异步 API
 * - 可注册默认异步 loader（单/批）
 * - REDIS 类型提供写回 / 删除 / 存在性检查
 * 
 *  使用建议：
 * - 通常不直接依赖此类，在 GameCacheService 中注册各业务类型的 loader 并对外提供门面方法。
 */
public class SimpleCacheManager {
	private static final Logger log = LoggerFactory.getLogger(SimpleCacheManager.class);

	private static volatile SimpleCacheManager instance;
	private static final Object INIT_LOCK = new Object();

	// 每个类型一个 Caffeine 异步缓存
	private final EnumMap<CacheDataType, AsyncCache<String, Object>> asyncCaches = new EnumMap<>(CacheDataType.class);
	// 对应的同步视图
	private final EnumMap<CacheDataType, Cache<String, Object>> syncCaches = new EnumMap<>(CacheDataType.class);

	// 默认单 key 异步 loader
	private final EnumMap<CacheDataType, Function<String, CompletionStage<Object>>> asyncLoaders = new EnumMap<>(CacheDataType.class);
	// 默认批量异步 loader（按 Set keys 返回 Map）
	private final EnumMap<CacheDataType, Function<Set<String>, CompletionStage<Map<String, Object>>>> bulkAsyncLoaders = new EnumMap<>(
			CacheDataType.class);

	// 异步执行器（把同步 loader 包装为异步）
	private final Executor asyncExecutor;

	// Redis 客户端
	private final RedissonClient redisson;

	// 缓存空值的哨兵对象
	private enum NullValue implements Serializable {
		INSTANCE
	}

	private SimpleCacheManager() {
		this.redisson = RedisUtil.getRedis();
		this.asyncExecutor = CompletableFuture.delayedExecutor(0, TimeUnit.MILLISECONDS, ForkJoinPool.commonPool());

		// 初始化各类型缓存
		for (CacheDataType type : CacheDataType.values()) {
			AsyncCache<String, Object> ac = Caffeine.newBuilder()
					.expireAfterWrite(Duration.ofSeconds(type.getTtlSeconds()))
					.maximumSize(type.getMaximumSize())
					.recordStats()
					.buildAsync();
			asyncCaches.put(type, ac);
			syncCaches.put(type, ac.synchronous());
		}
		// 注册 REDIS 默认 loader
		registerRedisDefaults();
		log.info("SimpleCacheManager initialized with {} cache types", CacheDataType.values().length);
	}

	public static SimpleCacheManager getInstance() {
		if (instance == null) {
			synchronized (INIT_LOCK) {
				if (instance == null) {
					instance = new SimpleCacheManager();
				}
			}
		}
		return instance;
	}

	// ============== 注册默认 Loader ==============

	public void registerLoader(CacheDataType type, Function<String, CompletionStage<Object>> singleAsyncLoader,
			Function<Set<String>, CompletionStage<Map<String, Object>>> bulkAsyncLoader) {
		if (singleAsyncLoader != null) {
			asyncLoaders.put(type, singleAsyncLoader);
		}
		if (bulkAsyncLoader != null) {
			bulkAsyncLoaders.put(type, bulkAsyncLoader);
		}
		log.info("Registered loaders for type={}, single={}, bulk={}", type.getPrefix(), singleAsyncLoader != null,
				bulkAsyncLoader != null);
	}

	private void registerRedisDefaults() {
		// 单 key
		asyncLoaders.put(CacheDataType.REDIS_CACHE, key -> {
			RBucket<Object> bucket = redisson.getBucket(key);
			return bucket.getAsync().toCompletableFuture().thenApply(SimpleCacheManager::wrapNull);
		});
		// 批量
		bulkAsyncLoaders.put(CacheDataType.REDIS_CACHE, keys -> {
			if (keys == null || keys.isEmpty()) {
				return CompletableFuture.completedFuture(Collections.emptyMap());
			}
			RBatch batch = redisson.createBatch();
			Map<String, RFuture<Object>> rfutures = new LinkedHashMap<>();
			for (String key : keys) {
				rfutures.put(key, batch.getBucket(key).getAsync());
			}
			return batch.executeAsync().toCompletableFuture().thenApply(br -> {
				Map<String, Object> map = new LinkedHashMap<>();
				for (Map.Entry<String, RFuture<Object>> e : rfutures.entrySet()) {
					map.put(e.getKey(), wrapNull(e.getValue().getNow()));
				}
				return map;
			});
		});
	}

	// ============== 工具：null 包装/解包 ==============

	private static Object wrapNull(Object v) {
		return v == null ? NullValue.INSTANCE : v;
	}

	@SuppressWarnings("unchecked")
	private static <T> T unwrapNull(Object v) {
		return v == NullValue.INSTANCE ? null : (T) v;
	}

	private static <T> T join(CompletionStage<T> stage) {
		try {
			// 确保在非事件循环线程调用
			AsyncUtils.checkEventLoop();
			return stage.toCompletableFuture().get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ============== 同步 API（按次传 loader） ==============

	public <T> T get(CacheDataType type, String key, Function<String, T> loader) {
		AsyncUtils.checkEventLoop();

		Objects.requireNonNull(type);
		Objects.requireNonNull(key);
		Objects.requireNonNull(loader);
		Cache<String, Object> cache = syncCaches.get(type);
		Object ret = cache.get(key, k -> wrapNull(loader.apply(k)));
		return unwrapNull(ret);
	}

	public <T> List<T> multiGet(CacheDataType type, List<String> keys, Function<List<String>, List<T>> bulkLoader,
			Function<String, T> singleLoader) {
		Objects.requireNonNull(type);
		if (keys == null || keys.isEmpty())
			return Collections.emptyList();

		Cache<String, Object> cache = syncCaches.get(type);
		Map<String, Object> map = cache.asMap();

		// 1) 命中
		List<T> result = new ArrayList<>(keys.size());
		List<String> missing = new ArrayList<>();
		for (String k : keys) {
			Object v = map.get(k);
			if (v != null) {
				result.add(SimpleCacheManager.<T>unwrapNull(v));
			} else {
				result.add(null); // 占位
				missing.add(k);
			}
		}
		if (missing.isEmpty()) {
			return result;
		}

		// 2) 加载缺失
		if (bulkLoader != null) {
			List<T> loaded = bulkLoader.apply(missing);
			for (int i = 0; i < missing.size(); i++) {
				String mk = missing.get(i);
				T mv = (loaded != null && i < loaded.size()) ? loaded.get(i) : null;
				cache.put(mk, wrapNull(mv));
			}
		} else {
			for (String mk : missing) {
				T mv = singleLoader.apply(mk);
				cache.put(mk, wrapNull(mv));
			}
		}

		// 3) 回填结果
		for (int i = 0; i < keys.size(); i++) {
			if (result.get(i) == null) {
				Object v = map.get(keys.get(i));
				result.set(i, SimpleCacheManager.<T>unwrapNull(v));
			}
		}
		return result;
	}

	// ============== 异步 API（按次传 loader） ==============

	public <T> Future<T> getAsync(CacheDataType type, String key, Function<String, T> loader) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(key);
		Objects.requireNonNull(loader);
		AsyncCache<String, Object> cache = asyncCaches.get(type);
		CompletableFuture<Object> cf = cache.get(key, k -> CompletableFuture.supplyAsync(() -> wrapNull(loader.apply(k)), asyncExecutor));
		return Future.fromCompletionStage(cf.thenApply(SimpleCacheManager::unwrapNull));
	}

	public <T> Future<List<T>> multiGetAsync(CacheDataType type, List<String> keys, Function<Set<String>, Map<String, T>> bulkLoaderSync,
			Function<String, T> singleLoader) {
		Objects.requireNonNull(type);
		if (keys == null || keys.isEmpty())
			return Future.succeededFuture(Collections.emptyList());

		Cache<String, Object> cacheSync = syncCaches.get(type);
		Map<String, Object> map = cacheSync.asMap();

		// 1) 命中
		List<T> result = new ArrayList<>(keys.size());
		Set<String> missing = new LinkedHashSet<>();
		for (String k : keys) {
			Object v = map.get(k);
			if (v != null) {
				result.add(SimpleCacheManager.<T>unwrapNull(v));
			} else {
				result.add(null);
				missing.add(k);
			}
		}
		if (missing.isEmpty()) {
			return Future.succeededFuture(result);
		}

		// 2) 异步加载缺失
		CompletableFuture<Void> loadFuture;
		if (bulkLoaderSync != null) {
			loadFuture = CompletableFuture.supplyAsync(() -> bulkLoaderSync.apply(missing), asyncExecutor).thenAccept(loaded -> {
				for (String mk : missing) {
					T mv = loaded != null ? (T) loaded.get(mk) : null;
					cacheSync.put(mk, wrapNull(mv));
				}
			});
		} else {
			List<CompletableFuture<Void>> futures = new ArrayList<>();
			for (String mk : missing) {
				futures.add(CompletableFuture.supplyAsync(() -> singleLoader.apply(mk), asyncExecutor)
						.thenAccept(v -> cacheSync.put(mk, wrapNull(v))));
			}
			loadFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		}

		// 3) 组装最终结果
		CompletableFuture<List<T>> out = loadFuture.thenApply(v -> {
			for (int i = 0; i < keys.size(); i++) {
				if (result.get(i) == null) {
					Object nv = map.get(keys.get(i));
					result.set(i, SimpleCacheManager.<T>unwrapNull(nv));
				}
			}
			return result;
		});
		return Future.fromCompletionStage(out);
	}

	// ============== 使用已注册默认 loader 的便捷 API ==============

	public <T> T get(CacheDataType type, String key) {
		AsyncUtils.checkEventLoop();

		Function<String, CompletionStage<Object>> loader = asyncLoaders.get(type);
		if (loader == null) {
			throw new IllegalStateException("No default async loader registered for type=" + type.getPrefix());
		}
		Cache<String, Object> cache = syncCaches.get(type);
		Object ret = cache.get(key, k -> wrapNull(join(loader.apply(k))));
		return unwrapNull(ret);
	}

	public <T> Future<T> getAsync(CacheDataType type, String key) {
		Function<String, CompletionStage<Object>> loader = asyncLoaders.get(type);
		if (loader == null) {
			return Future.failedFuture(new IllegalStateException("No default async loader for type=" + type.getPrefix()));
		}
		AsyncCache<String, Object> cache = asyncCaches.get(type);
		CompletableFuture<Object> cf = cache.get(key, k -> loader.apply(k).toCompletableFuture());
		return Future.fromCompletionStage(cf.thenApply(SimpleCacheManager::unwrapNull));
	}

	public <T> List<T> multiGet(CacheDataType type, List<String> keys) {
		if (keys == null || keys.isEmpty())
			return Collections.emptyList();
		Cache<String, Object> cache = syncCaches.get(type);
		Map<String, Object> map = cache.asMap();

		Function<Set<String>, CompletionStage<Map<String, Object>>> bulk = bulkAsyncLoaders.get(type);
		Function<String, CompletionStage<Object>> single = asyncLoaders.get(type);

		// 命中
		List<T> result = new ArrayList<>(keys.size());
		Set<String> missing = new LinkedHashSet<>();
		for (String k : keys) {
			Object v = map.get(k);
			if (v != null) {
				result.add(SimpleCacheManager.<T>unwrapNull(v));
			} else {
				result.add(null);
				missing.add(k);
			}
		}
		if (missing.isEmpty()) {
			return result;
		}

		// 加载缺失（优先批量）
		if (bulk != null) {
			Map<String, Object> loaded = join(bulk.apply(missing));
			if (loaded != null) {
				for (String mk : missing) {
					Object mv = loaded.get(mk);
					cache.put(mk, mv == null ? NullValue.INSTANCE : mv);
				}
			}
		} else if (single != null) {
			for (String mk : missing) {
				Object mv = join(single.apply(mk));
				cache.put(mk, mv == null ? NullValue.INSTANCE : mv);
			}
		} else {
			throw new IllegalStateException("No loader registered for type=" + type.getPrefix());
		}

		// 回填
		for (int i = 0; i < keys.size(); i++) {
			if (result.get(i) == null) {
				Object nv = map.get(keys.get(i));
				result.set(i, SimpleCacheManager.<T>unwrapNull(nv));
			}
		}
		return result;
	}

	public <T> Future<List<T>> multiGetAsync(CacheDataType type, List<String> keys) {
		if (keys == null || keys.isEmpty())
			return Future.succeededFuture(Collections.emptyList());
		Cache<String, Object> cache = syncCaches.get(type);
		Map<String, Object> map = cache.asMap();

		Function<Set<String>, CompletionStage<Map<String, Object>>> bulk = bulkAsyncLoaders.get(type);
		Function<String, CompletionStage<Object>> single = asyncLoaders.get(type);

		// 命中
		List<T> result = new ArrayList<>(keys.size());
		Set<String> missing = new LinkedHashSet<>();
		for (String k : keys) {
			Object v = map.get(k);
			if (v != null) {
				result.add(SimpleCacheManager.<T>unwrapNull(v));
			} else {
				result.add(null);
				missing.add(k);
			}
		}
		if (missing.isEmpty()) {
			return Future.succeededFuture(result);
		}

		CompletableFuture<Void> loadFuture;
		if (bulk != null) {
			loadFuture = bulk.apply(missing).toCompletableFuture().thenAccept(loaded -> {
				if (loaded != null) {
					for (String mk : missing) {
						Object mv = loaded.get(mk);
						cache.put(mk, mv == null ? NullValue.INSTANCE : mv);
					}
				}
			});
		} else if (single != null) {
			List<CompletableFuture<Void>> futures = new ArrayList<>();
			for (String mk : missing) {
				futures.add(single.apply(mk).toCompletableFuture().thenAccept(mv -> {
					cache.put(mk, mv == null ? NullValue.INSTANCE : mv);
				}));
			}
			loadFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		} else {
			return Future.failedFuture(new IllegalStateException("No loader registered for type=" + type.getPrefix()));
		}

		CompletableFuture<List<T>> out = loadFuture.thenApply(v -> {
			for (int i = 0; i < keys.size(); i++) {
				if (result.get(i) == null) {
					Object nv = map.get(keys.get(i));
					result.set(i, SimpleCacheManager.<T>unwrapNull(nv));
				}
			}
			return result;
		});
		return Future.fromCompletionStage(out);
	}

	// ============== put / putAsync（含 REDIS 写回） ==============

	public <T> void put(CacheDataType type, String key, T value) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(key);
		if (type.getBackend() == CacheBackend.REDIS) {
			RBucket<T> bucket = redisson.getBucket(key);
			bucket.set(value);
		}
		syncCaches.get(type).put(key, wrapNull(value));
	}

	public <T> Future<Void> putAsync(CacheDataType type, String key, T value) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(key);
		if (type.getBackend() == CacheBackend.REDIS) {
			RBucket<T> bucket = redisson.getBucket(key);
			CompletableFuture<Void> cf = bucket.setAsync(value)
					.toCompletableFuture()
					.thenAccept(v -> syncCaches.get(type).put(key, wrapNull(value)));
			return Future.fromCompletionStage(cf);
		} else {
			syncCaches.get(type).put(key, wrapNull(value));
			return Future.succeededFuture();
		}
	}

	// ============== 失效 / 删除 / 存在性 ==============

	public void evict(CacheDataType type, String key) {
		asyncCaches.get(type).synchronous().invalidate(key);
	}

	public void evictByType(CacheDataType type) {
		asyncCaches.get(type).synchronous().invalidateAll();
		log.info("Evicted all cache entries for type={}", type.getPrefix());
	}

	public void evictPlayerData(CacheDataType type, long playerId) {
		String pid = String.valueOf(playerId);
		Cache<String, Object> cache = syncCaches.get(type);
		Set<String> keys = new HashSet<>(cache.asMap().keySet());
		int before = keys.size();
		for (String k : keys) {
			if (k.equals(pid) || k.endsWith(":" + pid)) {
				cache.invalidate(k);
			}
		}
		int after = cache.asMap().size();
		log.info("Evict player cache for type={}, playerId={}, before={}, after={}", type.getPrefix(), playerId, before, after);
	}

	public void evictAllPlayerCache(long playerId) {
		for (CacheDataType type : CacheDataType.values()) {
			evictPlayerData(type, playerId);
		}
	}

	public boolean existsRemote(CacheDataType type, String key) {
		if (type.getBackend() != CacheBackend.REDIS)
			return false;
		try {
			return redisson.getBucket(key).isExists();
		} catch (Exception e) {
			log.warn("existsRemote error type={}, key={}", type.getPrefix(), key, e);
			return false;
		}
	}

	public Future<Boolean> deleteRemoteAsync(CacheDataType type, String key) {
		if (type.getBackend() != CacheBackend.REDIS) {
			return Future.succeededFuture(false);
		}
		RFuture<Boolean> rf = RedisUtil.deleteAsync(key);
		return Future.fromCompletionStage(rf.toCompletableFuture())
				.onSuccess(v -> evict(type, key))
				.onFailure(e -> log.warn("deleteRemoteAsync failed type={}, key={}", type.getPrefix(), key, e));
	}

	// ============== 统计与管理 ==============

	public void printStats() {
		for (CacheDataType type : CacheDataType.values()) {
			var sync = asyncCaches.get(type).synchronous();
			var stats = sync.stats();
			String hitRate = String.format("%.2f", stats.hitRate() * 100);
			String avgLoad = String.format("%.2f", stats.averageLoadPenalty() / 1_000_000.0);
			log.info("[CacheStats] type={}, size={}, hitRate={}%, hits={}, misses={}, avgLoad={}ms, evictions={}", type.getPrefix(),
					sync.estimatedSize(), hitRate, stats.hitCount(), stats.missCount(), avgLoad, stats.evictionCount());
		}
	}

	public void clearAll() {
		for (CacheDataType type : CacheDataType.values()) {
			evictByType(type);
		}
		log.info("Cleared all caches");
	}

	public AsyncCache<String, Object> getAsyncCache(CacheDataType type) {
		return asyncCaches.get(type);
	}

	public Cache<String, Object> getSyncCache(CacheDataType type) {
		return syncCaches.get(type);
	}
}