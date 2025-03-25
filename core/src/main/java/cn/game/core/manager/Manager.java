package cn.game.core.manager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 数据管理器基础接口
 * 定义数据管理的核心操作，不涉及标签或层级概念
 */
public interface Manager<ID, T> {

	/**
	 * 添加对象到管理器
	 */
	void add(ID id, T obj);

	/**
	 * 添加带过期时间的对象
	 */
	void addWithExpiry(ID id, T obj, long time, TimeUnit unit);

	/**
	 * 批量添加对象
	 */
	void addBatch(Map<ID, T> objects);

	/**
	 * 根据ID获取对象
	 */
	T get(ID id);

	/**
	 * 批量获取对象
	 */
	Collection<T> getBatch(Collection<ID> ids);

	/**
	 * 获取所有对象
	 */
	Collection<T> getAll();

	/**
	 * 根据谓词查找对象
	 */
	Collection<T> findByPredicate(Predicate<T> predicate);

	/**
	 * 删除对象
	 */
	boolean remove(ID id);

	/**
	 * 批量删除对象
	 */
	int removeBatch(Collection<ID> ids);

	/**
	 * 清空所有对象
	 */
	void clear();

	/**
	 * 设置最大容量
	 */
	void setCapacity(int capacity);

	/**
	 * 获取对象总数
	 */
	int count();

	/**
	 * 添加事件监听器
	 */
	void addListener(ManagerEventListener<T> listener);

	/**
	 * 移除事件监听器
	 */
	void removeListener(ManagerEventListener<T> listener);

	/**
	 * 关闭管理器，释放资源
	 */
	void shutdown();

	/**
	 * 对象谓词接口
	 */
	interface Predicate<T> {
		boolean test(T obj);
	}

	/**
	 * 管理器事件监听器接口
	 */
	interface ManagerEventListener<T> {
		void onObjectAdded(T obj);

		void onObjectRemoved(T obj);

		void onObjectAccessed(T obj);
	}

	/**
	 * 淘汰策略枚举
	 */
	enum EvictionPolicy {
		LEAST_RECENTLY_USED, LEAST_FREQUENTLY_USED, FIRST_IN_FIRST_OUT, RANDOM
	}
}