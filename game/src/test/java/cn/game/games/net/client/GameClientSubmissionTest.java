package cn.game.games.net.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.game.core.execute.TaskNotExecutedException;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.bytes.ByteArrayProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.core.net.socket.handler.Handler;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/** 控制提交与完成时机，验证真实处理器入口和 GameClient 的重复请求边界。 */
class GameClientSubmissionTest {
    /** 排队中的原请求不能因重发遇满队列而收到矛盾拒绝。 */
    @Test
    void queuedDuplicateDoesNotReportRejection() {
        ControlledProcessor processor = new ControlledProcessor();
        RecordingClient client = new RecordingClient();
        processor.process(client, request(41));
        processor.reject = true;
        processor.process(client, request(41));
        Assertions.assertEquals(1, processor.submissions);
        Assertions.assertTrue(client.errors.isEmpty());
        processor.actions.getFirst().run();
        processor.results.getFirst().complete();
        Assertions.assertEquals(1, processor.effects);
    }

    /** 运行中的请求即使已有部分缓存，也不能被重发拆成半份重放或拒绝。 */
    @Test
    void runningDuplicateWaitsForOriginalResult() {
        ControlledProcessor processor = new ControlledProcessor();
        RecordingClient client = new RecordingClient();
        processor.process(client, request(41));
        processor.actions.getFirst().run();
        client.getRecentMessages().put(41, List.of(new ByteArrayProtocol(2, new byte[] {1}, 41, 0)));
        processor.reject = true;
        processor.process(1, client, request(41));
        Assertions.assertEquals(1, processor.submissions);
        Assertions.assertTrue(client.errors.isEmpty());
        Assertions.assertEquals(1, processor.effects);
    }

    /** 已完成请求遇到当前队列拒绝时重放全部缓存，不改变原结果。 */
    @Test
    void completedDuplicateReplaysCacheOnRejection() {
        ControlledProcessor processor = new ControlledProcessor();
        processor.reject = true;
        RecordingClient client = new RecordingClient();
        client.getRecentMessages().put(41, List.of(new ByteArrayProtocol(2, new byte[] {1}, 41, 0),
                new ByteArrayProtocol(3, new byte[] {2}, 41, 0)));
        processor.process(client, request(41));
        Assertions.assertEquals(1, processor.submissions);
        Assertions.assertEquals(List.of(0, 0), client.errors);
        Assertions.assertEquals(List.of(41, 41), client.sequences);
        Assertions.assertEquals(2, client.getRecentMessages().get(41).size());
    }

    /** 正常受理的缓存重发仍走原限流计数及重放顺序。 */
    @Test
    void admittedCachedRequestKeepsNormalRateCounting() {
        ControlledProcessor processor = new ControlledProcessor();
        RecordingClient client = new RecordingClient();
        client.getRecentMessages().put(41, List.of(new ByteArrayProtocol(2, new byte[] {1}, 41, 0)));
        processor.process(client, request(41));
        Assertions.assertEquals(1, processor.submissions);
        Assertions.assertTrue(client.errors.isEmpty());
        processor.actions.getFirst().run();
        processor.results.getFirst().complete();
        Assertions.assertEquals(1, client.packetMaxCountPerSecond);
        Assertions.assertEquals(0, processor.effects);
        Assertions.assertEquals(List.of(0), client.errors);
    }

    /** 已知成功快照在排队期间被缓存淘汰后，关闭拒绝也不能改报未执行。 */
    @Test
    void pendingRejectionPreservesEvictedCachedResult() {
        ControlledProcessor processor = new ControlledProcessor();
        RecordingClient client = new RecordingClient();
        client.getRecentMessages().put(41, List.of(new ByteArrayProtocol(2, new byte[] {1}, 41, 0)));
        processor.process(client, request(41));
        client.clearRecentMessages();
        processor.results.getFirst().fail(new TaskNotExecutedException(new RejectedExecutionException("closed")));
        Assertions.assertEquals(List.of(0), client.errors);
        Assertions.assertTrue(client.getRecentMessages().isEmpty());
    }

