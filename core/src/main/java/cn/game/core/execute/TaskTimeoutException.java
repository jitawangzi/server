package cn.game.core.execute;

/**
 * 任务超时异常，在任务执行超时时抛出
 */
public class TaskTimeoutException extends TaskExecutionException {
	private static final long serialVersionUID = 1L;
	private final long timeoutMs;
    
    public TaskTimeoutException(long entityId, String taskDescription, long timeoutMs) {
        super("Task execution timed out after " + timeoutMs + "ms", null, entityId, taskDescription);
        this.timeoutMs = timeoutMs;
    }
    
    public long getTimeoutMs() {
        return timeoutMs;
    }
}

