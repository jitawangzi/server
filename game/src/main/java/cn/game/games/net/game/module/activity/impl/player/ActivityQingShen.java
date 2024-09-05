package cn.game.games.net.game.module.activity.impl.player;

import java.util.List;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@ActivityType(type = ActivityTypeEnum.ActivityQingShen)
public class ActivityQingShen extends PlayerActivityBase {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};

	@Override
	public Message buildActivityShowInfo() {
		return null;
	}

	@Override
	public boolean newDay() {
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
