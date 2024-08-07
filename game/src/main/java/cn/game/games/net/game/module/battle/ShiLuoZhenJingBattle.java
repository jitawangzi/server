package cn.game.games.net.game.module.battle;

import java.util.List;

import cn.game.games.core.ResultObject;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class ShiLuoZhenJingBattle extends XiYouBattleHandler {

	@Override
	public int battleStart(int id) {

		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request) {
		return null;
	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id) {
		return null;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.ShiLuoZhenJing.getId();
	}

	@Override
	void newDay() {
//		reset();
	}


}
