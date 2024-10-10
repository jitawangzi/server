package cn.game.login;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.game.login.net.clientpacket.vertx.VertxRegisterReq;
import cn.game.login.net.clientpacket.vertx.VertxServerListReq;
import cn.game.login.net.clientpacket.vertx.VertxThirdPartyConfirmReq;
import cn.game.login.net.clientpacket.vertx.gm.*;
import cn.game.login.net.clientpacket.vertx.wechat.*;
import cn.game.util.VxRedisUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

public class RestServer extends AbstractVerticle {

	private static int port;

	public static void main(String[] args) {
		// 获取vertx基类
		Vertx vertx = Vertx.vertx();
		// 部署发布rest服务
		vertx.deployVerticle(new RestServer());
		VxRedisUtil.setRedisUrl("redis://:32SSDgSDFsa3dsdfgg@192.168.1.67:6379/0");
		vertx.deployVerticle(new VxRedisUtil());
		vertx.deployVerticle(new RestServer());
	}

	// 重写start方法，加入我们的rest服务处理逻辑
	@Override
	public void start() throws Exception {
		// 实例化一个路由器出来，用来路由不同的rest接口
		Router router = Router.router(vertx);
		// 增加一个处理器，将请求的上下文信息，放到RoutingContext中
		router.route().handler(BodyHandler.create().setHandleFileUploads(false));
		// 处理一个post方法的rest接口
//		router.post("/post/:param1/:param2").handler(this::handlePost);
		// 处理一个get方法的rest接口
//		router.get("/account/register/:account/:pwd/").handler(this::handleGet);

		Set<String> allowedHeaders = new HashSet<>();
		allowedHeaders.add("x-requested-with");
		allowedHeaders.add("Access-Control-Allow-Origin");
		allowedHeaders.add("origin");
		allowedHeaders.add("Content-Type");
		allowedHeaders.add("accept");

		Set<HttpMethod> allowedMethods = new HashSet<>();
		allowedMethods.add(HttpMethod.GET);
		allowedMethods.add(HttpMethod.POST);
		allowedMethods.add(HttpMethod.DELETE);
		allowedMethods.add(HttpMethod.PATCH);
		allowedMethods.add(HttpMethod.OPTIONS);
		allowedMethods.add(HttpMethod.PUT);
		// 处理跨域
		router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));
		// 配置JSP模板引擎

		// 这里貌似只能这样写，不能加参数
		router.route("/account/register").handler(new VertxRegisterReq());
		router.route("/account/third_party_confirm").handler(new VertxThirdPartyConfirmReq());
		router.route("/account/server_list").handler(new VertxServerListReq());
		router.route("/wechat/ship/push").handler(new WechatShipPush());
		router.route("/wechat/test").handler(new WechatTest());
    	router.route("/gm/order_list").handler(new GmSelectOrderReq());
    	router.route("/gm/ip_whitelist").handler(new GmIpWhitelistReq());
    	router.route("/gm/add_ip_whitelist").handler(new GmAddIpWhitelistReq());
    	router.route("/gm/del_ip_whitelist").handler(new GmDelIpWhitelistReq());
    	router.route("/gm/addNotice").handler(new GmAddNoticeReq());
    	router.route("/gm/delNotice").handler(new GmDelNoticeReq());
    	router.route("/gm/NoticeList").handler(new GmNoticeListReq());
    	router.route("/gm/optList").handler(new GmOptListReq());

		//微信IOS 支付跳转页面
    	router.route("/wx_pay").handler(new WeChatPayPageReq());
    	router.route("/wx_customer").handler(new WeChatCustomerServiceReq());
    	router.route("/wx_pay_callback").handler(new PayCallbackSuccessReq());
	//		router.get().handler(this::handleGet2);
		// 创建一个httpserver，监听端口，并交由路由器分发处理用户请求
		vertx.createHttpServer().requestHandler(router::handle).listen(port);
	}

	// 处理post请求的handler
	private void handlePost(RoutingContext context) {
		// 从上下文获取请求参数，类似于从httprequest中获取parameter一样
		String param1 = context.request().getParam("account");
		String param2 = context.request().getParam("pwd");

		if (isBlank(param1) || isBlank(param2)) {
			// 如果参数空，交由httpserver提供默认的400错误界面
			context.response().setStatusCode(400).end();
		}

		JsonObject obj = new JsonObject();
		obj.put("method", "post").put("param1", param1).put("param2", param2);

		// 申明response类型为json格式，结束response并且输出json字符串
		context.response().putHeader("content-type", "application/json").end(obj.encodePrettily());
	}

	// 逻辑同post方法
	private void handleGet(RoutingContext context) {
		String param1 = context.request().getParam("param1");
		String param2 = context.request().getParam("param2");

		if (isBlank(param1) || isBlank(param2)) {
			context.response().setStatusCode(400).end();
		}
		JsonObject obj = new JsonObject();
		obj.put("method", "get").put("param1", param1).put("param2", param2);

		context.response().putHeader("content-type", "application/json").end(obj.encodePrettily());
	}

	private boolean isBlank(String str) {
		if (str == null || "".equals(str))
			return true;
		return false;
	}

	public static void setPort(int p) {
		port = p;
	}

}