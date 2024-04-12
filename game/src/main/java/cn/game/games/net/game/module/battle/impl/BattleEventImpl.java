package cn.game.games.net.game.module.battle.impl;

import java.util.List;
import java.util.Map.Entry;

import cn.game.games.cache.entity.BattleRandomEvent;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.IBattleHandler;
import cn.game.protocol.generated.config.BattleEventConfig;
import cn.game.protocol.generated.enume.DungeonTypeEnum;
import cn.game.protocol.generated.manager.BattleEventManager;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;

/**
 * @Description 随机事件里的战斗
 * @date 2021年1月15日 上午10:30:16
 * @author SYQ
 */
public class BattleEventImpl implements IBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterModule chapterOp = player.getModule(ChapterModule.class);

		if (!chapterOp.hasBattleEvent(uid)) {

			return OldErrorMsgEnum.player_check_error.getId();
		}

		return 0;

	}

	@Override
	public int battleEnd(long playerId, boolean win, int killMonsterCount, int hpPercent, BattleFieldEndResponse_13000004.Builder resp) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterModule chapterOp = player.getModule(ChapterModule.class);
		long uid = chapterOp.getAttackingUid();
		if (!chapterOp.hasBattleEvent(uid)) {

			return OldErrorMsgEnum.player_check_error.getId();
		}
		// 打完事件，给奖励
		BattleRandomEvent battleEvent = chapterOp.getBattleEvent(uid);
		int randomId = battleEvent.getRandomId();
		BattleEventConfig battleEventConfig = BattleEventManager.getInstance().getBattleEventConfig(randomId);
		List<Entry<Integer, Integer>> reward = battleEventConfig.getReward();
//		if (!reward.isEmpty()) {
//			resp.addAllCommonRewards(PlayerHelper.addResources(playerId, reward));
//		}
		if (battleEventConfig.getRandomReward() > 0) {
//			List<RewardInfo> addRandomRewards = PlayerHelper.addRandomRewards(playerId,
//					battleEventConfig.getRandomReward());
//			resp.addAllSpecialRewards(addRandomRewards);
		}
		chapterOp.removeBattleEvent(uid);

		return 0;

	}

	@Override
	public int getType() {
		return DungeonTypeEnum.BattleEvent.getId();
	}

}
