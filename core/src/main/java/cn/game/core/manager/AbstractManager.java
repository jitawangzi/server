package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 抽象对象管理器实现
 * 提供基础的对象管理功能和事件通知机制
 * 
 * @param <ID> 对象标识符类型
 * @param <T> 对象类型
 */
public abstract class AbstractManager<ID, T> implements Manager<ID, T> {
	// 对象存储
	protected Map<ID, T> storage = new ConcurrentHashMap<>();

	// 配置
	protected ManagerConfig config;

	// 事件监听器
	protected List<ManagerEventListener<T>> listeners;

	// 默认构造函数
	public AbstractManager() {
		this.config = new ManagerConfig();
	}

	// 带配置的构造函数
	public AbstractManager(ManagerConfig config) {
		this.config = config != null ? config : new ManagerConfig();
	}

	/**
	 * 添加对象前的钩子方法
	 */
	protected void beforeAdd(T obj) {
		// 默认实现为空，子类可以覆盖
	}

	/**
	 * 添加对象后的钩子方法
	 */
	protected void afterAdd(T obj) {
		// 默认实现为空，子类可以覆盖
	}

	/**
	 * 移除对象前的钩子方法
	 */
	protected void beforeRemove(T obj) {
		// 默认实现为空，子类可以覆盖
	}

	/**
	 * 移除对象后的钩子方法
	 */
	protected void afterRemove(T obj) {
		// 默认实现为空，子类可以覆盖
	}

	/**
	 * 添加对象
	 */
	@Override
	public final void add(ID id, T obj) {
		beforeAdd(obj);
		doAdd(id, obj);
		afterAdd(obj);
		if (config.isEventNotificationEnabled() && listeners != null) {
			notifyListeners(listener -> listener.onObjectAdded(obj));
		}
	}

	/**
	 * 添加对象实现
	 */
	protected void doAdd(ID id, T obj) {
		storage.put(id, obj);
	}

	/**
	 * 批量添加对象
	 */
	@Override
	public final void addBatch(Map<ID, T> objects) {
		if (objects == null || objects.isEmpty()) {
			return;
		}

		// 执行前置处理
		objects.values().forEach(this::beforeAdd);

		// 执行批量添加
		doAddBatch(objects);

		// 执行后置处理和通知
		boolean needsNotification = config.isEventNotificationEnabled() && listeners != null && !listeners.isEmpty();
		for (T obj : objects.values()) {
			afterAdd(obj);
			if (needsNotification) {
				notifyListeners(listener -> listener.onObjectAdded(obj));
			}
		}
	}

	/**
	 * 批量添加对象实现
	 */
	protected void doAddBatch(Map<ID, T> objects) {
		storage.putAll(objects);
	}

	/**
	 * 获取对象
	 */
	@Override
	public final T get(ID id) {
		T obj = doGet(id);
		if (obj != null && config.isEventNotificationEnabled() && listeners != null) {
			notifyListeners(listener -> listener.onObjectAccessed(obj));
		}
		return obj;
	}

	/**
	 * 获取对象实现
	 */
	protected T doGet(ID id) {
		return storage.get(id);
	}

	/**
	 * 批量获取对象
	 */
	@Override
	public final Collection<T> getBatch(Collection<ID> ids) {
		Collection<T> result = doGetBatch(ids);
		if (config.isEventNotificationEnabled() && listeners != null && !listeners.isEmpty()) {
			result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		}
		return result;
	}

	/**
	 * 批量获取对象实现
	 */
	protected Collection<T> doGetBatch(Collection<ID> ids) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}

		List<T> result = new ArrayList<>(ids.size());
		for (ID id : ids) {
			T obj = doGet(id);
			if (obj != null) {
				result.add(obj);
			}
		}

		return result;
	}

	/**
	 * 获取所有对象
	 */
	@Override
	public final Collection<T> getAll() {
		Collection<T> result = doGetAll();
		if (config.isEventNotificationEnabled() && listeners != null && !listeners.isEmpty()) {
			result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		}
		return result;
	}

	/**
	 * 获取所有对象实现
	 */
	protected Collection<T> doGetAll() {
		return new ArrayList<>(storage.values());
	}

	/**
	 * 根据条件查找对象
	 */
	@Override
	public final Collection<T> findByPredicate(Predicate<T> predicate) {
		Collection<T> result = doFindByPredicate(predicate);
		if (config.isEventNotificationEnabled() && listeners != null && !listeners.isEmpty()) {
			result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		}
		return result;
	}

	/**
	 * 根据条件查找对象实现
	 */
	protected Collection<T> doFindByPredicate(Predicate<T> predicate) {
		if (predicate == null) {
			return Collections.emptyList();
		}

		return storage.values().stream().filter(predicate).collect(Collectors.toList());
	}

	/**
	 * 移除对象
	 */
	@Override
	public final boolean remove(ID id) {
		T obj = doGet(id);
		if (obj == null) {
			return false;
		}

		beforeRemove(obj);
		boolean removed = doRemove(id);

		if (removed) {
			afterRemove(obj);
			if (config.isEventNotificationEnabled() && listeners != null) {
				notifyListeners(listener -> listener.onObjectRemoved(obj));
			}
		}

		return removed;
	}

	/**
	 * 移除对象实现
	 */
	protected boolean doRemove(ID id) {
		return storage.remove(id) != null;
	}

	/**
	 * 批量移除对象
	 */
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
			boolean needsNotification = config.isEventNotificationEnabled() && listeners != null && !listeners.isEmpty();
			for (T obj : objectsToRemove.values()) {
				afterRemove(obj);
				if (needsNotification) {
					notifyListeners(listener -> listener.onObjectRemoved(obj));
				}
			}
		}

		return objectsToRemove.size();
	}

	/**
	 * 批量移除对象实现
	 */
	protected boolean doRemoveBatch(Collection<ID> ids) {
		if (ids == null || ids.isEmpty()) {
			return false;
		}

		ids.forEach(storage::remove);
		return true;
	}

	/**
	 * 移除所有对象
	 */
	@Override
	public final void clear() {
		Collection<T> allObjects = doGetAll();
		allObjects.forEach(this::beforeRemove);
		doClear();

		boolean needsNotification = config.isEventNotificationEnabled() && listeners != null && !listeners.isEmpty();
		for (T obj : allObjects) {
			afterRemove(obj);
			if (needsNotification) {
				notifyListeners(listener -> listener.onObjectRemoved(obj));
			}
		}
	}

	/**
	 * 移除所有对象实现
	 */
	protected void doClear() {
		storage.clear();
	}

	/**
	 * 检查对象是否存在
	 */
	@Override
	public boolean exists(ID id) {
		return storage.containsKey(id);
	}

	/**
	 * 获取对象数量
	 */
	@Override
	public int size() {
		return storage.size();
	}

	/**
	 * 添加事件监听器
	 */
	@Override
	public void addListener(ManagerEventListener<T> listener) {
		if (listener == null) {
			return;
		}

		if (listeners == null) {
			listeners = new ArrayList<>();
		}

		listeners.add(listener);
	}

	/**
	 * 移除事件监听器
	 */
	@Override
	public boolean removeListener(ManagerEventListener<T> listener) {
		if (listeners == null || listener == null) {
			return false;
		}

		return listeners.remove(listener);
	}

	/**
	 * 通知所有监听器
	 */
	protected void notifyListeners(Consumer<ManagerEventListener<T>> action) {
		if (listeners != null && !listeners.isEmpty()) {
			for (ManagerEventListener<T> listener : listeners) {
				action.accept(listener);
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
}