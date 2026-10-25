package cn.game.core.execute;

import io.vertx.core.Promise;

/**
 * 任务包装器，封装任务和结果Promise
 */
public class TaskWrapper<T>{
    private final Task<T> task;
    private final Promise<T> resultPromise;
    /** 至少开始一次执行后保持为 true，避免将重试拒绝误认为业务从未执行。 */
    private volatile boolean started;
    /** 调度或关闭在首次执行前拒绝，独立于业务结果是否快速失败。 */
    private volatile boolean rejectedBeforeStart;
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

    /** 消费者已取出任务后，在调用业务代码前记录实际开始。 */
    void markStarted() { started = true; }

    /** @return 是否至少进入过一次业务执行 */
    boolean hasStarted() { return started; }

    /** @return 是否已在首次执行前被调度或关闭拒绝 */
    boolean wasRejectedBeforeStart() { return rejectedBeforeStart; }

    /** @param cause 从待执行队列移除后的明确失败原因；调用时必须已经取得该任务 */
    void reject(Throwable cause) {
        if (!started) { rejectedBeforeStart = true; }
        fail(cause);
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

