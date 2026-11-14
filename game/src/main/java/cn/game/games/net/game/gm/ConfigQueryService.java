package cn.game.games.net.game.gm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import cn.game.core.exception.LogicException;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 静态配表只读查询服务，支持MAP_BY_ID、GROUPED_LIST与GLOBAL_CONST三类配表形态
 *
 * @author shuyongqiang
 */
@Service
public class ConfigQueryService {

	private static final Logger log = LoggerFactory.getLogger(ConfigQueryService.class);

	public static final int DEFAULT_PAGE_SIZE = 50;
	public static final int MAX_PAGE_SIZE = 200;

	public enum TableKind {
		MAP_BY_ID,
		GROUPED_LIST,
		GLOBAL_CONST
	}

	private static final ObjectMapper FIELD_MAPPER = createFieldMapper();
	private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {
	};

	private final Supplier<Map<String, ResourceListener>> resourceSupplier;

	/**
	 * 使用WatchServiceManager默认数据源构造配表查询服务
	 */
	public ConfigQueryService() {
		this(() -> WatchServiceManager.getInstance().getResources());
	}

	/**
	 * 指定资源提供器构造配表查询服务（便于单元测试替身注入）
	 *
	 * @param resourceSupplier 配表资源提供器
	 */
	public ConfigQueryService(Supplier<Map<String, ResourceListener>> resourceSupplier) {
		this.resourceSupplier = resourceSupplier;
	}

	/**
	 * 轻量获取已加载配表目录摘要列表（不执行整表行级JSON转换）
	 *
	 * @return 配表元信息列表
	 */
	public List<Map<String, Object>> listTables() {
		Map<String, ResourceListener> resources = safeResources();
		List<Map<String, Object>> result = new ArrayList<>();
		for (Map.Entry<String, ResourceListener> entry : resources.entrySet()) {
			String tableName = entry.getKey();
			ResourceListener listener = entry.getValue();
			if (listener == null) {
				continue;
			}
			TableMetadata meta = inspectMetadata(tableName, listener);
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("table", tableName);
			item.put("kind", meta.kind.name());
			item.put("rowCount", meta.rowCount);
			item.put("fields", new ArrayList<>(meta.fieldNames));
			result.add(item);
		}
		return result;
	}

