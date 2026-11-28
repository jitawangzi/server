package cn.game.games.net.game.gm.web;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.command.PlayerGm;
import cn.game.games.net.game.gm.proto.VirtualGameClient;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.ObjUtil;
import io.vertx.core.Vertx;

/**
 * Web 控制台工作台端点、生产安全硬门禁与鉴权拦截 HTTP 集成测试 (TC-01, TC-05, TC-12, TC-LEGACY-GM-CONSOLE)
 *
 * @author shuyongqiang
 */
@Isolated
public class MiniClientGmWorkbenchHttpTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static Vertx vertx;
	private static HttpClient httpClient;

	private GmWebConsoleServer server;
	private int listenPort;
	private static final long HTTP_TEST_PLAYER_ID = 99887766L;

	@BeforeAll
	public static void beforeAll() {
		ClassManager.getInstance().init();
		GmDispatcher dispatcher = GmDispatcher.getInstance();
		if (!dispatcher.hasCommand("player.time.status")) {
			dispatcher.registerBean(new PlayerGm());
		}
		vertx = Vertx.vertx();
		httpClient = HttpClient.newHttpClient();
		if (ServerContext.getInstance().getEventBus() == null) {
			ServerContext.getInstance().setEventBus(ServerEventBus.getInstance());
		}
	}

	@AfterAll
	public static void afterAll() {
		if (vertx != null) {
			vertx.close();
		}
	}

	@BeforeEach
	public void setUp() throws Exception {
		System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
		ServerContext.getInstance().setRunMode();
		System.clearProperty(GmSecurityPolicy.PROP_REQUIRE_AUTH);
		System.clearProperty(GmSecurityPolicy.PROP_OPS_TOKEN);
		System.clearProperty(GmSecurityPolicy.PROP_RBAC_TOKENS);
		System.clearProperty(GmWebConsoleServer.PROP_STATIC_DIR);
		System.clearProperty(cn.game.games.net.game.gm.GmAuditLogService.PROP_AUDIT_FILE);
		cn.game.games.net.game.gm.GmAuditLogService.getInstance().clearMemoryForTest();

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(HTTP_TEST_PLAYER_ID);
		data.setUid(HTTP_TEST_PLAYER_ID);
		data.setName("HttpTestPlayer");
		data.setServerId("game_test_http");
		data.setLevel(15);

		Player player = new Player(data);
		player.setOnline(true);
		player.setIslogouting(false);
		player.setGameClient(new VirtualGameClient(HTTP_TEST_PLAYER_ID));

		registerPlayer(player);

		server = new GmWebConsoleServer(vertx);
		listenPort = server.start("127.0.0.1", 0).toCompletionStage().toCompletableFuture().get();
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (server != null) {
			server.stop().toCompletionStage().toCompletableFuture().get();
		}
		unregisterPlayer(HTTP_TEST_PLAYER_ID);
		System.clearProperty(GmSecurityPolicy.PROP_HTTP_PORT);
		System.clearProperty(GmSecurityPolicy.PROP_HTTP_HOST);
		System.clearProperty(GmSecurityPolicy.PROP_REQUIRE_AUTH);
		System.clearProperty(GmSecurityPolicy.PROP_OPS_TOKEN);
		System.clearProperty(GmSecurityPolicy.PROP_RBAC_TOKENS);
		System.clearProperty(GmWebConsoleServer.PROP_STATIC_DIR);
		System.clearProperty(cn.game.games.net.game.gm.GmAuditLogService.PROP_AUDIT_FILE);
		cn.game.games.net.game.gm.GmAuditLogService.getInstance().clearMemoryForTest();
		System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
		ServerContext.getInstance().setRunMode();
	}

	/**
	 * TC-01-PROD-GATE-AND-AUTH: 生产模式安全硬门禁与 X-GM-Token 鉴权拦截测试
	 */
	@Test
	public void testProductionGateAndTokenAuth() throws Exception {
		// 1. 验证生产模式硬阻断
		System.setProperty(ServerContext.SERVER_RUN_MODE, "PRODUCTION");
		ServerContext.getInstance().setRunMode();
		System.setProperty(GmSecurityPolicy.PROP_OPS_TOKEN, "ProductionOpsToken999");
		Assertions.assertTrue(GmSecurityPolicy.currentRunMode().isProduction());

		// (a) POST /gm-console/api/proto/invoke 生产短路
		HttpRequest invokeReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/proto/invoke"))
				.header("Content-Type", "application/json")
				.header("X-GM-Token", "ProductionOpsToken999")
				.POST(HttpRequest.BodyPublishers.ofString("{\"playerId\":99887766,\"msgId\":16777233}"))
				.build();
		HttpResponse<String> invokeResp = httpClient.send(invokeReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, invokeResp.statusCode());
		JsonNode invokeJson = MAPPER.readTree(invokeResp.body());
		Assertions.assertFalse(invokeJson.get("ok").asBoolean());
		Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.getId(), invokeJson.get("errorCode").asInt());

		// (b) POST /gm-console/api/workbench/test-login 生产短路
		HttpRequest loginReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/test-login"))
				.header("Content-Type", "application/json")
				.header("X-GM-Token", "ProductionOpsToken999")
				.POST(HttpRequest.BodyPublishers.ofString("{\"playerId\":99887766}"))
				.build();
		HttpResponse<String> loginResp = httpClient.send(loginReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, loginResp.statusCode());
		JsonNode loginJson = MAPPER.readTree(loginResp.body());
		Assertions.assertFalse(loginJson.get("ok").asBoolean());
		Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.getId(), loginJson.get("errorCode").asInt());

		// (c) GET /gm-console/api/workbench/modules 生产模式 simulationEnabled=false
		HttpRequest modReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/modules"))
				.header("X-GM-Token", "ProductionOpsToken999")
				.GET()
				.build();
		HttpResponse<String> modResp = httpClient.send(modReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, modResp.statusCode());
		JsonNode modJson = MAPPER.readTree(modResp.body());
		Assertions.assertTrue(modJson.get("ok").asBoolean());
		Assertions.assertFalse(modJson.get("data").get("simulationEnabled").asBoolean());

		// (d) GET /gm-console/api/workbench/players 生产模式返回空列表
		HttpRequest playersReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/players"))
				.header("X-GM-Token", "ProductionOpsToken999")
				.GET()
				.build();
		HttpResponse<String> playersResp = httpClient.send(playersReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, playersResp.statusCode());
		JsonNode playersJson = MAPPER.readTree(playersResp.body());
		Assertions.assertTrue(playersJson.get("ok").asBoolean());
		Assertions.assertFalse(playersJson.get("data").get("simulationEnabled").asBoolean());
		Assertions.assertEquals(0, playersJson.get("data").get("players").size());

		// (e) GET /gm-console/api/workbench/goods-catalog 生产模式短路返回空目录
		HttpRequest goodsProdReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/goods-catalog"))
				.header("X-GM-Token", "ProductionOpsToken999")
				.GET()
				.build();
		HttpResponse<String> goodsProdResp = httpClient.send(goodsProdReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, goodsProdResp.statusCode());
		JsonNode goodsProdJson = MAPPER.readTree(goodsProdResp.body());
		Assertions.assertTrue(goodsProdJson.get("ok").asBoolean());
		Assertions.assertFalse(goodsProdJson.get("data").get("simulationEnabled").asBoolean());
		Assertions.assertEquals(0, goodsProdJson.get("data").get("categories").size());

		// 2. 验证非生产下的 Token 鉴权拦截
		System.setProperty(ServerContext.SERVER_RUN_MODE, "TEST");
		ServerContext.getInstance().setRunMode();
		System.setProperty(GmSecurityPolicy.PROP_REQUIRE_AUTH, "true");
		System.setProperty(GmSecurityPolicy.PROP_OPS_TOKEN, "WorkbenchSecretToken7788");

		// (a) 无 Token 拦截 HTTP 401
		HttpRequest noTokenReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/modules"))
				.GET()
				.build();
		HttpResponse<String> noTokenResp = httpClient.send(noTokenReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(401, noTokenResp.statusCode());

		// (b) 错误 Token 拦截 HTTP 401
		HttpRequest wrongTokenReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/modules"))
				.header("X-GM-Token", "wrong-token-abc")
				.GET()
				.build();
		HttpResponse<String> wrongTokenResp = httpClient.send(wrongTokenReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(401, wrongTokenResp.statusCode());

		// (c) 正确 Token 放行 HTTP 200
		HttpRequest correctTokenReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/modules"))
				.header("X-GM-Token", "WorkbenchSecretToken7788")
				.GET()
				.build();
		HttpResponse<String> correctTokenResp = httpClient.send(correctTokenReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, correctTokenResp.statusCode());
		JsonNode correctJson = MAPPER.readTree(correctTokenResp.body());
		Assertions.assertTrue(correctJson.get("ok").asBoolean());
	}

	/**
	 * TC-05-WORKBENCH-PLAYERS-AND-TEST-LOGIN: 脱敏在线玩家列表与测试登录测试
	 */
	@Test
	public void testWorkbenchPlayersAndTestLogin() throws Exception {
		// 1. GET /gm-console/api/workbench/players
		HttpRequest playersReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/players"))
				.GET()
				.build();
		HttpResponse<String> playersResp = httpClient.send(playersReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, playersResp.statusCode());
		JsonNode playersJson = MAPPER.readTree(playersResp.body());
		Assertions.assertTrue(playersJson.get("ok").asBoolean());
		Assertions.assertTrue(playersJson.get("data").get("simulationEnabled").asBoolean());

		JsonNode playersArr = playersJson.get("data").get("players");
		Assertions.assertTrue(playersArr.isArray());
		boolean foundTestPlayer = false;
		for (JsonNode p : playersArr) {
			if (p.get("playerId").asLong() == HTTP_TEST_PLAYER_ID) {
				foundTestPlayer = true;
				Assertions.assertEquals("HttpTestPlayer", p.get("name").asText());
				Assertions.assertEquals(15, p.get("level").asInt());
				Assertions.assertTrue(p.get("isVirtualClient").asBoolean());
				Assertions.assertFalse(p.get("hasRealWsClient").asBoolean());
				break;
			}
		}
		Assertions.assertTrue(foundTestPlayer, "必须在脱敏玩家列表中检索到测试玩家");

		// 2. POST /gm-console/api/workbench/test-login (复用已在线测试玩家)
		HttpRequest loginReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/test-login"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"playerId\":" + HTTP_TEST_PLAYER_ID + "}"))
				.build();
		HttpResponse<String> loginResp = httpClient.send(loginReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, loginResp.statusCode());
		JsonNode loginJson = MAPPER.readTree(loginResp.body());
		Assertions.assertTrue(loginJson.get("ok").asBoolean());
		JsonNode loginData = loginJson.get("data");
		Assertions.assertTrue(loginData.get("reused").asBoolean(), "已在线玩家应当被标记为 reused=true");
		Assertions.assertEquals(HTTP_TEST_PLAYER_ID, loginData.get("playerId").asLong());
		Assertions.assertEquals("HttpTestPlayer", loginData.get("name").asText());
		Assertions.assertTrue(loginData.get("isVirtualClient").asBoolean());
	}

	/**
	 * TC-WORKBENCH-ACCOUNT-AND-PLAYER-TEST-LOGIN: 验证非在线玩家ID及账号名渠道测试登录与复用
	 */
	@Test
	public void testLoginForOfflinePlayerAndAccount() throws Exception {
		long newPlayerId = 55667788L;
		// 1. 尝试使用未在线的 playerId 进行测试登录 (自闭环创建/装配)
		HttpRequest offlineReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/test-login"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"playerId\":" + newPlayerId + "}"))
				.build();
		HttpResponse<String> offlineResp = httpClient.send(offlineReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, offlineResp.statusCode());
		JsonNode offlineJson = MAPPER.readTree(offlineResp.body());
		Assertions.assertTrue(offlineJson.get("ok").asBoolean(), "未在线玩家登录应当自闭环成功");
		JsonNode offlineData = offlineJson.get("data");
		Assertions.assertFalse(offlineData.get("reused").asBoolean(), "新创建测试玩家 reused=false");
		Assertions.assertEquals(newPlayerId, offlineData.get("playerId").asLong());
		Assertions.assertTrue(offlineData.get("isVirtualClient").asBoolean());
		Assertions.assertTrue(cn.game.games.net.game.helper.PlayerSaveCoordinator.isSandboxPlayerId(newPlayerId),
				"test-login 指定的纯内存离线 playerId 必须标记为免落盘沙盒玩家，避免5分钟后被 WRITE_PROTECTED 锁死");

		// 2. 再次使用相同的 playerId 登录，应当复用已在线角色
		HttpResponse<String> reusePidResp = httpClient.send(offlineReq, HttpResponse.BodyHandlers.ofString());
		JsonNode reusePidJson = MAPPER.readTree(reusePidResp.body());
		Assertions.assertTrue(reusePidJson.get("ok").asBoolean());
		Assertions.assertTrue(reusePidJson.get("data").get("reused").asBoolean(), "再次登录应当 reused=true");

		// 3. 尝试使用纯账号名 (accountId) 进行登录
		String accountName = "qa_tester_01";
		HttpRequest accountReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/test-login"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"accountId\":\"" + accountName + "\"}"))
				.build();
		HttpResponse<String> accountResp = httpClient.send(accountReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, accountResp.statusCode());
		JsonNode accountJson = MAPPER.readTree(accountResp.body());
		Assertions.assertTrue(accountJson.get("ok").asBoolean(), "账号名登录应当成功");
		JsonNode accountData = accountJson.get("data");
		Assertions.assertEquals(accountName, accountData.get("accountId").asText());
		Assertions.assertEquals(accountName, accountData.get("name").asText());
		long generatedAccountPid = accountData.get("playerId").asLong();
		Assertions.assertTrue(generatedAccountPid >= cn.game.games.net.game.helper.PlayerSaveCoordinator.SANDBOX_PLAYER_ID_MIN,
				"test-login 自动生成的纯内存玩家ID应分配在沙盒免落盘号段，实际=" + generatedAccountPid);
		Assertions.assertTrue(cn.game.games.net.game.helper.PlayerSaveCoordinator.isSandboxPlayerId(generatedAccountPid));

		// 4. 再次使用相同的 accountId 登录，应当复用已在线角色
		HttpResponse<String> reuseAccResp = httpClient.send(accountReq, HttpResponse.BodyHandlers.ofString());
		JsonNode reuseAccJson = MAPPER.readTree(reuseAccResp.body());
		Assertions.assertTrue(reuseAccJson.get("ok").asBoolean());
		Assertions.assertTrue(reuseAccJson.get("data").get("reused").asBoolean(), "相同账号名再次登录应当 reused=true");

		// 清理新创建的测试玩家与沙盒标记
		unregisterPlayer(newPlayerId);
		cn.game.games.net.game.helper.PlayerSaveCoordinator.unmarkMemoryOnlySandboxPlayer(newPlayerId);
		unregisterPlayer(generatedAccountPid);
	}

	/**
	 * TC-12-WEB-CONSOLE-STATIC-AND-PANELS: 单页工作台静态首页与 workbench.js 资源加载测试
	 */
	@Test
	public void testWebConsoleStaticAndPanels() throws Exception {
		// 1. GET /gm-console 首页内容
		HttpRequest indexReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console"))
				.GET()
				.build();
		HttpResponse<String> indexResp = httpClient.send(indexReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, indexResp.statusCode());
		Assertions.assertTrue(indexResp.headers().firstValue("Content-Type").orElse("").contains("text/html"));
		Assertions.assertEquals("no-store", indexResp.headers().firstValue("Cache-Control").orElse(""));
		String html = indexResp.body();
		Assertions.assertTrue(html.contains("workbench.js"), "首页必须引用 workbench.js");
		Assertions.assertTrue(html.contains("tabWorkbench"), "首页必须包含 tabWorkbench 容器");
		Assertions.assertTrue(html.contains("功能协议工作台"), "首页必须包含工作台导航项");
		Assertions.assertTrue(html.contains("data-theme=\"light-day\""), "首页必须定义晴日浅灰浅色主题");
		Assertions.assertTrue(html.contains("data-theme=\"light-warm\""), "首页必须定义暖纸护眼浅色主题");
		Assertions.assertTrue(html.contains("html[data-theme=\"light-day\"]"), "首页必须显式声明浅色主题根节点与body背景");
		Assertions.assertTrue(html.contains("html[data-theme=\"light-warm\"]"), "首页必须显式声明暖纸主题根节点与body背景");
		Assertions.assertTrue(html.contains("THEME_BG_COLORS"), "首页切换主题时必须同步更新html与body根背景色");
		Assertions.assertTrue(html.contains("id=\"selTheme\""), "首页必须提供主题切换控件");
		Assertions.assertTrue(html.contains("gm_console_theme"), "首页必须支持主题选择持久化");
		Assertions.assertTrue(html.contains("panelPortalView"), "首页必须提供面向玩法的业务测试台门户");
		Assertions.assertTrue(html.contains("panelDevDrawer"), "首页必须提供面向开发的底部可折叠诊断抽屉");
		Assertions.assertTrue(html.contains("quickGmBackdrop"), "首页必须提供跨页面随手呼出的快捷GM抽屉");
		Assertions.assertTrue(html.contains("min(860px, 95vw)"), "快捷GM抽屉必须加宽至宽屏双栏布局");
		Assertions.assertTrue(html.contains("qgmGoodsCategory"), "快捷GM抽屉必须包含第1级物品大类联动下拉");
		Assertions.assertTrue(html.contains("qgmGoodsItem"), "快捷GM抽屉必须包含第2级物品配置联动下拉");
		Assertions.assertTrue(html.contains("getGoodsCatalog:"), "window.GmConsole SDK v2 必须暴露 getGoodsCatalog");
		Assertions.assertFalse(html.contains("var(--header-bg)"), "首页不得引用未定义的 CSS 变量 --header-bg");
		Assertions.assertTrue(html.contains("setPlayerLevel:"), "window.GmConsole SDK v2 必须暴露 setPlayerLevel");
		Assertions.assertTrue(html.contains("setResource:"), "window.GmConsole SDK v2 必须暴露 setResource");
		Assertions.assertTrue(html.contains("invokeProto:"), "window.GmConsole SDK v2 必须暴露 invokeProto");
		Assertions.assertTrue(html.contains("inspectPlayer:"), "window.GmConsole SDK v2 必须暴露 inspectPlayer");
		Assertions.assertTrue(html.contains("resetModules:"), "window.GmConsole SDK v2 必须暴露 resetModules");

		// 2. GET /gm-console/workbench.js 静态脚本
		HttpRequest jsReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/workbench.js"))
				.GET()
				.build();
		HttpResponse<String> jsResp = httpClient.send(jsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, jsResp.statusCode());
		Assertions.assertTrue(jsResp.headers().firstValue("Content-Type").orElse("").contains("application/javascript"));
		Assertions.assertEquals("no-store", jsResp.headers().firstValue("Cache-Control").orElse(""));
		String js = jsResp.body();
		Assertions.assertTrue(js.contains("window.WorkbenchApp"), "workbench.js 必须暴露 window.WorkbenchApp");
		Assertions.assertTrue(js.contains("renderProtobufForm"), "workbench.js 必须包含 Protobuf 动态表单渲染逻辑");
		Assertions.assertTrue(js.contains("matchedPanels"), "workbench.js 必须在开发协议工作台侧边栏关联业务测试页");
		Assertions.assertTrue(js.contains("var(--surface-subtle)"), "workbench.js 必须使用主题语义变量以适配浅色主题");
		Assertions.assertFalse(js.contains("background:#0f172a"), "workbench.js 不得残留硬编码深色背景");
		Assertions.assertFalse(js.contains("background:#0b1120"), "workbench.js 不得残留硬编码极深蓝背景");

		// 3. 验证首个标杆业务测试页 shop.html 元数据扫描、多对多模块匹配与免重启热读取
		HttpRequest panelsReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/panels"))
				.GET()
				.build();
		HttpResponse<String> panelsResp = httpClient.send(panelsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, panelsResp.statusCode());
		JsonNode panelsData = MAPPER.readTree(panelsResp.body()).path("data");
		Assertions.assertTrue(panelsData.isArray());
		JsonNode shopPanel = null;
		for (JsonNode p : panelsData) {
			if ("shop.html".equals(p.path("file").asText())) {
				shopPanel = p;
				break;
			}
		}
		Assertions.assertNotNull(shopPanel, "必须扫描到商店业务测试页 shop.html");
		Assertions.assertEquals("商店与礼包测试台", shopPanel.path("title").asText());
		Assertions.assertEquals("核心玩法", shopPanel.path("category").asText());
		Assertions.assertTrue(shopPanel.path("hasMetadata").asBoolean());

		HttpRequest shopHtmlReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/panels/shop.html"))
				.GET()
				.build();
		HttpResponse<String> shopHtmlResp = httpClient.send(shopHtmlReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, shopHtmlResp.statusCode());
		Assertions.assertTrue(shopHtmlResp.body().contains("ShopQaPanel"), "shop.html 必须包含 ShopQaPanel 交互实现");
		Assertions.assertTrue(shopHtmlResp.body().contains("ShopItemBuyRequest_15000003"), "shop.html 必须对接商店真实协议");
		Assertions.assertTrue(shopHtmlResp.body().contains("totalPages"), "shop.html 必须按 totalPages 分页加载完整配表");

		HttpRequest toolboxReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/panels/common_toolbox.html"))
				.GET()
				.build();
		HttpResponse<String> toolboxResp = httpClient.send(toolboxReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, toolboxResp.statusCode());
		Assertions.assertTrue(toolboxResp.body().contains("CommonToolbox"), "common_toolbox.html 必须包含 CommonToolbox 实现");
		Assertions.assertTrue(toolboxResp.body().contains("nearLevelUp"), "common_toolbox.html 必须支持差1经验即升级操作");
		Assertions.assertTrue(toolboxResp.body().contains("tbGoodsCategory"), "common_toolbox.html 必须支持全品类物品2级联动下拉");
		Assertions.assertTrue(toolboxResp.body().contains("getGoodsCatalog"), "common_toolbox.html 必须调用 window.GmConsole.getGoodsCatalog");

		// 3.5 验证全物品大类 2 级联动目录 API (GET /gm-console/api/workbench/goods-catalog)
		HttpRequest catReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/goods-catalog"))
				.GET()
				.build();
		HttpResponse<String> catResp = httpClient.send(catReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, catResp.statusCode());
		JsonNode catJson = MAPPER.readTree(catResp.body());
		Assertions.assertTrue(catJson.path("ok").asBoolean());
		JsonNode categories = catJson.path("data").path("categories");
		Assertions.assertTrue(categories.isArray() && categories.size() >= 10, "物品大类目录必须覆盖全部主流 GoodsTypeEnum");
		JsonNode resourceCat = null;
		for (JsonNode c : categories) {
			if (c.path("typeId").asInt() == 1) {
				resourceCat = c;
				break;
			}
		}
		Assertions.assertNotNull(resourceCat, "必须包含 Resource(1) 玩家资源大类");
		Assertions.assertTrue(resourceCat.path("count").asInt() > 10, "Asset 枚举资源数应大于 10");
		Assertions.assertTrue(resourceCat.path("supportsSet").asBoolean());

		// 按 typeId=1 与关键字过滤检索元宝与经验，验证 canSet 能力标记
		HttpRequest resItemsReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/workbench/goods-catalog?typeId=1"))
				.GET()
				.build();
		HttpResponse<String> resItemsResp = httpClient.send(resItemsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, resItemsResp.statusCode());
		JsonNode resItems = MAPPER.readTree(resItemsResp.body()).path("data").path("items");
		Assertions.assertTrue(resItems.isArray() && resItems.size() > 10);
		boolean checkedDiamond = false;
		boolean checkedPlayerExp = false;
		for (JsonNode it : resItems) {
			int id = it.path("id").asInt();
			if (id == 100001) {
				checkedDiamond = true;
				Assertions.assertEquals("元宝", it.path("name").asText());
				Assertions.assertTrue(it.path("canSet").asBoolean());
			} else if (id == 100201) {
				checkedPlayerExp = true;
				Assertions.assertFalse(it.path("canSet").asBoolean(), "经验类资产必须标记 canSet=false");
			}
		}
		Assertions.assertTrue(checkedDiamond && checkedPlayerExp, "必须返回 100001 元宝与 100201 玩家经验");

		// 4. 验证 GmConsolePanelLoader.matchesModule 支持 1 协议模块对应多个独立玩法测试页（如 Activity 下多活动）
		Map<String, Object> actPanelA = Map.of("name", "activity_seven_days", "modules", java.util.List.of("activity", "0x11"));
		Map<String, Object> actPanelB = Map.of("name", "activity_dragon_boat", "modules", java.util.List.of("0x11"));
		Map<String, Object> shopPanelMeta = Map.of("name", "shop", "modules", java.util.List.of("shop", "0x15"));
		Assertions.assertTrue(GmConsolePanelLoader.matchesModule(actPanelA, 0x11, "0x11", "activity"));
		Assertions.assertTrue(GmConsolePanelLoader.matchesModule(actPanelB, 0x11, "0x11", "activity"));
		Assertions.assertFalse(GmConsolePanelLoader.matchesModule(shopPanelMeta, 0x11, "0x11", "activity"));
		Assertions.assertTrue(GmConsolePanelLoader.matchesModule(shopPanelMeta, 0x15, "0x15", "shop"));
	}

	/**
	 * TC-LEGACY-GM-CONSOLE: 既有控制台服务基础端点表征回归测试 (CHARACTERIZATION)
	 */
	@Test
	public void verifyLegacyGmConsoleEndpoints() throws Exception {
		// 1. GET /gm-console/api/bootstrap
		HttpRequest bootstrapReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/bootstrap"))
				.GET()
				.build();
		HttpResponse<String> bootstrapResp = httpClient.send(bootstrapReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, bootstrapResp.statusCode());
		JsonNode bsJson = MAPPER.readTree(bootstrapResp.body());
		Assertions.assertTrue(bsJson.get("ok").asBoolean());
		JsonNode bsData = bsJson.get("data");
		Assertions.assertNotNull(bsData.get("virtualClock"));
		Assertions.assertNotNull(bsData.get("schema"));
		Assertions.assertNotNull(bsData.get("panels"));

		// 2. GET /gm-console/api/panels
		HttpRequest panelsReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/panels"))
				.GET()
				.build();
		HttpResponse<String> panelsResp = httpClient.send(panelsReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, panelsResp.statusCode());
		JsonNode panelsJson = MAPPER.readTree(panelsResp.body());
		Assertions.assertTrue(panelsJson.get("ok").asBoolean());
		Assertions.assertTrue(panelsJson.get("data").isArray());

		// 3. POST /gm-console/api/execute 既有命令分发
		HttpRequest execReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/execute"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"cmd\":\"player.time.status\"}"))
				.build();
		HttpResponse<String> execResp = httpClient.send(execReq, HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, execResp.statusCode());
		JsonNode execJson = MAPPER.readTree(execResp.body());
		Assertions.assertTrue(execJson.get("ok").asBoolean());
	}

	/**
	 * 验证 index.html 与 workbench.js 在 DEV 与 PRODUCTION 模式下均免缓存实时热重载（无需重启服务端）
	 */
	@Test
	public void testStaticAssetsHotReloadInDevAndProduction(@TempDir Path tempStaticDir) throws Exception {
		Path indexFile = tempStaticDir.resolve("index.html");
		Path jsFile = tempStaticDir.resolve("workbench.js");
		Files.writeString(indexFile, "<html><body>GM-HOT-V1</body></html>", StandardCharsets.UTF_8);
		Files.writeString(jsFile, "window.__HOT_VER__='V1';", StandardCharsets.UTF_8);
		System.setProperty(GmWebConsoleServer.PROP_STATIC_DIR, tempStaticDir.toString());

		HttpRequest indexReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console"))
				.GET()
				.build();
		HttpRequest jsReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/workbench.js"))
				.GET()
				.build();

		// 1. DEV 模式读取 V1
		HttpResponse<String> idxV1 = httpClient.send(indexReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		HttpResponse<String> jsV1 = httpClient.send(jsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertTrue(idxV1.body().contains("GM-HOT-V1"));
		Assertions.assertTrue(jsV1.body().contains("V1"));
		Assertions.assertEquals("no-store", idxV1.headers().firstValue("Cache-Control").orElse(""));
		Assertions.assertEquals("no-store", jsV1.headers().firstValue("Cache-Control").orElse(""));

		// 2. 不重启服务修改为 V2，DEV 模式立刻生效
		Files.writeString(indexFile, "<html><body>GM-HOT-V2</body></html>", StandardCharsets.UTF_8);
		Files.writeString(jsFile, "window.__HOT_VER__='V2';", StandardCharsets.UTF_8);
		HttpResponse<String> idxV2 = httpClient.send(indexReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		HttpResponse<String> jsV2 = httpClient.send(jsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertTrue(idxV2.body().contains("GM-HOT-V2"), "DEV 模式下 index.html 必须免重启热生效");
		Assertions.assertTrue(jsV2.body().contains("V2"), "DEV 模式下 workbench.js 必须免重启热生效");

		// 3. 切换 PRODUCTION 模式修改为 V3，同样免缓存热生效
		System.setProperty(ServerContext.SERVER_RUN_MODE, "PRODUCTION");
		ServerContext.getInstance().setRunMode();
		Files.writeString(indexFile, "<html><body>GM-HOT-PROD-V3</body></html>", StandardCharsets.UTF_8);
		Files.writeString(jsFile, "window.__HOT_VER__='PROD-V3';", StandardCharsets.UTF_8);
		HttpResponse<String> idxV3 = httpClient.send(indexReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		HttpResponse<String> jsV3 = httpClient.send(jsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertTrue(idxV3.body().contains("GM-HOT-PROD-V3"), "PRODUCTION 模式下 index.html 必须免重启热生效");
		Assertions.assertTrue(jsV3.body().contains("PROD-V3"), "PRODUCTION 模式下 workbench.js 必须免重启热生效");
		Assertions.assertEquals("no-store", idxV3.headers().firstValue("Cache-Control").orElse(""));
		Assertions.assertEquals("no-store", jsV3.headers().firstValue("Cache-Control").orElse(""));
	}

	/**
	 * 验证全平台 P0/P1/P2 优化新增运维路由、RBAC 分级鉴权、审计落盘、并发协议入口、新热更面板与生产安全门禁
	 */
	@Test
	public void testPlatformOptimizationRoutesRbacAndPanels(@TempDir Path tempAuditDir) throws Exception {
		Path auditFile = tempAuditDir.resolve("http-ops-audit.jsonl");
		System.setProperty(cn.game.games.net.game.gm.GmAuditLogService.PROP_AUDIT_FILE, auditFile.toString());

		// 1. 静态前端契约与三大新热重载面板校验
		HttpResponse<String> indexResp = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console")).GET().build(),
				HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, indexResp.statusCode());
		String indexHtml = indexResp.body();
		Assertions.assertTrue(indexHtml.contains("tabOps"));
		Assertions.assertTrue(indexHtml.contains("lblPrincipalBadge"));
		Assertions.assertTrue(indexHtml.contains("exportDevScenario"));
		Assertions.assertTrue(indexHtml.contains("runImportedScenario"));
		Assertions.assertTrue(indexHtml.contains("invokeProtoConcurrent"));
		Assertions.assertTrue(indexHtml.contains("diffSnapshots"));
		Assertions.assertTrue(indexHtml.contains("getOpsHealth"));
		Assertions.assertTrue(indexHtml.contains("validateConfigs"));
		Assertions.assertTrue(indexHtml.contains("dryRunSupported"));

		HttpResponse<String> wbResp = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/workbench.js")).GET().build(),
				HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, wbResp.statusCode());
		Assertions.assertTrue(wbResp.body().contains("invokeConcurrent"));
		Assertions.assertTrue(wbResp.body().contains("/gm-console/api/proto/invoke-concurrent"));

		for (String panelFile : new String[] { "player_inspector.html", "activity_scheduler.html", "config_center.html" }) {
			HttpResponse<String> pResp = httpClient.send(
					HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/panels/" + panelFile)).GET().build(),
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			Assertions.assertEquals(200, pResp.statusCode(), "面板必须可热加载访问: " + panelFile);
			Assertions.assertTrue(pResp.body().contains("@gm-panel"));
		}

		// 2. DEV 模式校验 /api/bootstrap principal + /api/ops/* 只读与跨服接口
		JsonNode bootstrap = MAPPER.readTree(httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/bootstrap")).GET().build(),
				HttpResponse.BodyHandlers.ofString()).body());
		Assertions.assertNotNull(bootstrap.get("data").get("principal"));

		for (String getPath : new String[] {
				"/gm-console/api/ops/health",
				"/gm-console/api/ops/players/search?keyword=HttpTestPlayer",
				"/gm-console/api/ops/players/" + HTTP_TEST_PLAYER_ID + "/detail",
				"/gm-console/api/ops/audit",
				"/gm-console/api/ops/config/summary",
				"/gm-console/api/ops/config/validate",
				"/gm-console/api/ops/cluster" }) {
			HttpResponse<String> opsResp = httpClient.send(
					HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + getPath)).GET().build(),
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			Assertions.assertEquals(200, opsResp.statusCode(), "GET " + getPath);
			JsonNode json = MAPPER.readTree(opsResp.body());
			Assertions.assertTrue(json.get("ok").asBoolean(), "GET " + getPath + " 应返回 ok=true: " + opsResp.body());
		}

		// 3. 开启多角色 RBAC Token 校验 401/403 分权边界与审计落盘
		System.setProperty(GmSecurityPolicy.PROP_REQUIRE_AUTH, "true");
		System.setProperty(GmSecurityPolicy.PROP_RBAC_TOKENS, "qa_ro:READONLY:ro_tok,ops_op:OPERATOR:op_tok,sec_adm:ADMIN:adm_tok");

		HttpResponse<String> unauth = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/ops/health")).GET().build(),
				HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(401, unauth.statusCode());

		HttpResponse<String> roHealth = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/ops/health"))
						.header("X-GM-Token", "ro_tok").GET().build(),
				HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, roHealth.statusCode());

		HttpResponse<String> roDeniedMail = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/ops/players/" + HTTP_TEST_PLAYER_ID + "/mail"))
						.header("X-GM-Token", "ro_tok")
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"t\",\"content\":\"c\"}"))
						.build(),
				HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(403, roDeniedMail.statusCode(), "READONLY 角色严禁发送运维补偿邮件");

		HttpResponse<String> opDeniedReload = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/ops/config/reload"))
						.header("X-GM-Token", "op_tok")
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("{\"table\":\"ShopItem\"}"))
						.build(),
				HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(403, opDeniedReload.statusCode(), "OPERATOR 角色严禁触发配表热更新");

		// 3.5 引号命令绕过防护（\"player.super\"）、未知指令 fail-closed 与非 execute 写接口 RBAC 校验
		HttpResponse<String> quotedBypass = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/execute"))
						.header("X-GM-Token", "ro_tok")
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("{\"cmd\":\"\\\"player.super\\\"\",\"playerId\":" + HTTP_TEST_PLAYER_ID + "}"))
						.build(),
				HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(403, quotedBypass.statusCode(), "带引号命令不可绕过文本命令 RBAC 鉴权");

		HttpResponse<String> unknownCmdDeny = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/execute"))
						.header("X-GM-Token", "op_tok")
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("{\"cmd\":\"nonexistent.cmd.bypass\"}"))
						.build(),
				HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(403, unknownCmdDeny.statusCode(), "非 ADMIN 执行未注册命令应采用 fail-closed 拦截");

		HttpResponse<String> dryRunRejectHttp = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/execute"))
						.header("X-GM-Token", "adm_tok")
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("{\"action\":\"player.time.status\",\"dryRun\":true}"))
						.build(),
				HttpResponse.BodyHandlers.ofString());
		JsonNode dryRejectJson = MAPPER.readTree(dryRunRejectHttp.body());
		Assertions.assertFalse(dryRejectJson.get("ok").asBoolean(), "不支持 dryRun 的指令传入 dryRun=true 必须拒绝");
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), dryRejectJson.get("errorCode").asInt());

		for (String writePath : new String[] {
				"/gm-console/api/proto/invoke",
				"/gm-console/api/proto/invoke-concurrent",
				"/gm-console/api/workbench/test-login",
				"/gm-console/api/workbench/acceptance/run" }) {
			HttpResponse<String> roDeniedWrite = httpClient.send(
					HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + writePath))
							.header("X-GM-Token", "ro_tok")
							.header("Content-Type", "application/json")
							.POST(HttpRequest.BodyPublishers.ofString("{}"))
							.build(),
					HttpResponse.BodyHandlers.ofString());
			Assertions.assertEquals(403, roDeniedWrite.statusCode(), "READONLY 严禁调用写/调试接口: " + writePath);
		}

		// 3.6 运维 POST 接口传入畸形 JSON 返回 400 而非 500
		for (String malformedTarget : new String[] {
				"/gm-console/api/ops/players/" + HTTP_TEST_PLAYER_ID + "/mail",
				"/gm-console/api/ops/config/reload",
				"/gm-console/api/ops/cluster/action" }) {
			HttpResponse<String> badJsonResp = httpClient.send(
					HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + malformedTarget))
							.header("X-GM-Token", "adm_tok")
							.header("Content-Type", "application/json")
							.POST(HttpRequest.BodyPublishers.ofString("{bad_json"))
							.build(),
					HttpResponse.BodyHandlers.ofString());
			Assertions.assertEquals(400, badJsonResp.statusCode(), "畸形 JSON 必须返回 400: " + malformedTarget);
		}

		// 3.7 验证 401 鉴权失败已记入审计日志
		HttpResponse<String> auditAfterDeny = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/ops/audit?keyword=auth.deny&onlyFailed=true"))
						.header("X-GM-Token", "ro_tok").GET().build(),
				HttpResponse.BodyHandlers.ofString());
		JsonNode auditDenyJson = MAPPER.readTree(auditAfterDeny.body());
		Assertions.assertTrue(auditDenyJson.get("data").get("matchedTotal").asInt() >= 1, "401 鉴权失败必须写入审计日志");

		// 4. PRODUCTION 模式下运维只读接口保持可用，并发协议与跨服调试接口严格阻断
		System.setProperty(ServerContext.SERVER_RUN_MODE, "PRODUCTION");
		ServerContext.getInstance().setRunMode();

		HttpResponse<String> prodHealth = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/ops/health"))
						.header("X-GM-Token", "ro_tok").GET().build(),
				HttpResponse.BodyHandlers.ofString());
		Assertions.assertEquals(200, prodHealth.statusCode(), "PRODUCTION 下合法 RBAC 只读账号仍可查看健康诊断");

		HttpResponse<String> prodConcurrent = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/proto/invoke-concurrent"))
						.header("X-GM-Token", "adm_tok")
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("{\"playerId\":" + HTTP_TEST_PLAYER_ID + ",\"msgName\":\"PlayerNameRequest_01000011\",\"concurrency\":3}"))
						.build(),
				HttpResponse.BodyHandlers.ofString());
		JsonNode prodConcJson = MAPPER.readTree(prodConcurrent.body());
		Assertions.assertFalse(prodConcJson.get("ok").asBoolean(), "PRODUCTION 下禁止并发协议压测");
		Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.getId(), prodConcJson.get("errorCode").asInt());

		HttpResponse<String> prodClusterAction = httpClient.send(
				HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/ops/cluster/action"))
						.header("X-GM-Token", "adm_tok")
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("{\"op\":\"rank.top\",\"params\":{}}"))
						.build(),
				HttpResponse.BodyHandlers.ofString());
		JsonNode prodClusterJson = MAPPER.readTree(prodClusterAction.body());
		Assertions.assertFalse(prodClusterJson.get("ok").asBoolean(), "PRODUCTION 下禁止跨服联调写动作");
		Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.getId(), prodClusterJson.get("errorCode").asInt());

		Assertions.assertTrue(Files.exists(auditFile), "操作与越权拒绝审计日志必须已持久化到 JSONL 文件");
	}

	@SuppressWarnings("unchecked")
	private static void registerPlayer(Player player) throws Exception {
		Field field = PlayerManager.class.getDeclaredField("id_players");
		field.setAccessible(true);
		Map<Long, Player> map = (Map<Long, Player>) field.get(PlayerManager.getInstance());
		map.put(player.getPlayerId(), player);
	}

	@SuppressWarnings("unchecked")
	private static void unregisterPlayer(long playerId) throws Exception {
		Field field = PlayerManager.class.getDeclaredField("id_players");
		field.setAccessible(true);
		Map<Long, Player> map = (Map<Long, Player>) field.get(PlayerManager.getInstance());
		map.remove(playerId);
	}
}
