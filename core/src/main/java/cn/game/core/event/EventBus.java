package cn.game.core.event;

/**    
 * 自定义的事件总线
 * 对事件的处理逻辑支持排序
 * 使用枚举来表示不同的事件类型
 * 2025年5月22日 18:15:56
 * @author SYQ
 * @param <T>
 * @param <E>
 */
public interface EventBus<T, E extends AbstractEvent<T>> extends EventRegistry<T, E>, EventDispatcher<T, E> {
}