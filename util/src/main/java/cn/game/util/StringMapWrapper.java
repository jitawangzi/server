package cn.game.util;

import java.util.HashMap;
import java.util.Map;

public class StringMapWrapper {
	private Map<String, Integer> map = new HashMap<String, Integer>();

	public void setValue(int id, int value, int... ext) {
		map.put(key(id, ext), value);
	}

	private String key(int id, int... ext) {
		if (ext.length == 0) {
			return String.valueOf(id);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		for (int i = 0; i < ext.length; i++) {
			sb.append("_");
			sb.append(ext[i]);
		}
		return sb.toString();
	}

	public int getValue(int id, int... ext) {
		Integer value = map.get(key(id, ext));
		return value == null ? 0 : value;
	}

	public boolean hasValue(int id, int... ext) {
		Integer value = map.get(key(id, ext));
		return value != null;
	}

	public boolean removeValue(int id, int... ext) {
		return map.remove(key(id, ext)) != null;
	}

	public int add(int id, int value, int... ext) {
		return map.compute(key(id, ext), (k, v) -> v == null ? value : v + value);
	}

	public void add(int[] idAndValue, int... ext) {
		add(idAndValue[0], idAndValue[1], ext);
	}

	public int add(String key, int value) {
		return map.compute(key, (k, v) -> v == null ? value : v + value);
	}

	/** 
	 * 添加一批值到当前map
	 * @param map
	 * @return
	 */
	public StringMapWrapper addAll(Map<String, Integer> map) {
		map.forEach((k, v) -> this.add(k, v));
		return this;
	}

	public boolean del(int id, int value, int... ext) {
		String key = key(id, ext);
		Integer cur = map.get(key);
		if (cur == null || cur < value) {
			return false;
		}
		map.put(key, cur - value);
		return true;
	}

	public Integer remove(int id, int... ext) {
		return map.remove(key(id, ext));
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	public Map<String, Integer> getMap() {
		return map;
	}

	public void setMap(Map<String, Integer> map) {
		this.map = map;
	}

	public void clear() {
		this.map.clear();
	}

}
