package cn.game.login.net.clientpacket.vertx.gm;

import cn.game.util.JsonUtil;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmIpWhitelistReq
 *
 * @description: GM IP白名单查询接口
 * @author: ly
 * @create: 2024-09-19 15:34 @Version 1.0
 */
public class GmIpWhitelistReq implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        result.put("data", JsonUtil.toJsonString(IpWhitelistManger.getInstance().getIpWhitelistList()));
        response.end(result.toString());
    }
}
