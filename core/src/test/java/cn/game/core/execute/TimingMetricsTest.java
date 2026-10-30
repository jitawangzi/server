package cn.game.core.execute;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import cn.game.core.execute.error.ErrorHandler;
import cn.game.util.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/** 单调时钟、并发结束及真实邮箱时序验证，不以宿主延迟阈值判断性能通过。 */
class TimingMetricsTest {
    /** 恢复关闭状态，避免跨用例污染。 */
    @AfterEach
    void reset() { TimingMetrics.reset(false); }

    /** 分位数是桶上界，10ms超限用原始纳秒严格比较。 */
    @Test
    void monotonicHistogramHasExactThresholdAndBoundedBuckets() {
        AtomicLong clock = new AtomicLong();
        TimingMetrics metrics = new TimingMetrics(true, clock::get);
        for (long elapsed : List.of(0L, 1000L, 10_000_000L, 10_000_001L)) {
            var span = metrics.begin(TimingMetrics.Stage.SQL, TimingMetrics.Route.ALL);
            clock.addAndGet(elapsed); span.end(TimingMetrics.Outcome.SUCCESS);
            span.end(TimingMetrics.Outcome.FAILURE);
        }
        var row = metrics.snapshot().rows().getFirst();
        Assertions.assertEquals(4, row.count());
        Assertions.assertEquals(1, row.over10ms());
        Assertions.assertEquals(1, row.p50UpperMicros());
        Assertions.assertEquals(16384, row.p95UpperMicros());
        Assertions.assertEquals(16384, row.p99UpperMicros());
        Assertions.assertEquals(10_000_001L, row.maxNanos());
    }
    /** 同一计时被多个完成分支竞争也只记一次；新窗口不接收旧计时。 */
    @Test
    void concurrentEndAndResetDoNotDuplicateOrMoveSamples() throws Exception {
        TimingMetrics old = TimingMetrics.reset(true);
        var span = old.begin(TimingMetrics.Stage.EXECUTION, TimingMetrics.Route.ALL);
        TimingMetrics next = TimingMetrics.reset(true);
        try (var threads = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 64; i++) { threads.submit(() -> span.end(TimingMetrics.Outcome.SUCCESS)); }
        }
        Assertions.assertEquals(1, count(old, TimingMetrics.Stage.EXECUTION, TimingMetrics.Outcome.SUCCESS));
        Assertions.assertTrue(next.snapshot().rows().isEmpty());
    }
    /** complete/fail竞争时按原Promise实际赢家记录，不让指标CAS选择另一个结果。 */
    @Test
    void concurrentPromiseCompletionRecordsOnlyActualWinningOutcome() throws Exception {
        TimingMetrics metrics = TimingMetrics.reset(true);
        Promise<Integer> promise = Promise.promise();
        TaskWrapper<Integer> wrapper = new TaskWrapper<>(DefaultTask.<Integer>builder().action(() -> 7).build(), promise);
        wrapper.markStarted();
        try (var threads = Executors.newVirtualThreadPerTaskExecutor()) {
            List<java.util.concurrent.Future<?>> jobs = new java.util.ArrayList<>();
            for (int i = 0; i < 64; i++) {
                final boolean success = i % 2 == 0;
                jobs.add(threads.submit(() -> { if (success) { wrapper.complete(7); } else { wrapper.fail(new IllegalStateException("synthetic")); } }));
            }
            for (var job : jobs) { job.get(5, TimeUnit.SECONDS); }
        }
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.TASK_ACTUAL,
                promise.future().succeeded() ? TimingMetrics.Outcome.SUCCESS : TimingMetrics.Outcome.FAILURE));
        Assertions.assertEquals(0, count(metrics, TimingMetrics.Stage.TASK_ACTUAL,
                promise.future().succeeded() ? TimingMetrics.Outcome.FAILURE : TimingMetrics.Outcome.SUCCESS));
    }
    /** 快照竞争可丢观测，但不能影响调用结果，计数与丢样合计保持真实。 */
    @Test
    void concurrentSnapshotsAccountForEveryAttemptWithoutBlockingProducers() throws Exception {
        TimingMetrics metrics = TimingMetrics.reset(true);
        try (var threads = Executors.newVirtualThreadPerTaskExecutor()) {
            var reader = threads.submit(() -> { for (int i = 0; i < 500; i++) { metrics.snapshot(); } });
            for (int i = 0; i < 8; i++) {
                threads.submit(() -> { for (int j = 0; j < 1000; j++) { metrics.begin(TimingMetrics.Stage.SQL, TimingMetrics.Route.ALL).end(TimingMetrics.Outcome.SUCCESS); } });
            }
            reader.get(10, TimeUnit.SECONDS);
        }
        Assertions.assertEquals(8000, count(metrics, TimingMetrics.Stage.SQL, TimingMetrics.Outcome.SUCCESS) + metrics.snapshot().dropped());
    }
    /** 观测时钟故障不逃逸、不调用外部监听器，关闭时也不读取时钟。 */
    @Test
    void telemetryFailureAndDisabledModeCannotChangeBusinessResult() {
        TimingMetrics broken = new TimingMetrics(true, () -> { throw new AssertionError("synthetic clock"); });
        Assertions.assertDoesNotThrow(() -> broken.begin(TimingMetrics.Stage.SQL, TimingMetrics.Route.ALL).end(TimingMetrics.Outcome.FAILURE));
        Assertions.assertEquals(1, broken.snapshot().failures());
        TimingMetrics disabled = new TimingMetrics(false, () -> { throw new AssertionError("disabled clock called"); });
        disabled.begin(TimingMetrics.Stage.SQL, TimingMetrics.Route.ALL).end(TimingMetrics.Outcome.SUCCESS);
        Assertions.assertTrue(disabled.snapshot().rows().isEmpty());
        Assertions.assertEquals(0, disabled.snapshot().failures());
    }
    /** 超时不结束真实任务；等待与实际回调无论何种先后都只记一个迟到完成。 */
    @Test
    void timeoutAndLateActualCompletionRemainSeparateForBothCallbackOrders() {
        for (boolean reverse : List.of(false, true)) {
            AtomicLong clock = new AtomicLong(1);
            TimingMetrics metrics = new TimingMetrics(true, clock::get);
            Promise<Integer> actual = Promise.promise(), waited = Promise.promise();
            var span = metrics.begin(TimingMetrics.Stage.TASK_WAIT, TimingMetrics.Route.ALL);
            span.observe(actual.future(), waited.future());
            if (reverse) { clock.set(100); actual.complete(7); clock.set(200); waited.fail(new TimeoutException()); }
            else { clock.set(100); waited.fail(new TimeoutException()); Assertions.assertFalse(actual.future().isComplete()); clock.set(200); actual.complete(7); }
            Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.TASK_WAIT, TimingMetrics.Outcome.TIMEOUT));
            Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.TASK_WAIT, TimingMetrics.Outcome.LATE_SUCCESS));
            var row = metrics.snapshot().rows().stream().filter(r -> r.outcome() == TimingMetrics.Outcome.LATE_SUCCESS).findFirst().orElseThrow();
            Assertions.assertEquals(reverse ? 99 : 199, row.maxNanos());
        }
    }
    /** 失败的迟到真实结果也不得改成成功，RPC传输超时保留独立类型。 */
    @Test
    void lateFailureAndRpcTimeoutAreClassifiedWithoutChangingFutures() {
        TimingMetrics metrics = TimingMetrics.reset(true);
        Promise<Integer> actual = Promise.promise(), waited = Promise.promise();
        TimingMetrics.start(TimingMetrics.Stage.DB_WAIT).observe(actual.future(), waited.future());
        var timeout = new io.vertx.core.eventbus.ReplyException(io.vertx.core.eventbus.ReplyFailure.TIMEOUT, "synthetic");
        waited.fail(timeout); actual.fail(new IllegalStateException("actual"));
        Assertions.assertSame(timeout, waited.future().cause());
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.DB_WAIT, TimingMetrics.Outcome.TIMEOUT));
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.DB_WAIT, TimingMetrics.Outcome.LATE_FAILURE));
    }
    /** 实际业务自身的TimeoutException和共用Future都不证明调用者先超时。 */
    @Test
    void actualTimeoutFailureNeverInventsLateCompletion() {
        TimingMetrics metrics = TimingMetrics.reset(true);
        Promise<Integer> actual = Promise.promise(), waited = Promise.promise();
        var timeout = new TimeoutException("actual task failure");
        TimingMetrics.start(TimingMetrics.Stage.TASK_WAIT).observe(actual.future(), waited.future());
        actual.fail(timeout); waited.fail(timeout);
        TimingMetrics.start(TimingMetrics.Stage.RPC_WAIT).observe(actual.future(), actual.future());
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.TASK_WAIT, TimingMetrics.Outcome.TIMEOUT));
        Assertions.assertEquals(0, count(metrics, TimingMetrics.Stage.TASK_WAIT, TimingMetrics.Outcome.LATE_FAILURE));
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.RPC_WAIT, TimingMetrics.Outcome.TIMEOUT));
        Assertions.assertEquals(0, count(metrics, TimingMetrics.Stage.RPC_WAIT, TimingMetrics.Outcome.LATE_FAILURE));
    }
    /** 同线程复用、嵌套请求、跨连接与错误序号不能串到另一个请求。 */
    @Test
    void requestScopeCleansUpAndIsolatesConnectionsAndNestedDispatch() {
        TimingMetrics metrics = TimingMetrics.reset(true);
        Object first = new Object(), second = new Object();
        RequestTiming outer = new RequestTiming(0x01000005), inner = new RequestTiming(123);
        outer.run(first, 3, () -> {
            Assertions.assertNull(RequestTiming.response(second, 3, 0x01000006));
            Assertions.assertNull(RequestTiming.response(first, 4, 0x01000006));
            inner.run(second, 4, () -> { RequestTiming.response(second, 4, 124).written(Future.succeededFuture(), 9); });
            RequestTiming.response(first, 3, 0x01000006).written(Future.succeededFuture(), 0);
            Assertions.assertNull(RequestTiming.response(first, 3, 0x01000006));
        });
        Assertions.assertNull(RequestTiming.response(first, 3, 0x01000006));
        outer.finished(false); inner.finished(false);
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.REQUEST, TimingMetrics.Outcome.SUCCESS));
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.REQUEST, TimingMetrics.Outcome.FAILURE));
        Assertions.assertThrows(IllegalStateException.class, () -> new RequestTiming(1).run(first, 5, () -> { throw new IllegalStateException(); }));
        Assertions.assertNull(RequestTiming.response(first, 5, 2));
    }
    /** 异步写入完成后才计结果；没有响应与未执行拒绝各自独立。 */
    @Test
    void requestWaitsForWriteAndReportsMissingResponseAndRejection() {
        TimingMetrics metrics = TimingMetrics.reset(true);
        Object client = new Object(); Promise<Void> write = Promise.promise();
        RequestTiming timing = new RequestTiming(123);
        timing.run(client, 1, () -> RequestTiming.response(client, 1, 124).written(write.future(), 0));
        timing.finished(false);
        Assertions.assertTrue(metrics.snapshot().rows().isEmpty());
        write.fail("write failed");
        new RequestTiming(1).finished(false); new RequestTiming(1).finished(true);
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.REQUEST, TimingMetrics.Outcome.FAILURE));
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.REQUEST, TimingMetrics.Outcome.NO_RESPONSE));
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.REQUEST, TimingMetrics.Outcome.REJECTED));
    }
    /** 长队列、业务异常及关闭拒绝通过正式服务执行，观测不放行第二消费者。 */
    @Test
    void realMailboxLongQueueFailureAndCloseKeepSerialization() throws Exception {
        TimingMetrics metrics = TimingMetrics.reset(true);
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicInteger active = new AtomicInteger(), max = new AtomicInteger();
        var worker = Executors.newVirtualThreadPerTaskExecutor();
        TaskExecutorService service = new TaskExecutorService(TaskExecutionConfig.builder().defaultTaskTimeoutMs(0).build(),
                new OneToOneMailboxManager(2), worker, (a, d, u) -> { throw new IllegalStateException(); }, DateUtil::currentTimeMillis);
        try {
            Future<Integer> first = service.execute(1, () -> { max.accumulateAndGet(active.incrementAndGet(), Math::max); entered.countDown();
                try { Assertions.assertTrue(release.await(5, TimeUnit.SECONDS)); return 1; } finally { active.decrementAndGet(); } });
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            var failed = service.execute(2, () -> { throw new IllegalStateException("business"); });
            Assertions.assertThrows(Exception.class, () -> failed.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
            var pending = service.submitTaskWithResult(1, () -> {
                max.accumulateAndGet(active.incrementAndGet(), Math::max); active.decrementAndGet();
            }, false, "pending");
            release.countDown(); Assertions.assertEquals(1, first.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
            pending.result().otherwiseEmpty().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
            Assertions.assertEquals(1, max.get());
        } finally { release.countDown(); service.close(); }
        Assertions.assertEquals(2, count(metrics, TimingMetrics.Stage.EXECUTION, TimingMetrics.Outcome.SUCCESS));
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.EXECUTION, TimingMetrics.Outcome.FAILURE));
        service.submitTaskWithResult(3, () -> Assertions.fail("closed"), false, "closed");
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Stage.TASK_ACTUAL, TimingMetrics.Outcome.REJECTED));
    }
    /** 监控读取当前邮箱累计值，重复快照不会把同一任务算多遍。 */
    @Test
    void monitorRepeatedSnapshotsDoNotAccumulateExistingTotals() throws Exception {
        TimingMetrics.reset(true);
        var worker = Executors.newVirtualThreadPerTaskExecutor();
        try (TaskExecutorService service = new TaskExecutorService(TaskExecutionConfig.builder().defaultTaskTimeoutMs(0).build(),
                new OneToOneMailboxManager(2), worker, (a, d, u) -> { throw new IllegalStateException(); }, DateUtil::currentTimeMillis)) {
            service.execute(1, () -> 7).toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
            // Future完成可能早于旧MailboxStats更新；排空实际消费者后再读取同一快照两次。
            worker.shutdown(); Assertions.assertTrue(worker.awaitTermination(5, TimeUnit.SECONDS));
            var first = service.getMonitor().collectStatistics();
            var second = service.getMonitor().collectStatistics();
            Assertions.assertEquals(1, first.totalCompletedTasks);
            Assertions.assertEquals(first.totalCompletedTasks, second.totalCompletedTasks);
            Assertions.assertEquals(first.totalFailedTasks, second.totalFailedTasks);
            Assertions.assertTrue(second.timings.enabled());
        }
    }
    /** 注入单调时钟，长队列与实际执行耗时必须落入不同阶段，不能按等待超时释放消费权。 */
    @Test
    void injectedClockSeparatesLongQueueFromActualExecution() {
        AtomicLong clock = new AtomicLong();
        TimingMetrics metrics = TimingMetrics.reset(true, clock::get);
        HoldingExecutor worker = new HoldingExecutor();
        try (TaskExecutorService service = new TaskExecutorService(TaskExecutionConfig.builder().defaultTaskTimeoutMs(0).build(),
                new OneToOneMailboxManager(1), worker, (a, d, u) -> { throw new IllegalStateException(); }, DateUtil::currentTimeMillis)) {
            var result = service.execute(1, () -> { clock.addAndGet(2_000_000); return 7; });
            clock.set(10_000_001); worker.run();
            Assertions.assertEquals(7, result.result());
            var rows = metrics.snapshot().rows();
            Assertions.assertEquals(10_000_001, rows.stream().filter(r -> r.stage() == TimingMetrics.Stage.MAILBOX_QUEUE).findFirst().orElseThrow().maxNanos());
            Assertions.assertEquals(2_000_000, rows.stream().filter(r -> r.stage() == TimingMetrics.Stage.EXECUTION).findFirst().orElseThrow().maxNanos());
            Assertions.assertEquals(12_000_001, rows.stream().filter(r -> r.stage() == TimingMetrics.Stage.TASK_ACTUAL).findFirst().orElseThrow().maxNanos());
        }
    }
    /** 队列满与关闭均记录实际拒绝，不产生执行样本或重复业务完成。 */
    @Test
    void queuedCloseAndCapacityRejectionNeverCountAsExecuted() {
        TimingMetrics metrics = TimingMetrics.reset(true);
        HoldingExecutor worker = new HoldingExecutor();
        TaskExecutorService service = new TaskExecutorService(TaskExecutionConfig.builder().defaultTaskTimeoutMs(0).build(),
                new OneToOneMailboxManager(1), worker, (a, d, u) -> { throw new IllegalStateException(); }, DateUtil::currentTimeMillis);
        var first = service.submitTaskWithResult(1, () -> Assertions.fail("closed work ran"), false, "pending");
        var rejected = service.submitTaskWithResult(1, () -> Assertions.fail("full work ran"), false, "full");
        Assertions.assertFalse(rejected.isAccepted());
        service.close(); service.close(); worker.run();
        Assertions.assertTrue(first.result().failed());
        Assertions.assertEquals(2, count(metrics, TimingMetrics.Stage.MAILBOX_QUEUE, TimingMetrics.Outcome.REJECTED));
        Assertions.assertEquals(2, count(metrics, TimingMetrics.Stage.TASK_ACTUAL, TimingMetrics.Outcome.REJECTED));
        Assertions.assertEquals(0, count(metrics, TimingMetrics.Stage.EXECUTION, TimingMetrics.Outcome.SUCCESS));
    }
    /** 单槽手动消费者，仅控制调度时点，不替换正式邮箱执行逻辑。 */
    private static final class HoldingExecutor extends java.util.concurrent.AbstractExecutorService {
        /** 本用例唯一消费者与关闭状态。 */
        private Runnable pending;
        private boolean closed;
        @Override public void execute(Runnable task) { Assertions.assertNull(pending); pending = task; }
        /** 执行已调度消费者，旧代次和关闭后的行为仍由生产代码判定。 */
        void run() { Runnable task = pending; pending = null; if (task != null) { task.run(); } }
        @Override public void shutdown() { closed = true; }
        @Override public List<Runnable> shutdownNow() { closed = true; return List.of(); }
        @Override public boolean isShutdown() { return closed; }
        @Override public boolean isTerminated() { return closed; }
        @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return closed; }
    }
    /** @param metrics 窗口 @param stage 阶段 @param outcome 结果 @return 精确行计数之和 */
    private static long count(TimingMetrics metrics, TimingMetrics.Stage stage, TimingMetrics.Outcome outcome) {
        return metrics.snapshot().rows().stream().filter(r -> r.stage() == stage && r.outcome() == outcome).mapToLong(TimingMetrics.Row::count).sum();
    }
}
