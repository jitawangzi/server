package cn.game.core.cache;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cn.game.util.RedisUtil;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**    
 * redis本地缓存，加快redis访问速度，通常缓存一些显示用的数据。 
 * 一般按照时间自然过期，简单维持缓存数据一致性。 
 * 2024年9月4日 下午4:19:13
 * @author SYQ
 */
public class RedisLocalCache {

	private final long expirationTimeInMinutes = 10;
	private final Cache<String, Object> cache = CacheBuilder.newBuilder().expireAfterWrite(expirationTimeInMinutes, TimeUnit.MINUTES).build();
	private final RedissonClient redissonClient = RedisUtil.getRedis();
	private final ConcurrentHashMap<String, Future<Object>> loadingFutures = new ConcurrentHashMap<>();;
	private static RedisLocalCache instance = new RedisLocalCache();

	private RedisLocalCache() {
	}

	public static RedisLocalCache getInstance() {
		return instance;
	}

	/** 
	 * 同步get方法
	 * @param <T>
	 * @param key
	 * @return
	 */
	public <T> T get(String key) {
		T value = (T) cache.getIfPresent(key);
		if (value != null) {
			return value;
		}
		try {
			return loadSync(key);
		} catch (Exception e) {
			// 处理异常，可能需要返回null或抛出自定义异常
			return null;
		}
	}

	/** 
	 * 异步get方法
	 * @param key
	 * @return
	 */
	public <T> Future<T> getAsync(String key) {
		T value = (T) cache.getIfPresent(key);
		if (value != null) {
			return Future.succeededFuture(value);
		}

		return loadAsync(key);
	}

	private <T> T loadSync(String key) throws Exception {
		Future future = loadingFutures.computeIfAbsent(key, k -> {
			Promise promise = Promise.promise();
			T value = getFromRedis(key);
			if (value != null) {
                cache.put(key, value);
            }
			promise.complete(value);
			return promise.future();
		});

		try {
			return (T) future.toCompletionStage().toCompletableFuture().get();
		} finally {
			loadingFutures.remove(key);
        }
    }

	private <T> Future<T> loadAsync(String key) {
		return (Future<T>) loadingFutures.computeIfAbsent(key, k -> {
			Promise<T> promise = Promise.promise();
			getFromRedisAsync(key).onComplete(ar -> {
				if (ar.succeeded()) {
					T value = (T) ar.result();
					if (value != null) {
						cache.put(key, value);
					}
					promise.complete(value);
				} else {
					promise.fail(ar.cause());
				}
				loadingFutures.remove(key);
			});
			return (Future<Object>) promise.future();
		});
	}

	private <T> T getFromRedis(String key) {
		RBucket<T> bucket = redissonClient.getBucket(key);
		return bucket.get();
	}

	private <T> Future<T> getFromRedisAsync(String key) {
		RBucket<T> bucket = redissonClient.getBucket(key);
		return Future.fromCompletionStage(bucket.getAsync());
	}

	/** 
	 * 同步更新或添加缓存项
	 * @param key
	 * @param value
	 */
	public <T> void put(String key, T value) {
		cache.put(key, value);
		redissonClient.getBucket(key).set(value);
	}

	/** 
	 * 异步更新或添加缓存项
	 * @param key
	 * @param value
	 * @return
	 */
	public <T> Future<Void> putAsync(String key, T value) {
		Promise<Void> promise = Promise.promise();
		cache.put(key, value);
		redissonClient.getBucket(key).setAsync(value).whenComplete((result, throwable) -> {
			if (throwable != null) {
				promise.fail(throwable);
			} else {
				promise.complete();
			}
		});
		return promise.future();
	}

	/** 
	 * 从Redis批量获取数据（同步）
	 * @param keys
	 * @return
	 */
	@Deprecated
	private <T> Map<String, T> getMultiFromRedis(List<String> keys) {
		return keys.stream().collect(Collectors.toMap(key -> key, key -> redissonClient.<T>getBucket(key).get(), (v1, v2) -> v1, LinkedHashMap::new));
	}

	/** 
	 * 从Redis批量获取数据（异步）
	 * @param keys
	 * @return
	 */
	@Deprecated
	public <T> Future<Map<String, T>> getMultiFromRedisAsync(List<String> keys) {
		Promise<Map<String, T>> promise = Promise.promise();

		List<Future<Map.Entry<String, T>>> futures = keys.stream().map(key -> {
			Promise<Map.Entry<String, T>> p = Promise.promise();
			redissonClient.<T>getBucket(key).getAsync().whenComplete((result, throwable) -> {
				if (throwable != null) {
					p.fail(throwable);
				} else {
					p.complete(new AbstractMap.SimpleEntry<>(key, result));
				}
			});
			return p.future();
		}).collect(Collectors.toList());

		CompositeFuture.all(new ArrayList<>(futures)).onComplete(ar -> {
			if (ar.succeeded()) {
				Map<String, T> result = futures
						.stream()
						.map(Future::result)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
				promise.complete(result);
			} else {
				promise.fail(ar.cause());
			}
		});

		return promise.future();
	}

