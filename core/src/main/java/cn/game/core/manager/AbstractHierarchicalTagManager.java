package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 统一的层级标签管理器实现
 * 支持按ID检索和按层级标签检索
 */
public abstract class AbstractHierarchicalTagManager<ID, T> extends AbstractTaggedManager<ID, T> implements HierarchicalManager<ID, T> {
	// ID到标签路径的映射
	private final ConcurrentHashMap<ID, List<String[]>> idToLabelPaths = new ConcurrentHashMap<>();

	// 层级存储结构，支持按路径快速查找
	private final ConcurrentHashMap<String, Object> hierarchyStorage = new ConcurrentHashMap<>();

	/**
	 * 使用指定配置创建管理器
	 */
	public AbstractHierarchicalTagManager(ManagerConfig config) {
		super(config);
	}

	/**
	 * 使用默认最小配置创建管理器
	 */
	public AbstractHierarchicalTagManager() {
		this(ManagerConfig.minimal());
	}

	/**
	 * 添加对象到存储
	 * 可以同时添加到多个层级路径
	 * 
	 * @param id 对象ID
	 * @param obj 对象实例
	 * @param labels 层级标签路径数组
	 */
	@Override
	protected void doAdd(ID id, T obj, String... labels) {
		// 简单处理为单一路径
		String[][] labelPaths;
		if (labels.length == 0) {
			labelPaths = new String[0][0];
		} else {
			// 作为单一路径处理
			labelPaths = new String[][] { labels };
		}

		doAddWithPaths(id, obj, labelPaths);
	}

	@Override
	protected void doAddBatch(Map<ID, T> objects, String... tags) {
		for (Map.Entry<ID, T> entry : objects.entrySet()) {
			doAdd(entry.getKey(), entry.getValue(), tags);
		}
	}

	/**
	 * 实现抽象标签管理器的标签查询方法
	 */
	@Override
	protected Collection<T> doGetByLabels(String... tags) {
		if (tags == null || tags.length == 0) {
			return doGetAll();
		}

		// 将标签列表作为一个单一路径
		return getByPath(tags);
	}

	/**
	 * 实现抽象标签管理器的ID查询方法
	 */
	@Override
	protected Collection<ID> doGetIdsByLabels(String... tags) {
		Collection<T> objects = doGetByLabels(tags);
		return objects.stream().map(obj -> getIdByObject(obj)).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * 使用层级路径添加对象
	 * 
	 * @param id 对象ID
	 * @param obj 对象实例
	 * @param labelPaths 层级标签路径数组，每个数组代表一个完整路径
	 */
	@Override
	public void addWithPaths(ID id, T obj, String[]... labelPaths) {
		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}
		beforeAdd(obj);
		doAddWithPaths(id, obj, labelPaths);
		afterAdd(obj);
		notifyListeners(listener -> listener.onObjectAdded(obj));
	}

