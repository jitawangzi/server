package cn.game.games.core.event;

public interface GameEventRegistration {

	void registerEventHandler(EventTypeEnum eventType, EventHandler eventHandler);

	void registerEventHandler(EventTypeEnum[] eventTypes, EventHandler eventHandler);

	void unregisterEventHandler(EventTypeEnum eventType, EventHandler eventHandler);

	void unregisterEventHandler(EventTypeEnum[] eventType, EventHandler eventHandler);

	void registerEventHandler(EventHandler eventHandler);

	void unregisterEventHandler(EventHandler eventHandler);

	void handleEvent(GameEvent gameEvent);

	void handleEvent(EventTypeEnum eventType, Object... params);

}
