package cn.game.core.net.rpc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.game.core.exception.LogicException;
import cn.game.core.util.ExceptionHelper;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;

/** 通过真实 invoke 入口与可控传输结果验证四种返回接口，不启动集群或修改全局 Vert.x。 */
class RpcClientSemanticsTest {
    /** 请求模式均应应用相同响应规则，广播另行验证。 */
    private static final List<CallType> REQUEST_TYPES = List.of(CallType.PointToPoint, CallType.LoadBalancer);

    /** 仅提供反射所需签名，各返回类型实际走 RpcClient 的对应分支。 */
    private interface Contract {
        /** @return 同步结果 */
        Object sync();
        /** @return Vert.x 结果 */
        Future<Object> vertx();
        /** @return JDK 完成阶段 */
        CompletionStage<Object> stage();
        /** @return JDK Future 结果 */
        java.util.concurrent.Future<Object> jdk();
    }

    /** 普通对象与合法空响应在四种返回形式下保持原值。 */
    @Test
    void normalAndNullResponsesRemainSuccessful() throws Exception {
        for (CallType type : REQUEST_TYPES) {
            for (Method method : Contract.class.getDeclaredMethods()) {
                Object expected = new Object();
                Assertions.assertSame(expected, await(new StubClient(reply(expected)).invoke(type, method, new Object[0], "target", 7)));
                Assertions.assertNull(await(new StubClient(reply(null)).invoke(type, method, new Object[0], "target", 7)));
            }
        }
    }

    /** 直接或包装后的业务异常均保留业务错误码及信息，不作为成功响应体返回。 */
    @Test
    void businessErrorsFailEveryReturnType() {
        for (CallType type : REQUEST_TYPES) {
            for (Method method : Contract.class.getDeclaredMethods()) {
                for (boolean wrapped : List.of(false, true)) {
                    LogicException expected = new LogicException(12345, "business denied");
                    Object body = wrapped ? new RuntimeException("remote wrapper", expected) : expected;
                    Throwable failure = Assertions.assertThrows(Exception.class,
                            () -> await(new StubClient(reply(body)).invoke(type, method, new Object[0], "target", 7)));
                    Assertions.assertSame(expected, ExceptionHelper.findCause(failure, LogicException.class));
                    Assertions.assertEquals(12345, expected.getErrorCode());
                    Assertions.assertEquals("business denied", expected.getErrorMessage());
                }
            }
        }
    }

    /** 运行时和受检远端异常均失败，并保留原异常作为诊断原因。 */
    @Test
    void remoteExceptionsRetainTheirCause() {
        for (CallType type : REQUEST_TYPES) {
            for (Method method : Contract.class.getDeclaredMethods()) {
                for (Exception expected : List.of(new IOException("remote io"), new IllegalStateException("remote state"))) {
                    Throwable failure = Assertions.assertThrows(Exception.class,
                            () -> await(new StubClient(reply(expected)).invoke(type, method, new Object[0], "target", 7)));
                    Assertions.assertSame(expected, ExceptionHelper.findCause(failure, expected.getClass()));
                }
            }
        }
    }

    /** 远端 Error 是响应数据，转换为普通失败；不能作为本地致命 Error 直接逃逸。 */
    @Test
    void remoteErrorsAreWrappedConsistently() {
        for (Method method : Contract.class.getDeclaredMethods()) {
            AssertionError expected = new AssertionError("remote error");
            Throwable failure = Assertions.assertThrows(Exception.class,
                    () -> await(new StubClient(reply(expected)).invoke(CallType.PointToPoint, method, new Object[0], "target", 7)));
            Assertions.assertSame(expected, ExceptionHelper.findCause(failure, AssertionError.class));
        }
    }

    /** 传输超时、无消费者和接收方错误保持为传输失败，而非空的成功结果。 */
    @Test
    void transportFailuresRemainFailures() {
        for (Method method : Contract.class.getDeclaredMethods()) {
            for (ReplyFailure type : List.of(ReplyFailure.TIMEOUT, ReplyFailure.NO_HANDLERS, ReplyFailure.RECIPIENT_FAILURE)) {
                ReplyException expected = new ReplyException(type, 91, "transport failed");
                Throwable failure = Assertions.assertThrows(Exception.class,
                        () -> await(new StubClient(Future.failedFuture(expected)).invoke(CallType.PointToPoint, method, new Object[0], "target", 7)));
                Assertions.assertSame(expected, ExceptionHelper.findCause(failure, ReplyException.class));
            }
        }
    }

