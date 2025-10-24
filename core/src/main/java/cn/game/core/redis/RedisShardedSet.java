package cn.game.core.redis;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.redisson.api.RFuture;
import org.redisson.api.RSet;
import org.redisson.api.RSetAsync;
import org.redisson.api.RedissonClient;

/**
 * 分片Redis Set实现,提供水平扩展的Set数据结构
 * 通过将数据分散到多个Redis Set中来提高性能和可扩展性
 *
 * @param <T> 存储元素的类型
 */
public class RedisShardedSet<T> extends AbstractShardedRedis {

	/**
	 * 创建一个分片Redis Set实例
	 *
	 * @param redisClient Redis客户端实例
	 * @param baseKey 基础键名,用于生成分片键
	 * @param shardCount 分片数量
	 */
	public RedisShardedSet(RedissonClient redisClient, String baseKey, int shardCount) {
		super(redisClient, baseKey, shardCount);
	}


	/**
	 * 添加元素到Set中
	 *
	 * @param element 要添加的元素
	 * @return 如果元素被成功添加则返回true,如果元素已存在则返回false
	 */
	public boolean add(T element) {
		return getSet(getShardKey(element)).add(element);
	}

	/**
	 * 添加元素到Set中并设置过期时间
	 *
	 * @param element 要添加的元素
	 * @param timeout 过期时间值
	 * @param unit 过期时间单位
	 * @return 如果元素被成功添加则返回true,如果元素已存在则返回false
	 */
	public boolean add(T element, long timeout, TimeUnit unit) {
		String key = getShardKey(element);
		RSet<T> set = getSet(key);
		boolean result = set.add(element);
		if (result) {
			set.expire(timeout, unit);
		}
		return result;
	}

	/**
	 * 从Set中移除元素
	 *
	 * @param element 要移除的元素
	 * @return 如果元素被成功移除则返回true,如果元素不存在则返回false
	 */
	public boolean remove(T element) {
		return getSet(getShardKey(element)).remove(element);
	}

	/**
	 * 检查元素是否存在于Set中
	 *
	 * @param element 要检查的元素
	 * @return 如果元素存在则返回true,否则返回false
	 */
	public boolean contains(T element) {
		return getSet(getShardKey(element)).contains(element);
	}

	/**
	 * 异步添加元素到Set中
	 *
	 * @param element 要添加的元素
	 * @return 返回RFuture,完成时表示添加操作的结果
	 */
	public RFuture<Boolean> addAsync(T element) {
		return getSetAsync(getShardKey(element)).addAsync(element);
	}

	/**
	 * 异步添加元素到Set中并设置过期时间
	 *
	 * @param element 要添加的元素
	 * @param timeout 过期时间值
	 * @param unit 过期时间单位
	 * @return 返回CompletionStage,完成时表示添加和设置过期时间操作的结果
	 */
	public CompletionStage<Boolean> addAsync(T element, long timeout, TimeUnit unit) {
		String key = getShardKey(element);
		RSetAsync<T> set = getSetAsync(key);
		return set.addAsync(element).thenCompose(added -> {
			if (added) {
				return set.expireAsync(timeout, unit);
			}
			return CompletableFuture.completedFuture(false);
		});
	}

	/**
	 * 异步从Set中移除元素
	 *
	 * @param element 要移除的元素
	 * @return 返回RFuture,完成时表示移除操作的结果
	 */
	public RFuture<Boolean> removeAsync(T element) {
		return getSetAsync(getShardKey(element)).removeAsync(element);
	}

	/**
	 * 异步检查元素是否存在于Set中
	 *
	 * @param element 要检查的元素
	 * @return 返回RFuture,完成时表示检查操作的结果
	 */
	public RFuture<Boolean> containsAsync(T element) {
		return getSetAsync(getShardKey(element)).containsAsync(element);
	}

