package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 抽象管理器模板基类
 * 实现基础Manager接口
 */
public abstract class AbstractManagerTemplate<ID, T> implements Manager<ID, T> {
	// 管理器配置
	protected final ManagerConfig config;

	// 事件监听器列表
	private final List<ManagerEventListener<T>> listeners;

	// 主ID映射，用于直接ID访问 - 所有子类共享此存储结构
	protected final ConcurrentHashMap<ID, T> idToObject;

	// === 可选功能相关存储 ===
	// 过期时间映射
	protected final ConcurrentHashMap<ID, Long> expiryMapping;
	// 最后访问时间映射 (LRU支持)
	protected final ConcurrentHashMap<ID, Long> lastAccessMapping;
	// 访问计数映射 (LFU支持)
	protected final ConcurrentHashMap<ID, AtomicInteger> accessCountMapping;
	// 添加时间映射 (FIFO支持)
	protected final ConcurrentHashMap<ID, Long> insertionTimeMapping;
	// 容量限制
	protected int maxCapacity;
	// 淘汰策略
	protected EvictionPolicy evictionPolicy;
	// 过期清理调度器
	protected final ScheduledExecutorService expiryScheduler;

	/**
	 * 创建管理器实例
	 * @param config 管理器配置
	 */
	protected AbstractManagerTemplate(ManagerConfig config) {
		this.config = config;
		this.listeners = config.isEventNotificationEnabled() ? new ArrayList<>() : null;
		this.idToObject = new ConcurrentHashMap<>();

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
				Thread t = new Thread(r, "Manager-ExpiryThread");
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
	 * 使用默认最小配置创建管理器实例
	 */
	protected AbstractManagerTemplate() {
		this(ManagerConfig.minimal());
	}

	// === 存储操作方法 ===

	/**
	 * 基础添加对象实现
	 */
	protected void doAdd(ID id, T obj) {
		// 检查容量限制
		if (config.isCapacityLimitEnabled()) {
			enforceCapacityLimit();
		}

		long currentTime = System.currentTimeMillis();

		// 添加到主存储
		idToObject.put(id, obj);

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
	}

	/**
	 * 基础获取对象实现
	 */
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

	/**
	 * 获取所有对象的基础实现
	 */
	protected Collection<T> doGetAll() {
		// 如果启用了过期功能，清理已过期的条目
		if (config.isExpiryEnabled()) {
			cleanupExpiredEntries();
		}
		return new ArrayList<>(idToObject.values());
	}

	/**
	 * 获取所有ID的基础实现
	 */
	protected Collection<ID> doGetIdsAll() {
		// 如果启用了过期功能，清理已过期的条目
		if (config.isExpiryEnabled()) {
			cleanupExpiredEntries();
		}
		return new ArrayList<>(idToObject.keySet());
	}

	/**
	 * 移除对象的基础实现
	 */
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

		return true;
	}

