package cn.game.core.event;

import java.util.HashMap;
import java.util.Map;

import cn.game.util.SingleLinkedList;

public abstract class AbstractEventBus<T, E extends AbstractEvent<T>> implements EventBus<T, E> {
	private final Map<T, SingleLinkedList<EventProcessor<E>>> processors = new HashMap<>();

	@Override
	public void register(T eventType, EventProcessor<E> processor) {
		if (eventType == null || processor == null) {
			return;
		}
		SingleLinkedList<EventProcessor<E>> list = processors.computeIfAbsent(eventType,
				k -> new SingleLinkedList<>(EventProcessor.ORDER_COMPARATOR));
		if (!list.contains(processor)) {
			list.addSorted(processor);
		}
	}

	@Override
	public void register(T[] eventTypes, EventProcessor<E> processor) {
		if (eventTypes == null || processor == null) {
			return;
		}
		for (T type : eventTypes) {
			register(type, processor);
		}
	}

	@Override
	public void register(EventTypeProvider<T> typeProvider, EventProcessor<E> processor) {
		if (typeProvider == null || processor == null) {
			return;
		}

		register(typeProvider.getEventTypes(), processor);
	}

	@Override
	public void register(EventHandler<T, E> handler) {
		if (handler == null) {
			return;
		}
		register(handler.getEventTypes(), handler);
	}

	// 注销方法
	@Override
	public void unregister(T eventType, EventProcessor<E> processor) {
		if (eventType == null || processor == null) {
			return;
		}
		SingleLinkedList<EventProcessor<E>> list = processors.get(eventType);
		if (list != null) {
			list.remove(processor);
		}
	}

	@Override
	public void unregister(T[] eventTypes, EventProcessor<E> processor) {
		if (eventTypes == null || processor == null) {
			return;
		}
		for (T type : eventTypes) {
			unregister(type, processor);
		}
	}

	@Override
	public void unregister(EventTypeProvider<T> typeProvider, EventProcessor<E> processor) {
		if (typeProvider == null || processor == null) {
			return;
		}
		unregister(typeProvider.getEventTypes(), processor);
	}

	@Override
	public void unregister(EventHandler<T, E> handler) {
		if (handler == null) {
			return;
		}
		unregister(handler.getEventTypes(), handler);
	}

	@Override
	public void dispatch(E event) {
		if (event == null) {
			return;
		}

		SingleLinkedList<EventProcessor<E>> list = processors.get(event.getType());
		if (list != null) {
			for (EventProcessor<E> processor : list) {
				processor.handleEvent(event);
			}
		}
	}

	@Override
	public void dispatch(T eventType, Object... params) {
		dispatch(createEvent(eventType, params));
	}

	protected abstract E createEvent(T eventType, Object... params);
}