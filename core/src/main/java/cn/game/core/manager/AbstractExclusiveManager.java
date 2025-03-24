package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
 * 独占存储实现基类
 */
public abstract class AbstractExclusiveManager<ID, T> extends AbstractManagerTemplate<ID, T> {
	// 主存储
	private final ConcurrentHashMap<ID, T> storage = new ConcurrentHashMap<>();

	// 标签到ID的映射
	private final ConcurrentHashMap<String, Set<ID>> labelToIds = new ConcurrentHashMap<>();

	// ID到标签的映射
	private final ConcurrentHashMap<ID, Set<String>> idToLabels = new ConcurrentHashMap<>();

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
		Thread t = new Thread(r, "ExclusiveManager-ExpiryThread");
		t.setDaemon(true);
		return t;
	});

	public AbstractExclusiveManager() {
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

		// 添加对象到存储
		storage.put(id, obj);
		lastAccessMapping.put(id, currentTime);
		accessCountMapping.put(id, new AtomicInteger(0));
		insertionTimeMapping.put(id, currentTime);

		// 设置过期时间(如果有)
		if (expiryTimeMs > 0) {
			expiryMapping.put(id, currentTime + expiryTimeMs);
		}

		// 更新标签映射
		if (labels.length > 0) {
			Set<String> labelSet = new HashSet<>(Arrays.asList(labels));
			idToLabels.put(id, labelSet);

			for (String label : labels) {
				labelToIds.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(id);
			}
		}
	}

	@Override
	protected void doAddBatch(Map<ID, T> objects, String... labels) {
		objects.forEach((id, obj) -> doAdd(id, obj, labels));
	}

	@Override
	protected T doGet(ID id) {
		T obj = storage.get(id);
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

		// 查找包含所有指定标签的对象
		Set<ID> result = null;

		for (String label : labels) {
			Set<ID> ids = labelToIds.get(label);
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

		// 将ID转换为对象
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
		return new ArrayList<>(storage.values());
	}

	@Override
	protected Collection<T> doFindByPredicate(Predicate<T> predicate) {
		return storage.entrySet().stream().filter(entry -> {
			// 更新访问统计
			ID id = entry.getKey();
			lastAccessMapping.put(id, System.currentTimeMillis());
			accessCountMapping.get(id).incrementAndGet();
			return predicate.test(entry.getValue());
		}).map(Map.Entry::getValue).collect(Collectors.toList());
	}

	@Override
	protected boolean doRemove(ID id) {
		T removed = storage.remove(id);
		if (removed == null) {
			return false;
		}

		// 清理各种映射
		expiryMapping.remove(id);
		lastAccessMapping.remove(id);
		accessCountMapping.remove(id);
		insertionTimeMapping.remove(id);

		// 清理标签映射
		Set<String> labels = idToLabels.remove(id);
		if (labels != null) {
			for (String label : labels) {
				Set<ID> ids = labelToIds.get(label);
				if (ids != null) {
					ids.remove(id);
					if (ids.isEmpty()) {
						labelToIds.remove(label);
					}
				}
			}
		}

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

	@Override
	protected void doClear() {
		storage.clear();
		labelToIds.clear();
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
		for (Map.Entry<String, Set<ID>> entry : labelToIds.entrySet()) {
			stats.put(entry.getKey(), entry.getValue().size());
		}
		return stats;
	}

	@Override
	protected int getTotalObjectCount() {
		return storage.size();
	}

	/**
	 * 强制执行容量限制
	 */
	private void enforceCapacityLimit() {
		while (storage.size() >= maxCapacity && !storage.isEmpty()) {
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
		if (storage.isEmpty()) {
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
			List<ID> keys = new ArrayList<>(storage.keySet());
			return keys.get(ThreadLocalRandom.current().nextInt(keys.size()));

		default:
			return storage.keySet().iterator().next();
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
	 * 重新设置对象标签
	 */
	public void setLabels(ID id, String... newLabels) {
		if (!storage.containsKey(id)) {
			return;
		}

		// 移除旧标签
		Set<String> oldLabels = idToLabels.get(id);
		if (oldLabels != null) {
			for (String label : oldLabels) {
				Set<ID> ids = labelToIds.get(label);
				if (ids != null) {
					ids.remove(id);
					if (ids.isEmpty()) {
						labelToIds.remove(label);
					}
				}
			}
		}

		// 添加新标签
		if (newLabels.length > 0) {
			Set<String> labelSet = new HashSet<>(Arrays.asList(newLabels));
			idToLabels.put(id, labelSet);

			for (String label : newLabels) {
				labelToIds.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(id);
			}
		} else {
			idToLabels.remove(id);
		}
	}

	/**
	 * 获取对象的所有标签
	 */
	public Set<String> getLabels(ID id) {
		Set<String> labels = idToLabels.get(id);
		return labels != null ? new HashSet<>(labels) : Collections.emptySet();
	}

	/**
	 * 判断对象是否具有指定标签
	 */
	public boolean hasLabel(ID id, String label) {
		Set<String> labels = idToLabels.get(id);
		return labels != null && labels.contains(label);
	}

	/**
	 * 添加标签到对象
	 */
	public void addLabel(ID id, String label) {
		if (!storage.containsKey(id)) {
			return;
		}

		// 添加到ID-标签映射
		idToLabels.computeIfAbsent(id, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(label);

		// 添加到标签-ID映射
		labelToIds.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(id);
	}

	/**
	 * 从对象中移除标签
	 */
	public boolean removeLabel(ID id, String label) {
		if (!storage.containsKey(id)) {
			return false;
		}

		// 从ID-标签映射中移除
		Set<String> labels = idToLabels.get(id);
		if (labels != null) {
			boolean removed = labels.remove(label);
			if (labels.isEmpty()) {
				idToLabels.remove(id);
			}

			// 从标签-ID映射中移除
			Set<ID> ids = labelToIds.get(label);
			if (ids != null) {
				ids.remove(id);
				if (ids.isEmpty()) {
					labelToIds.remove(label);
				}
			}

			return removed;
		}

		return false;
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