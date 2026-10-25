package cn.game.core.execute;

import java.util.concurrent.RejectedExecutionException;

/** 处理器确认任务从未开始后的拒绝反馈，不能通过业务 Future 的瞬时失败推断。 */
public final class TaskNotExecutedException extends RejectedExecutionException {
    /** Java 序列化版本。 */
    private static final long serialVersionUID = 1L;

    /** @param cause 原始队列满、关闭或调度拒绝原因 */
    public TaskNotExecutedException(Throwable cause) {
        super("Task rejected before execution", cause);
    }
}
