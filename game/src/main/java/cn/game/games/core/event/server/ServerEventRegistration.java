package cn.game.games.core.event.server;

import cn.game.core.event.AbstractEventRegistration;

public class ServerEventRegistration extends AbstractEventRegistration<ServerEventTypeEnum, ServerEvent> {

	public static final ServerEventRegistration INSTANCE = new ServerEventRegistration();

	private ServerEventRegistration() {
	}

	public static ServerEventRegistration getInstance() {
		return INSTANCE;
	}

	@Override
	protected ServerEvent createEvent(ServerEventTypeEnum eventType, Object... params) {
		return new ServerEvent(eventType, params);
	}

}
