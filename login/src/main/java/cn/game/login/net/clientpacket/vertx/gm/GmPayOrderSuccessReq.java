package cn.game.login.net.clientpacket.vertx.gm;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import cn.game.core.net.vertx.VxHolder;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.login.net.clientpacket.vertx.UserHelper;
import cn.game.login.net.handler.LoginServerHandler;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.DateUtil;
import cn.game.util.ServerType;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmPayOrderSuccessReq
 *
 * @description: GM  补单
 * @author: ly
 * @create: 2024-10-23 10:15 @Version 1.0
 */
public class GmPayOrderSuccessReq implements Handler<RoutingContext> {
    protected static Logger log = LoggerFactory.getLogger(GmPayOrderSuccessReq.class);

    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        String id =  context.request().getParam("id");
        JSONObject result = GmSelectOrderReq.getResultData();
        PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
        PayOrder payOrder = mapper.selectByPrimaryKey(Long.parseLong(id));
        if (payOrder == null){
            result.put("result","param err order not found");
            log.error(String.format("改订单不存在 id:%s",id));
            response.end(result.toString());
            return;
        }
        if (payOrder.getIsDeliver()){
            result.put("result","order has pay");
            log.error(String.format("该订单已完成，不可再次补单 payOrder:%s",payOrder.toString()));
            response.end(result.toString());
            return;
        }
        String serverId = UserHelper.getServerId(payOrder.getPlayerId());
         ServerMsg.PaymentOrderShipRequest_7d000022 paymentOrderShipRequest_7d000022 =
        ServerMsg.PaymentOrderShipRequest_7d000022.newBuilder()
            .setPlayerId(payOrder.getPlayerId())
            .setUid(payOrder.getId())
            .build();
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
                    payOrder.setCompleteTime(DateUtil.nowTimeStr()) ;
                }
                log.info(String.format("补单成功 payOrder:%s", payOrder.toString()));
                payOrder.setPayState((byte) 2);
                payOrder.setPayDate(DateUtil.nowDateStr());
                payOrder.setPayTime(DateUtil.nowTimeStr()) ;
                mapper.updateByPrimaryKeyWithBLOBs(payOrder);
                LoginServerHandler.addGmOptRecord("payOrderSuccess", id,"补单成功:" +id,"");

            } else {
                result.put("result", "fail");
                mapper.updateByPrimaryKeyWithBLOBs(payOrder);
                log.error(String.format("补单失败 payOrder：%s",payOrder.toString()));
                LoginServerHandler.addGmOptRecord("payOrderSuccess", id,"补单失败:"+id,"");
            }
            response.end(result.toString());
        }).onFailure(err->{
            err.printStackTrace();
            log.error(err.getMessage());
        });
    }
}
