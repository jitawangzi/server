package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 抽象标签管理器实现
 * <p>
 * 提供基于标签的对象管理功能，允许为对象添加多个标签，并通过标签检索对象。
 * 标签是扁平结构的，标签之间没有层级关系。
 * </p>
 *
 * @param <ID> 对象标识符类型
 * @param <T> 对象类型
 */
public abstract class AbstractTaggedManager<ID, T> extends AbstractManager<ID, T> implements TaggedManager<ID, T> {

	// 对象ID到标签集合的映射
	protected Map<ID, Set<String>> idToTags = new ConcurrentHashMap<>();

	// 标签到对象集合的映射
	protected Map<String, Set<ID>> tagToObjects = new ConcurrentHashMap<>();

	/**
	 * 默认构造函数
	 */
	public AbstractTaggedManager() {
		super();
	}

	/**
	 * 带配置的构造函数
	 *
	 * @param config 管理器配置
	 */
	public AbstractTaggedManager(ManagerConfig config) {
		super(config);
	}

	/**
	 * 添加带标签的对象
	 */
	@Override
	public final void add(ID id, T obj, String... tags) {
		executeWithEvents(() -> {
			doAdd(id, obj, tags);
			return null;
		}, obj, this::beforeAdd, this::afterAdd, listener -> listener.onObjectAdded(obj));
	}

	/**
	 * 添加带标签的对象实现
	 *
	 * @param id 对象标识符
	 * @param obj 对象
	 * @param tags 标签数组
	 */
	protected void doAdd(ID id, T obj, String... tags) {
		// 添加对象
		super.doAdd(id, obj);
		// 处理标签
		if (tags != null && tags.length > 0) {
			// 获取或创建ID的标签集合
			Set<String> tagSet = idToTags.computeIfAbsent(id, k -> new HashSet<>());
			// 为对象添加标签
			for (String tag : tags) {
				if (tag != null && !tag.isEmpty()) {
					tagSet.add(tag);
					// 更新标签到对象的映射
					Set<ID> objectIds = tagToObjects.computeIfAbsent(tag, k -> new HashSet<>());
					objectIds.add(id);
				}
			}
		}
	}

