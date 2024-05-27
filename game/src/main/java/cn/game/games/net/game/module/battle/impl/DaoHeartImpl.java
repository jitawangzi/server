package cn.game.games.net.game.module.battle.impl;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.IBattleHandler;
import cn.game.protocol.generated.config.BattleLevelConfig;
import cn.game.protocol.generated.manager.BattleLevelManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.util.DateUtil;

public class DaoHeartImpl implements IBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterModule chapterOp = player.getModule(ChapterModule.class);

		//等级限制
//		if (routineTrainingConfig.getLevel() > player.getData().getLevel()) {
//			return ErrorMsgEnum.unlock.getId();
//		}

		//检查开启时间
		int dayOfWeek = DateUtil.getDayOfWeek();
//		if (!openTime.contains(dayOfWeek)) {
//			return ErrorMsgEnum.unlock.getId();
		return 0;
	}

	@Override
	public int battleEnd(long playerId, BattleFieldEndRequest_13000003 request, BattleFieldEndResponse_13000004.Builder resp) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterOp = player.getModule(ChapterModule.class);
		int id = chapterOp.getAttackingId();
		BattleLevelConfig levelConfig = BattleLevelManager.getInstance().getBattleLevelConfig(id);

//		if (player.getData().getTrainingRewardTimes() > 0) {
//			player.getData().setTrainingRewardTimes(player.getData().getTrainingRewardTimes() - 1);
//			List<RewardItem> rewardItems = PlayerHelper.addResources(player, levelConfig.getSpecialReward());
//			resp.addAllSpecialRewards(PbBuilder.buildRewardInfo(rewardItems));
//		}
//		if (win) {
//			chapterOp.addBattleLevelPass(id, starList);
//		}

		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.DaoHeart.getId();
	}

}
