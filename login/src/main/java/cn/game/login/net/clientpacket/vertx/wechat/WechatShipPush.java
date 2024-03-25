package cn.game.login.net.clientpacket.vertx.wechat;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class WechatShipPush implements Handler<RoutingContext> {

	protected static final Logger log = LoggerFactory.getLogger(WechatShipPush.class);

	private static final String WX_URL_STRING = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
	
	@Override
	public void handle(RoutingContext context) {
		
	}
}
