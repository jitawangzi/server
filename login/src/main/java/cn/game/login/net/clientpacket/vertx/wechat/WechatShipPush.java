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
		HttpServerRequest request = context.request();
		HttpServerResponse response = context.response().putHeader("content-type", "text/json");
		JSONObject responseObject = new JSONObject();
		log.info("receive wechat push");
		String signature = request.getParam("signature");
		String timestamp = request.getParam("timestamp");
		String nonce = request.getParam("nonce");
		String echostr = request.getParam("echostr");
		boolean checkRequest = WechatHelper.checkRequest(signature, timestamp, nonce);
		if (!StringUtils.isEmpty(echostr)) {
			log.info("receive wechat push ， echostr");
			if (checkRequest) {
				response.end(echostr);
			} else {
				response.end("error");
				log.error("微信测试失败");
			}
			return;
		}

		if (!checkRequest) {
			responseObject.put("ErrCode", 99998);
			responseObject.put("ErrMsg", "not from wechat");
			response.end(Buffer.buffer(responseObject.toJSONString()));
			log.error("ErrMsg,not from wechat");
			return;
		}

		Consumer<?> successConsumer = r -> {
			responseObject.put("ErrCode", 0);
			responseObject.put("ErrMsg", "Success");
			response.end(Buffer.buffer(responseObject.toJSONString()));
		};
		Consumer<?> failConsumer = r -> {
			responseObject.put("ErrCode", 99999);
			responseObject.put("ErrMsg", "internal error");
			response.end(Buffer.buffer(responseObject.toJSONString()));
		};

		String bodyAsString = context.body().asString();
		log.info("receive wechat ship push, message body[{}]", bodyAsString);
		WechatPushBean wechatPushBean = WechatHelper.parseWechatPushBean(bodyAsString);

		if (wechatPushBean.MiniGame.PayloadObj.OutTradeNo.equalsIgnoreCase("example_out_trade_no")) {
			successConsumer.accept(null);
			return;
		}

		PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
		PayOrder payOrder = mapper.selectByPrimaryKey(Long.parseLong(wechatPushBean.MiniGame.PayloadObj.OutTradeNo));

		if (payOrder.getIsDeliver()) {
			successConsumer.accept(null);
			return;
		}
		User user = UserHelper.getUserByName(wechatPushBean.MiniGame.PayloadObj.OpenId);
		PaymentOrderShipRequest_7d000022 paymentOrderShipRequest_7d000022 = PaymentOrderShipRequest_7d000022
				.newBuilder().setPlayerId(user.getId()).setUid(payOrder.getId()).build();
		String serverId = UserHelper.getServerId(user.getId());
		Future<PaymentOrderShipResponse_7d000023> future;
		if (StringUtils.isEmpty(serverId)) {
			future = VxHolder.requestRemoteServer(ServerType.Game, paymentOrderShipRequest_7d000022);
		} else {
			future = VxHolder.requestRemoteServer(serverId, paymentOrderShipRequest_7d000022);
		}
		future.onSuccess(r -> {
			log.info("wechat ship resp from game ret[{}]", r.getSuccess());
			if (r.getSuccess()) {
				if (!payOrder.getIsDeliver()) {
					payOrder.setIsDeliver(true);
					payOrder.setCompleteDate(DateUtil.nowDateStr()); 
					payOrder.setCompleteTime(DateUtil.nowTimeStr()) ; 
				}
				updatePayOrder(wechatPushBean, payOrder);
				mapper.updateByPrimaryKeyWithBLOBs(payOrder);
				successConsumer.accept(null);
			} else {
				failConsumer.accept(null);
				updatePayOrder(wechatPushBean, payOrder);
				mapper.updateByPrimaryKeyWithBLOBs(payOrder);
			}
		}).onFailure(r -> {
			log.error("wechat ship resp from game fail : " + wechatPushBean.MiniGame.PayloadObj.OutTradeNo, r);
			failConsumer.accept(null);
			updatePayOrder(wechatPushBean, payOrder);
			mapper.updateByPrimaryKeyWithBLOBs(payOrder);
		});

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
