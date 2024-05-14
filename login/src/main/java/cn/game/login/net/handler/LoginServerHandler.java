package cn.game.login.net.handler;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.cache.CacheType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.login.net.clientpacket.vertx.UserHelper;
import cn.game.login.net.clientpacket.vertx.wechat.WechatHelper;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg.PaymentOrderProto;
import cn.game.protocol.protobuf.BaseMsg.PaymentOrderProto.Builder;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ServerMsg.GameStatusPublish_7d000017;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidRequest_7d000018;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidResponse_7d000019;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateRequest_7d000020;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateResponse_7d000021;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.RedissonUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;

/**
 * 服务器之间的消息处理器
 */
@Component
public class LoginServerHandler extends BaseHandler {

	@Override
	protected int getModule() {
		return 0x7d;
	}
	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.GameStatusPublish_7d000017, this::gameStatus);
		putInvoker(PbProtocol.LoginPlayerUidRequest_7d000018, this::uid);
		putInvoker(PbProtocol.PaymentOrderCreateRequest_7d000020, this::paymentCreate);

	}

	protected void paymentCreate(NetClient client, Object message) {
		PaymentOrderCreateRequest_7d000020 request = (PaymentOrderCreateRequest_7d000020) message;
		PaymentOrderCreateResponse_7d000021.Builder resp = PaymentOrderCreateResponse_7d000021.newBuilder() ; 
		long playerId = request.getPlayerId();
		String sessionId = request.getSessionId(); 
		
		JSONObject signData = new JSONObject(); 
		// game? 
		signData.put("mode", "item") ; 
		signData.put("offerId", Config.wechat_midas_offerId) ; 
		signData.put("buyQuantity", 1) ; 
		signData.put("env", Config.wechat_midas_env) ; 
		signData.put("currencyType", "CNY") ;
		// ? 
		signData.put("productId", 1) ; 
		signData.put("goodsPrice", request.getGoodsPrice()) ; 
		long outTradeNo = IdUtil.genOrderId(playerId); 
		signData.put("outTradeNo", outTradeNo) ; 
		
		User user = UserHelper.getUserBySessionId(sessionId); 
		// 创建一个订单
		PayOrder payOrder = new PayOrder() ; 
		payOrder.setId(outTradeNo);
		payOrder.setCreateDate(DateUtil.nowDateStr());
		payOrder.setCreateTime(DateUtil.nowTimeStr());
		payOrder.setEnv(Config.wechat_midas_env);
		payOrder.setIsDeliver(false);
		payOrder.setPayState((byte) 1);
		payOrder.setPlayerId(playerId);
		payOrder.setUserId(playerId);
		payOrder.setThirdUid(user.getThirdUid());
//		ObjUtil.setDefaultValue(payOrder); 
		
		VxHolder.vertx.executeBlocking(r -> {
			PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class) ; 
			mapper.insert(payOrder); 
			
			String rawDate = signData.toJSONString();
			//? 
			String paySig = WechatHelper.calcPaymentGameItemPaySig(rawDate); 
			
			String signature = WechatHelper.calcSignature(rawDate, user.getSessionKey()); 
			
			Builder newBuilder = PaymentOrderProto.newBuilder(); 
			newBuilder.setSignData(rawDate); 
			newBuilder.setPaySig(paySig); 
			newBuilder.setSignature(signature); 
			resp.setOrderId(outTradeNo); 
			
			client.sendProtocol(resp.setOrder(newBuilder).build());
		}).onFailure(e -> {
			resp.setOrderId(outTradeNo); 
			client.sendProtocol(resp.build());

//			client.sendProtocol(ExceptionUtils.getFullStackTrace(e),1) ; 
		});
	}
	protected void uid(NetClient client, Object message) {
		LoginPlayerUidRequest_7d000018 request = (LoginPlayerUidRequest_7d000018) message;
		String passportSessionId = request.getPassportSessionId();

		// 查询用户
		RedissonUtil.getAndRunAsync(CacheType.PASSPORT_SESSION.key(passportSessionId), retU -> {
			if (retU == null) {
				client.sendProtocol(LoginPlayerUidResponse_7d000019.getDefaultInstance(),
						ErrorMsgEnum.session_not_exist.getId());
				return;
			}
			User u = (User) retU;
			client.sendProtocol(
					LoginPlayerUidResponse_7d000019.newBuilder().setUid(u.getId()).setAccountId(u.getThirdUid()).setDeviceId(u.getUsername()).build());
		});
	}
	protected void gameStatus(NetClient client, Object message) {
		GameStatusPublish_7d000017 request = (GameStatusPublish_7d000017) message;
		String serverId = request.getServerId();
		int onlinePlayerCount = request.getOnlinePlayerCount();
		ActiveServerListManager.getInstance().setPlayerCount(serverId, onlinePlayerCount);
		ActiveServerListManager.getInstance().addServer(serverId, ServerType.Game.name());
	}
}
