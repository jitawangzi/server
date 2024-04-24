package cn.game.games.net.game.module.battle.impl;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.IBattleHandler;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 战役
 * @date 2024年4月12日 下午7:17:56
 * @author SYQ
 */
public class BattleChapterImpl implements IBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterModule chapterOp = player.getModule(ChapterModule.class);

		// 检查章节事件开启条件，是否可以进行当前操作
//		boolean checkCondition = PlayerHelper.checkCondition(playerId, chapterConfig.getCondition());
//		if (!checkCondition) {
//			return OldErrorMsgEnum.player_check_error.getId();
//		}
//		// 检查关卡开启条件，是否可以进行当前操作
//		checkCondition = PlayerHelper.checkCondition(playerId, levelConfig.getCondition());
//		if (!checkCondition) {
//			return OldErrorMsgEnum.player_check_error.getId();
//		}

		// 可以打这个关了
		chapterOp.addChapter(dungeonId);
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
		if (hpPercent > chapter.getHpPercent()) {
			chapter.setHpPercent(hpPercent);
		}
		if (killMonsterCount > chapter.getKillMonsterCount()) {
			chapter.setKillMonsterCount(killMonsterCount);
		}
		if (!chapter.getPass() && win) {
			chapter.setPass(true);
		}
		// 发送奖励
		BattleConfig battleConfig = BattleManager.instance().get(chapter.getBattleId());
		List<RewardInfo> allRewards = new ArrayList<RewardInfo>();
		List<RewardInfo> rewards = PlayerHelper.addReward(player, win ? battleConfig.WinRandom : battleConfig.FailRandom);
		allRewards.addAll(rewards);
		String convertAwardFUN = battleConfig.ConvertAwardFUN;
		if (convertAwardFUN != null) {
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
						List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FUNRandom[index]);
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
						List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FUNRandom[index]);
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
