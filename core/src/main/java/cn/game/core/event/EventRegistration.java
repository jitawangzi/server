package cn.game.core.event;

public interface EventRegistration<T, E extends AbstractEvent<T>> {

	void registerEventHandler(T eventType, EventHandler<T, E> eventHandler);

	void registerEventHandler(T[] eventTypes, EventHandler<T, E> eventHandler);

	void unregisterEventHandler(T eventType, EventHandler<T, E> eventHandler);

	void unregisterEventHandler(T[] eventType, EventHandler<T, E> eventHandler);

	void registerEventHandler(EventHandler<T, E> eventHandler);

	void unregisterEventHandler(EventHandler<T, E> eventHandler);

	void handleEvent(E event);

	void handleEvent(T eventType, Object... params);

}
