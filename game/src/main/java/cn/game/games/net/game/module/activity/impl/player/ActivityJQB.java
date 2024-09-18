package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.protocol.generated.config.ActivityJQBConfig;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityJQBManager;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import io.vertx.core.Future;
import io.vertx.core.Promise;

@ActivityType(type = ActivityTypeEnum.ActivityJQB)
public class ActivityJQB extends PlayerActivityBase {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};
	/**
	 * 当前进行的id
	 */
	int curId;
	List<Integer> rewardIdList = new ArrayList<>();
	@Override
	public Message buildActivityShowInfo() {
		ActivityMsg.ActivityJQBInfoResponse_11000082.Builder res = ActivityMsg.ActivityJQBInfoResponse_11000082.newBuilder();
		res.setActivityId(id).setTaskId(ActivityJQBManager.instance().get(curId).taskID);
		return res.build();
	}

	@Override
	public List<RewardInfo> receive(int id) {
		return null;
	}

	@Override
	public void startUp() {
		curId = 0;
		rewardIdList.clear();
		refreshActivity(1);
	}

	private void refreshActivity(int maxLen) {
		if (maxLen < 0 ) return;
		curId ++;
		ActivityJQBConfig jqbConfig = ActivityJQBManager.instance().get(curId);
		if (jqbConfig == null){
			curId = 0;
			rewardIdList.clear();
			refreshActivity(--maxLen);
			return;
		}
		QuestModule questModule = player.getQuestModule();
		questModule.remove(jqbConfig.taskID);
		questModule.open(jqbConfig.taskID,true);

		GameLogger.activity(player, id, curId);
	}

	public void checkRefresh() {
		ActivityJQBConfig jqbConfig = ActivityJQBManager.instance().get(curId);
    	Quest quest = player.getQuestModule().get(jqbConfig.taskID);
		if (quest.getState() == QuestHelper.REWARDED){
			quest.setState(QuestHelper.DISABLED);
			refreshActivity(10);
		}
	}

	@Override
	public boolean newDay() {
		return true;
	}
	@Override
	public Future<List<RewardInfo>> asyncReceive(int id) {
		Promise<List<RewardInfo>> promise = Promise.promise();
		QuestModule questModule = player.getQuestModule();
		ActivityJQBConfig activityJQBConfig = ActivityJQBManager.instance().get(curId);
		Quest quest = questModule.get(activityJQBConfig.taskID);
		player.pay(PayType.ActivityJQB, id, activityJQBConfig.price).onComplete(result -> {
			if (result.result()){
				QuestConfig config = QuestManager.instance().get(quest.getId());
				promise.complete(new ArrayList<>(PlayerHelper.addReward(player,config.Reward, OpType.ActivityJQB)));
			}else {
				promise.fail(ErrorMsgEnum.unknown.getDesc());
			}
		});
		return promise.future();
	}

	@Override
	public int canReceive(List<Integer> ids) {
		int errcode =  canReceive(ids,rewardIdList,player);
		if (errcode != ErrorMsgEnum.ok.getId()){
			return errcode;
		}
		ActivityJQBConfig activityJQBConfig = ActivityJQBManager.instance().get(curId);
		if (activityJQBConfig == null){
			return  ErrorMsgEnum.activity_task_not_finish.getId();
		}
		return ErrorMsgEnum.ok.ID;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public int getCurId() {
		return curId;
	}
}
