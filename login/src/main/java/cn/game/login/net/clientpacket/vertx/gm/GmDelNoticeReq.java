package cn.game.login.net.clientpacket.vertx.gm;

import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmDelNoticeReq
 *
 * @description: GM删除公告接口
 * @author: ly
 * @create: 2024-09-19 19:06 @Version 1.0
 */
public class GmDelNoticeReq implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        String id =  context.request().getParam("id");
        JSONObject result = GmSelectOrderReq.getResultData();
        if (id == null) {
            result.put("result", "param error");
            response.end(result.toString());
            return;
        }
        result.put("data", NoticeManger.getInstance().delNotice(Integer.parseInt(id)));
        response.end(result.toString());
    }
}
