package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * Battle表的战斗
 * 2024年5月29日 上午10:32:10
 * @author SYQ
 */
public abstract class XiYouBattleHandler extends IBattleHandler {

	@Override
	public int check(int id, int subId) {
		BattleModule battleModule = player.getModule(BattleModule.class);
		BattleConfig battleConfig = BattleManager.instance().get(id);
//		BattleFieldConfig levelConfig = BattleFieldManager.instance().get(id);
		/*		if (battleConfig.BattleFieldID != id) {
					client.sendProtocol(resp, ErrorMsgEnum.request_parameter_error.getId());
					return;
				}*/

		/*		int[] openDay = battleConfig.openDay;
				if (openDay.length > 0) {
					int dayOfWeek = DateUtil.getDayOfWeek();
					if (!GameUtil.contains(openDay, dayOfWeek)) {
						client.sendProtocol(resp, ErrorMsgEnum.not_open.getId());
						return;
					}
				}*/
		if (battleConfig.preBattle > 0 && !battleModule.isBattlePass(battleConfig.preBattle)) {
			return ErrorMsgEnum.BattleLevel_pre.getId();
		}
//		if (!battleModule.checkChapterTimes(dungeonId)) {
//			return ErrorMsgEnum.times_limit.getId();
//		}
		if (battleConfig.BattleCondition > 0 && player.getDevelopModule().getHeavenlyDaoLevel() < battleConfig.BattleCondition) {
			return ErrorMsgEnum.level_not_enough.getId();
		}

		if (!PlayerHelper.checkCondition(player, battleConfig.PlayerCondition)) {
			return ErrorMsgEnum.condition_check_error.getId();
		}
		if (!PlayerHelper.isEnough(player, battleConfig.cost)) {
			return ErrorMsgEnum.resource_not_enough.getId();
		}
		PlayerHelper.delResources(player, battleConfig.cost, OpType.BattleStart);
		
		return checkCustom(id, subId);
	}
	
	/** 
	 * 玩法的特殊规则校验
	 * @param id
	 * @param subId
	 * @return
	 */
	@Override
	public int checkCustom(int id, int subId) {
		return 0 ; 
	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
		throw new UnsupportedOperationException("not support quickEnd, id: " + id + ", subId: " + subId + ", isWin: " + isWin);
	}

	@Override
	public void onLogin() {

	}
	
	@Override
	int battleStart(int id) {
		throw new UnsupportedOperationException("not implement battle, id: " + id);
	}
	
	@Override
	public int battleStart(int id,int subId) {
		return battleStart(id); 
	}

	@Override
	public List<RewardInfo> battleEndReward(BattleFieldEndRequest_13000003 request) {
		List<RewardInfo> rewards = new ArrayList<>();

		return rewards;

	}

}
