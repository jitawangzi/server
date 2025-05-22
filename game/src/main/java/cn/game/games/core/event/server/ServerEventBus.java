package cn.game.games.core.event.server;

import cn.game.core.event.AbstractEventBus;

public class ServerEventBus extends AbstractEventBus<ServerEventTypeEnum, ServerEvent> {

	public static final ServerEventBus INSTANCE = new ServerEventBus();

	private ServerEventBus() {
	}

	public static ServerEventBus getInstance() {
		return INSTANCE;
	}

	@Override
	protected ServerEvent createEvent(ServerEventTypeEnum eventType, Object... params) {
		return new ServerEvent(eventType, params);
	}

}
