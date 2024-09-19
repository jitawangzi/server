package cn.game.login.net.clientpacket.vertx.gm;

import cn.game.core.net.vertx.VxHolder;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.login.mapper.UserMapper;
import cn.game.util.JsonUtil;
import cn.game.util.SpringContextLoader;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmSelectOrderReq
 *
 * @description: GM 订单查询接口
 * @author: ly
 * @create: 2024-09-13 20:21 @Version 1.0
 */
public class GmSelectOrderReq implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
//        String orderId = context.request().getParam("orderId"); //第三方订单id
        String selfOrderId = context.request().getParam("selfOrderId"); //自己订单id
        String playerId = context.request().getParam("playerId"); //玩家id
        Integer status = Integer.parseInt(context.request().getParam("status")); //订单状态
        Integer page = Integer.parseInt(context.request().getParam("page")); //页数
        Integer pageSize = Integer.parseInt(context.request().getParam("pageSize")); //每页数量
        page = page == null ? 0 : page;
        pageSize = pageSize == null ? 10 : pageSize;
        PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
        Integer finalPage = page;
        Integer finalPageSize = pageSize;
        JSONObject result = getResultData();
        VxHolder.vertx.executeBlocking(future -> {
            try {
                future.complete(mapper.selectOrderList(playerId == null ? null : Long.parseLong(playerId), status, selfOrderId, finalPage, finalPageSize));
            } catch (Exception e) {
                e.printStackTrace();
                future.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                String resJson = "";
                if (res.result() != null) {
                    resJson = JsonUtil.toJsonString(res.result());
                }
                result.put("data", resJson);
                response.end(result.toString());
            } else {
                result.put("result","fail");
                response.end(result.toString());
            }
        });
    }

    static JSONObject getResultData(){
        JSONObject result = new JSONObject();
        result.put("result","success");
        return result;
    }
}
