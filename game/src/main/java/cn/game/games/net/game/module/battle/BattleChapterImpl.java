package cn.game.games.net.game.module.battle;

import java.util.List;

import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.op.impl.ChapterOp;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.BattleChapterConfig;
import cn.game.protocol.generated.config.BattleLevelConfig;
import cn.game.protocol.generated.enume.DungeonTypeEnum;
import cn.game.protocol.generated.manager.BattleChapterManager;
import cn.game.protocol.generated.manager.BattleLevelManager;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**
 * @Description 意识空间战斗
 * @date 2021年1月15日 上午10:30:54
 * @author SYQ
 */
public class BattleChapterImpl implements IBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterOp chapterOp = player.getModule(ChapterOp.class);

		BattleLevelConfig levelConfig = BattleLevelManager.getInstance().getBattleLevelConfig(id);
		if (levelConfig == null) {
			return OldErrorMsgEnum.config_data_not_found.getId() ;
		}
		BattleChapterConfig chapterConfig = BattleChapterManager.getInstance().getBattleChapterConfig(levelConfig.getBattleChapterId());
		if (chapterConfig == null) {
			return OldErrorMsgEnum.config_data_not_found.getId();
		}
		// 检查章节事件开启条件，是否可以进行当前操作
		boolean checkCondition = PlayerHelper.checkCondition(playerId, chapterConfig.getCondition());
		if (!checkCondition) {
			return OldErrorMsgEnum.player_check_error.getId();
		}
		// 检查关卡开启条件，是否可以进行当前操作
		checkCondition = PlayerHelper.checkCondition(playerId, levelConfig.getCondition());
		if (!checkCondition) {
			return OldErrorMsgEnum.player_check_error.getId();
		}

		// 可以打这个关了
		Chapter chapter = chapterOp.getChapter(levelConfig.getBattleChapterId());
		if (chapter == null) {
			chapter = Chapter.valueOf(playerId, levelConfig.getBattleChapterId());
			chapterOp.addChapter(chapter);
		}
		return 0;

	}

	@Override
	public int battleEnd(long playerId, boolean win, List<Integer> starList, BattleFieldEndResponse_13000004.Builder resp) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterOp chapterOp = player.getModule(ChapterOp.class);
		int id = chapterOp.getAttackingId();
		BattleLevelConfig levelConfig = BattleLevelManager.getInstance().getBattleLevelConfig(id);

		if (!win) {
			return 0 ; 
		}
		
		boolean first = chapterOp.addBattleLevelPass(id, starList);
		if (first) {
			List<RewardInfo> rewardItems = PlayerHelper.addResources(playerId, levelConfig.getSpecialReward());
			resp.addAllSpecialRewards(rewardItems);
		}

		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.BattleChapter.getId();
	}

}
