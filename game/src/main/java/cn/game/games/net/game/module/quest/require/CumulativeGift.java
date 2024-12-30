package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.CumulativeGift)
public class CumulativeGift extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.FairyFriendsGift };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public CumulativeGift() {

	}
	@Override
	public void updateRequireCount(GameEvent event) {
		finishCount += event.getIntParameter(0);
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}
}
