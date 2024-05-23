package cn.game.games.net.game.module.battle.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.IBattleHandler;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class BattleChapterImpl implements IBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);

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
			}
		}
		if (win) {
			player.handleEvent(EventTypeEnum.ChapterWin, battleConfig.ID);
		}
		chapter.setFinishTimes(chapter.getFinishTimes() + 1);
		GameLogger.pvefight(player, battleConfig.ID, 1, win, request.getBattleTime(), chapter.getFinishTimes());

		if (request.getBattleTime() > chapter.getBattleTime()) {
			chapter.setBattleTime(request.getBattleTime());
		}
		// 发送奖励
		List<RewardInfo> allRewards = new ArrayList<RewardInfo>();
		List<RewardInfo> rewards = PlayerHelper.addReward(player, win ? battleConfig.WinRandom : battleConfig.FailRandom, OpType.BattleEnd);
		allRewards.addAll(rewards);
		String convertAwardFUN = battleConfig.ConvertAwardFUN;
		if (!StringUtils.isEmpty(convertAwardFUN)) {
			switch (convertAwardFUN) {
			case "FunKillConvertAward": {
				int index = -1 ; 
				for (int i = 0; i < battleConfig.FUNCondition.length; i++) {
					int tmp = battleConfig.FUNCondition[i];
					if (killMonsterCount >= tmp) {
						index = i ; 
						break;
					}
				}
				if (index >= 0) {
					for (int i = 0; i < battleConfig.FUNFactor[index]; i++) {
						List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FUNRandom[index], OpType.BattleEnd);
						allRewards.addAll(reward);
					}
				}
				break;
			}
			case "FunKillScoreAward": {
				int index = -1;
				for (int i = 0; i < battleConfig.FUNCondition.length; i++) {
					int tmp = battleConfig.FUNCondition[i];
					if (killMonsterCount >= tmp) {
						index = i;
						break;
					}
				}
				if (index >= 0) {
					for (int i = 0; i < battleConfig.FUNFactor[index]; i++) {
						List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FUNRandom[index], OpType.BattleEnd);
						allRewards.addAll(reward);
					}
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + convertAwardFUN);
			}
		}

		// 增加次数。
		chapterModule.addChapterTimes(battleConfig.ID);

		resp.addAllRewards(allRewards);
		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.BattleChapter.getId();
	}

}
