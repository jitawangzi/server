package cn.game.games.core.event;

public class EventModule implements GameEventRegistration {

	private PlayerGameEventRegistration registration = new PlayerGameEventRegistration();


	private static class PlayerGameEventRegistration extends AbstractGameEventRegistration {


	}

	@Override
	public void registerEventHandler(EventTypeEnum eventType, EventHandler eventHandler) {
		registration.registerEventHandler(eventType, eventHandler);
	}

	@Override
	public void registerEventHandler(EventTypeEnum[] eventTypes, EventHandler eventHandler) {
		registration.registerEventHandler(eventTypes, eventHandler);
	}

	@Override
	public void unregisterEventHandler(EventTypeEnum eventType, EventHandler eventHandler) {
		registration.unregisterEventHandler(eventType, eventHandler);
	}

	@Override
	public void unregisterEventHandler(EventTypeEnum[] eventType, EventHandler eventHandler) {
		registration.unregisterEventHandler(eventType, eventHandler);
	}

	@Override
	public void unregisterEventHandler(EventHandler eventHandler) {
		registration.unregisterEventHandler(eventHandler.getEventTypes(), eventHandler);
	}

	@Override
	public void registerEventHandler(EventHandler eventHandler) {
		registration.registerEventHandler(eventHandler);
	}

	@Override
	public void handleEvent(GameEvent gameEvent) {
		registration.handleEvent(gameEvent);
	}

	@Override
	public void handleEvent(EventTypeEnum eventType, Object... params) {
		registration.handleEvent(new GameEvent(eventType, params));
	}

}
