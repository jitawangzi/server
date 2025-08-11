package cn.game.games.net.game.module.player.pointreward;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.battle.LingPoBattle;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.config.QuestPointRewardConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.generated.manager.QuestPointRewardManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.OpType;

public class PointRewardData {
	public int[] conditionStage;
	public int[] randomRewardStage = null;
	public int[][] fixRewardStage = null;
	public int pointType = 0;
	public OpType opType = null;
	
	public static PointRewardData valueOf(Player player, PointRewardType type, int subType) {
		PointRewardData data = new PointRewardData();
		if (type == PointRewardType.QUEST) {
			QuestPointRewardConfig questPointRewardConfig = QuestPointRewardManager.instance().get(subType);
			data.pointType = questPointRewardConfig.PointType;

			data.conditionStage = questPointRewardConfig.Stage;
			data.fixRewardStage = questPointRewardConfig.Reward;
			data.opType = OpType.QuestActiveReward;
		} else if (type == PointRewardType.DAY_CHALLENGE) {
			HCBattleConfig battleConfig = HCBattleManager.instance().get(subType);
			data.pointType = Asset.dailyIntegral.ID;

			data.conditionStage = battleConfig.DailyIntegralCondition;
			data.randomRewardStage = battleConfig.DailyInBoxRandomId;
			data.opType = OpType.DayChallengeReward;
		} else if (type == PointRewardType.LingPo) {
			LingPoBattle lingPoBattle = player.getBattleModule().getBattle(DungeonTypeEnum.LingPo);
			BattleConfig battleConfig = BattleManager.instance().getNullable(lingPoBattle.getBattleId());
			if (battleConfig != null) {
				data.pointType = Asset.SpiritBattlePoint.ID;

				data.conditionStage = battleConfig.BattleBoxTrigger;
				data.randomRewardStage = battleConfig.BattleBoxRandomId;
				data.opType = OpType.LingPoBattle;
			}
		} else if (type == PointRewardType.WorldBoss) {
			BattleConfig battleConfig = BattleManager.instance().get(subType);
//			pointType = Asset.SpiritBattlePoint.ID;
			data.conditionStage = battleConfig.BattleBoxTrigger;
			data.randomRewardStage = battleConfig.BattleBoxRandomId;
			data.opType = OpType.WorldBoss;
		} else if (type == PointRewardType.Guild) {
			QuestPointRewardConfig questPointRewardConfig = QuestPointRewardManager.instance().get(subType);
			data.pointType = questPointRewardConfig.PointType;

			data.conditionStage = questPointRewardConfig.Stage;
			data.fixRewardStage = questPointRewardConfig.Reward;
			data.opType = OpType.GuildQuestReward;
		}else {
			throw new IllegalArgumentException("没有实现的PointRewardType :" + type);
		}
		return data;
	}
	
}