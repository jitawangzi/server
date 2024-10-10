package cn.game.login.net.clientpacket.vertx.wechat;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName PayCallbackSuccessReq
 *
 * @description:
 * @author: ly
 * @create: 2024-10-10 15:14 @Version 1.0
 */
public class PayCallbackSuccessReq implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext event) {
        IOSPayOrderProcessor.notifyPayOrder(event);
    }
}
