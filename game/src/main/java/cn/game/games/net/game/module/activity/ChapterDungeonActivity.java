package cn.game.games.net.game.module.activity;

import java.util.List;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

//@ActivityType(type = ActivityTypeEnum.Test1)
public class ChapterDungeonActivity {

	/*@Override
	public ActivityInfo buildActivityInfo() {
	
		ActivityInfo.Builder builder = ActivityInfo.newBuilder();
		builder.setId(id);
		int state = ActivityStateManager.getInstance().getState(id);
		builder.setState(ActivityState.forNumber(state));
		if (state == ActivityState.VIEW_VALUE) {
			int openTimeRemaining = ActivityStateManager.getInstance().getOpenTimeRemaining(id);
			builder.setStartTime(openTimeRemaining);
		}
		int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		today -= 2;
		if (today < 0) {
			today = 6;
		}
		Collection<ChapterDungeonOpenConfig> list = ChapterDungeonOpenManager.getInstance().list();
		for (ChapterDungeonOpenConfig e : list) {
			if (e.getNeedLv() > player.getData().getLevel()) {
				continue;
			}
	
			List<Integer> integer = e.getOpenChapter().get(today);
			for (Integer i : integer) {
				ActivityBaseInfo.Builder baseInfo = ActivityBaseInfo.newBuilder();
				baseInfo.setId(i);
				builder.addBaseInfo(baseInfo.build());
			}
		}
	
		return builder.build();
	}*/

	// @Override
	public List<RewardInfo> receive(int id) {

		return null;
	}

	// @Override
	public void startUp() {

	}

	// @Override
	public void shutDown() {
	}

	// @Override
	public void setEvents(EventTypeEnum[] events) {

	}

}
