package cn.game.games.net.game.module.player;

import java.util.HashSet;
import java.util.Set;

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
	HERO_SKIN(5);

	private final int value;
	private static final Set<Integer> VALUES = new HashSet<>();

	IdConstant(int value) {
		this.value = value;
	}

	static {
		// 检查重复值
		for (IdConstant constant : values()) {
			if (!VALUES.add(constant.value)) {
				throw new IllegalStateException("Duplicate value: " + constant.value);
			}
		}
	}

	public int getValue() {
		return value;
	}
}