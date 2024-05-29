package cn.game.games.net.game.module.battle.impl;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.HCBattleHandler;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class HCBattleChapterImpl extends HCBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);

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
		chapterModule.addChapter(dungeonId);
		return 0;
	}

	@Override
	public int battleEnd(long playerId, BattleFieldEndRequest_13000003 request, BattleFieldEndResponse_13000004.Builder resp) {
		boolean win = request.getWin();
		int killMonsterCount = request.getKillMonsterCount();
		int hpPercent = request.getHpPercent();

		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		Chapter chapter = chapterModule.getChapter(chapterModule.getAttackingDungeonId());
		BattleConfig battleConfig = BattleManager.instance().get(chapter.getBattleId());

		if (hpPercent > chapter.getHpPercent()) {
			chapter.setHpPercent(hpPercent);
		}
		if (killMonsterCount > chapter.getKillMonsterCount()) {
			chapter.setKillMonsterCount(killMonsterCount);
		}
		if (!chapter.getPass() && win) {
			chapter.setPass(true);
			if (battleConfig.BattleType == 1) {
				chapterModule.setMainBattleHighest(chapter.getBattleId());
				player.handleEvent(EventTypeEnum.ChapterFirstWin, battleConfig.ID);
			}
		}
		// 发送奖励
		List<RewardInfo> allRewards = new ArrayList<RewardInfo>();
		if (win) {
			player.handleEvent(EventTypeEnum.ChapterWin, battleConfig.ID);
			List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.WinRandom, OpType.BattleEnd);
			allRewards.addAll(reward);
		}
		chapter.setFinishTimes(chapter.getFinishTimes() + 1);
//		GameLogger.pvefight(player, battleConfig.ID, 1, win, request.getBattleTime(), chapter.getFinishTimes());

		if (request.getBattleTime() > chapter.getBattleTime()) {
			chapter.setBattleTime(request.getBattleTime());
		}

		// 增加次数。
		chapterModule.addChapterTimes(battleConfig.ID);

		resp.addAllRewards(allRewards);
		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.HCBattleChapter.getId();
	}

}
