package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;

import cn.game.core.base.ServerContext;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.MailHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.ActivityQingShenConfig;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityQingShenManager;
import cn.game.protocol.generated.manager.QuestManager;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import io.vertx.core.Future;
import io.vertx.core.Promise;

@ActivityType(type = ActivityTypeEnum.ActivityQingShen)
public class ActivityQingShen extends PlayerActivityBase {
	int round;
	List<Integer> rewardIdList = new ArrayList<>();

	private static transient EventTypeEnum[] events = new EventTypeEnum[] {EventTypeEnum.QuestFinish};

	@Override
	public Message buildActivityShowInfo() {
		ActivityMsg.ActivityQingShenInfoResponse_11000072.Builder res = ActivityMsg.ActivityQingShenInfoResponse_11000072.newBuilder()
				.setActivityId(getId())
				.setRound(round);
		List<ActivityQingShenConfig> configList = getRoundConfigList();
		if (!configList.isEmpty()){
			res.setRoundTaskId(configList.get(Math.max(0,configList.size() - 1)).taskID);
		}
		for(int i = 0; i < configList.size() - 1; i++) {
			res.addTaskIds(configList.get(i).taskID);
		}
		return res.build();
	}

	List<ActivityQingShenConfig> getConfigList() {
//		return ActivityQingShenManager.instance().list().stream().filter(activityQingShenConfig -> activityQingShenConfig.ActivityiD == getId()).collect(Collectors.toList());
		return ActivityQingShenManager.instance().getActivityiDList(id);
	}
	List<ActivityQingShenConfig> getRoundConfigList(){
		return getConfigList().stream().filter(activityQingShenConfig -> activityQingShenConfig.Round == round).toList();
	}
	int getMaxRound(){
		int maxRound = 0;
		for (ActivityQingShenConfig config : getConfigList()) {
		    if (config.Round > maxRound){
				maxRound = config.Round;
			}
		}
		return maxRound;
	}

	@Override
	public boolean newDay() {
		return true;
	}
	@Override
	public  List<RewardInfo> receive(int id) {
		QuestModule questModule = player.getQuestModule();
		rewardIdList.add(id);
		return questModule.receive(id);
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
		round = 0;
		refreshActivity();
	}

	private void refreshActivity() {
		round++;
		int maxRound = getMaxRound();
		if (round > maxRound){
			round = maxRound;
			return;
		}
		List<ActivityQingShenConfig> newTaskIdList = getConfigList().stream().filter(activityQingShenConfig -> activityQingShenConfig.Round == round).toList();
		QuestModule questModule = player.getQuestModule();
		newTaskIdList.forEach(activityQingShenConfig -> {
			questModule.remove(activityQingShenConfig.taskID);
			questModule.open(activityQingShenConfig.taskID,true);
			if (!ServerContext.getInstance().getRunMode().isProduction()){
				log.info(String.format("create new taskId:%d, activityId:%d, round:%d  pid:%d,",activityQingShenConfig.taskID,id,round,player.getPlayerId()));
			}
		});
	}

	@Override
	public void handleEvent(GameEvent event) {
		if (event.getType() == EventTypeEnum.QuestFinish){
			int taskId = event.getIntParameter(0);
			List<ActivityQingShenConfig> roundConfigList = getRoundConfigList();
			if (!roundConfigList.isEmpty()) {
				int roundTaskId = roundConfigList.get(Math.max(0, roundConfigList.size() - 1)).taskID;
				if (taskId == roundTaskId) {
					log.info(String.format("open next round:%d, pid:%d, activityId:%d", round, player.getPlayerId(), id));
					refreshActivity();
				}
			}
		}
	}

	@Override
	public void shutDown() {
		QuestModule questModule = player.getQuestModule();
		getConfigList().forEach(activityQingShenConfig -> {
			if (!rewardIdList.contains(activityQingShenConfig.taskID)){
				Quest quest = questModule.get(activityQingShenConfig.taskID);
				if (quest.getState() == QuestHelper.CAN_GIVEWARD){
					QuestConfig questConfig = QuestManager.instance().get(quest.getId());
					MailHelper.sendMail(player.getPlayerId(), 10, PlayerHelper.randomReward(questConfig.Reward), true);
				}
			}
			//活动结束  删除活动相关的任务
			questModule.remove(activityQingShenConfig.taskID);
		});
	}
}
