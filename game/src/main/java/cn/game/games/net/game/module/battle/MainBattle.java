package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.game.games.cache.entity.Chapter;
import cn.game.games.core.ResultObject;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 主线战役
 * 2024年7月31日 下午12:02:38
 * @author SYQ
 */
public class MainBattle extends XiYouBattleHandler {

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

		BattleModule battleModule = player.getModule(BattleModule.class);
		Chapter chapter = battleModule.getChapter(battleModule.getAttackingId());
		BattleConfig battleConfig = BattleManager.instance().get(chapter.getBattleId());

		if (hpPercent > chapter.getHpPercent()) {
			chapter.setHpPercent(hpPercent);
		}
		if (killMonsterCount > chapter.getKillMonsterCount()) {
			chapter.setKillMonsterCount(killMonsterCount);
		}
		// 发送奖励
		List<RewardInfo> allRewards = new ArrayList<RewardInfo>();
		if (!chapter.getPass() && win) {
			chapter.setPass(true);
			if (battleConfig.BattleType == 1) {
				battleModule.setMainBattleHighest(chapter.getBattleId());
				player.handleEvent(EventTypeEnum.ChapterFirstWin, chapter.getBattleId());
				allRewards.addAll(PlayerHelper.addReward(player, battleConfig.FirstPassReward, OpType.BattleEnd));
			}
		}
		chapter.setFinishTimes(chapter.getFinishTimes() + 1);
		GameLogger.pvefight(player, battleConfig.ID, 1, win, request.getBattleTime(), chapter.getFinishTimes());

		if (request.getBattleTime() > chapter.getBattleTime()) {
			chapter.setBattleTime(request.getBattleTime());
		}

		String convertAwardFUN = battleConfig.ConvertAwardFUN;
		if (!StringUtils.isEmpty(convertAwardFUN)) {
			switch (convertAwardFUN) {
			case "FunKillConvertAward": {
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
		battleModule.addChapterTimes(battleConfig.ID);

		return ResultObject.success(allRewards);
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.BattleChapter.getId();
	}

	@Override
	void newDay() {
	}
}
