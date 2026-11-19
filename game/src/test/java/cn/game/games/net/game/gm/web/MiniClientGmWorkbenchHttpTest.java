package cn.game.games.net.game.gm.web;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
		String html = indexResp.body();
		Assertions.assertTrue(html.contains("workbench.js"), "首页必须引用 workbench.js");
		Assertions.assertTrue(html.contains("tabWorkbench"), "首页必须包含 tabWorkbench 容器");
		Assertions.assertTrue(html.contains("功能协议工作台"), "首页必须包含工作台导航项");

		// 2. GET /gm-console/workbench.js 静态脚本
		HttpRequest jsReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/workbench.js"))
				.GET()
				.build();
		HttpResponse<String> jsResp = httpClient.send(jsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, jsResp.statusCode());
		Assertions.assertTrue(jsResp.headers().firstValue("Content-Type").orElse("").contains("application/javascript"));
		String js = jsResp.body();
		Assertions.assertTrue(js.contains("window.WorkbenchApp"), "workbench.js 必须暴露 window.WorkbenchApp");
		Assertions.assertTrue(js.contains("renderProtobufForm"), "workbench.js 必须包含 Protobuf 动态表单渲染逻辑");
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
