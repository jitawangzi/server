package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.LogInGame)
public class LogGame2 extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.Login, EventTypeEnum.Reconnect };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public LogGame2() {

	}
	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}

}
