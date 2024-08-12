package cn.game.games.net.game.module.player.pointreward;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.ResultObject;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.battle.LingPoBattle;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.HCBattleConfig;
import cn.game.protocol.generated.config.QuestPointRewardConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.HCBattleManager;
import cn.game.protocol.generated.manager.QuestPointRewardManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 积分领宝箱， 七日任务， 每日挑战积分等等
 * 2024年5月27日 上午11:16:05
 * @author SYQ
 */
public class PointRewardModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ResourceRemove, EventTypeEnum.NewDay };

	/** 任务积分宝箱活跃奖励领取情况 type: subType : 领过的奖励index */
	private Map<PointRewardType, Map<Integer, List<Integer>>> activeRewardMap = new HashMap<PointRewardType, Map<Integer, List<Integer>>>();

	public Map<Integer, List<Integer>> getActiveRewardTypeMap(PointRewardType type) {
		Map<Integer, List<Integer>> map = activeRewardMap.get(type);
		if (map == null) {
			map = new HashMap<Integer, List<Integer>>();
			activeRewardMap.put(type, map);
		}
		return map;
	}
	public List<Integer> getActiveRewardList(PointRewardType type, int subId) {
		Map<Integer, List<Integer>> map = activeRewardMap.get(type);
		if (map == null) {
			map = new HashMap<Integer, List<Integer>>();
			activeRewardMap.put(type, map) ; 
		}
		List<Integer> list = map.get(subId);
		if (list == null) {
			list = new ArrayList<>();
			map.put(subId, list);
		}
		return list;
	}

	public List<Integer> getActiveRewardList(PointRewardType type) {
		return getActiveRewardList(type, 0);
	}

	public void clearActiveRewardList(PointRewardType type, int subType) {
		List<Integer> activeRewardList = getActiveRewardList(type, subType);
		activeRewardList.clear();
	}

	public void clearActiveRewardList(PointRewardType type) {
		clearActiveRewardList(type, 0);
	}

	public ResultObject<List<RewardInfo>> addReward(PointRewardType type, int subType, int index) {
		return addReward(type, subType, index, 0);
	}
	
	/**
	 * 领取任务积分宝箱奖励，达到xx积分，领取xx奖励的逻辑
	 *
	 * @param type  区分玩法的类型
	 * @param subType  某些玩法可能有多条这种奖励，一般用对应表的id来区分。 如果一个玩法只有一条这种奖励，则传0
	 * @param index  多个奖励，领取哪个奖励的索引，-1领取所有能领的奖励
	 * @param count 积分数值，一般是从player获取，如果没有在Asset里定义的，可以直接从这里传过来。大多用不到这个参数,例如伤害值 
	 * @return
	 */
	public ResultObject<List<RewardInfo>> addReward(PointRewardType type, int subType, int index, int count) {

		int[] conditionStage;
		int[] randomRewardStage = null;
		int[][] fixRewardStage = null;
		int pointType = 0;
		OpType opType = null;

		if (type == PointRewardType.QUEST) {
			QuestPointRewardConfig questPointRewardConfig = QuestPointRewardManager.instance().get(subType);
			pointType = questPointRewardConfig.PointType;

			conditionStage = questPointRewardConfig.Stage;
			fixRewardStage = questPointRewardConfig.Reward;
			opType = OpType.QuestActiveReward;
		} else if (type == PointRewardType.DAY_CHALLENGE) {
			HCBattleConfig battleConfig = HCBattleManager.instance().get(subType);
			pointType = Asset.dailyIntegral.ID;

			conditionStage = battleConfig.DailyIntegralCondition;
			randomRewardStage = battleConfig.DailyInBoxRandomId;
			opType = OpType.DayChallengeReward;
		} else if (type == PointRewardType.LingPo) {
			LingPoBattle lingPoBattle = player.getChapterModule().getBattle(DungeonTypeEnum.LingPo);
			BattleConfig battleConfig = BattleManager.instance().get(lingPoBattle.getBattleId());
			pointType = Asset.SpiritBattlePoint.ID;

			conditionStage = battleConfig.BattleBoxTrigger;
			randomRewardStage = battleConfig.BattleBoxRandomId;
			opType = OpType.LingPoBattle;
		} else if (type == PointRewardType.WorldBoss) {
			BattleConfig battleConfig = BattleManager.instance().get(subType);
//			pointType = Asset.SpiritBattlePoint.ID;
			conditionStage = battleConfig.BattleBoxTrigger;
			randomRewardStage = battleConfig.BattleBoxRandomId;
			opType = OpType.WorldBoss;
		} else {
			throw new IllegalArgumentException("没有实现的PointRewardType :" + type);
		}

		if (fixRewardStage != null && index >= fixRewardStage.length) {
			return ResultObject.fail(ErrorMsgEnum.request_parameter_error.getId());
		}
		if (randomRewardStage != null && index >= randomRewardStage.length) {
			return ResultObject.fail(ErrorMsgEnum.request_parameter_error.getId());
		}

		List<Integer> activeRewardList = getActiveRewardList(type, subType);

		List<RewardInfo> totalRewards = new ArrayList<>();

		// Check if index is -1 to indicate all rewards
		if (index == -1) {
			for (int i = 0; i < conditionStage.length; i++) {
				if (!activeRewardList.contains(i)) {
					long point = count > 0 ? count : player.getCurrencyModule().getCount(pointType);
					int needPoint = conditionStage[i];
					if (point >= needPoint) {
						List<RewardInfo> reward;
						if (randomRewardStage != null) {
							reward = PlayerHelper.addReward(player, randomRewardStage[i], opType);
						} else if (fixRewardStage != null) {
							reward = PlayerHelper.addResources(player, fixRewardStage[i], opType);
						} else {
							throw new IllegalArgumentException(MessageFormat.format("Invalid reward type,type[{}]subType[{}]index[{}]", type, subType, index));
						}
						totalRewards.addAll(reward);
						activeRewardList.add(i);
					}
				}
			}
		} else {
			if (activeRewardList.contains(index)) {
				return ResultObject.fail(ErrorMsgEnum.repeat_request.getId());
			}

			long point = count > 0 ? count : player.getCurrencyModule().getCount(pointType);
			int needPoint = conditionStage[index];
			if (point < needPoint) {
				return ResultObject.fail(ErrorMsgEnum.illegal_request.getId());
			}

			List<RewardInfo> reward = null;
			if (randomRewardStage != null) {
				reward = PlayerHelper.addReward(player, randomRewardStage[index], opType);
			} else if (fixRewardStage != null) {
				reward = PlayerHelper.addResources(player, fixRewardStage[index], opType);
			} else {
				throw new IllegalArgumentException(MessageFormat.format("Invalid reward type,type[{}]subType[{}]index[{}]", type, subType, index));
			}
			totalRewards.addAll(reward);
			activeRewardList.add(index);
		}

		return ResultObject.success(totalRewards);
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
	}
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case FuncOpen: {
			break;
		}
		}
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void initFromDbAfter() {
		// TODO Auto-generated method stub

	}

}
