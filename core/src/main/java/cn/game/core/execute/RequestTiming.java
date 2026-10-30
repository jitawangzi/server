package cn.game.core.execute;

import java.util.concurrent.atomic.AtomicBoolean;
import io.vertx.core.Future;

/**
 * 服务端已解码协议入口至首次同连接同序号响应写入完成的观测。
 * 上下文只在同步dispatch/reject调用期间绑定，finally恢复，异步写入回调只捕获计时标量。
 * 无业务身份索引；未在作用域内发起的异步响应不推测关联，也不声称客户端已收到。
 */
public final class RequestTiming {
    /** 线程复用与嵌套调用时恢复上一作用域，不跨线程隐式传播。 */
    private static final ThreadLocal<Binding> local = new ThreadLocal<>();
    /** 关闭时不为每个请求创建计时、原子变量或线程上下文。 */
    private static final RequestTiming DISABLED = new RequestTiming();
    /** 请求观察绑定窗口与固定路由。 */
    private final TimingMetrics.Span span;
    /** 仅关联协议约定的紧随请求号响应及通用错误；特殊协议不猜测响应。 */
    private final int responseMessageId;
    /** 首次匹配响应取得观察权，推送和后续同序号响应不重复计数。 */
    private final AtomicBoolean response = new AtomicBoolean();
    /** 构造永不绑定上下文的关闭实例。 */
    private RequestTiming() { span = null; responseMessageId = 0; }
    /** @param messageId 原请求号 @return 关闭时共享无操作实例，开启时独立请求计时 */
    public static RequestTiming start(int messageId) {
        return TimingMetrics.current().enabled() ? new RequestTiming(messageId) : DISABLED;
    }
    /** @param messageId 协议号，只映射到三个固定桶 */
    public RequestTiming(int messageId) {
        responseMessageId = messageId + 1;
        TimingMetrics.Route route = messageId == 0x01000005 ? TimingMetrics.Route.HEARTBEAT
                : messageId == 0x01000001 ? TimingMetrics.Route.LOGIN : TimingMetrics.Route.OTHER;
        span = TimingMetrics.current().begin(TimingMetrics.Stage.REQUEST, route);
    }
    /** @param client 仅作用域内引用，用身份比较隔离连接 @param sequence 原请求序号 @param action 同步处理动作 */
    public void run(Object client, int sequence, Runnable action) {
        if (span == null || !span.enabled()) { action.run(); return; }
        Binding previous = local.get();
        local.set(new Binding(client, sequence, this));
        try { action.run(); }
        finally { if (previous == null) { local.remove(); } else { local.set(previous); } }
    }
    /** @param client 原连接 @param sequence 实际发送序号 @param messageId 实际发送协议号 @return 仅首次匹配返回观察器，否则null */
    public static RequestTiming response(Object client, int sequence, int messageId) {
        Binding binding = local.get();
        if (binding == null || binding.client != client || binding.sequence != sequence
                || messageId != binding.timing.responseMessageId && messageId != 0x01000099
                || !binding.timing.response.compareAndSet(false, true)) { return null; }
        return binding.timing;
    }
    /** @param write 原写入Future，不改变其完成顺序和结果 @param errorCode 业务协议错误码 */
    public void written(Future<Void> write, int errorCode) {
        write.onComplete(result -> span.end(result.succeeded() && errorCode == 0
                ? TimingMetrics.Outcome.SUCCESS : TimingMetrics.Outcome.FAILURE));
    }
    /** 同步发送异常或未连接时记录失败。 */
    public void failed() { if (span != null) { span.end(TimingMetrics.Outcome.FAILURE); } }
    /** @param rejected 是否未执行就拒绝；拒绝单独统计，不能伪报业务失败或成功 */
    public void finished(boolean rejected) {
        if (span == null) { return; }
        if (rejected) { span.end(TimingMetrics.Outcome.REJECTED); }
        else if (!response.get()) { span.end(TimingMetrics.Outcome.NO_RESPONSE); }
    }
    /** 仅本次调用期间引用连接，无全局按玩家或序号增长的容器。 */
    private record Binding(Object client, int sequence, RequestTiming timing) {}
}
