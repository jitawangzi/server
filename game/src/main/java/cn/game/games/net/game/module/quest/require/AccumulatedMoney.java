package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.AccumulatedMoney)
public class AccumulatedMoney extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.Charge };
	public AccumulatedMoney() {

	}
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true;
	}

	@Override
	public void updateRequireCount(PlayerEvent event) {
		addCount(event.getIntParameter(0));
	}
}
