package cn.game.login.net.clientpacket.vertx.wechat;

import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.login.net.clientpacket.vertx.UserHelper;
import cn.game.login.net.clientpacket.vertx.wechat.combineModule.PrepayRequest;
import cn.game.login.net.clientpacket.vertx.wechat.combineModule.ReqAmountInfo;
import cn.game.login.net.clientpacket.vertx.wechat.combineModule.ReqSubOrderCompatible;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.DateUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

/**
 * @ClassName IOSPayOrderProcessor
 *
 * @description:
 * @author: ly
 * @create: 2024-09-24 15:45 @Version 1.0
 */
public class IOSPayOrderProcessor extends BasePayOrderProcessor{
    /** 商户号 */
    public static String merchantId = "190000****";
    /** 商户API私钥路径 */
    public static String privateKeyPath = "/Users/yourname/your/path/apiclient_key.pem";
    /** 商户证书序列号 */
    public static String merchantSerialNumber = "5157F09EFDC096DE15EBE81A47057A72********";
    /** 商户APIV3密钥 */
    public static String apiV3Key = "...";
    public static String appId = "...";
    // 使用自动更新平台证书的RSA配置
    // 一个商户号只能初始化一个配置，否则会因为重复的下载任务报错

    public final static  Config config =
            new RSAAutoCertificateConfig.Builder()
                    .merchantId(merchantId)
                    .privateKeyFromPath(privateKeyPath)
                    .merchantSerialNumber(merchantSerialNumber)
                    .apiV3Key(apiV3Key)
                    .build();


    public IOSPayOrderProcessor() {
        super(PayOrderPlatformEnum.IOS);
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
    public static void notifyPayOrder(RoutingContext context){
        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response().putHeader("content-type", "text/json");
        String wechatPaySerial = request.getHeader("Wechatpay-Serial");
        String wechatpayNonce = request.getHeader("Wechatpay-Nonce");
        String wechatSignature = request.getHeader("Wechatpay-Signature");
        String wechatTimestamp = request.getHeader("Wechatpay-Timestamp");
        String requestBody = context.getBodyAsString();
        // 构造 RequestParam
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(wechatPaySerial)
                .nonce(wechatpayNonce)
                .signature(wechatSignature)
                .timestamp(wechatTimestamp)
                .body(requestBody)
                .build();
        // 如果已经初始化了 RSAAutoCertificateConfig，可直接使用
// 没有的话，则构造一个
        NotificationConfig config = new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
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
            onFail(response,401,"sign verification failed");
        }
        if (transaction == null){
            onFail(response,500,"transaction is null");
            return;
        }

        PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
        PayOrder payOrder = mapper.selectByPrimaryKey(Long.parseLong(transaction.getOutTradeNo()));
        if (payOrder == null){
            onFail(response,500,"pay order is null");
            return;
        }
        if (payOrder.getIsDeliver()){
            onSuccess(response);
            return;
        }

        if (!payOrder.getThirdOrderId().equals(transaction.getTransactionId())){
            onFail(response,500,"payOrder.getThirdOrderId().equals(transaction.getTransactionId())");
            return;
        }
        User user = UserHelper.getUserByName(transaction.getPayer().getSpOpenid());
        if (user == null){
            onFail(response,500,"User user");
            return;
        }

        String serverId = UserHelper.getServerId(user.getId());
        ServerMsg.PaymentOrderShipRequest_7d000022 paymentOrderShipRequest_7d000022 = ServerMsg.PaymentOrderShipRequest_7d000022
                .newBuilder().setPlayerId(user.getId()).setUid(payOrder.getId()).build();
        Future<Message<ServerMsg.PaymentOrderShipResponse_7d000023>> future;
        if (StringUtils.isEmpty(serverId)) {
            future = VxHolder.requestRemoteServer(ServerType.Game, paymentOrderShipRequest_7d000022);
        } else {
            future = VxHolder.requestRemoteServer(serverId, paymentOrderShipRequest_7d000022);
        }
        future.onSuccess(r -> {
            if (r.body().getSuccess()) {
                if (!payOrder.getIsDeliver()) {
                    payOrder.setIsDeliver(true);
                    payOrder.setCompleteDate(DateUtil.nowDateStr());
                    payOrder.setCompleteTime(DateUtil.nowTimeStr()) ;
                }
                updatePayOrder(payOrder,requestBody);
                mapper.updateByPrimaryKey(payOrder);
                onSuccess(response);
            } else {
                updatePayOrder(payOrder,requestBody);
                mapper.updateByPrimaryKey(payOrder);
                onFail(response,500,"发货失败");
            }
        }).onFailure(err->{});


    }

