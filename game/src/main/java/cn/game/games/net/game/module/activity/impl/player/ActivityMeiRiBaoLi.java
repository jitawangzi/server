package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.event.GameEvent;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.ActivityMeiRiBaoLiConfig;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityMeiRiBaoLiManager;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@ActivityType(type = ActivityTypeEnum.ActivityMeiRiBaoLi)
public class ActivityMeiRiBaoLi extends PlayerActivityBase {
	List<Integer> rewardIdList = new ArrayList<>();

	/**
	 * 累计充值的天数
	 */
	private int totalRechargeNum;
	/**
	 * 累计充值的金额 完成一个任务则重置
	 */
	private int totalRecharge;
	private long lastRechargeTimer;
	private long finishRechargeTimer;
	@JsonIgnore
	private GameEvent oldRechargeEvent;
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};

	@Override
	public Message buildActivityShowInfo() {
		ActivityMsg.ActivityBaoLiInfoResponse_11000062.Builder res = ActivityMsg.ActivityBaoLiInfoResponse_11000062.newBuilder();
		res.setActivityId(getId());
		getConfigList().forEach(activityMeiRiBaoLiConfig -> {
			res.addTaskIds(activityMeiRiBaoLiConfig.taskID);
		});
		return res.build();
	}

	List<ActivityMeiRiBaoLiConfig> getConfigList(){
//		return ActivityMeiRiBaoLiManager.instance().list().stream().filter(activityMeiRiBaoLiConfig -> activityMeiRiBaoLiConfig.ActivityiD == getId()).collect(Collectors.toList());
		return ActivityMeiRiBaoLiManager.instance().getActivityiDList(id);
	}
	@Override
	public boolean newDay() {
		return true;
	}



	public int getTotalRechargeNum() {
		return totalRechargeNum;
	}

   public void addTotalRechargeNum(int num) {
		long now = System.currentTimeMillis();
		if (!DateUtil.isSameDay(lastRechargeTimer, now)){
			this.totalRechargeNum += num;
			setLastRechargeTimer(now);
		}
	}

	public int getTotalRecharge() {
		return totalRecharge;
	}
	public void addTotalRecharge(int recharge,GameEvent event){
		if (oldRechargeEvent != event){
			this.totalRecharge += recharge;
			this.oldRechargeEvent = event;
		}
	}

	public void setTotalRecharge(int totalRecharge) {
		this.totalRecharge = totalRecharge;
	}

	public long getLastRechargeTimer() {
		return lastRechargeTimer;
	}

	public void setLastRechargeTimer(long lastRechargeTimer) {
		this.lastRechargeTimer = lastRechargeTimer;
	}

	public long getFinishRechargeTimer() {
		return finishRechargeTimer;
	}

	public void setFinishRechargeTimer(long finishRechargeTimer) {
		this.finishRechargeTimer = finishRechargeTimer;
	}

	@Override
	public  List<RewardInfo> receive(int id) {
		QuestModule questModule = player.getQuestModule();
		rewardIdList.add(id);
		GameLogger.activity(player, super.id, id);
		List<RewardInfo> resList = questModule.receive(id);
		if (rewardIdList.size() == getConfigList().size()){
			player.getActivityModule().destroy(super.id, true);
		}
		return resList;
	}

	@Override
	public long calcEndTime() {
		if (endTime > 0){
			return endTime;
		}
		ActivityConfig activityConfig = ActivityManager.instance().get(getId());
		if (activityConfig.resetType == 4){
			int endDay = getConfigList().size() - rewardIdList.size();
			return DateUtil.nextDayStartTime(endDay);
		} else {
			return super.calcEndTime();
		}
	}

	@Override
	public int canReceive(List<Integer> ids) {
		return canReceive(ids,rewardIdList,player);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}



	@Override
	public void startUp() {
		super.startUp();
		rewardIdList.clear();
		QuestModule questModule = player.getQuestModule();
		getConfigList().forEach(activityMeiRiBaoLiConfig -> {
			questModule.remove(activityMeiRiBaoLiConfig.taskID);
			questModule.open(activityMeiRiBaoLiConfig.taskID,true);
		});
	}

	@Override
	public void shutDown() {
		// 未领取的活动 邮件发送
		QuestModule questModule = player.getQuestModule();
		getConfigList().forEach(activityMeiRiBaoLiConfig -> {
			questModule.receive(activityMeiRiBaoLiConfig.taskID);
			if (!rewardIdList.contains(activityMeiRiBaoLiConfig.taskID)){
				Quest quest = questModule.get(activityMeiRiBaoLiConfig.taskID);
				if (quest.getState() == QuestHelper.CAN_GIVEWARD){
					QuestConfig questConfig = QuestManager.instance().get(quest.getId());
					MailHelper.sendMail(player.getPlayerId(), 9, PlayerHelper.randomReward(questConfig.Reward), true);
				}
			}
			//活动结束  删除活动相关的任务
			questModule.remove(activityMeiRiBaoLiConfig.taskID);
		});
	}

	@Override
	public void destroy() {
		shutDown();
		super.destroy();
	}
}
