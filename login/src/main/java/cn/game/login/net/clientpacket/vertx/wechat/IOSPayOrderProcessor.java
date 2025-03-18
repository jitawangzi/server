package cn.game.login.net.clientpacket.vertx.wechat;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.google.gson.JsonObject;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.payments.model.Transaction;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.login.net.clientpacket.vertx.UserHelper;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.DateUtil;
import cn.game.util.HttpUtil;
import cn.game.util.JsonUtil;
import cn.game.util.LockUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName IOSPayOrderProcessor
 *
 * @description:
 * @author: ly
 * @create: 2024-09-24 15:45 @Version 1.0
 */
public class IOSPayOrderProcessor extends BasePayOrderProcessor {
	public static final String SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send";
	/** 商户号 */
	public static String merchantId = "1502514891";
	public static String appId = "wx06cbd7d8a5cf0da3";
	/** 小程序唯一凭证密钥，即 AppSecret，获取方式同 appid */
	public static String AppSecret = "cd4c3da6d0d1489b3303dc81228ce834";

	/** 商户API私钥路径 */
	public static String privateKeyPath = IOSPayOrderProcessor.class.getClassLoader().getResource("apiclient_key.pem").getPath();
//      "D:\\Party\\server\\server\\login\\src\\main\\resources\\apiclient_key.pem";

	/** 商户证书序列号 */
	public static String merchantSerialNumber = "580431AE81584BB7E7B0266180D5893C12F2B31A";
	/** 商户APIV3密钥 */
	public static String apiV3Key = "WxNtZmE1dXth7Qxffpz9swHFrkio2bfh";
	public final static Config config = new RSAAutoCertificateConfig.Builder().merchantId(merchantId)
			.privateKeyFromPath(privateKeyPath)
			.merchantSerialNumber(merchantSerialNumber)
			.apiV3Key(apiV3Key)
			.build();

	/**客服消息的TOKEN      */
	public static String CustomerToken = "qwerqwer";
	public static String encodingAesKey = "kI8SLJSbyHfXad5KMPGpwDDV8BEYVjTSl7zGV12niFh";

	// 使用自动更新平台证书的RSA配置
	// 一个商户号只能初始化一个配置，否则会因为重复的下载任务报错
	/***https://developers.weixin.qq.com/miniprogram/dev/framework/server-ability/message-push.html#%E5%BC%80%E5%8F%91%E8%80%85%E6%9C%8D%E5%8A%A1%E5%99%A8%E6%8E%A5%E6%94%B6%E6%B6%88%E6%81%AF%E6%8E%A8%E9%80%81*/
	public static String accessToken = "ggsaPOLW05QpMfA1w5SotegFUQgpMb";
	public static long accessTokenExpiresTimer = 0;
	/** https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html#58*/
	public static String jsapiTicket = "ggsaPOLW05QpMfA1w5SotegFUQgpMb";
	public static long jsapiTicketExpiresTimer = 0;

