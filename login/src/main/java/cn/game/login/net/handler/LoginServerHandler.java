package cn.game.login.net.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.cache.CacheType;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.login.cache.entity.GmOpt;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.GmOptMapper;
import cn.game.login.mapper.UserMapper;
import cn.game.login.net.clientpacket.vertx.UserHelper;
import cn.game.login.net.clientpacket.vertx.gm.IpWhitelistManger;
import cn.game.login.net.clientpacket.vertx.gm.NoticeManger;
import cn.game.login.net.clientpacket.vertx.wechat.AndroidAppPayOrderProcessor;
import cn.game.login.net.clientpacket.vertx.wechat.AndroidWechatPayOrderProcessor;
import cn.game.login.net.clientpacket.vertx.wechat.BasePayOrderProcessor;
import cn.game.login.net.clientpacket.vertx.wechat.IOSPayOrderProcessor;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.protocol.protobuf.ServerMsg.GameStatusPublish_7d000017;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerDeleteRequest_7d000080;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerDeleteResponse_7d000081;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidRequest_7d000018;
import cn.game.protocol.protobuf.ServerMsg.LoginPlayerUidResponse_7d000019;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateRequest_7d000020;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderCreateResponse_7d000021;
import cn.game.util.HttpHelp;
import cn.game.util.JsonUtil;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;

/**
 * 服务器之间的消息处理器
 */
@Component
public class LoginServerHandler extends BaseHandler {

	public static final int UPDATE_WHITE_LIST = 1;
	public static final int UPDATE_NOTICE = 2;

	Map<Integer, BasePayOrderProcessor> payOrderProcessorMap = new HashMap<>();
	// 总是使用畅游sdk支付
	private BasePayOrderProcessor payOrderProcessor = new AndroidAppPayOrderProcessor(); 
	@Override
	protected int getModule() {
		return 0x7d;
	}
	@Override
	protected void inititialize() {

		putInvoker(PbProtocol.GameStatusPublish_7d000017, this::gameStatus);
		putInvoker(PbProtocol.LoginPlayerUidRequest_7d000018, this::uid);
		putInvoker(PbProtocol.LoginPlayerDeleteRequest_7d000080, this::playerDelete);
		putInvoker(PbProtocol.PaymentOrderCreateRequest_7d000020, this::paymentCreate);
		putInvoker(PbProtocol.GmOptRecordRequest_7d000052, LoginServerHandler::addGmOptRecord);
		putInvoker(PbProtocol.LoginUpdateIOSAccessTokenRequest_7d000074, LoginServerHandler::updateIOSAccessToken);
		putInvoker(PbProtocol.LoginUpdateGmInfoRequest_7d000076, this::updateGmInfo);

		registerPayOrderProcessor(new AndroidWechatPayOrderProcessor());
		registerPayOrderProcessor(new IOSPayOrderProcessor());
		registerPayOrderProcessor(new AndroidAppPayOrderProcessor());

		putInvoker(PbProtocol.NotifyWechatSubscribeMessageRequest_7d000043, this::notifyWechatSubscribeMessage);

	}

	/**
	 * https://developers.weixin.qq.com/minigame/dev/api-backend/open-api/subscribe-message/subscribeMessage.send.html
	 * 微信推送订阅消息
	 * @param client
	 * @param o
	 */
	private void notifyWechatSubscribeMessage(NetClient client, Object o) {
		ServerMsg.NotifyWechatSubscribeMessageRequest_7d000043 req = (ServerMsg.NotifyWechatSubscribeMessageRequest_7d000043) o;
		log.info(String.format("notifyWechatSubscribeMessage:%s",req));
    		String url =
     String.format("https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s",IOSPayOrderProcessor.accessToken);
//    String url =
//        String.format(
//            "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s",
//            "88_796a_xcFp9QpatNgXnXOOLy2gMEBUTWSVfu5Y7FyfdQmo5dnK21h7e0ltwfz-Hq_kjrTnW0HAZokN4x81MGgeHdSMI9u5udhvaq0QyPW5d54aoSAwWTdvNpqlckQXDjAAAIPV");
		Map<String,Object> params = new HashMap<>();
		params.put("touser",req.getOpenid());
		params.put("template_id",req.getTemplateId());
		Map<String, Map<String,String>> valParam = new HashMap<>();
		req.getJsonDataMap().forEach((key,val) ->{
			Map<String,String> valMap = new HashMap<>();
			valMap.put("value",val);
			valParam.put(key,valMap);
		});
		params.put("data", valParam);
		HttpHelp.postJSonUrl(url,params,(result)->{
			log.info(String.format("url:%s, params:%s, result:%s",url,params.toString(),result));
			client.sendProtocol(ServerMsg.NotifyWechatSubscribeMessageResponse_7d000044.newBuilder().setResult(true).build());
		},(err)->{
			client.sendProtocol(ServerMsg.NotifyWechatSubscribeMessageResponse_7d000044.newBuilder().setResult(false).build());
		});

	}

	private void playerDelete(NetClient client, Object o) {
		LoginPlayerDeleteRequest_7d000080 req = (LoginPlayerDeleteRequest_7d000080) o;
		long playerId = req.getPlayerId();
		String account = req.getAccount();
		// 删除账号缓存
		User user = UserHelper.getUserByName(account);
		if (user != null) {
			UserHelper.removeUser(account).thenCompose(r -> UserHelper.removeUser(user.getSessionId())).thenCompose(r -> {
				return CompletableFuture.supplyAsync(() -> {
					UserMapper userMapper = SpringContextLoader.getContext().getBean(UserMapper.class);
					userMapper.deleteByPrimaryKey(playerId);
					return null;
				});
			}).exceptionally(e -> {
				log.error("删除玩家失败", e);
				return null;
			});
		}
		client.sendProtocol(LoginPlayerDeleteResponse_7d000081.getDefaultInstance());
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
		// 不通过平台选择sdk支付方式，直接指定畅游sdk
//		BasePayOrderProcessor payOrderProcessor = payOrderProcessorMap.get(Integer.parseInt(platform));
//		if (payOrderProcessor == null){
//			log.error(String.format(" BasePayOrderProcessor payOrderProcessor not found platform:%s not support, req:%s", platform,request.toString()));
//			resp.setOrderId(0);
//			client.sendProtocol(resp.build());
//			return;
//		}

		Future<PayOrder> payOrderFuture = payOrderProcessor.createPayOrder(request);
		payOrderFuture.onSuccess(payOrder -> {
			if (payOrder != null){
				log.info("create new order:" +  payOrder.toString());
				resp.setOrderId(payOrder.getId());
				if (payOrder.getPaymentOrderProto() != null) {
					resp.setOrder(payOrder.getPaymentOrderProto()) ; 
				}
			} else {
				resp.setOrderId(0);
				log.error(String.format(" BasePayOrderProcessor payOrderProcessor  create payOrder fail, req:%s", request.toString()));
			}
			client.sendProtocol(resp.build());
		}).onFailure(e -> {
			log.error("paymentCreate fail", e);
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
