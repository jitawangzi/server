package cn.game.games.net.game.module.battle.impl;

import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.DaoHeartBattle;
import cn.game.games.net.game.module.battle.XiYouBattleHandler;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class DaoHeartImpl extends XiYouBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		if (type == 2) {
			if (!player.isFuncOpen(InitialUI.DaoXinLLiLian)) {
				return ErrorMsgEnum.func_not_open.getId();
			}
		} else if (type == 3) {
			if (!player.isFuncOpen(InitialUI.XinMoShiLian)) {
				return ErrorMsgEnum.func_not_open.getId();
			}
		} else if (type == 4) {
			if (!player.isFuncOpen(InitialUI.YaoWangBiePao)) {
				return ErrorMsgEnum.func_not_open.getId();
			}
		} else {
			return ErrorMsgEnum.player_check_error.getId();
		}
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		DaoHeartBattle daoHeartBattle = chapterModule.getDaoHeartBattle(type);
		if (dungeonId != daoHeartBattle.getNextBattleId()) {
			return ErrorMsgEnum.request_parameter_error.getId();
		}
		return 0;
	}

	@Override
	public int battleEnd(long playerId, BattleFieldEndRequest_13000003 request, BattleFieldEndResponse_13000004.Builder resp) {

		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		int attackingType = chapterModule.getAttackingType();
		BattleConfig battleConfig = BattleManager.instance().get(chapterModule.getAttackingDungeonId());
		DaoHeartBattle daoHeartBattle = chapterModule.getDaoHeartBattle(attackingType);
		daoHeartBattle.battleCompleted();
		OpType opType = attackingType == 2 ? OpType.DaoXinFirstFinish : attackingType == 3 ? OpType.XinMoFirstFinish : OpType.YaoWangComplete;

		List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FirstPassReward, opType);
		resp.addAllRewards(reward);
		return 0;
	}

	@Override
	public int check(Player player, int type, int dungeonId) {
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		BattleConfig battleConfig = BattleManager.instance().get(dungeonId);
		if (!PlayerHelper.delResources(player, battleConfig.cost, OpType.BattleStart)) {
			return ErrorMsgEnum.resource_not_enough.getId();
		}
		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.DaoHeart.getId();
	}

}