    /** 已经执行但没有缓存的当前/旧序号沿用原抑制语义。 */
    @Test
    void processedSequenceWithoutCacheIsNotRejected() {
        ControlledProcessor processor = new ControlledProcessor();
        processor.reject = true;
        RecordingClient client = new RecordingClient();
        Assertions.assertTrue(client.needProcess(request(41)));
        processor.process(client, request(41));
        processor.process(client, request(40));
        Assertions.assertEquals(0, processor.submissions);
        Assertions.assertTrue(client.errors.isEmpty());
    }

    /** 拒绝写入完成前仍保留登记，避免同步重入产生成功与旧拒绝交错。 */
    @Test
    void rejectionKeepsRegistrationUntilReplyReturns() {
        ControlledProcessor processor = new ControlledProcessor();
        processor.reject = true;
        RecordingClient client = new RecordingClient();
        client.onSend = () -> {
            processor.reject = false;
            processor.process(client, request(41));
        };
        processor.process(client, request(41));
        Assertions.assertEquals(1, processor.submissions);
        Assertions.assertEquals(List.of(20), client.errors);
        client.onSend = () -> {};
        processor.process(client, request(41));
        Assertions.assertEquals(2, processor.submissions, "拒绝后同序号可以重新提交");
    }

    /** Future 失败及提交返回前同步抛错都不能遗留登记。 */
    @Test
    void executionAndSynchronousSubmissionFailuresReleaseRegistration() {
        ControlledProcessor processor = new ControlledProcessor();
        RecordingClient client = new RecordingClient();
        processor.process(client, request(41));
        processor.results.getFirst().fail(new IllegalStateException("business failed"));
        processor.throwBeforeReturn = true;
        Assertions.assertThrows(IllegalStateException.class, () -> processor.process(client, request(41)));
        processor.throwBeforeReturn = false;
        processor.process(client, request(41));
        Assertions.assertEquals(3, processor.submissions);
        Assertions.assertTrue(client.errors.isEmpty());
    }

    /** 零/负序号保持原行为，不因新增正序号登记被合并或改号。 */
    @Test
    void nonPositiveSequencesKeepOriginalRejectionBehavior() {
        ControlledProcessor processor = new ControlledProcessor();
        processor.reject = true;
        RecordingClient client = new RecordingClient();
        for (int seq : new int[] {0, 0, -7, -7}) { processor.process(client, request(seq)); }
        Assertions.assertEquals(4, processor.submissions);
        Assertions.assertEquals(List.of(0, 0, -7, -7), client.sequences);
        Assertions.assertEquals(List.of(20, 20, 20, 20), client.errors);
    }

