package cn.game.core.manager;

import java.util.ArrayList;
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
 * 抽象层级标签管理器
 * <p>
 * 在标签管理基础上支持标签层级路径功能。允许为对象添加多条标签路径，
 * 每条路径由有序的标签序列组成，表示层级关系。例如：["中国", "广东", "深圳"]。
 * </p>
 * <p>
 * 这种层级结构使得可以按完整路径或部分路径查询对象，例如查询"中国/广东"下的所有对象，
 * 包括所有城市。同时，对象也可以关联多条不同的标签路径。
 * </p>
 *
 * @param <ID> 对象标识符类型
 * @param <T> 对象类型
 */
public abstract class AbstractHierarchicalTagManager<ID, T> extends AbstractTaggedManager<ID, T> implements HierarchicalTagManager<ID, T> {

	// 对象ID到标签路径的映射
	protected Map<ID, List<TagPath>> idToLabelPaths = new ConcurrentHashMap<>();

	// 层级标签路径存储
	protected Node<ID> hierarchyStorage = new Node<>();

	/**
	 * 默认构造函数
	 */
	public AbstractHierarchicalTagManager() {
		super();
	}

	/**
	 * 带配置的构造函数
	 *
	 * @param config 管理器配置
	 */
	public AbstractHierarchicalTagManager(ManagerConfig config) {
		super(config);
	}

	/**
	 * 添加带标签路径的对象
	 */
	@Override
	public final void addWithPaths(ID id, T obj, String[]... labelPaths) {
		executeWithEvents(() -> {
			beforeAdd(obj);
			// 添加对象
			doAdd(id, obj);
			// 添加标签路径
			if (labelPaths != null) {
				for (String[] path : labelPaths) {
					addLabelPath(id, path);
				}
			}
			return null;
		}, obj, null, // 已在操作中调用beforeAdd
				this::afterAdd, listener -> listener.onObjectAdded(obj));
	}

	/**
	 * 为对象添加标签路径
	 */
	@Override
	public boolean addLabelPath(ID id, String... path) {
		if (id == null || path == null || path.length == 0 || !exists(id)) {
			return false;
		}

		// 创建TagPath对象
		TagPath tagPath = new TagPath(path);

		// 添加到ID到路径的映射
		List<TagPath> paths = idToLabelPaths.computeIfAbsent(id, k -> new ArrayList<>());
		// 检查路径是否已存在
		if (paths.contains(tagPath)) {
			// 路径已存在
			return false;
		}

		// 添加新路径
		paths.add(tagPath);

		// 添加到层级存储
		Node<ID> current = hierarchyStorage;
		for (int i = 0; i < tagPath.length(); i++) {
			String label = tagPath.get(i);
			current = current.getOrCreateChild(label);
		}
		current.addObject(id);

		// 添加单个标签
		for (int i = 0; i < tagPath.length(); i++) {
			super.addTag(id, tagPath.get(i));
		}

		return true;
	}

	/**
	 * 为对象添加标签路径 - 便捷方法
	 */
	public boolean addLabelPath(ID id, String pathString) {
		if (pathString == null || pathString.isEmpty()) {
			return false;
		}
		return addLabelPath(id, pathString.split("/"));
	}

	/**
	 * 移除对象的标签路径
	 */
	@Override
	public boolean removeLabelPath(ID id, String... path) {
		if (id == null || path == null || path.length == 0) {
			return false;
		}

		// 创建TagPath对象
		TagPath tagPath = new TagPath(path);

		// 从ID到路径的映射中移除
		List<TagPath> paths = idToLabelPaths.get(id);
		if (paths == null) {
			return false;
		}

		boolean removed = paths.remove(tagPath);
		if (!removed) {
			return false;
		}

		// 从层级存储中移除
		Node<ID> current = hierarchyStorage;
		for (int i = 0; i < tagPath.length(); i++) {
			Node<ID> child = current.getChild(tagPath.get(i));
			if (child == null) {
				return false;
			}
			current = child;
		}
		current.removeObject(id);

		// 如果这是对象的最后一个路径，清理空节点
		if (paths.isEmpty()) {
			idToLabelPaths.remove(id);
			cleanupEmptyNodes(hierarchyStorage, tagPath, 0);
		}

		return true;
	}

	/**
	 * 移除对象的标签路径 - 便捷方法
	 */
	public boolean removeLabelPath(ID id, String pathString) {
		if (pathString == null || pathString.isEmpty()) {
			return false;
		}
		return removeLabelPath(id, pathString.split("/"));
	}

	/**
	 * 清理没有对象的空节点
	 *
	 * @param node 当前节点
	 * @param path 要清理的路径
	 * @param index 当前路径索引
	 * @return 如果节点为空则返回true
	 */
	private boolean cleanupEmptyNodes(Node<ID> node, TagPath path, int index) {
		if (index >= path.length()) {
			return node.isEmpty();
		}

		Node<ID> child = node.getChild(path.get(index));
		if (child == null) {
			return false;
		}

		if (cleanupEmptyNodes(child, path, index + 1)) {
			node.removeChild(path.get(index));
			return node.isEmpty();
		}

		return false;
	}

	/**
	 * 根据标签路径获取对象
	 */
	@Override
	public final Collection<T> getByPath(String... path) {
		Collection<ID> ids = getIdsByPath(path);
		List<T> result = ids.stream().map(this::doGet).filter(Objects::nonNull).collect(Collectors.toList());
		// 只有在启用了事件通知且有监听器时才进行通知
		if (config.isEventNotificationEnabled() && listeners != null && !listeners.isEmpty()) {
			result.forEach(obj -> notifyListeners(listener -> listener.onObjectAccessed(obj)));
		}
		return result;
	}

