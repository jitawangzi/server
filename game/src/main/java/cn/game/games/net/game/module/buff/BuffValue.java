package cn.game.games.net.game.module.buff;

import java.util.Map;

/**    
 * BuffEffect影响的数值，通常在buff中检查效果类型使用
 * @date 2022年8月29日 上午10:47:35
 * @author SYQ
 */
public class BuffValue {

	private Map<Integer, Map<Integer, Integer>> effects;

	/** 通过数值改变 */
	public static final byte CHANGE_BY_VALUE = 0;
	/** 通过面板的百分比改变 */
	public static final byte CHANGE_BY_PANEL_PERCENT = 1;
	
	/** 通过当前值的百分比改变 */
	public static final byte CHANGE_BY_CUR_PERCENT = 2;
	
	/** 通过身体值的百分比改变 */
	public static final byte CHANGE_BY_BODY_PERCENT = 4;
	
	/** 通过上限的百分比改变 */
	public static final byte CHANGE_BY_MAX_PERCENT = 5;
	
	public BuffValue(Map<Integer, Map<Integer, Integer>> effects) {

		this.effects = effects;
	}

	/** 
	 * 是否有某种效果配置了。
	 * @param type
	 * @param id
	 * @return
	 */
	public boolean hasValue(int type, int id) {
		Map<Integer, Integer> map = effects.get(type);
		if (map == null) {
			return false;
		}
		Integer integer = map.get(id);
		return integer != null;
	}
	
	/** 
	 * 是否有某种效果配置了。
	 * @param id idParam
	 * @return
	 */
	public boolean hasValueDefault(int id) {
		Map<Integer, Integer> map = effects.get((int) CHANGE_BY_VALUE);
		if (map == null) {
			return false;
		}
		Integer integer = map.get(id);
		return integer != null;
	}

	/** 
	 * 获取改变的值,负数减少，正数增加
	 * @param type
	 * @param id 如果一条效果可能会影响多种类型的数值，用id来区分
	 * 比如 改变角色属性(当前) 这样的效果，这个id就是指定哪种类型的属性 
	 * @return
	 */
	public int getValue(int type, int id) {
		Map<Integer, Integer> map = effects.get(type);
		if (map == null) {
			return 0;
		}
		Integer integer = map.get(id);
		return integer == null ? 0 : integer;

	}
	/** 
	 * 获取改变的值,负数减少，正数增加
	 * @param type
	 * @return
	 */
	public int getValue(int type) {
		return getValue(type, 0);
	}
	/** 
	 * 获取按值增减的数值,区分细分的子类型
	 * @param id
	 * @return
	 */
	public int getValueDefault(int id) {
		return getValue(CHANGE_BY_VALUE, id);

	}
	/** 
	 * 获取按值增减的数值
	 * @return
	 */
	public int getValueDefault() {
		return getValue(CHANGE_BY_VALUE, 0);
	}

}