	/** 
	 * 可变参数的同步批量获取方法
	 * @param keys
	 * @return
	 */
	public <T> List<T> multiGet(String... keys) {
		return multiGet(Arrays.asList(keys));
	}

	/** 
	 * 可变参数的同步批量获取方法
	 * @param keys
	 * @return
	 */
	public <T> List<T> multiGet(CacheType cacheType, String... keys) {
		List<String> list = new ArrayList<>(keys.length);
		for (String key : keys) {
			list.add(cacheType.key(key));
		}
		return multiGet(list);
	}

	/** 
	 * 可变参数的异步批量获取方法
	 * @param keys
	 * @return
	 */
	public <T> Future<List<T>> multiGetAsync(String... keys) {
		return multiGetAsync(Arrays.asList(keys));
	}
	public <T> Future<List<T>> multiGetAsync(CacheType cacheType, String... keys) {
		List<String> list = new ArrayList<>(keys.length);
		for (String key : keys) {
			list.add(cacheType.key(key));
		}
		return multiGetAsync(list);
	}

	/** 
	 * 同步批量获取方法
	 * @param <T>
	 * @param keys
	 * @return
	 */
	public <T> List<T> multiGet(List<String> keys) {
		List<T> result = new ArrayList<>(Collections.nCopies(keys.size(), null));
		List<String> missingKeys = new ArrayList<>();
		Map<String, Integer> keyIndexMap = new HashMap<>();

		// 先从本地缓存中获取
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			T value = (T) cache.getIfPresent(key);
			if (value != null) {
				result.set(i, value);
			} else {
				missingKeys.add(key);
				keyIndexMap.put(key, i);
			}
		}

		// 如果有未命中的键，从Redis批量获取
		if (!missingKeys.isEmpty()) {
			RBatch batch = redissonClient.createBatch();
			Map<String, RFuture<T>> futures = new HashMap<>();

			for (String key : missingKeys) {
				futures.put(key, batch.<T>getBucket(key).getAsync());
			}

			batch.execute();

			for (Map.Entry<String, RFuture<T>> entry : futures.entrySet()) {
				String key = entry.getKey();
				try {
					T value = entry.getValue().get();
					int index = keyIndexMap.get(key);
					result.set(index, value);
					if (value != null) {
						cache.put(key, value);
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed to get value for key: " + key, e);
				}
			}
		}

		return result;
	}

	public <T> Future<List<T>> multiGetAsync(CacheType cacheType, List<String> keys) {
		List<String> list = new ArrayList<>(keys.size());
		for (String key : keys) {
			list.add(cacheType.key(key));
		}
		return multiGetAsync(list);
	}

	/** 
	 * 异步批量获取方法
	 * @param <T>
	 * @param keys
	 * @return
	 */
	public <T> Future<List<T>> multiGetAsync(List<String> keys) {
		Promise<List<T>> promise = Promise.promise();
		List<T> result = new ArrayList<>(Collections.nCopies(keys.size(), null));
		List<String> missingKeys = new ArrayList<>();
		Map<String, Integer> keyIndexMap = new HashMap<>();

		// 先从本地缓存中获取
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			T value = (T) cache.getIfPresent(key);
			if (value != null) {
				result.set(i, value);
			} else {
				missingKeys.add(key);
				keyIndexMap.put(key, i);
			}
		}

		// 如果所有键都在本地缓存中找到，直接返回结果
		if (missingKeys.isEmpty()) {
			return Future.succeededFuture(result);
		}

		// 异步从Redis批量获取未命中的键
		RBatch batch = redissonClient.createBatch();
		Map<String, RFuture<T>> futures = new HashMap<>();

		for (String key : missingKeys) {
			futures.put(key, batch.<T>getBucket(key).getAsync());
		}

		RFuture<BatchResult<?>> batchFuture = batch.executeAsync();
		batchFuture.whenComplete((batchResult, throwable) -> {
			if (throwable != null) {
				promise.fail(throwable);
			} else {
				for (Map.Entry<String, RFuture<T>> entry : futures.entrySet()) {
					String key = entry.getKey();
					T value = entry.getValue().getNow();
					int index = keyIndexMap.get(key);
					result.set(index, value);
					if (value != null) {
						cache.put(key, value);
					}
				}
				promise.complete(result);
			}
		});

		return promise.future();
	}
	

}