    private static void updatePayOrder(PayOrder payOrder,String desc) {
        if (payOrder.getCallback() == null) {
            payOrder.setCallback(JSON.toJSONString(desc));
        }
        if (payOrder.getPayState() == 1) {
            payOrder.setPayState((byte) 2);
            payOrder.setPayDate(DateUtil.nowDateStr());
            payOrder.setPayTime(DateUtil.nowTimeStr()) ;
        }
    }

    private static void onFail(HttpServerResponse response,int code, String errMsg) {
        response.setStatusCode(code);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","fail");
        jsonObject.put("message",errMsg);


    }

    private static void onSuccess(HttpServerResponse response) {
        response.setStatusCode(200);
        response.end();
    }

    @Override
    public Future<PayOrder> createPayOrder(ServerMsg.PaymentOrderCreateRequest_7d000020 req, ServerMsg.PaymentOrderCreateResponse_7d000021.Builder resp) {
        Promise<PayOrder> promise = Promise.promise();

        long playerId = req.getPlayerId();
        String sessionId = req.getSessionId();
        User user = UserHelper.getUserBySessionId(sessionId);
        long outTradeNo = IdUtil.genOrderId(playerId);

        // 创建一个订单
        PayOrder payOrder = new PayOrder() ;
        payOrder.setId(outTradeNo);
        payOrder.setCreateDate(DateUtil.nowDateStr());
        payOrder.setCreateTime(DateUtil.nowTimeStr());
        payOrder.setEnv(cn.game.util.Config.wechat_midas_env);
        payOrder.setIsDeliver(false);
        payOrder.setPayState((byte) 1);
        payOrder.setPlayerId(playerId);
        payOrder.setUserId(playerId);
        payOrder.setThirdUid(user.getThirdUid());
    VxHolder.vertx.executeBlocking(
        r -> {
          CombineJsapiService combineJsapiService =
              new CombineJsapiService.Builder().config(config).build();
          PrepayRequest request = new PrepayRequest();
          request.setCombineAppid(appId);
          request.setCombineOutTradeNo(payOrder.getId() + "");
          request.setCombineMchid(merchantId);
          request.setNotifyUrl("https://notify_url");
          ReqAmountInfo amount = new ReqAmountInfo();
          amount.setTotalAmount(req.getGoodsPrice());
          ReqSubOrderCompatible subOrderCompatible = new ReqSubOrderCompatible();
          subOrderCompatible.setAmount(amount);
          subOrderCompatible.setMchId(merchantId);
          subOrderCompatible.setDetail("测试商品标题");
          subOrderCompatible.setOutTradeNo(request.getCombineOutTradeNo());
          subOrderCompatible.setAttach("测试商品描述");

          request.addSubOrders(subOrderCompatible);
            // 调用下单方法，得到应答
            try {
                PrepayResponse response = combineJsapiService.prepay(request);
                payOrder.setThirdOrderId(response.getPrepayId());
                promise.complete(payOrder);

            }catch (Exception e){
                e.printStackTrace();
                promise.fail(e);
            }
          /*   // 构建service
          JsapiService service = new JsapiService.Builder().config(config).build();
          // request.setXxx(val)设置所需参数，具体参数可见Request定义
          PrepayRequest request = new PrepayRequest();
          Amount amount = new Amount();
          amount.setTotal(req.getGoodsPrice());
          request.setAmount(amount);
          request.setAppid(appId);
          request.setMchid(merchantId);
          request.setDescription("测试商品标题");
          request.setNotifyUrl("https://notify_url");
          request.setOutTradeNo(payOrder.getId()+"");
          // 调用下单方法，得到应答
          try {
              PrepayResponse response = service.prepay(request);
              payOrder.setThirdOrderId(response.getPrepayId());
              promise.complete(payOrder);

          }catch (Exception e){
              e.printStackTrace();
              promise.fail(e);
          }*/
        });

        // 使用微信扫描 code_url 对应的二维码，即可体验Native支付
//        System.out.println(response.getCodeUrl());
        return promise.future();
    }

}
