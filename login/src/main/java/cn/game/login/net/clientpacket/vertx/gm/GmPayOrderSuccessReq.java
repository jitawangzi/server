package cn.game.login.net.clientpacket.vertx.gm;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.game.core.net.vertx.VxHolder;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.login.net.clientpacket.vertx.BaseVertxHandler;
import cn.game.login.net.clientpacket.vertx.UserHelper;
import cn.game.login.net.handler.LoginServerHandler;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.DateUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmPayOrderSuccessReq
 *
 * @description: GM  补单
 * @author: ly
 * @create: 2024-10-23 10:15 @Version 1.0
 */
@Component
public class GmPayOrderSuccessReq implements BaseVertxHandler {
    protected static Logger log = LoggerFactory.getLogger(GmPayOrderSuccessReq.class);

    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        try {
        String id =  context.request().getParam("id");
        PayOrderMapper mapper = paymentMapper();
        PayOrder payOrderSelect = null;
        if (StringUtils.isBlank(id)) { throw new IllegalArgumentException("PAYMENT_ORDER_ID_REQUIRED"); }
        if (id.matches("[0-9]+") && new java.math.BigInteger(id).bitLength() < 64) {
            payOrderSelect = mapper.selectByPrimaryKey(Long.parseLong(id));
        }
        if (payOrderSelect == null) {
        	List<PayOrder> selectByThirdOrderId = mapper.selectByThirdOrderId(id); 
        	if (!selectByThirdOrderId.isEmpty()) {
        		payOrderSelect = selectByThirdOrderId.get(0); 
        		if (selectByThirdOrderId.size() > 1) {
					log.warn("selectByThirdOrderId size > 1 : " + id) ; 
				}
			}
		}
        PayOrder payOrder = payOrderSelect ; 
        if (!cn.game.login.net.clientpacket.vertx.wechat.PaymentOrderState.canShip(payOrder)){
            result.put("result","param err order not found");
            log.error(String.format("改订单不存在 id:%s",id));
            response.end(result.toString());
            return;
        }
        if (Boolean.TRUE.equals(payOrder.getIsDeliver())){
            result.put("result","order has pay");
            log.error(String.format("该订单已完成，不可再次补单 payOrder:%s",payOrder.toString()));
            response.end(result.toString());
            return;
        }

         ServerMsg.PaymentOrderShipRequest_7d000022 paymentOrderShipRequest_7d000022 =
        ServerMsg.PaymentOrderShipRequest_7d000022.newBuilder()
            .setPlayerId(payOrder.getPlayerId())
            .setUid(payOrder.getId())
            .setSdkOrderId(cn.game.login.net.clientpacket.vertx.wechat.PaymentOrderState.sdkOrderId(payOrder))
            .build();
			Future<ServerMsg.PaymentOrderShipResponse_7d000023> future;
        future = shipOrder(paymentOrderShipRequest_7d000022);
        // 派生 Future 接住订单合并异常，避免成功回调抛错后 HTTP 永远不结束。
        future.map(r -> {
			if (r.getSuccess()) {
                if (!Boolean.TRUE.equals(payOrder.getIsDeliver())) {
                    payOrder.setIsDeliver(true);
                    payOrder.setCompleteDate(DateUtil.nowDateStr());
                    payOrder.setCompleteTime(DateUtil.nowTimeStr()) ;
                }
                log.info(String.format("补单成功 payOrder:%s", payOrder.toString()));
                payOrder.setPayState((byte) 2);
                payOrder.setPayDate(DateUtil.nowDateStr());
                payOrder.setPayTime(DateUtil.nowTimeStr()) ;
                cn.game.login.net.clientpacket.vertx.wechat.PaymentOrderState.record(mapper, payOrder, true);
                audit(id, true);

            } else {
                result.put("result", "fail");
                // 人工补单失败没有新的支付凭据，不修改支付事实或交付状态。
                log.error(String.format("补单失败 payOrder：%s",payOrder.toString()));
                audit(id, false);
            }
            return result.toString();
        }).onSuccess(body -> response.end(body)).onFailure(err->{
            log.warn("人工补单发货或订单合并未确认 orderId={}", id, err);
            result.put("result", "fail");
            response.end(result.toString());
        });
        } catch (Exception failure) {
            log.warn("人工补单受理失败", failure);
            result.put("result", "fail");
            response.end(result.toString());
        }
    }

    /** @return 当前订单存储 */
    protected PayOrderMapper paymentMapper() { return SpringContextLoader.getContext().getBean(PayOrderMapper.class); }

    /** @param request 已校验订单请求 @return Game 原提交确认结果 */
    protected Future<ServerMsg.PaymentOrderShipResponse_7d000023> shipOrder(ServerMsg.PaymentOrderShipRequest_7d000022 request) {
        String serverId = UserHelper.getServerId(request.getPlayerId());
        return StringUtils.isEmpty(serverId) ? VxHolder.requestRemoteServer(ServerType.Game, request) : VxHolder.requestRemoteServer(serverId, request);
    }

    /** @param id 请求单号 @param success 发货是否已确认 */
    protected void audit(String id, boolean success) {
        LoginServerHandler.addGmOptRecord("payOrderSuccess", id, success ? "补单成功:" + id : "补单失败:" + id, "");
    }

	@Override
	public String getPath() {
		return "/gm/payOrderSuccess";
	}
}
