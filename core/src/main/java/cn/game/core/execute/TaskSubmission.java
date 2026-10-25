package cn.game.core.execute;

import java.util.function.BooleanSupplier;

import io.vertx.core.Future;

/**
 * 分开暴露提交时的受理结果与任务实际执行结果；业务快速失败不会改变受理结论。
 * 已入队仍可能在开始前被关闭或调度拒绝，调用者必须继续观察 result。
 * @param <T> 任务结果类型
 */
public final class TaskSubmission<T> {
    /** 返回提交调用时的受理快照，不等于业务完成或持久化成功。 */
    private final boolean accepted;
    /** 实际终态，不附加调用方等待超时。 */
    private final Future<T> result;
    /** 查询是否至少开始过一次业务执行，重试不能将其重置。 */
    private final BooleanSupplier started;

    /** @param accepted 提交时是否受理 @param result 实际终态 @param started 实际执行标记 */
    TaskSubmission(boolean accepted, Future<T> result, BooleanSupplier started) {
        this.accepted = accepted;
        this.result = result;
        this.started = started;
    }

    /** @return 提交返回时是否受理，不能据此自动重试业务 */
    public boolean isAccepted() { return accepted; }
    /** @return 实际结果，后续关闭或调度失败也会明确完成 */
    public Future<T> result() { return result; }
    /** @return 是否已经进入过业务执行；仅在结果失败后可据 false 认定未执行 */
    public boolean hasStarted() { return started.getAsBoolean(); }
}
