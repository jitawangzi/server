package cn.game.games.net.game.module.recharge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.Message;
import cn.game.core.exception.LogicException;
import cn.game.core.net.client.NetClient;
import cn.game.core.util.ExceptionHelper;
import cn.game.games.net.client.GameClient;
import cn.game.protocol.protobuf.PbProtocol;

/** 购买回调固定原连接和请求序号，避免支付等待期间的新请求接收旧结果；不负责发奖回滚或订单恢复。 */
public final class PaymentReply {
    /** 记录支付或发奖失败的完整异常及玩家上下文。 */
    private static final Logger log = LoggerFactory.getLogger(PaymentReply.class);
    /** 发起购买的原连接，不随玩家重连切换。 */
    private final NetClient client;
    /** 进入购买处理时的请求序号；无序号客户端不使用该值。 */
    private final int sequence;

    /** @param client 当前购买请求的连接，必须在调用异步支付之前捕获 */
    public PaymentReply(NetClient client) {
        this.client = client;
        this.sequence = client instanceof GameClient game ? game.getCurMessageSeq() : 0;
    }

    /** @param response 已构建的成功响应，发送及缓存均归属原请求 */
    public void send(Message response) { send(response, 0); }

    /** @param response 已构建的响应 @param error 协议错误码，零表示成功 */
    public void send(Message response, int error) {
        if (client instanceof GameClient game) {
            int messageId = PbProtocol.getInstance().getMsgId(response.getClass().getSimpleName());
            game.sendProtocol(messageId, sequence, response.toByteArray(), error, true);
        } else { client.sendProtocol(response, error); }
    }

    /**
     * @param response 本次购买的响应协议
     * @param failure 支付 Future 或后续业务调用的异常
     * @param fallback 未知错误沿用该购买入口原有错误码
     */
    public void fail(Message response, Throwable failure, int fallback) {
        log.warn("购买失败 playerId={} response={}", client.getPlayerId(), response.getDescriptorForType().getFullName(), failure);
        LogicException logic = ExceptionHelper.findCause(failure, LogicException.class);
        int error = logic == null ? fallback : logic.getErrorCode();
        // 沿用既有 Player.handleFail 对数字错误消息的识别，超出整数范围时仍返回明确失败。
        if (logic == null && failure.getMessage() != null && failure.getMessage().matches("[0-9]+")) {
            try { error = Integer.parseInt(failure.getMessage()); }
            catch (NumberFormatException invalid) { log.warn("购买错误码超出整数范围 playerId={}", client.getPlayerId(), invalid); }
        }
        send(response, error == 0 ? fallback : error);
    }
}
