package cn.game.core.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 层级标签管理器接口
 * <p>
 * 在基本标签管理功能基础上，提供层级标签路径支持。允许为对象添加多条标签路径，
 * 每条路径由有序的标签序列组成，表示层级关系。例如：["中国", "广东", "深圳"]表示一条
 * 从国家到省再到市的层级路径。
 * </p>
 * <p>
 * 这种层级结构使得可以按完整路径或部分路径查询对象，例如查询"中国/广东"下的所有对象，
 * 包括所有城市。同时，对象也可以关联多条不同的标签路径，如地理位置、组织结构等。
 * </p>
 *
 * @param <ID> 对象标识符类型
 * @param <T> 对象类型
 */
public interface HierarchicalTagManager<ID, T> extends TaggedManager<ID, T> {

	/**
	 * 添加带标签路径的对象
	 * <p>
	 * 每个标签路径(labelPath)是一个字符串数组，表示从根到叶的层级关系。
	 * 一个对象可以同时关联多条不同的标签路径。
	 * </p>
	 *
	 * @param id 对象标识符
	 * @param obj 对象
	 * @param labelPaths 标签路径数组
	 */
	void addWithPaths(ID id, T obj, String[]... labelPaths);

	/**
	 * 为对象添加标签路径
	 * <p>
	 * 标签路径是一个字符串数组，表示从根到叶的层级关系。
	 * </p>
	 *
	 * @param id 对象标识符
	 * @param path 标签路径
	 * @return 如果路径被添加则返回true，否则返回false
	 */
	boolean addLabelPath(ID id, String... path);

	/**
	 * 移除对象的标签路径
	 *
	 * @param id 对象标识符
	 * @param path 标签路径
	 * @return 如果路径被移除则返回true，否则返回false
	 */
	boolean removeLabelPath(ID id, String... path);

	/**
	 * 根据标签路径获取对象
	 * <p>
	 * 返回完全匹配指定路径的对象，以及路径下所有子节点的对象。
	 * 例如：getByPath("中国", "广东")将返回广东省所有城市中的对象。
	 * </p>
	 *
	 * @param path 标签路径
	 * @return 符合条件的对象集合
	 */
	Collection<T> getByPath(String... path);

	/**
	 * 根据标签路径获取对象ID
	 * <p>
	 * 返回完全匹配指定路径的对象ID，以及路径下所有子节点的对象ID。
	 * </p>
	 *
	 * @param path 标签路径
	 * @return 符合条件的对象ID集合
	 */
	Collection<ID> getIdsByPath(String... path);

	/**
	 * 获取对象的所有标签路径
	 *
	 * @param id 对象标识符
	 * @return 标签路径列表
	 */
	List<String[]> getLabelPaths(ID id);

	/**
	 * 获取路径到对象的映射
	 *
	 * @return 标签路径到对象集合的映射
	 */
	Map<String[], Collection<T>> getPathMap();
}