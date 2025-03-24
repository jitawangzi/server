package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 分片存储实现基类
 */
public abstract class AbstractShardedManager<ID, T> extends AbstractManagerTemplate<ID, T> {
	// 主存储 - 分片存储
	private final ConcurrentHashMap<String, Object> shardedStorage = new ConcurrentHashMap<>();

	// ID映射，用于快速查找
	private final ConcurrentHashMap<ID, T> idMapping = new ConcurrentHashMap<>();

	// ID到标签的映射
	private final ConcurrentHashMap<ID, Set<String>> idToLabels = new ConcurrentHashMap<>();

	// 标签映射，用于快速查找带有特定标签的对象
	private final ConcurrentHashMap<String, Set<ID>> labelMapping = new ConcurrentHashMap<>();

	// 过期时间映射
	private final ConcurrentHashMap<ID, Long> expiryMapping = new ConcurrentHashMap<>();

	// 最后访问时间映射 (LRU支持)
	private final ConcurrentHashMap<ID, Long> lastAccessMapping = new ConcurrentHashMap<>();

	// 访问计数映射 (LFU支持)
	private final ConcurrentHashMap<ID, AtomicInteger> accessCountMapping = new ConcurrentHashMap<>();

	// 添加时间映射 (FIFO支持)
	private final ConcurrentHashMap<ID, Long> insertionTimeMapping = new ConcurrentHashMap<>();

	// 容量限制
	private int maxCapacity = Integer.MAX_VALUE;

	// 淘汰策略
	private EvictionPolicy evictionPolicy = EvictionPolicy.LEAST_RECENTLY_USED;

