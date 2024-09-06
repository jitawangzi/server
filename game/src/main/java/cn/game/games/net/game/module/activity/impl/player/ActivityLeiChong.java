package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.ActivityLeiChongConfig;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.manager.ActivityLeiChongManager;
import cn.game.protocol.generated.manager.ConditionManager;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg;
import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@ActivityType(type = ActivityTypeEnum.ActivityLeiChong)
public class ActivityLeiChong extends PlayerActivityBase {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {EventTypeEnum.Charge};
	/**
	 *  超出的金额自动保存至下一轮；
	 */
	int moreRechargeNum;
	/**
	 * 领取过的任务奖励的id
	 */
	List<Integer> rewardTaskIds = new ArrayList<>();

	@Override
	public void startUp() {
		moreRechargeNum = 0;
		refreshActivityData();
	}

	//刷新任务
	private void refreshActivityData() {
		rewardTaskIds.clear();
		QuestModule questModule = player.getQuestModule();
		int maxRechargeNum = getMaxRechargeNum();
		moreRechargeNum = Math.max(0,moreRechargeNum - maxRechargeNum);
		getActivityLeiChongConfigList().forEach(activityLeiChongConfig -> {
					//删除旧任务
					questModule.remove(activityLeiChongConfig.ID);
					//注册新任务
					Quest newTask = questModule.open(activityLeiChongConfig.taskID,true);

					if (moreRechargeNum > 0){
						newTask.setConditionValue(moreRechargeNum);
					}
				});
		if (moreRechargeNum > maxRechargeNum){
			moreRechargeNum = moreRechargeNum -  maxRechargeNum;
		}
	}

	private int getMaxRechargeNum() {
		int maxRecharge = 0;
		for (ActivityLeiChongConfig config : getActivityLeiChongConfigList()) {
			QuestConfig questConfig = QuestManager.instance().get(config.taskID);
			ConditionConfig conditionConfig = ConditionManager.instance().get(questConfig.Condition);
			if (conditionConfig.numParam > maxRecharge){
				maxRecharge = conditionConfig.numParam;
			}
		}
		return maxRecharge;
	}

	private List<ActivityLeiChongConfig> getActivityLeiChongConfigList() {
		return ActivityLeiChongManager.instance().list().stream().filter(activityLeiChongConfig -> activityLeiChongConfig.ActivityiD == id).collect(Collectors.toList());
	}

	@Override
	public Message buildActivityShowInfo() {
		ActivityMsg.ActivityLeiChongInfoResponse_11000052.Builder res = ActivityMsg.ActivityLeiChongInfoResponse_11000052.newBuilder();
		res.setActivityId(id);
		getActivityLeiChongConfigList().forEach(activityLeiChongConfig -> {
			res.addTaskIds(activityLeiChongConfig.taskID);
		});
		return res.build();
	}


	@Override
	public boolean newDay() {
		return true;
	}
	@Override
	public List<RewardInfo> receive(int id) {
		QuestModule questModule = player.getQuestModule();
		rewardTaskIds.add(id);
		return questModule.receive(id);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		if (event.getType() == EventTypeEnum.Charge){
			int num = event.getIntParameter(0);
			moreRechargeNum += num;
		}
	}

	@Override
	public int canReceive(List<Integer> ids) {
		return canReceive(ids,rewardTaskIds,player);
	}

	@Override
	public void checkRefreshActivity() {
		if (rewardTaskIds.size() == getActivityLeiChongConfigList().size()){
			refreshActivityData();
		}
	}

	@Override
	public void shutDown() {
		QuestModule questModule = player.getQuestModule();
		getActivityLeiChongConfigList().forEach(activityLeiChongConfig -> {
			questModule.remove(activityLeiChongConfig.taskID);
		});

	}
}
