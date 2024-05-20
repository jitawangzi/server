package cn.game.games.net.game.module.activity.impl.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.FirstChargeConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.FirstChargeManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityFirstChargeResponse_11000008;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

@ActivityType(type = ActivityTypeEnum.SevenDaysCarnival)
public class SevenDayHappyActivity extends PlayerActivityBase {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};
	/** 活动哪天开启的 */
	private int openDay;
	/** 已经初始过任务的天 */
	private List<Integer> initDays = new ArrayList<>();

	@Override
	public Message buildActivityShowInfo() {
		ActivityFirstChargeResponse_11000008.Builder resp = ActivityFirstChargeResponse_11000008.newBuilder();
		int nowDay = DateUtil.getDay();
		Collection<FirstChargeConfig> list = FirstChargeManager.instance().list();
		return resp.build();
	}

	@Override
	public void startUp() {
		super.startUp();

		this.openDay = DateUtil.getDay();
		// 开启任务
		QuestModule questModule = player.getQuestModule();

	}

	@Override
	public boolean newDay() {
		return super.newDay();
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