	/**
	 * 分页与过滤查询指定配表
	 *
	 * @param table 表名
	 * @param filters 字段过滤条件（字符串支持子串模糊匹配，集合支持元素匹配）
	 * @param page 页码（从1开始）
	 * @param pageSize 每页条数（默认50，上限200）
	 * @return 分页结果视图
	 */
	public Map<String, Object> listRows(String table, Map<String, String> filters, Integer page, Integer pageSize) {
		if (table == null || table.isBlank()) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(), "配置表名不能为空");
		}
		int effectivePage = page != null ? page : 1;
		int effectivePageSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;
		if (effectivePage <= 0) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(), "page必须大于0: " + effectivePage);
		}
		if (effectivePageSize <= 0 || effectivePageSize > MAX_PAGE_SIZE) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(),
					"pageSize必须在1到" + MAX_PAGE_SIZE + "之间: " + effectivePageSize);
		}

		ResourceListener listener = resolveTableListener(table);
		TableMetadata meta = inspectMetadata(table, listener);
		Map<String, String> safeFilters = filters != null ? filters : Collections.emptyMap();
		validateFilters(meta.fieldNames, safeFilters);

		List<Map<String, Object>> allRows = materializeRows(table, listener, meta);
		List<Map<String, Object>> matched = new ArrayList<>();
		for (Map<String, Object> row : allRows) {
			if (matchesFilters(row, safeFilters)) {
				matched.add(row);
			}
		}

		int total = matched.size();
		int totalPages = total == 0 ? 0 : (total + effectivePageSize - 1) / effectivePageSize;
		int fromIndex = Math.min((effectivePage - 1) * effectivePageSize, total);
		int toIndex = Math.min(fromIndex + effectivePageSize, total);
		List<Map<String, Object>> pageRows = new ArrayList<>(matched.subList(fromIndex, toIndex));

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("schemaVersion", GmResult.SCHEMA_VERSION);
		payload.put("table", table);
		payload.put("kind", meta.kind.name());
		payload.put("page", effectivePage);
		payload.put("pageSize", effectivePageSize);
		payload.put("total", total);
		payload.put("totalPages", totalPages);
		payload.put("fields", new ArrayList<>(meta.fieldNames));
		payload.put("rows", pageRows);
		return payload;
	}

	/**
	 * 按ID或键O(1)优先查询单条或单组配置（不触发整表预转换）
	 *
	 * @param table 表名
	 * @param id 数值主键（MAP_BY_ID / GROUPED_LIST）
	 * @param key 字符键名（GLOBAL_CONST）
	 * @return 匹配结果
	 */
	public Object getRow(String table, Integer id, String key) {
		if (table == null || table.isBlank()) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(), "配置表名不能为空");
		}
		ResourceListener listener = resolveTableListener(table);
		Class<?> managerClass = listener.getClass();
		Method listMethod = findNoArgMethod(managerClass, "list");
		if (isGlobalConstManager(table, managerClass, listMethod)) {
			String targetKey = key != null && !key.isBlank() ? key.trim() : (id != null ? String.valueOf(id) : null);
			if (targetKey == null || targetKey.isBlank()) {
				throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(), "GLOBAL_CONST查询需要指定key");
			}
			Map<String, Object> constRow = findGlobalConstRow(managerClass, targetKey);
			if (constRow == null) {
				throw new LogicException(ErrorMsgEnum.config_data_not_found.getId(),
						"配置常量不存在: table=" + table + ", key=" + targetKey);
			}
			constRow.put("schemaVersion", GmResult.SCHEMA_VERSION);
			return constRow;
		}

		Integer targetId = id;
		if (targetId == null && key != null && !key.isBlank()) {
			try {
				targetId = Integer.parseInt(key.trim());
			} catch (NumberFormatException e) {
				log.warn("配置ID格式非法: table={}, key={}", table, key, e);
				throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(), "配置ID格式非法: " + key);
			}
		}
		if (targetId == null) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(), "配置查询需要指定id");
		}

		TableMetadata meta = inspectMetadata(table, listener);
		if (meta.kind == TableKind.GROUPED_LIST) {
			GetterInvocationResult grouped = invokeGroupedGetter(listener, table, targetId);
			if (grouped.invoked) {
				if (grouped.value instanceof Collection<?> list && !list.isEmpty()) {
					List<Map<String, Object>> rows = new ArrayList<>(list.size());
					for (Object item : list) {
						rows.add(toRowMap(item));
					}
					Map<String, Object> res = new LinkedHashMap<>();
					res.put("schemaVersion", GmResult.SCHEMA_VERSION);
					res.put("table", table);
					res.put("kind", TableKind.GROUPED_LIST.name());
					res.put("id", targetId);
					res.put("items", rows);
					return res;
				}
				throw new LogicException(ErrorMsgEnum.config_data_not_found.getId(),
						"分组配置数据不存在: table=" + table + ", id=" + targetId);
			}
			List<Map<String, Object>> matchingRows = findMatchingRawRows(meta.rawItems, targetId);
			if (!matchingRows.isEmpty()) {
				Map<String, Object> res = new LinkedHashMap<>();
				res.put("schemaVersion", GmResult.SCHEMA_VERSION);
				res.put("table", table);
				res.put("kind", TableKind.GROUPED_LIST.name());
				res.put("id", targetId);
				res.put("items", matchingRows);
				return res;
			}
			throw new LogicException(ErrorMsgEnum.config_data_not_found.getId(),
					"分组配置数据不存在: table=" + table + ", id=" + targetId);
		}

		GetterInvocationResult single = invokeSingleIdGetter(listener, targetId);
		if (single.invoked) {
			if (single.value != null) {
				Map<String, Object> res = new LinkedHashMap<>();
				res.put("schemaVersion", GmResult.SCHEMA_VERSION);
				res.putAll(toRowMap(single.value));
				return res;
			}
			throw new LogicException(ErrorMsgEnum.config_data_not_found.getId(),
					"配置数据不存在: table=" + table + ", id=" + targetId);
		}
		List<Map<String, Object>> fallbackMatches = findMatchingRawRows(meta.rawItems, targetId);
		if (!fallbackMatches.isEmpty()) {
			Map<String, Object> res = new LinkedHashMap<>();
			res.put("schemaVersion", GmResult.SCHEMA_VERSION);
			res.putAll(fallbackMatches.get(0));
			return res;
		}
		throw new LogicException(ErrorMsgEnum.config_data_not_found.getId(),
				"配置数据不存在: table=" + table + ", id=" + targetId);
	}
	private ResourceListener resolveTableListener(String table) {
		Map<String, ResourceListener> resources = safeResources();
		ResourceListener listener = resources.get(table);
		if (listener == null) {
			for (Map.Entry<String, ResourceListener> entry : resources.entrySet()) {
				if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(table)) {
					listener = entry.getValue();
					break;
				}
			}
		}
		if (listener == null) {
			throw new LogicException(ErrorMsgEnum.config_data_not_found.getId(), "未知配置表: " + table);
		}
		return listener;
	}

	private Map<String, ResourceListener> safeResources() {
		Map<String, ResourceListener> resources = resourceSupplier != null ? resourceSupplier.get() : null;
		return resources != null ? resources : Collections.emptyMap();
	}

	private boolean isGlobalConstManager(String tableName, Class<?> managerClass, Method listMethod) {
		return "GlobalConst".equalsIgnoreCase(tableName)
				|| "GlobalConst".equals(managerClass.getSimpleName())
				|| listMethod == null;
	}

	private TableMetadata inspectMetadata(String tableName, ResourceListener listener) {
		Class<?> managerClass = listener.getClass();
		Method listMethod = findNoArgMethod(managerClass, "list");
		if (isGlobalConstManager(tableName, managerClass, listMethod)) {
			int count = 0;
			for (Field field : managerClass.getFields()) {
				if (isEligibleGlobalConstField(field)) {
					count++;
				}
			}
			Set<String> fields = new LinkedHashSet<>();
			fields.add("key");
			fields.add("type");
			fields.add("value");
			return new TableMetadata(TableKind.GLOBAL_CONST, count, fields, Collections.emptyList());
		}

		Object listResult;
		try {
			listMethod.setAccessible(true);
			listResult = listMethod.invoke(listener);
		} catch (Exception e) {
			log.error("反射读取配置表list失败: table={}", tableName, e);
			throw new LogicException(ErrorMsgEnum.unknown.getId(), "读取配置表失败: " + tableName);
		}

		TableKind kind = detectCollectionKind(listMethod, listResult);
		List<Object> rawItems = new ArrayList<>();
		if (listResult instanceof Collection<?> col) {
			if (kind == TableKind.GROUPED_LIST) {
				for (Object elem : col) {
					if (elem instanceof Collection<?> inner) {
						for (Object item : inner) {
							if (item != null) {
								rawItems.add(item);
							}
						}
					} else if (elem != null) {
						rawItems.add(elem);
					}
				}
			} else {
				for (Object elem : col) {
					if (elem != null) {
						rawItems.add(elem);
					}
				}
			}
		}

		Set<String> fieldNames = new LinkedHashSet<>();
		Class<?> itemClass = inferElementClass(listMethod, rawItems);
		if (itemClass != null) {
			collectInstanceFieldNames(itemClass, fieldNames);
		}
		return new TableMetadata(kind, rawItems.size(), fieldNames, rawItems);
	}

	private List<Map<String, Object>> materializeRows(String tableName, ResourceListener listener, TableMetadata meta) {
		if (meta.kind == TableKind.GLOBAL_CONST) {
			return extractGlobalConstRows(listener.getClass());
		}
		List<Map<String, Object>> rows = new ArrayList<>(meta.rawItems.size());
		for (Object item : meta.rawItems) {
			rows.add(toRowMap(item));
		}
		return rows;
	}

	private TableKind detectCollectionKind(Method listMethod, Object listResult) {
		if (listResult instanceof Collection<?> col) {
			for (Object elem : col) {
				if (elem != null) {
					return (elem instanceof Collection<?>) ? TableKind.GROUPED_LIST : TableKind.MAP_BY_ID;
				}
			}
		}
		Type returnType = listMethod.getGenericReturnType();
		if (returnType instanceof ParameterizedType pt && pt.getActualTypeArguments().length > 0) {
			Type arg0 = pt.getActualTypeArguments()[0];
			if (arg0 instanceof ParameterizedType innerPt && innerPt.getRawType() instanceof Class<?> rawInner
					&& Collection.class.isAssignableFrom(rawInner)) {
				return TableKind.GROUPED_LIST;
			}
		}
		return TableKind.MAP_BY_ID;
	}

	private Class<?> inferElementClass(Method listMethod, List<Object> rawItems) {
		if (!rawItems.isEmpty()) {
			return rawItems.get(0).getClass();
		}
		Type returnType = listMethod.getGenericReturnType();
		if (returnType instanceof ParameterizedType pt && pt.getActualTypeArguments().length > 0) {
			Type arg0 = pt.getActualTypeArguments()[0];
			if (arg0 instanceof Class<?> c) {
				return c;
			}
			if (arg0 instanceof ParameterizedType innerPt && innerPt.getActualTypeArguments().length > 0
					&& innerPt.getActualTypeArguments()[0] instanceof Class<?> innerClass) {
				return innerClass;
			}
		}
		return null;
	}

	private boolean isEligibleGlobalConstField(Field field) {
		if (!Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
			return false;
		}
		String name = field.getName();
		return !"xmlFileName".equalsIgnoreCase(name)
				&& !"instance".equalsIgnoreCase(name)
				&& !"log".equalsIgnoreCase(name);
	}

	private Map<String, Object> findGlobalConstRow(Class<?> constClass, String targetKey) {
		for (Field field : constClass.getFields()) {
			if (!isEligibleGlobalConstField(field)) {
				continue;
			}
			if (targetKey.equalsIgnoreCase(field.getName())) {
				return toGlobalConstRow(field);
			}
		}
		return null;
	}

	private List<Map<String, Object>> extractGlobalConstRows(Class<?> constClass) {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (Field field : constClass.getFields()) {
			if (!isEligibleGlobalConstField(field)) {
				continue;
			}
			Map<String, Object> row = toGlobalConstRow(field);
			if (row != null) {
				rows.add(row);
			}
		}
		return rows;
	}

	private Map<String, Object> toGlobalConstRow(Field field) {
		String name = field.getName();
		try {
			Object rawVal = field.get(null);
			Object normalized = rawVal != null ? FIELD_MAPPER.convertValue(rawVal, Object.class) : null;
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("key", name);
			row.put("type", field.getType().getSimpleName());
			row.put("value", normalized);
			return row;
		} catch (Exception e) {
			log.warn("读取GlobalConst字段失败: field={}", name, e);
			return null;
		}
	}

	private GetterInvocationResult invokeSingleIdGetter(ResourceListener listener, int id) {
		Class<?> clazz = listener.getClass();
		for (String methodName : List.of("getNullable", "get")) {
			Method method = findIntArgMethod(clazz, methodName, false);
			if (method == null) {
				continue;
			}
			try {
				return new GetterInvocationResult(true, method.invoke(listener, id));
			} catch (Exception e) {
				log.warn("单条配置getter调用失败: method={}, id={}", methodName, id, e);
				return new GetterInvocationResult(true, null);
			}
		}
		return new GetterInvocationResult(false, null);
	}

	private GetterInvocationResult invokeGroupedGetter(ResourceListener listener, String tableName, int id) {
		Class<?> clazz = listener.getClass();
		List<String> priorityNames = List.of(
				"get" + tableName + "List",
				"get" + tableName + "IdList",
				"getById",
				"getList");
		for (String candidate : priorityNames) {
			Method method = findIntArgMethodCaseInsensitive(clazz, candidate, true);
			if (method != null) {
				try {
					return new GetterInvocationResult(true, method.invoke(listener, id));
				} catch (Exception e) {
					log.warn("分组配置getter调用失败: method={}, id={}", method.getName(), id, e);
					return new GetterInvocationResult(true, null);
				}
			}
		}
		List<Method> fallbackMethods = new ArrayList<>();
		for (Method m : clazz.getMethods()) {
			if (m.getParameterCount() == 1
					&& (m.getParameterTypes()[0] == int.class || m.getParameterTypes()[0] == Integer.class)
					&& Collection.class.isAssignableFrom(m.getReturnType())
					&& m.getName().startsWith("get")) {
				fallbackMethods.add(m);
			}
		}
		fallbackMethods.sort(Comparator.comparing(Method::getName));
		for (Method m : fallbackMethods) {
			try {
				return new GetterInvocationResult(true, m.invoke(listener, id));
			} catch (Exception e) {
				log.warn("分组配置getter调用失败: method={}, id={}", m.getName(), id, e);
				return new GetterInvocationResult(true, null);
			}
		}
		return new GetterInvocationResult(false, null);
	}

	private List<Map<String, Object>> findMatchingRawRows(List<Object> rawItems, int targetId) {
		List<Map<String, Object>> matches = new ArrayList<>();
		for (Object item : rawItems) {
			if (matchesRawId(item, targetId)) {
				matches.add(toRowMap(item));
			}
		}
		return matches;
	}

	private boolean matchesRawId(Object item, int targetId) {
		if (item == null) {
			return false;
		}
		for (String fieldName : List.of("ID", "id", "groupId", "GroupId")) {
			Field field = findField(item.getClass(), fieldName);
			if (field == null) {
				continue;
			}
			try {
				field.setAccessible(true);
				Object val = field.get(item);
				if (val instanceof Number num && num.intValue() == targetId) {
					return true;
				}
				if (val != null && String.valueOf(targetId).equals(String.valueOf(val))) {
					return true;
				}
			} catch (Exception e) {
				log.warn("反射读取配置ID字段失败: class={}, field={}", item.getClass().getSimpleName(), fieldName, e);
			}
		}
		return false;
	}

	private Field findField(Class<?> clazz, String name) {
		Class<?> cursor = clazz;
		while (cursor != null && cursor != Object.class) {
			for (Field f : cursor.getDeclaredFields()) {
				if (!Modifier.isStatic(f.getModifiers()) && f.getName().equals(name)) {
					return f;
				}
			}
			cursor = cursor.getSuperclass();
		}
		return null;
	}

	private void validateFilters(Set<String> validFields, Map<String, String> filters) {
		for (String filterField : filters.keySet()) {
			if (filterField == null || filterField.isBlank()) {
				throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(), "过滤字段名不能为空");
			}
			if (!validFields.contains(filterField)) {
				throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(), "未知配置过滤字段: " + filterField);
			}
		}
	}

	private boolean matchesFilters(Map<String, Object> row, Map<String, String> filters) {
		for (Map.Entry<String, String> entry : filters.entrySet()) {
			Object actual = row.get(entry.getKey());
			String expected = entry.getValue();
			if (expected == null) {
				if (actual != null) {
					return false;
				}
				continue;
			}
			if (!matchesFilterValue(actual, expected)) {
				return false;
			}
		}
		return true;
	}

	private boolean matchesFilterValue(Object actual, String expected) {
		if (actual == null) {
			return false;
		}
		String expectedLower = expected.trim().toLowerCase(Locale.ROOT);
		if (actual instanceof String strVal) {
			String actualLower = strVal.toLowerCase(Locale.ROOT);
			return actualLower.equals(expectedLower) || actualLower.contains(expectedLower);
		}
		if (actual instanceof Collection<?> col) {
			for (Object elem : col) {
				if (matchesFilterValue(elem, expected)) {
					return true;
				}
			}
			return false;
		}
		if (actual instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (matchesFilterValue(entry.getKey(), expected) || matchesFilterValue(entry.getValue(), expected)) {
					return true;
				}
			}
			return false;
		}
		return expectedLower.equals(String.valueOf(actual).trim().toLowerCase(Locale.ROOT));
	}

	private Map<String, Object> toRowMap(Object item) {
		return FIELD_MAPPER.convertValue(item, MAP_TYPE_REF);
	}

	private void collectInstanceFieldNames(Class<?> clazz, Set<String> names) {
		Class<?> cursor = clazz;
		while (cursor != null && cursor != Object.class) {
			for (Field f : cursor.getDeclaredFields()) {
				if (!Modifier.isStatic(f.getModifiers()) && !f.isSynthetic()) {
					names.add(f.getName());
				}
			}
			cursor = cursor.getSuperclass();
		}
	}

	private Method findNoArgMethod(Class<?> clazz, String name) {
		for (Method m : clazz.getMethods()) {
			if (m.getParameterCount() == 0 && m.getName().equals(name)) {
				return m;
			}
		}
		return null;
	}

	private Method findIntArgMethod(Class<?> clazz, String name, boolean requireCollectionReturn) {
		for (Method m : clazz.getMethods()) {
			if (m.getParameterCount() == 1
					&& (m.getParameterTypes()[0] == int.class || m.getParameterTypes()[0] == Integer.class)
					&& m.getName().equals(name)
					&& (!requireCollectionReturn || Collection.class.isAssignableFrom(m.getReturnType()))) {
				return m;
			}
		}
		return null;
	}

	private Method findIntArgMethodCaseInsensitive(Class<?> clazz, String name, boolean requireCollectionReturn) {
		for (Method m : clazz.getMethods()) {
			if (m.getParameterCount() == 1
					&& (m.getParameterTypes()[0] == int.class || m.getParameterTypes()[0] == Integer.class)
					&& m.getName().equalsIgnoreCase(name)
					&& (!requireCollectionReturn || Collection.class.isAssignableFrom(m.getReturnType()))) {
				return m;
			}
		}
		return null;
	}

	private static ObjectMapper createFieldMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		return mapper;
	}

	private static final class TableMetadata {
		final TableKind kind;
		final int rowCount;
		final Set<String> fieldNames;
		final List<Object> rawItems;

		TableMetadata(TableKind kind, int rowCount, Set<String> fieldNames, List<Object> rawItems) {
			this.kind = kind;
			this.rowCount = rowCount;
			this.fieldNames = fieldNames;
			this.rawItems = rawItems;
		}
	}

	private static final class GetterInvocationResult {
		final boolean invoked;
		final Object value;

		GetterInvocationResult(boolean invoked, Object value) {
			this.invoked = invoked;
			this.value = value;
		}
	}
}