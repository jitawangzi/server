package cn.game.games.net.game.handler;

import java.io.File;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.execute.TaskExecutorService;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.core.net.socket.handler.Handler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.runtime.RuntimeIdentityInventory;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.runtime.RuntimeIdentity;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.gm.ConfigGm;
import cn.game.games.net.game.gm.ConfigQueryService;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmResult;
import cn.game.games.net.game.gm.PlayerInspectService;
import cn.game.games.net.game.gm.ProtoCatalogService;
import cn.game.games.net.game.gm.ProtoGm;
import cn.game.games.net.game.gm.command.MiscGm;
import cn.game.games.net.game.gm.command.PlayerGm;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.TestMsg.TestConfigQueryRequest_6f000104;
import cn.game.protocol.protobuf.TestMsg.TestConfigQueryResponse_6f000105;
import cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001;
import cn.game.protocol.protobuf.TestMsg.TestGmCmdResponse_6f000002;
import cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102;
import cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103;
import cn.game.protocol.protobuf.TestMsg.TestGmSchemaRequest_6f000100;
import cn.game.protocol.protobuf.TestMsg.TestGmSchemaResponse_6f000101;
import cn.game.protocol.protobuf.TestMsg.TestInspectPlayerRequest_6f000108;
import cn.game.protocol.protobuf.TestMsg.TestInspectPlayerResponse_6f000109;
import cn.game.protocol.protobuf.TestMsg.TestProtoCatalogRequest_6f000106;
import cn.game.protocol.protobuf.TestMsg.TestProtoCatalogResponse_6f000107;
import cn.game.util.CborSnappyUtil;
import cn.game.util.ObjUtil;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;
import io.vertx.core.Vertx;

/**
 * 0x6f协议生成与1280线程池清理、runtimeIdentity表征（含真实子进程RuntimeIdentity.observe）及全链路Trace关联验证
 *
 * @author shuyongqiang
 */
@Isolated
public class TestHandler0x6fAndLegacyIdentityTest {
	private static final Logger log = LoggerFactory.getLogger(TestHandler0x6fAndLegacyIdentityTest.class);

	/**
	 * 0x6f测试用桩客户端
	 *
	 * @author shuyongqiang
	 */
	static final class StubGameClient extends GameClient {
		private final long playerId;
		private Object lastMessage;
		private int lastErrorCode;
		private int stubSeq;

		StubGameClient(long playerId) {
			super(null);
			this.playerId = playerId;
		}

		@Override
		public boolean needProcess(IProtocol<?> protocol) {
			this.stubSeq = protocol.getSeq();
			return true;
		}

		@Override
		public void afterProcess(IProtocol<?> protocol) {
		}

		@Override
		public void sendProtocol(Object message) {
			this.lastMessage = message;
			this.lastErrorCode = 0;
		}

		@Override
		public void sendProtocol(Object message, int errorCode) {
			this.lastMessage = message;
			this.lastErrorCode = errorCode;
		}

		@Override
		public long getPlayerId() {
			return playerId;
		}

		@Override
		public int getCurMessageSeq() {
			return stubSeq;
		}

		void setStubSeq(int stubSeq) {
			this.stubSeq = stubSeq;
		}
	}

	/**
	 * 配表端到端查询测试行模型
	 *
	 * @author shuyongqiang
	 */
	static final class SampleCfgRow {
		public int ID;
		public String title;

		SampleCfgRow(int id, String title) {
			this.ID = id;
			this.title = title;
		}
	}

	/**
	 * 配表端到端查询测试监听器
	 *
	 * @author shuyongqiang
	 */
	static final class SampleCfgListener extends ResourceListener {
		private final SampleCfgRow row = new SampleCfgRow(101, "测试表条目");

		@Override
		public String name() {
			return "SampleEndToEndTable";
		}

		@Override
		public void load() {
		}

		@SuppressWarnings("unused")
		public SampleCfgRow getNullable(int id) {
			return id == 101 ? row : null;
		}

		@SuppressWarnings("unused")
		public java.util.Collection<SampleCfgRow> list() {
			return java.util.List.of(row);
		}
	}

