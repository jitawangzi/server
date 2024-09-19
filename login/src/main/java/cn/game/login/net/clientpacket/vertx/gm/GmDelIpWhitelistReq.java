package cn.game.login.net.clientpacket.vertx.gm;

import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmDelIpWhitelistReq
 *
 * @description: GM IP白名单删除接口
 * @author: ly
 * @create: 2024-09-19 16:25 @Version 1.0
 */
public class GmDelIpWhitelistReq implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        String ip = context.request().getParam("ip");
        result.put("data", IpWhitelistManger.getInstance().delIpWhitelist(ip));
        response.end(result.toString());
    }
}
