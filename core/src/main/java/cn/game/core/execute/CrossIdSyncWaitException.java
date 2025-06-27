package cn.game.core.execute;

/**
 * 跨id任务有依赖时，不能同步等待
 */
public class CrossIdSyncWaitException extends TaskExecutionException {
    
	private static final long serialVersionUID = 5062610007584390992L;

	public CrossIdSyncWaitException(long entityId, String taskDescription) {
		super("Task execution timed out after ", null, entityId, taskDescription);
    }
}

