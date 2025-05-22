package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.ReceiveHangup)
public class ReceiveHangup extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.Patrol };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public ReceiveHangup() {

	}
	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return !event.getBoolParameter(0);
	}
}
