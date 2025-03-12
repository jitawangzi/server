package cn.game.login;

import com.ctrip.framework.apollo.ConfigService;

import cn.game.util.SpringContextLoader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

public class RestServer extends AbstractVerticle {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestServer.class);

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

		Router router = SpringContextLoader.getContext().getBean(Router.class);
//		Router router = Router.router(vertx);

		// 添加安全过滤器（放在最前面）
//		router.route().handler(new SecurityHandler());

		// Body 处理器
//		router.route()
//				.handler(BodyHandler.create().setHandleFileUploads(false).setMergeFormAttributes(false).setDeleteUploadedFilesOnEnd(true));
//
//		// CORS 配置
//		Set<String> allowedHeaders = new HashSet<>();
//		allowedHeaders.add("x-requested-with");
//		allowedHeaders.add("Access-Control-Allow-Origin");
//		allowedHeaders.add("origin");
//		allowedHeaders.add("Content-Type");
//		allowedHeaders.add("accept");
//
//		Set<HttpMethod> allowedMethods = new HashSet<>();
//		allowedMethods.add(HttpMethod.GET);
//		allowedMethods.add(HttpMethod.POST);
//		allowedMethods.add(HttpMethod.DELETE);
//		allowedMethods.add(HttpMethod.PATCH);
//		allowedMethods.add(HttpMethod.OPTIONS);
//		allowedMethods.add(HttpMethod.PUT);
//
//		router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));
//
//		// 静态资源处理器配置
//		StaticHandler staticHandler = StaticHandler.create()
//				.setAllowRootFileSystemAccess(false)
//				.setCachingEnabled(true)
//				.setFilesReadOnly(true)
//				.setMaxAgeSeconds(24 * 60 * 60) // 24小时缓存
//				.setDirectoryListing(false); // 禁止目录浏览
//
//		router.route("/*").handler(staticHandler);
//
		regRouter(router);

		// 错误处理
//		router.route().failureHandler(ctx -> {
//			ctx.response()
//					.setStatusCode(500)
//					.putHeader("Content-Type", "application/json")
//					.end(new JsonObject().put("error", "Internal Server Error").encode());
//		});

		// 启动服务器
		vertx.createHttpServer(serverOptions)
				.requestHandler(router)
				.connectionHandler(ctx -> {
					if (log.isDebugEnabled()) {
						log.debug("New connection from: " + ctx.remoteAddress());
					}
				})
				.exceptionHandler(error -> {
					if (error instanceof java.net.SocketException && error.getMessage().contains("Connection reset")) {
						return;
					}
					log.error("Connection error: ", error);
				})
				.listen(vertHttpPort)
				.onSuccess(server -> log.info("HTTP server started on port " + vertHttpPort))
				.onFailure(error -> log.error("Failed to start HTTP server: " + error.getMessage()));
	}

	/** 
	 * 注册业务路由
	 * @param router
	 */
	protected void regRouter(Router router) {
//		router.route("/account/register").handler(new VertxRegisterReq());
//		router.route("/account/third_party_confirm").handler(new VertxThirdPartyConfirmReq());
//		router.route("/account/server_list").handler(new VertxServerListReq());
//		router.route("/wechat/ship/push").handler(new WechatShipPush());
//		router.route("/changyou/ship/push").handler(new ChangYouShipPush());
//		router.route("/wechat/test").handler(new WechatTest());
//		router.route("/gm/order_list").handler(new GmSelectOrderReq());
//		router.route("/gm/ip_whitelist").handler(new GmIpWhitelistReq());
//		router.route("/gm/add_ip_whitelist").handler(new GmAddIpWhitelistReq());
//		router.route("/gm/del_ip_whitelist").handler(new GmDelIpWhitelistReq());
//		router.route("/gm/addNotice").handler(new GmAddNoticeReq());
//		router.route("/gm/delNotice").handler(new GmDelNoticeReq());
//		router.route("/gm/NoticeList").handler(new GmNoticeListReq());
//		router.route("/gm/optList").handler(new GmOptListReq());
//		router.route("/gm/payOrderSuccess").handler(new GmPayOrderSuccessReq());
//		router.route("/wx_pay").handler(new WeChatPayPageReq());
//		router.route("/wx_customer").handler(new WeChatCustomerServiceReq());
//		router.route("/wx_pay_callback").handler(new PayCallbackSuccessReq());
//		router.route("/sojump_callback").handler(new SojumpCallbackReq());
	}
}

