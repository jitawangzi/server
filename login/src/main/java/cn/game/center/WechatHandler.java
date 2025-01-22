package cn.game.center;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName WechatHandler
 *
 * @description:
 * @author: ly
 * @create: 2025-01-10 17:34 @Version 1.0
 */
public class WechatHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "text/json");
        Map<String,String> result = new HashMap<>();
        result.put("access_token", WeiXinParamManager.getInstance().getAccessToken());
        result.put("expires_in", WeiXinParamManager.getInstance().getAccessTokenExpiresTimer()+"");
        WeiXinParamManager.log.info(String.format("WechatHandler:%s", result));
        response.end(result.toString());
    }
}
