package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 独占存储实现基类
 */
public abstract class AbstractExclusiveManager<ID, T> extends AbstractManagerTemplate<ID, T> {
	private final ConcurrentHashMap<ID, T> storage = new ConcurrentHashMap<>();
	// 标签到ID的映射
	private final ConcurrentHashMap<String, Set<ID>> labelToIds = new ConcurrentHashMap<>();
	// ID到标签的映射
	private final ConcurrentHashMap<ID, Set<String>> idToLabels = new ConcurrentHashMap<>();

	@Override
	protected void doAdd(ID id, T obj, String... labels) {
		storage.put(id, obj);

		// 更新标签映射
		if (labels.length > 0) {
			Set<String> labelSet = new HashSet<>(Arrays.asList(labels));
			idToLabels.put(id, labelSet);

			for (String label : labels) {
				labelToIds.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(id);
			}
		}
	}

	@Override
	protected T doGet(ID id) {
		return storage.get(id);
	}

	@Override
	protected Collection<T> doGetByLabels(String... labels) {
		if (labels.length == 0) {
			return doGetAll();
		}

		// 查找包含所有指定标签的对象
		Set<ID> result = null;

		for (String label : labels) {
			Set<ID> ids = labelToIds.get(label);
			if (ids == null) {
				return Collections.emptyList();
			}

			if (result == null) {
				result = new HashSet<>(ids);
			} else {
				result.retainAll(ids);
			}

			if (result.isEmpty()) {
				return Collections.emptyList();
			}
		}

		// 将ID转换为对象
		return result.stream().map(storage::get).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	protected Collection<T> doGetAll() {
		return new ArrayList<>(storage.values());
	}

	@Override
	protected boolean doRemove(ID id) {
		T removed = storage.remove(id);
		if (removed == null) {
			return false;
		}

		// 清理标签映射
		Set<String> labels = idToLabels.remove(id);
		if (labels != null) {
			for (String label : labels) {
				Set<ID> ids = labelToIds.get(label);
				if (ids != null) {
					ids.remove(id);
					if (ids.isEmpty()) {
						labelToIds.remove(label);
					}
				}
			}
		}

		return true;
	}

	@Override
	protected void doClear() {
		storage.clear();
		labelToIds.clear();
		idToLabels.clear();
	}

	/**
	 * 重新设置对象标签
	 */
	public void setLabels(ID id, String... newLabels) {
		if (!storage.containsKey(id)) {
			return;
		}

		// 移除旧标签
		Set<String> oldLabels = idToLabels.get(id);
		if (oldLabels != null) {
			for (String label : oldLabels) {
				Set<ID> ids = labelToIds.get(label);
				if (ids != null) {
					ids.remove(id);
					if (ids.isEmpty()) {
						labelToIds.remove(label);
					}
				}
			}
		}

		// 添加新标签
		if (newLabels.length > 0) {
			Set<String> labelSet = new HashSet<>(Arrays.asList(newLabels));
			idToLabels.put(id, labelSet);

			for (String label : newLabels) {
				labelToIds.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(id);
			}
		} else {
			idToLabels.remove(id);
		}
	}

	/**
	 * 获取对象的所有标签
	 */
	public Set<String> getLabels(ID id) {
		Set<String> labels = idToLabels.get(id);
		return labels != null ? new HashSet<>(labels) : Collections.emptySet();
	}

	/**
	 * 获取存储的对象数量
	 */
	public int size() {
		return storage.size();
	}
}