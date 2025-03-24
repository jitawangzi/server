package cn.game.core.manager.m2;

import java.util.Collection;

/**
 * 通用对象管理器接口
 * @param <ID> 对象ID类型（通常为Long或String）
 * @param <T>  管理对象的类型
 */
public interface GenericManager<ID, T> {

	/**
	 * 初始化管理器（例如加载持久化数据）
	 */
	void initialize();

	/**
	 * 添加对象到管理器
	 * @param id          对象唯一标识
	 * @param obj         要添加的对象
	 * @param labelValues 按预设层级顺序的标签值
	 * @throws IllegalArgumentException 当标签值数量不匹配预设层级时抛出
	 */
	void addWithLabels(ID id, T obj, String... labelValues);

	/**
	 * 精确匹配查询（必须提供完整的层级值）
	 * @param labelValues 完整的层级标签值
	 * @return 匹配的所有对象
	 */
	Collection<T> getByExactLabels(String... labelValues);

	/**
	 * 部分匹配查询（匹配前N个层级）
	 * @param labelValues 层级标签值
	 * @return 匹配的所有对象
	 */
	Collection<T> getByPartialLabels(String... labelValues);

	/**
	 * 根据ID获取对象
	 * @param id 对象唯一标识
	 * @return 匹配的对象，未找到返回null
	 */
	T get(ID id);

	/**
	 * 获取所有对象
	 * @return 当前管理的所有对象集合
	 */
	Collection<T> getAll();

	/**
	 * 移除指定对象
	 * @param id 要移除的对象ID
	 * @return 是否成功移除
	 */
	boolean remove(ID id);

	/**
	 * 清空所有数据
	 */
	void clear();
}

