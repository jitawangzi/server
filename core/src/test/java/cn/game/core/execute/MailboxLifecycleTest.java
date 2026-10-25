package cn.game.core.execute;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.game.core.execute.error.ErrorHandler;
import cn.game.core.execute.error.ErrorPolicy;
import cn.game.util.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * 用测试自有执行器、闩锁和可控时钟复现邮箱生命周期交错，不启动或关闭全局 Vert.x。
 * 手动执行器只控制消费者开始时机，任务仍经过正式提交和消费入口。
 */
class MailboxLifecycleTest {
    /** 当前用例创建的服务，结束时逐一关闭。 */
    private final List<TaskExecutorService> services = new ArrayList<>();
    /** 用于实际跨过监控扫描门槛的毫秒时钟。 */
    private final AtomicLong clock = new AtomicLong(DateUtil.currentTimeMillis());

    /** 关闭本用例的资源，不影响其他测试或业务服务。 */
    @AfterEach
    void closeServices() {
        services.forEach(TaskExecutorService::close);
    }

    /**
     * 创建不带生产后台任务的服务。
     * @param worker 控制消费者启动的执行器
     * @param capacity 每个邮箱的队列容量
     * @return 使用当前用例时钟的执行服务
     */
    private TaskExecutorService service(ManualExecutor worker, int capacity) {
        return service(worker, capacity, new OneToOneMailboxManager(capacity, clock::get),
                (action, delay, unit) -> { throw new RejectedExecutionException("test scheduler"); });
    }

    /**
     * 为共享邮箱和延迟重试注入本用例自有资源。
     * @param worker 手动执行器
     * @param capacity 容量配置
     * @param manager 需要验证的映射与退役策略
     * @param scheduler 手动或拒绝调度器
     * @return 本用例负责关闭的服务
     */
    private TaskExecutorService service(ManualExecutor worker, int capacity, MailboxManager manager,
            TaskExecutorService.RetryScheduler scheduler) {
        TaskExecutionConfig config = TaskExecutionConfig.builder().maxQueueSize(capacity)
                .defaultTaskTimeoutMs(0).mailboxCleanupIntervalMs(100).idleTimeoutMs(100)
                .deadlockDetectionThresholdMs(100).build();
        TaskExecutorService service = new TaskExecutorService(config, manager, worker, scheduler, clock::get);
        services.add(service);
        return service;
    }

    /** 空消费者也必须归还消费权，后续提交仍能推进。 */
    @Test
    void emptyConsumerReleasesOwnership() {
        ManualExecutor worker = new ManualExecutor();
        TaskExecutorService service = service(worker, 2);
        ActorMailbox mailbox = service.getMailboxManager().getOrCreateMailbox(1);
        Assertions.assertTrue(mailbox.compareAndSetProcessing(false, true));
        service.submitProcessor(mailbox);
        worker.runAll();
        Assertions.assertFalse(mailbox.isProcessing());
        Future<String> result = service.execute(1, () -> "next");
        worker.runAll();
        Assertions.assertEquals("next", result.result());
    }

    /** 错误策略自身抛错时原任务必须失败，后续任务不能被遗留消费权阻塞。 */
    @Test
    void throwingErrorHandlerCompletesOriginalTask() {
        ManualExecutor worker = new ManualExecutor();
        TaskExecutorService service = service(worker, 2);
        Future<String> first = service.execute(1, () -> { throw new IllegalStateException("action"); }, false,
                "throwing handler", 0, context -> { throw new IllegalArgumentException("handler"); });
        Future<String> second = service.execute(1, () -> "next");
        worker.runAll();
        Assertions.assertTrue(first.isComplete(), "错误处理器异常不能遗失原 Promise");
        Assertions.assertTrue(first.failed());
        Assertions.assertEquals("next", second.result());
        Assertions.assertFalse(service.getMailbox(1).isProcessing());
    }

    /** 立即重试遇队列满时必须明确失败，不能只忽略 offer 的返回值。 */
    @Test
    void fullImmediateRetryCompletesOriginalTask() {
        ManualExecutor worker = new ManualExecutor();
        TaskExecutorService service = service(worker, 1);
        AtomicReference<Future<String>> queued = new AtomicReference<>();
        Future<String> first = service.execute(1, () -> {
            queued.set(service.execute(1, () -> "queued"));
            throw new IllegalStateException("retry");
        }, false, "queue full retry", 0, context -> ErrorPolicy.retryHeadImmediately());
        worker.runAll();
        Assertions.assertTrue(first.isComplete(), "重试拒绝必须完成原 Promise");
        Assertions.assertInstanceOf(QueueFullException.class, first.cause());
        Assertions.assertEquals("queued", queued.get().result());
    }

