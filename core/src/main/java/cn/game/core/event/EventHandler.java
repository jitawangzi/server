package cn.game.core.event;

/**
 * 事件处理器接口：
 * 
 * 职责1：通过实现类返回支持的事件类型（注册逻辑）
 * 职责2：处理传入的事件对象（业务逻辑）
 * 
 * 2025年5月22日 13:42:21
 * @author SYQ
 * @param <T> 事件类型
 * @param <E> 事件对象
 */
public interface EventHandler<T, E extends AbstractEvent<T>> extends EventTypeProvider<T>, EventProcessor<E> 
{


}
