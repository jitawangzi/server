package cn.game.core.event;

import java.util.Comparator;

/**
 * 游戏事件处理器
 * 
 * @author SYQ
 * 
 */
public interface EventHandler<T, E extends AbstractEvent<T>> 
{
	@SuppressWarnings("rawtypes")
	public static final Comparator<EventHandler> ORDER_COMPARATOR = Comparator.comparingInt(EventHandler::eventHandlerOrder);

	/**
	 * 监听的事件类型<EventHandler>
	 * @return
	 */
	T[] getEventTypes();
	
	/**
	 * 处理事件
	 * 
	 * @param event
	 */
	public void handleEvent(E event);

	/** 
	 * 有时可能对一个事件的处理，需要有先后顺序，这个方法用来指定处理的顺序，数值越小，越先处理
	 * @return
	 */
	default int eventHandlerOrder() {
		return 10000;
	}
}
