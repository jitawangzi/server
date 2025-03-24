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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
	private final ConcurrentHashMap<String, Set<T>> labelMapping = new ConcurrentHashMap<>();

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
	public AbstractShardedManager(ManagerConfig config) {
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
				Thread t = new Thread(r, "ShardedManager-ExpiryThread");
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
	public AbstractShardedManager() {
		this(ManagerConfig.minimal());
	}

	@Override
	protected void doAdd(ID id, T obj, String... labels) {
		// 检查容量限制
		if (config.isCapacityLimitEnabled()) {
			enforceCapacityLimit();
		}

		long currentTime = System.currentTimeMillis();

		// 先添加到ID映射中
		idMapping.put(id, obj);

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

		// 保存标签映射
		Set<String> labelSet = new HashSet<>(Arrays.asList(labels));
		idToLabels.put(id, labelSet);

		if (labels == null || labels.length == 0) {
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
			labelMapping.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(obj);
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
		T obj = idMapping.get(id);
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
		List<T> result = new ArrayList<>(ids.size());
		for (ID id : ids) {
			T obj = doGet(id);
			if (obj != null) {
				result.add(obj);
			}
		}
		return result;
	}

	@Override
	protected Collection<T> doGetByLabels(String... labels) {
		if (labels == null || labels.length == 0) {
			return doGetAll();
		}

		// 使用标签映射高效查询
		Set<T> result = null;

		// 找出具有所有标签的对象集合（取交集）
		for (String label : labels) {
			Set<T> objects = labelMapping.get(label);
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

		// 获取满足标签条件的对象
		Collection<T> objects = doGetByLabels(labels);

		// 将对象转换为ID集合
		List<ID> result = new ArrayList<>();
		for (Map.Entry<ID, T> entry : idMapping.entrySet()) {
			if (objects.contains(entry.getValue())) {
				result.add(entry.getKey());
			}
		}

		return result;
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
		// 如果启用了过期功能，清理已过期的条目
		if (config.isExpiryEnabled()) {
			cleanupExpiredEntries();
		}
		return new ArrayList<>(idMapping.values());
	}
	@Override
	protected Collection<ID> doGetIdsAll() {
		// 如果启用了过期功能，清理已过期的条目
		if (config.isExpiryEnabled()) {
			cleanupExpiredEntries();
		}
		return idMapping.keySet();
	}

	@Override
	protected Collection<T> doFindByPredicate(Predicate<T> predicate) {
		List<T> result = new ArrayList<>();

		for (Map.Entry<ID, T> entry : idMapping.entrySet()) {
			ID id = entry.getKey();
			T obj = entry.getValue();

			// 如果启用了统计功能，更新访问统计
			if (config.isAccessStatsEnabled()) {
				if (lastAccessMapping != null) {
					lastAccessMapping.put(id, System.currentTimeMillis());
				}

				if (accessCountMapping != null) {
					accessCountMapping.get(id).incrementAndGet();
				}
			}

			if (predicate.test(obj)) {
				result.add(obj);
			}
		}

		return result;
	}

	@Override
	protected boolean doRemove(ID id) {
		T obj = idMapping.remove(id);
		if (obj == null) {
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

		// 从标签映射中移除
		Set<String> labels = idToLabels.remove(id);
		if (labels != null) {
			for (String label : labels) {
				Set<T> objects = labelMapping.get(label);
				if (objects != null) {
					objects.remove(obj);
					if (objects.isEmpty()) {
						labelMapping.remove(label);
					}
				}
			}
		}

		// 从分片存储中移除
		return removeFromShards(shardedStorage, id);
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
		for (Map.Entry<String, Set<T>> entry : labelMapping.entrySet()) {
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
		if (!config.isCapacityLimitEnabled() || !config.isEvictionEnabled()) {
			return;
		}

		while (idMapping.size() >= maxCapacity && !idMapping.isEmpty()) {
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
		if (idMapping.isEmpty() || !config.isEvictionEnabled()) {
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
			List<ID> keys = new ArrayList<>(idMapping.keySet());
			if (!keys.isEmpty()) {
				return keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
			}
			break;
		}

		// 如果无法应用策略，返回第一个元素
		return idMapping.isEmpty() ? null : idMapping.keySet().iterator().next();
	}

	/**
	 * 清理过期的条目
	 */
	private void cleanupExpiredEntries() {
		if (!config.isExpiryEnabled() || expiryMapping == null) {
			return;
		}

		long currentTime = System.currentTimeMillis();
		List<ID> expiredIds = new ArrayList<>();

		for (Map.Entry<ID, Long> entry : expiryMapping.entrySet()) {
			if (entry.getValue() <= currentTime) {
				expiredIds.add(entry.getKey());
			}
		}

		for (ID id : expiredIds) {
			doRemove(id);
		}
	}

	/**
	 * 根据标签路径获取对象集合
	 * 
	 * @param path 标签路径数组
	 * @return 符合路径的对象集合
	 */
	@SuppressWarnings("unchecked")
	public Collection<T> getByPath(String... path) {
		if (path == null || path.length == 0) {
			return doGetAll();
		}

		Object current = shardedStorage;

		// 遍历路径
		for (int i = 0; i < path.length; i++) {
			if (!(current instanceof Map)) {
				return Collections.emptyList();
			}

			Map<String, Object> map = (Map<String, Object>) current;
			current = map.get(path[i]);

			if (current == null) {
				return Collections.emptyList();
			}
		}

		// 找到了最终节点
		if (current instanceof Map) {
			Map<?, ?> finalMap = (Map<?, ?>) current;

			// 如果是ID到对象的映射，直接返回值集合
			if (!finalMap.isEmpty() && finalMap.values().iterator().next() instanceof Map) {
				// 这是中间节点，需要收集所有叶子节点
				List<T> result = new ArrayList<>();
				collectLeafValues((Map<String, Object>) current, result);
				return result;
			} else {
				// 这是叶子节点
				return new ArrayList<>(((Map<ID, T>) current).values());
			}
		} else if (current instanceof Collection) {
			return (Collection<T>) current;
		} else if (current != null) {
			return Collections.singletonList((T) current);
		}

		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	private void collectLeafValues(Map<String, Object> map, List<T> result) {
		for (Object value : map.values()) {
			if (value instanceof Map) {
				// 检查是否是中间节点（值也是Map）或叶子节点
				Map<?, ?> valueMap = (Map<?, ?>) value;
				if (!valueMap.isEmpty() && valueMap.values().iterator().next() instanceof Map) {
					// 递归收集子Map的值
					collectLeafValues((Map<String, Object>) value, result);
				} else {
					// 这是叶子节点，添加所有值
					result.addAll(((Map<ID, T>) value).values());
				}
			}
			// 忽略非Map类型的值，因为在我们的设计中，只有Map才会包含实体
		}
	}

	/**
	 * 获取指定标签的对象数量
	 */
	public int getCountByLabel(String label) {
		Set<T> objects = labelMapping.get(label);
		return objects != null ? objects.size() : 0;
	}

	/**
	 * 获取所有标签
	 */
	public Set<String> getAllLabels() {
		return new HashSet<>(labelMapping.keySet());
	}

	/**
	 * 获取分片存储的总层次结构
	 * 仅用于调试或监控目的
	 */
	public Map<String, Object> getStorageStructure() {
		return new HashMap<>(shardedStorage);
	}

	/**
	 * 设置对象标签
	 */
	public void setLabels(ID id, String... newLabels) {
		if (!idMapping.containsKey(id)) {
			return;
		}

		T obj = idMapping.get(id);

		// 先从原来的位置移除
		doRemove(id);

		// 重新添加到新标签位置
		doAdd(id, obj, newLabels);
	}

	/**
	 * 获取对象的标签
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
	 * 注意：这将重新组织对象在分片存储中的位置
	 */
	public void addLabel(ID id, String label) {
		if (!idMapping.containsKey(id)) {
			return;
		}

		Set<String> labels = idToLabels.get(id);
		if (labels != null && !labels.contains(label)) {
			Set<String> newLabels = new HashSet<>(labels);
			newLabels.add(label);
			setLabels(id, newLabels.toArray(new String[0]));
		}
	}

	/**
	 * 从对象中移除标签
	 * 注意：这将重新组织对象在分片存储中的位置
	 */
	public boolean removeLabel(ID id, String label) {
		if (!idMapping.containsKey(id)) {
			return false;
		}

		Set<String> labels = idToLabels.get(id);
		if (labels != null && labels.contains(label)) {
			Set<String> newLabels = new HashSet<>(labels);
			newLabels.remove(label);
			setLabels(id, newLabels.toArray(new String[0]));
			return true;
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