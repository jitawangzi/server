package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.entity.Chapter;
import cn.game.games.core.ResultObject;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 灵魄之战
 * 2024年7月31日 下午12:02:38
 * @author SYQ
 */
public class HCMainBattle extends XiYouBattleHandler {


	@Override
	public int battleStart(int id) {
		BattleModule battleModule = player.getModule(BattleModule.class);

		// 检查章节事件开启条件，是否可以进行当前操作
//		boolean checkCondition = PlayerHelper.checkCondition(playerId, chapterConfig.getCondition());
//		if (!checkCondition) {
//			return ErrorMsgEnum.player_check_error.getId();
//		}
//		// 检查关卡开启条件，是否可以进行当前操作
//		checkCondition = PlayerHelper.checkCondition(playerId, levelConfig.getCondition());
//		if (!checkCondition) {
//			return ErrorMsgEnum.player_check_error.getId();
//		}

		// 可以打这个关了
		battleModule.addChapter(id);
		return 0;
	}

	@Override
	public ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request) {
		boolean win = request.getWin();
		int killMonsterCount = request.getKillMonsterCount();
		int hpPercent = request.getHpPercent();
		int battleTime = request.getBattleTime();

		BattleModule battleModule = player.getModule(BattleModule.class);
		Chapter chapter = battleModule.getChapter(battleModule.getAttackingId());
		Integer battleId = chapter.getBattleId();
		HCBattleConfig battleConfig = HCBattleManager.instance().get(battleId);

		if (hpPercent > chapter.getHpPercent()) {
			chapter.setHpPercent(hpPercent);
		}
		if (killMonsterCount > chapter.getKillMonsterCount()) {
			chapter.setKillMonsterCount(killMonsterCount);
		}
		if (!chapter.getPass() && win) {
			chapter.setPass(true);
			if (battleConfig.BattleType == 11) {
				battleModule.setMainBattleHighest(battleId);
				player.handleEvent(EventTypeEnum.HCChapterFirstWin, battleConfig.ID);
			}
		}
		// 发送奖励
		List<RewardInfo> allRewards = new ArrayList<RewardInfo>();
		if (win) {
			player.handleEvent(EventTypeEnum.HCChapterWin, battleConfig.ID);
			List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.WinRandom, OpType.BattleEnd);
			allRewards.addAll(reward);
		} else {
			int hcFailRewardId = BattleHelper.hcFailRewardId(battleId, battleTime);
			List<RewardInfo> reward = PlayerHelper.addReward(player, hcFailRewardId, OpType.BattleEnd);
			allRewards.addAll(reward);
		}
		chapter.setFinishTimes(chapter.getFinishTimes() + 1);
//		GameLogger.pvefight(player, battleConfig.ID, 1, win, request.getBattleTime(), chapter.getFinishTimes());

		if (request.getBattleTime() > chapter.getBattleTime()) {
			chapter.setBattleTime(request.getBattleTime());
		}

		// 增加次数。
//		battleModule.addChapterTimes(battleConfig.ID);

		return ResultObject.success(allRewards);
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.HCBattleChapter.getId();
	}

	@Override
	void newDay() {
	}

}
