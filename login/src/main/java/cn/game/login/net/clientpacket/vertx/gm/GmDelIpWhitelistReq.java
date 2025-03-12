package cn.game.login.net.clientpacket.vertx.gm;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.game.login.net.clientpacket.vertx.BaseVertxHandler;
import cn.game.login.net.handler.LoginServerHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmDelIpWhitelistReq
 *
 * @description: GM IP白名单删除接口
 * @author: ly
 * @create: 2024-09-19 16:25 @Version 1.0
 */
@Component
public class GmDelIpWhitelistReq implements BaseVertxHandler {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        String ip = context.request().getParam("ip");
        result.put("data", IpWhitelistManger.getInstance().delIpWhitelist(ip));
        response.end(result.toString());
        LoginServerHandler.addGmOptRecord("delIpWhiteList","删除IP: "+ ip,"删除结果: "+result.getString("data"),"");
    }

	@Override
	public String getPath() {
		return "/gm/del_ip_whitelist";
	}
}
