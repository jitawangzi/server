package cn.game.games.net.game.gm.web;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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
import cn.game.games.net.game.gm.GmActionSchema;
import cn.game.games.net.game.gm.GmAuditLogService;
import cn.game.games.net.game.gm.GmAuthPrincipal;
import cn.game.games.net.game.gm.GmCommandTokenizer;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmResult;
import cn.game.games.net.game.gm.GmRole;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.GmSecurityTier;
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

	public static final String PROP_STATIC_DIR = "gm.http.staticDir";
	public static final String ENV_STATIC_DIR = "GM_HTTP_STATIC_DIR";

	private static volatile GmWebConsoleServer instance;

	private final Vertx vertx;
	private final boolean externalVertx;

	private HttpServer server;
	private int activePort;

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
			GmAuthPrincipal principal = getPrincipal(ctx);
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("serverId", ServerContext.getInstance().getServerId());
			data.put("runMode", mode.name());
			data.put("production", mode.isProduction());
			data.put("requireAuth", GmSecurityPolicy.isRequireAuth(mode));
			data.put("principal", principal.toViewMap());
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
			GmAuthPrincipal principal = getPrincipal(ctx);

			// RBAC 细粒度动作鉴权拦截（支持未识别命令 fail-closed）
			String resolvedActionKey = resolveActionKey(action, cmd);
			GmActionSchema actionSchema = resolvedActionKey != null ? GmDispatcher.getInstance().getActionSchema(resolvedActionKey) : null;
			if (actionSchema != null) {
				if (!GmSecurityPolicy.isRoleAllowedForAction(principal.getRole(), actionSchema.getSecurity(),
						actionSchema.getId(), actionSchema.isReadOnly())) {
					String denyMsg = "当前角色 [" + principal.getRole().name() + "] 无权执行动作: " + actionSchema.getId();
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/execute", actionSchema.getId(),
							playerId, dryRun, args, false, ErrorMsgEnum.gm_not.getId(), denyMsg, 0, 0L);
					ctx.response().setStatusCode(403);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.gm_not, denyMsg));
					return;
				}
			} else if (principal.getRole() != GmRole.ADMIN) {
				String denyMsg = "当前角色 [" + principal.getRole().name() + "] 无权执行未注册或不可识别指令: "
						+ (resolvedActionKey != null ? resolvedActionKey : "");
				GmAuditLogService.getInstance().record(principal, "/gm-console/api/execute",
						resolvedActionKey != null ? resolvedActionKey : "unknown.cmd",
						playerId, dryRun, Map.of("cmd", cmd != null ? cmd : ""), false, ErrorMsgEnum.gm_not.getId(), denyMsg, 0, 0L);
				ctx.response().setStatusCode(403);
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.gm_not, denyMsg));
				return;
			}

			final Object auditArgs = (action != null && !action.isBlank()) ? args : Map.of("cmd", cmd != null ? cmd : "");
			final String auditActionName = actionSchema != null ? actionSchema.getId()
					: (resolvedActionKey != null ? resolvedActionKey : "");

			effectiveVertx.executeBlocking(() -> {
				cn.game.games.net.game.gm.repair.OpsRepairService.setAuthenticatedOperator(principal.getOperator());
				try {
					GmResult result;
					if (action != null && !action.isBlank()) {
						result = GmDispatcher.getInstance().dispatchAction(action.trim(), playerId, args, traceId, dryRun);
					} else if (cmd != null && !cmd.isBlank()) {
						result = GmDispatcher.getInstance().dispatchHttpText(playerId, cmd.trim(), traceId, dryRun);
					} else {
						result = GmResult.fail(ErrorMsgEnum.request_parameter_error, "action 与 cmd 不能同时为空");
					}
					return result;
				} finally {
					cn.game.games.net.game.gm.repair.OpsRepairService.clearAuthenticatedOperator();
				}
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					GmResult res = ar.result();
					int diffCnt = res.getDiff() instanceof Collection<?> c ? c.size()
							: (res.getDiff() instanceof Map<?, ?> m ? m.size() : 0);
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/execute", auditActionName,
							playerId, dryRun, auditArgs, res.isOk(), res.getErrorCode(), res.getSummary(), diffCnt, res.getElapsedMs());
					sendJson(ctx, res);
				} else {
					log.error("执行GM HTTP请求异常 action={}, cmd={}", action, cmd, ar.cause());
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/execute", auditActionName,
							playerId, dryRun, auditArgs, false, ErrorMsgEnum.unknown.getId(), ar.cause().getMessage(), 0, 0L);
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
			if (!requireRole(ctx, GmRole.ADMIN)) {
				return;
			}
			JsonObject json = parseJsonBody(ctx, true);
			if (json == null) {
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

			GmAuthPrincipal principal = getPrincipal(ctx);
			effectiveVertx.executeBlocking(() -> {
				return ProtocolSimulationService.getInstance().invoke(req);
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					Map<String, Object> outcome = ar.result();
					boolean ok = Boolean.TRUE.equals(outcome.get("ok"));
					int errCode = outcome.get("errorCode") instanceof Number n ? n.intValue() : 0;
					long elapsed = outcome.get("elapsedMs") instanceof Number n ? n.longValue() : 0L;
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/proto/invoke",
							req.getMsgName() != null ? req.getMsgName() : String.valueOf(req.getMsgId()),
							req.getPlayerId(), false, req.getPayload(), ok, errCode,
							String.valueOf(outcome.getOrDefault("errorName", ok ? "ok" : "failed")), 0, elapsed);
					sendJson(ctx, outcome);
				} else {
					log.error("执行协议模拟请求异常 msgId={}", req.getMsgId(), ar.cause());
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/proto/invoke",
							req.getMsgName() != null ? req.getMsgName() : String.valueOf(req.getMsgId()),
							req.getPlayerId(), false, req.getPayload(), false, ErrorMsgEnum.unknown.getId(),
							ar.cause().getMessage(), 0, 0L);
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
			if (!requireRole(ctx, GmRole.ADMIN)) {
				return;
			}
			JsonObject json = parseJsonBody(ctx, true);
			if (json == null) {
				return;
			}
			GmAuthPrincipal principal = getPrincipal(ctx);

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

			// 若角色已存在，先检查是否存在活跃真实连接；仅对非活跃真实连接恢复在线态并按需挂载 VirtualGameClient
			if (targetPlayer != null) {
				boolean hasActiveRealClient = targetPlayer.getGameClient() != null
						&& !(targetPlayer.getGameClient() instanceof VirtualGameClient)
						&& targetPlayer.getGameClient().isActive();
				if (!hasActiveRealClient) {
					targetPlayer.setOnline(true);
					targetPlayer.setIslogouting(false);
					if (targetPlayer.getGameClient() == null || !targetPlayer.getGameClient().isActive()) {
						targetPlayer.setGameClient(new VirtualGameClient(targetPlayer.getPlayerId()));
					}
				}
				Map<String, Object> data = new LinkedHashMap<>();
				data.put("ok", true);
				data.put("reused", true);
				data.put("playerId", targetPlayer.getPlayerId());
				data.put("name", targetPlayer.getData() != null ? targetPlayer.getData().getName() : "");
				data.put("level", targetPlayer.getData() != null ? targetPlayer.getData().getLevel() : 1);
				data.put("accountId", targetPlayer.getData() != null ? targetPlayer.getData().getAccountId() : "");
				data.put("isVirtualClient", targetPlayer.getGameClient() instanceof VirtualGameClient);
				GmAuditLogService.getInstance().record(principal, "/gm-console/api/workbench/test-login",
						"workbench.testLogin", targetPlayer.getPlayerId(), false,
						Map.of("reused", true, "hasActiveRealClient", hasActiveRealClient), true, 0, "ok", 0, 0L);
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
				if (newPlayer.getModules().isEmpty()) {
					newPlayer.initModule(new java.util.HashMap<String, cn.game.games.core.BasePlayerModule>());
				}
				PlayerManager.getInstance().initAdd(newPlayer);
				if (newPlayer.getPlayerModule() != null) {
					newPlayer.getPlayerModule().initLevel();
				}
				if (newPlayer.getFuncModule() != null) {
					newPlayer.getFuncModule().initFuncOpenData();
				}
			} catch (Throwable t) {
				log.warn("初始化测试玩家基础模块告警: pid={}", allocatedPid, t);
			}

			Map<String, Object> respData = new LinkedHashMap<>();
			respData.put("ok", true);
			respData.put("reused", false);
			respData.put("playerId", allocatedPid);
			respData.put("name", effectiveName);
			respData.put("level", 1);
			respData.put("accountId", effectiveAccount);
			respData.put("isVirtualClient", true);
			GmAuditLogService.getInstance().record(principal, "/gm-console/api/workbench/test-login",
					"workbench.testLogin", allocatedPid, false,
					Map.of("reused", false, "accountId", effectiveAccount), true, 0, "created", 0, 0L);
			sendJson(ctx, GmResult.success("ok", respData));
		});

		// 10. GET /gm-console/workbench.js
		router.get("/gm-console/workbench.js").handler(ctx -> {
			String js = loadWorkbenchJs();
			ctx.response()
					.putHeader("Content-Type", "application/javascript; charset=utf-8")
					.putHeader("Cache-Control", "no-store")
					.end(js);
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
			if (!requireRole(ctx, GmRole.ADMIN)) {
				return;
			}
			JsonObject json = parseJsonBody(ctx, true);
			if (json == null) {
				return;
			}
			GmAuthPrincipal principal = getPrincipal(ctx);

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
					log.warn("解析 moduleId 参数失败: {}", s, e);
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
				log.warn("未知验收运行模式 mode={}，降级使用 ALL", modeStr, e);
				runMode = AcceptanceRunMode.ALL;
			}
			String caseId = json.getString("caseId", "");
			int maxMatrixRows = json.getInteger("maxMatrixRows", 2000);
			Boolean reqLiveObj = json.getBoolean("requireLiveCluster");
			boolean requireLiveCluster = (reqLiveObj != null) ? reqLiveObj : ModuleAcceptanceEngine.getInstance().isLiveServerRuntime();

			final int targetModuleId = moduleId;
			final AcceptanceRunMode targetMode = runMode;
			final String targetCaseId = caseId;
			final int targetMaxRows = maxMatrixRows;
			final boolean targetRequireLive = requireLiveCluster;

			Thread.startVirtualThread(() -> {
				try {
					ModuleAcceptanceReport report = ModuleAcceptanceEngine.getInstance().runSuite(
							targetModuleId, targetMode, targetCaseId, targetMaxRows, targetRequireLive);
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/workbench/acceptance/run",
							"workbench.acceptance.run", 0L, false, Map.of("moduleId", targetModuleId, "mode", targetMode.name()),
							true, 0, "ok", 0, 0L);
					sendJson(ctx, GmResult.success("ok", report.toMap()));
				} catch (LogicException le) {
					log.warn("执行自动化验收业务异常: moduleId={}", targetModuleId, le);
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/workbench/acceptance/run",
							"workbench.acceptance.run", 0L, false, Map.of("moduleId", targetModuleId),
							false, le.getErrorCode(), le.getMessage(), 0, 0L);
					sendJson(ctx, GmResult.fail(le.getErrorCode(), "LOGIC_ERROR", "执行自动化验收异常: " + le.getMessage()));
				} catch (IllegalArgumentException iae) {
					log.warn("执行自动化验收参数异常: moduleId={}", targetModuleId, iae);
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/workbench/acceptance/run",
							"workbench.acceptance.run", 0L, false, Map.of("moduleId", targetModuleId),
							false, ErrorMsgEnum.request_parameter_error.getId(), iae.getMessage(), 0, 0L);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "执行自动化验收参数异常: " + iae.getMessage()));
				} catch (IllegalStateException ise) {
					log.warn("执行自动化验收集群拓扑未就绪: moduleId={}", targetModuleId, ise);
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/workbench/acceptance/run",
							"workbench.acceptance.run", 0L, false, Map.of("moduleId", targetModuleId),
							false, ErrorMsgEnum.condition_check_error.getId(), ise.getMessage(), 0, 0L);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.condition_check_error.getId(), "CLUSTER_TOPOLOGY_NOT_READY", "集群拓扑未就绪: " + ise.getMessage()));
				} catch (Throwable t) {
					log.warn("执行自动化验收失败: moduleId={}", targetModuleId, t);
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/workbench/acceptance/run",
							"workbench.acceptance.run", 0L, false, Map.of("moduleId", targetModuleId),
							false, ErrorMsgEnum.unknown.getId(), t.getMessage(), 0, 0L);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "执行自动化验收异常: " + t.getMessage()));
				}
			});
		});

		// 13. GET /gm-console/api/workbench/goods-catalog
		router.get("/gm-console/api/workbench/goods-catalog").handler(ctx -> {
			RunMode mode = GmSecurityPolicy.currentRunMode();
			if (mode.isProduction()) {
				Map<String, Object> prodData = new LinkedHashMap<>();
				prodData.put("simulationEnabled", false);
				prodData.put("categories", Collections.emptyList());
				prodData.put("items", Collections.emptyList());
				sendJson(ctx, GmResult.success("ok", prodData));
				return;
			}
			String typeIdParam = ctx.request().getParam("typeId");
			Integer parsedTypeId = null;
			if (typeIdParam != null && !typeIdParam.isBlank()) {
				try {
					parsedTypeId = Integer.parseInt(typeIdParam.trim());
				} catch (NumberFormatException e) {
					ctx.response().setStatusCode(400);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "typeId 必须为整数: " + typeIdParam));
					return;
				}
			}
			String keywordParam = ctx.request().getParam("keyword");
			if (keywordParam == null) {
				keywordParam = ctx.request().getParam("k");
			}
			String limitParam = ctx.request().getParam("limit");
			Integer parsedLimit = null;
			if (limitParam != null && !limitParam.isBlank()) {
				try {
					parsedLimit = Integer.parseInt(limitParam.trim());
				} catch (NumberFormatException e) {
					ctx.response().setStatusCode(400);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "limit 必须为整数: " + limitParam));
					return;
				}
			}
			final Integer finalTypeId = parsedTypeId;
			final String finalKeyword = keywordParam;
			final Integer finalLimit = parsedLimit;
			effectiveVertx.executeBlocking(() -> {
				GoodsCatalogService service = new GoodsCatalogService();
				return service.queryCatalog(finalTypeId, finalKeyword, finalLimit);
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, GmResult.success("ok", ar.result()));
				} else {
					log.error("查询物品目录异常 typeId={}", finalTypeId, ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "查询物品目录异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 14. POST /gm-console/api/proto/invoke-concurrent (并发连发幂等防刷压测)
		router.post("/gm-console/api/proto/invoke-concurrent").handler(ctx -> {
			RunMode mode = GmSecurityPolicy.currentRunMode();
			if (mode.isProduction()) {
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.production_gm_not_allow, "生产环境禁止执行并发协议连发测试"));
				return;
			}
			if (!requireRole(ctx, GmRole.ADMIN)) {
				return;
			}
			JsonObject json = parseJsonBody(ctx, true);
			if (json == null) {
				return;
			}
			ProtoInvokeRequest req = new ProtoInvokeRequest();
			req.setPlayerId(json.getLong("playerId", 0L));
			req.setMsgId(json.getInteger("msgId", 0));
			req.setMsgName(json.getString("msgName"));
			req.setSeq(json.getInteger("seq", 1));
			req.setMirrorToRealClient(json.getBoolean("mirrorToRealClient", false));
			req.setWithTrace(json.getBoolean("withTrace", true));
			req.setWithDiff(false);
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
			int concurrency = json.getInteger("concurrency", 5);
			GmAuthPrincipal principal = getPrincipal(ctx);

			effectiveVertx.executeBlocking(() -> {
				return ProtocolSimulationService.getInstance().invokeConcurrent(req, concurrency);
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					Map<String, Object> outcome = ar.result();
					boolean ok = Boolean.TRUE.equals(outcome.get("ok"));
					int diffCnt = outcome.get("diffCount") instanceof Number n ? n.intValue() : 0;
					long elapsed = outcome.get("elapsedMs") instanceof Number n ? n.longValue() : 0L;
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/proto/invoke-concurrent",
							String.valueOf(outcome.getOrDefault("msgName", req.getMsgName())), req.getPlayerId(), false,
							Map.of("concurrency", concurrency), ok, 0,
							"success=" + outcome.getOrDefault("successCount", 0) + ", fail=" + outcome.getOrDefault("failCount", 0),
							diffCnt, elapsed);
					ctx.response().putHeader("Content-Type", "application/json; charset=utf-8");
					try {
						ctx.response().end(MAPPER.writeValueAsString(outcome));
					} catch (Exception e) {
						log.error("序列化并发协议测试响应失败", e);
						ctx.response().setStatusCode(500).end("{\"ok\":false,\"message\":\"JSON serialization error\"}");
					}
				} else {
					log.error("执行并发协议模拟请求异常 msgId={}", req.getMsgId(), ar.cause());
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/proto/invoke-concurrent",
							req.getMsgName() != null ? req.getMsgName() : String.valueOf(req.getMsgId()),
							req.getPlayerId(), false, Map.of("concurrency", concurrency), false,
							ErrorMsgEnum.unknown.getId(), ar.cause().getMessage(), 0, 0L);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "执行并发协议模拟异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 15. GET /gm-console/api/ops/audit
		router.get("/gm-console/api/ops/audit").handler(ctx -> {
			if (!requireRole(ctx, GmRole.READONLY)) {
				return;
			}
			String operator = ctx.request().getParam("operator");
			String keyword = ctx.request().getParam("keyword");
			String pidStr = ctx.request().getParam("playerId");
			String onlyFailedStr = ctx.request().getParam("onlyFailed");
			String limitStr = ctx.request().getParam("limit");
			Long playerId = null;
			if (pidStr != null && !pidStr.isBlank()) {
				try {
					playerId = Long.parseLong(pidStr.trim());
				} catch (NumberFormatException e) {
					log.warn("审计日志查询 playerId 参数非整数: {}", pidStr, e);
				}
			}
			boolean onlyFailed = "true".equalsIgnoreCase(onlyFailedStr);
			int limit = 50;
			if (limitStr != null && !limitStr.isBlank()) {
				try {
					limit = Integer.parseInt(limitStr.trim());
				} catch (NumberFormatException e) {
					log.warn("审计日志查询 limit 参数非整数: {}", limitStr, e);
				}
			}
			Map<String, Object> data = GmAuditLogService.getInstance().query(operator, keyword, playerId, onlyFailed, limit);
			sendJson(ctx, GmResult.success("ok", data));
		});

		// 16. GET /gm-console/api/ops/health
		router.get("/gm-console/api/ops/health").handler(ctx -> {
			if (!requireRole(ctx, GmRole.READONLY)) {
				return;
			}
			effectiveVertx.executeBlocking(() -> {
				return new ServerDiagnosticsService().buildHealthSnapshot();
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, GmResult.success("ok", ar.result()));
				} else {
					log.error("采集服务端健康快照异常", ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "采集运行态健康快照异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 17. GET /gm-console/api/ops/players/search
		router.get("/gm-console/api/ops/players/search").handler(ctx -> {
			if (!requireRole(ctx, GmRole.READONLY)) {
				return;
			}
			String kw = ctx.request().getParam("keyword");
			if (kw == null) {
				kw = ctx.request().getParam("k");
			}
			String limitStr = ctx.request().getParam("limit");
			int limit = 30;
			if (limitStr != null && !limitStr.isBlank()) {
				try {
					limit = Integer.parseInt(limitStr.trim());
				} catch (NumberFormatException e) {
					log.warn("玩家检索 limit 参数非整数: {}", limitStr, e);
				}
			}
			final String finalKw = kw;
			final int finalLimit = limit;
			effectiveVertx.executeBlocking(() -> {
				return new OpsPlayerQueryService().searchPlayers(finalKw, finalLimit);
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, GmResult.success("ok", ar.result()));
				} else {
					log.error("检索运维玩家列表异常", ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "检索玩家异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 18. GET /gm-console/api/ops/players/:playerId/detail
		router.get("/gm-console/api/ops/players/:playerId/detail").handler(ctx -> {
			if (!requireRole(ctx, GmRole.READONLY)) {
				return;
			}
			long playerId;
			try {
				playerId = Long.parseLong(ctx.pathParam("playerId"));
			} catch (Exception e) {
				log.warn("玩家详单接口 playerId 非法: {}", ctx.pathParam("playerId"), e);
				ctx.response().setStatusCode(400);
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "playerId 必须为整数"));
				return;
			}
			effectiveVertx.executeBlocking(() -> {
				return new OpsPlayerQueryService().getPlayerDetail(playerId);
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, GmResult.success("ok", ar.result()));
				} else {
					log.error("获取玩家只读详情异常 playerId={}", playerId, ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "获取玩家详情异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 19. POST /gm-console/api/ops/players/:playerId/mail
		router.post("/gm-console/api/ops/players/:playerId/mail").handler(ctx -> {
			if (!requireRole(ctx, GmRole.OPERATOR)) {
				return;
			}
			long playerId;
			try {
				playerId = Long.parseLong(ctx.pathParam("playerId"));
			} catch (Exception e) {
				log.warn("运维补偿邮件接口 playerId 非法: {}", ctx.pathParam("playerId"), e);
				ctx.response().setStatusCode(400);
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "playerId 必须为整数"));
				return;
			}
			JsonObject json = parseJsonBody(ctx, true);
			if (json == null) {
				return;
			}
			String title = json.getString("title", "系统补偿邮件");
			String content = json.getString("content", "");
			int itemId = json.getInteger("itemId", 0);
			int count = json.getInteger("count", 0);
			GmAuthPrincipal principal = getPrincipal(ctx);

			effectiveVertx.executeBlocking(() -> {
				return new OpsPlayerQueryService().sendOpsMail(playerId, title, content, itemId, count);
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					Map<String, Object> mailRes = ar.result();
					boolean ok = Boolean.TRUE.equals(mailRes.get("ok"));
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/ops/players/" + playerId + "/mail",
							"ops.player.mail", playerId, false, Map.of("title", title, "itemId", itemId, "count", count),
							ok, ok ? 0 : ErrorMsgEnum.unknown.getId(), String.valueOf(mailRes.getOrDefault("message", "ok")), 0, 0L);
					if (ok) {
						sendJson(ctx, GmResult.success("邮件发送成功", mailRes));
					} else {
						sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, String.valueOf(mailRes.getOrDefault("message", "邮件发送失败"))));
					}
				} else {
					log.error("运维补偿邮件发送异常 playerId={}", playerId, ar.cause());
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/ops/players/" + playerId + "/mail",
							"ops.player.mail", playerId, false, Map.of("title", title, "itemId", itemId, "count", count),
							false, ErrorMsgEnum.unknown.getId(), ar.cause().getMessage(), 0, 0L);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "邮件发送异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 20. GET /gm-console/api/ops/config/summary
		router.get("/gm-console/api/ops/config/summary").handler(ctx -> {
			if (!requireRole(ctx, GmRole.READONLY)) {
				return;
			}
			effectiveVertx.executeBlocking(() -> {
				return new ConfigHealthService().getSummary();
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, GmResult.success("ok", ar.result()));
				} else {
					log.error("查询配表目录摘要异常", ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "查询配表目录摘要异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 21. POST /gm-console/api/ops/config/reload
		router.post("/gm-console/api/ops/config/reload").handler(ctx -> {
			if (!requireRole(ctx, GmRole.ADMIN)) {
				return;
			}
			JsonObject json = parseJsonBody(ctx, false);
			if (json == null) {
				return;
			}
			String rawTable = json.getString("table", "");
			if (rawTable.isBlank() && ctx.request().getParam("table") != null) {
				rawTable = ctx.request().getParam("table");
			}
			final String table = rawTable;
			GmAuthPrincipal principal = getPrincipal(ctx);
			effectiveVertx.executeBlocking(() -> {
				return new ConfigHealthService().reloadTables(table);
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					Map<String, Object> res = ar.result();
					boolean ok = Boolean.TRUE.equals(res.get("ok"));
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/ops/config/reload",
							"ops.config.reload", 0L, false, Map.of("table", table != null ? table : "ALL"),
							ok, ok ? 0 : ErrorMsgEnum.unknown.getId(),
							"success=" + res.getOrDefault("successCount", 0) + ", fail=" + res.getOrDefault("failCount", 0), 0, 0L);
					sendJson(ctx, GmResult.success("ok", res));
				} else {
					log.error("配表热重载异常 table={}", table, ar.cause());
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/ops/config/reload",
							"ops.config.reload", 0L, false, Map.of("table", table != null ? table : "ALL"),
							false, ErrorMsgEnum.unknown.getId(), ar.cause().getMessage(), 0, 0L);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "配表热重载异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 22. GET /gm-console/api/ops/config/validate
		router.get("/gm-console/api/ops/config/validate").handler(ctx -> {
			if (!requireRole(ctx, GmRole.READONLY)) {
				return;
			}
			effectiveVertx.executeBlocking(() -> {
				return new ConfigHealthService().validateIntegrity();
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, GmResult.success("ok", ar.result()));
				} else {
					log.error("配表静态完整性体检异常", ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "配表体检异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 23. GET /gm-console/api/ops/cluster
		router.get("/gm-console/api/ops/cluster").handler(ctx -> {
			if (!requireRole(ctx, GmRole.READONLY)) {
				return;
			}
			effectiveVertx.executeBlocking(() -> {
				return new CrossClusterDiagnosticsService().getClusterOverview();
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					sendJson(ctx, GmResult.success("ok", ar.result()));
				} else {
					log.error("获取跨服集群概览异常", ar.cause());
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "获取跨服概览异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 24. POST /gm-console/api/ops/cluster/action
		router.post("/gm-console/api/ops/cluster/action").handler(ctx -> {
			GmAuthPrincipal principal = getPrincipal(ctx);
			if (GmSecurityPolicy.currentRunMode().isProduction()) {
				GmAuditLogService.getInstance().record(principal, "/gm-console/api/ops/cluster/action",
						"ops.cluster.action", 0L, false, Collections.emptyMap(), false,
						ErrorMsgEnum.production_gm_not_allow.getId(), "生产环境禁止执行跨服调试动作", 0, 0L);
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.production_gm_not_allow, "生产环境禁止执行跨服调试动作"));
				return;
			}
			if (!requireRole(ctx, GmRole.ADMIN)) {
				return;
			}
			JsonObject json = parseJsonBody(ctx, true);
			if (json == null) {
				return;
			}
			String op = json.getString("op", "");
			JsonObject paramsObj = json.getJsonObject("params");
			Map<String, Object> params = paramsObj != null ? paramsObj.getMap() : Collections.emptyMap();
			effectiveVertx.executeBlocking(() -> {
				return new CrossClusterDiagnosticsService().executeDevAction(op, params);
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					Map<String, Object> res = ar.result();
					boolean ok = Boolean.TRUE.equals(res.get("ok"));
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/ops/cluster/action",
							"ops.cluster." + (op != null ? op : ""), 0L, false, params, ok,
							ok ? 0 : ErrorMsgEnum.unknown.getId(), String.valueOf(res.getOrDefault("message", ok ? "ok" : "failed")), 0, 0L);
					sendJson(ctx, GmResult.success("ok", res));
				} else {
					log.error("执行跨服联调动作异常 op={}", op, ar.cause());
					GmAuditLogService.getInstance().record(principal, "/gm-console/api/ops/cluster/action",
							"ops.cluster." + (op != null ? op : ""), 0L, false, params, false,
							ErrorMsgEnum.unknown.getId(), ar.cause().getMessage(), 0, 0L);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.unknown, "执行跨服联调异常: " + ar.cause().getMessage()));
				}
			});
		});

		// 5. 静态首页单页控制台
		Handler<RoutingContext> indexHandler = ctx -> {
			String html = loadIndexHtml();
			ctx.response()
					.putHeader("Content-Type", "text/html; charset=utf-8")
					.putHeader("Cache-Control", "no-store")
					.end(html);
		};
		router.get("/gm-console").handler(indexHandler);
		router.get("/gm-console/").handler(indexHandler);
		router.get("/gm-console/index.html").handler(indexHandler);

		return router;
	}

	private static final String CTX_PRINCIPAL_KEY = "gmPrincipal";

	private void handleAuth(RoutingContext ctx) {
		RunMode mode = GmSecurityPolicy.currentRunMode();
		String remoteIp = ctx.request().remoteAddress() != null ? ctx.request().remoteAddress().hostAddress() : "";
		if (GmSecurityPolicy.isRequireAuth(mode)) {
			String authHeader = ctx.request().getHeader("Authorization");
			String gmTokenHeader = ctx.request().getHeader("X-GM-Token");
			String token = GmSecurityPolicy.extractToken(authHeader, gmTokenHeader);
			GmAuthPrincipal principal = GmSecurityPolicy.authenticateToken(token, remoteIp);
			if (principal == null) {
				GmAuditLogService.getInstance().record(new GmAuthPrincipal("anonymous", GmRole.READONLY, remoteIp, false),
						ctx.request().path(), "auth.deny", 0L, false, Collections.emptyMap(),
						false, ErrorMsgEnum.gm_not.getId(), "GM HTTP 鉴权未通过", 0, 0L);
				ctx.response().setStatusCode(401);
				sendJson(ctx, GmResult.fail(ErrorMsgEnum.gm_not, "GM HTTP 鉴权未通过"));
				return;
			}
			ctx.put(CTX_PRINCIPAL_KEY, principal);
		} else {
			ctx.put(CTX_PRINCIPAL_KEY, GmAuthPrincipal.anonymousDev(remoteIp));
		}
		ctx.next();
	}

	private GmAuthPrincipal getPrincipal(RoutingContext ctx) {
		Object p = ctx.get(CTX_PRINCIPAL_KEY);
		if (p instanceof GmAuthPrincipal principal) {
			return principal;
		}
		String remoteIp = ctx.request().remoteAddress() != null ? ctx.request().remoteAddress().hostAddress() : "";
		return GmAuthPrincipal.anonymousDev(remoteIp);
	}

	private boolean requireRole(RoutingContext ctx, GmRole required) {
		GmAuthPrincipal principal = getPrincipal(ctx);
		if (principal.getRole().atLeast(required)) {
			return true;
		}
		String msg = "当前角色 [" + principal.getRole().name() + "] 权限不足，需要 [" + required.name() + "] 及以上权限";
		GmAuditLogService.getInstance().record(principal, ctx.request().path(), "rbac.deny", 0L, false,
				Collections.emptyMap(), false, ErrorMsgEnum.gm_not.getId(), msg, 0, 0L);
		ctx.response().setStatusCode(403);
		sendJson(ctx, GmResult.fail(ErrorMsgEnum.gm_not, msg));
		return false;
	}

	private JsonObject parseJsonBody(RoutingContext ctx, boolean required) {
		try {
			String raw = ctx.body() != null ? ctx.body().asString() : null;
			if (raw == null || raw.isBlank()) {
				if (required) {
					ctx.response().setStatusCode(400);
					sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "请求体不能为空"));
					return null;
				}
				return new JsonObject();
			}
			JsonObject json = new JsonObject(raw);
			return json;
		} catch (Exception e) {
			log.warn("解析 HTTP 请求体 JSON 失败: path={}", ctx.request().path(), e);
			ctx.response().setStatusCode(400);
			sendJson(ctx, GmResult.fail(ErrorMsgEnum.request_parameter_error, "请求体非合法JSON: " + e.getMessage()));
			return null;
		}
	}

	private String resolveActionKey(String action, String cmd) {
		if (action != null && !action.isBlank()) {
			return action.trim();
		}
		if (cmd != null && !cmd.isBlank()) {
			List<String> tokens;
			try {
				tokens = GmCommandTokenizer.tokenize(cmd);
			} catch (IllegalArgumentException e) {
				log.warn("解析 GM 文本命令分词失败: cmd={}", cmd, e);
				return null;
			}
			if (tokens.isEmpty()) {
				return null;
			}
			if (tokens.size() >= 2) {
				String composite = tokens.get(0) + "." + tokens.get(1);
				if (GmDispatcher.getInstance().hasCommand(composite)) {
					return composite;
				}
			}
			return tokens.get(0);
		}
		return null;
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

	/**
	 * 实时读取控制台首页 HTML（免内存缓存，优先磁盘直读，支持全运行模式免重启热生效）
	 *
	 * @return index.html 内容
	 */
	private String loadIndexHtml() {
		return loadStaticConsoleAsset("index.html",
				"<!DOCTYPE html><html><head><title>GM Console</title></head><body><h1>GM Console</h1><p>Ready</p></body></html>");
	}

	/**
	 * 实时读取协议工作台脚本 workbench.js（免内存缓存，优先磁盘直读，支持全运行模式免重启热生效）
	 *
	 * @return workbench.js 内容
	 */
	private String loadWorkbenchJs() {
		return loadStaticConsoleAsset("workbench.js", "/* gm workbench.js not found */");
	}

	/**
	 * 按“自定义静态目录 -> 源码资源目录 -> 运行目录外置 gm-console -> classpath（禁缓存）”顺序实时加载前端静态文件
	 *
	 * @param fileName 固定白名单静态文件名（如 index.html / workbench.js）
	 * @param fallback 全部来源均缺失时的兜底内容
	 * @return 文件文本内容
	 */
	private String loadStaticConsoleAsset(String fileName, String fallback) {
		List<Path> candidates = resolveStaticAssetCandidates(fileName);
		for (Path candidate : candidates) {
			if (candidate != null && Files.isRegularFile(candidate)) {
				try {
					return Files.readString(candidate, StandardCharsets.UTF_8);
				} catch (Exception e) {
					log.warn("从磁盘热加载 GM 控制台静态文件失败: path={}", candidate, e);
				}
			}
		}
		try {
			URL resource = getClass().getClassLoader().getResource("gm-console/" + fileName);
			if (resource != null) {
				URLConnection conn = resource.openConnection();
				conn.setUseCaches(false);
				try (InputStream in = conn.getInputStream()) {
					return new String(in.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		} catch (Exception e) {
			log.warn("读取 classpath gm-console/{} 失败", fileName, e);
		}
		return fallback;
	}

	/**
	 * 解析静态资源磁盘候选路径列表（按优先级排序）
	 *
	 * @param fileName 静态文件名
	 * @return 候选磁盘路径列表
	 */
	private List<Path> resolveStaticAssetCandidates(String fileName) {
		List<Path> candidates = new ArrayList<>(5);
		String configured = System.getProperty(PROP_STATIC_DIR);
		if (configured == null || configured.isBlank()) {
			configured = System.getenv(ENV_STATIC_DIR);
		}
		if (configured != null && !configured.isBlank()) {
			candidates.add(Path.of(configured.trim()).resolve(fileName).toAbsolutePath().normalize());
		}
		candidates.add(Path.of("game", "src", "main", "resources", "gm-console", fileName).toAbsolutePath().normalize());
		candidates.add(Path.of("src", "main", "resources", "gm-console", fileName).toAbsolutePath().normalize());
		candidates.add(Path.of("gm-console", fileName).toAbsolutePath().normalize());
		candidates.add(Path.of("..", "gm-console", fileName).toAbsolutePath().normalize());
		return candidates;
	}
}
