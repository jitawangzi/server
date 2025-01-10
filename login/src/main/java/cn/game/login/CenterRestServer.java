package cn.game.login;

import cn.game.login.net.clientpacket.vertx.wechat.WechatTest;
import io.vertx.ext.web.Router;

public class CenterRestServer extends RestServer {
	
	@Override
	public void regRouter(Router router) {
		// 注册业务路由
		// 测试的
		router.route("/wechat/test/test").handler(new WechatTest());

	}

}

