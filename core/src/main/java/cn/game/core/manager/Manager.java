package cn.game.core.manager;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 对象管理器接口
 * 
 * @param <ID> 对象标识符类型
 * @param <T> 对象类型
 */
public interface Manager<ID, T> {
	/**
	 * 添加对象
	 * 
	 * @param id 对象标识符
	 * @param obj 对象
	 */
	void add(ID id, T obj);


	/**
	 * 批量添加对象
	 * 
	 * @param objects 对象映射(ID到对象)
	 */
	void addBatch(Map<ID, T> objects);

	/**
	 * 获取对象
	 * 
	 * @param id 对象标识符
	 * @return 对象，如果不存在则返回null
	 */
	T get(ID id);

	/**
	 * 批量获取对象
	 * 
	 * @param ids 对象标识符集合
	 * @return 对象集合
	 */
	Collection<T> getBatch(Collection<ID> ids);

	/**
	 * 获取所有对象
	 * 
	 * @return 所有对象的集合
	 */
	Collection<T> getAll();

	/**
	 * 根据条件查找对象
	 * 
	 * @param predicate 条件断言
	 * @return 符合条件的对象集合
	 */
	Collection<T> findByPredicate(Predicate<T> predicate);

	/**
	 * 移除对象
	 * 
	 * @param id 对象标识符
	 * @return 如果对象被移除则返回true，否则返回false
	 */
	boolean remove(ID id);

	/**
	 * 批量移除对象
	 * 
	 * @param ids 对象标识符集合
	 * @return 被移除的对象数量
	 */
	int removeBatch(Collection<ID> ids);

	/**
	 * 移除所有对象
	 */
	void clear();

	/**
	 * 检查对象是否存在
	 * 
	 * @param id 对象标识符
	 * @return 如果对象存在则返回true，否则返回false
	 */
	boolean exists(ID id);

	/**
	 * 获取对象数量
	 * 
	 * @return 对象数量
	 */
	int size();

	/**
	 * 添加事件监听器
	 * 
	 * @param listener 事件监听器
	 */
	void addListener(ManagerEventListener<T> listener);

	/**
	 * 移除事件监听器
	 * 
	 * @param listener 事件监听器
	 * @return 如果监听器被移除则返回true，否则返回false
	 */
	boolean removeListener(ManagerEventListener<T> listener);

	/**
	 * 管理器事件监听器接口
	 * 
	 * @param <T> 对象类型
	 */
	interface ManagerEventListener<T> {
		/**
		 * 当对象被访问时调用
		 * 
		 * @param obj 被访问的对象
		 */
		void onObjectAccessed(T obj);

		/**
		 * 当对象被添加时调用
		 * 
		 * @param obj 被添加的对象
		 */
		void onObjectAdded(T obj);

		/**
		 * 当对象被移除时调用
		 * 
		 * @param obj 被移除的对象
		 */
		void onObjectRemoved(T obj);
	}

}