package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * HCBattle表的战斗
 * 2024年5月29日 上午10:32:23
 * @author SYQ
 */
public abstract class HCBattleHandler extends IBattleHandler {
	@Override
	public int check(int id, int subId,long ...args) {

		HCBattleConfig battleConfig = HCBattleManager.instance().get(id);
		BattleModule battleModule = player.getBattleModule();
		if (battleConfig.preBattle > 0 && !battleModule.isHCBattlePass(battleConfig.preBattle)) {
			return ErrorMsgEnum.BattleLevel_pre.getId();
		}
//		if (!battleModule.checkChapterTimes(dungeonId)) {
//			return ErrorMsgEnum.times_limit.getId();
//		}

		PlayerHelper.delResources(player, battleConfig.cost, OpType.BattleStart);
		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
		throw new UnsupportedOperationException("not support quickEnd, id: " + id + ", subId: " + subId + ", isWin: " + isWin);
	}

	@Override
	public List<RewardInfo> battleEndReward(BattleFieldEndRequest_13000003 request) {
		List<RewardInfo> rewards = new ArrayList<>();

		return rewards;

	}
	
	/** 
	 * 玩法的特殊规则校验
	 * @param id
	 * @param subId
	 * @return
	 */
	@Override
	public int checkCustom(int id, int subId,long ... args) {
		return 0 ; 
	}
	
	@Override
	public int battleStart(int id,int subId) {
		return battleStart(id); 
	}

	@Override
	public void onLogin() {
	}
	@Override
	public void reLogin() {
		
	}
}
