package cn.game.core.event;

public interface EventDispatcher<T, E extends AbstractEvent<T>> {

	void dispatch(E event);

	void dispatch(T eventType, Object... params);
}