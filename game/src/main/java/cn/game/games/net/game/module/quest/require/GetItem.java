package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.GetItem)
public class GetItem extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.GetItem };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public GetItem() {

	}

	@Override
	public void updateRequireCount(PlayerEvent event) {
		int count = event.getIntParameter(1);
		finishCount += count;
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int id = event.getIntParameter(0);
		if (id == getRequireId()) {
			return true;
		}
		return false;
	}
}
