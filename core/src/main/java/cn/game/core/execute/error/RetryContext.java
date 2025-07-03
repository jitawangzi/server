package cn.game.core.execute.error;

import cn.game.core.execute.Task;

/**
 * 包含重试决策所需上下文信息
 */
public class RetryContext {
    private final Task<?> task;
    private final Throwable cause;
    private final int attemptCount;

    public RetryContext(Task<?> task, Throwable cause, int attemptCount) {
        this.task = task;
        this.cause = cause;
        this.attemptCount = attemptCount;
    }

    /**
     * @return 失败的任务
     */
    public Task<?> getTask() {
        return task;
    }

    /**
     * @return 导致失败的异常
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * @return 当前是第几次尝试 (从1开始)
     */
    public int getAttemptCount() {
        return attemptCount;
    }
}