	/**
	 * 实际添加对象到层级路径的实现
	 */
	@SuppressWarnings("unchecked")
	protected void doAddWithPaths(ID id, T obj, String[]... labelPaths) {
		// 使用父类的添加方法，会处理容量限制和可选存储
		super.doAdd(id, obj);

		// 1. 保存ID到标签路径的映射
		List<String[]> paths = new ArrayList<>();
		for (String[] path : labelPaths) {
			if (path != null && path.length > 0) {
				paths.add(path);
			}
		}

		if (!paths.isEmpty()) {
			idToLabelPaths.put(id, paths);
		}

		// 2. 更新常规标签映射
		Set<String> allTags = new HashSet<>();
		for (String[] path : labelPaths) {
			if (path != null) {
				for (String segment : path) {
					if (segment != null && !segment.isEmpty()) {
						allTags.add(segment);
					}
				}
			}
		}
		updateObjectTags(id, obj, allTags.toArray(new String[0]));

		// 3. 添加到层级存储结构
		if (labelPaths.length == 0) {
			// 如果没有路径，直接添加到根节点
			hierarchyStorage.put(id.toString(), obj);
			return;
		}

		// 对于每个路径，都将对象添加到对应位置
		for (String[] path : labelPaths) {
			if (path == null || path.length == 0) {
				hierarchyStorage.put(id.toString(), obj);
				continue;
			}

			ConcurrentHashMap<String, Object> current = hierarchyStorage;

			// 沿路径构建或导航层级结构
			for (int i = 0; i < path.length - 1; i++) {
				String segment = path[i];
				Object next = current.get(segment);

				if (next == null || !(next instanceof ConcurrentHashMap)) {
					ConcurrentHashMap<String, Object> newMap = new ConcurrentHashMap<>();
					current.put(segment, newMap);
					current = newMap;
				} else {
					current = (ConcurrentHashMap<String, Object>) next;
				}
			}

			// 获取最后一级节点
			String lastSegment = path[path.length - 1];

			// 检查最后一级是否已经有一个Map
			Object lastLevel = current.get(lastSegment);

			if (lastLevel == null) {
				// 创建叶子节点存储
				ConcurrentHashMap<ID, T> leafMap = new ConcurrentHashMap<>();
				leafMap.put(id, obj);
				current.put(lastSegment, leafMap);
			} else if (lastLevel instanceof ConcurrentHashMap) {
				// 检查是否是叶子节点还是中间节点
				ConcurrentHashMap<?, ?> map = (ConcurrentHashMap<?, ?>) lastLevel;
				if (!map.isEmpty()) {
					Object firstValue = map.values().iterator().next();
					if (firstValue instanceof ConcurrentHashMap) {
						// 这是中间节点，转换为叶子节点
						ConcurrentHashMap<ID, T> leafMap = new ConcurrentHashMap<>();
						leafMap.put(id, obj);
						current.put(lastSegment, leafMap);
					} else {
						// 这是叶子节点，直接添加
						((ConcurrentHashMap<ID, T>) map).put(id, obj);
					}
				} else {
					// 空Map，假设是叶子节点
					((ConcurrentHashMap<ID, T>) map).put(id, obj);
				}
			} else {
				// 最后一级是具体对象，需要转换为Map
				ConcurrentHashMap<ID, T> leafMap = new ConcurrentHashMap<>();
				leafMap.put(id, obj);
				current.put(lastSegment, leafMap);
			}
		}
	}

	@Override
	protected void doAddWithExpiry(ID id, T obj, long expiryTimeMs, String... labels) {
		if (!config.isExpiryEnabled()) {
			throw new UnsupportedOperationException("Expiry feature is not enabled");
		}

		// 调用父类设置过期时间
		super.doAddWithExpiry(id, obj, expiryTimeMs);

		// 然后添加到路径
		doAdd(id, obj, labels);
	}

	/**
	 * 使用层级路径添加带过期时间的对象
	 */
	@Override
	public void addWithPathsAndExpiry(ID id, T obj, long time, TimeUnit unit, String[]... labelPaths) {
		if (!config.isExpiryEnabled()) {
			throw new UnsupportedOperationException("Expiry feature is not enabled");
		}

		if (obj == null || id == null) {
			throw new IllegalArgumentException("Object or id cannot be null");
		}

		beforeAdd(obj);

		// 调用父类设置过期时间
		super.doAddWithExpiry(id, obj, unit.toMillis(time));

		// 然后添加到路径
		doAddWithPaths(id, obj, labelPaths);

		afterAdd(obj);
		notifyListeners(listener -> listener.onObjectAdded(obj));
	}

