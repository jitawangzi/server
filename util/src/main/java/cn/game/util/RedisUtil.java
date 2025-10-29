package cn.game.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
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


/**
 * Redisson操作工具类，封装常用方法
 * 
 * 后续看看增加Vert.x的Redis客户端，回调线程更自然。 
 * 2021年3月11日 下午3:07:23
 * @author SYQ
 */
public class RedisUtil {

	private static Logger logger = LoggerFactory.getLogger(RedisUtil.class);

	static final String configFile = System.getProperty("redisson", "redisson.yaml");

	private static RedissonClient redis;

	private RedisUtil() {

	}

	static {
		try {
			init();
		} catch (IOException e) {
			logger.error("Redisson 初始化失败", e);
			throw new ExceptionInInitializerError("Redisson 初始化失败: " + e.getMessage());
		}
	}

	/**
	 * 使用高级功能如集合等等，可以获取redis实例进行操作，一般的存储读取使用封装好的方法
	 * @return
	 */
	public static RedissonClient getRedis() {
		return redis;
	}

	private static void init() throws IOException {

		Config redisConfig = ConfigService.getConfig("redisson");
		String content = redisConfig.getProperty("redisson", "");
		org.redisson.config.Config config = org.redisson.config.Config.fromYAML(content);
		redis = Redisson.create(config);

		logger.info("=== Redisson Connection Test ===\\n");
		// 1. 配置信息
		printConfig(config);

		// 2. 基本操作测试
		testBasicOps(redis);
		// 定期打印连接池状态
//	    ClusterServersConfig clusterConfig = redis.getConfig().useClusterServers();
//	    logger.info("Master pool size: {}, min idle: {}", 
//	        clusterConfig.getMasterConnectionPoolSize(),
//	        clusterConfig.getMasterConnectionMinimumIdleSize());

	}

	/**
	 * 异步设置值
	 * @param key
	 * @param value
	 * @return 
	 */
	public static <V> RFuture<Void> setAsync(String key, V value) {
		RBucket<V> bucket = redis.getBucket(key);
		RFuture<Void> futrue = bucket.setAsync(value);
		futrue.onComplete((k, v) -> {
			if (v != null) {
				v.printStackTrace();
				logger.error("redisson set error " + "key" + key + "value " + value, v);
			}
		});
		return futrue;
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

	/**
	 * 同步设置，带过期时间,key不存在才设置
	 * @param key
	 * @param value
	 * @param expire
	 * @param timeUnit
	 * @return 
	 */
	public static <V> boolean trySet(String key, V value, int expire, TimeUnit timeUnit) {
		RBucket<V> bucket = redis.getBucket(key);
		return bucket.trySet(value, expire, timeUnit);
	}
	/** 
	 * 同步设置，key不存在才设置
	 * @param <V>
	 * @param key
	 * @param value
	 * @return
	 */
	public static <V> boolean trySet(String key, V value) {
		RBucket<V> bucket = redis.getBucket(key);
		return bucket.trySet(value);
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

	public static <V> boolean delete(String key) {
		RBucket<V> bucket = redis.getBucket(key);
		return bucket.delete();
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
	
	private static void printConfig(org.redisson.config.Config config) {
        System.out.println("--- Configuration ---");
        if (config.isClusterConfig()) {
            System.out.println("Mode: CLUSTER");
            System.out.println("Nodes: " + config.useClusterServers().getNodeAddresses());
            System.out.println("Master pool: " + config.useClusterServers().getMasterConnectionPoolSize());
            System.out.println("Slave pool: " + config.useClusterServers().getSlaveConnectionPoolSize());
            System.out.println("Read mode: " + config.useClusterServers().getReadMode());
        } else {
            System.out.println("Mode: SINGLE");
            System.out.println("Address: " + config.useSingleServer().getAddress());
            System.out.println("Pool size: " + config.useSingleServer().getConnectionPoolSize());
        }
        System.out.println();
    }
    
    private static void testBasicOps(RedissonClient redisson) {
        System.out.println("--- Basic Operations ---");
        
        String testKey = "test_" + System.currentTimeMillis();
        RBucket<String> bucket = redisson.getBucket(testKey);
        
        // 写入
        bucket.set("Hello Redisson 3.52.0");
        System.out.println("✓ Write successful");
        
        // 读取
        String value = bucket.get();
        System.out.println("✓ Read successful: " + value);
        
        // 删除
        bucket.delete();
        System.out.println("✓ Delete successful\n");
    }
    
    private static void stressTest(RedissonClient redisson, int usersPerSecond, int opsPerUser) 
            throws InterruptedException {
        
        System.out.println("--- Stress Test ---");
        System.out.println("Simulating: " + usersPerSecond + " users/sec × " + opsPerUser + " ops/user");
        
        ExecutorService executor = Executors.newFixedThreadPool(50);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        int totalUsers = usersPerSecond * 5; // 测试5秒
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < totalUsers; i++) {
            final int userId = i;
            
            executor.submit(() -> {
                try {
                    // 模拟每个用户的多次操作
                    for (int op = 0; op < opsPerUser; op++) {
                        String key = "PLAYER_SERVER_ID_" + userId;
                        RBucket<String> bucket = redisson.getBucket(key);
                        bucket.set("server_xy_game_1");
                        bucket.get();
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Error for user " + userId + ": " + e.getMessage());
                }
            });
            
            // 控制速率：每秒 usersPerSecond 个用户
            if ((i + 1) % usersPerSecond == 0) {
                Thread.sleep(1000);
            }
        }
        
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        
        long duration = System.currentTimeMillis() - startTime;
        int totalOps = totalUsers * opsPerUser;
        
        System.out.println("\nResults:");
        System.out.println("  Total users: " + totalUsers);
        System.out.println("  Total operations: " + totalOps);
        System.out.println("  Success: " + successCount.get());
        System.out.println("  Errors: " + errorCount.get());
        System.out.println("  Duration: " + duration + "ms");
        System.out.println("  QPS: " + (totalOps * 1000 / duration));
        
        if (errorCount.get() > 0) {
            System.err.println("\n✗ Test failed with " + errorCount.get() + " errors!");
        } else {
            System.out.println("\n✓ Stress test passed!");
        }
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
