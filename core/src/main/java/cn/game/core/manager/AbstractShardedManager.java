package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 分片存储实现基类
 */
public abstract class AbstractShardedManager<ID, T> extends AbstractManagerTemplate<ID, T> {
	private final ConcurrentHashMap<String, Object> shardedStorage = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<ID, T> idMapping = new ConcurrentHashMap<>();
	// 标签映射，用于快速查找带有特定标签的对象
	private final ConcurrentHashMap<String, Set<ID>> labelMapping = new ConcurrentHashMap<>();

	@Override
	protected void doAdd(ID id, T obj, String... labels) {
		// 先添加到ID映射中
		idMapping.put(id, obj);

		if (labels.length == 0) {
			// 没有标签时，添加到根存储
			shardedStorage.put(id.toString(), obj);
			return;
		}

		// 分片存储通用实现
		ConcurrentHashMap<String, Object> current = shardedStorage;
		for (int i = 0; i < labels.length - 1; i++) {
			current = (ConcurrentHashMap<String, Object>) current.computeIfAbsent(labels[i], k -> new ConcurrentHashMap<>());
		}

		String lastLabel = labels[labels.length - 1];
		ConcurrentHashMap<ID, T> leaf = (ConcurrentHashMap<ID, T>) current.computeIfAbsent(lastLabel, k -> new ConcurrentHashMap<>());

		leaf.put(id, obj);

		// 更新标签映射
		for (String label : labels) {
			labelMapping.computeIfAbsent(label, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(id);
		}
	}

	@Override
	protected T doGet(ID id) {
		return idMapping.get(id);
	}

	@Override
	protected Collection<T> doGetByLabels(String... labels) {
		if (labels.length == 0) {
			return doGetAll();
		}

		// 使用标签映射高效查询
		Set<ID> result = null;

		// 找出具有所有标签的ID集合（取交集）
		for (String label : labels) {
			Set<ID> ids = labelMapping.get(label);
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

		// 将ID转换为对应的对象
		return result.stream().map(this::doGet).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	protected Collection<T> doGetAll() {
		return new ArrayList<>(idMapping.values());
	}

	@Override
	protected boolean doRemove(ID id) {
		T obj = idMapping.remove(id);
		if (obj == null) {
			return false;
		}

		// 从标签映射中移除
		for (Set<ID> ids : labelMapping.values()) {
			ids.remove(id);
		}

		// 清理空的标签映射
		labelMapping.entrySet().removeIf(entry -> entry.getValue().isEmpty());

		// 从分片存储中移除（这里简化处理，实际上可能需要遍历）
		removeFromShards(shardedStorage, id);

		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean removeFromShards(Map<String, Object> map, ID id) {
		boolean removed = false;

		// 遍历当前层级的所有项
		for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Object> entry = it.next();
			Object value = entry.getValue();

			if (value instanceof Map) {
				// 递归处理子Map
				if (removeFromShards((Map<String, Object>) value, id)) {
					removed = true;
					// 如果子Map现在为空，可以移除它
					if (((Map<?, ?>) value).isEmpty()) {
						it.remove();
					}
				}
			} else if (value instanceof ConcurrentHashMap && ((ConcurrentHashMap<?, ?>) value).containsKey(id)) {
				// 如果是叶子节点且包含目标ID
				((ConcurrentHashMap<ID, T>) value).remove(id);
				removed = true;
				// 如果Map现在为空，可以移除它
				if (((Map<?, ?>) value).isEmpty()) {
					it.remove();
				}
			}
		}

		return removed;
	}

	@Override
	protected void doClear() {
		shardedStorage.clear();
		idMapping.clear();
		labelMapping.clear();
	}

	/**
	 * 获取指定标签的对象数量
	 */
	public int getCountByLabel(String label) {
		Set<ID> ids = labelMapping.get(label);
		return ids != null ? ids.size() : 0;
	}

	/**
	 * 获取所有标签
	 */
	public Set<String> getAllLabels() {
		return new HashSet<>(labelMapping.keySet());
	}
}