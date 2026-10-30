package cn.game.core.execute;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcClient;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** 仅替换RPC传输，计时、返回类型分流和远端响应解码均走正式默认方法。 */
class RpcTimingTest {
    /** 反射所需的最小异步契约。 */
    public interface Contract { /** @return 远端结果 */ Future<Object> call(); }
    /** 恢复关闭窗口。 */
    @AfterEach void reset() { TimingMetrics.reset(false); }
    /** 异步远端业务失败只算失败，不能因为传输成功就算成功。 */
    @Test void decodedBusinessFailureIsNotTransportSuccess() throws Exception {
        TimingMetrics metrics = TimingMetrics.reset(true);
        Promise<Message<Object>> transport = Promise.promise();
        Future<?> result = invoke(client(transport.future(), null));
        var expected = new cn.game.core.exception.LogicException(12345);
        transport.complete(message(expected));
        Assertions.assertSame(expected, result.cause());
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Outcome.FAILURE));
        Assertions.assertEquals(0, count(metrics, TimingMetrics.Outcome.SUCCESS));
    }
    /** 同步提交异常保留原对象并记录一次，未产生可等待的远端Future。 */
    @Test void synchronousRpcSubmissionFailurePreservesIdentity() {
        TimingMetrics metrics = TimingMetrics.reset(true);
        IllegalStateException expected = new IllegalStateException("synthetic transport");
        Assertions.assertSame(expected, Assertions.assertThrows(IllegalStateException.class, () -> invoke(client(null, expected))));
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Outcome.FAILURE));
    }
    /** 远端传输超时与普通失败不同，且不能据此宣称远端实际任务已停止或迟到完成。 */
    @Test void rpcTransportTimeoutIsSeparateWithoutInventedRemoteCompletion() throws Exception {
        TimingMetrics metrics = TimingMetrics.reset(true);
        ReplyException expected = new ReplyException(ReplyFailure.TIMEOUT, "synthetic timeout");
        Future<?> result = invoke(client(Future.failedFuture(expected), null));
        Assertions.assertSame(expected, result.cause());
        Assertions.assertEquals(1, count(metrics, TimingMetrics.Outcome.TIMEOUT));
        Assertions.assertEquals(0, count(metrics, TimingMetrics.Outcome.LATE_SUCCESS));
        Assertions.assertEquals(0, count(metrics, TimingMetrics.Outcome.LATE_FAILURE));
    }
    /** @param client 受控传输 @return 正式invoke的异步结果 @throws Exception 反射签名缺失 */
    private static Future<?> invoke(RpcClient client) throws Exception {
        return (Future<?>) client.invoke(CallType.PointToPoint, Contract.class.getMethod("call"), new Object[0], "synthetic", 1);
    }
    /** @param response 受控响应 @param failure 同步失败 @return 执行真实默认方法的客户端 */
    private static RpcClient client(Future<Message<Object>> response, RuntimeException failure) {
        return (RpcClient) Proxy.newProxyInstance(RpcClient.class.getClassLoader(), new Class<?>[]{RpcClient.class}, (proxy, method, args) -> {
            if (method.isDefault()) { return InvocationHandler.invokeDefault(proxy, method, args); }
            if (method.getName().equals("request")) { if (failure != null) { throw failure; } return response; }
            if (method.getName().equals("checkAllowSync")) { return true; }
            throw new UnsupportedOperationException(method.getName());
        });
    }
    /** @param body 原响应体 @return 只替换传输的消息，解码仍由生产代码负责 */
    @SuppressWarnings("unchecked")
    private static Message<Object> message(Object body) {
        return (Message<Object>) Proxy.newProxyInstance(Message.class.getClassLoader(), new Class<?>[]{Message.class}, (p, m, a) -> {
            if (m.getName().equals("body")) { return body; } throw new UnsupportedOperationException(m.getName());
        });
    }
    /** @param metrics 窗口 @param outcome 结果 @return RPC计数 */
    private static long count(TimingMetrics metrics, TimingMetrics.Outcome outcome) {
        return metrics.snapshot().rows().stream().filter(r -> r.stage() == TimingMetrics.Stage.RPC_WAIT && r.outcome() == outcome).mapToLong(TimingMetrics.Row::count).sum();
    }
}
