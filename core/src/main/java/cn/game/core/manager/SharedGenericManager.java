package cn.game.core.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 分片模式实现（按层级标签管理对象）
 */
@Component
@Profile("shared")
public class SharedGenericManager<ID, T> implements GenericManager<ID, T> {

	// 嵌套存储结构示例：Map<region, Map<server, Map<id, T>>>
	private final Map<String, Object> storage = new ConcurrentHashMap<>();

	// 预设的标签层级（如["region", "server"]）
	private final List<String> labelKeys;

	// 反向索引：ID到完整标签路径的映射
	private final Map<ID, List<String>> idToLabels = new ConcurrentHashMap<>();

	/**
	 * 构造方法（通过Spring注入配置）
	 * @param labels 预设的标签层级配置
	 */
	public SharedGenericManager(@Value("${manager.labels}") List<String> labels) {
		if (labels == null || labels.isEmpty()) {
			throw new IllegalArgumentException("必须配置至少一个标签层级");
		}
		this.labelKeys = List.copyOf(labels);
	}

	@Override
	public void initialize() {
		// 示例：从分布式存储加载数据
		// loadFromClusterStorage();
	}

	@Override
	public void addWithLabels(ID id, T obj, String... labelValues) {
		validateLabelValues(labelValues);

		// 构建存储路径
		Map<String, Object> current = storage;
		for (int i = 0; i < labelKeys.size() - 1; i++) {
			current = getOrCreateNextLevel(current, labelValues[i]);
		}

		// 最后一层存储对象
		String finalLevelValue = labelValues[labelKeys.size() - 1];
		Map<ID, T> objectMap = getOrCreateObjectMap(current, finalLevelValue);
		objectMap.put(id, obj);

		// 更新反向索引
		idToLabels.put(id, List.of(labelValues));
	}

	@Override
	public Collection<T> getByExactLabels(String... labelValues) {
		validateLabelValues(labelValues);

		Map<String, Object> current = storage;
		for (int i = 0; i < labelKeys.size() - 1; i++) {
			current = (Map<String, Object>) current.get(labelValues[i]);
			if (current == null)
				return Collections.emptyList();
		}

		Map<ID, T> objectMap = (Map<ID, T>) current.get(labelValues[labelKeys.size() - 1]);
		return objectMap != null ? objectMap.values() : Collections.emptyList();
	}

	@Override
	public Collection<T> getByPartialLabels(String... labelValues) {
		validatePartialQuery(labelValues);

		List<T> results = new ArrayList<>();
		int levels = labelValues == null ? 0 : labelValues.length;
		partialTraverse(storage, 0, levels, labelValues, results);
		return results;
	}

	@Override
	public T get(ID id) {
		List<String> labels = idToLabels.get(id);
		if (labels == null)
			return null;

		Map<String, Object> current = storage;
		for (String label : labels) {
			current = (Map<String, Object>) current.get(label);
			if (current == null)
				return null;
		}
		return (T) current.get(id);
	}

	@Override
	public Collection<T> getAll() {
		List<T> results = new ArrayList<>();
		collectAllObjects(storage, results);
		return results;
	}

	@Override
	public boolean remove(ID id) {
		List<String> labels = idToLabels.get(id);
		if (labels == null)
			return false;

		Map<String, Object> current = storage;
		for (int i = 0; i < labels.size() - 1; i++) {
			current = (Map<String, Object>) current.get(labels.get(i));
			if (current == null)
				return false;
		}

		Map<ID, T> objectMap = (Map<ID, T>) current.get(labels.get(labels.size() - 1));
		boolean removed = objectMap != null && objectMap.remove(id) != null;
		if (removed) {
			idToLabels.remove(id);
		}
		return removed;
	}

	@Override
	public void clear() {
		storage.clear();
		idToLabels.clear();
	}

	// region 私有工具方法

	/**
	 * 验证标签值数量是否匹配
	 */
	private void validateLabelValues(String[] labelValues) {
		if (labelValues.length != labelKeys.size()) {
			throw new IllegalArgumentException(String.format("需要 %d 个标签值，实际收到 %d", labelKeys.size(), labelValues.length));
		}
	}

	/**
	 * 验证部分查询参数合法性
	 */
	private void validatePartialQuery(String[] labelValues) {
		if (labelValues != null && labelValues.length > labelKeys.size()) {
			throw new IllegalArgumentException("无效的查询: " + labelValues);
		}
	}

	/**
	 * 获取或创建下一层级存储
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getOrCreateNextLevel(Map<String, Object> current, String labelValue) {
		return (Map<String, Object>) current.computeIfAbsent(labelValue, k -> new ConcurrentHashMap<>());
	}

	/**
	 * 获取或创建对象存储Map
	 */
	@SuppressWarnings("unchecked")
	private Map<ID, T> getOrCreateObjectMap(Map<String, Object> current, String labelValue) {
		return (Map<ID, T>) current.computeIfAbsent(labelValue, k -> new ConcurrentHashMap<>());
	}

	/**
	 * 部分层级遍历收集对象
	 */
	private void partialTraverse(Map<String, Object> current, int currentDepth, int targetDepth, String[] filterValues, List<T> results) {
		if (currentDepth == targetDepth) {
			collectAllObjects(current, results);
			return;
		}

		String expectedValue = filterValues[currentDepth];
		Map<String, Object> nextLevel = (Map<String, Object>) current.get(expectedValue);
		if (nextLevel != null) {
			partialTraverse(nextLevel, currentDepth + 1, targetDepth, filterValues, results);
		}
	}

	/**
	 * 递归收集所有对象
	 */
	@SuppressWarnings("unchecked")
	private void collectAllObjects(Map<String, Object> node, List<T> results) {
		for (Object value : node.values()) {
			if (value instanceof Map) {
				if (((Map<?, ?>) value).isEmpty())
					continue;

				if (((Map<?, ?>) value).values().iterator().next() instanceof Map) {
					// 中间层级，继续递归
					collectAllObjects((Map<String, Object>) value, results);
				} else {
					// 对象存储层
					results.addAll(((Map<ID, T>) value).values());
				}
			}
		}
	}
	// endregion
}