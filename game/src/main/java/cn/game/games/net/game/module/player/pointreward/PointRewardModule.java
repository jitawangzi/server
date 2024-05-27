package cn.game.games.net.game.module.player.pointreward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.ResultObject;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.QuestPointRewardConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.QuestPointRewardManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 积分领宝箱， 七日任务， 每日挑战积分等等
 * @date 2024年5月27日 上午11:16:05
 * @author SYQ
 */
public class PointRewardModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ResourceRemove, EventTypeEnum.NewDay };

	/** 任务积分宝箱活跃奖励领取情况 */
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

	public void clearActiveRewardList(PointRewardType type, int subId) {
		List<Integer> activeRewardList = getActiveRewardList(type, subId);
		activeRewardList.clear();
	}

	public ResultObject addReward(PointRewardType type, int subId, int index) {

		int[] conditionStage;
		int[] randomRewardStage = null;
		int[][] fixRewardStage = null;
		int pointType = 0;
		OpType opType = null ; 
		if (type == PointRewardType.QUEST) {
			QuestPointRewardConfig questPointRewardConfig = QuestPointRewardManager.instance().get(subId);
			pointType = questPointRewardConfig.PointType;

			conditionStage = questPointRewardConfig.Stage;
			fixRewardStage = questPointRewardConfig.Reward;
			opType = OpType.QuestActiveReward; 
		} else if (type == PointRewardType.DAY_CHALLENGE) {
			BattleConfig battleConfig = BattleManager.instance().get(subId);
			pointType = Asset.dailyIntegral.ID;

			conditionStage = battleConfig.DailyIntegralCondition;
			randomRewardStage = battleConfig.DailyInBoxRandomId;
			opType = OpType.DayChallengeReward; 
		} else {
			throw new IllegalArgumentException("没有实现的PointRewardType :" + type);
		}
		if (fixRewardStage != null && index >= fixRewardStage.length) {
			return ResultObject.fail(ErrorMsgEnum.request_parameter_error.getId());
		}
		if (randomRewardStage != null && index >= randomRewardStage.length) {
			return ResultObject.fail(ErrorMsgEnum.request_parameter_error.getId());
		}

		List<Integer> activeRewardList = getActiveRewardList(type, subId);
		if (activeRewardList.contains(index)) {
			return ResultObject.fail(ErrorMsgEnum.repeat_request.getId());
		}

		long point = player.getCurrencyModule().getCount(pointType);

		int needPoint = conditionStage[index];
		if (point < needPoint) {
			return ResultObject.fail(ErrorMsgEnum.illegal_request.getId());
		}
		List<RewardInfo> reward = null;
		if (randomRewardStage != null) {
			reward = PlayerHelper.addReward(player, randomRewardStage[index], opType);
		} else if (fixRewardStage != null) {
			reward = PlayerHelper.addResources(player, fixRewardStage[index], opType);
		}
		activeRewardList.add(index);
		return ResultObject.success(reward);
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