    /** 执行器拒绝消费者时，已入队任务必须收到失败且不保留消费权。 */
    @Test
    void rejectedProcessorCompletesQueuedTasks() {
        ManualExecutor worker = new ManualExecutor();
        TaskExecutorService service = service(worker, 2);
        worker.reject = true;
        Future<String> result = service.execute(1, () -> "must not run");
        Assertions.assertTrue(result.isComplete(), "提交拒绝不能留下永久等待的任务");
        Assertions.assertInstanceOf(RejectedExecutionException.class, result.cause());
        Assertions.assertFalse(service.getMailbox(1).isProcessing());
    }

    /** 真实跨过扫描门槛时，监控只能告警，不能替仍在执行的任务释放消费权。 */
    @Test
    void monitorDoesNotCreateSecondConsumer() throws InterruptedException {
        ManualExecutor worker = new ManualExecutor();
        TaskExecutorService service = service(worker, 3);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();
        Future<String> first = service.execute(1, () -> {
            maxActive.accumulateAndGet(active.incrementAndGet(), Math::max);
            entered.countDown();
            try { release.await(); }
            finally { active.decrementAndGet(); }
            return "first";
        });
        Thread consumer = Thread.ofVirtual().start(worker.take());
        try {
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            Future<String> second = service.execute(1, () -> {
                maxActive.accumulateAndGet(active.incrementAndGet(), Math::max);
                active.decrementAndGet();
                return "second";
            });
            clock.addAndGet(TimeUnit.HOURS.toMillis(1));
            service.getMonitor().checkMailboxes();
            Assertions.assertEquals(0, worker.pending(), "监控不得另行提交消费者");
            Assertions.assertFalse(second.isComplete());
        } finally {
            release.countDown();
            consumer.join(5000);
            Assertions.assertFalse(consumer.isAlive());
            worker.runAll();
        }
        Assertions.assertEquals("first", first.result());
        Assertions.assertEquals(1, maxActive.get());
    }

    /** 新任务不能提前唤醒延迟重试，旧回调也不能绕过下一轮延迟或复活退役邮箱。 */
    @Test
    void delayedRetryKeepsOwnershipAndRejectsStaleCallbacks() {
        ManualExecutor worker = new ManualExecutor();
        ManualScheduler scheduler = new ManualScheduler();
        TaskExecutorService service = service(worker, 4, new OneToOneMailboxManager(4, clock::get), scheduler);
        AtomicInteger attempts = new AtomicInteger();
        Future<String> first = service.execute(1, () -> {
            if (attempts.incrementAndGet() < 3) { throw new IllegalStateException("retry"); }
            return "recovered";
        }, false, "delayed", 0, context -> ErrorPolicy.retryHeadWithDelay(10, TimeUnit.SECONDS));
        worker.runAll();
        Future<String> second = service.execute(1, () -> "next");
        ActorMailbox original = service.getMailbox(1);
        clock.addAndGet(1000);
        service.getMonitor().checkMailboxes();
        Assertions.assertEquals(0, service.getMailboxManager().cleanIdleMailboxes(100));
        Assertions.assertSame(original, service.getMailbox(1));
        Assertions.assertEquals(0, worker.pending());
        Assertions.assertFalse(second.isComplete());

        Runnable oldCallback = scheduler.take();
        oldCallback.run();
        worker.runAll();
        Assertions.assertEquals(2, attempts.get());
        oldCallback.run();
        Assertions.assertEquals(0, worker.pending(), "上一轮回调不能提前执行新一轮重试");
        scheduler.take().run();
        worker.runAll();
        Assertions.assertEquals("recovered", first.result());
        Assertions.assertEquals("next", second.result());

        Assertions.assertTrue(service.getMailboxManager().removeMailbox(original, -1));
        Future<String> fresh = service.execute(1, () -> "fresh");
        oldCallback.run();
        Assertions.assertEquals(1, worker.pending());
        worker.runAll();
        Assertions.assertEquals("fresh", fresh.result());
    }

    /** 延迟重试入队失败必须立即结束原任务，不再创建无效定时器。 */
    @Test
    void fullDelayedRetryCompletesOriginalTask() {
        ManualExecutor worker = new ManualExecutor();
        ManualScheduler scheduler = new ManualScheduler();
        TaskExecutorService service = service(worker, 1, new OneToOneMailboxManager(1, clock::get), scheduler);
        Future<String> first = service.execute(1, () -> {
            service.execute(1, () -> "queued");
            throw new IllegalStateException("retry");
        }, false, "full delayed", 0, context -> ErrorPolicy.retryHeadWithDelay(1, TimeUnit.SECONDS));
        worker.runAll();
        Assertions.assertTrue(first.isComplete());
        Assertions.assertInstanceOf(QueueFullException.class, first.cause());
        Assertions.assertEquals(0, scheduler.pending());
    }

