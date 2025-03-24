package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 统一的层级标签管理器实现
 * 支持按ID检索和按层级标签检索
 */
public abstract class AbstractShardedManager<ID, T> extends AbstractManagerTemplate<ID, T> {
	// 主ID映射，用于直接ID访问
	private final ConcurrentHashMap<ID, T> idToObject = new ConcurrentHashMap<>();

	// ID到标签路径的映射
	private final ConcurrentHashMap<ID, List<String[]>> idToLabelPaths = new ConcurrentHashMap<>();

	// 层级存储结构，支持按路径快速查找
	private final ConcurrentHashMap<String, Object> hierarchyStorage = new ConcurrentHashMap<>();

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

			// 启动过期清理任务，每5秒检查一次
			this.expiryScheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 5, TimeUnit.SECONDS);
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

	/**
	 * 添加对象到存储
	 * 可以同时添加到多个层级路径
	 * 
	 * @param id 对象ID
	 * @param obj 对象实例
	 * @param labels 层级标签路径数组
	 */
	@Override
	protected void doAdd(ID id, T obj, String... labels) {
		// 简单处理为单一路径
		String[][] labelPaths;
		if (labels.length == 0) {
			labelPaths = new String[0][0];
		} else {
			// 作为单一路径处理
			labelPaths = new String[][] { labels };
		}

		doAddWithPaths(id, obj, labelPaths);
	}

	/**
	 * 使用层级路径添加对象
	 * 
	 * @param id 对象ID
	 * @param obj 对象实例
	 * @param labelPaths 层级标签路径数组，每个数组代表一个完整路径
	 */
	public void addWithPaths(ID id, T obj, String[]... labelPaths) {
		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}
		beforeAdd(obj);
		doAddWithPaths(id, obj, labelPaths);
		afterAdd(obj);
		notifyListeners(listener -> listener.onObjectAdded(obj));
	}

	/**
	 * 实际添加对象到层级路径的实现
	 */
	@SuppressWarnings("unchecked")
	protected void doAddWithPaths(ID id, T obj, String[]... labelPaths) {
		// 检查容量限制
		if (config.isCapacityLimitEnabled()) {
			enforceCapacityLimit();
		}

		long currentTime = System.currentTimeMillis();

		// 1. 添加到主存储
		idToObject.put(id, obj);

		// 2. 根据配置更新可选存储
		if (lastAccessMapping != null) {
			lastAccessMapping.put(id, currentTime);
		}

		if (accessCountMapping != null) {
			accessCountMapping.put(id, new AtomicInteger(0));
		}

		if (insertionTimeMapping != null) {
			insertionTimeMapping.put(id, currentTime);
		}

		// 3. 保存ID到标签路径的映射
		List<String[]> paths = new ArrayList<>();
		for (String[] path : labelPaths) {
			if (path != null && path.length > 0) {
				paths.add(path);
			}
		}

		if (!paths.isEmpty()) {
			idToLabelPaths.put(id, paths);
		}

		// 4. 添加到层级存储结构
		if (labelPaths.length == 0) {
			// 如果没有路径，直接添加到根节点
			hierarchyStorage.put(id.toString(), obj);
			return;
		}

		// 对于每个路径，都将对象添加到对应位置
		for (String[] path : labelPaths) {
			if (path == null || path.length == 0) {
				hierarchyStorage.put(id.toString(), obj);
				continue;
			}

			ConcurrentHashMap<String, Object> current = hierarchyStorage;

			// 沿路径构建或导航层级结构
			for (int i = 0; i < path.length - 1; i++) {
				String segment = path[i];
				Object next = current.get(segment);

				if (next == null || !(next instanceof ConcurrentHashMap)) {
					ConcurrentHashMap<String, Object> newMap = new ConcurrentHashMap<>();
					current.put(segment, newMap);
					current = newMap;
				} else {
					current = (ConcurrentHashMap<String, Object>) next;
				}
			}

			// 获取最后一级节点
			String lastSegment = path[path.length - 1];

			// 检查最后一级是否已经有一个Map
			Object lastLevel = current.get(lastSegment);

			if (lastLevel == null) {
				// 创建叶子节点存储
				ConcurrentHashMap<ID, T> leafMap = new ConcurrentHashMap<>();
				leafMap.put(id, obj);
				current.put(lastSegment, leafMap);
			} else if (lastLevel instanceof ConcurrentHashMap) {
				// 检查是否是叶子节点还是中间节点
				ConcurrentHashMap<?, ?> map = (ConcurrentHashMap<?, ?>) lastLevel;
				if (!map.isEmpty()) {
					Object firstValue = map.values().iterator().next();
					if (firstValue instanceof ConcurrentHashMap) {
						// 这是中间节点，转换为叶子节点
						ConcurrentHashMap<ID, T> leafMap = new ConcurrentHashMap<>();
						leafMap.put(id, obj);
						current.put(lastSegment, leafMap);
					} else {
						// 这是叶子节点，直接添加
						((ConcurrentHashMap<ID, T>) map).put(id, obj);
					}
				} else {
					// 空Map，假设是叶子节点
					((ConcurrentHashMap<ID, T>) map).put(id, obj);
				}
			} else {
				// 最后一级是具体对象，需要转换为Map
				ConcurrentHashMap<ID, T> leafMap = new ConcurrentHashMap<>();
				leafMap.put(id, obj);
				current.put(lastSegment, leafMap);
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

	/**
	 * 使用层级路径添加带过期时间的对象
	 */
	public void addWithPathsAndExpiry(ID id, T obj, long time, TimeUnit unit, String[]... labelPaths) {
		if (!config.isExpiryEnabled()) {
			throw new UnsupportedOperationException("Expiry feature is not enabled");
		}

		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}

		beforeAdd(obj);
		doAddWithPaths(id, obj, labelPaths);

		// 设置过期时间
		if (time > 0) {
			expiryMapping.put(id, System.currentTimeMillis() + unit.toMillis(time));
		}

		afterAdd(obj);
		notifyListeners(listener -> listener.onObjectAdded(obj));
	}

	@Override
	protected T doGet(ID id) {
		T obj = idToObject.get(id);
		if (obj != null) {
			// 如果启用了统计功能，更新访问统计
			updateAccessStats(id);

			// 如果启用了过期功能，检查是否过期
			if (config.isExpiryEnabled() && isExpired(id)) {
				doRemove(id);
				return null;
			}
		}
		return obj;
	}

	private void updateAccessStats(ID id) {
		if (config.isAccessStatsEnabled()) {
			long currentTime = System.currentTimeMillis();

			if (lastAccessMapping != null) {
				lastAccessMapping.put(id, currentTime);
			}

			if (accessCountMapping != null) {
				accessCountMapping.get(id).incrementAndGet();
			}
		}
	}

	private boolean isExpired(ID id) {
		if (!config.isExpiryEnabled() || expiryMapping == null) {
			return false;
		}

		Long expiryTime = expiryMapping.get(id);
		return expiryTime != null && System.currentTimeMillis() > expiryTime;
	}

	@Override
	protected Collection<T> doGetByLabels(String... labels) {
		if (labels == null || labels.length == 0) {
			return doGetAll();
		}

		// 将标签列表作为一个单一路径
		return getByPath(labels);
	}

	/**
	 * 根据层级路径获取对象集合
	 * 
	 * @param path 层级路径
	 * @return 该路径下的所有对象
	 */
	@SuppressWarnings("unchecked")
	public Collection<T> getByPath(String... path) {
		if (path == null || path.length == 0) {
			return doGetAll();
		}

		Object current = hierarchyStorage;

		// 沿路径导航
		for (String segment : path) {
			if (!(current instanceof Map)) {
				return Collections.emptyList();
			}

			Map<String, Object> map = (Map<String, Object>) current;
			current = map.get(segment);

			if (current == null) {
				return Collections.emptyList();
			}
		}

		// 找到了最终节点
		if (current instanceof Map) {
			Map<?, ?> finalMap = (Map<?, ?>) current;

			// 判断是叶子节点还是中间节点
			if (!finalMap.isEmpty()) {
				Object firstValue = finalMap.values().iterator().next();
				if (firstValue instanceof Map) {
					// 这是中间节点，需要收集所有叶子节点
					List<T> result = new ArrayList<>();
					collectLeafValues((Map<String, Object>) current, result);
					return result;
				} else {
					// 这是叶子节点，直接返回值
					return new ArrayList<>(((Map<ID, T>) finalMap).values());
				}
			} else {
				return Collections.emptyList();
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
				Map<?, ?> valueMap = (Map<?, ?>) value;
				if (!valueMap.isEmpty()) {
					Object firstValue = valueMap.values().iterator().next();
					if (firstValue instanceof Map) {
						// 递归收集子Map的值
						collectLeafValues((Map<String, Object>) value, result);
					} else {
						// 这是叶子节点，添加所有值
						result.addAll(((Map<ID, T>) value).values());
					}
				}
			} else if (value != null) {
				// 这是单个对象
				result.add((T) value);
			}
		}
	}

	@Override
	protected Collection<ID> doGetIdsByLabels(String... labels) {
		Collection<T> objects = doGetByLabels(labels);
		return objects.stream().map(obj -> getIdByObject(obj)).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * 根据对象查找ID
	 */
	private ID getIdByObject(T obj) {
		for (Map.Entry<ID, T> entry : idToObject.entrySet()) {
			if (entry.getValue().equals(obj)) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	protected Collection<T> doGetAll() {
		// 如果启用了过期功能，清理已过期的条目
		if (config.isExpiryEnabled()) {
			cleanupExpiredEntries();
		}
		return new ArrayList<>(idToObject.values());
	}

	@Override
	protected Collection<ID> doGetIdsAll() {
		// 如果启用了过期功能，清理已过期的条目
		if (config.isExpiryEnabled()) {
			cleanupExpiredEntries();
		}
		return new ArrayList<>(idToObject.keySet());
	}

	@Override
	protected Collection<T> doFindByPredicate(Predicate<T> predicate) {
		return idToObject.entrySet()
				.stream()
				.peek(entry -> updateAccessStats(entry.getKey()))
				.map(Map.Entry::getValue)
				.filter(obj -> predicate.test(obj))
				.collect(Collectors.toList());
	}

	@Override
	protected boolean doRemove(ID id) {
		T removed = idToObject.remove(id);
		if (removed == null) {
			return false;
		}

		// 清理可选映射
		if (config.isExpiryEnabled() && expiryMapping != null) {
			expiryMapping.remove(id);
		}

		if (lastAccessMapping != null) {
			lastAccessMapping.remove(id);
		}

		if (accessCountMapping != null) {
			accessCountMapping.remove(id);
		}

		if (insertionTimeMapping != null) {
			insertionTimeMapping.remove(id);
		}

		// 从层级存储中移除
		List<String[]> paths = idToLabelPaths.remove(id);
		if (paths != null) {
			for (String[] path : paths) {
				removeFromPath(hierarchyStorage, id, path, 0);
			}
		} else {
			// 如果没有路径记录，尝试从根节点移除
			hierarchyStorage.remove(id.toString());
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean removeFromPath(Map<String, Object> current, ID id, String[] path, int depth) {
		if (path == null || depth >= path.length) {
			return false;
		}

		String segment = path[depth];
		Object next = current.get(segment);

		if (next == null) {
			return false;
		}

		if (depth == path.length - 1) {
			// 最后一级节点
			if (next instanceof Map) {
				Map<?, ?> leafMap = (Map<?, ?>) next;
				boolean removed = leafMap.remove(id) != null;

				if (leafMap.isEmpty()) {
					current.remove(segment);
				}

				return removed;
			}
			return false;
		} else {
			// 中间节点
			if (next instanceof Map) {
				boolean removed = removeFromPath((Map<String, Object>) next, id, path, depth + 1);

				if (removed && ((Map<?, ?>) next).isEmpty()) {
					current.remove(segment);
				}

				return removed;
			}
			return false;
		}
	}

	@Override
	protected void doClear() {
		hierarchyStorage.clear();
		idToObject.clear();
		idToLabelPaths.clear();

		// 清理可选映射
		if (config.isExpiryEnabled() && expiryMapping != null) {
			expiryMapping.clear();
		}

		if (lastAccessMapping != null) {
			lastAccessMapping.clear();
		}

		if (accessCountMapping != null) {
			accessCountMapping.clear();
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

		// 遍历所有ID-路径映射，统计每个标签的使用次数
		for (List<String[]> pathsList : idToLabelPaths.values()) {
			for (String[] path : pathsList) {
				for (String segment : path) {
					stats.put(segment, stats.getOrDefault(segment, 0) + 1);
				}
			}
		}

		return stats;
	}

	@Override
	protected int getTotalObjectCount() {
		return idToObject.size();
	}

	/**
	 * 强制执行容量限制
	 */
	private void enforceCapacityLimit() {
		if (!config.isCapacityLimitEnabled() || !config.isEvictionEnabled()) {
			return;
		}

		while (idToObject.size() >= maxCapacity && !idToObject.isEmpty()) {
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
		if (idToObject.isEmpty() || !config.isEvictionEnabled()) {
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
			List<ID> keys = new ArrayList<>(idToObject.keySet());
			if (!keys.isEmpty()) {
				return keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
			}
			break;
		}

		// 如果无法应用策略，返回第一个元素
		return idToObject.isEmpty() ? null : idToObject.keySet().iterator().next();
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
	 * 设置对象的标签路径
	 * 
	 * @param id 对象ID
	 * @param labelPaths 新的标签路径数组
	 */
	public void setLabelPaths(ID id, String[]... labelPaths) {
		T obj = idToObject.get(id);
		if (obj == null) {
			return;
		}

		// 移除对象的所有现有路径
		doRemove(id);

		// 重新添加对象到新路径
		doAddWithPaths(id, obj, labelPaths);
	}

	/**
	 * 获取对象的所有标签路径
	 * 
	 * @param id 对象ID
	 * @return 标签路径列表
	 */
	public List<String[]> getLabelPaths(ID id) {
		List<String[]> paths = idToLabelPaths.get(id);
		if (paths == null) {
			return Collections.emptyList();
		}

		// 返回路径的深拷贝
		List<String[]> result = new ArrayList<>(paths.size());
		for (String[] path : paths) {
			result.add(Arrays.copyOf(path, path.length));
		}
		return result;
	}

	/**
	 * 添加一个标签路径到对象
	 * 
	 * @param id 对象ID
	 * @param labelPath 新的标签路径
	 */
	public void addLabelPath(ID id, String... labelPath) {
		T obj = idToObject.get(id);
		if (obj == null || labelPath == null || labelPath.length == 0) {
			return;
		}

		// 获取现有路径
		List<String[]> existingPaths = idToLabelPaths.computeIfAbsent(id, k -> new ArrayList<>());

		// 添加新路径
		existingPaths.add(labelPath);

		// 添加对象到新路径
		doAddWithPaths(id, obj, labelPath);
	}

	/**
	 * 移除对象的标签路径
	 * 
	 * @param id 对象ID
	 * @param labelPath 要移除的标签路径
	 * @return 是否成功移除
	 */
	public boolean removeLabelPath(ID id, String... labelPath) {
		if (labelPath == null || labelPath.length == 0) {
			return false;
		}

		List<String[]> paths = idToLabelPaths.get(id);
		if (paths == null) {
			return false;
		}

		// 查找匹配的路径
		boolean removed = false;
		for (Iterator<String[]> it = paths.iterator(); it.hasNext();) {
			String[] path = it.next();
			if (Arrays.equals(path, labelPath)) {
				it.remove();
				removed = true;
				break;
			}
		}

		if (removed) {
			// 从旧路径移除对象
			removeFromPath(hierarchyStorage, id, labelPath, 0);

			// 如果没有路径了，完全移除对象
			if (paths.isEmpty()) {
				idToLabelPaths.remove(id);

				// 如果对象不在任何路径下，重新添加到根节点
				T obj = idToObject.get(id);
				if (obj != null) {
					hierarchyStorage.put(id.toString(), obj);
				}
			}
		}

		return removed;
	}

	/**
	 * 判断对象是否具有指定标签
	 */
	public boolean hasLabel(ID id, String label) {
		List<String[]> labels = idToLabelPaths.get(id);
		if (labels == null) {
            return false;
        }
		for (String[] path : labels) {
			for (String segment : path) {
				if (segment.equals(label)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取对象的标签
	 */
	public List<String> getLabels(ID id) {
		List<String[]> labels = idToLabelPaths.get(id);
		if (labels == null) {
			return Collections.emptyList();
		}
		List<String> ret = new ArrayList<>();
		for (String[] path : labels) {
            for (String segment : path) {
				ret.add(segment);
            }
        }
		return ret;
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