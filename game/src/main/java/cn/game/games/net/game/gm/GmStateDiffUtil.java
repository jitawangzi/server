package cn.game.games.net.game.gm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * GM状态前后差异对比共享工具类 (DC-05, AC-04)
 * 供 OpsRepairService 与 ProtocolSimulationService 共享状态差异计算逻辑，杜绝重复实现。
 *
 * @author shuyongqiang
 */
public class GmStateDiffUtil {

	private GmStateDiffUtil() {}

	/**
	 * 计算扁平状态差异（保持与 OpsRepairService 完全一致的 {path, before, after} 结构）
	 *
	 * @param before 修改前状态Map
	 * @param after  修改后状态Map
	 * @return 变更列表
	 */
	public static List<Map<String, Object>> computeFlatDiff(Map<String, Object> before, Map<String, Object> after) {
		if (before == null && after == null) {
			return Collections.emptyList();
		}
		if (before == null) {
			before = Collections.emptyMap();
		}
		if (after == null) {
			after = Collections.emptyMap();
		}

		List<Map<String, Object>> diffs = new ArrayList<>();
		for (Map.Entry<String, Object> entry : after.entrySet()) {
			String key = entry.getKey();
			Object afterVal = entry.getValue();
			Object beforeVal = before.get(key);
			if (!Objects.equals(beforeVal, afterVal)) {
				Map<String, Object> diffItem = new LinkedHashMap<>();
				diffItem.put("path", key);
				diffItem.put("before", beforeVal);
				diffItem.put("after", afterVal);
				diffs.add(diffItem);
			}
		}
		// 检查被删除的键
		for (Map.Entry<String, Object> entry : before.entrySet()) {
			String key = entry.getKey();
			if (!after.containsKey(key)) {
				Map<String, Object> diffItem = new LinkedHashMap<>();
				diffItem.put("path", key);
				diffItem.put("before", entry.getValue());
				diffItem.put("after", null);
				diffs.add(diffItem);
			}
		}
		return diffs;
	}

	/**
	 * 计算点分路径的嵌套状态差异（用于协议模拟 withDiff=true 深度字段级观测）
	 *
	 * @param before 修改前状态Map
	 * @param after  修改后状态Map
	 * @return 嵌套点分路径变更列表
	 */
	public static List<Map<String, Object>> computeNestedDiff(Map<String, Object> before, Map<String, Object> after) {
		List<Map<String, Object>> diffs = new ArrayList<>();
		compareNested("", before != null ? before : Collections.emptyMap(), after != null ? after : Collections.emptyMap(), diffs);
		return diffs;
	}

	@SuppressWarnings("unchecked")
	private static void compareNested(String prefix, Map<String, Object> before, Map<String, Object> after, List<Map<String, Object>> diffs) {
		for (Map.Entry<String, Object> entry : after.entrySet()) {
			String key = entry.getKey();
			String path = prefix.isEmpty() ? key : prefix + "." + key;
			Object afterVal = entry.getValue();
			Object beforeVal = before.get(key);

			if (beforeVal instanceof Map && afterVal instanceof Map) {
				compareNested(path, (Map<String, Object>) beforeVal, (Map<String, Object>) afterVal, diffs);
			} else if (!Objects.equals(beforeVal, afterVal)) {
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("path", path);
				item.put("before", beforeVal);
				item.put("after", afterVal);
				diffs.add(item);
			}
		}

		for (Map.Entry<String, Object> entry : before.entrySet()) {
			String key = entry.getKey();
			if (!after.containsKey(key)) {
				String path = prefix.isEmpty() ? key : prefix + "." + key;
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("path", path);
				item.put("before", entry.getValue());
				item.put("after", null);
				diffs.add(item);
			}
		}
	}
}
