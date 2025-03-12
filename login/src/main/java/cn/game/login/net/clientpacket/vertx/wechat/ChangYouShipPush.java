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
		try {
			HttpServerRequest request = context.request();
			HttpServerResponse response = context.response().putHeader("content-type", "text/json");
			JSONObject responseObject = new JSONObject();
//		log.info("receive changyou ship push");

			Consumer<?> successConsumer = r -> {
				responseObject.put("status", 1);
				response.end(Buffer.buffer(responseObject.toJSONString()));
			};

			String bodyAsString = context.getBodyAsString();
			log.info("receive changyou ship push, message body[{}]", bodyAsString);
			ChangYouPaymentNotification paymentNotification = ChangYouSdk.getInstance().parsePaymentNotification(bodyAsString);

			PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);

			PayOrder payOrder = mapper.selectByPrimaryKey(paymentNotification.getPushInfo().getGameOrderId());

			if (payOrder.getIsDeliver()) {
				log.warn("payOrder is deliver" + payOrder);
				successConsumer.accept(null);
				notifyUpdateOrder(paymentNotification);
				return;
			}
			// 校验一下价格
			if (payOrder.getPrice() / 100 != paymentNotification.getReceipt().getGoodsPrice()) {
				log.warn("price not match,orderId[{}] orderPrice[{}], notificationPrice[{}]", payOrder.getId(), payOrder.getPrice(),
						paymentNotification.getReceipt().getGoodsPrice());
				responseObject.put("status", 0);
				response.end(Buffer.buffer(responseObject.toJSONString()));
				return;
			}

			responseObject.put("status", 1);

			Future<Void> endFuture = response.end(Buffer.buffer(responseObject.toJSONString()));
			// 校验订单， 有鸟用？
			endFuture.compose(r -> ChangYouSdk.getInstance()
						.orderVerification(paymentNotification.getReceipt().getChannelId(), 0,
							paymentNotification.getReceiptJsonOriginal()))
					.compose(r -> {
				log.info("start game ship " + r);
				com.alibaba.fastjson.JSONObject object = com.alibaba.fastjson.JSON.parseObject(r);
				int state = object.getInteger("state");
				if (state != 200) {
					log.warn("orderVerification fail, state[{}]", state);
					return Future.failedFuture("orderVerification fail, state[" + state + "]");
				}
				String data = object.getString("data");
				if (!data.equals("0")) {
					log.warn("orderVerification fail, data[{}]", data);
					return Future.failedFuture("orderVerification fail, data[" + data + "]");
				}
				// 通知game发货
				User user = UserHelper.getUserByName(payOrder.getThirdUid());
				PaymentOrderShipRequest_7d000022 paymentOrderShipRequest_7d000022 = PaymentOrderShipRequest_7d000022.newBuilder()
						.setPlayerId(user.getId())
						.setUid(payOrder.getId())
						.build();
				String serverId = UserHelper.getServerId(user.getId());
				Future<PaymentOrderShipResponse_7d000023> future;
				if (StringUtils.isEmpty(serverId)) {
					future = VxHolder.requestRemoteServer(ServerType.Game, paymentOrderShipRequest_7d000022);
				} else {
					future = VxHolder.requestRemoteServer(serverId, paymentOrderShipRequest_7d000022);
				}
				return future;
			}).compose(r -> {
				log.info("wechat ship resp from game ret[{}]", r.getSuccess());
				if (!r.getSuccess()) {
					updatePayOrder(paymentNotification, payOrder);
					mapper.updateByPrimaryKeyWithBLOBs(payOrder);
					return Future.failedFuture("game ship fail");
				}
				// 发货成功， 更新订单交付状态
				if (!payOrder.getIsDeliver()) {
					payOrder.setIsDeliver(true);
					payOrder.setCompleteDate(DateUtil.nowDateStr());
					payOrder.setCompleteTime(DateUtil.nowTimeStr());
				}
				updatePayOrder(paymentNotification, payOrder);
				mapper.updateByPrimaryKeyWithBLOBs(payOrder);
				// 通知畅游sdk更新订单状态
				return notifyUpdateOrder(paymentNotification);
			}).onSuccess(r -> {
				log.info("wechat ship resp from game success : " + paymentNotification.getPushInfo().getGameOrderId());
			}).onFailure(r -> {
				log.error("wechat ship resp from game fail : " + paymentNotification.getPushInfo().getGameOrderId(), r);
				updatePayOrder(paymentNotification, payOrder);
				mapper.updateByPrimaryKeyWithBLOBs(payOrder);
			});
		} catch (Exception e) {
			log.error("ChangYouShipPush error " + e);
		}
	}

	private Future<String> notifyUpdateOrder(ChangYouPaymentNotification paymentNotification) {
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
