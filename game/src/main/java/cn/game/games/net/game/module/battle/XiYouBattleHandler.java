package cn.game.games.net.game.module.battle;

import java.util.List;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * Battle表的战斗
 * 2024年5月29日 上午10:32:10
 * @author SYQ
 */
public abstract class XiYouBattleHandler extends IBattleHandler {

	@Override
	public int check(int id) {
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
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
		if (battleConfig.preBattle > 0 && !chapterModule.isBattlePass(battleConfig.preBattle)) {
			return ErrorMsgEnum.BattleLevel_pre.getId();
		}
//		if (!chapterModule.checkChapterTimes(dungeonId)) {
//			return ErrorMsgEnum.times_limit.getId();
//		}
		if (battleConfig.BattleCondition > 0 && player.getDevelopModule().getHeavenlyDaoLevel() < battleConfig.BattleCondition) {
			return ErrorMsgEnum.player_level_not_enough.getId();
		}

//		if (!PlayerHelper.checkCondition(playerId, battleConfig.enterCondtion)) {
//			client.sendProtocol(resp, ErrorMsgEnum.condition_check_error.getId());
//			return;
//		}

		if (!PlayerHelper.delResources(player, battleConfig.cost, OpType.BattleStart)) {
			return ErrorMsgEnum.resource_not_enough.getId();
		}
		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id) {
		return null;
	}


}
