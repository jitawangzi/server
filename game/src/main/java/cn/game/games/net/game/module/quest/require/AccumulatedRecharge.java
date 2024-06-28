package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.AccumulatedRecharge)
public class AccumulatedRecharge extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.Charge };
	public AccumulatedRecharge() {

	}
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}
}
