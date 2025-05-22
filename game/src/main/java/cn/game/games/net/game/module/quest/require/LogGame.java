package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.LogGame)
public class LogGame extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LoginFinish, EventTypeEnum.NewDay,
			EventTypeEnum.Reconnect };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public LogGame() {

	}
//	@Override
//	public boolean isAchieve() {
//		setFinishCount(1);
//		return super.isAchieve();
//	}
	@Override
	public long getFinishCount() {
		return 1;
	}

	@Override
	public boolean isAchieve() {
		return true;
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true;
	}

}
