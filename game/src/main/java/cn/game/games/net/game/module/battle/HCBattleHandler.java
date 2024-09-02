package cn.game.games.net.game.module.battle;

import java.util.List;

import cn.game.games.core.ResultObject;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * HCBattle表的战斗
 * 2024年5月29日 上午10:32:23
 * @author SYQ
 */
public abstract class HCBattleHandler extends IBattleHandler {
	@Override
	public int check(int id, int subId) {

		HCBattleConfig battleConfig = HCBattleManager.instance().get(id);
		ChapterModule chapterModule = player.getChapterModule();
		if (battleConfig.preBattle > 0 && !chapterModule.isHCBattlePass(battleConfig.preBattle)) {
			return ErrorMsgEnum.BattleLevel_pre.getId();
		}
//		if (!chapterModule.checkChapterTimes(dungeonId)) {
//			return ErrorMsgEnum.times_limit.getId();
//		}

		if (!PlayerHelper.delResources(player, battleConfig.cost, OpType.BattleStart)) {
			return ErrorMsgEnum.resource_not_enough.getId();
		}
		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin) {
		throw new UnsupportedOperationException("not support quickEnd, id: " + id + ", subId: " + subId + ", isWin: " + isWin);
	}
}
