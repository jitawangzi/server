package cn.game.login.net.clientpacket.vertx.gm;

import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmAddNoticeReq
 *
 * @description: GM添加公告接口
 * @author: ly
 * @create: 2024-09-19 17:04 @Version 1.0
 */
public class GmAddNoticeReq implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        String text = context.getBodyAsJson().getString("text");
        response.end(result.toString());

    }
}
