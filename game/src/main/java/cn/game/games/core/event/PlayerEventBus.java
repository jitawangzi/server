package cn.game.games.core.event;

import cn.game.core.event.AbstractEventBus;

public class PlayerEventBus extends AbstractEventBus<EventTypeEnum, PlayerEvent> {

	@Override
	protected PlayerEvent createEvent(EventTypeEnum eventType, Object... params) {
		return new PlayerEvent(eventType, params);
	}
}
