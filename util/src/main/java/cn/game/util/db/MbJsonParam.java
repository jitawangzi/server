package cn.game.util.db;

import java.util.HashMap;
import java.util.Map;

/**
 * mybatis json 参数封装
 * 2021年4月2日 下午3:55:28
 * @author SYQ
 */
@Deprecated
public class MbJsonParam {

	public static Map<String, String> add(String jsonKey, String jsonValue, String column, Object pk) {

		Map<String, String> map = add(jsonKey, jsonValue, pk);
		map.put("column", column);
		return map;
	}
	public static Map<String, String> add(String jsonKey, String jsonValue, Object pk) {

		Map<String, String> map = new HashMap<String, String>();
		map.put("key", "$." + jsonKey);
		map.put("value", jsonValue);
		map.put("pk", pk + "");
		return map;
	}
	public static Map<String, String> add(String jsonValue, Object pk) {

		Map<String, String> map = new HashMap<String, String>();
		map.put("value", jsonValue);
		map.put("pk", pk + "");
		return map;
	}

	/**
	 * @Description
	 * @param column
	 *            更改哪个列
	 * @param index
	 *            想要删除的array index
	 * @param pk
	 *            行数据的主键
	 * @return
	 */
	public static Map<String, String> delFromArray(String column, int index, Object pk) {

		Map<String, String> map = new HashMap<String, String>();
		map.put("pk", pk.toString());
		map.put("index", index + "");
		map.put("column", column);
		return map;
	}
}
