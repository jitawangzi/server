package cn.game.core.execute;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.game.core.execute.error.ErrorPolicy;
import io.vertx.core.Future;

/** 用虚拟单调时间和手动消费者验证重试边界，不依赖真实等待或全局调度器。 */
class TaskRetryTimingTest {
    /** 两段三秒延迟到期前，新入队任务不能偷跑；第三次失败直接丢弃并继续队列。 */
    @Test
    void retryWaitsForEachDeadlineAndPreservesQueueOrder() {
        ManualExecutor worker = new ManualExecutor();
        ManualScheduler scheduler = new ManualScheduler();
        List<Integer> attempts = new ArrayList<>();
        List<String> order = new ArrayList<>();
        RuntimeException failure = new RuntimeException("retry contract");
        TaskExecutionConfig config = TaskExecutionConfig.builder().defaultTaskTimeoutMs(0).build();
        try (TaskExecutorService service = new TaskExecutorService(config, new OneToOneMailboxManager(16),
                worker, scheduler, () -> 0L)) {
            Future<String> result = service.execute(20, () -> {
                order.add("attempt");
                throw failure;
            }, false, "deterministic retry", 0, context -> {
                attempts.add(context.getAttemptCount());
                return context.getAttemptCount() == 3 ? ErrorPolicy.discard()
                        : ErrorPolicy.retryHeadWithDelay(3, TimeUnit.SECONDS);
            });
            worker.runAll();
            Future<String> follower = service.execute(20, () -> { order.add("follower"); return "done"; });

            for (int expectedAttempt = 1; expectedAttempt <= 2; expectedAttempt++) {
                Assertions.assertEquals(expectedAttempt, attempts.size());
                Assertions.assertEquals(3, scheduler.delay);
                Assertions.assertEquals(TimeUnit.SECONDS, scheduler.unit);
                scheduler.advance(TimeUnit.SECONDS.toNanos(3) - 1);
                worker.runAll();
                Assertions.assertEquals(expectedAttempt, attempts.size(), "到期前不能执行下一次尝试");
                Assertions.assertFalse(result.isComplete());
                Assertions.assertFalse(follower.isComplete(), "后续任务不能绕过延迟中的队首任务");
                scheduler.advance(1);
                worker.runAll();
            }
            Assertions.assertEquals(List.of(1, 2, 3), attempts);
            Assertions.assertSame(failure, result.cause());
            Assertions.assertEquals("done", follower.result());
            Assertions.assertEquals(List.of("attempt", "attempt", "attempt", "follower"), order);
            Assertions.assertNull(scheduler.pending, "最终丢弃后不能遗留第三段延迟");
            Assertions.assertFalse(service.getMailbox(20).isProcessing());
        }
    }

    /** 只在虚拟时间达到入队时刻加延迟后释放一次回调。 */
    private static final class ManualScheduler implements TaskExecutorService.RetryScheduler {
        /** 虚拟单调时间，单位纳秒。 */
        private final AtomicLong now = new AtomicLong();
        /** 当前待触发回调；当前消费权只允许存在一条。 */
        private Runnable pending;
        /** 当前回调的纳秒截止点。 */
        private long deadline;
        /** 原始延迟数值。 */
        private long delay;
        /** 原始延迟单位。 */
        private TimeUnit unit;

        @Override
        public void schedule(Runnable action, long delay, TimeUnit unit) {
            Assertions.assertNull(pending);
            this.delay = delay;
            this.unit = unit;
            deadline = now.get() + unit.toNanos(delay);
            pending = action;
        }

        /**
         * 推进虚拟时间并释放已到期的回调。
         * @param nanos 本次前进的纳秒数
         */
        private void advance(long nanos) {
            now.addAndGet(nanos);
            if (pending != null && now.get() >= deadline) {
                Runnable action = pending;
                pending = null;
                action.run();
            }
        }
    }

    /** 手动运行消费者，使测试能观察每个延迟截止点前后的完整队列状态。 */
    private static final class ManualExecutor extends AbstractExecutorService {
        /** 已受理的消费者。 */
        private final Queue<Runnable> tasks = new ArrayDeque<>();
        /** 执行器关闭标记。 */
        private boolean shutdown;

        @Override
        public void execute(Runnable command) { tasks.add(command); }

        /** 在当前虚拟时刻运行所有已提交的消费者，并检测无界重提。 */
        private void runAll() {
            int count = 0;
            while (!tasks.isEmpty()) {
                Assertions.assertTrue(++count < 10, "消费者无界重提");
                tasks.remove().run();
            }
        }

        @Override
        public void shutdown() { shutdown = true; }
        @Override
        public List<Runnable> shutdownNow() { shutdown = true; List<Runnable> rest = new ArrayList<>(tasks); tasks.clear(); return rest; }
        @Override
        public boolean isShutdown() { return shutdown; }
        @Override
        public boolean isTerminated() { return shutdown && tasks.isEmpty(); }
        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) { return isTerminated(); }
    }
}
