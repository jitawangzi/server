package cn.game.core.execute;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.game.core.execute.error.ErrorHandler;
import cn.game.core.net.client.AbstractNetClient;
import cn.game.core.net.process.DirectProcessor;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.bytes.ByteArrayProtocol;
import cn.game.core.net.rpc.vertx.VertxRPCService;
import cn.game.core.net.transport.Command;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.DateUtil;
import cn.game.util.ServerType;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;

/** 手动调度及闩锁复现提交、关闭和快速执行交错，并走真实网络/RPC处理器入口。 */
class TaskAdmissionTest {
    /** 每个用例负责关闭自己的执行服务。 */
    private final List<TaskExecutorService> services = new ArrayList<>();

    /** 关闭用例资源，不触碰全局服务。 */
    @AfterEach
    void closeServices() { services.forEach(TaskExecutorService::close); }

    /** @param worker 可控执行器 @param manager 邮箱管理器 @return 本用例的服务 */
    private TaskExecutorService service(Worker worker, MailboxManager manager) {
        TaskExecutorService service = new TaskExecutorService(TaskExecutionConfig.builder().defaultTaskTimeoutMs(0).build(),
                manager, worker, (action, delay, unit) -> { throw new RejectedExecutionException("no timer"); }, DateUtil::currentTimeMillis);
        services.add(service);
        return service;
    }

    /** 布尔入口依据受理状态返回，覆盖队列满、执行器拒绝和已关闭。 */
    @Test
    void booleanSubmissionReportsAdmissionFailure() {
        Worker worker = new Worker();
        TaskExecutorService service = service(worker, new OneToOneMailboxManager(1));
        Assertions.assertTrue(service.submitTask(1, () -> {}));
        Assertions.assertFalse(service.submitTask(1, () -> Assertions.fail("queue full task ran")));
        worker.reject = true;
        Assertions.assertFalse(service.submitTask(2, () -> Assertions.fail("rejected task ran")));
        service.close();
        Assertions.assertFalse(service.submitTask(3, () -> Assertions.fail("closed task ran")));
    }

    /** 即使业务在提交返回之前抛出拒绝异常，也属于已受理后的执行失败。 */
    @Test
    void immediateBusinessFailureIsStillAccepted() {
        Worker worker = new Worker();
        worker.inline = true;
        TaskExecutorService service = service(worker, new OneToOneMailboxManager(2));
        AtomicInteger calls = new AtomicInteger();
        Runnable task = () -> { calls.incrementAndGet(); throw new RejectedExecutionException("business failure"); };
        Assertions.assertTrue(service.submitTask(1, task));
        TaskSubmission<Void> receipt = service.submitTaskWithResult(1, task, false, "fast failure");
        Assertions.assertTrue(receipt.isAccepted());
        Assertions.assertTrue(receipt.hasStarted());
        Assertions.assertTrue(receipt.result().failed());
        Future<Void> result = new IdVirtualThreadProcessor(service).processAsync(1, task, false);
        Assertions.assertTrue(result.failed());
        Assertions.assertFalse(result.cause() instanceof TaskNotExecutedException);
        Assertions.assertEquals(3, calls.get(), "每次提交只运行一次，不自动重试");
    }

    /** 已经执行的任务抛出内层未执行标记时，两个处理器都必须保留当前任务已执行事实。 */
    @Test
    void nestedRejectionDoesNotImpersonateCurrentSubmission() {
        Worker worker = new Worker();
        worker.inline = true;
        TaskExecutorService service = service(worker, new OneToOneMailboxManager(2));
        for (cn.game.core.net.process.Processor processor : List.of(new DirectProcessor(), new IdVirtualThreadProcessor(service))) {
            AtomicInteger effects = new AtomicInteger();
            TaskNotExecutedException inner = new TaskNotExecutedException(new RejectedExecutionException("nested"));
            Future<Void> result = processor.processAsync(1, () -> { effects.incrementAndGet(); throw inner; }, false);
            Assertions.assertEquals(1, effects.get());
            Assertions.assertTrue(result.failed());
            Assertions.assertInstanceOf(TaskExecutionException.class, result.cause());
            Assertions.assertSame(inner, result.cause().getCause());
        }
    }

