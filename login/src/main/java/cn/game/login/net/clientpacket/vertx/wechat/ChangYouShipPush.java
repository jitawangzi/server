package cn.game.login.net.clientpacket.vertx.wechat;

import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import cn.game.core.net.vertx.VxHolder;
import cn.game.core.sdk.ChangYouPaymentNotification;
import cn.game.core.sdk.ChangYouSdk;
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
 * 畅游sdk发货通知
 * 2025年2月25日 11:52:05
 * @author SYQ
 */
@Component
public class ChangYouShipPush implements BaseVertxHandler {

	private static final Logger log = LoggerFactory.getLogger(ChangYouShipPush.class);

    @Override
    public void handle(RoutingContext context) {
        var response = context.response().putHeader("content-type", "text/json");
        try {
            ChangYouPaymentNotification notification = ChangYouSdk.getInstance().parsePaymentNotification(context.body().asString());
            processNotification(notification).onComplete(result -> {
                if (result.failed()) { log.warn("畅游回执未完成，返回失败等待平台核对或重试", result.cause()); }
                response.end(new JSONObject().fluentPut("status", result.succeeded() ? 1 : 0).toJSONString());
            });
        } catch (Exception failure) {
            log.warn("畅游回执解析失败", failure);
            response.end("{\"status\":0}");
        }
    }

    /** @param notification 已解析回执 @return 验单、Game 保存、Login 单调落库及平台更新均成功后的结果 */
    protected Future<Void> processNotification(ChangYouPaymentNotification notification) {
        try {
            PayOrderMapper mapper = paymentMapper();
            PayOrder order = mapper.selectByPrimaryKey(notification.getPushInfo().getGameOrderId());
            var receipt = notification.getReceipt();
            // SDK 示例使用字符串 SKU，不能假设它等于内部 itemId 或强制数量为 1。
            // 沿用原验单链，并核对本地身份、精确金额和已绑定单号；商品映射仍需平台正式契约。
            if (!PaymentOrderState.canShip(order) || receipt == null || StringUtils.isBlank(receipt.getOrderId())
                    || !String.valueOf(order.getPlayerId()).equals(receipt.getRoleId())
                    || !java.util.Objects.equals(order.getThirdUid(), receipt.getUserid())
                    || !Double.isFinite(receipt.getGoodsPrice()) || order.getPrice() == null
                    || java.math.BigDecimal.valueOf(receipt.getGoodsPrice()).movePointRight(2)
                            .compareTo(java.math.BigDecimal.valueOf(order.getPrice())) != 0
                    || (StringUtils.isNotEmpty(order.getThirdOrderId()) && !order.getThirdOrderId().equals(receipt.getOrderId()))
                    || (StringUtils.isNotEmpty(order.getTransactionId()) && !order.getTransactionId().equals(receipt.getOrgOrderId()))) {
                return Future.failedFuture("PAYMENT_NOTIFICATION_MISMATCH");
            }
            return verifyOrder(notification).compose(raw -> {
                JSONObject verified = JSON.parseObject(raw);
                if (!Integer.valueOf(200).equals(verified.getInteger("state")) || !"0".equals(verified.getString("data"))) {
                    return Future.failedFuture("PAYMENT_VERIFICATION_FAILED");
                }
                return shipOrder(PaymentOrderShipRequest_7d000022.newBuilder().setPlayerId(order.getPlayerId())
                        .setUid(order.getId()).setSdkOrderId(receipt.getOrderId()).build());
            }).compose(result -> {
                updatePayOrder(notification, order);
                // 失败也仅记录支付事实，不可回退已由其他通知完成的交付状态。
                PaymentOrderState.record(mapper, order, result.getSuccess());
                return result.getSuccess() ? notifyUpdateOrder(notification).mapEmpty()
                        : Future.failedFuture("PAYMENT_GAME_NOT_COMMITTED");
            });
        } catch (Exception failure) {
            log.warn("畅游回执受理失败", failure);
            return Future.failedFuture(failure);
        }
    }

    /** @return 当前订单存储；测试只替换外部边界 */
    protected PayOrderMapper paymentMapper() { return SpringContextLoader.getContext().getBean(PayOrderMapper.class); }

    /** @param notification 已校验本地身份的回执 @return SDK 验单原始结果 */
    protected Future<String> verifyOrder(ChangYouPaymentNotification notification) {
        return ChangYouSdk.getInstance().orderVerification(notification.getReceipt().getChannelId(), 0, notification.getReceiptJsonOriginal());
    }

    /** @param request 原订单身份 @return Game 的持久化确认结果 */
    protected Future<PaymentOrderShipResponse_7d000023> shipOrder(PaymentOrderShipRequest_7d000022 request) {
        String serverId = UserHelper.getServerId(request.getPlayerId());
        return StringUtils.isEmpty(serverId) ? VxHolder.requestRemoteServer(ServerType.Game, request)
                : VxHolder.requestRemoteServer(serverId, request);
    }

	/** @param paymentNotification 已由 Game 和 Login 确认的订单 @return 平台状态更新原始结果；不假设重试或幂等保证 */
	protected Future<String> notifyUpdateOrder(ChangYouPaymentNotification paymentNotification) {
		JSONObject orderObject = new JSONObject();
		orderObject.put("orderedId", paymentNotification.getReceipt().getOrderId());
		JSONArray jsonArray = new JSONArray();
		jsonArray.add(orderObject);
		JSONObject dataObject = new JSONObject();
		dataObject.put("orders", jsonArray);
		if (paymentNotification.getReceipt().isAd()) {
			dataObject.put("ad_flag", true);
		}
		return ChangYouSdk.getInstance().orderUpdate(paymentNotification.getReceipt().getChannelId(), 0, dataObject.toJSONString());
	}

	private void updatePayOrder(ChangYouPaymentNotification paymentNotification, PayOrder payOrder) {
		if (payOrder.getCallback() == null) {
			payOrder.setCallback(JSON.toJSONString(paymentNotification));
		}
		if (StringUtils.isEmpty(payOrder.getThirdOrderId())) {
			payOrder.setThirdOrderId(paymentNotification.getReceipt().getOrderId());
		}
		if (StringUtils.isEmpty(payOrder.getTransactionId())) {
			payOrder.setTransactionId(paymentNotification.getReceipt().getOrgOrderId());
		}
		if (payOrder.getPayState() == 1) {
			payOrder.setPayState((byte) 2);
			payOrder.setPayDate(DateUtil.nowDateStr());
			payOrder.setPayTime(DateUtil.nowTimeStr());
		}
	}

	@Override
	public String getPath() {
		return "/changyou/ship/push";
	}
}