	public IOSPayOrderProcessor() {
		super(PayOrderPlatformEnum.IOS_WECAHT);
	}

//    HTTP 头 Wechatpay-Signature。应答的微信支付签名。
//    HTTP 头 Wechatpay-Serial。微信支付平台证书的序列号，验签必须使用序列号对应的微信支付平台证书。
//    HTTP 头 Wechatpay-Nonce。签名中的随机数。
//    HTTP 头 Wechatpay-Timestamp。签名中的时间戳。
//    HTTP 头 Wechatpay-Signature-Type。签名类型。
//    初始化 RSAAutoCertificateConfig。微信支付平台证书由 SDK 的自动更新平台能力提供，也可以使用本地证书。
//    初始化 NotificationParser。
//    调用 NotificationParser.parse() 验签、解密并将 JSON 转换成具体的通知回调对象。如果验签失败，SDK 会抛出 ValidationException。
//    接下来可以执行你的业务逻辑了。如果执行成功，你应返回 200 OK 的状态码。如果执行失败，你应返回 4xx 或者 5xx的状态码，例如数据库操作失败建议返回 500 Internal Server Error。
	public static void notifyPayOrder(RoutingContext context) {
		HttpServerRequest request = context.request();
		HttpServerResponse response = context.response().putHeader("content-type", "text/json");
		String wechatPaySerial = request.getHeader("Wechatpay-Serial");
		String wechatpayNonce = request.getHeader("Wechatpay-Nonce");
		String wechatSignature = request.getHeader("Wechatpay-Signature");
		String wechatTimestamp = request.getHeader("Wechatpay-Timestamp");
		String requestBody = context.getBodyAsString();
		// 构造 RequestParam
		RequestParam requestParam = new RequestParam.Builder().serialNumber(wechatPaySerial)
				.nonce(wechatpayNonce)
				.signature(wechatSignature)
				.timestamp(wechatTimestamp)
				.body(requestBody)
				.build();
		// 如果已经初始化了 RSAAutoCertificateConfig，可直接使用
// 没有的话，则构造一个
		NotificationConfig config = new RSAAutoCertificateConfig.Builder().merchantId(merchantId)
				.privateKeyFromPath(privateKeyPath)
				.merchantSerialNumber(merchantSerialNumber)
				.apiV3Key(apiV3Key)
				.build();

// 初始化 NotificationParser
		NotificationParser parser = new NotificationParser(config);
		Transaction transaction = null;
		try {
			// 以支付通知回调为例，验签、解密并转换成 Transaction
			transaction = parser.parse(requestParam, Transaction.class);
		} catch (Exception e) {
			e.printStackTrace();
			onFail(response, 401, "sign verification failed");
		}
		if (transaction == null) {
			onFail(response, 500, "transaction is null");
			return;
		}

		PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
		PayOrder payOrder = mapper.selectByPrimaryKey(Long.parseLong(transaction.getOutTradeNo()));
		if (payOrder == null) {
			onFail(response, 500, "pay order is null");
			return;
		}
		WeChatCustomerServiceReq.delRunOrderData(transaction.getPayer().getOpenid(), payOrder.getId() + "");
		if (payOrder.getIsDeliver()) {
			onSuccess(response);
			return;
		}

		if (payOrder.getId().longValue() != Long.parseLong(transaction.getOutTradeNo())) {
			onFail(response, 500, "payOrder.getThirdOrderId().equals(transaction.getTransactionId())");
			return;
		}
		User user = UserHelper.getUserByName(transaction.getPayer().getOpenid());
		if (user == null) {
			onFail(response, 500, "User user");
			return;
		}
		payOrder.setTransactionId(transaction.getTransactionId());
		payOrder.setMchOrderNo(payOrder.getId() + "");
		String serverId = UserHelper.getServerId(user.getId());
		ServerMsg.PaymentOrderShipRequest_7d000022 paymentOrderShipRequest_7d000022 = ServerMsg.PaymentOrderShipRequest_7d000022
				.newBuilder()
				.setPlayerId(user.getId())
				.setUid(payOrder.getId())
				.build();
		log.info(String.format("充值成功 通知 game：%s ", paymentOrderShipRequest_7d000022.toString()));
		Future<ServerMsg.PaymentOrderShipResponse_7d000023> future;
		if (StringUtils.isEmpty(serverId)) {
			future = VxHolder.requestRemoteServer(ServerType.Game, paymentOrderShipRequest_7d000022);
		} else {
			future = VxHolder.requestRemoteServer(serverId, paymentOrderShipRequest_7d000022);
		}
		future.onSuccess(r -> {
			if (r.getSuccess()) {
				if (!payOrder.getIsDeliver()) {
					payOrder.setIsDeliver(true);
					payOrder.setCompleteDate(DateUtil.nowDateStr());
					payOrder.setCompleteTime(DateUtil.nowTimeStr());
				}
				updatePayOrder(payOrder, requestBody);
				mapper.updateByPrimaryKeyWithBLOBs(payOrder);
				onSuccess(response);
			} else {
				updatePayOrder(payOrder, requestBody);
				mapper.updateByPrimaryKeyWithBLOBs(payOrder);
				onFail(response, 500, "发货失败");
			}
		}).onFailure(err -> {
		});

	}

