package cn.game.login;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import com.ctrip.framework.apollo.ConfigService;

import cn.game.login.net.clientpacket.vertx.VertxRegisterReq;
import cn.game.login.net.clientpacket.vertx.VertxServerListReq;
import cn.game.login.net.clientpacket.vertx.VertxThirdPartyConfirmReq;
import cn.game.login.net.clientpacket.vertx.gm.GmAddIpWhitelistReq;
import cn.game.login.net.clientpacket.vertx.gm.GmAddNoticeReq;
import cn.game.login.net.clientpacket.vertx.gm.GmDelIpWhitelistReq;
import cn.game.login.net.clientpacket.vertx.gm.GmDelNoticeReq;
import cn.game.login.net.clientpacket.vertx.gm.GmIpWhitelistReq;
import cn.game.login.net.clientpacket.vertx.gm.GmNoticeListReq;
import cn.game.login.net.clientpacket.vertx.gm.GmOptListReq;
import cn.game.login.net.clientpacket.vertx.gm.GmPayOrderSuccessReq;
import cn.game.login.net.clientpacket.vertx.gm.GmSelectOrderReq;
import cn.game.login.net.clientpacket.vertx.sojump.SojumpCallbackReq;
import cn.game.login.net.clientpacket.vertx.wechat.PayCallbackSuccessReq;
import cn.game.login.net.clientpacket.vertx.wechat.WeChatCustomerServiceReq;
import cn.game.login.net.clientpacket.vertx.wechat.WeChatPayPageReq;
import cn.game.login.net.clientpacket.vertx.wechat.WechatShipPush;
import cn.game.login.net.clientpacket.vertx.wechat.WechatTest;
import cn.game.util.VxRedisUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class RestServer extends AbstractVerticle {

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

		com.ctrip.framework.apollo.Config config = ConfigService.getAppConfig(); // config instance is singleton for
		// each namespace and is never null
		int vertHttpPort = config.getIntProperty("vertx.http.port", 0);
		// 创建带安全选项的 HTTP 服务器配置
		HttpServerOptions serverOptions = new HttpServerOptions().setHandle100ContinueAutomatically(false)
				.setMaxInitialLineLength(4096)
				.setMaxHeaderSize(8192)
				.setMaxChunkSize(8192);

		Router router = Router.router(vertx);

		// 添加安全过滤器（放在最前面）
		router.route().handler(new SecurityHandler());

		// Body 处理器
		router.route()
				.handler(BodyHandler.create().setHandleFileUploads(false).setMergeFormAttributes(false).setDeleteUploadedFilesOnEnd(true));

		// CORS 配置
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

		router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

		// 静态资源处理器配置
		StaticHandler staticHandler = StaticHandler.create()
				.setAllowRootFileSystemAccess(false)
				.setCachingEnabled(true)
				.setFilesReadOnly(true)
				.setMaxAgeSeconds(24 * 60 * 60) // 24小时缓存
				.setDirectoryListing(false); // 禁止目录浏览

		router.route("/*").handler(staticHandler);

		regRouter(router);

		// 错误处理
		router.route().failureHandler(ctx -> {
			ctx.response()
					.setStatusCode(500)
					.putHeader("Content-Type", "application/json")
					.end(new JsonObject().put("error", "Internal Server Error").encode());
		});

		// 启动服务器
		vertx.createHttpServer(serverOptions)
				.requestHandler(router)
				.listen(vertHttpPort)
				.onSuccess(server -> System.out.println("HTTP server started on port " + vertHttpPort))
				.onFailure(error -> System.err.println("Failed to start HTTP server: " + error.getMessage()));
	}

	/** 
	 * 注册业务路由
	 * @param router
	 */
	protected void regRouter(Router router) {
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
		router.route("/gm/payOrderSuccess").handler(new GmPayOrderSuccessReq());
		router.route("/wx_pay").handler(new WeChatPayPageReq());
		router.route("/wx_customer").handler(new WeChatCustomerServiceReq());
		router.route("/wx_pay_callback").handler(new PayCallbackSuccessReq());
		router.route("/sojump_callback").handler(new SojumpCallbackReq());
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

}


// 安全处理器实现
class SecurityHandler implements Handler<RoutingContext> {
	@Override
	public void handle(RoutingContext ctx) {
		String path = ctx.request().path();

		try {
			String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

			// 检查危险路径模式
			if (decodedPath.contains("../") || decodedPath.contains("..\\") || decodedPath.contains("%2e%2e%2f")
					|| decodedPath.contains("%2e%2e/") || decodedPath.contains("..%2f") || !decodedPath.matches("^[a-zA-Z0-9/._-]+$")) {

				ctx.response()
						.setStatusCode(400)
						.putHeader("Content-Type", "application/json")
						.end(new JsonObject().put("error", "Invalid request path").encode());
				return;
			}

			// 添加安全响应头
//			ctx.response()
//					.putHeader("X-Content-Type-Options", "nosniff")
//					.putHeader("X-Frame-Options", "DENY")
//					.putHeader("X-XSS-Protection", "1; mode=block")
//					.putHeader("Strict-Transport-Security", "max-age=31536000")
//					.putHeader("Content-Security-Policy", "default-src 'self'");

			ctx.next();

		} catch (Exception e) {
			ctx.response().setStatusCode(400).end("Invalid request");
		}
	}
}
