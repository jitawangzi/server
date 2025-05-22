package cn.game.core.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.game.util.SingleLinkedList;

public abstract class AbstractEventRegistration<T, E extends AbstractEvent<T>> implements EventRegistration<T, E>
{

	Map<T, SingleLinkedList<EventHandler<T, E>>> eventHandlers = new HashMap<T, SingleLinkedList<EventHandler<T, E>>>();

	@Override
	public void registerEventHandler(T eventType, EventHandler<T, E> eventHandler) {
		if (eventType == null) {
			return;
		}
		SingleLinkedList<EventHandler<T, E>> hlist = eventHandlers.get(eventType);
		if (hlist != null) {
			if (!hlist.contains(eventHandler))// 不重复添加
				hlist.addFirst(eventHandler);
		} else {
			hlist = new SingleLinkedList<EventHandler<T, E>>(EventHandler.ORDER_COMPARATOR);
			hlist.addFirst(eventHandler);
			eventHandlers.put(eventType, hlist);
		}
	}

	@Override
	public void registerEventHandler(T[] eventTypes, EventHandler<T, E> eventHandler) {
		if (eventTypes != null) {
			for (T type : eventTypes) {
				registerEventHandler(type, eventHandler);
			}
		}
	}
	@Override
	public void registerEventHandler(EventHandler<T, E> eventHandler) {
		T[] eventTypes = eventHandler.getEventTypes();
		if (eventTypes != null) {
			for (T type : eventTypes) {
				registerEventHandler(type, eventHandler);
			}
		}
	}

	@Override
	public void unregisterEventHandler(T eventType, EventHandler<T, E> eventHandler) {
		if (eventType == null) {
			return;
		}
		SingleLinkedList<EventHandler<T, E>> hlist = eventHandlers.get(eventType);
		if (hlist != null) {
			hlist.remove(eventHandler);
		}
	}

	@Override
	public void unregisterEventHandler(T[] eventType, EventHandler<T, E> eventHandler) {
		if (eventType != null) {
			for (T eventType2 : eventType) {
				unregisterEventHandler(eventType2, eventHandler);
			}
		}
	}

	@Override
	public void unregisterEventHandler(EventHandler<T, E> eventHandler) {
		T[] eventTypes = eventHandler.getEventTypes();
		if (eventTypes != null) {
			for (T eventType2 : eventTypes) {
				unregisterEventHandler(eventType2, eventHandler);
			}
		}
	}

	@Override
	public void handleEvent(E event) {
		
		SingleLinkedList<EventHandler<T, E>> hlist = eventHandlers.get(event.getType());
		if (hlist != null) {
			Iterator<EventHandler<T, E>> iterator = hlist.iterator();
			while (iterator.hasNext()) {
				EventHandler<T, E> eventHandler = iterator.next();
				eventHandler.handleEvent(event);
			}
		}
	}

	@Override
	public void handleEvent(T eventType, Object... params) {
		handleEvent(createEvent(eventType, params));
	}

	protected abstract E createEvent(T eventType, Object... params);

}