	// 过期清理调度器
	private final ScheduledExecutorService expiryScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "ShardedManager-ExpiryThread");
		t.setDaemon(true);
		return t;
	});

	public AbstractShardedManager() {
		// 启动过期清理任务，每秒检查一次
		expiryScheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.SECONDS);
	}

	@Override
	protected void doAdd(ID id, T obj, String... labels) {
		doAddWithExpiry(id, obj, -1, labels);
	}

	@Override
	protected void doAddWithExpiry(ID id, T obj, long expiryTimeMs, String... labels) {
		// 检查容量限制
		enforceCapacityLimit();

		// 记录当前时间用于FIFO和LRU
		long currentTime = System.currentTimeMillis();

		// 先添加到ID映射中
		idMapping.put(id, obj);
		lastAccessMapping.put(id, currentTime);
		accessCountMapping.put(id, new AtomicInteger(0));
		insertionTimeMapping.put(id, currentTime);

		// 设置过期时间(如果有)
		if (expiryTimeMs > 0) {
			expiryMapping.put(id, currentTime + expiryTimeMs);
		}

		// 保存标签映射
		Set<String> labelSet = new HashSet<>(Arrays.asList(labels));
		idToLabels.put(id, labelSet);

		if (labels.length == 0) {
			// 没有标签时，添加到根存储
			shardedStorage.put(id.toString(), obj);
			return;
		}

		// 分片存储实现
		ConcurrentHashMap<String, Object> current = shardedStorage;
		for (int i = 0; i < labels.length - 1; i++) {
			current = (ConcurrentHashMap<String, Object>) current.computeIfAbsent(labels[i], k -> new ConcurrentHashMap<>());
		}

		String lastLabel = labels[labels.length - 1];
		ConcurrentHashMap<ID, T> leaf = (ConcurrentHashMap<ID, T>) current.computeIfAbsent(lastLabel, k -> new ConcurrentHashMap<>());

		leaf.put(id, obj);

		// 更新标签映射
		for (String label : labels) {
			labelMapping.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(id);
		}
	}

	@Override
	protected void doAddBatch(Map<ID, T> objects, String... labels) {
		objects.forEach((id, obj) -> doAdd(id, obj, labels));
	}

	@Override
	protected T doGet(ID id) {
		T obj = idMapping.get(id);
		if (obj != null) {
			// 更新访问统计
			lastAccessMapping.put(id, System.currentTimeMillis());
			accessCountMapping.get(id).incrementAndGet();

			// 检查是否过期
			Long expiryTime = expiryMapping.get(id);
			if (expiryTime != null && System.currentTimeMillis() > expiryTime) {
				doRemove(id);
				return null;
			}
		}
		return obj;
	}

	@Override
	protected Collection<T> doGetBatch(Collection<ID> ids) {
		return ids.stream().map(this::doGet).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	protected Collection<T> doGetByLabels(String... labels) {
		if (labels.length == 0) {
			return doGetAll();
		}

		// 使用标签映射高效查询
		Set<ID> result = null;

		// 找出具有所有标签的ID集合（取交集）
		for (String label : labels) {
			Set<ID> ids = labelMapping.get(label);
			if (ids == null) {
				return Collections.emptyList();
			}

			if (result == null) {
				result = new HashSet<>(ids);
			} else {
				result.retainAll(ids);
			}

			if (result.isEmpty()) {
				return Collections.emptyList();
			}
		}

		// 将ID转换为对应的对象
		return result.stream().map(this::doGet).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	protected Collection<T> doGetPagedAndSorted(int page, int size, Comparator<T> comparator, String... labels) {
		// 首先获取满足标签条件的所有对象
		Collection<T> allMatching = doGetByLabels(labels);

		// 排序
		List<T> sorted = new ArrayList<>(allMatching);
		if (comparator != null) {
			sorted.sort(comparator);
		}

		// 分页
		int fromIndex = page * size;
		int toIndex = Math.min(fromIndex + size, sorted.size());

		if (fromIndex >= sorted.size()) {
			return Collections.emptyList();
		}

		return sorted.subList(fromIndex, toIndex);
	}

	@Override
	protected Collection<T> doGetAll() {
		// 清理已过期的条目
		cleanupExpiredEntries();
		return new ArrayList<>(idMapping.values());
	}

	@Override
	protected Collection<T> doFindByPredicate(Predicate<T> predicate) {
		return idMapping.values().stream().filter(obj -> {
			// 更新访问统计
			ID id = getIdForObject(obj);
			if (id != null) {
				lastAccessMapping.put(id, System.currentTimeMillis());
				accessCountMapping.get(id).incrementAndGet();
			}
			return predicate.test(obj);
		}).collect(Collectors.toList());
	}

	// 辅助方法：从对象反向查找ID（可在子类重写以提高效率）
	protected ID getIdForObject(T obj) {
		for (Map.Entry<ID, T> entry : idMapping.entrySet()) {
			if (entry.getValue().equals(obj)) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	protected boolean doRemove(ID id) {
		T obj = idMapping.remove(id);
		if (obj == null) {
			return false;
		}

		// 清理各种映射
		expiryMapping.remove(id);
		lastAccessMapping.remove(id);
		accessCountMapping.remove(id);
		insertionTimeMapping.remove(id);

		// 从标签映射中移除
		Set<String> labels = idToLabels.remove(id);
		if (labels != null) {
			for (String label : labels) {
				Set<ID> ids = labelMapping.get(label);
				if (ids != null) {
					ids.remove(id);
					if (ids.isEmpty()) {
						labelMapping.remove(label);
					}
				}
			}
		}

		// 从分片存储中移除
		removeFromShards(shardedStorage, id);

		return true;
	}

	@Override
	protected boolean doRemoveBatch(Collection<ID> ids) {
		boolean allRemoved = true;
		for (ID id : ids) {
			allRemoved &= doRemove(id);
		}
		return allRemoved;
	}

	@SuppressWarnings("unchecked")
	private boolean removeFromShards(Map<String, Object> map, ID id) {
		boolean removed = false;

		// 遍历当前层级的所有项
		for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Object> entry = it.next();
			Object value = entry.getValue();

			if (value instanceof Map) {
				// 递归处理子Map
				if (removeFromShards((Map<String, Object>) value, id)) {
					removed = true;
					// 如果子Map现在为空，可以移除它
					if (((Map<?, ?>) value).isEmpty()) {
						it.remove();
					}
				}
			} else if (value instanceof ConcurrentHashMap && ((ConcurrentHashMap<?, ?>) value).containsKey(id)) {
				// 如果是叶子节点且包含目标ID
				((ConcurrentHashMap<ID, T>) value).remove(id);
				removed = true;
				// 如果Map现在为空，可以移除它
				if (((Map<?, ?>) value).isEmpty()) {
					it.remove();
				}
			}
		}

		return removed;
	}

	@Override
	protected void doClear() {
		shardedStorage.clear();
		idMapping.clear();
		labelMapping.clear();
		idToLabels.clear();
		expiryMapping.clear();
		lastAccessMapping.clear();
		accessCountMapping.clear();
		insertionTimeMapping.clear();
	}

	@Override
	protected void setMaxCapacity(int capacity) {
		this.maxCapacity = capacity;
		enforceCapacityLimit();
	}

	@Override
	protected void setEvictionPolicy(EvictionPolicy policy) {
		this.evictionPolicy = policy;
	}

	@Override
	protected Map<String, Integer> getLabelStatistics() {
		Map<String, Integer> stats = new HashMap<>();
		for (Map.Entry<String, Set<ID>> entry : labelMapping.entrySet()) {
			stats.put(entry.getKey(), entry.getValue().size());
		}
		return stats;
	}

	@Override
	protected int getTotalObjectCount() {
		return idMapping.size();
	}

	/**
	 * 强制执行容量限制
	 */
	private void enforceCapacityLimit() {
		while (idMapping.size() >= maxCapacity && !idMapping.isEmpty()) {
			ID idToEvict = selectIdForEviction();
			if (idToEvict != null) {
				doRemove(idToEvict);
			}
		}
	}

	/**
	 * 根据淘汰策略选择要淘汰的ID
	 */
	private ID selectIdForEviction() {
		if (idMapping.isEmpty()) {
			return null;
		}

		switch (evictionPolicy) {
		case LEAST_RECENTLY_USED:
			return lastAccessMapping.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

		case LEAST_FREQUENTLY_USED:
			return accessCountMapping.entrySet()
					.stream()
					.min(Comparator.comparingInt(e -> e.getValue().get()))
					.map(Map.Entry::getKey)
					.orElse(null);

		case FIRST_IN_FIRST_OUT:
			return insertionTimeMapping.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

		case RANDOM:
			List<ID> keys = new ArrayList<>(idMapping.keySet());
			return keys.get(ThreadLocalRandom.current().nextInt(keys.size()));

		default:
			return idMapping.keySet().iterator().next();
		}
	}

	/**
	 * 清理过期的条目
	 */
	private void cleanupExpiredEntries() {
		long currentTime = System.currentTimeMillis();
		List<ID> expiredIds = expiryMapping.entrySet()
				.stream()
				.filter(entry -> entry.getValue() <= currentTime)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		for (ID id : expiredIds) {
			doRemove(id);
		}
	}

	/**
	 * 获取指定标签的对象数量
	 */
	public int getCountByLabel(String label) {
		Set<ID> ids = labelMapping.get(label);
		return ids != null ? ids.size() : 0;
	}

	/**
	 * 获取所有标签
	 */
	public Set<String> getAllLabels() {
		return new HashSet<>(labelMapping.keySet());
	}

	/**
	 * 关闭管理器，释放资源
	 */
	public void shutdown() {
		expiryScheduler.shutdown();
		try {
			if (!expiryScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
				expiryScheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			expiryScheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}