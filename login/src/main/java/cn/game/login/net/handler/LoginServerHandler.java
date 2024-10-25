package cn.game.login.net.handler;

import cn.game.login.cache.entity.GmOpt;
import cn.game.login.mapper.GmOptMapper;
import cn.game.login.net.clientpacket.vertx.gm.IpWhitelistManger;
import cn.game.login.net.clientpacket.vertx.gm.NoticeManger;
import cn.game.login.net.clientpacket.vertx.wechat.AndroidPayOrderProcessor;
import cn.game.login.net.clientpacket.vertx.wechat.BasePayOrderProcessor;
import cn.game.login.net.clientpacket.vertx.wechat.IOSPayOrderProcessor;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.*;
import io.vertx.core.Future;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 服务器之间的消息处理器
 */
@Component
public class LoginServerHandler extends BaseHandler {

	public static final int UPDATE_WHITE_LIST = 1;
	public static final int UPDATE_NOTICE = 2;

	Map<String,BasePayOrderProcessor> payOrderProcessorMap = new HashMap<>();
	@Override
	protected int getModule() {
		return 0x7d;
	}
	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.GameStatusPublish_7d000017, this::gameStatus);
		putInvoker(PbProtocol.LoginPlayerUidRequest_7d000018, this::uid);
		putInvoker(PbProtocol.PaymentOrderCreateRequest_7d000020, this::paymentCreate);
		putInvoker(PbProtocol.GmOptRecordRequest_7d000052, LoginServerHandler::addGmOptRecord);
		putInvoker(PbProtocol.LoginUpdateIOSAccessTokenRequest_7d000074, LoginServerHandler::updateIOSAccessToken);
		putInvoker(PbProtocol.LoginUpdateGmInfoRequest_7d000076, this::updateGmInfo);

		registerPayOrderProcessor(new AndroidPayOrderProcessor());
		registerPayOrderProcessor(new IOSPayOrderProcessor());
	}

	private void updateGmInfo(NetClient client, Object o) {
		ServerMsg.LoginUpdateGmInfoRequest_7d000076 req = (ServerMsg.LoginUpdateGmInfoRequest_7d000076) o;
		if (req.getType() == UPDATE_WHITE_LIST){
			IpWhitelistManger.getInstance().refreshIpWhitelistList();
			log.info(String.format("updateGmInfo refreshIpWhitelistList"));
		}else if (req.getType() == UPDATE_NOTICE){
			NoticeManger.getInstance().refreshNoticeList();
			log.info(String.format("updateGmInfo refreshNoticeList"));
		}
		ServerMsg.LoginUpdateGmInfoResponse_7d000077.Builder res = ServerMsg.LoginUpdateGmInfoResponse_7d000077.newBuilder() ;
		res.setResult(true);
		client.sendProtocol(res);

	}

	private static void updateIOSAccessToken(NetClient client, Object o) {
		ServerMsg.LoginUpdateIOSAccessTokenRequest_7d000074 req = (ServerMsg.LoginUpdateIOSAccessTokenRequest_7d000074) o;
    	IOSPayOrderProcessor.updateAccessToken(req.getAccessToken(), req.getExpireTime());
    	IOSPayOrderProcessor.updateJsapiTicket(req.getJsapiTicket(), req.getTickExpireTime());
		ServerMsg.LoginUpdateIOSAccessTokenResponse_7d000075.Builder res = ServerMsg.LoginUpdateIOSAccessTokenResponse_7d000075.newBuilder() ;
		res.setResult(true);
		client.sendProtocol(res);
	}

	private void registerPayOrderProcessor(BasePayOrderProcessor payOrderProcessor) {
		payOrderProcessorMap.put(payOrderProcessor.platform.getPlatform(),payOrderProcessor);
	}

	public static  void addGmOptRecord(NetClient client, Object o) {
		ServerMsg.GmOptRecordRequest_7d000052 req = (ServerMsg.GmOptRecordRequest_7d000052) o;
		ServerMsg.GmOptRecordResponse_7d000053.Builder res = ServerMsg.GmOptRecordResponse_7d000053.newBuilder() ;
		res.setResult(addGmOptRecord(req.getOptmsg(), req.getOptParam(), req.getOptResult(), req.getOptPid()));
		client.sendProtocol(res);
	}

	public static boolean addGmOptRecord(String optmsg,String opt_param,String opt_result, String optPid) {
		GmOpt opt = new GmOpt() ;
		opt.setCreateTime(new Date());
		opt.setOptmsg(optmsg);
        opt.setOptParam(opt_param == null ? "null" : opt_param);
        opt.setOptResult(opt_result == null ? "null": opt_result);
        opt.setOptpid(optPid == null ? "" : optPid);
		GmOptMapper gmOptMapper = SpringContextLoader.getContext().getBean(GmOptMapper.class);
    	System.out.println(String.format("addGmOptRecord:%s", JsonUtil.toJsonString(opt)));
        return gmOptMapper.insert(opt) > 0;
    }

	protected void paymentCreate(NetClient client, Object message) {
		PaymentOrderCreateRequest_7d000020 request = (PaymentOrderCreateRequest_7d000020) message;
		PaymentOrderCreateResponse_7d000021.Builder resp = PaymentOrderCreateResponse_7d000021.newBuilder() ; 
		long playerId = request.getPlayerId();
		String sessionId = request.getSessionId();
		String platform = request.getPlatform();
		log.info(String.format("paymentCreate:%s", request.toString()));
		BasePayOrderProcessor payOrderProcessor = payOrderProcessorMap.get(platform);
		if (payOrderProcessor == null){
			log.error(String.format(" BasePayOrderProcessor payOrderProcessor not found platform:%s not support, req:%s", platform,request.toString()));
			resp.setOrderId(0);
			client.sendProtocol(resp.build());
			return;
		}

		Future<PayOrder> payOrderFuture = payOrderProcessor.createPayOrder(request, resp);
		payOrderFuture.onSuccess(payOrder -> {
			if (payOrder != null){
				log.info("create new order:" +  payOrder.toString());
				resp.setOrderId(payOrder.getId());
				PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
				mapper.insert(payOrder);
			} else {
				resp.setOrderId(0);
				log.error(String.format(" BasePayOrderProcessor payOrderProcessor  create payOrder fail, req:%s", request.toString()));
			}
			client.sendProtocol(resp.build());
		}).onFailure(e -> {
			e.printStackTrace();
			resp.setOrderId(0);
			client.sendProtocol(resp.build());
		});


	/*	JSONObject signData = new JSONObject();
		// game? 
		signData.put("mode", "goods");
		signData.put("offerId", Config.wechat_midas_offerId) ; 
		signData.put("buyQuantity", 1) ; 
		signData.put("env", Config.wechat_midas_env) ; 
		signData.put("currencyType", "CNY") ;
		signData.put("productId", request.getItemId());
		signData.put("zoneId", "1");
		signData.put("goodsPrice", request.getGoodsPrice()) ; 
		long outTradeNo = IdUtil.genOrderId(playerId);
		signData.put("outTradeNo", outTradeNo + "");
		
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
			
			String rawDate = signData.toJSONString();
			//? 
			String paySig = WechatHelper.calcPaymentGameItemPaySig(rawDate); 
			String signature = WechatHelper.calcSignature(rawDate, user.getSessionKey()); 
			
			Builder newBuilder = PaymentOrderProto.newBuilder(); 
			newBuilder.setSignData(rawDate); 
			newBuilder.setPaySig(paySig); 
			newBuilder.setSignature(signature); 
			resp.setOrderId(outTradeNo); 
			
			PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
			mapper.insert(payOrder);

			client.sendProtocol(resp.setOrder(newBuilder.build()));
		}).onFailure(e -> {
			resp.setOrderId(0);
			client.sendProtocol(resp.build());

//			client.sendProtocol(ExceptionUtils.getFullStackTrace(e),1) ; 
		});*/
	}
	protected void uid(NetClient client, Object message) {
		LoginPlayerUidRequest_7d000018 request = (LoginPlayerUidRequest_7d000018) message;
		String passportSessionId = request.getPassportSessionId();

		// 查询用户
		RedisUtil.getAndRunAsync(CacheType.PASSPORT_SESSION.key(passportSessionId), retU -> {
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
