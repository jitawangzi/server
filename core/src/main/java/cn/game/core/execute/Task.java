package cn.game.core.execute;

import cn.game.core.execute.error.ErrorHandler;

/**
 * 表示可以在ActorMailbox中执行的任务
 */
public interface Task<T> {
    /**
     * 任务执行方法
     * @return 任务执行结果
     * @throws Exception 如果任务执行过程中发生错误
     */
    T execute() throws Exception;

    /**
     * 任务是否需要快速执行，也就是优先级较高的任务
     * @return 
     */
    boolean isFast();

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

    /**
     * 获取此任务的错误处理器
     * @return 错误处理器
     */
    ErrorHandler getErrorHandler();
}

