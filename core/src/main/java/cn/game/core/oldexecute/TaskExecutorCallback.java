package cn.game.core.oldexecute;

/**
 * 任务执行器回调接口 - 为TaskProcessor提供回调方法
 */
public interface TaskExecutorCallback {
    /**
     * 任务完成时的回调
     */
    void taskCompleted(Task<?> task);
}
