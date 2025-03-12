package cn.game.games.net.game.module.player;

import cn.game.util.UniqueValueEnum;

public enum VarConstant {
	/** 玩家改名次数 */
	RANAME_COUNT(1),

	/** 玩家改性别次数 */
	GENDER_COUNT(2),

	/** 城池等级 */
	WALL_LEVEL(3),

	/** 战斗速度广告计数 */
	BATTLE_SPEED_ADS_COUNT(10),

	// 微信设置相关
	/** 是否开启遨游奖励满了微信推送 */
	WECHAT_NOTIFY_AOYOU_REWARD(21),

	/** 是否开启首充次日领取微信推送 */
	WECHAT_NOTIFY_FIRST_RECHARGE_REWARD(22),

	/** 是否开启月签到微信推送 */
	WECHAT_NOTIFY_MONTH_SIGN_REWARD(23),

	/** 是否开启体力满了微信推送 */
	WECHAT_NOTIFY_ENERGY(24);

	private final int value;

	static {
		UniqueValueEnum.checkDuplicateValues(VarConstant.class, VarConstant::getValue);
	}
	VarConstant(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}