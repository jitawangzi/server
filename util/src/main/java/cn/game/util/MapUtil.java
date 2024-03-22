package cn.game.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapUtil {

    public static final float LOAD_FACTOR = 0.75f;

    public static <K, V> HashMap<K, V> newHashMap(int size) {
        return new HashMap<>((int) (size / LOAD_FACTOR) + 1);
    }
    
    /**
	 * 向一个map里添加物品和数量
	 * @param items
	 * @param itemId
	 * @param count
	 */
	public static void addItemCounts(Map<Integer, Integer> items, int itemId, int count) {
		if(items.containsKey(itemId))
			 count += items.get(itemId);
		
		items.put(itemId, count);
	}
	
	/**
	 * 向一个map里添加物品和数量
	 * @param items
	 * @param addItems
	 */
	public static void addItemCounts(Map<Integer, Integer> items, Map<Integer, Integer> addItems) {
		for (Map.Entry<Integer, Integer> entry : addItems.entrySet()) {
			addItemCounts(items, entry.getKey(), entry.getValue());
		}
	}
	
	public static int getValue(Map<Integer, Integer> map, int key) {
		Integer integer = map.get(key);
		return integer == null ? 0 : integer;
	}
	
	public static Map<Integer, Set<Integer>> merge(Map<Integer, Set<Integer>> map, int key, Set<Integer> set) {
		Set<Integer> mySet = map.get(key);
		if (mySet != null) {
			mySet.addAll(set);
			
		} else {
			map.put(key, set);
		}
		return map;
	}
	
	/**
	 * 合并Map<Integer, Map<Integer, Integer>>里的小value
	 * @param bigMap
	 * @return 所有小value之和
	 */
	public static int mergeLittleValue(Map<Integer, Map<Integer, Integer>> bigMap) {
		int value = 0;
		for (int bigKey : bigMap.keySet()) {
			Map<Integer, Integer> littleMap = bigMap.get(bigKey);
			for (int littleKey : littleMap.keySet()) {
				value += littleMap.get(littleKey);
			}
		}
		return value;
	}

	/**
	 * 所有value的和
	 * @param map
	 * @return
	 */
	public static int valuesSum(Map<Integer, Integer> map) {
		int res = 0;
		for (int key : map.keySet()) {
			res += map.get(key);
		}
		return res;
	}

	/** 
	 * 给定字符串值，生成一个map，给代码生成使用。 
	 * @param value
	 * @param type
	 * @return
	 */
	public static Map newHashMap(String value, String type) {
		if (value == null || value.trim().isEmpty()) {
			return newHashMap(0);
		}
		Map map = new HashMap();

		// Map<Integer,Long>
		type = type.trim();
		int index = type.indexOf("<");
		type = type.substring(index + 1, type.length() - 1);
		String keyType = type.split(",")[0];
		String valueType = type.split(",")[1];
		try {
			Class<?> keyClass = Class.forName("java.lang." + keyType);
			Class<?> valueClass = Class.forName("java.lang." + valueType);
			Method keyMethod = keyClass.getMethod("valueOf", String.class);
			Method valueMethod = valueClass.getMethod("valueOf", String.class);

			String[] rows = value.split("\\|");
			for (String row : rows) {
				String[] split = row.split(";");
				map.put(keyMethod.invoke(null, split[0]), valueMethod.invoke(null, split[1]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

}
