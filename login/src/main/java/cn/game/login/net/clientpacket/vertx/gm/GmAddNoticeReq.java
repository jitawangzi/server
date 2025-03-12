package cn.game.login.net.clientpacket.vertx.gm;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.game.login.net.clientpacket.vertx.BaseVertxHandler;
import cn.game.login.net.handler.LoginServerHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName GmAddNoticeReq
 *
 * @description: GM添加公告接口
 * @author: ly
 * @create: 2024-09-19 17:04 @Version 1.0
 */
@Component
public class GmAddNoticeReq implements BaseVertxHandler {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        JsonObject reqBody = context.getBodyAsJson();
        if (reqBody == null) {
            result.put("result", "param error");
            response.end(result.toString());
            return;
        }
        String text = reqBody.getString("text");
        String tab = reqBody.getString("tab");
        String title = reqBody.getString("title");
        Integer id  = reqBody.getInteger("id");
        Integer orderNum  = reqBody.getInteger("orderNum");
        String showStartTimer = reqBody.getString("showStartTimer");
        String showEndTimer = reqBody.getString("showEndTimer");
        if (text == null || tab == null || title == null || showStartTimer == null || showEndTimer == null) {
            result.put("result", "param error");
            response.end(result.toString());
            return;
        }
        long startTime = Long.parseLong(showStartTimer)*1000L;
        long endTime = Long.parseLong(showEndTimer)*1000L;
        if (startTime > endTime) {
            result.put("result", "param error");
            response.end(result.toString());
            return;
        }
        result.put("data", NoticeManger.getInstance().addNotice(id == null ? 0 : id, tab, title, text, startTime, endTime, orderNum == null ? 999 : orderNum));
        response.end(result.toString());
        LoginServerHandler.addGmOptRecord("addNotice", reqBody.toString(),result.getString("data"),"");
    }

	@Override
	public String getPath() {
		return "/gm/addNotice";
	}
}
