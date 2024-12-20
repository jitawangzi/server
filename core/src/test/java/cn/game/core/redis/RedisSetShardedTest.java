package cn.game.core.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.redisson.api.RedissonClient;

import cn.game.util.RedisUtil;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisSetShardedTest {

	private static RedissonClient redisClient;
	private static RedisSetSharded<String> shardedSet;
	private static final String BASE_KEY = "test:sharded:set";
	private static final int SHARD_COUNT = 4;

	@BeforeAll
	static void setUp() {
//		Config config = new Config();
//		config.useClusterServers()
//				.addNodeAddress("redis://localhost:7001")
//				.addNodeAddress("redis://localhost:7002")
//				.addNodeAddress("redis://localhost:7003");
//		redisClient = Redisson.create(config);

		try {
			RedisUtil.getInstance().init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		redisClient = RedisUtil.getRedis();

		shardedSet = new RedisSetSharded<>(redisClient, BASE_KEY, SHARD_COUNT);
	}

	@AfterAll
	static void tearDown() {
		redisClient.shutdown();
	}

	@BeforeEach
	void clearData() {
		for (int i = 0; i < SHARD_COUNT; i++) {
			redisClient.getSet(BASE_KEY + ":" + i).delete();
		}
	}

	@Test
	@Order(1)
	void testBasicOperations() {
		// 测试添加元素
		assertTrue(shardedSet.add("test1"));
		assertTrue(shardedSet.add("test2"));
		assertFalse(shardedSet.add("test1")); // 重复添加

		// 测试包含元素
		assertTrue(shardedSet.contains("test1"));
		assertTrue(shardedSet.contains("test2"));
		assertFalse(shardedSet.contains("test3"));

		// 测试移除元素
		assertTrue(shardedSet.remove("test1"));
		assertFalse(shardedSet.contains("test1"));
		assertFalse(shardedSet.remove("test3")); // 移除不存在的元素
	}

	@Test
	@Order(2)
	void testExpireOperations() throws InterruptedException {
		// 测试带过期时间的添加
		assertTrue(shardedSet.add("expire1", 2, TimeUnit.SECONDS));
		assertTrue(shardedSet.contains("expire1"));

		// 等待过期
		Thread.sleep(2500);
		assertFalse(shardedSet.contains("expire1"));

		// 测试获取过期时间
		shardedSet.add("expire2", 10, TimeUnit.SECONDS);
		long ttl = shardedSet.getExpire("expire2");
		assertTrue(ttl > 0 && ttl <= 10000);

		// 测试清除过期时间
		assertTrue(shardedSet.clearExpire("expire2"));
		assertEquals(-1, shardedSet.getExpire("expire2"));
	}

	@Test
	@Order(3)
	void testBatchOperations() {
		// 准备测试数据
		Set<String> testData = IntStream.range(0, 100).mapToObj(i -> "element" + i).collect(Collectors.toSet());

		// 测试批量添加
		assertTrue(shardedSet.addAll(testData).toCompletableFuture().join());

		// 测试批量检查
		Map<String, Boolean> containsResults = shardedSet.containsAll(testData);
		assertTrue(containsResults.values().stream().allMatch(v -> v));

		// 测试是否所有元素都存在
		assertTrue(shardedSet.containsAllElements(testData));

		// 测试是否存在任意元素
		assertTrue(shardedSet.containsAnyElements(Collections.singleton("element0")));

		// 测试批量移除
		Set<String> toRemove = new HashSet<>(Arrays.asList("element0", "element1"));
		assertTrue(shardedSet.removeAll(toRemove));
		assertFalse(shardedSet.containsAnyElements(toRemove));
	}
	@Test
	@Order(4)
	void testRandomOperations() {
		// 准备测试数据
		Set<String> testData = IntStream.range(0, 100).mapToObj(i -> "random" + i).collect(Collectors.toSet());
		shardedSet.addAll(testData).toCompletableFuture().join();

		// 测试随机获取元素
		Set<String> randomElements = shardedSet.random(10).toCompletableFuture().join();
		// 检查获取的元素数量（可能少于请求的数量）
		assertTrue(randomElements.size() > 0 && randomElements.size() <= 10,
				"Random elements size should be between 1 and 10, but was " + randomElements.size());
		// 检查获取的元素都在原始数据集中
		assertTrue(testData.containsAll(randomElements), "All random elements should be from the original dataset");
	}

	@Test
	@Order(5)
	void testStatsAndSizes() {
		// 准备测试数据
		Map<Integer, List<String>> shardData = new HashMap<>();
		int totalElements = 0;
		for (int i = 0; i < SHARD_COUNT; i++) {
			final int shardIndex = i;
			List<String> elements = IntStream.range(0, 10)
					.mapToObj(j -> "shard" + shardIndex + "_element" + j)
					.collect(Collectors.toList());
			shardData.put(i, elements);
			shardedSet.addAll(elements);
			totalElements += elements.size();
		}

		// 测试分片大小
		Map<Integer, Integer> shardSizes = shardedSet.getShardSizes();
		assertEquals(SHARD_COUNT, shardSizes.size());
		// 验证总元素数量而不是每个分片的大小
		assertEquals(totalElements, shardSizes.values().stream().mapToInt(Integer::intValue).sum(),
				"Total number of elements should match");
		// 确保每个分片都有元素
		shardSizes.forEach((shard, size) -> assertTrue(size > 0, "Shard " + shard + " should have at least one element"));

		// 测试统计信息
		Map<String, Long> stats = shardedSet.getStats();
		assertEquals(SHARD_COUNT + 1, stats.size()); // +1 for total
		assertEquals(totalElements, stats.get("total").longValue());
	}

	@Test
	@Order(6)
	void testResharding() {
		try {
			// 准备测试数据
			Set<String> testData = IntStream.range(0, 100).mapToObj(i -> "reshard_element" + i).collect(Collectors.toSet());

			// 清理可能存在的旧数据
			for (int i = 0; i < SHARD_COUNT * 2; i++) {
				redisClient.getSet(BASE_KEY + ":" + i).delete();
				redisClient.getSet(BASE_KEY + ":temp:" + i).delete();
			}

			// 分批写入数据
			List<String> dataList = new ArrayList<>(testData);
			int batchSize = 10;
			for (int i = 0; i < dataList.size(); i += batchSize) {
				int end = Math.min(i + batchSize, dataList.size());
				List<String> batch = dataList.subList(i, end);
				boolean addResult = shardedSet.addAll(batch).toCompletableFuture().get(5, TimeUnit.SECONDS);
				assertTrue(addResult, "Failed to add batch data");
			}

			// 验证初始数据
			Set<String> initialData = shardedSet.readAll();
			assertEquals(testData.size(), initialData.size(), "Initial data size mismatch");
			assertTrue(testData.containsAll(initialData) && initialData.containsAll(testData), "Initial data content mismatch");

			// 执行重分片
			int newShardCount = SHARD_COUNT * 2;
			boolean reshardResult = shardedSet.reshard(newShardCount).toCompletableFuture().get(30, TimeUnit.SECONDS);
			assertTrue(reshardResult, "Reshard operation failed");

			// 验证重分片后的数据
			Set<String> afterReshard = shardedSet.readAll();
			assertEquals(testData.size(), afterReshard.size(), "Data size mismatch after reshard");
			assertTrue(testData.containsAll(afterReshard), "Missing elements after reshard");
			assertTrue(afterReshard.containsAll(testData), "Extra elements after reshard");

		} catch (Exception e) {
			fail("Test failed: " + e.getMessage(), e);
		} finally {
			// 清理测试数据
			for (int i = 0; i < SHARD_COUNT * 2; i++) {
				redisClient.getSet(BASE_KEY + ":" + i).delete();
				redisClient.getSet(BASE_KEY + ":temp:" + i).delete();
			}
		}
	}
	@Test
	@Order(7)
	void testAsyncOperations() throws Exception {
		// 测试异步添加
		assertTrue(shardedSet.addAsync("async1").get());
		assertFalse(shardedSet.addAsync("async1").get());

		// 测试异步检查存在
		assertTrue(shardedSet.containsAsync("async1").get());
		assertFalse(shardedSet.containsAsync("async2").get());

		// 测试异步移除
		assertTrue(shardedSet.removeAsync("async1").get());
		assertFalse(shardedSet.removeAsync("async1").get());

		// 测试异步批量操作
		Set<String> batchData = new HashSet<>(Arrays.asList("batch1", "batch2", "batch3"));
		assertTrue(shardedSet.addAll(batchData).toCompletableFuture().get());

		Map<String, Boolean> asyncContains = shardedSet.containsAllAsync(batchData).get();
		assertTrue(asyncContains.values().stream().allMatch(v -> v));

		assertTrue(shardedSet.removeAllAsync(batchData).get());
	}
}