	/**
	 * 根据层级路径获取对象集合
	 * 
	 * @param path 层级路径
	 * @return 该路径下的所有对象
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Collection<T> getByPath(String... path) {
		if (path == null || path.length == 0) {
			return doGetAll();
		}

		Object current = hierarchyStorage;

		// 沿路径导航
		for (String segment : path) {
			if (!(current instanceof Map)) {
				return Collections.emptyList();
			}

			Map<String, Object> map = (Map<String, Object>) current;
			current = map.get(segment);

			if (current == null) {
				return Collections.emptyList();
			}
		}

		// 找到了最终节点
		if (current instanceof Map) {
			Map<?, ?> finalMap = (Map<?, ?>) current;

			// 判断是叶子节点还是中间节点
			if (!finalMap.isEmpty()) {
				Object firstValue = finalMap.values().iterator().next();
				if (firstValue instanceof Map) {
					// 这是中间节点，需要收集所有叶子节点
					List<T> result = new ArrayList<>();
					collectLeafValues((Map<String, Object>) current, result);
					return result;
				} else {
					// 这是叶子节点，直接返回值
					return new ArrayList<>(((Map<ID, T>) finalMap).values());
				}
			} else {
				return Collections.emptyList();
			}
		} else if (current instanceof Collection) {
			return (Collection<T>) current;
		} else if (current != null) {
			return Collections.singletonList((T) current);
		}

		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	private void collectLeafValues(Map<String, Object> map, List<T> result) {
		for (Object value : map.values()) {
			if (value instanceof Map) {
				Map<?, ?> valueMap = (Map<?, ?>) value;
				if (!valueMap.isEmpty()) {
					Object firstValue = valueMap.values().iterator().next();
					if (firstValue instanceof Map) {
						// 递归收集子Map的值
						collectLeafValues((Map<String, Object>) value, result);
					} else {
						// 这是叶子节点，添加所有值
						result.addAll(((Map<ID, T>) value).values());
					}
				}
			} else if (value != null) {
				// 这是单个对象
				result.add((T) value);
			}
		}
	}

	/**
	 * 根据对象查找ID
	 */
	private ID getIdByObject(T obj) {
		for (Map.Entry<ID, T> entry : super.idToObject.entrySet()) {
			if (entry.getValue().equals(obj)) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	protected boolean doRemove(ID id) {
		T removed = idToObject.get(id);
		if (removed == null) {
			return false;
		}

		// 从层级存储中移除
		List<String[]> paths = idToLabelPaths.remove(id);
		if (paths != null) {
			for (String[] path : paths) {
				removeFromPath(hierarchyStorage, id, path, 0);
			}
		} else {
			// 如果没有路径记录，尝试从根节点移除
			hierarchyStorage.remove(id.toString());
		}

		// 调用父类移除方法处理标签、可选映射和存储
		return super.doRemove(id);
	}

	@SuppressWarnings("unchecked")
	private boolean removeFromPath(Map<String, Object> current, ID id, String[] path, int depth) {
		if (path == null || depth >= path.length) {
			return false;
		}

		String segment = path[depth];
		Object next = current.get(segment);

		if (next == null) {
			return false;
		}

		if (depth == path.length - 1) {
			// 最后一级节点
			if (next instanceof Map) {
				Map<?, ?> leafMap = (Map<?, ?>) next;
				boolean removed = leafMap.remove(id) != null;

				if (leafMap.isEmpty()) {
					current.remove(segment);
				}

				return removed;
			}
			return false;
		} else {
			// 中间节点
			if (next instanceof Map) {
				boolean removed = removeFromPath((Map<String, Object>) next, id, path, depth + 1);

				if (removed && ((Map<?, ?>) next).isEmpty()) {
					current.remove(segment);
				}

				return removed;
			}
			return false;
		}
	}

	@Override
	protected void doClear() {
		// 清理层级存储和路径映射
		hierarchyStorage.clear();
		idToLabelPaths.clear();

		// 调用父类清理方法处理标签、可选映射和存储
		super.doClear();
	}

	@Override
	protected Map<String, Integer> getLabelStatistics() {
		Map<String, Integer> stats = new HashMap<>();

		// 遍历所有ID-路径映射，统计每个标签的使用次数
		for (List<String[]> pathsList : idToLabelPaths.values()) {
			for (String[] path : pathsList) {
				for (String segment : path) {
					stats.put(segment, stats.getOrDefault(segment, 0) + 1);
				}
			}
		}

		return stats;
	}

	/**
	 * 设置对象的标签路径
	 * 
	 * @param id 对象ID
	 * @param labelPaths 新的标签路径数组
	 */
	@Override
	public void setLabelPaths(ID id, String[]... labelPaths) {
		T obj = super.idToObject.get(id);
		if (obj == null) {
			return;
		}

		// 移除对象的所有现有路径
		List<String[]> oldPaths = idToLabelPaths.get(id);
		if (oldPaths != null) {
			for (String[] path : oldPaths) {
				removeFromPath(hierarchyStorage, id, path, 0);
			}
			idToLabelPaths.remove(id);
		}

		// 重新添加对象到新路径
		doAddWithPaths(id, obj, labelPaths);
	}

	/**
	 * 获取对象的所有标签路径
	 * 
	 * @param id 对象ID
	 * @return 标签路径列表
	 */
	@Override
	public List<String[]> getLabelPaths(ID id) {
		List<String[]> paths = idToLabelPaths.get(id);
		if (paths == null) {
			return Collections.emptyList();
		}

		// 返回路径的深拷贝
		List<String[]> result = new ArrayList<>(paths.size());
		for (String[] path : paths) {
			result.add(Arrays.copyOf(path, path.length));
		}
		return result;
	}

	/**
	 * 添加一个标签路径到对象
	 * 
	 * @param id 对象ID
	 * @param labelPath 新的标签路径
	 */
	@Override
	public void addLabelPath(ID id, String... labelPath) {
		T obj = super.idToObject.get(id);
		if (obj == null || labelPath == null || labelPath.length == 0) {
			return;
		}

		// 获取现有路径
		List<String[]> existingPaths = idToLabelPaths.computeIfAbsent(id, k -> new ArrayList<>());

		// 检查是否已存在相同路径
		for (String[] path : existingPaths) {
			if (Arrays.equals(path, labelPath)) {
				return; // 路径已存在，不需要添加
			}
		}

		// 添加新路径
		existingPaths.add(labelPath);

		// 构建层级存储
		addObjectToPath(id, obj, labelPath);

		// 更新标签
		updateTagsFromPaths(id);
	}

	/**
	 * 添加对象到路径
	 */
	@SuppressWarnings("unchecked")
	private void addObjectToPath(ID id, T obj, String[] path) {
		if (path == null || path.length == 0) {
			hierarchyStorage.put(id.toString(), obj);
			return;
		}

		ConcurrentHashMap<String, Object> current = hierarchyStorage;

		// 沿路径构建或导航层级结构
		for (int i = 0; i < path.length - 1; i++) {
			String segment = path[i];
			Object next = current.get(segment);

			if (next == null || !(next instanceof ConcurrentHashMap)) {
				ConcurrentHashMap<String, Object> newMap = new ConcurrentHashMap<>();
				current.put(segment, newMap);
				current = newMap;
			} else {
				current = (ConcurrentHashMap<String, Object>) next;
			}
		}

		// 获取最后一级节点
		String lastSegment = path[path.length - 1];

		// 检查最后一级是否已经有一个Map
		Object lastLevel = current.get(lastSegment);

		if (lastLevel == null) {
			// 创建叶子节点存储
			ConcurrentHashMap<ID, T> leafMap = new ConcurrentHashMap<>();
			leafMap.put(id, obj);
			current.put(lastSegment, leafMap);
		} else if (lastLevel instanceof ConcurrentHashMap) {
			// 检查是否是叶子节点还是中间节点
			ConcurrentHashMap<?, ?> map = (ConcurrentHashMap<?, ?>) lastLevel;
			if (!map.isEmpty()) {
				Object firstValue = map.values().iterator().next();
				if (firstValue instanceof ConcurrentHashMap) {
					// 这是中间节点，转换为叶子节点
					ConcurrentHashMap<ID, T> leafMap = new ConcurrentHashMap<>();
					leafMap.put(id, obj);
					current.put(lastSegment, leafMap);
				} else {
					// 这是叶子节点，直接添加
					((ConcurrentHashMap<ID, T>) map).put(id, obj);
				}
			} else {
				// 空Map，假设是叶子节点
				((ConcurrentHashMap<ID, T>) map).put(id, obj);
			}
		} else {
			// 最后一级是具体对象，需要转换为Map
			ConcurrentHashMap<ID, T> leafMap = new ConcurrentHashMap<>();
			leafMap.put(id, obj);
			current.put(lastSegment, leafMap);
		}
	}

	/**
	 * 移除对象的标签路径
	 * 
	 * @param id 对象ID
	 * @param labelPath 要移除的标签路径
	 * @return 是否成功移除
	 */
	@Override
	public boolean removeLabelPath(ID id, String... labelPath) {
		if (labelPath == null || labelPath.length == 0) {
			return false;
		}

		List<String[]> paths = idToLabelPaths.get(id);
		if (paths == null) {
			return false;
		}

		// 查找匹配的路径
		boolean removed = false;
		for (Iterator<String[]> it = paths.iterator(); it.hasNext();) {
			String[] path = it.next();
			if (Arrays.equals(path, labelPath)) {
				it.remove();
				removed = true;
				break;
			}
		}

		if (removed) {
			// 从旧路径移除对象
			removeFromPath(hierarchyStorage, id, labelPath, 0);

			// 如果没有路径了，完全移除对象
			if (paths.isEmpty()) {
				idToLabelPaths.remove(id);

				// 如果对象不在任何路径下，重新添加到根节点
				T obj = super.idToObject.get(id);
				if (obj != null) {
					hierarchyStorage.put(id.toString(), obj);
				}
			}

			// 更新标签
			updateTagsFromPaths(id);
		}

		return removed;
	}

	/**
	 * 根据路径更新对象的标签
	 */
	private void updateTagsFromPaths(ID id) {
		T obj = super.idToObject.get(id);
		if (obj == null) {
			return;
        }

		List<String[]> paths = idToLabelPaths.get(id);
		Set<String> allTags = new HashSet<>();

		if (paths != null) {
			for (String[] path : paths) {
				if (path != null) {
					for (String segment : path) {
						if (segment != null && !segment.isEmpty()) {
							allTags.add(segment);
						}
					}
				}
            }
        }

		updateObjectTags(id, obj, allTags.toArray(new String[0]));
	}
}