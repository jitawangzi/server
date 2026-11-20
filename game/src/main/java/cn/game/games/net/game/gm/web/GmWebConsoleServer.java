package cn.game.games.net.game.gm.web;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.client.GameClient.GameClientState;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmResult;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.VirtualClockService;
import cn.game.games.net.game.gm.acceptance.AcceptanceRunMode;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceEngine;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceReport;
import cn.game.games.net.game.gm.proto.ProtocolSimulationService;
import cn.game.games.net.game.gm.proto.ProtocolSimulationService.ProtoInvokeRequest;
import cn.game.games.net.game.gm.proto.VirtualGameClient;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.ObjUtil;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
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
	private String cachedWorkbenchJs;

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

		// 6. GET /gm-console/api/workbench/modules
		router.get("/gm-console/api/workbench/modules").handler(ctx -> {
			String keyword = ctx.request().getParam("keyword");
			if (keyword == null) {
				keyword = ctx.request().getParam("k");
			}
			final String finalKeyword = keyword;
			effectiveVertx.executeBlocking(() -> {
				FeatureWorkbenchService service = new FeatureWorkbenchService();
				return service.buildCatalog(finalKeyword);
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, GmResult.success("ok", ar.result()));
				} else {
					log.error("构建工作台模块目录异常", ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "构建工作台模块目录异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 7. GET /gm-console/api/workbench/players
		router.get("/gm-console/api/workbench/players").handler(ctx -> {
			RunMode mode = GmSecurityPolicy.currentRunMode();
			if (mode.isProduction()) {
				Map<String, Object> data = new LinkedHashMap<>();
				data.put("simulationEnabled", false);
				data.put("players", Collections.emptyList());
				sendJson(ctx, GmResult.success("ok", data));
				return;
			}
			effectiveVertx.executeBlocking(() -> {
				List<Map<String, Object>> players = new ArrayList<>();
				for (Player p : PlayerManager.getInstance().getAllPlayer().values()) {
					if (p != null && !p.isIslogouting() && p.isOnline()) {
						Map<String, Object> pInfo = new LinkedHashMap<>();
						pInfo.put("playerId", p.getPlayerId());
						pInfo.put("name", p.getData() != null ? p.getData().getName() : "");
						pInfo.put("level", p.getData() != null ? p.getData().getLevel() : 0);
						pInfo.put("hasRealWsClient", p.getGameClient() != null && !(p.getGameClient() instanceof VirtualGameClient));
						pInfo.put("isVirtualClient", p.getGameClient() instanceof VirtualGameClient);
						players.add(pInfo);
					}
				}
				players.sort(Comparator.comparingLong(m -> (long) m.get("playerId")));
				Map<String, Object> data = new LinkedHashMap<>();
				data.put("simulationEnabled", true);
				data.put("players", players);
				return data;
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, GmResult.success("ok", ar.result()));
				} else {
					log.error("获取在线玩家列表异常", ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "获取在线玩家列表异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 8. POST /gm-console/api/proto/invoke
		router.post("/gm-console/api/proto/invoke").handler(ctx -> {
			RunMode mode = GmSecurityPolicy.currentRunMode();
			if (mode.isProduction()) {
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.production_gm_not_allow, "生产环境禁止执行协议模拟调用"));
				return;
			}
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

			ProtoInvokeRequest req = new ProtoInvokeRequest();
			req.setPlayerId(json.getLong("playerId", 0L));
			req.setMsgId(json.getInteger("msgId", 0));
			req.setMsgName(json.getString("msgName"));
			req.setSeq(json.getInteger("seq", 0));
			req.setMirrorToRealClient(json.getBoolean("mirrorToRealClient", false));
			req.setWithTrace(json.getBoolean("withTrace", true));
			req.setWithDiff(json.getBoolean("withDiff", true));
			req.setTimeoutMs(json.getInteger("timeoutMs", 5000));
			JsonObject payloadObj = json.getJsonObject("payload");
			if (payloadObj != null) {
				req.setPayload(payloadObj.getMap());
			}
			JsonArray diffMods = json.getJsonArray("diffModules");
			if (diffMods != null) {
				List<String> dList = new ArrayList<>();
				for (Object d : diffMods) {
					dList.add(String.valueOf(d));
				}
				req.setDiffModules(dList);
			}

			effectiveVertx.executeBlocking(() -> {
				return ProtocolSimulationService.getInstance().invoke(req);
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, ar.result());
				} else {
					log.error("执行协议模拟请求异常 msgId={}", req.getMsgId(), ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "执行协议模拟异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 9. POST /gm-console/api/workbench/test-login
		router.post("/gm-console/api/workbench/test-login").handler(ctx -> {
			RunMode mode = GmSecurityPolicy.currentRunMode();
			if (mode.isProduction()) {
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.production_gm_not_allow, "生产环境禁止执行测试登录"));
				return;
			}
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

			long playerId = json.getLong("playerId", 0L);
			String accountId = json.getString("accountId");
			if (accountId == null || accountId.isBlank()) {
				accountId = json.getString("account");
			}
			if (accountId != null) {
				accountId = accountId.trim();
			}
			String serverId = json.getString("serverId");
			if (serverId == null || serverId.isBlank()) {
				serverId = ServerContext.getInstance().getServerId() != null ? ServerContext.getInstance().getServerId() : "game_1";
			}

			// 优先根据 accountId 检索已有的角色对象
			Player targetPlayer = null;
			if (accountId != null && !accountId.isEmpty()) {
				for (Player p : PlayerManager.getInstance().getAllPlayer().values()) {
					if (p.getData() != null) {
						if (accountId.equalsIgnoreCase(p.getData().getAccountId())
								|| accountId.equalsIgnoreCase(p.getData().getName())) {
							targetPlayer = p;
							break;
						}
					}
				}
			}

			// 若未通过账号匹配到，且指定了有效 playerId，则直接根据 playerId 获取
			if (targetPlayer == null && playerId > 0) {
				targetPlayer = PlayerManager.getInstance().getPlayer(playerId);
			}

			// 若角色已存在，确保其在线并挂载 VirtualGameClient，直接复用
			if (targetPlayer != null) {
				targetPlayer.setOnline(true);
				targetPlayer.setIslogouting(false);
				if (targetPlayer.getGameClient() == null || !targetPlayer.getGameClient().isActive()) {
					targetPlayer.setGameClient(new VirtualGameClient(targetPlayer.getPlayerId()));
				}
				Map<String, Object> data = new LinkedHashMap<>();
				data.put("ok", true);
				data.put("reused", true);
				data.put("playerId", targetPlayer.getPlayerId());
				data.put("name", targetPlayer.getData() != null ? targetPlayer.getData().getName() : "");
				data.put("level", targetPlayer.getData() != null ? targetPlayer.getData().getLevel() : 1);
				data.put("accountId", targetPlayer.getData() != null ? targetPlayer.getData().getAccountId() : "");
				data.put("isVirtualClient", targetPlayer.getGameClient() instanceof VirtualGameClient);
				sendJson(ctx, GmResult.success("ok", data));
				return;
			}

			// 若角色不存在，在非生产测试环境下自闭环快速创建测试角色并挂载 VirtualGameClient
			long allocatedPid = playerId > 0 ? playerId : 0L;
			if (allocatedPid <= 0) {
				if (accountId != null && !accountId.isEmpty()) {
					long hashPid = Math.abs((long) accountId.hashCode()) % 900000L + 100000L;
					while (PlayerManager.getInstance().getPlayer(hashPid) != null) {
						hashPid++;
					}
					allocatedPid = hashPid;
				} else {
					long autoPid = 200001L;
					while (PlayerManager.getInstance().getPlayer(autoPid) != null) {
						autoPid++;
					}
					allocatedPid = autoPid;
				}
			}

			String effectiveAccount = (accountId != null && !accountId.isEmpty()) ? accountId : "test_" + allocatedPid;
			String effectiveName = effectiveAccount;

			PlayerData data = new PlayerData();
			ObjUtil.setDefaultValue(data);
			data.setPlayerId(allocatedPid);
			data.setUid(allocatedPid);
			data.setAccountId(effectiveAccount);
			data.setName(effectiveName);
			data.setServerId(serverId);
			data.setLevel(1);

			Player newPlayer = new Player(data);
			newPlayer.setOnline(true);
			newPlayer.setIslogouting(false);
			VirtualGameClient vClient = new VirtualGameClient(allocatedPid);
			newPlayer.setGameClient(vClient);

			try {
				PlayerManager.getInstance().initAdd(newPlayer);
			} catch (Throwable t) {
				log.warn("initAdd test player warning: pid={}, msg={}", allocatedPid, t.getMessage());
			}

			Map<String, Object> respData = new LinkedHashMap<>();
			respData.put("ok", true);
			respData.put("reused", false);
			respData.put("playerId", allocatedPid);
			respData.put("name", effectiveName);
			respData.put("level", 1);
			respData.put("accountId", effectiveAccount);
			respData.put("isVirtualClient", true);
			sendJson(ctx, GmResult.success("ok", respData));
		});

		// 10. GET /gm-console/workbench.js
		router.get("/gm-console/workbench.js").handler(ctx -> {
			String js = loadWorkbenchJs();
			ctx.response().putHeader("Content-Type", "application/javascript; charset=utf-8").end(js);
		});

		// 11. GET /gm-console/api/workbench/acceptance/suites
		router.get("/gm-console/api/workbench/acceptance/suites").handler(ctx -> {
			RunMode mode = GmSecurityPolicy.currentRunMode();
			if (mode.isProduction()) {
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.production_gm_not_allow, "生产环境禁止访问模块验收套件"));
				return;
			}
			sendJson(ctx, GmResult.success("ok", Map.of("suites", ModuleAcceptanceEngine.getInstance().listSuiteSummaries())));
		});

		// 12. POST /gm-console/api/workbench/acceptance/run
		router.post("/gm-console/api/workbench/acceptance/run").handler(ctx -> {
			RunMode mode = GmSecurityPolicy.currentRunMode();
			if (mode.isProduction()) {
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.production_gm_not_allow, "生产环境禁止执行模块自动化验收"));
				return;
			}
			io.vertx.core.json.JsonObject json;
			try {
				json = ctx.body().asJsonObject();
			} catch (Exception e) {
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "请求体非合法JSON: " + e.getMessage()));
				return;
			}
			if (json == null) {
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "请求体不能为空"));
				return;
			}

			Object modIdObj = json.getValue("moduleId");
			int moduleId = 0;
			if (modIdObj instanceof Number) {
				moduleId = ((Number) modIdObj).intValue();
			} else if (modIdObj instanceof String) {
				String s = ((String) modIdObj).trim();
				try {
					if (s.startsWith("0x") || s.startsWith("0X")) {
						moduleId = Integer.parseInt(s.substring(2), 16);
					} else {
						moduleId = Integer.parseInt(s);
					}
				} catch (Exception e) {
					moduleId = 0;
				}
			}
			if (moduleId <= 0) {
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "moduleId 必须大于0"));
				return;
			}
			if (ModuleAcceptanceEngine.getInstance().getSuite(moduleId) == null) {
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "该模块尚未注册自动化验收套件: " + moduleId));
				return;
			}

			String modeStr = json.getString("mode", "ALL");
			AcceptanceRunMode runMode = AcceptanceRunMode.ALL;
			try {
				runMode = AcceptanceRunMode.valueOf(modeStr.toUpperCase());
			} catch (Exception e) {
				runMode = AcceptanceRunMode.ALL;
			}
			String caseId = json.getString("caseId", "");
			int maxMatrixRows = json.getInteger("maxMatrixRows", 2000);

			final int targetModuleId = moduleId;
			final AcceptanceRunMode targetMode = runMode;
			final String targetCaseId = caseId;
			final int targetMaxRows = maxMatrixRows;

			Thread.startVirtualThread(() -> {
				try {
					ModuleAcceptanceReport report = ModuleAcceptanceEngine.getInstance().runSuite(targetModuleId, targetMode, targetCaseId, targetMaxRows);
					sendJson(ctx, GmResult.success("ok", report.toMap()));
				} catch (Throwable t) {
					log.warn("执行自动化验收失败: moduleId={}", targetModuleId, t);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "执行自动化验收异常: " + t.getMessage()));
				}
			});
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

	private synchronized String loadWorkbenchJs() {
		if (cachedWorkbenchJs != null) {
			return cachedWorkbenchJs;
		}
		try (InputStream in = getClass().getClassLoader().getResourceAsStream("gm-console/workbench.js")) {
			if (in != null) {
				cachedWorkbenchJs = new String(in.readAllBytes(), StandardCharsets.UTF_8);
				return cachedWorkbenchJs;
			}
		} catch (Exception e) {
			log.warn("读取 classpath gm-console/workbench.js 失败", e);
		}
		return "/* gm workbench.js not found */";
	}
}