    /** 返回受理后关闭，排队任务得到未执行终态；旧处理器不再运行其业务。 */
    @Test
    void acceptedPendingTaskReportsLaterShutdown() {
        Worker worker = new Worker();
        TaskExecutorService service = service(worker, new OneToOneMailboxManager(2));
        AtomicInteger calls = new AtomicInteger();
        TaskSubmission<Void> receipt = service.submitTaskWithResult(1, calls::incrementAndGet, false, "pending");
        Future<Void> response = new IdVirtualThreadProcessor(service).processAsync(1, calls::incrementAndGet, false);
        Assertions.assertTrue(receipt.isAccepted());
        Assertions.assertFalse(response.isComplete());
        service.close();
        Assertions.assertTrue(receipt.result().failed());
        Assertions.assertFalse(receipt.hasStarted());
        Assertions.assertInstanceOf(TaskNotExecutedException.class, response.cause());
        worker.runAll();
        Assertions.assertEquals(0, calls.get());
    }

    /** 已通过最初 running 检查、但在 close 扫描之后入队的请求也必须明确收尾。 */
    @Test
    void enqueueAfterCloseScanDoesNotStrandTask() throws Exception {
        CountDownLatch entering = new CountDownLatch(1);
        CountDownLatch proceed = new CountDownLatch(1);
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch finishTask = new CountDownLatch(1);
        CountDownLatch scanned = new CountDownLatch(1);
        CountDownLatch finishClose = new CountDownLatch(1);
        Worker worker = new Worker() {
            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) {
                scanned.countDown();
                Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> finishClose.await(5, TimeUnit.SECONDS)));
                return true;
            }
        };
        AtomicInteger submissions = new AtomicInteger();
        OneToOneMailboxManager manager = new OneToOneMailboxManager(2) {
            @Override
            public ActorMailbox enqueueTask(long entityId, TaskWrapper<?> task, boolean fast) {
                if (submissions.incrementAndGet() == 2) {
                    entering.countDown();
                    Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> proceed.await(5, TimeUnit.SECONDS)));
                }
                return super.enqueueTask(entityId, task, fast);
            }
        };
        TaskExecutorService service = service(worker, manager);
        TaskSubmission<Void> first = service.submitTaskWithResult(1, () -> {
            running.countDown();
            Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> finishTask.await(5, TimeUnit.SECONDS)));
        }, false, "running during close");
        Thread consumer = Thread.ofVirtual().start(worker.tasks.remove());
        try (var callers = Executors.newVirtualThreadPerTaskExecutor()) {
            try {
                Assertions.assertTrue(running.await(5, TimeUnit.SECONDS));
                var submitting = callers.submit(() -> service.submitTaskWithResult(1,
                        () -> Assertions.fail("closed task ran"), false, "close race"));
                Assertions.assertTrue(entering.await(5, TimeUnit.SECONDS));
                var closing = callers.submit(service::close);
                Assertions.assertTrue(scanned.await(5, TimeUnit.SECONDS));
                // close 已扫描空队列，但原消费者仍持有消费权；第二次入队不会触发新处理器提交。
                proceed.countDown();
                TaskSubmission<Void> receipt = submitting.get(5, TimeUnit.SECONDS);
                Assertions.assertFalse(receipt.isAccepted());
                Assertions.assertTrue(receipt.result().failed());
                Assertions.assertFalse(receipt.hasStarted());
                Assertions.assertFalse(first.result().isComplete());
                finishTask.countDown();
                consumer.join(5000);
                Assertions.assertFalse(consumer.isAlive());
                finishClose.countDown();
                closing.get(5, TimeUnit.SECONDS);
                worker.runAll();
            } finally {
                proceed.countDown(); finishTask.countDown(); finishClose.countDown();
                consumer.join(5000);
            }
        }
    }

    /** 已执行后重试调度失败仍属于执行失败，不会变成可以安全重投的未执行请求。 */
    @Test
    void retryRejectionDoesNotEraseStartedState() {
        Worker worker = new Worker();
        List<TaskWrapper<?>> captured = new ArrayList<>();
        OneToOneMailboxManager manager = new OneToOneMailboxManager(2) {
            @Override
            public ActorMailbox enqueueTask(long entityId, TaskWrapper<?> task, boolean fast) {
                captured.add(task);
                return super.enqueueTask(entityId, task, fast);
            }
        };
        TaskExecutorService service = service(worker, manager);
        Future<Object> result = service.execute(1, () -> { throw new IllegalStateException("retry"); }, false,
                "retry then reject", 0, context -> cn.game.core.execute.error.ErrorPolicy.retryHeadWithDelay(1, TimeUnit.SECONDS));
        worker.runAll();
        Assertions.assertTrue(result.failed());
        Assertions.assertTrue(captured.getFirst().hasStarted());
        Assertions.assertFalse(captured.getFirst().wasRejectedBeforeStart());
    }

    /** 两个网络入口均返回原请求的拒绝，不触发业务、完成钩子或自动重试。 */
    @Test
    void networkEntryReportsQueueAndExecutorRejection() {
        Worker worker = new Worker();
        TaskExecutorService service = service(worker, new OneToOneMailboxManager(1));
        IdVirtualThreadProcessor processor = new IdVirtualThreadProcessor(service);
        RecordingClient client = new RecordingClient();
        client.setPlayerId(1);
        service.submitTask(1, () -> {});
        processor.process(client, new ByteArrayProtocol(1, new byte[0], 41));
        worker.reject = true;
        processor.process(2, client, new ByteArrayProtocol(1, new byte[0], 42));
        service.close();
        processor.process(client, new ByteArrayProtocol(1, new byte[0], 43));
        Assertions.assertEquals(List.of(41, 42, 43), client.sequences);
        Assertions.assertEquals(List.of(20, 20, 20), client.errors);
        Assertions.assertEquals(0, client.afterCalls);
    }

    /** RPC 尚未执行即队列满或调度拒绝时立即返回错误，不能等到传输超时。 */
    @Test
    void rpcEntryReportsQueueAndExecutorRejectionOnce() {
        for (boolean full : List.of(false, true)) {
            Worker worker = new Worker();
            TaskExecutorService service = service(worker, new OneToOneMailboxManager(1));
            if (full) { service.submitTask(1, () -> {}); }
            else { worker.reject = true; }
            ReplyProbe probe = new ReplyProbe();
            new VertxRPCService<>(null, "test", ServerType.Game, new IdVirtualThreadProcessor(service)).handleMessage(probe.message());
            Assertions.assertEquals(List.of(ErrorMsgEnum.system_overload.getId()), probe.failures);
            Assertions.assertEquals(0, probe.replies);
        }
    }

    /** 已经排队的 RPC 在关闭时也只返回一次失败，旧消费回调不能再次回复。 */
    @Test
    void rpcPendingShutdownRepliesOnce() {
        Worker worker = new Worker();
        TaskExecutorService service = service(worker, new OneToOneMailboxManager(2));
        ReplyProbe probe = new ReplyProbe();
        new VertxRPCService<>(null, "test", ServerType.Game, new IdVirtualThreadProcessor(service)).handleMessage(probe.message());
        Assertions.assertTrue(probe.failures.isEmpty());
        service.close();
        worker.runAll();
        Assertions.assertEquals(List.of(ErrorMsgEnum.system_overload.getId()), probe.failures);
        Assertions.assertEquals(0, probe.replies);
    }

    /** 其他处理器的兼容入口同样区分调度拒绝与已经执行的异常。 */
    @Test
    void defaultProcessorAdapterDistinguishesExecutionFailure() {
        DirectProcessor direct = new DirectProcessor();
        Assertions.assertTrue(direct.processAsync(1, () -> {}, true).succeeded());
        Future<Void> businessFailure = direct.processAsync(1, () -> { throw new RejectedExecutionException("business"); }, true);
        Assertions.assertTrue(businessFailure.failed());
        Assertions.assertFalse(businessFailure.cause() instanceof TaskNotExecutedException);
        DirectProcessor rejecting = new DirectProcessor() {
            @Override
            public void process(long id, Runnable task, boolean fast) { throw new RejectedExecutionException("not submitted"); }
        };
        Assertions.assertInstanceOf(TaskNotExecutedException.class, rejecting.processAsync(1, () -> {}, true).cause());
    }

    /** 网络反馈探针，不允许拒绝路径执行请求完成钩子。 */
    private static final class RecordingClient extends AbstractNetClient {
        /** 拒绝请求的序号。 */
        private final List<Integer> sequences = new ArrayList<>();
        /** 对应错误码。 */
        private final List<Integer> errors = new ArrayList<>();
        /** 正常业务结束钩子调用次数。 */
        private int afterCalls;
        @Override
        public void rejectProtocol(IProtocol<?> request, int errorCode) { sequences.add(request.getSeq()); errors.add(errorCode); }
        @Override
        public void afterProcess(IProtocol<?> protocol) { afterCalls++; }
        @Override
        public void sendProtocol(Object message) { Assertions.fail("unexpected business reply"); }
        @Override
        public void sendProtocol(Object message, int errorCode) { Assertions.fail("unexpected business reply"); }
        @Override
        public void close() {}
    }

    /** 在真实 RPC 服务的 Message 边界记录回复，拒绝时不应调用业务反射。 */
    private static final class ReplyProbe {
        /** 每次 fail 的协议码。 */
        private final List<Integer> failures = new ArrayList<>();
        /** 普通回复次数。 */
        private int replies;
        /** @return 可记录 RPC 服务 fail/reply 的消息 */
        @SuppressWarnings("unchecked")
        Message<Object> message() {
            return (Message<Object>) Proxy.newProxyInstance(Message.class.getClassLoader(), new Class<?>[] {Message.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "body" -> new Command("must.not.execute", "missing", new Class<?>[0], new Object[0], 1);
                        case "headers" -> MultiMap.caseInsensitiveMultiMap();
                        case "replyAddress" -> "reply.test";
                        case "fail" -> { failures.add((Integer) args[0]); yield null; }
                        case "reply" -> { replies++; yield null; }
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }
    }

    /** 可手动执行、立即执行或拒绝的测试自有执行器。 */
    private static class Worker extends AbstractExecutorService {
        /** 排队消费者。 */
        private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
        /** 是否拒绝调度。 */
        private boolean reject;
        /** 是否在提交返回前完成任务，用于复现快速失败。 */
        private boolean inline;
        /** 是否已关闭。 */
        private boolean shutdown;
        @Override
        public void execute(Runnable task) {
            if (reject || shutdown) { throw new RejectedExecutionException("worker rejected"); }
            if (inline) { task.run(); } else { tasks.add(task); }
        }
        /** 执行队列并限制次数，暴露消费者重复提交。 */
        void runAll() {
            int count = 0;
            while (!tasks.isEmpty()) {
                Assertions.assertTrue(++count < 100);
                tasks.remove().run();
            }
        }
        @Override
        public void shutdown() { shutdown = true; }
        @Override
        public List<Runnable> shutdownNow() { shutdown = true; List<Runnable> remaining = new ArrayList<>(tasks); tasks.clear(); return remaining; }
        @Override
        public boolean isShutdown() { return shutdown; }
        @Override
        public boolean isTerminated() { return shutdown && tasks.isEmpty(); }
        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) { return isTerminated(); }
    }
}