	/**
	 * 验证1280遗留线程池与硬编码ssss命令移除、0x6f000100-0x6f000109注册与PRODUCTION裁剪
	 *
	 * @throws Exception 反射或测试异常
	 */
	@Test
	void verifyLegacyPoolAndMiscSsssRemoved() throws Exception {
		// variantKeys: legacyTest1280PoolAndMiscSsss
		for (Field field : TestHandler.class.getDeclaredFields()) {
			Assertions.assertFalse(ExecutorService.class.isAssignableFrom(field.getType()),
					"TestHandler不应再持有1280遗留线程池字段: " + field.getName());
		}

		for (java.lang.reflect.Method method : MiscGm.class.getDeclaredMethods()) {
			GmAction action = method.getAnnotation(GmAction.class);
			if (action != null) {
				Assertions.assertNotEquals("ssss", action.name(), "MiscGm不应再注册硬编码ssss命令");
			}
			Assertions.assertNotEquals("ssss", method.getName());
		}

		GmDispatcher dispatcher = new GmDispatcher(new IdVirtualThreadProcessor());
		dispatcher.registerBean(new MiscGm());
		GmResult ssssRes = dispatcher.dispatchText(null, "ssss");
		Assertions.assertFalse(ssssRes.isOk());
		Assertions.assertEquals(ErrorMsgEnum.gm_cmd_not_exist.getId(), ssssRes.getErrorCode());

		for (int msgId = 0x6f000100; msgId <= 0x6f000109; msgId++) {
			Assertions.assertTrue(PbProtocol.idNameMap.containsKey(msgId),
					"PbProtocol缺少协议号映射: 0x" + Integer.toHexString(msgId));
		}

		Field runModeField = ServerContext.class.getDeclaredField("runMode");
		runModeField.setAccessible(true);
		RunMode previousMode = (RunMode) runModeField.get(ServerContext.getInstance());
		try {
			runModeField.set(ServerContext.getInstance(), RunMode.TEST);
			TestHandler nonProdHandler = initHandler();
			Assertions.assertTrue(nonProdHandler.getRegisteredCmds().contains(PbProtocol.TestGmCmdRequest_6f000001));
			Assertions.assertTrue(nonProdHandler.getRegisteredCmds().contains(PbProtocol.TestGmSchemaRequest_6f000100));
			Assertions.assertTrue(nonProdHandler.getRegisteredCmds().contains(PbProtocol.TestGmExecRequest_6f000102));

			runModeField.set(ServerContext.getInstance(), RunMode.PRODUCTION);
			TestHandler prodHandler = initHandler();
			Assertions.assertFalse(prodHandler.getRegisteredCmds().contains(PbProtocol.TestGmSchemaRequest_6f000100));
			Assertions.assertFalse(prodHandler.getRegisteredCmds().contains(PbProtocol.TestGmExecRequest_6f000102));
		} finally {
			runModeField.set(ServerContext.getInstance(), previousMode);
		}
	}

