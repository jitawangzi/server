package cn.game.games.net.game.gm;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.game.gm.web.GmConsolePanelLoader;
import cn.game.games.net.game.gm.web.GmWebConsoleServer;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.gm.command.PlayerGm;
import cn.game.protocol.manual.ErrorMsgEnum;
import io.vertx.core.Vertx;

/**
 * Web控制台与多重安全门禁测试 (TC-B08, TC-B09, TC-B10)
 *
 * @author shuyongqiang
 */
public class GmWebConsoleServerTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static Vertx vertx;
	private static HttpClient httpClient;

	private Path testPanelsDir;
	private GmWebConsoleServer server;
	private int listenPort;

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
		testPanelsDir = Path.of("target", "test-panels-" + System.currentTimeMillis());
		Files.createDirectories(testPanelsDir);
		System.setProperty(GmConsolePanelLoader.PROP_PANELS_DIR, testPanelsDir.toAbsolutePath().toString());

		// 确保处于非生产模式
		System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
		ServerContext.getInstance().setRunMode();
		System.clearProperty(GmSecurityPolicy.PROP_REQUIRE_AUTH);
		System.clearProperty(GmSecurityPolicy.PROP_OPS_TOKEN);

		server = new GmWebConsoleServer(vertx);
		// 绑定端口 0 获得随机空闲端口
		listenPort = server.start("127.0.0.1", 0).toCompletionStage().toCompletableFuture().get();
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (server != null) {
			server.stop().toCompletionStage().toCompletableFuture().get();
		}
		System.clearProperty(GmConsolePanelLoader.PROP_PANELS_DIR);
		System.clearProperty(GmSecurityPolicy.PROP_HTTP_PORT);
		System.clearProperty(GmSecurityPolicy.PROP_HTTP_HOST);
		System.clearProperty(GmSecurityPolicy.PROP_REQUIRE_AUTH);
		System.clearProperty(GmSecurityPolicy.PROP_OPS_TOKEN);
		System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
		ServerContext.getInstance().setRunMode();

		if (Files.exists(testPanelsDir)) {
			Files.walk(testPanelsDir)
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
		}
	}

	@Test
	public void testWebConsoleApiBootstrapAndExecute() throws Exception {
		// 1. GET /gm-console/api/bootstrap
		HttpRequest bootstrapReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/bootstrap"))
				.GET()
				.build();
		HttpResponse<String> bootstrapResp = httpClient.send(bootstrapReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, bootstrapResp.statusCode());

		JsonNode root = MAPPER.readTree(bootstrapResp.body());
		Assertions.assertTrue(root.path("ok").asBoolean());
		JsonNode data = root.path("data");
		Assertions.assertFalse(data.path("production").asBoolean());
		Assertions.assertTrue(data.has("virtualClock"));
		Assertions.assertTrue(data.has("schema"));
		Assertions.assertTrue(data.path("schema").has("actions"));
		Assertions.assertTrue(data.has("panels"));

		// 2. POST /gm-console/api/execute (action)
		String actionPayload = "{\"action\": \"time.status\"}";
		HttpRequest execActionReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/execute"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(actionPayload, StandardCharsets.UTF_8))
				.build();
		HttpResponse<String> execActionResp = httpClient.send(execActionReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, execActionResp.statusCode());
		JsonNode actionResult = MAPPER.readTree(execActionResp.body());
		Assertions.assertTrue(actionResult.path("ok").asBoolean());
		Assertions.assertTrue(actionResult.path("data").has("virtualTime"));

		// 3. POST /gm-console/api/execute (cmd)
		String cmdPayload = "{\"cmd\": \"time.status\"}";
		HttpRequest execCmdReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/execute"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(cmdPayload, StandardCharsets.UTF_8))
				.build();
		HttpResponse<String> execCmdResp = httpClient.send(execCmdReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, execCmdResp.statusCode());
		JsonNode cmdResult = MAPPER.readTree(execCmdResp.body());
		Assertions.assertTrue(cmdResult.path("ok").asBoolean());

		// 4. POST 非法 JSON 请求体
		HttpRequest malformedReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/execute"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{invalid_json", StandardCharsets.UTF_8))
				.build();
		HttpResponse<String> malformedResp = httpClient.send(malformedReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(400, malformedResp.statusCode());
		JsonNode malformedResult = MAPPER.readTree(malformedResp.body());
		Assertions.assertFalse(malformedResult.path("ok").asBoolean());
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), malformedResult.path("errorCode").asInt());

		// 5. GET /gm-console 首页资源可访问
		HttpRequest indexReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console"))
				.GET()
				.build();
		HttpResponse<String> indexResp = httpClient.send(indexReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, indexResp.statusCode());
		Assertions.assertTrue(indexResp.body().contains("GM Console"));
	}

	@Test
	public void testPanelHotReloadAndPathTraversalSecurity() throws Exception {
		// 1. 创建测试自定义面板
		Path panelFile = testPanelsDir.resolve("custom-test.html");
		Files.writeString(panelFile, "<div>Initial Panel V1</div>", StandardCharsets.UTF_8);

		// 2. GET /gm-console/api/panels 应能扫描到该面板
		HttpRequest listReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/api/panels"))
				.GET()
				.build();
		HttpResponse<String> listResp = httpClient.send(listReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, listResp.statusCode());
		JsonNode listNode = MAPPER.readTree(listResp.body());
		Assertions.assertTrue(listNode.path("ok").asBoolean());
		JsonNode panels = listNode.path("data");
		Assertions.assertTrue(panels.isArray());
		Assertions.assertEquals(1, panels.size());
		Assertions.assertEquals("custom-test.html", panels.get(0).path("file").asText());

		// 3. GET /gm-console/panels/custom-test.html 读取内容
		HttpRequest getPanelReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/panels/custom-test.html"))
				.GET()
				.build();
		HttpResponse<String> getPanelResp = httpClient.send(getPanelReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, getPanelResp.statusCode());
		Assertions.assertTrue(getPanelResp.body().contains("Initial Panel V1"));
		Assertions.assertEquals("no-store", getPanelResp.headers().firstValue("Cache-Control").orElse(""));

		// 4. 热重载测试：修改文件内容，免重启再次请求
		Files.writeString(panelFile, "<div>Updated Panel V2 Hot Reloaded</div>", StandardCharsets.UTF_8);
		HttpResponse<String> getPanelV2Resp = httpClient.send(getPanelReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(200, getPanelV2Resp.statusCode());
		Assertions.assertTrue(getPanelV2Resp.body().contains("Updated Panel V2 Hot Reloaded"));

		// 5. 路径穿越与非法文件名防护
		HttpRequest traversalReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/panels/..%2F..%2Fpom.xml"))
				.GET()
				.build();
		HttpResponse<String> traversalResp = httpClient.send(traversalReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(400, traversalResp.statusCode());

		HttpRequest invalidNameReq = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + listenPort + "/gm-console/panels/invalid$name.html"))
				.GET()
				.build();
		HttpResponse<String> invalidNameResp = httpClient.send(invalidNameReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		Assertions.assertEquals(400, invalidNameResp.statusCode());
	}

	@Test
	public void testProductionModeSecurityAndTokenAuth() throws Exception {
		// 1. 端口 17011 冲突保护
		System.setProperty(GmSecurityPolicy.PROP_HTTP_PORT, "17011");
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			GmSecurityPolicy.resolveHttpPort();
		});
		System.clearProperty(GmSecurityPolicy.PROP_HTTP_PORT);

		// 2. 生产模式网络强制收敛为 127.0.0.1
		System.setProperty(GmSecurityPolicy.PROP_HTTP_HOST, "0.0.0.0");
		String resolvedProdHost = GmSecurityPolicy.resolveHttpHost(RunMode.PRODUCTION);
		Assertions.assertEquals("127.0.0.1", resolvedProdHost);
		System.clearProperty(GmSecurityPolicy.PROP_HTTP_HOST);

		// 3. 生产模式下启动独立生产测试实例
		System.setProperty(ServerContext.SERVER_RUN_MODE, "PRODUCTION");
		ServerContext.getInstance().setRunMode();
		System.setProperty(GmSecurityPolicy.PROP_OPS_TOKEN, "secret-token-123456");

		GmWebConsoleServer prodServer = new GmWebConsoleServer(vertx);
		int prodPort = prodServer.start("127.0.0.1", 0).toCompletionStage().toCompletableFuture().get();

		try {
			// 4. 未带 Token 访问 bootstrap 返回 401
			HttpRequest noTokenReq = HttpRequest.newBuilder()
					.uri(URI.create("http://127.0.0.1:" + prodPort + "/gm-console/api/bootstrap"))
					.GET()
					.build();
			HttpResponse<String> noTokenResp = httpClient.send(noTokenReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			Assertions.assertEquals(401, noTokenResp.statusCode());
			JsonNode noTokenResult = MAPPER.readTree(noTokenResp.body());
			Assertions.assertFalse(noTokenResult.path("ok").asBoolean());
			Assertions.assertEquals(ErrorMsgEnum.gm_not.getId(), noTokenResult.path("errorCode").asInt());

			// 5. 错误 Token 访问返回 401
			HttpRequest wrongTokenReq = HttpRequest.newBuilder()
					.uri(URI.create("http://127.0.0.1:" + prodPort + "/gm-console/api/bootstrap"))
					.header("X-GM-Token", "wrong-token")
					.GET()
					.build();
			HttpResponse<String> wrongTokenResp = httpClient.send(wrongTokenReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			Assertions.assertEquals(401, wrongTokenResp.statusCode());

			// 6. 正确 Token 访问放行 (通过 Authorization: Bearer 或 X-GM-Token)
			HttpRequest authReq = HttpRequest.newBuilder()
					.uri(URI.create("http://127.0.0.1:" + prodPort + "/gm-console/api/bootstrap"))
					.header("X-GM-Token", "secret-token-123456")
					.GET()
					.build();
			HttpResponse<String> authResp = httpClient.send(authReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			Assertions.assertEquals(200, authResp.statusCode());
			JsonNode prodBootstrap = MAPPER.readTree(authResp.body());
			Assertions.assertTrue(prodBootstrap.path("ok").asBoolean());
			Assertions.assertTrue(prodBootstrap.path("data").path("production").asBoolean());

			// 7. 生产模式 Schema 物理裁剪断言：不含 DEV_TEST_ONLY (例如 time.offset)
			JsonNode actions = prodBootstrap.path("data").path("schema").path("actions");
			boolean hasDevTestOnly = false;
			for (JsonNode action : actions) {
				if ("time.offset".equals(action.path("id").asText()) || "DEV_TEST_ONLY".equals(action.path("security").asText())) {
					hasDevTestOnly = true;
					break;
				}
			}
			Assertions.assertFalse(hasDevTestOnly, "生产模式Schema必须物理裁剪DEV_TEST_ONLY动作");

			// 8. 生产模式自定义面板接口硬阻断
			HttpRequest prodPanelsReq = HttpRequest.newBuilder()
					.uri(URI.create("http://127.0.0.1:" + prodPort + "/gm-console/api/panels"))
					.header("X-GM-Token", "secret-token-123456")
					.GET()
					.build();
			HttpResponse<String> prodPanelsResp = httpClient.send(prodPanelsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			Assertions.assertEquals(200, prodPanelsResp.statusCode());
			JsonNode prodPanelsNode = MAPPER.readTree(prodPanelsResp.body());
			Assertions.assertEquals(0, prodPanelsNode.path("data").size());

			HttpRequest prodGetPanelReq = HttpRequest.newBuilder()
					.uri(URI.create("http://127.0.0.1:" + prodPort + "/gm-console/panels/custom-test.html"))
					.header("X-GM-Token", "secret-token-123456")
					.GET()
					.build();
			HttpResponse<String> prodGetPanelResp = httpClient.send(prodGetPanelReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			Assertions.assertEquals(403, prodGetPanelResp.statusCode());
			JsonNode prodGetPanelResult = MAPPER.readTree(prodGetPanelResp.body());
			Assertions.assertFalse(prodGetPanelResult.path("ok").asBoolean());
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.getId(), prodGetPanelResult.path("errorCode").asInt());

			// 9. 生产模式执行 DEV_TEST_ONLY 动作硬阻断
			String devActionPayload = "{\"action\": \"time.offset\", \"args\": {\"offset\": \"+1d\"}}";
			HttpRequest prodExecReq = HttpRequest.newBuilder()
					.uri(URI.create("http://127.0.0.1:" + prodPort + "/gm-console/api/execute"))
					.header("X-GM-Token", "secret-token-123456")
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(devActionPayload, StandardCharsets.UTF_8))
					.build();
			HttpResponse<String> prodExecResp = httpClient.send(prodExecReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			Assertions.assertEquals(200, prodExecResp.statusCode());
			JsonNode prodExecResult = MAPPER.readTree(prodExecResp.body());
			Assertions.assertFalse(prodExecResult.path("ok").asBoolean());
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.getId(), prodExecResult.path("errorCode").asInt());

		} finally {
			prodServer.stop().toCompletionStage().toCompletableFuture().get();
		}
	}
}