    /** 调度器拒绝后等待重试及其后的任务均有终态，后续提交仍可运行。 */
    @Test
    void rejectedRetrySchedulerCompletesWaitingTasks() {
        ManualExecutor worker = new ManualExecutor();
        TaskExecutorService service = service(worker, 3);
        Future<String> first = service.execute(1, () -> { throw new IllegalStateException("retry"); }, false,
                "scheduler reject", 0, context -> ErrorPolicy.retryHeadWithDelay(1, TimeUnit.SECONDS));
        Future<String> second = service.execute(1, () -> "queued");
        worker.runAll();
        Assertions.assertInstanceOf(RejectedExecutionException.class, first.cause());
        Assertions.assertInstanceOf(RejectedExecutionException.class, second.cause());
        Assertions.assertFalse(service.getMailbox(1).isProcessing());
        Future<String> next = service.execute(1, () -> "next");
        worker.runAll();
        Assertions.assertEquals("next", next.result());
    }

    /** 定时器已到期但执行器拒绝时，不能只释放标志而遗失等待者。 */
    @Test
    void rejectedDelayedProcessorCompletesWaitingTasks() {
        ManualExecutor worker = new ManualExecutor();
        ManualScheduler scheduler = new ManualScheduler();
        TaskExecutorService service = service(worker, 3, new OneToOneMailboxManager(3, clock::get), scheduler);
        Future<String> first = service.execute(1, () -> { throw new IllegalStateException("retry"); }, false,
                "delayed reject", 0, context -> ErrorPolicy.retryHeadWithDelay(1, TimeUnit.SECONDS));
        worker.runAll();
        Future<String> second = service.execute(1, () -> "queued");
        worker.reject = true;
        scheduler.take().run();
        Assertions.assertInstanceOf(RejectedExecutionException.class, first.cause());
        Assertions.assertInstanceOf(RejectedExecutionException.class, second.cause());
        Assertions.assertFalse(service.getMailbox(1).isProcessing());
    }

    /** 关闭等待重试的邮箱时明确结束未开始任务，旧定时器不能继续执行。 */
    @Test
    void closeRejectsPendingRetryAndInvalidatesCallback() {
        ManualExecutor worker = new ManualExecutor();
        ManualScheduler scheduler = new ManualScheduler();
        TaskExecutorService service = service(worker, 3, new OneToOneMailboxManager(3, clock::get), scheduler);
        Future<String> first = service.execute(1, () -> { throw new IllegalStateException("retry"); }, false,
                "closing", 0, context -> ErrorPolicy.retryHeadWithDelay(1, TimeUnit.SECONDS));
        worker.runAll();
        Runnable callback = scheduler.take();
        service.close();
        Assertions.assertInstanceOf(RejectedExecutionException.class, first.cause());
        callback.run();
        Assertions.assertEquals(0, worker.pending());
    }

    /** 退役实例拒绝旧引用的迟到入队，旧实例清理不能删除同 ID 的新邮箱。 */
    @Test
    void retiredReferenceCannotAcceptOrRemoveReplacement() {
        ManualExecutor worker = new ManualExecutor();
        TaskExecutorService service = service(worker, 2);
        MailboxManager manager = service.getMailboxManager();
        ActorMailbox old = manager.getOrCreateMailbox(1);
        Assertions.assertTrue(manager.removeMailbox(old, -1));
        TaskWrapper<String> staleTask = new TaskWrapper<>(DefaultTask.<String>builder().action(() -> "stale").build(), Promise.promise());
        Assertions.assertFalse(old.offerTask(staleTask));
        Assertions.assertFalse(old.compareAndSetProcessing(false, true));
        Future<String> result = service.execute(1, () -> "fresh");
        ActorMailbox fresh = service.getMailbox(1);
        Assertions.assertNotSame(old, fresh);
        Assertions.assertFalse(manager.removeMailbox(old, -1));
        Assertions.assertSame(fresh, service.getMailbox(1));
        worker.runAll();
        Assertions.assertEquals("fresh", result.result());
    }

