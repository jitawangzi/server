package cn.game.login.net.clientpacket.vertx.gm;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.game.login.net.clientpacket.vertx.BaseVertxHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmNoticeListReq
 *
 * @description:
 * @author: ly
 * @create: 2024-09-19 19:14 @Version 1.0
 */
@Component
public class GmNoticeListReq implements BaseVertxHandler {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        result.put("data", NoticeManger.getInstance().getNoticeList());
        response.end(result.toString());
    }

	@Override
	public String getPath() {
		return "/gm/NoticeList";
	}
}
