package cn.game.core.net.rpc.vertx;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.game.core.execute.TaskNotExecutedException;
import cn.game.core.net.process.DirectProcessor;
import cn.game.core.net.process.Processor;
import cn.game.core.net.transport.Command;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.ServerType;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyFailure;

/** 在真实 RPC 回复链中区分当前提交拒绝与已执行业务返回的嵌套拒绝。 */
class RpcRejectionOriginTest {
    /** 已产生副作用后立即返回嵌套拒绝，不能被报告成整个 RPC 未执行。 */
    @Test
    void completedNestedRejectionIsExecutionFailure() {
        assertNestedRejection(false);
    }

    /** 异步业务稍后失败仍只回一次执行失败，不能把内层拒绝归给外层。 */
    @Test
    void pendingNestedRejectionIsExecutionFailure() {
        assertNestedRejection(true);
    }

    /** @param delayed 是否在 handleMessage 返回后完成业务 Future */
    private void assertNestedRejection(boolean delayed) {
        Promise<Object> business = Promise.promise();
        if (!delayed) { business.fail(new TaskNotExecutedException(new RejectedExecutionException("inner"))); }
        AtomicInteger effects = new AtomicInteger();
        VertxRPCService<Object> service = service(new DirectProcessor(), effects, business.future());
        ReplyProbe probe = new ReplyProbe();
        service.handleMessage(probe.message());
        Assertions.assertEquals(1, effects.get());
        if (delayed) {
            Assertions.assertTrue(probe.errors.isEmpty());
            business.fail(new TaskNotExecutedException(new RejectedExecutionException("inner")));
        }
        Assertions.assertEquals(List.of(ReplyFailure.ERROR.toInt()), probe.errors);
        Assertions.assertEquals(0, probe.replies);
        Assertions.assertEquals(1, effects.get(), "不得自动重试已经执行的业务");
    }

    /** 真实外层提交未执行时仍返回拒绝，不因修复而丢失背压反馈。 */
    @Test
    void outerSubmissionRejectionStillReportsNotExecuted() {
        AtomicInteger effects = new AtomicInteger();
        Processor rejecting = new DirectProcessor() {
            @Override
            public Future<Void> processAsync(long id, Runnable task, boolean fast) {
                return Future.failedFuture(new TaskNotExecutedException(new RejectedExecutionException("outer")));
            }
        };
        ReplyProbe probe = new ReplyProbe();
        service(rejecting, effects, Future.succeededFuture("unused")).handleMessage(probe.message());
        Assertions.assertEquals(0, effects.get());
        Assertions.assertEquals(List.of(ErrorMsgEnum.system_overload.getId()), probe.errors);
        Assertions.assertEquals(0, probe.replies);
    }

    /** @param processor 当前调度器 @param effects 执行次数 @param business 业务异步结果 @return 真实回复实现的服务 */
    private VertxRPCService<Object> service(Processor processor, AtomicInteger effects, Future<Object> business) {
        return new VertxRPCService<>(null, "test", ServerType.Game, processor) {
            @Override
            protected Object invokeCommand(Command command) {
                effects.incrementAndGet();
                return business;
            }
        };
    }

    /** 仅替换网络写入，记录真实 RPC 实现发出的终态。 */
    private static final class ReplyProbe {
        /** 失败协议码。 */
        private final List<Integer> errors = new ArrayList<>();
        /** 正常回复次数。 */
        private int replies;
        /** @return 输入及输出均由本用例控制的消息 */
        @SuppressWarnings("unchecked")
        Message<Object> message() {
            return (Message<Object>) Proxy.newProxyInstance(Message.class.getClassLoader(), new Class<?>[] {Message.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "body" -> new Command("example.Service", "change", new Class<?>[0], new Object[0], 1);
                        case "headers" -> MultiMap.caseInsensitiveMultiMap();
                        case "replyAddress" -> "test.reply";
                        case "fail" -> { errors.add((Integer) args[0]); yield null; }
                        case "reply" -> { replies++; yield null; }
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }
    }
}
