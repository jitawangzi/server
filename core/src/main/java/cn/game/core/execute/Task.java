package cn.game.core.execute;

/**
 * 表示可以在ActorMailbox中执行的任务
 */
public interface Task<T> extends Comparable<Task<?>> {
    /**
     * 任务执行方法
     * @return 任务执行结果
     * @throws Exception 如果任务执行过程中发生错误
     */
    T execute() throws Exception;
    
    /**
     * 获取任务优先级
     * @return 优先级值，值越大优先级越高
     */
    int getPriority();
    
    /**
     * 获取任务超时时间（毫秒）
     * @return 超时时间，0表示不设置超时
     */
    long getTimeoutMs();
    
    /**
     * 获取任务描述信息，用于日志和调试
     * @return 描述信息
     */
    String getDescription();
}

