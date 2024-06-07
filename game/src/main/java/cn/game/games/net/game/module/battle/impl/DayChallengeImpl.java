package cn.game.games.net.game.module.battle.impl;

import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.BattleDayChallenge;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.HCBattleHandler;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class DayChallengeImpl extends HCBattleHandler {

	@Override
	public int battleStart(long playerId, int type, int dungeonId, int id, int lineupId, long uid) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		BattleDayChallenge dayChallenge = chapterModule.getDayChallenge();
		if (dayChallenge.getBattleTimes() >= GlobalConst.DailyNum) {
			return ErrorMsgEnum.times_limit.getId();
		}
		return 0;
	}

	@Override
	public int battleEnd(long playerId, BattleFieldEndRequest_13000003 request, BattleFieldEndResponse_13000004.Builder resp) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		int battleId = chapterModule.getAttackingDungeonId();

		int battleTime = request.getBattleTime(); 
		int hcFailRewardId = BattleHelper.hcFailRewardId(battleId, battleTime);
		List<RewardInfo> reward = PlayerHelper.addReward(player, hcFailRewardId, OpType.BattleEnd);
		resp.addAllRewards(reward);
		BattleDayChallenge dayChallenge = chapterModule.getDayChallenge();
		dayChallenge.setBattleTimes(dayChallenge.getBattleTimes() + 1);
		return 0;
	}

	@Override
	public int getType() {
		return DungeonTypeEnum.DayChallenge.getId();
	}

}
