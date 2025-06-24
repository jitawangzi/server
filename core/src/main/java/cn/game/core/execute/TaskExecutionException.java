package cn.game.core.execute;

/**
 * 任务执行异常，封装任务执行过程中发生的异常
 */
public class TaskExecutionException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final long entityId;
    private final String taskDescription;
    
    public TaskExecutionException(String message, Throwable cause, long entityId, String taskDescription) {
        super(message, cause);
        this.entityId = entityId;
        this.taskDescription = taskDescription;
    }
    
    public long getEntityId() {
        return entityId;
    }
    
    public String getTaskDescription() {
        return taskDescription;
    }
}

