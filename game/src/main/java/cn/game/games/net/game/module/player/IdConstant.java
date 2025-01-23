package cn.game.games.net.game.module.player;

import cn.game.util.UniqueValueEnum;

public enum IdConstant {
	/** 章节礼包 */
	CHAPTER_PACK(1),
	/** 头像框 */
	HEAD_BOX(2),
	/** 问卷调查奖励 */
	Questionnaire(3),
	/** 功能开启奖励 */
	FUNC_OPEN_REWARD(4),
	/** 英雄皮肤 */
	HERO_SKIN(5),
	/** 头像*/
	HEAD_PORTRAIT(6),
	;

	private final int value;

	IdConstant(int value) {
		this.value = value;
		UniqueValueEnum.checkDuplicateValue(this.getClass(), value);
	}

	public int getValue() {
		return value;
	}
}