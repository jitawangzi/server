package cn.game.login.net.clientpacket.vertx.wechat;


import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import cn.game.core.net.vertx.VxHolder;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.login.net.clientpacket.vertx.BaseVertxHandler;
import cn.game.login.net.clientpacket.vertx.UserHelper;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderShipRequest_7d000022;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderShipResponse_7d000023;
import cn.game.util.DateUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**    
 * 微信平台发货推送消息
 * 2024年3月26日 下午2:26:34
 * @author SYQ
 */
@Component
public class WechatShipPush implements BaseVertxHandler {

	protected static final Logger log = LoggerFactory.getLogger(WechatShipPush.class);

	private static final String WX_URL_STRING = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
	
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "text/json");
        try {
            var request = context.request();
            boolean valid = checkRequest(request.getParam("signature"), request.getParam("timestamp"), request.getParam("nonce"));
            String echo = request.getParam("echostr");
            if (StringUtils.isNotEmpty(echo)) { response.end(valid ? echo : "error"); return; }
            if (!valid) { reply(response, 99998); return; }
            WechatPushBean bean = WechatHelper.parseWechatPushBean(context.body().asString());
            var payload = bean.MiniGame.PayloadObj;
            if (payload == null) { throw new IllegalArgumentException("PAYMENT_PAYLOAD_REQUIRED"); }
            if ("example_out_trade_no".equalsIgnoreCase(payload.OutTradeNo)) { reply(response, 0); return; }
            PayOrderMapper mapper = paymentMapper();
            PayOrder order = mapper.selectByPrimaryKey(Long.parseLong(payload.OutTradeNo));
            var info = payload.WeChatPayInfo;
            // 创单保存的是 goodsPrice 原价分值；核对 OrigPrice，允许 ActualPrice 因优惠低于原价。
            if (!PaymentOrderState.canShip(order) || info == null || payload.GoodsInfo == null
                    || StringUtils.isBlank(payload.OpenId) || !java.util.Objects.equals(order.getThirdUid(), payload.OpenId)
                    || StringUtils.isBlank(info.MchOrderNo) || StringUtils.isBlank(info.TransactionId)
                    || !java.util.Objects.equals(order.getPrice(), payload.GoodsInfo.OrigPrice)
                    || payload.GoodsInfo.ActualPrice == null || payload.GoodsInfo.ActualPrice < 0
                    || !PaymentOrderState.matches(order.getMchOrderNo(), info.MchOrderNo)
                    || !PaymentOrderState.matches(order.getTransactionId(), info.TransactionId)) {
                reply(response, 99999); return;
            }
            updatePayOrder(bean, order);
            // 先以条件 SQL 绑定支付事实，冲突并发回执不得先到 Game 再被 Login 拒绝。
            PaymentOrderState.record(mapper, order, false);
            var ship = PaymentOrderShipRequest_7d000022.newBuilder().setPlayerId(order.getPlayerId()).setUid(order.getId())
                    .setSdkOrderId(PaymentOrderState.sdkOrderId(order)).build();
            shipOrder(ship).map(result -> {
                PaymentOrderState.record(mapper, order, result.getSuccess());
                return result.getSuccess() ? 0 : 99999;
            }).onSuccess(code -> reply(response, code)).onFailure(failure -> {
                log.warn("微信发货或订单保存未确认 orderId={}", order.getId(), failure);
                reply(response, 99999);
            });
        } catch (Exception failure) {
            log.warn("微信回执解析或受理失败", failure);
            reply(response, 99999);
        }
    }

    /** @param response 当前 HTTP 响应 @param code 微信约定的处理结果 */
    private void reply(HttpServerResponse response, int code) {
        response.end(Buffer.buffer(new JSONObject().fluentPut("ErrCode", code).fluentPut("ErrMsg", code == 0 ? "Success" : "internal error").toJSONString()));
    }

    /** @param signature 请求签名 @param timestamp 时间戳 @param nonce 随机串 @return 是否通过原微信签名检查 */
    protected boolean checkRequest(String signature, String timestamp, String nonce) { return WechatHelper.checkRequest(signature, timestamp, nonce); }

    /** @return 当前订单存储 */
    protected PayOrderMapper paymentMapper() { return SpringContextLoader.getContext().getBean(PayOrderMapper.class); }

    /** @param request 已绑定支付事实的发货请求 @return Game 原提交确认结果 */
    protected Future<PaymentOrderShipResponse_7d000023> shipOrder(PaymentOrderShipRequest_7d000022 request) {
        String serverId = UserHelper.getServerId(request.getPlayerId());
        return StringUtils.isEmpty(serverId) ? VxHolder.requestRemoteServer(ServerType.Game, request) : VxHolder.requestRemoteServer(serverId, request);
    }

	private void updatePayOrder(WechatPushBean wechatPushBean, PayOrder payOrder) {
		if (payOrder.getCallback() == null) {
			payOrder.setCallback(JSON.toJSONString(wechatPushBean));
		}
		if (StringUtils.isEmpty(payOrder.getMchOrderNo())) {
			payOrder.setMchOrderNo(wechatPushBean.MiniGame.PayloadObj.WeChatPayInfo.MchOrderNo);
		}
		if (StringUtils.isEmpty(payOrder.getTransactionId())) {
			payOrder.setTransactionId(wechatPushBean.MiniGame.PayloadObj.WeChatPayInfo.TransactionId);
		}
		if (payOrder.getPayState() == 1) {
			payOrder.setPayState((byte) 2); 
			payOrder.setPayDate(DateUtil.nowDateStr()); 
			payOrder.setPayTime(DateUtil.nowTimeStr()) ; 
		}
	}

	@Override
	public String getPath() {
		return "/wechat/ship/push";
	}
}
