package cn.game.login.net.clientpacket.vertx.wechat;

import cn.game.core.net.vertx.VxHolder;
import cn.game.login.LoginServer;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName WeChatPayPageReq
 *
 * @description:
 * @author: ly
 * @create: 2024-09-29 10:08 @Version 1.0
 */
public class WeChatPayPageReq implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext ctx) {
        FreeMarkerTemplateEngine engine = FreeMarkerTemplateEngine.create(VxHolder.vertx);
        Map<String,Object> data = new HashMap<>();
        data.put("name", "Vert.x JSP 示例");
        data.put("timestamp", System.currentTimeMillis());
        data.put("nonceStr", "Vert.x JSP 示例");
        data.put("prepay_id", "Vert.x JSP 示例");
        data.put("paySign", "Vert.x JSP 示例");
        data.put("jsApiList", "Vert.x JSP 示例");
        data.put("appId", "Vert.x JSP 示例");
        data.put("signature", "Vert.x JSP 示例");
        data.put("price", "Vert.x JSP 示例");
        data.put("itemName", "Vert.x JSP 示例");
        engine.render(data, "/template/wx_pay.ftl", res -> {
            if (res.succeeded()) {
                ctx.response().end(res.result());
            } else {
                ctx.fail(res.cause());
            }
        });

    }
}
