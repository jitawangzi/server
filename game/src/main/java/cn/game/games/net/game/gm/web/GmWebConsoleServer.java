package cn.game.games.net.game.gm.web;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmResult;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.VirtualClockService;
import cn.game.protocol.manual.ErrorMsgEnum;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * GameServer纯前端JSON驱动Web控制台服务 (DC-12)
 *
 * @author shuyongqiang
 */
public class GmWebConsoleServer {

	private static final Logger log = LoggerFactory.getLogger(GmWebConsoleServer.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static volatile GmWebConsoleServer instance;

	private final Vertx vertx;
	private final boolean externalVertx;

	private HttpServer server;
	private int activePort;
	private String cachedIndexHtml;

	public GmWebConsoleServer() {
		this(null);
	}

	public GmWebConsoleServer(Vertx vertx) {
		this.vertx = vertx;
		this.externalVertx = (vertx != null);
	}

	public static GmWebConsoleServer getInstance() {
		GmWebConsoleServer current = instance;
		if (current == null) {
			synchronized (GmWebConsoleServer.class) {
				current = instance;
				if (current == null) {
					current = new GmWebConsoleServer();
					instance = current;
				}
			}
		}
		return current;
	}

	/**
	 * 启动HTTP Web控制台服务器
	 *
	 * @param host 监听主机
	 * @param port 监听端口（0由OS动态分配）
	 * @return 包含实际监听端口的Future
	 */
	public synchronized Future<Integer> start(String host, int port) {
		Vertx effectiveVertx = (this.vertx != null) ? this.vertx : (VxHolder.vertx != null ? VxHolder.vertx : Vertx.vertx());
		Router router = buildRouter(effectiveVertx);
		HttpServerOptions options = new HttpServerOptions().setHost(host).setPort(port);
		this.server = effectiveVertx.createHttpServer(options);

		return this.server.requestHandler(router)
				.listen(port, host)
				.map(HttpServer::actualPort)
				.onSuccess(p -> {
					this.activePort = p;
					log.info("GM Web Console 启动成功, listening on http://{}:{}", host, p);
				})
				.onFailure(err -> {
					log.error("GM Web Console 启动失败 on http://{}:{}", host, port, err);
				});
	}

	/**
	 * 停止HTTP Web控制台服务
	 *
	 * @return 停止完成Future
	 */
	public synchronized Future<Void> stop() {
		if (server != null) {
			HttpServer s = server;
			server = null;
			activePort = 0;
			return s.close().onSuccess(v -> log.info("GM Web Console 已停止"));
		}
		return Future.succeededFuture();
	}

	/**
	 * 在服务器启动时根据安全策略判断并挂载启动
	 */
	public synchronized void startIfEnabled() {
		RunMode mode = GmSecurityPolicy.currentRunMode();
		if (!GmSecurityPolicy.isHttpGmEnabled(mode)) {
			log.info("GM HTTP 控制台未启用 (runMode={})", mode);
			return;
		}
		if (server != null) {
			log.warn("GM HTTP 控制台已处于运行状态, activePort={}", activePort);
			return;
		}
		try {
			int port = GmSecurityPolicy.resolveHttpPort();
			String host = GmSecurityPolicy.resolveHttpHost(mode);
			start(host, port);
		} catch (Throwable e) {
			log.error("启动 GM Web 控制台服务失败", e);
		}
	}

	public int getActivePort() {
		return activePort;
	}

	private Router buildRouter(Vertx effectiveVertx) {
		Router router = Router.router(effectiveVertx);

		// 跨域支持 (OPTIONS放行)
		router.route("/gm-console/*").handler(ctx -> {
			ctx.response().putHeader("Access-Control-Allow-Origin", "*");
			ctx.response().putHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			ctx.response().putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-GM-Token");
			if (ctx.request().method() == HttpMethod.OPTIONS) {
				ctx.response().setStatusCode(204).end();
				return;
			}
			ctx.next();
		});

		// JSON请求体解析
		router.route("/gm-console/api/*").handler(BodyHandler.create());

		// 安全与鉴权拦截器
		router.route("/gm-console/api/*").handler(this::handleAuth);
		router.route("/gm-console/panels/*").handler(this::handleAuth);

		// 1. GET /gm-console/api/bootstrap
		router.get("/gm-console/api/bootstrap").handler(ctx -> {
			RunMode mode = GmSecurityPolicy.currentRunMode();
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("serverId", ServerContext.getInstance().getServerId());
			data.put("runMode", mode.name());
			data.put("production", mode.isProduction());
			data.put("requireAuth", GmSecurityPolicy.isRequireAuth(mode));
			data.put("virtualClock", VirtualClockService.getInstance().getStatusMap());
			data.put("schema", GmDispatcher.getInstance().getSchemaCatalog(mode));
			data.put("panels", GmConsolePanelLoader.listPanels(mode));
			sendJson(ctx, GmResult.success("ok", data));
		});

		// 2. POST /gm-console/api/execute
		router.post("/gm-console/api/execute").handler(ctx -> {
			JsonObject json;
			try {
				json = ctx.body().asJsonObject();
			} catch (Exception e) {
				ctx.response().setStatusCode(400);
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "请求体非合法JSON: " + e.getMessage()));
				return;
			}
			if (json == null) {
				ctx.response().setStatusCode(400);
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "请求体不能为空"));
				return;
			}
			String action = json.getString("action");
			String cmd = json.getString("cmd");
			long playerId = json.getLong("playerId", 0L);
			String traceId = json.getString("traceId");
			boolean dryRun = json.getBoolean("dryRun", false);
			JsonObject argsObj = json.getJsonObject("args");
			Map<String, Object> args = argsObj != null ? argsObj.getMap() : Collections.emptyMap();