	private static void updatePayOrder(PayOrder payOrder, String desc) {
		if (payOrder.getCallback() == null) {
			payOrder.setCallback(JSON.toJSONString(desc));
		}
		if (payOrder.getPayState() == 1) {
			payOrder.setPayState((byte) 2);
			payOrder.setPayDate(DateUtil.nowDateStr());
			payOrder.setPayTime(DateUtil.nowTimeStr());
		}
	}

	private static void onFail(HttpServerResponse response, int code, String errMsg) {
		response.setStatusCode(code);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("code", "fail");
		jsonObject.put("message", errMsg);

	}

	private static void onSuccess(HttpServerResponse response) {
		response.setStatusCode(200);
		response.end();
	}

	public static String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s&force_refresh=false";
	public static String centerTokenUrl = "";
	static String jsapiTicketUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";

	public static void startRefreshAccessTokenTask() {
		// 正式环境的才会更新accessToken
		/*if (!cn.game.util.Config.wechat_pay_page_url.endsWith("https://dhpartylogin.changyou.com/wx_pay")){
		    return;
		}*/
		if (!cn.game.util.Config.use_wechat_access_token_flag) {
			return;
		}
		try {
			refreshOnceAccessToken(System.currentTimeMillis());
		} catch (Exception e) {
			log.error("", e);
		}
		new Thread(() -> {
			long lastRefreshTimer = 0;

			while (true) {
				try {
					long now = System.currentTimeMillis();
					long difTimer = 2 * DateUtil.MINUTE_MILLIS;
					if (now - lastRefreshTimer >= difTimer) {
						lastRefreshTimer = now;
						if (refreshOnceAccessToken(now))
							continue;
					} else {
						Thread.sleep(1 * DateUtil.MINUTE_MILLIS);
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}).start();
	}

	static boolean refreshOnceAccessToken(long now) throws Exception {
		if (ServerContext.getInstance().getServerType() != ServerType.Login) {
			return false;
		}

		boolean lock = LockUtil.tryLockNoWaitSync(6, CacheType.IOS_WE_CHAT_ACCESS_TOKEN_REFRESH_LOCK.key());
		if (!lock) {
			log.info("startRefreshAccessTokenTask not lock");
			return true;
		}
		if (!StringUtils.isEmpty(cn.game.util.Config.center_server_url)) {
			String url = cn.game.util.Config.center_server_url + "/wechat/getWxAccessToken";
			log.info("startRefreshAccessTokenTask url:" + url);
			String result = HttpUtil.get(url);
			if (result != null) {
				JsonObject jsonObject = JsonUtil.parserJson(result);
				String accessToken = jsonObject.get("access_token").getAsString();
				String expires_in = jsonObject.get("expires_in").getAsString();
				updateAccessToken(accessToken, Long.parseLong(expires_in));
				// 获取 jsapi_ticket jsapi_ticket是公众号用于调用微信JS接口的临时票据
				url = String.format(jsapiTicketUrl, accessToken);
				result = HttpUtil.get(url);
				if (result != null) {
					jsonObject = JsonUtil.parserJson(result);
					String jsapiTicket = jsonObject.get("ticket").getAsString();
					expires_in = jsonObject.get("expires_in").getAsString();
					updateJsapiTicket(jsapiTicket, now + Long.parseLong(expires_in) * DateUtil.SECOND_MILLIS);
				}

				// 通知其他节点
				VxHolder.broadcastRemoteServer(ServerType.Login,
						ServerMsg.LoginUpdateIOSAccessTokenRequest_7d000074.newBuilder()
								.setAccessToken(accessToken)
								.setExpireTime(accessTokenExpiresTimer)
								.setJsapiTicket(jsapiTicket)
								.setTickExpireTime(jsapiTicketExpiresTimer)
								.build());

				VxHolder.broadcastRemoteServer(ServerType.Game,
						ServerMsg.LoginUpdateIOSAccessTokenRequest_7d000074.newBuilder()
								.setAccessToken(accessToken)
								.setExpireTime(accessTokenExpiresTimer)
								.setJsapiTicket(jsapiTicket)
								.setTickExpireTime(jsapiTicketExpiresTimer)
								.build());
			}

		}
		return false;
	}

	public static void updateJsapiTicket(String jsapiTicket, long l) {
		if (IOSPayOrderProcessor.jsapiTicket.equals(jsapiTicket)) {
			return;
		}
		IOSPayOrderProcessor.jsapiTicket = jsapiTicket;
		IOSPayOrderProcessor.jsapiTicketExpiresTimer = l;
		log.info(String.format("更新jsapiTicket成功 jsapiTicket:%s, jsapiTicketExpiresTimer:%s", jsapiTicket, jsapiTicketExpiresTimer));

	}

	public static void updateAccessToken(String accessToken, long accessTokenExpiresTimer) {
		if (IOSPayOrderProcessor.accessToken.equals(accessToken)) {
			return;
		}
		IOSPayOrderProcessor.accessToken = accessToken;
		IOSPayOrderProcessor.accessTokenExpiresTimer = accessTokenExpiresTimer;
		log.info(String.format("更新access_token成功 accessToken:%s, accessTokenExpiresTimer:%s", accessToken, accessTokenExpiresTimer));
	}

	@Override
	public Future<PayOrder> createPayOrder(ServerMsg.PaymentOrderCreateRequest_7d000020 req,
			ServerMsg.PaymentOrderCreateResponse_7d000021.Builder resp) {
		Promise<PayOrder> promise = Promise.promise();
		final int goodPrice = req.getGoodsPrice();
//        final  int goodPrice = 1;
		long playerId = req.getPlayerId();
		String sessionId = req.getSessionId();
		User user = UserHelper.getUserBySessionId(sessionId);
		long outTradeNo = IdUtil.genOrderId(playerId);
		// 创建一个订单
		PayOrder payOrder = new PayOrder();
		payOrder.setId(outTradeNo);
		payOrder.setCreateDate(DateUtil.nowDateStr());
		payOrder.setCreateTime(DateUtil.nowTimeStr());
		payOrder.setEnv(cn.game.util.Config.wechat_midas_env);
		payOrder.setIsDeliver(false);
		payOrder.setPayState((byte) 1);
		payOrder.setPrice(goodPrice);
		payOrder.setPlayerId(playerId);
		payOrder.setUserId(playerId);
		payOrder.setThirdUid(user.getUsername());
		VxHolder.vertx.executeBlocking(r -> {
			// 构建service
			JsapiService service = new JsapiService.Builder().config(config).build();
			// request.setXxx(val)设置所需参数，具体参数可见Request定义
			PrepayRequest request = new PrepayRequest();
			Amount amount = new Amount();
			amount.setTotal(goodPrice);
			Payer p = new Payer();
			p.setOpenid(user.getUsername());

			request.setPayer(p);
			request.setAmount(amount);
			request.setAppid(cn.game.util.Config.wechat_appid);
			request.setMchid(merchantId);
			request.setDescription(req.getItemId());
			request.setNotifyUrl(cn.game.util.Config.wechat_pay_callback_url);
			request.setOutTradeNo(payOrder.getId() + "");
			// 调用下单方法，得到应答
			try {
				PrepayResponse response = service.prepay(request);
				log.info(String.format("下单成功，prepayId=%s", response.getPrepayId()));
				payOrder.setThirdOrderId(response.getPrepayId());
				PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
				mapper.insert(payOrder);

				promise.complete(payOrder);

			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.getMessage());
				promise.fail(e);
			}
		});
		// 使用微信扫描 code_url 对应的二维码，即可体验Native支付
//        System.out.println(response.getCodeUrl());
		return promise.future();
	}

	public static void main(String[] args) {
		IOSPayOrderProcessor payOrderProcessor = new IOSPayOrderProcessor();
		ServerMsg.PaymentOrderCreateRequest_7d000020 req = ServerMsg.PaymentOrderCreateRequest_7d000020.newBuilder()
				.setPlayerId(1231)
				.setItemId(111 + "")
				.setGoodsPrice(100)
				.setSessionId("111")
				.setPlatform("")
				.build();

		ServerMsg.PaymentOrderCreateResponse_7d000021.Builder resp = ServerMsg.PaymentOrderCreateResponse_7d000021.newBuilder();
		payOrderProcessor.createPayOrder(req, resp);
	}
}
