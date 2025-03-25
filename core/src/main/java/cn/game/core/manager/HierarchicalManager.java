package cn.game.core.manager;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 层级数据管理器接口
 * 扩展标签管理器，添加层级路径操作功能
 */
public interface HierarchicalManager<ID, T> extends TaggedManager<ID, T> {

	/**
	 * 使用层级路径添加对象
	 */
	void addWithPaths(ID id, T obj, String[]... labelPaths);

	/**
	 * 使用层级路径添加带过期时间的对象
	 */
	void addWithPathsAndExpiry(ID id, T obj, long time, TimeUnit unit, String[]... labelPaths);

	/**
	 * 根据层级路径获取对象
	 */
	Collection<T> getByPath(String... path);

	/**
	 * 设置对象的标签路径
	 */
	void setLabelPaths(ID id, String[]... labelPaths);

	/**
	 * 获取对象的所有标签路径
	 */
	List<String[]> getLabelPaths(ID id);

	/**
	 * 添加一个标签路径到对象
	 */
	void addLabelPath(ID id, String... labelPath);

	/**
	 * 移除对象的标签路径
	 */
	boolean removeLabelPath(ID id, String... labelPath);
}