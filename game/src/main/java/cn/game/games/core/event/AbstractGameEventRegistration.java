package cn.game.games.core.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.game.util.SingleLinkedList;

public abstract class AbstractGameEventRegistration implements GameEventRegistration {

	Map<EventTypeEnum, SingleLinkedList<EventHandler>> eventHandlers = new HashMap<EventTypeEnum, SingleLinkedList<EventHandler>>();

	@Override
	public void registerEventHandler(EventTypeEnum eventType, EventHandler eventHandler) {
		if (eventType == null) {
			return;
		}
		SingleLinkedList<EventHandler> hlist = eventHandlers.get(eventType);
		if (hlist != null) {
			if (!hlist.contains(eventHandler))// 不重复添加
				hlist.addFirst(eventHandler);
		} else {
			hlist = new SingleLinkedList<EventHandler>();
			hlist.addFirst(eventHandler);
			eventHandlers.put(eventType, hlist);
		}
	}

	@Override
	public void registerEventHandler(EventTypeEnum[] eventTypes, EventHandler eventHandler) {
		if (eventTypes != null) {
			for (EventTypeEnum type : eventTypes) {
				registerEventHandler(type, eventHandler);
			}
		}
	}
	@Override
	public void registerEventHandler(EventHandler eventHandler) {
		EventTypeEnum[] eventTypes = eventHandler.getEventTypes();
		if (eventTypes != null) {
			for (EventTypeEnum type : eventTypes) {
				registerEventHandler(type, eventHandler);
			}
		}
	}

	@Override
	public void unregisterEventHandler(EventTypeEnum eventType, EventHandler eventHandler) {
		if (eventType == null) {
			return;
		}
		SingleLinkedList<EventHandler> hlist = eventHandlers.get(eventType);
		if (hlist != null) {
			hlist.remove(eventHandler);
		}
	}

	@Override
	public void unregisterEventHandler(EventTypeEnum[] eventType, EventHandler eventHandler) {
		if (eventType != null) {
			for (EventTypeEnum eventType2 : eventType) {
				unregisterEventHandler(eventType2, eventHandler);
			}
		}
	}

	@Override
	public void unregisterEventHandler(EventHandler eventHandler) {
		EventTypeEnum[] eventTypes = eventHandler.getEventTypes();
		if (eventTypes != null) {
			for (EventTypeEnum eventType2 : eventTypes) {
				unregisterEventHandler(eventType2, eventHandler);
			}
		}
	}

	@Override
	public void handleEvent(GameEvent gameEvent) {
		SingleLinkedList<EventHandler> hlist = eventHandlers.get(gameEvent.getType());
		if (hlist != null) {
			Iterator<EventHandler> iterator = hlist.iterator();
			while (iterator.hasNext()) {
				EventHandler eventHandler = iterator.next();
				eventHandler.handleEvent(gameEvent);
			}
		}
	}

	@Override
	public void handleEvent(EventTypeEnum eventType, Object... params) {
		handleEvent(new GameEvent(eventType, params));
	}

}
