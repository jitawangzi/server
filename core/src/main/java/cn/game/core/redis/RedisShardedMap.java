package cn.game.core.redis;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RMapAsync;
import org.redisson.api.RedissonClient;

/**
 * 分片Redis Map实现，提供水平扩展的Map数据结构
 *
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public class RedisShardedMap<K, V> extends AbstractShardedRedis {

	public RedisShardedMap(RedissonClient redisClient, String baseKey, int shardCount) {
		super(redisClient, baseKey, shardCount);
	}

	/**
	 * 放入键值对
	 *
	 * @param key 键
	 * @param value 值
	 * @return 返回之前关联的值，如果没有则返回null
	 */
	public V put(K key, V value) {
		return getMap(getShardKey(key)).put(key, value);
	}

	/**
	 * 放入键值对并设置过期时间
	 *
	 * @param key 键
	 * @param value 值
	 * @param timeout 过期时间值
	 * @param unit 过期时间单位
	 * @return 返回之前关联的值，如果没有则返回null
	 */
	public V put(K key, V value, long timeout, TimeUnit unit) {
		String shardKey = getShardKey(key);
		RMap<K, V> map = getMap(shardKey);
		V oldValue = map.put(key, value);
		map.expire(timeout, unit);
		return oldValue;
	}

	/**
	 * 获取值
	 *
	 * @param key 键
	 * @return 返回关联的值，如果不存在则返回null
	 */
	public V get(K key) {
		return getMap(getShardKey(key)).get(key);
	}

	/**
	 * 获取值
	 *
	 * @param key 键
	 * @return 返回关联的值，如果不存在则返回null
	 */
	public RFuture<V> getAsync(K key) {
		return getMap(getShardKey(key)).getAsync(key);
	}

	/**
	 * 移除键值对
	 *
	 * @param key 键
	 * @return 返回被移除的值，如果不存在则返回null
	 */
	public V remove(K key) {
		return getMap(getShardKey(key)).remove(key);
	}

	/**
	 * 检查键是否存在
	 *
	 * @param key 键
	 * @return 如果键存在则返回true
	 */
	public boolean containsKey(K key) {
		return getMap(getShardKey(key)).containsKey(key);
	}

	/**
	 * 异步放入键值对
	 *
	 * @param key 键
	 * @param value 值
	 * @return 返回RFuture，完成时包含之前关联的值
	 */
	public RFuture<V> putAsync(K key, V value) {
		return getMapAsync(getShardKey(key)).putAsync(key, value);
	}

	/**
	 * 异步放入键值对并设置过期时间
	 *
	 * @param key 键
	 * @param value 值
	 * @param timeout 过期时间值
	 * @param unit 过期时间单位
	 * @return 返回CompletionStage，完成时包含之前关联的值
	 */
	public CompletionStage<V> putAsync(K key, V value, long timeout, TimeUnit unit) {
		String shardKey = getShardKey(key);
		RMapAsync<K, V> map = getMapAsync(shardKey);
		return map.putAsync(key, value).thenCompose(oldValue -> map.expireAsync(timeout, unit).thenApply(expired -> oldValue));
	}

	/**
	 * 批量放入键值对
	 *
	 * @param map 要放入的键值对映射
	 */
	public void putAll(Map<? extends K, ? extends V> map) {
		Map<String, Map<K, V>> shardedMap = new HashMap<>();

		// 按分片分组键值对
		map.forEach((key, value) -> {
			String shardKey = getShardKey(key);
			shardedMap.computeIfAbsent(shardKey, k -> new HashMap<>()).put(key, value);
		});

		// 批量写入每个分片
		shardedMap.forEach((shardKey, entries) -> getMap(shardKey).putAll(entries));
	}

	/**
	 * 异步批量放入键值对
	 *
	 * @param map 要放入的键值对映射
	 * @return 返回CompletionStage，完成时表示操作完成
	 */
	public CompletionStage<Void> putAllAsync(Map<? extends K, ? extends V> map) {
		Map<String, Map<K, V>> shardedMap = new HashMap<>();

		// 按分片分组键值对
		map.forEach((key, value) -> {
			String shardKey = getShardKey(key);
			shardedMap.computeIfAbsent(shardKey, k -> new HashMap<>()).put(key, value);
		});

		// 创建所有分片的异步操作
		List<CompletableFuture<Void>> futures = shardedMap.entrySet()
				.stream()
				.map(entry -> getMapAsync(entry.getKey()).putAllAsync(entry.getValue()).toCompletableFuture())
				.collect(Collectors.toList());

		// 等待所有操作完成
		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
	}

	/**
	 * 获取所有键值对
	 *
	 * @return 返回所有键值对的映射
	 */
	public Map<K, V> getAll() {
		Map<K, V> result = new HashMap<>();
		for (int i = 0; i < shardCount; i++) {
			result.putAll(getMap(getShardKey(i)).readAllMap());
		}
		return result;
	}

	/**
	 * 异步获取所有键值对
	 *
	 * @return 返回CompletionStage，完成时包含所有键值对的映射
	 */
	public CompletionStage<Map<K, V>> getAllAsync() {
		List<CompletableFuture<Map<K, V>>> futures = new ArrayList<>();

		for (int i = 0; i < shardCount; i++) {
			futures.add(getMapAsync(getShardKey(i)).readAllMapAsync().toCompletableFuture());
		}

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures.stream()
						.map(CompletableFuture::join)
						.flatMap(map -> map.entrySet().stream())
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, HashMap::new)));
	}

	/**
	 * 获取分片统计信息
	 *
	 * @return 返回包含每个分片大小和总数量的统计信息
	 */
	public Map<String, Long> getStats() {
		Map<String, Long> stats = new LinkedHashMap<>();
		long total = 0;
		for (int i = 0; i < shardCount; i++) {
			String key = getShardKey(i);
			RMap<K, V> map = getMap(key);
			long size = map.size();
			stats.put("shard_" + i, size);
			total += size;
		}
		stats.put("total", total);
		return stats;
	}

	/**
	 * 清空所有数据
	 */
	public void clear() {
		for (int i = 0; i < shardCount; i++) {
			getMap(getShardKey(i)).clear();
		}
	}

	/**
	 * 重新分片，将数据迁移到新的分片数量配置中
	 * 注意：此操作会暂时影响数据访问，建议在系统维护时进行
	 *
	 * @param newShardCount 新的分片数量
	 * @return 返回CompletionStage，完成时表示迁移是否成功
	 * @throws IllegalArgumentException 如果新分片数量小于等于0
	 */
	public CompletionStage<Boolean> reshard(int newShardCount) {
		if (newShardCount <= 0) {
			throw new IllegalArgumentException("New shard count must be positive");
		}

		// 创建新的分片Map实例
		RedisShardedMap<K, V> newShardedMap = new RedisShardedMap<>(redisClient, baseKey + "_new", newShardCount);

		// 读取所有现有数据
		return getAllAsync().thenCompose(currentData ->
		// 迁移数据到新分片
		newShardedMap.putAllAsync(currentData).thenCompose(v ->
		// 验证数据完整性
		validateMigration(currentData, newShardedMap).thenCompose(valid -> {
			if (!valid) {
				return CompletableFuture.completedFuture(false);
			}
			// 替换旧数据
			return switchToNewShards(newShardedMap);
		})));
	}

	/**
	 * 验证数据迁移的完整性
	 */
	private CompletionStage<Boolean> validateMigration(Map<K, V> originalData, RedisShardedMap<K, V> newMap) {
		return newMap.getAllAsync().thenApply(newData -> {
			// 检查数据大小是否匹配
			if (originalData.size() != newData.size()) {
				log.info("Size mismatch - Original: {}, New: {}", originalData.size(), newData.size());
				return false;
			}

			// 检查所有数据是否一致
			boolean allMatch = originalData.entrySet().stream().allMatch(entry -> {
				V newValue = newData.get(entry.getKey());
				boolean matches = Objects.equals(entry.getValue(), newValue);
				if (!matches) {
					log.info("Data mismatch for key: {} - Original: {}, New: {}", entry.getKey(), entry.getValue(), newValue);
				}
				return matches;
			});

			return allMatch;
		});
	}


	private CompletionStage<Boolean> switchToNewShards(RedisShardedMap<K, V> newMap) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				// 1. 获取原始数据
				Map<K, V> originalData = newMap.getAll();
				log.info("Original data size: {}", originalData.size());

				// 2. 先清除所有旧数据
				for (int i = 0; i < shardCount; i++) {
					String oldKey = getShardKey(i);
					redisClient.getMap(oldKey).delete();
				}

				// 3. 使用新的分片数量重新分配数据
				this.shardCount = newMap.shardCount; // 更新分片数量

				// 4. 重新写入数据
				putAll(originalData);

				// 5. 验证数据
				Map<K, V> verifyData = getAll();
				boolean verified = verifyData.equals(originalData);

				log.info("Data verification: {}", verified ? "successful" : "failed");
				log.info("Original size: {}, New size: {}", originalData.size(), verifyData.size());

				return verified;
			} catch (Exception e) {
				log.error("Error during reshard: ", e);
				return false;
			}
		});
	}

	/**
	 * 原子操作：仅当键不存在时才设置值
	 *
	 * @param key 键
	 * @param value 值
	 * @return 如果设置成功返回true，如果键已存在返回false
	 */
	public boolean putIfAbsent(K key, V value) {
		return getMap(getShardKey(key)).putIfAbsent(key, value) == null;
	}

	/**
	 * 原子操作：仅当键不存在时才设置值，并设置过期时间
	 *
	 * @param key 键
	 * @param value 值
	 * @param timeout 过期时间值
	 * @param unit 过期时间单位
	 * @return 如果设置成功返回true，如果键已存在返回false
	 */
	public boolean putIfAbsent(K key, V value, long timeout, TimeUnit unit) {
		String shardKey = getShardKey(key);
		RMap<K, V> map = getMap(shardKey);
		if (map.putIfAbsent(key, value) == null) {
			map.expire(timeout, unit);
			return true;
		}
		return false;
	}

	/**
	 * 原子操作：仅当当前值等于期望值时才更新
	 *
	 * @param key 键
	 * @param expectedValue 期望的当前值
	 * @param newValue 要设置的新值
	 * @return 如果更新成功返回true
	 */
	public boolean replace(K key, V expectedValue, V newValue) {
		return getMap(getShardKey(key)).replace(key, expectedValue, newValue);
	}

	/**
	 * 原子操作：更新键的值并返回旧值
	 *
	 * @param key 键
	 * @param value 新值
	 * @return 返回之前的值
	 */
	public V getAndPut(K key, V value) {
		return getMap(getShardKey(key)).put(key, value);
	}

	/**
	 * 批量获取指定键的值
	 *
	 * @param keys 要获取的键集合
	 * @return 返回键值对映射，不存在的键将不会包含在结果中
	 */
	public Map<K, V> getAll(Collection<K> keys) {
		Map<String, List<K>> shardedKeys = keys.stream().collect(Collectors.groupingBy(this::getShardKey));

		Map<K, V> result = new HashMap<>();
		shardedKeys.forEach((shardKey, shardKeys) -> {
			RMap<K, V> map = getMap(shardKey);
			result.putAll(map.getAll(new HashSet<>(shardKeys)));
		});
		return result;
	}

	/**
	 * 异步批量获取指定键的值
	 *
	 * @param keys 要获取的键集合
	 * @return 返回CompletionStage，完成时包含键值对映射
	 */
	public CompletionStage<Map<K, V>> getAllAsync(Collection<K> keys) {
		Map<String, List<K>> shardedKeys = keys.stream().collect(Collectors.groupingBy(this::getShardKey));

		List<CompletableFuture<Map<K, V>>> futures = shardedKeys.entrySet()
				.stream()
				.map(entry -> getMapAsync(entry.getKey()).getAllAsync(new HashSet<>(entry.getValue())).toCompletableFuture())
				.collect(Collectors.toList());

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures.stream()
						.map(CompletableFuture::join)
						.flatMap(map -> map.entrySet().stream())
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, HashMap::new)));
	}

	/**
	 * 批量删除键
	 *
	 * @param keys 要删除的键集合
	 * @return 返回成功删除的键的数量
	 */
	public long deleteAll(Collection<K> keys) {
		Map<String, List<K>> shardedKeys = keys.stream().collect(Collectors.groupingBy(this::getShardKey));

		return shardedKeys.entrySet().stream().mapToLong(entry -> {
			RMap<K, V> map = getMap(entry.getKey());
			return entry.getValue().stream().map(map::remove).filter(Objects::nonNull).count();
		}).sum();
	}

	/**
	 * 异步批量删除键
	 *
	 * @param keys 要删除的键集合
	 * @return 返回CompletionStage，完成时包含成功删除的键的数量
	 */
	public CompletionStage<Long> deleteAllAsync(Collection<K> keys) {
		Map<String, List<K>> shardedKeys = keys.stream().collect(Collectors.groupingBy(this::getShardKey));

		List<CompletableFuture<Long>> futures = shardedKeys.entrySet().stream().map(entry -> {
			RMapAsync<K, V> map = getMapAsync(entry.getKey());
			List<RFuture<V>> removeFutures = entry.getValue().stream().map(map::removeAsync).collect(Collectors.toList());

			return CompletableFuture.allOf(removeFutures.stream().map(RFuture::toCompletableFuture).toArray(CompletableFuture[]::new))
					.thenApply(v -> removeFutures.stream().map(RFuture::getNow).filter(Objects::nonNull).count());
		}).map(CompletionStage::toCompletableFuture).collect(Collectors.toList());

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures.stream().mapToLong(CompletableFuture::join).sum());
	}

	/**
	 * 批量检查键是否存在
	 *
	 * @param keys 要检查的键集合
	 * @return 返回Map，key为检查的键，value为该键是否存在
	 */
	public Map<K, Boolean> containsKeys(Collection<K> keys) {
		return keys.stream().collect(Collectors.toMap(key -> key, this::containsKey, (v1, v2) -> v1, LinkedHashMap::new));
	}

	/**
	 * 异步批量检查键是否存在
	 *
	 * @param keys 要检查的键集合
	 * @return 返回CompletableFuture，完成时包含检查结果
	 */
	public CompletableFuture<Map<K, Boolean>> containsKeysAsync(Collection<K> keys) {
		List<CompletableFuture<Map.Entry<K, Boolean>>> futures = new ArrayList<>();

		for (K key : keys) {
			CompletableFuture<Map.Entry<K, Boolean>> future = getMapAsync(getShardKey(key))
					.containsKeyAsync(key)
					.toCompletableFuture()
					.thenApply(exists -> new AbstractMap.SimpleEntry<>(key, exists));
			futures.add(future);
		}

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures.stream()
						.map(CompletableFuture::join)
						.collect(Collectors.toMap(
								Map.Entry::getKey,
								Map.Entry::getValue,
								(v1, v2) -> v1,
								LinkedHashMap::new)));
	}

	/**
	 * 获取底层的Redis Map实例
	 *
	 * @param key 分片键
	 * @return 返回对应的RMap实例
	 */
	private RMap<K, V> getMap(String shardKey) {
		return redisClient.getMap(shardKey);
	}

	/**
	 * 获取底层的异步Redis Map实例
	 *
	 * @param key 分片键
	 * @return 返回对应的RMapAsync实例
	 */
	private RMapAsync<K, V> getMapAsync(String shardKey) {
		return redisClient.getMap(shardKey);
	}
}