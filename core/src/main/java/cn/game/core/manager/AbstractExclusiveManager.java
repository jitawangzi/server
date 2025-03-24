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

	// 标签到对象的映射
	private final ConcurrentHashMap<String, Set<T>> labelToObjects = new ConcurrentHashMap<>();

	// ID到标签的映射
	private final ConcurrentHashMap<ID, Set<String>> idToLabels = new ConcurrentHashMap<>();

	// === 可选功能相关存储 ===
	// 过期时间映射
	private final ConcurrentHashMap<ID, Long> expiryMapping;

	// 最后访问时间映射 (LRU支持)
	private final ConcurrentHashMap<ID, Long> lastAccessMapping;

	// 访问计数映射 (LFU支持)
	private final ConcurrentHashMap<ID, AtomicInteger> accessCountMapping;

	// 添加时间映射 (FIFO支持)
	private final ConcurrentHashMap<ID, Long> insertionTimeMapping;

	// 容量限制
	private int maxCapacity;

	// 淘汰策略
	private EvictionPolicy evictionPolicy;

	// 过期清理调度器
	private final ScheduledExecutorService expiryScheduler;

	/**
	 * 使用指定配置创建管理器
	 */
	public AbstractExclusiveManager(ManagerConfig config) {
		super(config);

		// 根据配置初始化可选功能
		this.maxCapacity = config.isCapacityLimitEnabled() ? config.getMaxCapacity() : Integer.MAX_VALUE;
		this.evictionPolicy = config.isEvictionEnabled() ? config.getEvictionPolicy() : null;

		// 初始化可选存储
		this.expiryMapping = config.isExpiryEnabled() ? new ConcurrentHashMap<>() : null;

		boolean needsAccessStats = config.isAccessStatsEnabled()
				|| (config.isEvictionEnabled() && (config.getEvictionPolicy() == EvictionPolicy.LEAST_RECENTLY_USED
						|| config.getEvictionPolicy() == EvictionPolicy.LEAST_FREQUENTLY_USED));

		this.lastAccessMapping = needsAccessStats ? new ConcurrentHashMap<>() : null;
		this.accessCountMapping = needsAccessStats && config.getEvictionPolicy() == EvictionPolicy.LEAST_FREQUENTLY_USED
				? new ConcurrentHashMap<>()
				: null;

		boolean needsInsertionTime = config.isEvictionEnabled() && config.getEvictionPolicy() == EvictionPolicy.FIRST_IN_FIRST_OUT;
		this.insertionTimeMapping = needsInsertionTime ? new ConcurrentHashMap<>() : null;

		// 初始化过期清理调度器
		if (config.isExpiryEnabled()) {
			this.expiryScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, "ExclusiveManager-ExpiryThread");
				t.setDaemon(true);
				return t;
			});

			// 启动过期清理任务，每秒检查一次
			this.expiryScheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.SECONDS);
		} else {
			this.expiryScheduler = null;
		}
	}

	/**
	 * 使用默认最小配置创建管理器
	 */
	public AbstractExclusiveManager() {
		this(ManagerConfig.minimal());
	}

	@Override
	protected void doAdd(ID id, T obj, String... labels) {
		// 检查容量限制
		if (config.isCapacityLimitEnabled()) {
			enforceCapacityLimit();
		}

		long currentTime = System.currentTimeMillis();

		// 添加对象到存储
		storage.put(id, obj);

		// 根据配置更新可选存储
		if (lastAccessMapping != null) {
			lastAccessMapping.put(id, currentTime);
		}

		if (accessCountMapping != null) {
			accessCountMapping.put(id, new AtomicInteger(0));
		}

		if (insertionTimeMapping != null) {
			insertionTimeMapping.put(id, currentTime);
		}

		// 更新标签映射
		if (labels != null && labels.length > 0) {
			Set<String> labelSet = new HashSet<>(Arrays.asList(labels));
			idToLabels.put(id, labelSet);

			for (String label : labels) {
				labelToObjects.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(obj);
			}
		}
	}

	@Override
	protected void doAddWithExpiry(ID id, T obj, long expiryTimeMs, String... labels) {
		if (!config.isExpiryEnabled()) {
			throw new UnsupportedOperationException("Expiry feature is not enabled");
		}

		doAdd(id, obj, labels);

		// 设置过期时间
		if (expiryTimeMs > 0) {
			expiryMapping.put(id, System.currentTimeMillis() + expiryTimeMs);
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
			// 如果启用了统计功能，更新访问统计
			if (config.isAccessStatsEnabled()) {
				long currentTime = System.currentTimeMillis();

				if (lastAccessMapping != null) {
					lastAccessMapping.put(id, currentTime);
				}

				if (accessCountMapping != null) {
					accessCountMapping.get(id).incrementAndGet();
				}
			}

			// 如果启用了过期功能，检查是否过期
			if (config.isExpiryEnabled()) {
				Long expiryTime = expiryMapping.get(id);
				if (expiryTime != null && System.currentTimeMillis() > expiryTime) {
					doRemove(id);
					return null;
				}
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
		if (labels == null || labels.length == 0) {
			return doGetAll();
		}

		// 查找包含所有指定标签的对象
		Set<T> result = null;

		for (String label : labels) {
			Set<T> objects = labelToObjects.get(label);
			if (objects == null) {
				return Collections.emptyList();
			}

			if (result == null) {
				result = new HashSet<>(objects);
			} else {
				result.retainAll(objects);
			}

			if (result.isEmpty()) {
				return Collections.emptyList();
			}
		}

		return new ArrayList<>(result);
	}

	@Override
	protected Collection<ID> doGetIdsByLabels(String... labels) {
		if (labels == null || labels.length == 0) {
			return doGetIdsAll();
		}

		// 查找包含所有指定标签的对象
		Collection<T> objects = doGetByLabels(labels);

		// 将对象转换为ID
		List<ID> result = new ArrayList<>();
		for (Map.Entry<ID, T> entry : storage.entrySet()) {
			if (objects.contains(entry.getValue())) {
				result.add(entry.getKey());
			}
		}

		return result;
	}

	@Override
	protected Collection<T> doGetAll() {
		// 如果启用了过期功能，清理已过期的条目
		if (config.isExpiryEnabled()) {
			cleanupExpiredEntries();
		}
		return new ArrayList<>(storage.values());
	}

	@Override
	protected Collection<ID> doGetIdsAll() {
		// 如果启用了过期功能，清理已过期的条目
		if (config.isExpiryEnabled()) {
			cleanupExpiredEntries();
		}
		return storage.keySet();
	}

	@Override
	protected Collection<T> doFindByPredicate(Predicate<T> predicate) {
		return storage.entrySet().stream().filter(entry -> {
			// 如果启用了统计功能，更新访问统计
			if (config.isAccessStatsEnabled()) {
				ID id = entry.getKey();

				if (lastAccessMapping != null) {
					lastAccessMapping.put(id, System.currentTimeMillis());
				}

				if (accessCountMapping != null) {
					accessCountMapping.get(id).incrementAndGet();
				}
			}
			return predicate.test(entry.getValue());
		}).map(Map.Entry::getValue).collect(Collectors.toList());
	}

	@Override
	protected boolean doRemove(ID id) {
		T removed = storage.remove(id);
		if (removed == null) {
			return false;
		}

		// 清理可选映射
		if (config.isExpiryEnabled() && expiryMapping != null) {
			expiryMapping.remove(id);
		}

		if (config.isAccessStatsEnabled() || config.isEvictionEnabled()) {
			if (lastAccessMapping != null) {
				lastAccessMapping.remove(id);
			}

			if (accessCountMapping != null) {
				accessCountMapping.remove(id);
			}
		}

		if (insertionTimeMapping != null) {
			insertionTimeMapping.remove(id);
		}

		// 清理标签映射
		Set<String> labels = idToLabels.remove(id);
		if (labels != null) {
			for (String label : labels) {
				Set<T> objects = labelToObjects.get(label);
				if (objects != null) {
					objects.remove(removed);
					if (objects.isEmpty()) {
						labelToObjects.remove(label);
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
		labelToObjects.clear();
		idToLabels.clear();

		// 清理可选映射
		if (config.isExpiryEnabled() && expiryMapping != null) {
			expiryMapping.clear();
		}

		if (config.isAccessStatsEnabled() || config.isEvictionEnabled()) {
			if (lastAccessMapping != null) {
				lastAccessMapping.clear();
			}

			if (accessCountMapping != null) {
				accessCountMapping.clear();
			}
		}

		if (insertionTimeMapping != null) {
			insertionTimeMapping.clear();
		}
	}

	@Override
	protected void setMaxCapacity(int capacity) {
		if (!config.isCapacityLimitEnabled()) {
			throw new UnsupportedOperationException("Capacity limit feature is not enabled");
		}

		this.maxCapacity = capacity;
		enforceCapacityLimit();
	}

	@Override
	protected void setEvictionPolicy(EvictionPolicy policy) {
		if (!config.isEvictionEnabled()) {
			throw new UnsupportedOperationException("Eviction policy feature is not enabled");
		}

		this.evictionPolicy = policy;
	}

	@Override
	protected Map<String, Integer> getLabelStatistics() {
		Map<String, Integer> stats = new HashMap<>();
		for (Map.Entry<String, Set<T>> entry : labelToObjects.entrySet()) {
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
		if (!config.isCapacityLimitEnabled() || !config.isEvictionEnabled()) {
			return;
		}

		while (storage.size() >= maxCapacity && !storage.isEmpty()) {
			ID idToEvict = selectIdForEviction();
			if (idToEvict != null) {
				doRemove(idToEvict);
			} else {
				break;
			}
		}
	}

	/**
	 * 根据淘汰策略选择要淘汰的ID
	 */
	private ID selectIdForEviction() {
		if (storage.isEmpty() || !config.isEvictionEnabled()) {
			return null;
		}

		switch (evictionPolicy) {
		case LEAST_RECENTLY_USED:
			if (lastAccessMapping != null && !lastAccessMapping.isEmpty()) {
				return lastAccessMapping.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
			}
			break;

		case LEAST_FREQUENTLY_USED:
			if (accessCountMapping != null && !accessCountMapping.isEmpty()) {
				return accessCountMapping.entrySet()
						.stream()
						.min(Comparator.comparingInt(e -> e.getValue().get()))
						.map(Map.Entry::getKey)
						.orElse(null);
			}
			break;

		case FIRST_IN_FIRST_OUT:
			if (insertionTimeMapping != null && !insertionTimeMapping.isEmpty()) {
				return insertionTimeMapping.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
			}
			break;

		case RANDOM:
			List<ID> keys = new ArrayList<>(storage.keySet());
			if (!keys.isEmpty()) {
				return keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
			}
			break;
		}

		// 如果无法应用策略，返回第一个元素
		return storage.isEmpty() ? null : storage.keySet().iterator().next();
	}

	/**
	 * 清理过期的条目
	 */
	private void cleanupExpiredEntries() {
		if (!config.isExpiryEnabled() || expiryMapping == null) {
			return;
		}

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

		T obj = storage.get(id);

		// 移除旧标签
		Set<String> oldLabels = idToLabels.get(id);
		if (oldLabels != null) {
			for (String label : oldLabels) {
				Set<T> objects = labelToObjects.get(label);
				if (objects != null) {
					objects.remove(obj);
					if (objects.isEmpty()) {
						labelToObjects.remove(label);
					}
				}
			}
		}

		// 添加新标签
		if (newLabels.length > 0) {
			Set<String> labelSet = new HashSet<>(Arrays.asList(newLabels));
			idToLabels.put(id, labelSet);

			for (String label : newLabels) {
				labelToObjects.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(obj);
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

		T obj = storage.get(id);

		// 添加到ID-标签映射
		idToLabels.computeIfAbsent(id, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(label);

		// 添加到标签-对象映射
		labelToObjects.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(obj);
	}

	/**
	 * 从对象中移除标签
	 */
	public boolean removeLabel(ID id, String label) {
		if (!storage.containsKey(id)) {
			return false;
		}

		T obj = storage.get(id);

		// 从ID-标签映射中移除
		Set<String> labels = idToLabels.get(id);
		if (labels != null) {
			boolean removed = labels.remove(label);
			if (labels.isEmpty()) {
				idToLabels.remove(id);
			}

			// 从标签-对象映射中移除
			Set<T> objects = labelToObjects.get(label);
			if (objects != null) {
				objects.remove(obj);
				if (objects.isEmpty()) {
					labelToObjects.remove(label);
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
		if (config.isExpiryEnabled() && expiryScheduler != null) {
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
}