	/**
	 * 验证runtimeIdentity表征：生产环境返回production_gm_not_allow、未登录返回need_login、
	 * 非法挑战返回unknown、合法挑战经真实子JVM中未改写TestHandler调用RuntimeIdentity.observe端到端成功，且冷重载无状态篡改
	 *
	 * @throws Exception 反射、子进程或序列化异常
	 */
	@Test
	void verifyRuntimeIdentityProbeCharacterization() {
		try {
		ClassManager.getInstance().init();
		Field runModeField = ServerContext.class.getDeclaredField("runMode");
		runModeField.setAccessible(true);
		Field serverIdField = ServerContext.class.getDeclaredField("serverId");
		serverIdField.setAccessible(true);
		RunMode previousMode = (RunMode) runModeField.get(ServerContext.getInstance());
		String previousServerId = (String) serverIdField.get(ServerContext.getInstance());
		long playerId = 880003L;
		Map<Long, Player> playersMap = getPlayersMap();
		Map<Long, byte[]> selectByIdStore = new ConcurrentHashMap<>();

		try {
			runModeField.set(ServerContext.getInstance(), RunMode.TEST);
			serverIdField.set(ServerContext.getInstance(), "game_test");
			PlayerData data = new PlayerData();
			ObjUtil.setDefaultValue(data);
			data.setPlayerId(playerId);
			data.setUid(playerId);
			data.setName("RuntimeProbePlayer");
			data.setServerId("logic-2");
			data.setLevel(9);
			selectByIdStore.put(playerId, CborSnappyUtil.compress(data));

			Player player = new Player(data);
			player.initModule(new HashMap<String, BasePlayerModule>());
			StubGameClient client = new StubGameClient(playerId);
			player.setGameClient(client);
			player.setOnline(true);
			PlayerManager.getInstance().initAdd(player);

			TestHandler realHandler = initHandler();
			// CHARACTERIZATION: MUTATE:TestGmCmdRequest_6f000001 + variantKeys: runtimeIdentity
			TestGmCmdRequest_6f000001 invalidChallengeReq = TestGmCmdRequest_6f000001.newBuilder()
					.setCmd("runtimeIdentity nonce-20261114-xyz")
					.build();
			realHandler.dispatch(client, new ProtobufProtocol(PbProtocol.TestGmCmdRequest_6f000001, invalidChallengeReq, 4));
			Assertions.assertEquals(ErrorMsgEnum.unknown.getId(), client.lastErrorCode);

			String validChallenge = "0123456789abcdef0123456789abcdef";
			TestGmCmdRequest_6f000001 probeReq = TestGmCmdRequest_6f000001.newBuilder()
					.setCmd("runtimeIdentity " + validChallenge)
					.build();

			// 未登录/未绑定客户端应返回 need_login
			StubGameClient unboundClient = new StubGameClient(playerId);
			realHandler.dispatch(unboundClient, new ProtobufProtocol(PbProtocol.TestGmCmdRequest_6f000001, probeReq, 5));
			Assertions.assertEquals(ErrorMsgEnum.need_login.getId(), unboundClient.lastErrorCode);

			// PRODUCTION 运行态应直接返回 production_gm_not_allow
			runModeField.set(ServerContext.getInstance(), RunMode.PRODUCTION);
			realHandler.dispatch(client, new ProtobufProtocol(PbProtocol.TestGmCmdRequest_6f000001, probeReq, 6));
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.getId(), client.lastErrorCode);
			runModeField.set(ServerContext.getInstance(), RunMode.TEST);

			// 在独立子JVM中启用真实RuntimeIdentity，经未覆写的TestHandler执行6f000001端到端观察
			JsonNode identityNode = runRealRuntimeIdentityInChildJvm(validChallenge);
			Assertions.assertEquals(validChallenge, identityNode.get("challenge").asText());
			Assertions.assertEquals("game_test", identityNode.get("targetId").asText());
			Assertions.assertEquals("880003", identityNode.get("playerId").asText());
			Assertions.assertEquals("logic-2", identityNode.get("logicServerId").asText());
			Assertions.assertFalse(identityNode.get("instanceId").asText().isBlank());
			Assertions.assertFalse(identityNode.get("artifactSha256").asText().isBlank());
			Assertions.assertFalse(identityNode.get("configSha256").asText().isBlank());

			// 从独立存储字节流冷重载校验状态一致性，且反序列化实例非同一引用
			PlayerData reloaded = selectById(selectByIdStore, playerId);
			Assertions.assertNotSame(data, reloaded);
			Assertions.assertEquals(9, reloaded.getLevel());
			Assertions.assertEquals("logic-2", reloaded.getServerId());
			Player coldPlayer = new Player(reloaded);
			Assertions.assertNotSame(player, coldPlayer);
			Assertions.assertEquals(playerId, coldPlayer.getPlayerId());
		} finally {
			PlayerManager.getInstance().deletePlayer(playerId);
			playersMap.remove(playerId);
			runModeField.set(ServerContext.getInstance(), previousMode);
			serverIdField.set(ServerContext.getInstance(), previousServerId);
		}
	
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * 验证0x6f结构化协议全链路Trace透传、参数边界拦截以及IdVirtualThreadProcessor邮箱内自省不自死锁
	 *
	 * @throws Exception 反射或异步等待异常
	 */
	@Test
	void verifyStructured0x6fEndToEndFlow() throws Exception {
		ClassManager.getInstance().init();
		Field runModeField = ServerContext.class.getDeclaredField("runMode");
		runModeField.setAccessible(true);
		Field procField = ServerContext.class.getDeclaredField("processor");
		procField.setAccessible(true);
		RunMode previousMode = (RunMode) runModeField.get(ServerContext.getInstance());
		Processor previousProcessor = (Processor) procField.get(ServerContext.getInstance());
		long playerId = 880004L;
		Map<Long, Player> playersMap = getPlayersMap();
		ObjectMapper mapper = new ObjectMapper();
		TaskExecutorService executor = new TaskExecutorService();

		try {
			runModeField.set(ServerContext.getInstance(), RunMode.TEST);
			IdVirtualThreadProcessor processor = new IdVirtualThreadProcessor(executor);
			procField.set(ServerContext.getInstance(), processor);

			WatchServiceManager.getInstance().register(new SampleCfgListener());
			GmDispatcher dispatcher = GmDispatcher.getInstance();
			if (!dispatcher.hasCommand("player.time")) {
				dispatcher.registerBean(new PlayerGm());
			}
			if (!dispatcher.hasCommand("gm.config")) {
				dispatcher.registerBean(new ConfigGm(new ConfigQueryService()));
			}
			if (!dispatcher.hasCommand("gm.proto")) {
				dispatcher.registerBean(new ProtoGm(new ProtoCatalogService()));
			}
			if (!dispatcher.hasCommand("gm.inspect")) {
				dispatcher.registerBean(new PlayerInspectService());
			}

			PlayerData data = new PlayerData();
			ObjUtil.setDefaultValue(data);
			data.setPlayerId(playerId);
			data.setUid(playerId);
			data.setName("EndToEndPlayer");
			data.setServerId("game_test");
			Player player = new Player(data);
			player.initModule(new HashMap<String, BasePlayerModule>());
			player.getDevelopModule().setHeavenlyDaoLevel(1);
			StubGameClient client = new StubGameClient(playerId);
			player.setGameClient(client);
			PlayerManager.getInstance().initAdd(player);

			TestHandler handler = initHandler();

			// QUERY:TestGmSchemaRequest_6f000100 (variantKeys: structuredGmExec)
			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestGmSchemaRequest_6f000100,
					TestGmSchemaRequest_6f000100.newBuilder().setGroupFilter("player").build(), 1));
			Assertions.assertTrue(client.lastMessage instanceof TestGmSchemaResponse_6f000101);
			Assertions.assertEquals("1.0", ((TestGmSchemaResponse_6f000101) client.lastMessage).getSchemaVersion());

			// QUERY:TestConfigQueryRequest_6f000104
			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestConfigQueryRequest_6f000104,
					TestConfigQueryRequest_6f000104.newBuilder()
							.setOp("list")
							.setTable("SampleEndToEndTable")
							.setPage(1)
							.setPageSize(10)
							.build(), 2));
			Assertions.assertTrue(client.lastMessage instanceof TestConfigQueryResponse_6f000105);
			JsonNode cfgJson = mapper.readTree(((TestConfigQueryResponse_6f000105) client.lastMessage).getResultJson());
			Assertions.assertEquals(1, cfgJson.get("rows").size());

			// QUERY:TestProtoCatalogRequest_6f000106
			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestProtoCatalogRequest_6f000106,
					TestProtoCatalogRequest_6f000106.newBuilder()
							.setModuleFilter(0x6f)
							.setIncludeSchemas(false)
							.build(), 3));
			Assertions.assertTrue(client.lastMessage instanceof TestProtoCatalogResponse_6f000107);
			JsonNode catalogJson = mapper.readTree(((TestProtoCatalogResponse_6f000107) client.lastMessage).getCatalogJson());
			Assertions.assertTrue(catalogJson.get("modules").has("0x6f"));
			Assertions.assertTrue(catalogJson.get("errorCodes").isArray());

			// MUTATE:TestGmExecRequest_6f000102 (显式trace_id与空trace_id自动生成)
			client.setStubSeq(77);
			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestGmExecRequest_6f000102,
					TestGmExecRequest_6f000102.newBuilder()
							.setAction("tdlv")
							.setParamsJson("{\"level\":5}")
							.setTargetPlayerId(playerId)
							.setTraceId("trace-custom-001")
							.build(), 77));
			Assertions.assertTrue(client.lastMessage instanceof TestGmExecResponse_6f000103);
			TestGmExecResponse_6f000103 execResp = (TestGmExecResponse_6f000103) client.lastMessage;
			Assertions.assertEquals(playerId, execResp.getPlayerId());
			Assertions.assertEquals(PbProtocol.TestGmExecRequest_6f000102, execResp.getMsgId());
			Assertions.assertEquals("TestGmExecRequest_6f000102", execResp.getMsgName());
			Assertions.assertEquals(77, execResp.getSeq());
			Assertions.assertTrue(execResp.getTimestampMs() > 0L);
			Assertions.assertEquals("trace-custom-001", execResp.getTraceId());
			Assertions.assertTrue(mapper.readTree(execResp.getResultJson()).get("ok").asBoolean());

			// 空trace_id自动补齐校验
			client.setStubSeq(78);
			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestGmExecRequest_6f000102,
					TestGmExecRequest_6f000102.newBuilder()
							.setAction("tdlv")
							.setParamsJson("{\"level\":6}")
							.setTargetPlayerId(playerId)
							.build(), 78));
			TestGmExecResponse_6f000103 autoTraceResp = (TestGmExecResponse_6f000103) client.lastMessage;
			Assertions.assertFalse(autoTraceResp.getTraceId().isBlank());
			Assertions.assertEquals(78, autoTraceResp.getSeq());

			// 外部线程通过0x6f000108观测6f000102修改后的状态
			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestInspectPlayerRequest_6f000108,
					TestInspectPlayerRequest_6f000108.newBuilder()
							.setTargetPlayerId(playerId)
							.addModules("DevelopModule")
							.setScope("player")
							.build(), 79));
			Assertions.assertTrue(client.lastMessage instanceof TestInspectPlayerResponse_6f000109);
			JsonNode inspectJson = mapper.readTree(((TestInspectPlayerResponse_6f000109) client.lastMessage).getResultJson());
			Assertions.assertEquals(6, inspectJson.get("data").get("modules").get("DevelopModule").get("heavenlyDaoLevel").asInt());

			// 真实WS路由下：IdVirtualThreadProcessor投递6f000108到目标玩家自身邮箱内执行，毫秒级返回且无5秒自死锁
			CompletableFuture<JsonNode> selfInspectFuture = new CompletableFuture<>();
			long startNs = System.nanoTime();
			processor.process(playerId, () -> {
				try {
					StubGameClient mailboxClient = new StubGameClient(playerId);
					handler.dispatch(mailboxClient, new ProtobufProtocol(PbProtocol.TestInspectPlayerRequest_6f000108,
							TestInspectPlayerRequest_6f000108.newBuilder()
									.setTargetPlayerId(0L)
									.addModules("DevelopModule")
									.setScope("player")
									.build(), 80));
					TestInspectPlayerResponse_6f000109 selfResp = (TestInspectPlayerResponse_6f000109) mailboxClient.lastMessage;
					selfInspectFuture.complete(mapper.readTree(selfResp.getResultJson()));
				} catch (Exception e) {
					selfInspectFuture.completeExceptionally(e);
				}
			});
			JsonNode selfInspectJson = selfInspectFuture.get(1500, TimeUnit.MILLISECONDS);
			long mailboxElapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
			Assertions.assertTrue(selfInspectJson.get("ok").asBoolean(), "邮箱线程内6f000108自省自身必须成功");
			Assertions.assertTrue(mailboxElapsedMs < 1500L, "邮箱线程内6f000108自省自身不得发生自死锁 elapsedMs=" + mailboxElapsedMs);
			Assertions.assertEquals(6, selfInspectJson.get("data").get("modules").get("DevelopModule").get("heavenlyDaoLevel").asInt());

			// 负数及越界参数严格拒绝校验
			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestGmExecRequest_6f000102,
					TestGmExecRequest_6f000102.newBuilder().setAction("gm.schema").setTargetPlayerId(-1L).build(), 81));
			JsonNode negTargetExec = mapper.readTree(((TestGmExecResponse_6f000103) client.lastMessage).getResultJson());
			Assertions.assertFalse(negTargetExec.get("ok").asBoolean());
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), negTargetExec.get("errorCode").asInt());

			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestConfigQueryRequest_6f000104,
					TestConfigQueryRequest_6f000104.newBuilder().setOp("get").setTable("SampleEndToEndTable").setId(-5).build(), 82));
			JsonNode negCfgGet = mapper.readTree(((TestConfigQueryResponse_6f000105) client.lastMessage).getResultJson());
			Assertions.assertFalse(negCfgGet.get("ok").asBoolean());
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), negCfgGet.get("errorCode").asInt());

			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestProtoCatalogRequest_6f000106,
					TestProtoCatalogRequest_6f000106.newBuilder().setModuleFilter(-1).build(), 83));
			JsonNode negFilterProto = mapper.readTree(((TestProtoCatalogResponse_6f000107) client.lastMessage).getCatalogJson());
			Assertions.assertFalse(negFilterProto.get("ok").asBoolean());
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), negFilterProto.get("errorCode").asInt());

			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestProtoCatalogRequest_6f000106,
					TestProtoCatalogRequest_6f000106.newBuilder().setModuleFilter(256).build(), 84));
			JsonNode overFilterProto = mapper.readTree(((TestProtoCatalogResponse_6f000107) client.lastMessage).getCatalogJson());
			Assertions.assertFalse(overFilterProto.get("ok").asBoolean());
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), overFilterProto.get("errorCode").asInt());

			handler.dispatch(client, new ProtobufProtocol(PbProtocol.TestInspectPlayerRequest_6f000108,
					TestInspectPlayerRequest_6f000108.newBuilder().setTargetPlayerId(-2L).build(), 85));
			JsonNode negInspect = mapper.readTree(((TestInspectPlayerResponse_6f000109) client.lastMessage).getResultJson());
			Assertions.assertFalse(negInspect.get("ok").asBoolean());
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), negInspect.get("errorCode").asInt());
		} finally {
			executor.close();
			PlayerManager.getInstance().deletePlayer(playerId);
			playersMap.remove(playerId);
			runModeField.set(ServerContext.getInstance(), previousMode);
			procField.set(ServerContext.getInstance(), previousProcessor);
		}
	}

	private JsonNode runRealRuntimeIdentityInChildJvm(String challenge) throws Exception {
		Path repoRoot = Path.of("..").toAbsolutePath().normalize();
		Path gameTarget = Path.of("target").toAbsolutePath().normalize();
		Files.createDirectories(gameTarget);
		Path workDir = Files.createTempDirectory(gameTarget, "runtime-id-child-");
		try {
			Path resDir = Files.createDirectory(workDir.resolve("res"));
			Files.writeString(resDir.resolve("runtime-build.properties"),
					"sourceDigest=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef\nbuildId=test-build-001\n",
					StandardCharsets.UTF_8);

			Path apolloCache = Files.createDirectory(workDir.resolve("apollo-cache"));
			String appId = "game_test";
			String cluster = "default";
			Path cacheNsDir = apolloCache.resolve(appId).resolve("config-cache");
			Files.createDirectories(cacheNsDir);
			writeProps(cacheNsDir.resolve(appId + "+" + cluster + "+application.properties"), Map.of("springArgs", "fixture.xml"));
			writeProps(cacheNsDir.resolve(appId + "+" + cluster + "+config.properties"), Map.of("hot.update", "false"));
			writeProps(cacheNsDir.resolve(appId + "+" + cluster + "+jdbc.properties"), Map.of("dburl", "jdbc:mysql://127.0.0.1:3306/game_test"));
			writeProps(cacheNsDir.resolve(appId + "+" + cluster + "+redisson.properties"), Map.of("redisson", "redis://127.0.0.1:6379"));
			writeProps(cacheNsDir.resolve(appId + "+" + cluster + "+zookeeper.properties"), Map.of("zk", "127.0.0.1:2181"));
			writeProps(cacheNsDir.resolve(appId + "+" + cluster + "+fixture.xml.properties"), Map.of("content", "<beans/>"));
			writeProps(cacheNsDir.resolve(appId + "+" + cluster + "+log4j2.xml.properties"), Map.of("content", "<Configuration/>"));

			Path expectedPath = workDir.resolve("expected.json");
			Path logFile = workDir.resolve("child.log");
			int port;
			try (ServerSocket socket = new ServerSocket(0)) {
				port = socket.getLocalPort();
			}

			String baseCp = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
			String childCp = resDir + File.pathSeparator + baseCp;
			List<String> projectClasspathEntries = List.of(
					resDir.toString(),
					repoRoot.resolve("game/target/classes").toString(),
					repoRoot.resolve("game/target/test-classes").toString(),
					repoRoot.resolve("core/target/classes").toString(),
					repoRoot.resolve("protocol/target/classes").toString(),
					repoRoot.resolve("util/target/classes").toString());
			String joinedProjectCp = String.join(File.pathSeparator, projectClasspathEntries);

			List<String> captureCmd = new ArrayList<>();
			captureCmd.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
			captureCmd.add("-Dfile.encoding=UTF-8");
			captureCmd.add("-Dstdout.encoding=UTF-8");
			captureCmd.add("-Dapp.id=" + appId);
			captureCmd.add("-Denv=LOCAL");
			captureCmd.add("-Dapollo.cluster=" + cluster);
			captureCmd.add("-Dapollo.cache-dir=" + apolloCache);
			captureCmd.add("-Druntime.config.freeze=true");
			captureCmd.add("-Dserver.run.mode=test");
			captureCmd.add("-Dgame.runtimeIdentity.root=" + repoRoot);
			captureCmd.add("-cp");
			captureCmd.add(childCp);
			captureCmd.add(TestHandler0x6fAndLegacyIdentityTest.class.getName());
			captureCmd.add("capture");
			captureCmd.add(repoRoot.toString());
			captureCmd.add(joinedProjectCp);
			captureCmd.add(expectedPath.toString());
			captureCmd.add(challenge);

			Path captureLog = workDir.resolve("capture.log");
			Process captureProc = new ProcessBuilder(captureCmd)
					.directory(repoRoot.toFile())
					.redirectErrorStream(true)
					.redirectOutput(captureLog.toFile())
					.start();
			try {
				Assertions.assertTrue(captureProc.waitFor(45, TimeUnit.SECONDS), "RuntimeIdentity期望采集子进程超时");
				String captureOut = new String(Files.readAllBytes(captureLog), StandardCharsets.UTF_8);
				Assertions.assertEquals(0, captureProc.exitValue(), "RuntimeIdentity期望采集子进程失败: " + captureOut);
			} finally {
				if (captureProc.isAlive()) {
					captureProc.destroyForcibly();
				}
			}

			List<String> cmd = new ArrayList<>();
			cmd.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
			cmd.add("-Dfile.encoding=UTF-8");
			cmd.add("-Dstdout.encoding=UTF-8");
			cmd.add("-Dapp.id=" + appId);
			cmd.add("-Denv=LOCAL");
			cmd.add("-Dapollo.cluster=" + cluster);
			cmd.add("-Dapollo.cache-dir=" + apolloCache);
			cmd.add("-Druntime.config.freeze=true");
			cmd.add("-Dgame.runtimeIdentity.enabled=true");
			cmd.add("-Dserver.run.mode=test");
			cmd.add("-Dgame.runtimeIdentity.root=" + repoRoot);
			cmd.add("-Dgame.runtimeIdentity.port=" + port);
			cmd.add("-Druntime.expected=" + expectedPath);
			cmd.add("-cp");
			cmd.add(childCp);
			cmd.add(TestHandler0x6fAndLegacyIdentityTest.class.getName());
			cmd.add("probe");
			cmd.add(repoRoot.toString());
			cmd.add(joinedProjectCp);
			cmd.add(expectedPath.toString());
			cmd.add(challenge);

			Process process = new ProcessBuilder(cmd)
					.directory(repoRoot.toFile())
					.redirectErrorStream(true)
					.redirectOutput(logFile.toFile())
					.start();
			try {
				Assertions.assertTrue(process.waitFor(45, TimeUnit.SECONDS), "RuntimeIdentity子进程超时");
				String output = new String(Files.readAllBytes(logFile), StandardCharsets.UTF_8);
				Assertions.assertEquals(0, process.exitValue(), "RuntimeIdentity子进程失败: " + output);
				String marker = "RUNTIME_IDENTITY_JSON=";
				for (String line : output.split("\\R")) {
					if (line.startsWith(marker)) {
						return new ObjectMapper().readTree(line.substring(marker.length()));
					}
				}
				Assertions.fail("子进程未输出RUNTIME_IDENTITY_JSON: " + output);
				return null;
			} finally {
				if (process.isAlive()) {
					process.destroyForcibly();
				}
			}
		} finally {
			deleteRecursively(workDir);
		}
	}

	/**
	 * 子进程入口：支持先采集期望摘要再在独立JVM中启用真实RuntimeIdentity并通过未覆写TestHandler执行观察
	 *
	 * @param args 命令行参数（模式、仓库根目录、受控类路径、期望摘要路径、挑战串）
	 * @throws Exception 启动或测试异常
	 */
	public static void main(String[] args) throws Exception {
		String mode = args[0];
		Path repoRoot = Path.of(args[1]);
		String controlledClasspath = args[2];
		Path expectedPath = Path.of(args[3]);
		String challenge = args[4];

		System.setProperty("java.class.path", controlledClasspath);
		if ("capture".equals(mode)) {
			cn.game.util.runtime.FrozenApollo.INSTANCE.freeze();
			Map<String, String> expected = new RuntimeIdentityInventory(repoRoot, false).capture("game_test");
			new ObjectMapper().writeValue(expectedPath.toFile(), expected);
			System.exit(0);
			return;
		}

		Field vxField = VxHolder.class.getField("vertx");
		if (vxField.get(null) == null) {
			vxField.set(null, Vertx.vertx());
		}

		Field runModeField = ServerContext.class.getDeclaredField("runMode");
		runModeField.setAccessible(true);
		runModeField.set(ServerContext.getInstance(), RunMode.TEST);
		Field serverIdField = ServerContext.class.getDeclaredField("serverId");
		serverIdField.setAccessible(true);
		serverIdField.set(ServerContext.getInstance(), "game_test");

		RuntimeIdentity.prepare("game_test");
		RuntimeIdentity.ready();

		try {
			ClassManager.getInstance().init();
			long playerId = 880003L;
			PlayerData data = new PlayerData();
			ObjUtil.setDefaultValue(data);
			data.setPlayerId(playerId);
			data.setUid(playerId);
			data.setName("RuntimeProbeChildPlayer");
			data.setServerId("logic-2");
			data.setLevel(9);
			Player player = new Player(data);
			player.initModule(new HashMap<String, BasePlayerModule>());
			StubGameClient client = new StubGameClient(playerId);
			player.setGameClient(client);
			player.setOnline(true);
			PlayerManager.getInstance().initAdd(player);

			TestHandler realHandler = initHandler();
			TestGmCmdRequest_6f000001 probeReq = TestGmCmdRequest_6f000001.newBuilder()
					.setCmd("runtimeIdentity " + challenge)
					.build();
			realHandler.dispatch(client, new ProtobufProtocol(PbProtocol.TestGmCmdRequest_6f000001, probeReq, 9));
			if (client.lastErrorCode != 0 || !(client.lastMessage instanceof TestGmCmdResponse_6f000002)) {
				throw new IllegalStateException("TestHandler returned errorCode=" + client.lastErrorCode);
			}
			TestGmCmdResponse_6f000002 resp = (TestGmCmdResponse_6f000002) client.lastMessage;
			System.out.println("RUNTIME_IDENTITY_JSON=" + resp.getDebugInfo());
		} finally {
			RuntimeIdentity.stop();
			Vertx v = VxHolder.vertx;
			if (v != null) {
				v.close();
			}
		}
		System.exit(0);
	}

	private static void writeProps(Path file, Map<String, String> values) throws Exception {
		Properties props = new Properties();
		props.putAll(values);
		try (var out = Files.newOutputStream(file)) {
			props.store(out, null);
		}
	}

	private static void deleteRecursively(Path root) {
		if (root == null || !Files.exists(root)) {
			return;
		}
		try (Stream<Path> walk = Files.walk(root)) {
			walk.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (Exception e) {
					log.warn("清理子进程临时文件失败 path={}", path, e);
				}
			});
		} catch (Exception e) {
			log.warn("遍历清理子进程临时目录失败 root={}", root, e);
		}
	}

	private static TestHandler initHandler() throws Exception {
		TestHandler handler = new TestHandler();
		Field dispField = BaseHandler.class.getDeclaredField("dispatcher");
		dispField.setAccessible(true);
		dispField.set(handler, new DispatcherImpl() {
			@Override
			public void put(int module, Handler h) {
				if (!DispatcherImpl.getModuleHandlers().containsKey(module)) {
					super.put(module, h);
				}
			}
		});
		handler.init();
		return handler;
	}

	@SuppressWarnings("unchecked")
	private static PlayerData selectById(Map<Long, byte[]> store, long playerId) {
		return CborSnappyUtil.decompress(store.get(playerId), PlayerData.class);
	}

	private static Map<Long, Player> getPlayersMap() throws Exception {
		Field field = PlayerManager.class.getDeclaredField("id_players");
		field.setAccessible(true);
		return (Map<Long, Player>) field.get(PlayerManager.getInstance());
	}
}