    /** 两条清理路径均按实际 ID 退役共享桶，避免将桶零再次解释为随机业务 ID。 */
    @Test
    void bothCleanersUseActualSharedMailboxId() {
        for (boolean monitor : List.of(false, true)) {
            ManualExecutor worker = new ManualExecutor();
            SharedMailboxManager manager = new SharedMailboxManager(3, 1, clock::get);
            TaskExecutorService service = service(worker, 3, manager, new ManualScheduler());
            Future<String> first = service.execute(1, () -> "first");
            Future<String> second = service.execute(2, () -> "second");
            Assertions.assertEquals(1, worker.pending());
            worker.runAll();
            Assertions.assertEquals("first", first.result());
            Assertions.assertEquals("second", second.result());
            clock.addAndGet(1000);
            if (monitor) { service.getMonitor().checkMailboxes(); }
            else { Assertions.assertEquals(1, manager.cleanIdleMailboxes(100)); }
            Assertions.assertTrue(manager.getMailboxes().isEmpty());
        }
    }

    /** 零 ID 在每次提交时只映射一次；清理实际邮箱不能触发新的随机映射。 */
    @Test
    void zeroIdMappingOccursOncePerSubmission() {
        ManualExecutor worker = new ManualExecutor();
        AtomicInteger mappings = new AtomicInteger();
        OneToOneMailboxManager manager = new OneToOneMailboxManager(2, clock::get) {
            @Override
            protected long mapToMailboxId0() { return Long.MAX_VALUE - mappings.incrementAndGet(); }
        };
        TaskExecutorService service = service(worker, 2, manager, new ManualScheduler());
        Future<String> first = service.execute(0, () -> "first");
        Future<String> second = service.execute(0, () -> "second");
        Assertions.assertEquals(2, mappings.get());
        Assertions.assertEquals(2, worker.pending());
        worker.runAll();
        Assertions.assertEquals("first", first.result());
        Assertions.assertEquals("second", second.result());
        clock.addAndGet(1000);
        service.getMonitor().checkMailboxes();
        Assertions.assertTrue(manager.getMailboxes().isEmpty());
        Assertions.assertEquals(2, mappings.get());
    }

    /** 普通任务保持 FIFO，fast 重载确实插入队首，成功受理任务各执行一次。 */
    @Test
    void fifoAndFastOrderingRemainExplicit() {
        ManualExecutor worker = new ManualExecutor();
        TaskExecutorService service = service(worker, 4);
        List<String> order = new ArrayList<>();
        service.execute(1, () -> order.add("first"));
        service.execute(1, () -> order.add("second"));
        service.execute(1, () -> order.add("fast"), true, "fast overload");
        worker.runAll();
        Assertions.assertEquals(List.of("fast", "first", "second"), order);
    }

