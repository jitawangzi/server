package cn.game.core.execute;

import io.vertx.core.Promise;

/**
 * 任务包装器，封装任务和结果Promise
 */
public class TaskWrapper<T>{
    private final Task<T> task;
    private final Promise<T> resultPromise;
	/** 记录重试次数 */
	private int attempts = 0;

    public TaskWrapper(Task<T> task, Promise<T> resultPromise) {
        this.task = task;
        this.resultPromise = resultPromise;
    }

    /**
     * 获取任务
     * @return 任务
     */
    public Task<T> getTask() {
        return task;
    }

    /**
     * 获取结果Promise
     * @return 结果Promise
     */
    public Promise<T> getResultPromise() {
        return resultPromise;
    }

    /**
     * 完成Promise，设置结果
     * @param result 结果
     */
    public void complete(T result) {
        resultPromise.tryComplete(result);
    }

    /**
     * 完成Promise，设置异常
     * @param cause 异常
     */
    public void fail(Throwable cause) {
        resultPromise.tryFail(cause);
    }

    /**
     * 获取尝试次数
     * @return 尝试次数
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * 增加尝试次数
     */
    public void incrementAttempts() {
        this.attempts++;
    }
}

