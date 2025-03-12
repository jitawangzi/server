package cn.game.login.net.clientpacket.vertx.gm;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.game.login.net.clientpacket.vertx.BaseVertxHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmIpWhitelistReq
 *
 * @description: GM IP白名单查询接口
 * @author: ly
 * @create: 2024-09-19 15:34 @Version 1.0
 */
@Component
public class GmIpWhitelistReq implements BaseVertxHandler {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        result.put("data",  IpWhitelistManger.getInstance().getIpWhitelistList());
        response.end(result.toString());
    }

	@Override
	public String getPath() {
		return "/gm/ip_whitelist";
	}
}