	/**
	 * 批量添加带标签的对象
	 */
	@Override
	public final void addBatch(Map<ID, T> objects, String... tags) {
		if (objects == null || objects.isEmpty()) {
			return;
		}
		// 执行前置处理
		objects.values().forEach(this::beforeAdd);
		// 执行批量添加
		doAddBatch(objects, tags);
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
	 * 批量添加带标签的对象实现 - 优化版本
	 *
	 * @param objects 对象映射(ID到对象)
	 * @param tags 标签数组
	 */
	protected void doAddBatch(Map<ID, T> objects, String... tags) {
		// 1. 添加对象到存储
		super.doAddBatch(objects);

		// 如果没有标签，直接返回
		if (tags == null || tags.length == 0) {
			return;
		}

		// 过滤有效标签
		List<String> validTags = Arrays.stream(tags).filter(tag -> tag != null && !tag.isEmpty()).collect(Collectors.toList());

		if (validTags.isEmpty()) {
			return;
		}

		// 2. 一次性处理所有标签关联
		for (ID id : objects.keySet()) {
			// 为每个对象创建标签集合
			Set<String> tagSet = idToTags.computeIfAbsent(id, k -> new HashSet<>(validTags.size()));
			tagSet.addAll(validTags);
		}

		// 3. 更新标签到对象的映射
		for (String tag : validTags) {
			Set<ID> objectIds = tagToObjects.computeIfAbsent(tag, k -> new HashSet<>(objects.size()));
			objectIds.addAll(objects.keySet());
		}
	}

	/**
	 * 根据标签获取对象
	 */
	@Override
	public final Collection<T> getByTags(String... tags) {
		Collection<T> result = doGetByLabels(tags);
		// 只有在启用了事件通知且有监听器时才进行通知
		if (config.isEventNotificationEnabled() && listeners != null && !listeners.isEmpty()) {
			result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		}
		return result;
	}

	/**
	 * 根据标签获取对象实现
	 *
	 * @param tags 标签数组
	 * @return 符合条件的对象集合
	 */
	protected Collection<T> doGetByLabels(String... tags) {
		if (tags == null || tags.length == 0) {
			return Collections.emptyList();
		}
		// 获取第一个标签对应的对象ID
		Set<ID> result = new HashSet<>(tagToObjects.getOrDefault(tags[0], Collections.emptySet()));
		// 与其他标签的对象ID取交集
		for (int i = 1; i < tags.length; i++) {
			String tag = tags[i];
			Set<ID> ids = tagToObjects.getOrDefault(tag, Collections.emptySet());
			result.retainAll(ids);
			// 如果交集为空，提前返回
			if (result.isEmpty()) {
				return Collections.emptyList();
			}
		}
		// 获取对象
		return result.stream().map(this::doGet).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * 根据标签获取ID列表
	 */
	@Override
	public final Collection<ID> getIdsByTags(String... tags) {
		return doGetIdsByLabels(tags);
	}

	/**
	 * 根据标签获取ID列表实现
	 *
	 * @param tags 标签数组
	 * @return 符合条件的对象ID集合
	 */
	protected Collection<ID> doGetIdsByLabels(String... tags) {
		if (tags == null || tags.length == 0) {
			return Collections.emptyList();
		}
		// 获取第一个标签对应的对象ID
		Set<ID> result = new HashSet<>(tagToObjects.getOrDefault(tags[0], Collections.emptySet()));
		// 与其他标签的对象ID取交集
		for (int i = 1; i < tags.length; i++) {
			String tag = tags[i];
			Set<ID> ids = tagToObjects.getOrDefault(tag, Collections.emptySet());
			result.retainAll(ids);
			// 如果交集为空，提前返回
			if (result.isEmpty()) {
				return Collections.emptyList();
			}
		}
		return new ArrayList<>(result);
	}

	/**
	 * 移除对象的标签
	 */
	@Override
	public boolean removeTag(ID id, String tag) {
		if (id == null || tag == null || tag.isEmpty()) {
			return false;
		}
		// 从ID到标签的映射中移除
		Set<String> tags = idToTags.get(id);
		if (tags != null) {
			tags.remove(tag);
		}
		// 从标签到对象的映射中移除
		Set<ID> objects = tagToObjects.get(tag);
		if (objects != null) {
			objects.remove(id);
			if (objects.isEmpty()) {
				tagToObjects.remove(tag);
			}
			return true;
		}
		return false;
	}

	/**
	 * 为对象添加标签
	 */
	@Override
	public boolean addTag(ID id, String tag) {
		if (id == null || tag == null || tag.isEmpty() || !exists(id)) {
			return false;
		}
		// 添加到ID到标签的映射
		Set<String> tags = idToTags.computeIfAbsent(id, k -> new HashSet<>());
		tags.add(tag);
		// 添加到标签到对象的映射
		Set<ID> objects = tagToObjects.computeIfAbsent(tag, k -> new HashSet<>());
		return objects.add(id);
	}

	/**
	 * 获取对象的所有标签
	 */
	@Override
	public Set<String> getTags(ID id) {
		if (id == null || !exists(id)) {
			return Collections.emptySet();
		}
		return new HashSet<>(idToTags.getOrDefault(id, Collections.emptySet()));
	}

	/**
	 * 获取标签到对象的映射
	 */
	@Override
	public Map<String, Collection<T>> getTagMap() {
		Map<String, Collection<T>> result = new HashMap<>();
		for (Map.Entry<String, Set<ID>> entry : tagToObjects.entrySet()) {
			String tag = entry.getKey();
			Set<ID> ids = entry.getValue();
			List<T> objects = ids.stream().map(this::doGet).filter(Objects::nonNull).collect(Collectors.toList());
			if (!objects.isEmpty()) {
				result.put(tag, objects);
			}
		}
		return result;
	}

	/**
	 * 删除对象时，同时清理标签关联
	 */
	@Override
	protected boolean doRemove(ID id) {
		boolean removed = super.doRemove(id);
		if (removed) {
			// 获取对象的标签
			Set<String> tags = idToTags.remove(id);
			if (tags != null) {
				// 从每个标签的对象集合中移除该对象
				for (String tag : tags) {
					Set<ID> objects = tagToObjects.get(tag);
					if (objects != null) {
						objects.remove(id);
						if (objects.isEmpty()) {
							tagToObjects.remove(tag);
						}
					}
				}
			}
		}
		return removed;
	}

	/**
	 * 清空所有对象和标签
	 */
	@Override
	protected void doClear() {
		super.doClear();
		idToTags.clear();
		tagToObjects.clear();
	}
}