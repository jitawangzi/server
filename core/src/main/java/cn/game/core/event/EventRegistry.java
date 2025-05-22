package cn.game.core.event;

/**    
 * 事件注册、取消注册
 * 2025年5月22日 14:31:10
 * @author SYQ
 * @param <T>
 * @param <E>
 */
public interface EventRegistry<T, E extends AbstractEvent<T>> {
	/** 
	 * 注册单个事件类型的处理器
	 * @param eventType	事件类型
	 * @param processor 事件的处理逻辑
	 */
	void register(T eventType, EventProcessor<E> processor);

	/** 
	 * 注册多个事件类型的处理器
	 * @param eventTypes 事件类型
	 * @param processor 事件的处理逻辑
	 */
	void register(T[] eventTypes, EventProcessor<E> processor);

	/** 
	 * 通过类型提供者注册
	 * @param typeProvider
	 * @param processor
	 */
	void register(EventTypeProvider<T> typeProvider, EventProcessor<E> processor);

	/** 
	 * 注册完整的事件处理器
	 * @param handler
	 */
	void register(EventHandler<T, E> handler);

	// 注销方法
	void unregister(T eventType, EventProcessor<E> processor);

	void unregister(T[] eventTypes, EventProcessor<E> processor);

	void unregister(EventTypeProvider<T> typeProvider, EventProcessor<E> processor);

	void unregister(EventHandler<T, E> handler);
}
