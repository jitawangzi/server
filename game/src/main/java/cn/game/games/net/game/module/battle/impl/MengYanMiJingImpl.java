package cn.game.games.net.game.module.battle.impl;

import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.MengYanMiJingBattle;
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

public class MengYanMiJingImpl extends XiYouBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		if (!player.isFuncOpen(InitialUI.NightmareRealm)) {
			return ErrorMsgEnum.func_not_open.getId();
		}

		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		MengYanMiJingBattle battle = chapterModule.getMengYanMiJingBattle();
		if (dungeonId != battle.getStartBattleId()) {
			return ErrorMsgEnum.request_parameter_error.getId();
		}
		return 0;
	}

	@Override
	public int battleEnd(long playerId, BattleFieldEndRequest_13000003 request, BattleFieldEndResponse_13000004.Builder resp) {

		if (!request.getWin()) {
			return 0;
		}

		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		int attackingType = chapterModule.getAttackingType();
		BattleConfig battleConfig = BattleManager.instance().get(chapterModule.getAttackingDungeonId());
		MengYanMiJingBattle battle = chapterModule.getMengYanMiJingBattle();
		boolean newRecord = battle.battleCompleted();
		if (newRecord) {
			OpType opType = OpType.MengYanMiJingFirstFinish;
			List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FirstPassReward, opType);
			resp.addAllRewards(reward);
		}
		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.MengYanMiJing.getId();
	}

}
