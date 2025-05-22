package cn.game.games.core.event;

import cn.game.core.event.AbstractEventRegistration;

public class PlayerEventRegistration extends AbstractEventRegistration<EventTypeEnum, PlayerEvent> {

	@Override
	protected PlayerEvent createEvent(EventTypeEnum eventType, Object... params) {
		return new PlayerEvent(eventType, params);
	}
}
