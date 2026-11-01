package cn.game.core.task;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/** 验证相对延迟不经过墙钟或毫秒截断；所有调度资源由用例独占。 */
class SchedulerServiceTimingTest {
    /** 向前偏移墙钟不能缩短调用者要求的相对等待。 */
    @Test
    void relativeDelayIgnoresForwardWallClock() {
        assertSubmittedDelay(Duration.ofHours(1), 3, TimeUnit.SECONDS);
    }

    /** 向后偏移墙钟不能将三秒等待放大到数小时。 */
    @Test
    void relativeDelayIgnoresBackwardWallClock() {
        assertSubmittedDelay(Duration.ofHours(-1), 3, TimeUnit.SECONDS);
    }

    /** 亚毫秒延迟必须保留原始精度，不能截断为立即执行。 */
    @Test
    void submillisecondDelayKeepsPrecision() {
        assertSubmittedDelay(Duration.ZERO, 500_001, TimeUnit.NANOSECONDS);
    }

    /** 真实线程池重复使用同一服务，逐次验证单调时间下不早于要求的延迟。 */
    @Test
    void realSchedulerDoesNotFireEarlyAcrossReuse() throws Exception {
        SchedulerService service = new SchedulerService(new ThreadPoolTaskScheduler());
        List<Long> elapsed = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                AtomicLong firedAt = new AtomicLong();
                long start = System.nanoTime();
                ScheduledFuture<?> future = service.scheduleTask(() -> firedAt.set(System.nanoTime()),
                        200, TimeUnit.MILLISECONDS);
                future.get(5, TimeUnit.SECONDS);
                long duration = firedAt.get() - start;
                elapsed.add(duration);
                Assertions.assertTrue(duration >= TimeUnit.MILLISECONDS.toNanos(200),
                        "相对延迟提前触发，实际纳秒=" + duration);
            }
            org.slf4j.LoggerFactory.getLogger(getClass()).info("重复调度的实际等待纳秒: {}", elapsed);
        } finally {
            service.shutdown();
        }
    }

    /** 取消句柄仍控制原任务，关闭后不得受理一次性调度。 */
    @Test
    void cancellationAndShutdownKeepTheirContract() {
        SchedulerService service = new SchedulerService(new ThreadPoolTaskScheduler());
        try {
            ScheduledFuture<?> future = service.scheduleTask(() -> Assertions.fail("已取消任务不能执行"),
                    1, TimeUnit.DAYS);
            Assertions.assertTrue(future.cancel(false));
            Assertions.assertTrue(future.isCancelled());
        } finally {
            service.shutdown();
        }
        Assertions.assertThrows(java.util.concurrent.RejectedExecutionException.class,
                () -> service.scheduleTask(() -> { }, 1, TimeUnit.SECONDS));
    }

    /** 一次性任务仍沿用 Spring 的 Throwable 处理策略，异常后调度器可以继续使用。 */
    @Test
    void taskThrowableDoesNotBreakSchedulerReuse() throws Exception {
        SchedulerService service = new SchedulerService(new ThreadPoolTaskScheduler());
        try {
            ScheduledFuture<?> failed = service.scheduleTask(() -> { throw new AssertionError("expected task error"); },
                    0, TimeUnit.SECONDS);
            Assertions.assertNull(failed.get(5, TimeUnit.SECONDS));
            AtomicLong calls = new AtomicLong();
            service.scheduleTask(calls::incrementAndGet, 0, TimeUnit.SECONDS).get(5, TimeUnit.SECONDS);
            Assertions.assertEquals(1, calls.get());
        } finally {
            service.shutdown();
        }
    }

    /**
     * 检查实际提交给底层执行器的相对等待，使用偏移墙钟确定性复现时钟转换缺陷。
     * @param offset Spring 墙钟偏移；不修改操作系统时间
     * @param delay 调用者要求的延迟
     * @param unit 延迟单位
     */
    private void assertSubmittedDelay(Duration offset, long delay, TimeUnit unit) {
        RecordingScheduler scheduler = new RecordingScheduler();
        scheduler.setClock(Clock.offset(Clock.systemUTC(), offset));
        SchedulerService service = new SchedulerService(scheduler);
        try {
            ScheduledFuture<?> future = service.scheduleTask(() -> { }, delay, unit);
            Assertions.assertEquals(unit.toNanos(delay), scheduler.recording.delayNanos,
                    "底层执行器应收到完整相对延迟，与墙钟无关");
            Assertions.assertTrue(future.cancel(false));
        } finally {
            scheduler.recording.shutdownNow();
            service.shutdown();
        }
    }

    /** 保留 Spring 的真实调度路径，只在最终入队处记录延迟。 */
    private static final class RecordingScheduler extends ThreadPoolTaskScheduler {
        /** 独占的底层记录器；不启动真实业务动作。 */
        private final RecordingExecutor recording = new RecordingExecutor();

        @Override
        protected ScheduledExecutorService createExecutor(int poolSize, ThreadFactory threadFactory,
                java.util.concurrent.RejectedExecutionHandler rejectedExecutionHandler) {
            return recording;
        }
    }

    /** 捕获原始入队参数，将占位任务放到远期，避免测试依赖线程启动速度。 */
    private static final class RecordingExecutor extends ScheduledThreadPoolExecutor {
        /** 最后一次入队的原始相对延迟，单位为纳秒。 */
        private long delayNanos;

        /** 创建用例独占的单线程执行器。 */
        private RecordingExecutor() { super(1, Executors.defaultThreadFactory()); }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            delayNanos = unit.toNanos(delay);
            return super.schedule(command, 1, TimeUnit.DAYS);
        }
    }
}
