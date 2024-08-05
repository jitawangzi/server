package cn.game.util;

import java.util.HashMap;
import java.util.Map;

/**    
 * 对Map的一个简单封装，方便计算保存kv数值，key int  value long
 * 2024年3月19日 下午6:59:46
 * @author SYQ
 */
public class MapWrapper {
	private Map<Integer, Long> map = new HashMap<Integer, Long>();

	public void setValue(int id, long value) {
		map.put(id, value);
	}

	public long getValue(int id) {
		Long value = map.get(id);
		return value == null ? 0 : value;
	}

	public boolean hasValue(int id) {
		Long value = map.get(id);
		return value != null;
	}

	public boolean removeValue(int id) {
		return map.remove(id) != null;
	}

	public long add(int id, int value) {
		if (value == 0) {
			return 0;
		}
		return map.compute(id, (k, v) -> v == null ? value : v + value);
	}

	public long add(int id) {
		return add(id, 1);
	}

	public boolean del(int id, int value) {
		Long cur = map.get(id);
		if (cur == null || cur < value) {
			return false;
		}
		map.put(id, cur - value);
		return true;
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	public Map<Integer, Long> getMap() {
		return map;
	}

	public void clear() {
		this.map.clear();
	}

}