    /** 异步请求返回后才到达的业务异常也必须使等待者失败，不靠瞬时状态判断。 */
    @Test
    void lateBusinessFailureCompletesPendingCall() throws Exception {
        for (String name : List.of("vertx", "stage", "jdk")) {
            Promise<Message<Object>> response = Promise.promise();
            Object result = new StubClient(response.future()).invoke(CallType.PointToPoint,
                    Contract.class.getDeclaredMethod(name), new Object[0], "target", 7);
            if (result instanceof Future<?> future) { Assertions.assertFalse(future.isComplete()); }
            else { Assertions.assertFalse(((java.util.concurrent.Future<?>) result).isDone()); }
            LogicException expected = new LogicException(54321);
            response.complete(message(expected));
            Throwable failure = Assertions.assertThrows(Exception.class, () -> await(result));
            Assertions.assertSame(expected, ExceptionHelper.findCause(failure, LogicException.class));
        }
    }

    /** 异步广播维持无返回值语义，不发送需要响应的请求。 */
    @Test
    void broadcastDoesNotPretendToReceiveRemoteResult() throws Exception {
        for (String name : List.of("vertx", "stage", "jdk")) {
            StubClient client = new StubClient(Future.failedFuture("must not request"));
            Assertions.assertNull(await(client.invoke(CallType.Broadcast, Contract.class.getDeclaredMethod(name), new Object[0], "target", 7)));
            Assertions.assertEquals(1, client.broadcasts);
            Assertions.assertEquals(0, client.requests);
        }
    }

    /**
     * 在测试线程等待异步返回值，不修改生产线程的同步准入检查。
     * @param result invoke 的返回对象
     * @return 最终正常值，异步失败按 JDK 等待接口抛出
     */
    private Object await(Object result) throws Exception {
        if (result instanceof Future<?> future) { return future.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS); }
        if (result instanceof CompletionStage<?> stage) { return stage.toCompletableFuture().get(5, TimeUnit.SECONDS); }
        if (result instanceof java.util.concurrent.Future<?> future) { return future.get(5, TimeUnit.SECONDS); }
        return result;
    }

    /** @param body 传输成功后的响应体 @return 已完成的传输结果 */
    private Future<Message<Object>> reply(Object body) { return Future.succeededFuture(message(body)); }

    /** @param body 响应体 @return 仅实现 body 的消息替身，其他调用直接失败 */
    @SuppressWarnings("unchecked")
    private Message<Object> message(Object body) {
        return (Message<Object>) Proxy.newProxyInstance(Message.class.getClassLoader(), new Class<?>[] {Message.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("body")) { return body; }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    /** 只替换传输，默认解码和返回类型选择仍由正式 RpcClient 实现完成。 */
    private static final class StubClient implements RpcClient {
        /** 可立即完成或由用例稍后完成的传输结果。 */
        private final Future<Message<Object>> response;
        /** 已调用的请求次数，用于排除广播错误走请求路径。 */
        private int requests;
        /** 已发送的广播次数。 */
        private int broadcasts;
        /** @param response 本轮传输结果 */
        StubClient(Future<Message<Object>> response) { this.response = response; }
        @Override
        public <T> Future<Message<T>> request(String addr, T body) { return request(addr, body, new DeliveryOptions()); }
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> Future<Message<T>> request(String addr, T body, DeliveryOptions options) { requests++; return (Future) response; }
        @Override
        public <T> void broadcast(String addr, T body) { broadcasts++; }
        @Override
        public <T> void broadcast(String addr, T body, DeliveryOptions options) { broadcasts++; }
        @Override
        public <T> void send(String addr, T body) { throw new UnsupportedOperationException(); }
        @Override
        public <T> void send(T body) { throw new UnsupportedOperationException(); }
        @Override
        public <T> void fireAndForget(String addr, T body, DeliveryOptions options) { throw new UnsupportedOperationException(); }
        @Override
        public boolean checkAllowSync() { return true; }
    }
}
