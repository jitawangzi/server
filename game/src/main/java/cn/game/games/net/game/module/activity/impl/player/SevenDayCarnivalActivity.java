package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.module.activity.ActivityHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.SevenDaysCarnivalConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.SevenDaysCarnivalManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysCarnivalResponse_11000021;
import cn.game.protocol.protobuf.ActivityMsg.SevenDaysQuest;
import cn.game.protocol.protobuf.ActivityMsg.SevenDaysQuest.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

@ActivityType(type = ActivityTypeEnum.SevenDaysCarnival)
public class SevenDayCarnivalActivity extends PlayerActivityBase {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};
	/** 已经初始过任务的天  1 - 7 */
	private List<Integer> initDays = new ArrayList<>();
	/** 可以看到多少天的任务 */
	private List<Integer> showDays = new ArrayList<>();

	@Override
	public Message buildActivityShowInfo() {
		ActivitySevenDaysCarnivalResponse_11000021.Builder builder = ActivitySevenDaysCarnivalResponse_11000021.newBuilder();
		QuestModule questModule = player.getQuestModule();
		questModule.get(id);
		for (Integer day : showDays) {
			Builder newBuilder = SevenDaysQuest.newBuilder(); 
			newBuilder.setDay(day);
			SevenDaysCarnivalConfig config = SevenDaysCarnivalManager.instance().getUITypeDay(ActivityHelper.SEVENDAYS_CARNIVAL, day);
			for (int questId : config.TaskID) {
				Quest quest = questModule.get(questId);
				if (quest == null) {
					// SevenDaysCarnival新加任务，老号可能没有，直接跳过
//					log.warn("ActivitySevenDays quest {} is null", questId);
//					log.info("SevenDaysCarniva quests : " + questModule.getGroup(QuestTypeEnum.SevenDaysCarniva));
					continue;
				}
				newBuilder.addQuests(quest.toQuestInfo());
			}
			builder.addSevenDay(newBuilder.build());
		}
		return builder.build();
	}

	@Override
	public void startUp() {
		super.startUp();
		for (int i = 1; i <= 7; i++) {
			openDay(i);
		}
		showDays.add(1);
	}

	@Override
	public void destroy() {
		// 移除七日任务
		QuestModule questModule = player.getQuestModule();
		for (Integer day : initDays) {
			SevenDaysCarnivalConfig config = SevenDaysCarnivalManager.instance().getUITypeDay(ActivityHelper.SEVENDAYS_CARNIVAL, day);
			if (config == null) {
				return;
			}
			questModule.close(config.TaskID, false);
		}
	};

	private void openDay(int day) {
		SevenDaysCarnivalConfig config = SevenDaysCarnivalManager.instance().getUITypeDay(ActivityHelper.SEVENDAYS_CARNIVAL, day);
		if (config == null) {
			return;
		}
		// 开启任务
		QuestModule questModule = player.getQuestModule();
		questModule.open(config.TaskID, true);
		initDays.add(day);
	}

	@Override
	public boolean newDay() {
		int days = DateUtil.diffDays(startTime) + 1;
		for (int i = 1; i <= days; i++) {
			if (showDays.contains(i)) {
				continue;
			}
			showDays.add(i);
//			openDay(i);
		}
		return true;
	}
	@Override
	public List<RewardInfo> receive(int id) {
		return null;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

}
