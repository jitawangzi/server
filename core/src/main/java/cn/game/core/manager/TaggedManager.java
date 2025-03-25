package cn.game.core.manager;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 标签管理器接口
 * <p>
 * 提供基于标签的对象管理功能，允许为对象添加多个标签，并通过标签检索对象。
 * 标签是扁平结构的，标签之间没有层级关系。当使用多个标签查询时，采用AND逻辑（同时满足所有标签）。
 * </p>
 * 
 * @param <ID> 对象标识符类型
 * @param <T> 对象类型
 */
public interface TaggedManager<ID, T> extends Manager<ID, T> {
	/**
	 * 添加带标签的对象
	 * 
	 * @param id 对象标识符
	 * @param obj 对象
	 * @param tags 标签数组
	 */
	void add(ID id, T obj, String... tags);


	/**
	 * 批量添加带标签的对象
	 * 
	 * @param objects 对象映射(ID到对象)
	 * @param tags 标签数组
	 */
	void addBatch(Map<ID, T> objects, String... tags);

	/**
	 * 根据标签获取对象
	 * <p>
	 * 当指定多个标签时，返回同时具有所有指定标签的对象（AND逻辑）
	 * </p>
	 * 
	 * @param tags 标签数组
	 * @return 符合条件的对象集合
	 */
	Collection<T> getByTags(String... tags);

	/**
	 * 根据标签获取对象ID
	 * <p>
	 * 当指定多个标签时，返回同时具有所有指定标签的对象ID（AND逻辑）
	 * </p>
	 * 
	 * @param tags 标签数组
	 * @return 符合条件的对象ID集合
	 */
	Collection<ID> getIdsByTags(String... tags);

	/**
	 * 移除对象的标签
	 * 
	 * @param id 对象标识符
	 * @param tag 标签
	 * @return 如果标签被移除则返回true，否则返回false
	 */
	boolean removeTag(ID id, String tag);

	/**
	 * 为对象添加标签
	 * 
	 * @param id 对象标识符
	 * @param tag 标签
	 * @return 如果标签被添加则返回true，否则返回false
	 */
	boolean addTag(ID id, String tag);

	/**
	 * 获取对象的所有标签
	 * 
	 * @param id 对象标识符
	 * @return 标签集合
	 */
	Set<String> getTags(ID id);

	/**
	 * 获取标签到对象的映射
	 * 
	 * @return 标签到对象集合的映射
	 */
	Map<String, Collection<T>> getTagMap();
}