    /** 获取入队的协调范围必须覆盖清理交错，不能产生已受理任务所在的孤立实例。 */
    @Test
    void enqueueAndRetirementShareTheSameMailboxBoundary() throws Exception {
        ManualExecutor worker = new ManualExecutor();
        OneToOneMailboxManager manager = new OneToOneMailboxManager(3, clock::get);
        TaskExecutorService service = service(worker, 3, manager, new ManualScheduler());
        CountDownLatch offering = new CountDownLatch(1);
        CountDownLatch allowOffer = new CountDownLatch(1);
        CountDownLatch cleanupStarted = new CountDownLatch(1);
        ActorMailbox original = new ActorMailbox(1, 3, clock::get) {
            @Override
            public boolean offerTask(TaskWrapper<?> task, boolean fast) {
                offering.countDown();
                Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> allowOffer.await(5, TimeUnit.SECONDS)));
                return super.offerTask(task, fast);
            }
        };
        manager.getMailboxes().put(1L, original);
        try (var callers = Executors.newVirtualThreadPerTaskExecutor()) {
            try {
                var submitted = callers.submit(() -> service.execute(1, () -> "accepted"));
                Assertions.assertTrue(offering.await(5, TimeUnit.SECONDS));
                var removed = callers.submit(() -> {
                    cleanupStarted.countDown();
                    return manager.removeMailbox(original, -1);
                });
                Assertions.assertTrue(cleanupStarted.await(5, TimeUnit.SECONDS));
                allowOffer.countDown();
                Future<String> result = submitted.get(5, TimeUnit.SECONDS);
                Assertions.assertFalse(removed.get(5, TimeUnit.SECONDS));
                Assertions.assertSame(original, service.getMailbox(1));
                worker.runAll();
                Assertions.assertEquals("accepted", result.result());
                Assertions.assertEquals(1, original.getStats().completedTasks.get());
            } finally { allowOffer.countDown(); }
        }
    }

    /** 一个邮箱的业务阻塞不占用其他邮箱的协调区域，关闭也不伪报运行任务已取消。 */
    @Test
    void blockedMailboxDoesNotBlockOthersOrFakeRunningCancellation() throws InterruptedException {
        ManualExecutor worker = new ManualExecutor();
        TaskExecutorService service = service(worker, 3);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Future<String> running = service.execute(1, () -> {
            entered.countDown();
            release.await();
            return "actual completion";
        });
        Thread consumer = Thread.ofVirtual().start(worker.take());
        try {
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            Future<String> other = service.execute(2, () -> "independent");
            Future<String> queued = service.execute(1, () -> "pending");
            worker.runAll();
            Assertions.assertEquals("independent", other.result());
            Assertions.assertFalse(running.isComplete());
            service.close();
            Assertions.assertInstanceOf(RejectedExecutionException.class, queued.cause());
            Assertions.assertFalse(running.isComplete(), "关闭不能将仍在执行的任务伪报为已取消");
        } finally {
            release.countDown();
            consumer.join(5000);
            Assertions.assertFalse(consumer.isAlive());
        }
        Assertions.assertEquals("actual completion", running.result());
    }

    /** 等待方超时不释放实际消费权，后继任务必须等待原业务真正结束。 */
    @Test
    void waitingTimeoutDoesNotReleaseRunningConsumer() throws InterruptedException {
        ManualExecutor worker = new ManualExecutor();
        TaskExecutorService service = service(worker, 3);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger finished = new AtomicInteger();
        Future<String> waiting = service.execute(1, () -> {
            entered.countDown();
            release.await();
            finished.incrementAndGet();
            return "actual";
        }, false, "waiting timeout", 20, ErrorHandler.DISCARD_HANDLER);
        Thread consumer = Thread.ofVirtual().start(worker.take());
        try {
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS));
            Future<String> queued = service.execute(1, () -> "next");
            ExecutionException failure = Assertions.assertThrows(ExecutionException.class,
                    () -> waiting.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
            Assertions.assertInstanceOf(TimeoutException.class, failure.getCause());
            Assertions.assertEquals(0, finished.get());
            Assertions.assertTrue(service.getMailbox(1).isProcessing());
            Assertions.assertEquals(0, worker.pending());
            Assertions.assertFalse(queued.isComplete());
        } finally {
            release.countDown();
            consumer.join(5000);
            Assertions.assertFalse(consumer.isAlive());
            worker.runAll();
        }
        Assertions.assertEquals(1, finished.get());
    }

    /** 手动定时器只保存已受理回调，测试显式触发到期，不依赖固定 sleep。 */
    private static final class ManualScheduler implements TaskExecutorService.RetryScheduler {
        /** 按受理顺序保存的延迟回调。 */
        private final Queue<Runnable> callbacks = new ConcurrentLinkedQueue<>();

        @Override
        public void schedule(Runnable action, long delay, TimeUnit unit) {
            Assertions.assertTrue(delay > 0);
            Assertions.assertNotNull(unit);
            callbacks.add(action);
        }

        /** @return 取出一个到期回调，缺失时使测试失败。 */
        Runnable take() { return callbacks.remove(); }

        /** @return 尚未触发的回调数。 */
        int pending() { return callbacks.size(); }
    }

    /** 手动消费提交的 Runnable，拒绝开关用于确定性模拟执行器关闭或容量拒绝。 */
    private static final class ManualExecutor extends AbstractExecutorService {
        /** 已受理但尚未执行的消费者。 */
        private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
        /** 是否模拟拒绝新的消费者。 */
        private volatile boolean reject;
        /** 执行器关闭状态。 */
        private volatile boolean shutdown;

        @Override
        public void execute(Runnable command) {
            if (reject || shutdown) { throw new RejectedExecutionException("manual executor rejected"); }
            queue.add(command);
        }

        /** @return 取出一个受理的消费者，无任务时使测试失败。 */
        Runnable take() { return queue.remove(); }

        /** @return 等待执行的消费者数量。 */
        int pending() { return queue.size(); }

        /** 执行已受理消费者，显式限制轮次以暴露重复提交或无限循环。 */
        void runAll() {
            int runs = 0;
            while (!queue.isEmpty()) {
                Assertions.assertTrue(++runs <= 100, "消费者出现无界重提");
                take().run();
            }
        }

        @Override
        public void shutdown() { shutdown = true; }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            List<Runnable> remaining = new ArrayList<>(queue);
            queue.clear();
            return remaining;
        }

        @Override
        public boolean isShutdown() { return shutdown; }

        @Override
        public boolean isTerminated() { return shutdown && queue.isEmpty(); }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) { return isTerminated(); }
    }
}
