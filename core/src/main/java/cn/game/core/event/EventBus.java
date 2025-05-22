package cn.game.core.event;

public interface EventBus<T, E extends AbstractEvent<T>> extends EventRegistry<T, E>, EventDispatcher<T, E> {
}