	/**
	 * 清空所有对象的基础实现
	 */
	protected void doClear() {
		idToObject.clear();

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

	// === 可选的存储操作方法 ===
	protected void doAddWithExpiry(ID id, T obj, long expiryTimeMs) {
		if (!config.isExpiryEnabled()) {
			throw new UnsupportedOperationException("Expiry feature not supported");
		}

		doAdd(id, obj);

		// 设置过期时间
		if (expiryTimeMs > 0) {
			expiryMapping.put(id, System.currentTimeMillis() + expiryTimeMs);
		}
	}

	protected Collection<T> doGetBatch(Collection<ID> ids) {
		List<T> result = new ArrayList<>();
		for (ID id : ids) {
			T obj = doGet(id);
			if (obj != null) {
				result.add(obj);
			}
		}
		return result;
	}

	protected Collection<T> doFindByPredicate(Predicate<T> predicate) {
		List<T> result = new ArrayList<>();
		for (T obj : doGetAll()) {
			if (predicate.test(obj)) {
				result.add(obj);
			}
		}
		return result;
	}

	protected boolean doRemoveBatch(Collection<ID> ids) {
		boolean allRemoved = true;
		for (ID id : ids) {
			allRemoved &= doRemove(id);
		}
		return allRemoved;
	}

	protected void doAddBatch(Map<ID, T> objects) {
		if (config.isCapacityLimitEnabled()) {
			// 检查批量添加是否会超过容量限制
			while (idToObject.size() + objects.size() > maxCapacity && !idToObject.isEmpty()) {
				enforceCapacityLimit();
			}
		}

		long currentTime = System.currentTimeMillis();

		// 批量添加到主存储
		idToObject.putAll(objects);

		// 更新可选映射
		if (lastAccessMapping != null || accessCountMapping != null || insertionTimeMapping != null) {
			for (ID id : objects.keySet()) {
				if (lastAccessMapping != null) {
					lastAccessMapping.put(id, currentTime);
				}

				if (accessCountMapping != null) {
					accessCountMapping.put(id, new AtomicInteger(0));
				}

				if (insertionTimeMapping != null) {
					insertionTimeMapping.put(id, currentTime);
				}
			}
		}
	}

	protected void setMaxCapacity(int capacity) {
		if (!config.isCapacityLimitEnabled()) {
			throw new UnsupportedOperationException("Capacity limit feature not supported");
		}

		this.maxCapacity = capacity;
		enforceCapacityLimit();
	}

	protected void setEvictionPolicy(EvictionPolicy policy) {
		if (!config.isEvictionEnabled()) {
			throw new UnsupportedOperationException("Eviction policy feature not supported");
		}

		this.evictionPolicy = policy;
	}

	protected int getTotalObjectCount() {
		return idToObject.size();
	}

	/**
	 * 更新访问统计
	 */
	protected void updateAccessStats(ID id) {
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

	/**
	 * 检查对象是否过期
	 */
	protected boolean isExpired(ID id) {
		if (!config.isExpiryEnabled() || expiryMapping == null) {
			return false;
		}

		Long expiryTime = expiryMapping.get(id);
		return expiryTime != null && System.currentTimeMillis() > expiryTime;
	}

	/**
	 * 清理过期的条目
	 */
	protected void cleanupExpiredEntries() {
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
	 * 强制执行容量限制
	 */
	protected void enforceCapacityLimit() {
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
	protected ID selectIdForEviction() {
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

	// === 基础操作模板方法 ===
	@Override
	public final void add(ID id, T obj) {
		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}
		beforeAdd(obj);
		doAdd(id, obj);
		afterAdd(obj);
		notifyListeners(listener -> listener.onObjectAdded(obj));
	}

	@Override
	public final void addWithExpiry(ID id, T obj, long time, TimeUnit unit) {
		if (!config.isExpiryEnabled()) {
			throw new UnsupportedOperationException("Expiry feature is not enabled");
		}

		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}
		beforeAdd(obj);
		doAddWithExpiry(id, obj, unit.toMillis(time));
		afterAdd(obj);
		notifyListeners(listener -> listener.onObjectAdded(obj));
	}

	@Override
	public final void addBatch(Map<ID, T> objects) {
		if (objects == null || objects.isEmpty()) {
			return;
		}

		// 执行前置处理
		objects.values().forEach(this::beforeAdd);

		// 执行批量添加
		doAddBatch(objects);

		// 执行后置处理
		Collection<T> addedObjects = objects.values();
		for (T obj : addedObjects) {
			afterAdd(obj);
		}

		// 通知添加事件
		notifyBatchAdded(addedObjects);
	}

	@Override
	public final T get(ID id) {
		T obj = doGet(id);
		if (obj != null) {
			notifyListeners(listener -> listener.onObjectAccessed(obj));
		}
		return obj;
	}
	@Override
	public final Collection<T> getBatch(Collection<ID> ids) {
		Collection<T> result = doGetBatch(ids);
		notifyBatchAccessed(result);
		return result;
	}

	@Override
	public final Collection<T> getAll() {
		Collection<T> result = doGetAll();
		notifyBatchAccessed(result);
		return result;
	}

	@Override
	public final Collection<T> findByPredicate(Predicate<T> predicate) {
		Collection<T> result = doFindByPredicate(predicate);
		notifyBatchAccessed(result);
		return result;
	}

	@Override
	public final boolean remove(ID id) {
		T obj = get(id);
		if (obj != null) {
			beforeRemove(obj);
			boolean result = doRemove(id);
			if (result) {
				afterRemove(obj);
				notifyListeners(listener -> listener.onObjectRemoved(obj));
			}
			return result;
		}
		return false;
	}

	@Override
	public final int removeBatch(Collection<ID> ids) {
		if (ids == null || ids.isEmpty()) {
			return 0;
		}

		// 先获取所有要删除的对象
		Map<ID, T> objectsToRemove = new HashMap<>();
		for (ID id : ids) {
			T obj = doGet(id);
			if (obj != null) {
				objectsToRemove.put(id, obj);
				beforeRemove(obj);
			}
		}

		// 执行批量删除
		boolean result = doRemoveBatch(ids);

		// 处理后置回调和事件
		if (result) {
			Collection<T> removedObjects = objectsToRemove.values();
			for (T obj : removedObjects) {
				afterRemove(obj);
			}

			// 通知移除事件
			notifyBatchRemoved(removedObjects);
		}

		return objectsToRemove.size();
	}

	@Override
	public final void clear() {
		Collection<T> allObjects = doGetAll();
		allObjects.forEach(this::beforeRemove);
		doClear();

		for (T obj : allObjects) {
			afterRemove(obj);
		}
		// 通知移除事件
		notifyBatchRemoved(allObjects);
	}

	@Override
	public final void setCapacity(int capacity) {
		if (!config.isCapacityLimitEnabled()) {
			throw new UnsupportedOperationException("Capacity limit feature is not enabled");
		}

		if (capacity <= 0) {
			throw new IllegalArgumentException("Capacity must be greater than zero");
		}
		setMaxCapacity(capacity);
	}

	@Override
	public final int count() {
		return getTotalObjectCount();
	}

	// === 钩子方法 ===
	protected void beforeAdd(T obj) {
	}

	protected void afterAdd(T obj) {
	}

	protected void beforeRemove(T obj) {
	}

	protected void afterRemove(T obj) {
	}

	// === 事件监听机制 ===
	@Override
	public void addListener(ManagerEventListener<T> listener) {
		if (!config.isEventNotificationEnabled()) {
			throw new UnsupportedOperationException("Event notification feature is not enabled");
		}

		if (listener != null) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(ManagerEventListener<T> listener) {
		if (!config.isEventNotificationEnabled()) {
			throw new UnsupportedOperationException("Event notification feature is not enabled");
		}

		listeners.remove(listener);
	}

	protected void notifyListeners(Consumer<ManagerEventListener<T>> action) {
		if (!config.isEventNotificationEnabled() || listeners == null) {
			return;
		}

		for (ManagerEventListener<T> listener : listeners) {
			try {
				action.accept(listener);
			} catch (Exception e) {
				// 记录异常，但不允许监听器影响核心功能
				System.err.println("Error in manager listener: " + e.getMessage());
			}
		}
	}

	/**
	 * 批量通知对象访问事件
	 * 只有在启用了事件通知且有监听器时才进行通知
	 * 
	 * @param objects 需要通知的对象集合
	 * @param eventAction 事件动作
	 */
	protected void notifyBatchEvents(Collection<T> objects, Consumer<T> eventAction) {
		if (config.isEventNotificationEnabled() && listeners != null && !listeners.isEmpty()) {
			for (T obj : objects) {
				eventAction.accept(obj);
			}
		}
	}

	/**
	 * 批量通知对象的访问事件
	 * 
	 * @param objects 被访问的对象集合
	 */
	protected void notifyBatchAccessed(Collection<T> objects) {
		notifyBatchEvents(objects, obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
	}

	/**
	 * 批量通知对象的添加事件
	 * 
	 * @param objects 被添加的对象集合
	 */
	protected void notifyBatchAdded(Collection<T> objects) {
		notifyBatchEvents(objects, obj -> notifyListeners(listener -> listener.onObjectAdded(obj)));
	}

	/**
	 * 批量通知对象的移除事件
	 * 
	 * @param objects 被移除的对象集合
	 */
	protected void notifyBatchRemoved(Collection<T> objects) {
		notifyBatchEvents(objects, obj -> notifyListeners(listener -> listener.onObjectRemoved(obj)));
	}

	@Override
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