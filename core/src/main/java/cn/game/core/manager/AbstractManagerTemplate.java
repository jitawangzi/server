package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 抽象管理器模板基类
 */
public abstract class AbstractManagerTemplate<ID, T> {
	// 管理器配置
	protected final ManagerConfig config;

	// 事件监听器列表
	private final List<ManagerEventListener<T>> listeners;

	/**
	 * 创建管理器实例
	 * @param config 管理器配置
	 */
	protected AbstractManagerTemplate(ManagerConfig config) {
		this.config = config;
		this.listeners = config.isEventNotificationEnabled() ? new ArrayList<>() : null;
	}

	/**
	 * 使用默认最小配置创建管理器实例
	 */
	protected AbstractManagerTemplate() {
		this(ManagerConfig.minimal());
	}

	// === 存储操作抽象方法 ===
	protected abstract void doAdd(ID id, T obj, String... labels);

	protected abstract T doGet(ID id);

	protected abstract Collection<T> doGetByLabels(String... labels);

	protected abstract Collection<ID> doGetIdsByLabels(String... labels);

	protected abstract Collection<T> doGetAll();

	protected abstract Collection<ID> doGetIdsAll();

	protected abstract boolean doRemove(ID id);

	protected abstract void doClear();

	// === 可选的存储操作抽象方法 ===
	protected void doAddWithExpiry(ID id, T obj, long expiryTimeMs, String... labels) {
		throw new UnsupportedOperationException("Expiry feature not supported");
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

	protected void doAddBatch(Map<ID, T> objects, String... labels) {
		for (Map.Entry<ID, T> entry : objects.entrySet()) {
			doAdd(entry.getKey(), entry.getValue(), labels);
		}
	}

	protected void setMaxCapacity(int capacity) {
		throw new UnsupportedOperationException("Capacity limit feature not supported");
	}

	protected void setEvictionPolicy(EvictionPolicy policy) {
		throw new UnsupportedOperationException("Eviction policy feature not supported");
	}

	protected Map<String, Integer> getLabelStatistics() {
		throw new UnsupportedOperationException("Label statistics feature not supported");
	}

	protected int getTotalObjectCount() {
		throw new UnsupportedOperationException("Object counting feature not supported");
	}

	// === 基础操作模板方法 ===
	public final void add(ID id, T obj, String... labels) {
		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}
		beforeAdd(obj);
		doAdd(id, obj, labels);
		afterAdd(obj);
		notifyListeners(listener -> listener.onObjectAdded(obj));
	}

	public final void addWithExpiry(ID id, T obj, long time, TimeUnit unit, String... labels) {
		if (!config.isExpiryEnabled()) {
			throw new UnsupportedOperationException("Expiry feature is not enabled");
		}

		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}
		beforeAdd(obj);
		doAddWithExpiry(id, obj, unit.toMillis(time), labels);
		afterAdd(obj);
		notifyListeners(listener -> listener.onObjectAdded(obj));
	}

	public final void addBatch(Map<ID, T> objects, String... labels) {
		if (objects == null || objects.isEmpty()) {
			return;
		}

		// 执行前置处理
		objects.values().forEach(this::beforeAdd);

		// 执行批量添加
		doAddBatch(objects, labels);

		// 执行后置处理
		objects.values().forEach(obj -> {
			afterAdd(obj);
			notifyListeners(listener -> listener.onObjectAdded(obj));
		});
	}

	public final T get(ID id) {
		T obj = doGet(id);
		if (obj != null) {
			notifyListeners(listener -> listener.onObjectAccessed(obj));
		}
		return obj;
	}

	public final Collection<T> getBatch(Collection<ID> ids) {
		Collection<T> result = doGetBatch(ids);
		result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		return result;
	}

	public final Collection<T> getByLabels(String... labels) {
		Collection<T> result = doGetByLabels(labels);
		result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		return result;
	}

	public final Collection<T> getPagedAndSorted(int page, int size, Comparator<T> comparator, String... labels) {
		Collection<T> result = doGetPagedAndSorted(page, size, comparator, labels);
		result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		return result;
	}

	public final Collection<T> getAll() {
		Collection<T> result = doGetAll();
		result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		return result;
	}

	public final Collection<T> findByPredicate(Predicate<T> predicate) {
		Collection<T> result = doFindByPredicate(predicate);
		result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		return result;
	}

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
			objectsToRemove.values().forEach(obj -> {
				afterRemove(obj);
				notifyListeners(listener -> listener.onObjectRemoved(obj));
			});
		}

		return objectsToRemove.size();
	}

	public final void clear() {
		Collection<T> allObjects = doGetAll();
		allObjects.forEach(this::beforeRemove);
		doClear();
		allObjects.forEach(obj -> {
			afterRemove(obj);
			notifyListeners(listener -> listener.onObjectRemoved(obj));
		});
	}

	public final void setCapacity(int capacity) {
		if (!config.isCapacityLimitEnabled()) {
			throw new UnsupportedOperationException("Capacity limit feature is not enabled");
		}

		if (capacity <= 0) {
			throw new IllegalArgumentException("Capacity must be greater than zero");
		}
		setMaxCapacity(capacity);
	}

	public final void setEvictionStrategy(EvictionPolicy policy) {
		if (!config.isEvictionEnabled()) {
			throw new UnsupportedOperationException("Eviction policy feature is not enabled");
		}

		if (policy == null) {
			throw new IllegalArgumentException("Eviction policy cannot be null");
		}
		setEvictionPolicy(policy);
	}

	public final Map<String, Integer> getStatistics() {
		return getLabelStatistics();
	}

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
	public void addListener(ManagerEventListener<T> listener) {
		if (!config.isEventNotificationEnabled()) {
			throw new UnsupportedOperationException("Event notification feature is not enabled");
		}

		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void removeListener(ManagerEventListener<T> listener) {
		if (!config.isEventNotificationEnabled()) {
			throw new UnsupportedOperationException("Event notification feature is not enabled");
		}

		listeners.remove(listener);
	}

	private void notifyListeners(Consumer<ManagerEventListener<T>> action) {
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

	// === 辅助接口 ===
	public interface Predicate<T> {
		boolean test(T obj);
	}

	public interface ManagerEventListener<T> {
		void onObjectAdded(T obj);

		void onObjectRemoved(T obj);

		void onObjectAccessed(T obj);
	}

	public enum EvictionPolicy {
		LEAST_RECENTLY_USED, LEAST_FREQUENTLY_USED, FIRST_IN_FIRST_OUT, RANDOM
	}
}