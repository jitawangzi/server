package cn.game.games.net.game.module.battle.impl;

import cn.game.protocol.manual.DungeonTypeEnum;

public class XinMoImpl extends DaoHeartImpl {

	@Override
	public int getType() {
		return DungeonTypeEnum.XinMo.getId();
	}

}
