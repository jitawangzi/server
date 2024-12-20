package cn.game.core.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;

import cn.game.util.RedisUtil;

class RedisShardedMapTest {
	private static RedissonClient redisClient;
	private RedisShardedMap<String, String> shardedMap;
	private static final String TEST_MAP_KEY = "test_map";
	private static final int SHARD_COUNT = 4;

	@BeforeAll
	static void setUp() {
//		Config config = new Config();
//		config.useSingleServer().setAddress("redis://localhost:6379").setDatabase(0);
//		redisClient = Redisson.create(config);

		try {
			RedisUtil.getInstance().init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		redisClient = RedisUtil.getRedis();

	}

	@AfterAll
	static void tearDown() {
		redisClient.shutdown();
	}

	@BeforeEach
	void init() {
		shardedMap = new RedisShardedMap<>(redisClient, TEST_MAP_KEY, SHARD_COUNT);
		shardedMap.clear(); // 确保测试开始前map为空
	}

	@Test
	void testBasicOperations() {
		// 测试put和get
		assertNull(shardedMap.put("key1", "value1"));
		assertEquals("value1", shardedMap.get("key1"));

		// 测试更新已存在的值
		assertEquals("value1", shardedMap.put("key1", "value2"));
		assertEquals("value2", shardedMap.get("key1"));

		// 测试getall
		assertEquals(1, shardedMap.getAll().size());
		assertEquals(1, shardedMap.getAllAsync().toCompletableFuture().join() .size());
		// 测试删除
		assertEquals("value2", shardedMap.remove("key1"));
		assertNull(shardedMap.get("key1"));

		// 测试containsKey
		assertFalse(shardedMap.containsKey("key1"));
		shardedMap.put("key1", "value1");
		assertTrue(shardedMap.containsKey("key1"));
	}

	@Test
	void testBatchOperations() {
		// 准备测试数据
		Map<String, String> testData = IntStream.range(0, 10).boxed().collect(Collectors.toMap(i -> "key" + i, i -> "value" + i));

		// 测试批量put
		shardedMap.putAll(testData);

		// 测试批量get
		Map<String, String> retrieved = shardedMap.getAll(testData.keySet());
		assertEquals(testData, retrieved);

		// 测试批量删除
		Set<String> keysToDelete = Set.of("key0", "key1", "key2");
		long deletedCount = shardedMap.deleteAll(keysToDelete);
		assertEquals(keysToDelete.size(), deletedCount);

		// 验证删除结果
		Map<String, Boolean> existsCheck = shardedMap.containsKeys(keysToDelete);
		assertTrue(existsCheck.values().stream().noneMatch(exists -> exists));
	}

	@Test
	void testAsyncOperations() throws Exception {
		// 测试异步put
		CompletableFuture<String> putFuture = shardedMap.putAsync("key1", "value1").toCompletableFuture();
		assertNull(putFuture.get(5, TimeUnit.SECONDS));

		// 测试异步get
		CompletableFuture<String> getFuture = shardedMap.getAsync("key1").toCompletableFuture();
		assertEquals("value1", getFuture.get(5, TimeUnit.SECONDS));

		// 测试异步批量操作
		Map<String, String> batchData = Map.of("key2", "value2", "key3", "value3", "key4", "value4");

		// 异步批量put
		CompletableFuture<Void> putAllFuture = shardedMap.putAllAsync(batchData).toCompletableFuture();
		putAllFuture.get(5, TimeUnit.SECONDS);

		// 异步批量get
		CompletableFuture<Map<String, String>> getAllFuture = shardedMap.getAllAsync(batchData.keySet()).toCompletableFuture();
		assertEquals(batchData, getAllFuture.get(5, TimeUnit.SECONDS));
	}

	@Test
	void testAtomicOperations() {
		// 测试putIfAbsent
		assertTrue(shardedMap.putIfAbsent("key1", "value1"));
		assertFalse(shardedMap.putIfAbsent("key1", "value2"));
		assertEquals("value1", shardedMap.get("key1"));

		// 测试replace
		assertFalse(shardedMap.replace("key1", "wrongValue", "value2"));
		assertTrue(shardedMap.replace("key1", "value1", "value2"));
		assertEquals("value2", shardedMap.get("key1"));

		// 测试getAndPut
		assertEquals("value2", shardedMap.getAndPut("key1", "value3"));
		assertEquals("value3", shardedMap.get("key1"));
	}

	@Test
	void testExpiryOperations() throws InterruptedException {
		// 测试带过期时间的put
		shardedMap.put("key1", "value1", 1, TimeUnit.SECONDS);
		assertEquals("value1", shardedMap.get("key1"));

		// 等待过期
		Thread.sleep(1500);
		assertNull(shardedMap.get("key1"));

		// 测试带过期时间的putIfAbsent
		assertTrue(shardedMap.putIfAbsent("key2", "value2", 1, TimeUnit.SECONDS));
		assertFalse(shardedMap.putIfAbsent("key2", "value3", 1, TimeUnit.SECONDS));
		assertEquals("value2", shardedMap.get("key2"));

		// 等待过期
		Thread.sleep(1500);
		assertNull(shardedMap.get("key2"));
	}

	@Test
	void testStats() {
		// 准备测试数据
		Map<String, String> testData = IntStream.range(0, 10).boxed().collect(Collectors.toMap(i -> "key" + i, i -> "value" + i));
		shardedMap.putAll(testData);

		// 获取统计信息
		Map<String, Long> stats = shardedMap.getStats();

		// 验证总数
		assertEquals(testData.size(), stats.get("total"));

		// 验证分片数量
		assertEquals(SHARD_COUNT + 1, stats.size()); // +1 是因为包含total
	}

	@Test
	void testResharding() throws Exception {
		// 准备测试数据
		Map<String, String> testData = IntStream.range(0, 100).boxed().collect(Collectors.toMap(i -> "key" + i, i -> "value" + i));

		// 记录原始状态
		System.out.println("Original shard count: " + SHARD_COUNT);
		Map<String, Long> originalStats = shardedMap.getStats();
		System.out.println("Original stats: " + originalStats);

		// 写入测试数据
		shardedMap.putAll(testData);

		// 验证数据写入成功
		Map<String, String> beforeReshard = shardedMap.getAll();
		assertEquals(testData.size(), beforeReshard.size(), "Data size before reshard doesn't match");
		assertEquals(testData, beforeReshard, "Data content before reshard doesn't match");

		// 打印重分片前的状态
		System.out.println("Stats before reshard: " + shardedMap.getStats());

		// 执行重分片
		int newShardCount = SHARD_COUNT * 2;
		System.out.println("New shard count will be: " + newShardCount);

		CompletableFuture<Boolean> reshardFuture = shardedMap.reshard(newShardCount).toCompletableFuture();

		// 等待重分片完成
		boolean reshardResult = reshardFuture.get(30, TimeUnit.SECONDS);
		System.out.println("Reshard result: " + reshardResult);
		assertTrue(reshardResult, "Reshard operation failed");

		// 打印重分片后的状态
		Map<String, Long> newStats = shardedMap.getStats();
		System.out.println("Stats after reshard: " + newStats);

		// 验证数据完整性
		Map<String, String> afterReshard = shardedMap.getAll();
		assertEquals(testData.size(), afterReshard.size(), "Data size after reshard doesn't match");
		assertEquals(testData, afterReshard, "Data content after reshard doesn't match");

		// 验证分片数量
		assertEquals(newShardCount + 1, newStats.size(), "New shard count doesn't match"); // +1 for total

		// 清理测试数据
		shardedMap.clear();
	}

	@Test
	void testConcurrentOperations() throws Exception {
		int threadCount = 10;
		int operationsPerThread = 100;

		List<CompletableFuture<Void>> futures = new ArrayList<>();

		// 创建多个线程并发操作
		for (int i = 0; i < threadCount; i++) {
			final int threadId = i;
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				for (int j = 0; j < operationsPerThread; j++) {
					String key = "key_" + threadId + "_" + j;
					String value = "value_" + threadId + "_" + j;

					// 执行一系列操作
					shardedMap.put(key, value);
					assertEquals(value, shardedMap.get(key));
					shardedMap.putIfAbsent(key, "other_value");
					assertEquals(value, shardedMap.get(key));
				}
			});
			futures.add(future);
		}

		// 等待所有操作完成
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);

		// 验证最终结果
		Map<String, Long> stats = shardedMap.getStats();
		assertEquals(threadCount * operationsPerThread, stats.get("total"));
	}

	@Test
	void testEdgeCases() {
		// 测试空键值
		assertThrows(NullPointerException.class, () -> shardedMap.put(null, "value"));
		assertThrows(NullPointerException.class, () -> shardedMap.put("key", null));

		// 测试空集合操作
		assertEquals(0, shardedMap.deleteAll(Collections.emptyList()));
		assertTrue(shardedMap.getAll(Collections.emptyList()).isEmpty());
		assertTrue(shardedMap.containsKeys(Collections.emptyList()).isEmpty());

		// 测试大量数据
		int largeSize = 10000;
		Map<String, String> largeData = IntStream.range(0, largeSize).boxed().collect(Collectors.toMap(i -> "key" + i, i -> "value" + i));
		shardedMap.putAll(largeData);
		assertEquals(largeSize, shardedMap.getStats().get("total"));
	}
}
