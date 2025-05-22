package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.ParticipatePVP)
public class ParticipatePVP extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ParticipatePVPStart };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public ParticipatePVP() {

	}
	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true;
	}
}
