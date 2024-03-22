package cn.game.games.net.game.module.battle;

import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.cache.op.impl.ChapterOp;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.BattleLevelConfig;
import cn.game.protocol.generated.config.RoutineTrainingConfig;
import cn.game.protocol.generated.enume.DungeonTypeEnum;
import cn.game.protocol.generated.manager.BattleLevelManager;
import cn.game.protocol.generated.manager.RoutineTrainingManager;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.protobuf.BattleChapterMsg.BattleLevelEndResponse_13000004;
import cn.game.util.DateUtil;

/**
 * @Description 进阶训练战斗
 * @date 2021年1月15日 上午10:30:42
 * @author SYQ
 */
public class BattleTrainingImpl implements IBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterOp chapterOp = player.getModule(ChapterOp.class);

		RoutineTrainingConfig routineTrainingConfig = RoutineTrainingManager.getInstance().getRoutineTrainingConfig(dungeonId);
		if (routineTrainingConfig == null) {
			return OldErrorMsgEnum.config_data_not_found.getId();
		}
		//等级限制
		if (routineTrainingConfig.getLevel() > player.getData().getLevel()) {
			return OldErrorMsgEnum.unlock.getId();
		}

		int profession = routineTrainingConfig.getProfession();

		//检查职业
		boolean checkPro = chapterOp.checkProfession(playerId, profession, lineupId, type);
		if (!checkPro) {
			return OldErrorMsgEnum.routinetranin_profession_not_match.getId();
		}
		//检查开启时间
		List<Integer> openTime = routineTrainingConfig.getOpenTime();
		int dayOfWeek = DateUtil.getDayOfWeek();
		if (!openTime.contains(dayOfWeek)) {
			return OldErrorMsgEnum.unlock.getId();
		}
		//检查前进阶训练关卡
		RoutineTrainingConfig config = RoutineTrainingManager.getInstance().getRoutineTrainingConfigNullable(dungeonId - 1);
		if (config != null && config.getProfession() == routineTrainingConfig.getProfession()) {
			if (!chapterOp.checkPreTraining(config)) {
				return OldErrorMsgEnum.BattleLevel_pre.getId();
			}
		}
		return 0;
	}

	@Override
	public int battleEnd(long playerId, boolean win, List<Integer> starList, BattleLevelEndResponse_13000004.Builder resp) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterOp chapterOp = player.getModule(ChapterOp.class);
		int id = chapterOp.getAttackingId();
		BattleLevelConfig levelConfig = BattleLevelManager.getInstance().getBattleLevelConfig(id);

//		if (player.getData().getTrainingRewardTimes() > 0) {
//			player.getData().setTrainingRewardTimes(player.getData().getTrainingRewardTimes() - 1);
//			List<RewardItem> rewardItems = PlayerHelper.addResources(playerId, levelConfig.getSpecialReward());
//			resp.addAllSpecialRewards(PbBuilder.buildRewardInfo(rewardItems));
//		}
//		if (win) {
//			chapterOp.addBattleLevelPass(id, starList);
//		}

		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.RoutineTraining.getId();
	}

}
