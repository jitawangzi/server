package cn.game.games.net.game.module.activity.impl.player;

import java.util.List;

import com.google.protobuf.Message;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.protocol.generated.config.SevenDaysSigninConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.SevenDaysSigninManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg.ActivitySevenDaysSigninInfoResponse_11000025;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@ActivityType(type = ActivityTypeEnum.SevenDaysSignin)
public class SevenDaysSignin extends PlayerActivityBase {
	private static transient EventTypeEnum[] events = new EventTypeEnum[] {};
	/** 今天登陆是否签到了 */
	private boolean isSignin;
	/** 已经签到的天数   1 - 7 */
	private int day;

	@Override
	public Message buildActivityShowInfo() {
		ActivitySevenDaysSigninInfoResponse_11000025.Builder builder = ActivitySevenDaysSigninInfoResponse_11000025.newBuilder();
		builder.setDay(day);
		builder.setCanSignin(!isSignin);
		return builder.build();
	}

	@Override
	public boolean newDay() {
		isSignin = false;
		return true;
	}
	@Override
	public List<RewardInfo> receive(int id) {
		SevenDaysSigninConfig config = SevenDaysSigninManager.instance().getNullable(day + 1);
		List<RewardInfo> resources = PlayerHelper.addResources(player, config.Item, OpType.SevenDaysSignin);
		day++;
		isSignin = true;
		GameLogger.activity(player, super.id, day);
		return resources;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public boolean isSignin() {
		return isSignin;
	}

}
