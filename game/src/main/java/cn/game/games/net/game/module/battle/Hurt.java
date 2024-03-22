package cn.game.games.net.game.module.battle;

import cn.game.games.net.game.manager.GameConstants;

/**
 * 角色受到的属性伤害
 *
 */
public class Hurt {
	
	private int value0;// 0-绝对值
	
	private int value1;// 1-上限百分比
	
	private int value2;// 2-当前百分比
	
	public int getDefaultValue() {
		return value0;
	}

	/**
	 * 获取损失的值
	 * @param type {@LINK GameConstants.ABSOLUTE_VALUE}
	 * @return
	 */
	public int getValue(int type) {
		switch (type) {
			case GameConstants.ABSOLUTE_VALUE:
				return value0;
			case GameConstants.TOPLIMIT_PERCENT:
				return value1;
			case GameConstants.CUR_PERCENT:
				return value2;
			default:
				throw new IllegalArgumentException("错误的数值类型 : " + type);
		}
	}

	public int getValue0() {
		return value0;
	}
	public void setValue0(int value0) {
		this.value0 = value0;
	}
	public int getValue1() {
		return value1;
	}
	public void setValue1(int value1) {
		this.value1 = value1;
	}
	public int getValue2() {
		return value2;
	}
	public void setValue2(int value2) {
		this.value2 = value2;
	}
	
}