	/**
	 * 根据标签路径获取对象 - 便捷方法
	 */
	public Collection<T> getByPathString(String pathString) {
		if (pathString == null || pathString.isEmpty()) {
			return Collections.emptyList();
		}
		return getByPath(pathString.split("/"));
	}

	/**
	 * 根据标签路径获取ID集合
	 */
	@Override
	public Collection<ID> getIdsByPath(String... path) {
		if (path == null || path.length == 0) {
			return Collections.emptyList();
		}

		// 导航到路径节点
		Node<ID> current = hierarchyStorage;
		for (String label : path) {
			Node<ID> child = current.getChild(label);
			if (child == null) {
				return Collections.emptyList();
			}
			current = child;
		}

		// 获取该节点及所有子节点的对象
		return current.getAllObjects();
	}

	/**
	 * 根据标签路径获取ID集合 - 便捷方法
	 */
	public Collection<ID> getIdsByPathString(String pathString) {
		if (pathString == null || pathString.isEmpty()) {
			return Collections.emptyList();
		}
		return getIdsByPath(pathString.split("/"));
	}

	/**
	 * 获取对象的所有标签路径
	 */
	@Override
	public List<String[]> getLabelPaths(ID id) {
		if (id == null || !exists(id)) {
			return Collections.emptyList();
		}

		List<TagPath> paths = idToLabelPaths.get(id);
		if (paths == null) {
			return Collections.emptyList();
		}

		// 返回路径的副本，防止外部修改
		List<String[]> result = new ArrayList<>(paths.size());
		for (TagPath path : paths) {
			result.add(path.getPath());
		}

		return result;
	}

	/**
	 * 获取路径到对象的映射
	 */
	@Override
	public Map<String[], Collection<T>> getPathMap() {
		Map<String[], Collection<T>> result = new HashMap<>();

		// 按TagPath分组
		Map<TagPath, List<ID>> pathToIds = new HashMap<>();

		// 收集每个路径下的所有ID
		for (Map.Entry<ID, List<TagPath>> entry : idToLabelPaths.entrySet()) {
			ID id = entry.getKey();
			if (!exists(id)) {
				continue;
			}

			List<TagPath> paths = entry.getValue();
			for (TagPath path : paths) {
				List<ID> ids = pathToIds.computeIfAbsent(path, k -> new ArrayList<>());
				ids.add(id);
			}
		}

		// 转换为标签路径数组到对象集合的映射
		for (Map.Entry<TagPath, List<ID>> entry : pathToIds.entrySet()) {
			TagPath path = entry.getKey();
			List<ID> ids = entry.getValue();

			// 获取对象并过滤null
			List<T> objects = ids.stream().map(this::doGet).filter(Objects::nonNull).collect(Collectors.toList());

			if (!objects.isEmpty()) {
				result.put(path.getPath(), objects);
			}
		}

		return result;
	}

	/**
	 * 删除对象时，同时清理标签路径关联
	 */
	@Override
	protected boolean doRemove(ID id) {
		boolean removed = super.doRemove(id);
		if (removed) {
			// 获取对象的标签路径
			List<TagPath> paths = idToLabelPaths.remove(id);
			if (paths != null) {
				// 从层级存储中移除该对象
				for (TagPath path : paths) {
					Node<ID> current = hierarchyStorage;
					boolean validPath = true;

					for (int i = 0; i < path.length(); i++) {
						Node<ID> child = current.getChild(path.get(i));
						if (child == null) {
							validPath = false;
							break;
						}
						current = child;
					}

					if (validPath) {
						current.removeObject(id);
						cleanupEmptyNodes(hierarchyStorage, path, 0);
					}
				}
			}
		}
		return removed;
	}

	/**
	 * 清空所有对象和标签路径
	 */
	@Override
	protected void doClear() {
		super.doClear();
		idToLabelPaths.clear();
		hierarchyStorage = new Node<>();
	}

	/**
	 * 层级存储的节点类
	 *
	 * @param <ID> 对象标识符类型
	 */
	protected static class Node<ID> {

		private final Map<String, Node<ID>> children = new HashMap<>();

		private final Set<ID> objects = new HashSet<>();

		/**
		 * 获取子节点
		 *
		 * @param label 标签
		 * @return 子节点，如果不存在则返回null
		 */
		public Node<ID> getChild(String label) {
			return children.get(label);
		}

		/**
		 * 获取或创建子节点
		 *
		 * @param label 标签
		 * @return 子节点
		 */
		public Node<ID> getOrCreateChild(String label) {
			return children.computeIfAbsent(label, k -> new Node<>());
		}

		/**
		 * 移除子节点
		 *
		 * @param label 标签
		 */
		public void removeChild(String label) {
			children.remove(label);
		}

		/**
		 * 添加对象
		 *
		 * @param id 对象标识符
		 */
		public void addObject(ID id) {
			objects.add(id);
		}

		/**
		 * 移除对象
		 *
		 * @param id 对象标识符
		 */
		public void removeObject(ID id) {
			objects.remove(id);
		}

		/**
		 * 获取当前节点的对象
		 *
		 * @return 对象集合
		 */
		public Set<ID> getObjects() {
			return new HashSet<>(objects);
        }

		/**
		 * 获取当前节点及所有子节点的对象
		 *
		 * @return 对象集合
		 */
		public Collection<ID> getAllObjects() {
			Set<ID> result = new HashSet<>(objects);
			// 添加所有子节点的对象
			for (Node<ID> child : children.values()) {
				result.addAll(child.getAllObjects());
            }
			return result;
		}

		/**
		 * 检查节点是否为空
		 *
		 * @return 如果节点没有对象且没有子节点则返回true
		 */
		public boolean isEmpty() {
			return objects.isEmpty() && children.isEmpty();
        }
	}
}