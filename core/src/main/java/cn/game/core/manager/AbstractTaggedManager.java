package cn.game.core.manager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 抽象标签管理器
 * 实现标签功能
 */
public abstract class AbstractTaggedManager<ID, T> extends AbstractManagerTemplate<ID, T> implements TaggedManager<ID, T> {
	// ID到标签的映射
	protected final ConcurrentHashMap<ID, Set<String>> idToTags = new ConcurrentHashMap<>();

	// 标签到对象的映射
	protected final ConcurrentHashMap<String, Set<T>> tagToObjects = new ConcurrentHashMap<>();

	/**
	 * 使用指定配置创建管理器
	 */
	protected AbstractTaggedManager(ManagerConfig config) {
		super(config);
	}

	/**
	 * 使用默认最小配置创建管理器
	 */
	protected AbstractTaggedManager() {
		super(ManagerConfig.minimal());
	}

	@Override
	public void add(ID id, T obj, String... tags) {
		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}
		beforeAdd(obj);
		doAdd(id, obj, tags);
		afterAdd(obj);
		notifyListeners(listener -> listener.onObjectAdded(obj));
	}

	@Override
	public void addWithExpiry(ID id, T obj, long time, TimeUnit unit, String... tags) {
		if (!config.isExpiryEnabled()) {
			throw new UnsupportedOperationException("Expiry feature is not enabled");
		}

		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}
		beforeAdd(obj);
		doAddWithExpiry(id, obj, unit.toMillis(time), tags);
		afterAdd(obj);
		notifyListeners(listener -> listener.onObjectAdded(obj));
	}

	@Override
	public void addBatch(Map<ID, T> objects, String... tags) {
		if (objects == null || objects.isEmpty()) {
			return;
		}

		// 执行前置处理
		objects.values().forEach(this::beforeAdd);

		// 执行批量添加
		doAddBatch(objects, tags);

		// 执行后置处理
		objects.values().forEach(obj -> {
			afterAdd(obj);
			notifyListeners(listener -> listener.onObjectAdded(obj));
		});
	}

	@Override
	public Collection<T> getByTags(String... tags) {
		Collection<T> result = doGetByLabels(tags);
		result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		return result;
	}

	@Override
	public Collection<ID> getIdsByTags(String... tags) {
		return doGetIdsByLabels(tags);
	}

	@Override
	public Collection<T> getPagedAndSorted(int page, int size, Comparator<T> comparator, String... tags) {
		Collection<T> result = doGetPagedAndSorted(page, size, comparator, tags);
		result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		return result;
	}

	@Override
	public Map<String, Integer> getTagStatistics() {
		return getLabelStatistics();
	}

	@Override
	public Set<String> getAllTags() {
		return new HashSet<>(tagToObjects.keySet());
	}

	/**
	 * 更新对象的标签
	 */
	protected void updateObjectTags(ID id, T obj, String... tags) {
		// 清理旧标签
		Set<String> oldTags = idToTags.get(id);
		if (oldTags != null) {
			for (String tag : oldTags) {
				Set<T> taggedObjects = tagToObjects.get(tag);
				if (taggedObjects != null) {
					taggedObjects.remove(obj);
					if (taggedObjects.isEmpty()) {
						tagToObjects.remove(tag);
					}
				}
			}
		}

		// 设置新标签
		if (tags != null && tags.length > 0) {
			Set<String> tagSet = new HashSet<>(Arrays.asList(tags));
			idToTags.put(id, tagSet);

			// 更新标签到对象的映射
			for (String tag : tags) {
				tagToObjects.computeIfAbsent(tag, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(obj);
			}
		} else {
			idToTags.remove(id);
		}
	}

	@Override
	public void setTags(ID id, String... newTags) {
		T obj = doGet(id);
		if (obj == null) {
			return;
		}

		updateObjectTags(id, obj, newTags);
	}

	@Override
	public Set<String> getTags(ID id) {
		Set<String> tags = idToTags.get(id);
		return tags != null ? new HashSet<>(tags) : Collections.emptySet();
	}

	@Override
	public boolean hasTag(ID id, String tag) {
		Set<String> tags = idToTags.get(id);
		return tags != null && tags.contains(tag);
	}

	@Override
	public void addTag(ID id, String tag) {
		if (tag == null) {
			return;
		}

		T obj = doGet(id);
		if (obj == null) {
			return;
		}

		Set<String> tags = idToTags.computeIfAbsent(id, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
		tags.add(tag);

		// 更新标签到对象的映射
		tagToObjects.computeIfAbsent(tag, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(obj);
	}

	@Override
	public boolean removeTag(ID id, String tag) {
		if (tag == null) {
			return false;
		}

		T obj = doGet(id);
		if (obj == null) {
			return false;
		}

		Set<String> tags = idToTags.get(id);
		if (tags == null) {
			return false;
		}

		boolean removed = tags.remove(tag);
		if (removed) {
			// 更新标签到对象的映射
			Set<T> objects = tagToObjects.get(tag);
			if (objects != null) {
				objects.remove(obj);
				if (objects.isEmpty()) {
					tagToObjects.remove(tag);
				}
			}

			// 如果没有标签了，移除整个映射
			if (tags.isEmpty()) {
				idToTags.remove(id);
			}
		}

		return removed;
	}
}