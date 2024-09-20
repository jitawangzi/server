package cn.game.login.net.clientpacket.vertx.gm;

import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmNoticeListReq
 *
 * @description:
 * @author: ly
 * @create: 2024-09-19 19:14 @Version 1.0
 */
public class GmNoticeListReq implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        result.put("data", NoticeManger.getInstance().getNoticeList());
        response.end(result.toString());
    }
}
