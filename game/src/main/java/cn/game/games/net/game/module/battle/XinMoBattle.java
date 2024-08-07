package cn.game.games.net.game.module.battle;

import cn.game.protocol.manual.DungeonTypeEnum;

public class XinMoBattle extends DaoHeartBattle {
	@Override
	public int getType() {
		return DungeonTypeEnum.XinMo.getId();
	}
}
