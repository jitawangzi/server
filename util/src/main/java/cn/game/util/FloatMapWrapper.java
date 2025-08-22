package cn.game.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 对 Map 的一个简单封装，方便计算保存 kv 数值，key int  value float
 * 2025年8月22日
 * @author SYQ
 */
public class FloatMapWrapper {
	private Map<Integer, Float> map = new HashMap<>();

	public void setValue(int id, float value) {
		map.put(id, value);
	}

	public float getValue(int id) {
		return map.getOrDefault(id, 0f);
	}

	public boolean hasValue(int id) {
		return map.containsKey(id);
	}

	public boolean removeValue(int id) {
		return map.remove(id) != null;
	}

	public Float remove(int id) {
		return map.remove(id);
	}

	public float add(int id, float value) {
		if (value == 0f) {
			return map.getOrDefault(id, 0f);
		}
		return map.merge(id, value, Float::sum);
	}

	public float add(int id) {
		return add(id, 1f);
	}

	public void add(int id, int valueAsInt) {
		add(id, (float) valueAsInt);
	}

	public void add(int[] idAndValue) {
		if (idAndValue == null || idAndValue.length < 2)
			return;
		add(idAndValue[0], (float) idAndValue[1]);
	}

	public void add(int id, double valueAsDouble) {
		add(id, (float) valueAsDouble);
	}

	public void add(int id, long valueAsLong) {
		add(id, (float) valueAsLong);
	}

	/**
	 * 添加一批值到当前 map
	 */
	public FloatMapWrapper addAll(Map<Integer, Float> map) {
		if (map == null || map.isEmpty())
			return this;
		map.forEach(this::add);
		return this;
	}

	public FloatMapWrapper addAllInt(Map<Integer, Integer> map) {
		if (map == null || map.isEmpty())
			return this;
		map.forEach(this::add);
		return this;
	}

	/**
	 * 按行添加二维数组：每一行形如 [id, value]
	 * 示例：new float[][] { {1, 2.5f}, {2, -1f} }
	 */
	public FloatMapWrapper addAll(float[][] attr) {
		if (attr == null)
			return this;
		for (int i = 0; i < attr.length; i++) {
			float[] row = attr[i];
			if (row == null || row.length < 2)
				continue;
			int id = (int) row[0];
			float value = row[1];
			this.add(id, value);
		}
		return this;
	}

	public boolean del(int id, float value) {
		return del(id, value, false);
	}

	/**
	 * 获取所有值的和
	 */
	public float sum() {
		float s = 0f;
		for (Float v : map.values()) {
			if (v != null)
				s += v;
		}
		return s;
	}

	/**
	 * @param id
	 * @param value
	 * @param allowNegative 减少的时候，是否允许负值
	 */
	public boolean del(int id, float value, boolean allowNegative) {
		Float cur = map.get(id);
		if (cur == null) {
			return false;
		}
		if (!allowNegative && cur < value) {
			return false;
		}
		float next = cur - value;
		map.put(id, next);
		return true;
	}

	public int size() {
		return map.size();
	}

	/**
	 * 减少所有值，如果减少到小于0，则设置为0
	 */
	public void reduceAllValues(float value) {
		if (value <= 0f)
			return;
		map.replaceAll((k, v) -> {
			float nv = (v == null ? 0f : v) - value;
			return nv < 0f ? 0f : nv;
		});
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	public Map<Integer, Float> getMap() {
		return map;
	}

	public void clear() {
		this.map.clear();
	}

	public void reset() {
		this.map.clear();
	}
}