package cn.game.core.event;

/**    
 * 事件类型注册
 * 2025年5月22日 13:35:31
 * @author SYQ
 * @param <T>
 */
@FunctionalInterface
public interface EventTypeProvider<T> {
	/** 
	 * 监听的事件类型
	 * @return
	 */
	T[] getEventTypes();
}