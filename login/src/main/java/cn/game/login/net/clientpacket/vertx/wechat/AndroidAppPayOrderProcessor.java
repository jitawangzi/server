package cn.game.login.net.clientpacket.vertx.wechat;

import cn.game.core.base.ServerContext;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.login.net.clientpacket.vertx.UserHelper;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.DateUtil;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**    
 * 安卓app，走畅游sdk支付
 * 2025年2月25日 11:20:41
 * @author SYQ
 */
public class AndroidAppPayOrderProcessor extends BasePayOrderProcessor{
    public AndroidAppPayOrderProcessor() {
		super(PayOrderPlatformEnum.ANDROID_APP);
    }

    @Override
    public Future<PayOrder> createPayOrder(ServerMsg.PaymentOrderCreateRequest_7d000020 request, ServerMsg.PaymentOrderCreateResponse_7d000021.Builder resp) {
        Promise<PayOrder> promise = Promise.promise();
        long playerId = request.getPlayerId();
        String sessionId = request.getSessionId();
		long outTradeNo = IdUtil.genOrderId(playerId);

        User user = UserHelper.getUserBySessionId(sessionId);
        // 创建一个订单
        PayOrder payOrder = new PayOrder() ;
        payOrder.setId(outTradeNo);
        payOrder.setCreateDate(DateUtil.nowDateStr());
        payOrder.setCreateTime(DateUtil.nowTimeStr());
		payOrder.setEnv((byte) (ServerContext.getInstance().getRunMode().isProduction() ? 0 : 1));
        payOrder.setIsDeliver(false);
        payOrder.setPayState((byte) 1);
        payOrder.setPlayerId(playerId);
        payOrder.setPrice(request.getGoodsPrice());
        payOrder.setUserId(playerId);
		payOrder.setThirdUid(user.getUsername());
//		ObjUtil.setDefaultValue(payOrder);

        VxHolder.vertx.executeBlocking(r -> {
			PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
			mapper.insert(payOrder);
			promise.complete(payOrder);
        }).onFailure(e -> {
			log.error("", e);
            promise.fail(e);
        });
        return promise.future();
    }
}
