package cn.game.games.net.game.module.battle.impl;

import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.BattleDayChallenge;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.battle.IBattleHandler;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndResponse_13000004;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class DayChallengeImpl implements IBattleHandler {

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
		BattleConfig battleConfig = BattleManager.instance().get(battleId);

//		3;5;8
//		≤3——第1个
//		3＜x≤5——第2个
//		5＜x≤8——第3个,> 8 第三个。 

		int battleTime = request.getBattleTime(); 
		int[] failRandomTrigger = battleConfig.FailRandomTrigger;
		int index = 0;
		for (int i = 0; i < failRandomTrigger.length; i++) {
			if (battleTime <= failRandomTrigger[i]) {
				index = i;
			}
		}
		if (battleTime >= failRandomTrigger[failRandomTrigger.length - 1]) {
			index = failRandomTrigger.length - 1;
		}
		List<RewardInfo> reward = PlayerHelper.addReward(player, battleConfig.FailRandom[index], OpType.BattleEnd);
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