    /** 跨线程重发发生在拒绝写入尚未返回时，也只能观察原请求的一个拒绝。 */
    @Test
    void concurrentDuplicateDuringReplyDoesNotEnterQueue() throws InterruptedException {
        ControlledProcessor processor = new ControlledProcessor();
        processor.reject = true;
        RecordingClient client = new RecordingClient();
        CountDownLatch sending = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        client.onSend = () -> {
            sending.countDown();
            Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> release.await(5, TimeUnit.SECONDS)));
        };
        Thread first = Thread.ofVirtual().start(() -> {
            try { processor.process(client, request(41)); }
            catch (Throwable error) { failure.set(error); }
        });
        try {
            Assertions.assertTrue(sending.await(5, TimeUnit.SECONDS));
            processor.reject = false;
            processor.process(client, request(41));
            Assertions.assertEquals(1, processor.submissions);
        } finally {
            release.countDown();
            first.join(5000);
        }
        Assertions.assertFalse(first.isAlive());
        Assertions.assertNull(failure.get());
        Assertions.assertEquals(List.of(20), client.errors);
    }

    /** 旧请求的迟到清理不能撤销同序号的新登记。 */
    @Test
    void staleCompletionCannotReleaseNewRegistration() {
        RecordingClient client = new RecordingClient();
        IProtocol<?> oldRequest = request(41);
        IProtocol<?> nextRequest = request(41);
        Assertions.assertTrue(client.beginProtocolSubmission(oldRequest));
        client.endProtocolSubmission(oldRequest);
        Assertions.assertTrue(client.beginProtocolSubmission(nextRequest));
        client.endProtocolSubmission(oldRequest);
        Assertions.assertFalse(client.beginProtocolSubmission(request(41)));
        client.endProtocolSubmission(nextRequest);
        Assertions.assertTrue(client.beginProtocolSubmission(request(41)));
    }

    /** 拒绝写入抛错仍释放登记，后续重发不被永久压住。 */
    @Test
    void throwingRejectionReplyStillReleasesRegistration() {
        ControlledProcessor processor = new ControlledProcessor();
        processor.reject = true;
        RecordingClient client = new RecordingClient();
        client.onSend = () -> { throw new IllegalStateException("write failed"); };
        processor.process(client, request(41));
        client.onSend = () -> {};
        processor.reject = false;
        processor.process(client, request(41));
        Assertions.assertEquals(2, processor.submissions);
    }

    /** 同步 Error 保留类型并释放登记，不能转换为可以安全重投的未执行回包。 */
    @Test
    void synchronousErrorPreservesTypeAndReleasesRegistration() {
        ControlledProcessor processor = new ControlledProcessor();
        RecordingClient client = new RecordingClient();
        processor.errorBeforeReturn = new AssertionError("local failure");
        Assertions.assertSame(processor.errorBeforeReturn,
                Assertions.assertThrows(AssertionError.class, () -> processor.process(client, request(41))));
        processor.errorBeforeReturn = null;
        processor.process(client, request(41));
        Assertions.assertEquals(2, processor.submissions);
        Assertions.assertTrue(client.errors.isEmpty());
    }

    /** @param seq 原协议序号 @return 每次独立分配的请求 */
    private static IProtocol<?> request(int seq) { return new ByteArrayProtocol(1, new byte[0], seq); }

    /** 只控制调度边界，复用处理器网络入口及真实 GameClient。 */
    private static final class ControlledProcessor extends IdVirtualThreadProcessor {
        /** 可手动进入业务的任务。 */
        private final List<Runnable> actions = new ArrayList<>();
        /** 对应的实际完成信号。 */
        private final List<Promise<Void>> results = new ArrayList<>();
        /** 提交尝试次数。 */
        private int submissions;
        /** 实际业务次数。 */
        private int effects;
        /** 模拟队列拒绝。 */
        private boolean reject;
        /** 模拟 Future 建立前异常。 */
        private boolean throwBeforeReturn;
        /** 同步 Error 的身份保留探针。 */
        private Error errorBeforeReturn;

        /** 分发替身仍执行真实的旧序号检查。 */
        ControlledProcessor() {
            dispatcher = new Dispatcher() {
                @Override
                public void put(int module, Handler handler) { throw new UnsupportedOperationException(); }
                @Override
                public void dispatch(NetClient client, IProtocol<?> protocol) {
                    if (client.needProcess(protocol)) { effects++; }
                }
            };
        }

        @Override
        public Future<Void> processAsync(long id, Runnable task, boolean fast) {
            submissions++;
            if (errorBeforeReturn != null) { throw errorBeforeReturn; }
            if (throwBeforeReturn) { throw new IllegalStateException("before Future"); }
            if (reject) { return Future.failedFuture(new TaskNotExecutedException(new RejectedExecutionException("full"))); }
            Promise<Void> result = Promise.promise();
            actions.add(task);
            results.add(result);
            return result.future();
        }
    }

    /** 在最终发送边界记录响应，并允许构造发送期间的同步重入。 */
    private static final class RecordingClient extends GameClient {
        /** 观察到的协议错误码。 */
        private final List<Integer> errors = new ArrayList<>();
        /** 观察到的原序号。 */
        private final List<Integer> sequences = new ArrayList<>();
        /** 写入期间的测试动作。 */
        private Runnable onSend = () -> {};
        /** 不建立真实连接。 */
        RecordingClient() { super(null); }
        @Override
        public boolean sendProtocol(int msgId, int seq, byte[] data, int errorCode, boolean record) {
            Assertions.assertFalse(record, "拒绝/重放不能新增业务缓存");
            errors.add(errorCode);
            sequences.add(seq);
            onSend.run();
            return true;
        }
    }
}