			effectiveVertx.executeBlocking(() -> {
				GmResult result;
				if (action != null && !action.isBlank()) {
					result = GmDispatcher.getInstance().dispatchAction(action.trim(), playerId, args, traceId, dryRun);
				} else if (cmd != null && !cmd.isBlank()) {
					result = GmDispatcher.getInstance().dispatchHttpText(playerId, cmd.trim(), traceId, dryRun);
				} else {
					result = GmResult.fail(ErrorMsgEnum.request_parameter_error, "action 与 cmd 不能同时为空");
				}
				return result;
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, ar.result());
				} else {
					log.error("执行GM HTTP请求异常 action={}, cmd={}", action, cmd, ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "内部执行异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 3. GET /gm-console/api/panels
		router.get("/gm-console/api/panels").handler(ctx -> {
			RunMode mode = GmSecurityPolicy.currentRunMode();
			sendJson(ctx, GmResult.success("ok", GmConsolePanelLoader.listPanels(mode)));
		});

		// 4. GET /gm-console/panels/:panelFile
		router.get("/gm-console/panels/:panelFile").handler(ctx -> {
			RunMode mode = GmSecurityPolicy.currentRunMode();
			String panelFile = ctx.pathParam("panelFile");
			try {
				String html = GmConsolePanelLoader.loadPanelContent(panelFile, mode);
				ctx.response().putHeader("Content-Type", "text/html; charset=utf-8");
				ctx.response().putHeader("Cache-Control", "no-store");
				ctx.response().end(html);
			} catch (LogicException e) {
				int status = 400;
				if (e.getErrorCode() == ErrorMsgEnum.production_gm_not_allow.getId()) {
					status = 403;
				} else if (e.getErrorCode() == ErrorMsgEnum.gm_cmd_not_exist.getId()) {
					status = 404;
				}
				ctx.response().setStatusCode(status);
				sendJson(ctx, GmResult.fail(e.getErrorCode(), "ERROR_" + e.getErrorCode(), e.getMessage()));
			} catch (Exception e) {
				ctx.response().setStatusCode(500);
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, e.getMessage()));
			}
		});

		// 5. 静态首页单页控制台
		Handler<RoutingContext> indexHandler = ctx -> {
			String html = loadIndexHtml();
			ctx.response().putHeader("Content-Type", "text/html; charset=utf-8").end(html);
		};
		router.get("/gm-console").handler(indexHandler);
		router.get("/gm-console/").handler(indexHandler);
		router.get("/gm-console/index.html").handler(indexHandler);

		return router;
	}

	private void handleAuth(RoutingContext ctx) {
		RunMode mode = GmSecurityPolicy.currentRunMode();
		if (GmSecurityPolicy.isRequireAuth(mode)) {
			String authHeader = ctx.request().getHeader("Authorization");
			String gmTokenHeader = ctx.request().getHeader("X-GM-Token");
			String token = GmSecurityPolicy.extractToken(authHeader, gmTokenHeader);
			if (!GmSecurityPolicy.validateToken(token)) {
				ctx.response().setStatusCode(401);
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.gm_not, "GM HTTP 鉴权未通过"));
				return;
			}
		}
		ctx.next();
	}

	private void sendJson(RoutingContext ctx, Object obj) {
		try {
			String json = MAPPER.writeValueAsString(obj);
			ctx.response()
					.putHeader("Content-Type", "application/json; charset=utf-8")
					.end(json);
		} catch (Exception e) {
			log.error("序列化JSON响应失败", e);
			ctx.response().setStatusCode(500).end("{\"ok\":false,\"message\":\"JSON serialization error\"}");
		}
	}

	private synchronized String loadIndexHtml() {
		if (cachedIndexHtml != null) {
			return cachedIndexHtml;
		}
		try (InputStream in = getClass().getClassLoader().getResourceAsStream("gm-console/index.html")) {
			if (in != null) {
				cachedIndexHtml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
				return cachedIndexHtml;
			}
		} catch (Exception e) {
			log.warn("读取 classpath gm-console/index.html 失败", e);
		}
		return "<!DOCTYPE html><html><head><title>GM Console</title></head><body><h1>GM Console</h1><p>Ready</p></body></html>";
	}
}
