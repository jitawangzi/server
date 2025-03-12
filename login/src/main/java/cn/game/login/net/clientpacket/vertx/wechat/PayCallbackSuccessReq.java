package cn.game.login.net.clientpacket.vertx.wechat;

import org.springframework.stereotype.Component;

import cn.game.login.net.clientpacket.vertx.BaseVertxHandler;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName PayCallbackSuccessReq
 *
 * @description:
 * @author: ly
 * @create: 2024-10-10 15:14 @Version 1.0
 */
@Component
public class PayCallbackSuccessReq implements BaseVertxHandler {
    @Override
    public void handle(RoutingContext event) {
        IOSPayOrderProcessor.notifyPayOrder(event);
    }

	@Override
	public String getPath() {
		return "/wx_pay_callback";
	}
}
