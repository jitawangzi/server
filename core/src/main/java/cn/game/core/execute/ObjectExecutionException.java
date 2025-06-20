package cn.game.core.execute;

/**
 * 对象执行异常 - 当对象任务执行出错时抛出
 */
public class ObjectExecutionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * 创建对象执行异常
     * @param message 错误消息
     */
    public ObjectExecutionException(String message) {
        super(message);
    }
    
    /**
     * 创建对象执行异常
     * @param message 错误消息
     * @param cause 原始异常
     */
    public ObjectExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

