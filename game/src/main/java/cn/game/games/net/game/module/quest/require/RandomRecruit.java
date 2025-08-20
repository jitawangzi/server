package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.RandomRecruit)
public class RandomRecruit extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.HeroRecruit };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public RandomRecruit() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true;
	}
}
