package cn.game.util;

import java.util.HashMap;
import java.util.Map;

/**    
 * 对Map的一个简单封装，方便计算保存kv数值，key int  value int
 * 2024年3月19日 下午6:59:46
 * @author SYQ
 */
public class IntMapWrapper {
	private Map<Integer, Integer> map = new HashMap<Integer, Integer>();

	public void setValue(int id, int value) {
		map.put(id, value);
	}
	public int getValue(int id) {
		Integer value = map.get(id);
		return value == null ? 0 : value;
	}

	public boolean hasValue(int id) {
		Integer value = map.get(id);
		return value != null;
	}

	public boolean removeValue(int id) {
		return map.remove(id) != null;
	}

	public int add(int id, int value) {
		if (value == 0) {
			return 0;
		}
		return map.compute(id, (k, v) -> v == null ? value : v + value);
	}

	public int add(int id) {
		return add(id, 1);
	}

	public void add(int[] idAndValue) {
		add(idAndValue[0], idAndValue[1]);
	}

	/** 
	 * 添加一批值到当前map
	 * @param map
	 * @return
	 */
	public IntMapWrapper addAll(Map<Integer, Integer> map) {
		map.forEach((k, v) -> this.add(k, v));
		return this;
	}

	public IntMapWrapper addAll(int[][] attr) {
		for (int i = 0; i < attr.length; i++) {
			for (int j = 0; j < attr[i].length; j++) {
				this.add(attr[i][0], attr[i][0]);
			}
		}
		return this;
	}

	public boolean del(int id, int value) {
		Integer cur = map.get(id);
		if (cur == null || cur < value) {
			return false;
		}
		map.put(id, cur - value);
		return true;
	}

	public Integer remove(int id) {
		return map.remove(id);
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	public Map<Integer, Integer> getMap() {
		return map;
	}

	public void clear() {
		this.map.clear();
	}

}
