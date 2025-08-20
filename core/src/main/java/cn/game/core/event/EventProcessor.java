package cn.game.core.event;

import java.util.Comparator;

/**    
 * 事件处理
 * 2025年5月22日 13:35:08
 * @author SYQ
 * @param <E>
 */
@FunctionalInterface
public interface EventProcessor<E> {
	@SuppressWarnings("rawtypes")
	public static final Comparator<EventProcessor> ORDER_COMPARATOR = Comparator.comparingInt(EventProcessor::processOrder);
    int EVENT_PROCESS_ORDER_HIGH = 100;
    int EVENT_PROCESS_ORDER_MEDIUM = 10_000;
    int EVENT_PROCESS_ORDER_LOW = Integer.MAX_VALUE;
    
	void handleEvent(E event);

	/** 
	 * 有时可能对同一个事件的处理逻辑，需要有先后顺序，
	 * 这个方法用来指定处理的顺序，数值越小，越先处理
	 * @return
	 */
	default int processOrder() {
		return EVENT_PROCESS_ORDER_MEDIUM;
	}

}
