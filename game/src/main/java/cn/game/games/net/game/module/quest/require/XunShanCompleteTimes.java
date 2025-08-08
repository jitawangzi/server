package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.XunShanCompleteTimes)
public class XunShanCompleteTimes extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.XunShanComplete };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public XunShanCompleteTimes() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true;
	}
}
