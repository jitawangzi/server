package cn.game.login.net.clientpacket.vertx.wechat;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.login.net.clientpacket.vertx.BaseVertxHandler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

@Component
public class WechatTest implements BaseVertxHandler {

	String token = "token" ; 
	
	protected static final Logger log = LoggerFactory.getLogger(WechatTest.class);

	@Override
	public void handle(RoutingContext context) {
		HttpServerRequest request = context.request();
		HttpServerResponse response = context.response().putHeader("content-type", "text/json");

		String signature = request.getParam("signature"); 
		String timestamp= request.getParam("timestamp"); 
		String nonce= request.getParam("nonce"); 
		String echostr= request.getParam("echostr"); 
		
		boolean checkRequest = WechatHelper.checkRequest(signature, timestamp, nonce);
		if (checkRequest) {
			response.end(echostr);
		} else {
			log.error("微信测试失败");
		}

	}

	@Override
	public String getPath() {
		return "/wechat/test";
	}

}
