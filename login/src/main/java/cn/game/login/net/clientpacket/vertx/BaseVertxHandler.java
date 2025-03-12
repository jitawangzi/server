package cn.game.login.net.clientpacket.vertx;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface BaseVertxHandler extends Handler<RoutingContext> {
	default String getPath() {
		return null; // 默认返回null，表示使用注解方式
	}
}