	/**
	 * 批量添加元素到Set中
	 *
	 * @param elements 要添加的元素集合
	 * @return 返回CompletionStage,完成时表示所有元素是否都成功添加
	 */
	public CompletionStage<Boolean> addAll(Collection<T> elements) {
		Map<String, List<T>> shardedElements = elements.stream().collect(Collectors.groupingBy(this::getShardKey));

		List<CompletableFuture<Boolean>> futures = shardedElements.entrySet()
				.stream()
				.map(entry -> getSetAsync(entry.getKey()).addAllAsync(entry.getValue()).toCompletableFuture())
				.collect(Collectors.toList());

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures.stream().map(CompletableFuture::join).allMatch(Boolean::booleanValue));
	}

	/**
	 * 批量添加元素到Set中并设置过期时间
	 *
	 * @param elements 要添加的元素集合
	 * @param timeout 过期时间值
	 * @param unit 过期时间单位
	 * @return 返回CompletionStage,完成时表示所有操作是否都成功
	 */
	public CompletionStage<Boolean> addAll(Collection<T> elements, long timeout, TimeUnit unit) {
		Map<String, List<T>> shardedElements = elements.stream().collect(Collectors.groupingBy(this::getShardKey));

		List<CompletableFuture<Boolean>> futures = shardedElements.entrySet().stream().map(entry -> {
			RSetAsync<T> set = getSetAsync(entry.getKey());
			return set.addAllAsync(entry.getValue()).thenCompose(added -> {
				if (added) {
					return set.expireAsync(timeout, unit);
				}
				return CompletableFuture.completedFuture(false);
			}).toCompletableFuture();
		}).collect(Collectors.toList());

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures.stream().map(CompletableFuture::join).allMatch(Boolean::booleanValue));
	}

	/**
	 * 重新分片,将数据迁移到新的分片数量配置中
	 * 注意:此操作会暂时影响数据访问,建议在系统维护时进行
	 *
	 * @param newShardCount 新的分片数量
	 * @return 返回CompletionStage,完成时表示迁移是否成功
	 * @throws IllegalArgumentException 如果新分片数量小于等于0
	 */
	public CompletionStage<Boolean> reshard(int newShardCount) {
		if (newShardCount <= 0) {
			throw new IllegalArgumentException("New shard count must be positive");
		}

		return CompletableFuture.supplyAsync(() -> {
			try {
				// 1. 读取所有现有数据
				Set<T> originalData = readAll();
				log.info("Original data size: {}", originalData.size());

				// 2. 创建临时键来存储新的分片数据
				String tempPrefix = baseKey + ":temp:";
				Map<String, Set<T>> newShardData = new HashMap<>();

				// 3. 使用新的分片数计算数据分布
				for (T element : originalData) {
					// 使用新的分片数计算分片键
					int newShard = Math.abs(element.hashCode() % newShardCount);
					String newKey = tempPrefix + newShard;
					newShardData.computeIfAbsent(newKey, k -> new HashSet<>()).add(element);
				}

				// 4. 写入新的分片数据到临时键
				for (Map.Entry<String, Set<T>> entry : newShardData.entrySet()) {
					RSet<T> tempSet = redisClient.getSet(entry.getKey());
					tempSet.addAll(entry.getValue());
				}

				// 5. 验证临时数据
				Set<T> tempData = new HashSet<>();
				for (int i = 0; i < newShardCount; i++) {
					RSet<T> tempSet = redisClient.getSet(tempPrefix + i);
					tempData.addAll(tempSet.readAll());
				}

				if (tempData.size() != originalData.size() || !tempData.containsAll(originalData) || !originalData.containsAll(tempData)) {
					log.error("Data verification failed during reshard");
					// 清理临时数据
					for (int i = 0; i < newShardCount; i++) {
						redisClient.getSet(tempPrefix + i).delete();
					}
					return false;
				}

				// 6. 更新分片数量
				int oldShardCount = this.shardCount;
				this.shardCount = newShardCount;

				// 7. 删除旧数据
				for (int i = 0; i < oldShardCount; i++) {
					redisClient.getSet(getShardKey(i)).delete();
				}

				// 8. 将临时数据移动到新的分片
				for (int i = 0; i < newShardCount; i++) {
					RSet<T> tempSet = redisClient.getSet(tempPrefix + i);
					RSet<T> newSet = redisClient.getSet(getShardKey(i));
					Set<T> data = tempSet.readAll();
					newSet.addAll(data);
					tempSet.delete();
				}

				// 9. 最终验证
				Set<T> finalData = readAll();
				boolean verified = finalData.size() == originalData.size() && finalData.containsAll(originalData)
						&& originalData.containsAll(finalData);

				if (!verified) {
					log.error("Final verification failed. Original size: {}, Final size: {}", originalData.size(), finalData.size());
					// 如果验证失败,可以考虑回滚操作
					return false;
				}

				log.info("Reshard completed successfully. New shard count: {}", newShardCount);
				return true;

			} catch (Exception e) {
				log.error("Error during reshard: ", e);
				return false;
			}
		});
	}
	/**
	 * 获取元素的过期时间
	 *
	 * @param element 要查询的元素
	 * @return 返回剩余过期时间(毫秒),-1表示永不过期,-2表示元素不存在
	 */
	public long getExpire(T element) {
		return getSet(getShardKey(element)).remainTimeToLive();
	}

	/**
	 * 异步获取元素的过期时间
	 *
	 * @param element 要查询的元素
	 * @return 返回RFuture,完成时包含剩余过期时间
	 */
	public RFuture<Long> getExpireAsync(T element) {
		return getSetAsync(getShardKey(element)).remainTimeToLiveAsync();
	}

	/**
	 * 获取元素的过期时间,并转换为指定时间单位
	 *
	 * @param element 要查询的元素
	 * @param unit 时间单位
	 * @return 返回指定单位的剩余过期时间,-1表示永不过期,-2表示元素不存在
	 */
	public long getExpire(T element, TimeUnit unit) {
		long ttl = getSet(getShardKey(element)).remainTimeToLive();
		return ttl > 0 ? unit.convert(ttl, TimeUnit.MILLISECONDS) : ttl;
	}

	/**
	 * 异步获取元素的过期时间,并转换为指定时间单位
	 *
	 * @param element 要查询的元素
	 * @param unit 时间单位
	 * @return 返回CompletableFuture,完成时包含指定单位的剩余过期时间
	 */
	public CompletableFuture<Long> getExpireAsync(T element, TimeUnit unit) {
		return getSetAsync(getShardKey(element)).remainTimeToLiveAsync()
				.toCompletableFuture()
				.thenApply(ttl -> ttl > 0 ? unit.convert(ttl, TimeUnit.MILLISECONDS) : ttl);
	}

	/**
	 * 清除元素的过期时间设置
	 *
	 * @param element 要操作的元素
	 * @return 如果成功清除过期时间则返回true
	 */
	public boolean clearExpire(T element) {
		return getSet(getShardKey(element)).clearExpire();
	}

	/**
	 * 异步清除元素的过期时间设置
	 *
	 * @param element 要操作的元素
	 * @return 返回RFuture,完成时表示是否成功清除过期时间
	 */
	public RFuture<Boolean> clearExpireAsync(T element) {
		return getSetAsync(getShardKey(element)).clearExpireAsync();
	}

	/**
	 * 获取所有分片的大小信息
	 *
	 * @return 返回Map,key为分片索引,value为对应分片的元素数量
	 */
	public Map<Integer, Integer> getShardSizes() {
		Map<Integer, Integer> sizes = new HashMap<>();
		for (int i = 0; i < shardCount; i++) {
			sizes.put(i, getSet(getShardKey(i)).size());
		}
		return sizes;
	}

	/**
	 * 异步获取所有分片的大小信息
	 *
	 * @return 返回CompletionStage,完成时包含所有分片的大小信息
	 */
	public CompletionStage<Map<Integer, Integer>> getShardSizesAsync() {
		List<CompletableFuture<Map.Entry<Integer, Integer>>> futures = new ArrayList<>();
		for (int i = 0; i < shardCount; i++) {
			final int shardIndex = i;
			CompletableFuture<Map.Entry<Integer, Integer>> future = getSetAsync(getShardKey(i)).sizeAsync()
					.toCompletableFuture()
					.thenApply(size -> new AbstractMap.SimpleEntry<>(shardIndex, size));
			futures.add(future);
		}

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures.stream()
						.map(CompletableFuture::join)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, HashMap::new)));
	}


	/**
	 * 随机获取指定数量的元素
	 *
	 * @param count 要获取的元素数量
	 * @return 返回CompletionStage,完成时包含随机获取的元素集合
	 */
	public CompletionStage<Set<T>> random(int count) {
		Set<T> result = new HashSet<>();
		List<Integer> shards = IntStream.range(0, shardCount).boxed().collect(Collectors.toList());
		Collections.shuffle(shards);

		return queryRandomFromShards(shards, 0, count, result);
	}

	/**
	 * 递归从分片中获取随机元素
	 *
	 * @param shards 分片索引列表
	 * @param index 当前处理的分片索引
	 * @param count 需要获取的总数量
	 * @param result 当前已获取的结果集
	 * @return 返回CompletionStage,完成时包含获取的随机元素集合
	 */
	private CompletionStage<Set<T>> queryRandomFromShards(List<Integer> shards, int index, int count, Set<T> result) {
		if (result.size() >= count || index >= shards.size()) {
			return CompletableFuture.completedFuture(result.stream().limit(count).collect(Collectors.toSet()));
		}

		int shard = shards.get(index);
		RSetAsync<T> set = getSetAsync(getShardKey(shard));

		return set.randomAsync(count - result.size()).thenCompose(randomElements -> {
			result.addAll(randomElements);
			return queryRandomFromShards(shards, index + 1, count, result);
		});
	}

	/**
	 * 批量移除元素
	 *
	 * @param elements 要移除的元素集合
	 * @return 如果有任何元素被成功移除则返回true
	 */
	public boolean removeAll(Collection<T> elements) {
		Map<String, List<T>> shardedElements = elements.stream().collect(Collectors.groupingBy(this::getShardKey));

		return shardedElements.entrySet()
				.stream()
				.map(entry -> getSet(entry.getKey()).removeAll(entry.getValue()))
				.reduce(Boolean.FALSE, Boolean::logicalOr);
	}

	/**
	 * 异步批量移除元素
	 *
	 * @param elements 要移除的元素集合
	 * @return 返回CompletableFuture,完成时表示是否有任何元素被成功移除
	 */
	public CompletableFuture<Boolean> removeAllAsync(Collection<T> elements) {
		Map<String, List<T>> shardedElements = elements.stream().collect(Collectors.groupingBy(this::getShardKey));

		List<CompletableFuture<Boolean>> futures = shardedElements.entrySet()
				.stream()
				.map(entry -> getSetAsync(entry.getKey()).removeAllAsync(entry.getValue()).toCompletableFuture())
				.collect(Collectors.toList());

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures.stream()
						.map(CompletableFuture::join)
						.reduce(Boolean.FALSE, Boolean::logicalOr));
	}

	/**
	 * 批量检查元素是否存在
	 *
	 * @param elements 要检查的元素集合
	 * @return 返回Map,key为元素,value为该元素是否存在
	 */
	public Map<T, Boolean> containsAll(Collection<T> elements) {
        return elements.stream()
                .collect(Collectors.toMap(
                        element -> element,
						element -> getSet(getShardKey(element)).contains(element), (v1, v2) -> v1, LinkedHashMap::new
                ));
    }

	/**
	 * 异步批量检查元素是否存在
	 *
	 * @param elements 要检查的元素集合
	 * @return 返回CompletableFuture,完成时包含检查结果的Map
	 */
	public CompletableFuture<Map<T, Boolean>> containsAllAsync(Collection<T> elements) {
		List<CompletableFuture<Map.Entry<T, Boolean>>> futures = new ArrayList<>();
		
		for (T element : elements) {
			CompletableFuture<Map.Entry<T, Boolean>> future = getSetAsync(getShardKey(element))
					.containsAsync(element)
					.toCompletableFuture()
					.thenApply(exists -> new AbstractMap.SimpleEntry<>(element, exists));
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
	 * 检查是否所有元素都存在
	 *
	 * @param elements 要检查的元素集合
	 * @return 如果所有元素都存在则返回true
	 */
	public boolean containsAllElements(Collection<T> elements) {
		return elements.stream().allMatch(element -> getSet(getShardKey(element)).contains(element));
	}

	/**
	 * 异步检查是否所有元素都存在
	 *
	 * @param elements 要检查的元素集合
	 * @return 返回CompletableFuture,完成时表示是否所有元素都存在
	 */
	public CompletableFuture<Boolean> containsAllElementsAsync(Collection<T> elements) {
		List<CompletableFuture<Boolean>> futures = elements.stream()
				.map(element -> getSetAsync(getShardKey(element)).containsAsync(element).toCompletableFuture())
				.collect(Collectors.toList());

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.thenApply(v -> futures.stream()
						.map(CompletableFuture::join)
						.allMatch(Boolean::booleanValue));
	}

	/**
	 * 检查是否存在任意一个元素
	 *
	 * @param elements 要检查的元素集合
	 * @return 如果存在任意一个元素则返回true
	 */
	public boolean containsAnyElements(Collection<T> elements) {
		return elements.stream().anyMatch(element -> getSet(getShardKey(element)).contains(element));
	}

	/**
	 * 异步检查是否存在任意一个元素
	 *
	 * @param elements 要检查的元素集合
	 * @return 返回CompletableFuture,完成时表示是否存在任意一个元素
	 */
	public CompletableFuture<Boolean> containsAnyElementsAsync(Collection<T> elements) {
		if (elements.isEmpty()) {
			return CompletableFuture.completedFuture(false);
		}

		CompletableFuture<Boolean> result = new CompletableFuture<>();
		AtomicInteger counter = new AtomicInteger(elements.size());
		AtomicBoolean found = new AtomicBoolean(false);

		for (T element : elements) {
			getSetAsync(getShardKey(element)).containsAsync(element)
					.whenComplete((exists, ex) -> {
						if (ex != null) {
							result.completeExceptionally(ex);
							return;
						}

						if (exists && !found.getAndSet(true)) {
							result.complete(true);
							return;
						}

						if (counter.decrementAndGet() == 0 && !result.isDone()) {
							result.complete(false);
						}
					});
		}

		return result;
	}

	/**
	 * 获取所有分片的统计信息
	 *
	 * @return 返回Map,包含每个分片的大小和总元素数量
	 */
    public Map<String, Long> getStats() {
		Map<String, Long> stats = new LinkedHashMap<>();
        long total = 0;
        for (int i = 0; i < shardCount; i++) {
			String key = getShardKey(i);
			RSet<T> set = getSet(key);
            long size = set.size();
            stats.put("shard_" + i, size);
            total += size;
        }
        stats.put("total", total);
        return stats;
    }

	/**
	 * 异步获取所有分片的统计信息
	 *
	 * @return 返回CompletableFuture,完成时包含所有分片的统计信息
	 */
    public CompletableFuture<Map<String, Long>> getStatsAsync() {
		List<CompletableFuture<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < shardCount; i++) {
            futures.add(getSetAsync(getShardKey(i)).sizeAsync().toCompletableFuture());
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
					Map<String, Long> stats = new LinkedHashMap<>();
                    long total = 0;
                    for (int i = 0; i < futures.size(); i++) {
                        long size = futures.get(i).join();
                        stats.put("shard_" + i, size);
                        total += size;
                    }
                    stats.put("total", total);
                    return stats;
                });
    }

	/**
	 * 读取所有分片中的所有元素(仅用于测试,生产环境慎用)
	 *
	 * @return 返回包含所有分片数据的Set
	 */
	public Set<T> readAll() {
		Set<T> result = new HashSet<>();
		for (int i = 0; i < shardCount; i++) {
			result.addAll(getSet(getShardKey(i)).readAll());
		}
		return result;
	}

	/**
	 * 获取底层的Redis Set实例
	 *
	 * @param key 分片键
	 * @return 返回对应的RSet实例
	 */
	private RSet<T> getSet(String shardKey) {
		return redisClient.getSet(shardKey);
	}

	/**
	 * 获取底层的异步Redis Set实例
	 *
	 * @param key 分片键
	 * @return 返回对应的RSetAsync实例
	 */
	private RSetAsync<T> getSetAsync(String key) {
		return redisClient.getSet(key);
	}
}