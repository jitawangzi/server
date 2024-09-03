package cn.game.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.redisson.Redisson;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;

import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * Redisson操作工具类，封装常用方法
 * 2021年3月11日 下午3:07:23
 * @author SYQ
 */
public class RedisUtil {

	private static Logger logger = LoggerFactory.getLogger(RedisUtil.class);

	static final String configFile = System.getProperty("redisson", "redisson.yaml");

	private static RedisUtil instance = new RedisUtil();
	private static RedissonClient redis;

	private RedisUtil() {

	}

	public static RedisUtil getInstance() {
		return instance;
	}

	/**
	 * 使用高级功能如集合等等，可以获取redis实例进行操作，一般的存储读取使用封装好的方法
	 * @return
	 */
	public static RedissonClient getRedis() {
		return redis;
	}

//	static {
//		try {
//			org.redisson.config.Config config = org.redisson.config.Config.fromYAML(RedissonUtil.class.getClassLoader().getResource(
//					configFile));
//			redis = Redisson.create(config);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	public void init() throws IOException {

		Config redisConfig = ConfigService.getConfig("redisson");
		String content = redisConfig.getProperty("redisson", "");
		org.redisson.config.Config config = org.redisson.config.Config.fromYAML(content);
		redis = Redisson.create(config);

	}

	/**
	 * 异步设置值
	 * @param key
	 * @param value
	 */
	public static <V> void setAsync(String key, V value) {
		RBucket<V> bucket = redis.getBucket(key);
		RFuture<Void> futrue = bucket.setAsync(value);
		futrue.onComplete((k, v) -> {
			if (v != null) {
				v.printStackTrace();
				logger.error("redisson set error " + "key" + key + "value " + value, v);
			}
		});
	}

	/**
	 * 异步设置，带过期时间
	 * @param key
	 * @param value
	 * @param expire
	 * @param timeUnit
	 * @return 
	 * @return 
	 */
	public static <V> RFuture<Void> setAsync(String key, V value, int expire, TimeUnit timeUnit) {
		RBucket<V> bucket = redis.getBucket(key);
		RFuture<Void> futrue = bucket.setAsync(value, expire, timeUnit);
		futrue.onComplete((k, v) -> {
			if (v != null) {
				v.printStackTrace();
				logger.error("redisson set error " + "key" + key + "value " + value, v);
			}
		});
		return futrue;
	}
	
	/**
	 * 异步设置，带过期时间,key不存在才设置
	 * @param key
	 * @param value
	 * @param expire
	 * @param timeUnit
	 * @return 
	 */
	public static <V> RFuture<Boolean> trySetAsync(String key, V value, int expire, TimeUnit timeUnit) {
		RBucket<V> bucket = redis.getBucket(key);
		return bucket.trySetAsync(value, expire, timeUnit);
	}

	public static <V> RFuture<Boolean> compareAndSetAsync(String key, V expect, V update) {
		RBucket<V> bucket = redis.getBucket(key);
		return bucket.compareAndSetAsync(expect, update);
	}

	/**
	 * 设置成功后执行逻辑
	 * @param <V>
	 * @param key
	 * @param value
	 * @param consumer
	 */
	public static <V> void setAsyncAndRun(String key, V value, Consumer<Boolean> consumer) {
		RBucket<V> bucket = redis.getBucket(key);
		RFuture<Void> futrue = bucket.setAsync(value);
		futrue.onComplete((k, v) -> {
			if (v != null) {
				v.printStackTrace();
				logger.error("redisson set error " + "key" + key + "value " + value, v);
			}
			consumer.accept(futrue.isSuccess());
		});
	}

	/**
	 * 同步设置值( 尽量用异步方法)
	 * @param key
	 * @param value
	 */
	public static <V> void set(String key, V value) {
		RBucket<V> bucket = redis.getBucket(key);
		bucket.set(value);
	}
	/**
	 * 同步设置值,带过期时间(尽量用异步方法)
	 * @param <V>
	 * @param key
	 * @param value
	 * @param expire
	 * @param timeUnit
	 */
	public static <V> void set(String key, V value, int expire, TimeUnit timeUnit) {
		RBucket<V> bucket = redis.getBucket(key);
		bucket.set(value, expire, timeUnit);
	}

	/**
	 * 异步获取所有key后，执行业务,调用这个方法时，至少传入两个key
	 * @param consumer
	 * @param keys
	 */
	public static void getAndRunAsyncBatch(Consumer<List<?>> consumer, String... keys) {

		RFuture<BatchResult<?>> future = getAsyncFutureBatch(keys);
		future.onComplete((r, e) -> {
			if (e != null) {
				logger.error("get keys[{}] error", keys, e);
			}
			try {
				consumer.accept(r == null || r.getResponses() == null ? new ArrayList<>() : r.getResponses());
			} catch (Exception ee) {
				logger.error(" getAndRunAsyncBatch err , key[{}]  ", keys, ee);
			}
		});
	}
	/**
	 * 异步获取key后，执行业务
	 * @param <V>
	 * @param key
	 * @param consumer
	 */
	public static <V> void getAndRunAsync(String key, Consumer<V> consumer) {

		RFuture<V> future = getAsync(key);
		future.onComplete((r, e) -> {
			if (e != null) {
				logger.error("get key[{}] error [{}]", key, e);
			}
			try {
				consumer.accept(r);
			} catch (Exception ee) {
				logger.error(" getAndRunAsyncBatch err , key[{}] [{}] ", key, ee);
			}
		});
	}
	/**
	 * 异步批量设置key
	 * @param keys
	 * @param values
	 */
	public static void setAsyncBatch(List<String> keys, List<?> values) {

		RBatch batch = redis.createBatch();

		for (int i = 0; i < keys.size(); i++) {
			batch.getBucket(keys.get(i)).setAsync(values.get(i));
		}
		RFuture<BatchResult<?>> executeAsync = batch.executeAsync();
		executeAsync.onComplete((r, e) -> {
			if (e != null) {
				logger.error("set keys[{}]  error [{}]", keys, e);
			}
		});

	}
	/**
	 * 异步批量设置key，返回Futrue
	 * @param keys
	 * @param values
	 * @return
	 */
	public static RFuture<BatchResult<?>> setAsyncBatch2(List<String> keys, List<?> values) {

		RBatch batch = redis.createBatch();

		for (int i = 0; i < keys.size(); i++) {
			batch.getBucket(keys.get(i)).setAsync(values.get(i));
		}
		RFuture<BatchResult<?>> executeAsync = batch.executeAsync();
		return executeAsync;

	}
	
	public static <V> RFuture<Boolean> deleteAsync(String key) {
		RBucket<V> bucket = redis.getBucket(key);
		return bucket.deleteAsync();
	}

	/**
	 * 获取keys的异步查询结果
	 * @param keys
	 * @return
	 */
	private static RFuture<BatchResult<?>> getAsyncFutureBatch(String... keys) {

		if (keys == null || keys.length == 0) { 
			throw new IllegalArgumentException(" keys is null"); 
		}
		RBatch batch = redis.createBatch();
		for (int i = 0; i < keys.length; i++) {
			batch.getBucket(keys[i]).getAsync();
		}
		return batch.executeAsync();
	}
	/**
	 * 异步获取key
	 * @param <V>
	 * @param key
	 * @return
	 */
	public static <V> RFuture<V> getAsync(String key) {
		RBucket<V> bucket = redis.getBucket(key);
		return bucket.getAsync();
	}

	/**
	 * 同步方法，等待异步查询结果并消费，如果没有数据，结果是空的ArrayList
	 * @param action
	 * @param keys
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static void getAndWaitAsyncBatch(Consumer<List<?>> action, String... keys) throws InterruptedException,
			ExecutionException, TimeoutException {
		action.accept(getAndWaitAsyncResult(keys));
	}
	/**
	 * 等待异步批量查询结果
	 * @param keys
	 * @return 非空
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static List<?> getAndWaitAsyncResult(String... keys) throws InterruptedException, ExecutionException, TimeoutException {
		
		RFuture<BatchResult<?>> batcchFuture = getAsyncFutureBatch(keys);
		BatchResult<?> batchResult = batcchFuture.get(LockUtil.waitTime, TimeUnit.SECONDS);
		return batchResult == null || batchResult.getResponses() == null ? new ArrayList<>() : batchResult.getResponses();
	}

	/**
	 * 同步获取key的值
	 * @param <V>
	 * @param key
	 * @return
	 */
	public static <V> V get(String key) {
		RBucket<V> bucket = redis.getBucket(key);
		return bucket.get();
	}

	public static <T> Future<T> toVertxFuture(RFuture<T> rFuture) {
		Promise<T> promise = Promise.promise();
		rFuture.onComplete((result, throwable) -> {
			if (throwable != null) {
				promise.fail(throwable);
			} else {
				promise.complete(result);
			}
		});
		return promise.future();
	}

	public static void main(String args[]) throws Exception {

		set("a", 1);
		set("b", 2);
		set("c", 3);
		
//		setBatchAsync(List.of("a", "b", "c"), List.of("aa", "bb", "cc"));
//		getBatchAsync(List.of("a", "b", "c"));
		getAndRunAsyncBatch(r -> {
			
			for (Object string : r) {
				System.out.println(string);
			}
		}, "a", "a", "c");


//		getAndRunAsync("ff", r -> {
//			System.out.println(2 / 0);
//		});
//		
		
		redis.shutdown();
		Thread.currentThread().join();

	}
}
