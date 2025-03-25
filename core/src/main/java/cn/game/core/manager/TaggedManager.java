package cn.game.core.manager;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 标签数据管理器接口
 * 扩展基础管理器，添加标签功能
 */
public interface TaggedManager<ID, T> extends Manager<ID, T> {

	/**
	 * 添加带标签的对象
	 */
	void add(ID id, T obj, String... tags);

	/**
	 * 添加带标签和过期时间的对象
	 */
	void addWithExpiry(ID id, T obj, long time, TimeUnit unit, String... tags);

	/**
	 * 批量添加带标签的对象
	 */
	void addBatch(Map<ID, T> objects, String... tags);

	/**
	 * 根据标签获取对象
	 */
	Collection<T> getByTags(String... tags);

	/**
	 * 根据标签获取ID列表
	 */
	Collection<ID> getIdsByTags(String... tags);

	/**
	 * 分页排序获取对象
	 */
	Collection<T> getPagedAndSorted(int page, int size, Comparator<T> comparator, String... tags);

	/**
	 * 设置对象标签
	 */
	void setTags(ID id, String... newTags);

	/**
	 * 获取对象的标签
	 */
	Set<String> getTags(ID id);

	/**
	 * 判断对象是否有指定标签
	 */
	boolean hasTag(ID id, String tag);

	/**
	 * 添加标签到对象
	 */
	void addTag(ID id, String tag);

	/**
	 * 从对象移除标签
	 */
	boolean removeTag(ID id, String tag);

	/**
	 * 获取标签统计
	 */
	Map<String, Integer> getTagStatistics();

	/**
	 * 获取所有标签
	 */
	Set<String> getAllTags();
}