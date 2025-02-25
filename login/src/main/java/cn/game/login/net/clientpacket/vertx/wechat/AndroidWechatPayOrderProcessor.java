package cn.game.login.net.clientpacket.vertx.wechat;

import com.alibaba.fastjson2.JSONObject;

import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.cache.entity.User;
import cn.game.login.net.clientpacket.vertx.UserHelper;
import cn.game.protocol.protobuf.BaseMsg;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @ClassName AndroidPayOrderProcessor
 *
 * @description:  安卓小游戏支付订单处理器
 * @author: ly
 * @create: 2024-09-24 15:04 @Version 1.0
 */
public class AndroidWechatPayOrderProcessor extends BasePayOrderProcessor{
    public AndroidWechatPayOrderProcessor() {
		super(PayOrderPlatformEnum.ANDROID_WECAHT);
    }

    @Override
    public Future<PayOrder> createPayOrder(ServerMsg.PaymentOrderCreateRequest_7d000020 request, ServerMsg.PaymentOrderCreateResponse_7d000021.Builder resp) {
        Promise<PayOrder> promise = Promise.promise();
        long playerId = request.getPlayerId();
        String sessionId = request.getSessionId();
        JSONObject signData = new JSONObject();
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
        payOrder.setPrice(request.getGoodsPrice());
        payOrder.setUserId(playerId);
        payOrder.setThirdUid(user.getThirdUid());
//		ObjUtil.setDefaultValue(payOrder);

        VxHolder.vertx.executeBlocking(r -> {

            String rawDate = signData.toJSONString();
            //?
            String paySig = WechatHelper.calcPaymentGameItemPaySig(rawDate);
            String signature = WechatHelper.calcSignature(rawDate, user.getSessionKey());

            BaseMsg.PaymentOrderProto.Builder newBuilder = BaseMsg.PaymentOrderProto.newBuilder();
            newBuilder.setSignData(rawDate);
            newBuilder.setPaySig(paySig);
            newBuilder.setSignature(signature);
            resp.setOrderId(outTradeNo);
            resp.setOrder(newBuilder.build());
            promise.complete(payOrder);
//            PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
//            mapper.insert(payOrder);
//
//            client.sendProtocol();
        }).onFailure(e -> {
            e.printStackTrace();
            promise.fail(e);
//            resp.setOrderId(0);
//            client.sendProtocol(resp.build());

        });
        return promise.future();
    }
}
