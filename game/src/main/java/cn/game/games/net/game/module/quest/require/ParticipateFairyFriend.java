package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.ParticipateFairyFriend)
public class ParticipateFairyFriend extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.FairyFriendsTravel };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public ParticipateFairyFriend() {

	}
	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true;
	}
}
