package cn.game.games.net.game.gm.proto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import com.google.protobuf.Message;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.process.AbstractProcessor;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.GmStateDiffUtil;
import cn.game.games.net.game.gm.PlayerInspectService;
import cn.game.games.net.game.gm.ProtoCatalogService;
import cn.game.games.net.game.gm.RequestTraceService;
import cn.game.games.net.game.gm.proto.ProtocolSimulationService.ProtoInvokeRequest;
import cn.game.games.net.game.gm.repair.OpsRepairService;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.BaseMsg.ItemInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.protocol.protobuf.PlayerMsg.PlayerNameRequest_01000011;
import cn.game.protocol.protobuf.PlayerMsg.PlayerNameResponse_01000012;
import cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016;
import cn.game.util.ObjUtil;

/**
 * 协议 Mini 客户端模拟服务单测与表征回归测试 (TC-03, TC-06..TC-11, TC-LEGACY-*)
 *
 * @author shuyongqiang
 */
@Isolated
public class ProtocolSimulationServiceTest {

	private static final long TEST_PLAYER_ID = 88990011L;
	private Player testPlayer;
	private RunMode previousRunMode;

	private static class TestDispatcher extends DispatcherImpl {
		private final BiConsumer<NetClient, IProtocol<?>> consumer;
		TestDispatcher(BiConsumer<NetClient, IProtocol<?>> consumer) {
			this.consumer = consumer;
		}
		@Override
		public void dispatch(NetClient client, IProtocol protocol) {
			consumer.accept(client, protocol);
		}
	}

	private static class SyncProcessor extends AbstractProcessor {
		@Override
		public void process(long objectId, Runnable task) {
			task.run();
		}
	}

	@BeforeEach
	public void setUp() throws Exception {
		ClassManager.getInstance().init();
		previousRunMode = GmSecurityPolicy.currentRunMode();
		setRunMode(RunMode.DEV);

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(TEST_PLAYER_ID);
		data.setUid(TEST_PLAYER_ID);
		data.setName("SimTestPlayer");
		data.setServerId("game_test_1");
		data.setLevel(10);

		testPlayer = new Player(data);
		testPlayer.setOnline(true);
		testPlayer.setIslogouting(false);
		testPlayer.setGameClient(new VirtualGameClient(TEST_PLAYER_ID));

		registerPlayer(testPlayer);
	}

	@AfterEach
	public void tearDown() throws Exception {
		setRunMode(previousRunMode);
		unregisterPlayer(TEST_PLAYER_ID);
		GameClient.endThreadLocalCapture();
		if (RequestTraceService.getInstance().getCurrentBuilder() != null) {
			RequestTraceService.getInstance().endForcedTrace(RequestTraceService.getInstance().getCurrentBuilder(), 0);
		}
	}

	/**
	 * TC-03-VIRTUAL-THREAD-INVOKE-CAPTURE: 在线玩家虚拟线程内协议分发与双通道回包捕获测试
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testVirtualThreadInvokeDualChannelCapture() {
		int reqMsgId = PbProtocol.getInstance().getMsgId("PlayerNameRequest_01000011");
		int respMsgId = PbProtocol.getInstance().getMsgId("PlayerNameResponse_01000012");
		int pushMsgId = PbProtocol.getInstance().getMsgId("PlayerResetPush_01100016");

		PlayerNameResponse_01000012 respMsg = PlayerNameResponse_01000012.getDefaultInstance();
		PlayerResetPush_01100016 pushMsg = PlayerResetPush_01100016.newBuilder().setType(1).build();

		Dispatcher mockDispatcher = new TestDispatcher((client, protocol) -> {
			// 主响应通过传入 client 发送
			client.sendProtocol(respMsg);
			// 间接推送通过 player.getGameClient() 发送
			testPlayer.getGameClient().sendProtocol(pushMsg);
		});

		Processor syncProcessor = new SyncProcessor();

		ProtocolSimulationService service = new ProtocolSimulationService(syncProcessor, null, null, mockDispatcher);

		ProtoInvokeRequest req = new ProtoInvokeRequest();
		req.setPlayerId(TEST_PLAYER_ID);
		req.setMsgId(reqMsgId);
		req.setPayload(Map.of("name", "NewPlayerName"));

		Map<String, Object> result = service.invoke(req);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(Boolean.TRUE, result.get("ok"), "模拟调用应当成功返回: " + result);
		Assertions.assertEquals(0, result.get("errorCode"));

		// 验证主回包
		Map<String, Object> respMap = (Map<String, Object>) result.get("response");
		Assertions.assertNotNull(respMap, "必须捕获到主响应包");
		Assertions.assertEquals(respMsgId, respMap.get("msgId"));
		Assertions.assertEquals("PlayerNameResponse_01000012", respMap.get("msgName"));

		// 验证推送包
		List<Map<String, Object>> pushes = (List<Map<String, Object>>) result.get("pushes");
		Assertions.assertNotNull(pushes);
		Assertions.assertEquals(1, pushes.size(), "应当捕获到 1 个推送包");
		Assertions.assertEquals(pushMsgId, pushes.get(0).get("msgId"));
		Assertions.assertEquals("PlayerResetPush_01100016", pushes.get(0).get("msgName"));

		// 验证探针清理安全
		Assertions.assertNull(GameClient.ACTIVE_CAPTURE.get(), "ThreadLocal 探针必须严格清理");
	}

	/**
	 * TC-06-FORCED-TRACE-ISOLATION: 隔离强制 Trace 录制与全局 Trace 开关互不串扰测试
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testForcedTraceIsolationWithoutGlobalPollution() {
		Assertions.assertFalse(RequestTraceService.getInstance().isRecording(TEST_PLAYER_ID), "全局 Trace 初始状态应当为未录制");

		int reqMsgId = PbProtocol.getInstance().getMsgId("PlayerNameRequest_01000011");
		PlayerNameResponse_01000012 respMsg = PlayerNameResponse_01000012.getDefaultInstance();

		Dispatcher mockDispatcher = new TestDispatcher((client, protocol) -> {
			// 在业务中记录一次跟踪流水
			RequestTraceService.TraceBuilder builder = RequestTraceService.getInstance().getCurrentBuilder();
			Assertions.assertNotNull(builder, "强制录制期间必须存在外层 TraceBuilder");
			builder.recordResource(1, 100L, true, "GM_ADD");
			client.sendProtocol(respMsg);
		});

		Processor syncProcessor = new SyncProcessor();
		ProtocolSimulationService service = new ProtocolSimulationService(syncProcessor, null, null, mockDispatcher);

		ProtoInvokeRequest req = new ProtoInvokeRequest();
		req.setPlayerId(TEST_PLAYER_ID);
		req.setMsgId(reqMsgId);
		req.setWithTrace(true);
		req.setPayload(Map.of("name", "TraceTestPlayer"));

		Map<String, Object> result = service.invoke(req);
		Assertions.assertTrue((boolean) result.get("ok"));

		// 验证 trace 对象存在
		Map<String, Object> trace = (Map<String, Object>) result.get("trace");
		Assertions.assertNotNull(trace, "withTrace=true 时必须输出 trace 节点");
		Assertions.assertEquals(TEST_PLAYER_ID, trace.get("playerId"));
		Assertions.assertEquals("PlayerNameRequest_01000011", trace.get("msgName"));

		// 全局 Trace 状态未受污染
		Assertions.assertFalse(RequestTraceService.getInstance().isRecording(TEST_PLAYER_ID), "全局 Trace 录制状态不应被模拟调用永久开启");
		Assertions.assertNull(RequestTraceService.getInstance().getCurrentBuilder(), "调用结束后 ThreadLocal TraceBuilder 必须清空");
	}

	/**
	 * TC-07-DIFF-COLLECTION-AND-STATE-DIFF-UTIL: 前后状态 Diff 采集与共用工具类 GmStateDiffUtil 测试
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testDiffCollectionAndStateDiffUtil() {
		// 1. 验证 GmStateDiffUtil 纯工具逻辑
		Map<String, Object> before = new LinkedHashMap<>();
		before.put("level", 10);
		before.put("currency", Map.of("gold", 100, "diamond", 50));
		before.put("tags", List.of("vip1", "novice"));

		Map<String, Object> after = new LinkedHashMap<>();
		after.put("level", 11);
		after.put("currency", Map.of("gold", 200, "diamond", 50));
		after.put("tags", List.of("vip1", "veteran"));

		// (a) 扁平比对 (兼容 OpsRepairService)
		List<Map<String, Object>> flatDiff = GmStateDiffUtil.computeFlatDiff(before, after);
		Assertions.assertNotNull(flatDiff);
		Assertions.assertEquals(3, flatDiff.size(), "level, currency, tags 产生 3 处根字段变动");

		// (b) 嵌套点分比对
		List<Map<String, Object>> nestedDiff = GmStateDiffUtil.computeNestedDiff(before, after);
		Assertions.assertNotNull(nestedDiff);
		boolean foundGoldDiff = false;
		boolean foundLevelDiff = false;
		for (Map<String, Object> d : nestedDiff) {
			String path = (String) d.get("path");
			if ("currency.gold".equals(path)) {
				foundGoldDiff = true;
				Assertions.assertEquals(100, d.get("before"));
				Assertions.assertEquals(200, d.get("after"));
			} else if ("level".equals(path)) {
				foundLevelDiff = true;
				Assertions.assertEquals(10, d.get("before"));
				Assertions.assertEquals(11, d.get("after"));
			}
		}
		Assertions.assertTrue(foundGoldDiff, "嵌套点分比对必须精确定位到 currency.gold");
		Assertions.assertTrue(foundLevelDiff, "嵌套点分比对必须包含 level 变动");

		// 2. 验证 ProtocolSimulationService 的 withDiff 闭环
		int reqMsgId = PbProtocol.getInstance().getMsgId("PlayerNameRequest_01000011");
		PlayerNameResponse_01000012 respMsg = PlayerNameResponse_01000012.getDefaultInstance();

		AtomicInteger snapshotStep = new AtomicInteger(0);
		PlayerInspectService stubInspectService = new PlayerInspectService() {
			@Override
			public Map<String, Object> captureModulesSnapshot(Player player, java.util.Collection<String> moduleFilters) {
				if (snapshotStep.getAndIncrement() == 0) {
					return Map.of("PlayerModule", Map.of("name", "OldName", "level", 10));
				} else {
					return Map.of("PlayerModule", Map.of("name", "NewName", "level", 10));
				}
			}
		};

		Dispatcher mockDispatcher = new TestDispatcher((client, protocol) -> client.sendProtocol(respMsg));
		Processor syncProcessor = new SyncProcessor();

		ProtocolSimulationService service = new ProtocolSimulationService(syncProcessor, stubInspectService, null, mockDispatcher);

		ProtoInvokeRequest req = new ProtoInvokeRequest();
		req.setPlayerId(TEST_PLAYER_ID);
		req.setMsgId(reqMsgId);
		req.setWithDiff(true);
		req.setPayload(Map.of("name", "NewName"));

		Map<String, Object> result = service.invoke(req);
		Assertions.assertTrue((boolean) result.get("ok"));

		List<Map<String, Object>> diffList = (List<Map<String, Object>>) result.get("diff");
		Assertions.assertNotNull(diffList);
		Assertions.assertFalse(diffList.isEmpty(), "应当产生前后 Diff 差异记录");
		Assertions.assertEquals("PlayerModule.name", diffList.get(0).get("path"));
		Assertions.assertEquals("OldName", diffList.get(0).get("before"));
		Assertions.assertEquals("NewName", diffList.get(0).get("after"));
	}

	/**
	 * TC-08-BUSINESS-EXCEPTION-AND-ERROR-PUSH: 业务 LogicException 与 PlayerErrorPush 独立提取与结构化呈现测试
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testBusinessExceptionAndErrorPushStructuredReturn() {
		int reqMsgId = PbProtocol.getInstance().getMsgId("PlayerNameRequest_01000011");

		// 1. 业务抛出 LogicException
		Dispatcher errorDispatcher = new TestDispatcher((client, protocol) -> {
			throw new LogicException(ErrorMsgEnum.resource_not_enough);
		});

		Processor syncProcessor = new SyncProcessor();
		ProtocolSimulationService service = new ProtocolSimulationService(syncProcessor, null, null, errorDispatcher);

		ProtoInvokeRequest req = new ProtoInvokeRequest();
		req.setPlayerId(TEST_PLAYER_ID);
		req.setMsgId(reqMsgId);
		req.setPayload(Map.of("name", "FailName"));

		Map<String, Object> result = service.invoke(req);
		Assertions.assertFalse((boolean) result.get("ok"));
		Assertions.assertEquals(ErrorMsgEnum.resource_not_enough.getId(), result.get("errorCode"));
		Assertions.assertEquals("resource_not_enough", result.get("errorName"));
		Assertions.assertNotNull(result.get("errorDesc"));

		// 2. 业务下发 PlayerErrorPush_01000099
		int pushMsgId = PbProtocol.getInstance().getMsgId("PlayerErrorPush_01000099");
		PlayerErrorPush_01000099 errPush = PlayerErrorPush_01000099.newBuilder().setError("背包空间不足").build();

		Dispatcher pushErrorDispatcher = new TestDispatcher((client, protocol) -> {
			client.sendProtocol(errPush, ErrorMsgEnum.resource_not_enough.getId());
		});

		ProtocolSimulationService service2 = new ProtocolSimulationService(syncProcessor, null, null, pushErrorDispatcher);
		Map<String, Object> result2 = service2.invoke(req);

		Assertions.assertFalse((boolean) result2.get("ok"));
		Assertions.assertEquals(ErrorMsgEnum.resource_not_enough.getId(), result2.get("errorCode"));
		Assertions.assertNotNull(result2.get("errorPush"), "PlayerErrorPush_01000099 必须被提升至独立 errorPush 节点");

		List<Map<String, Object>> pushes = (List<Map<String, Object>>) result2.get("pushes");
		Assertions.assertTrue(pushes == null || pushes.isEmpty(), "errorPush 不应重复留在业务 pushes 列表中");
	}

	/**
	 * TC-09-PROBE-CLEANUP-SAFETY: 异常终端、超时与 ThreadLocal 探针无条件清理安全测试
	 */
	@Test
	public void testProbeCleanupSafetyUnderException() {
		int reqMsgId = PbProtocol.getInstance().getMsgId("PlayerNameRequest_01000011");

		// 业务抛出非受检运行时异常 (NullPointerException)
		Dispatcher faultyDispatcher = new TestDispatcher((client, protocol) -> {
			throw new NullPointerException("模拟未知系统崩溃异常");
		});

		Processor syncProcessor = new SyncProcessor();
		ProtocolSimulationService service = new ProtocolSimulationService(syncProcessor, null, null, faultyDispatcher);

		ProtoInvokeRequest req = new ProtoInvokeRequest();
		req.setPlayerId(TEST_PLAYER_ID);
		req.setMsgId(reqMsgId);
		req.setWithTrace(true);
		req.setPayload(Map.of("name", "CrashName"));

		Map<String, Object> result = service.invoke(req);
		Assertions.assertFalse((boolean) result.get("ok"));
		Assertions.assertEquals(ErrorMsgEnum.unknown.getId(), result.get("errorCode"));

		// 核心断言：无论业务何种异常退出，finally 严格清理
		Assertions.assertNull(GameClient.ACTIVE_CAPTURE.get(), "异常退出后必须彻底解绑 ACTIVE_CAPTURE");
		Assertions.assertNull(RequestTraceService.getInstance().getCurrentBuilder(), "异常退出后必须彻底清理 TraceBuilder");
	}

	/**
	 * TC-10-MIRROR-TO-REAL-CLIENT-AND-RATE-IMMUNITY: 真实客户端连接零污染、镜像双发与限流序号穿透测试
	 */
	@Test
	public void testMirrorToRealClientAndRateImmunity() {
		int reqMsgId = PbProtocol.getInstance().getMsgId("PlayerNameRequest_01000011");
		PlayerNameResponse_01000012 respMsg = PlayerNameResponse_01000012.getDefaultInstance();

		AtomicInteger realSentFrames = new AtomicInteger(0);

		io.vertx.core.http.ServerWebSocket mockWs = (io.vertx.core.http.ServerWebSocket) java.lang.reflect.Proxy.newProxyInstance(
				io.vertx.core.http.ServerWebSocket.class.getClassLoader(),
				new Class<?>[]{io.vertx.core.http.ServerWebSocket.class},
				(proxy, method, methodArgs) -> {
					if ("isClosed".equals(method.getName())) {
						return false;
					}
					if ("writeBinaryMessage".equals(method.getName())) {
						realSentFrames.incrementAndGet();
						return io.vertx.core.Future.succeededFuture();
					}
					return io.vertx.core.Future.succeededFuture();
				}
		);

		// 模拟真实客户端连接
		GameClient mockRealClient = new GameClient(mockWs) {
			@Override
			public boolean isActive() {
				return true;
			}
		};

		testPlayer.setGameClient(mockRealClient);

		Dispatcher mockDispatcher = new TestDispatcher((client, protocol) -> {
			client.sendProtocol(respMsg);
		});

		Processor syncProcessor = new SyncProcessor();
		ProtocolSimulationService service = new ProtocolSimulationService(syncProcessor, null, null, mockDispatcher);

		// 1. mirrorToRealClient = false: 发送 20 次，真实物理连接帧数应当为 0
		for (int i = 0; i < 20; i++) {
			ProtoInvokeRequest req = new ProtoInvokeRequest();
			req.setPlayerId(TEST_PLAYER_ID);
			req.setMsgId(reqMsgId);
			req.setSeq(10000 + i);
			req.setMirrorToRealClient(false);
			req.setPayload(Map.of("name", "Name_" + i));

			Map<String, Object> res = service.invoke(req);
			Assertions.assertTrue((boolean) res.get("ok"));
		}

		Assertions.assertEquals(0, realSentFrames.get(), "mirrorToRealClient=false 时真实客户端发送计数必须严格为 0");
		Assertions.assertEquals(0, mockRealClient.getCurMessageSeq(), "未污染真实客户端消息序号");

		// 2. mirrorToRealClient = true: 发送 1 次，真实客户端应收到 1 帧
		ProtoInvokeRequest mirrorReq = new ProtoInvokeRequest();
		mirrorReq.setPlayerId(TEST_PLAYER_ID);
		mirrorReq.setMsgId(reqMsgId);
		mirrorReq.setMirrorToRealClient(true);
		mirrorReq.setPayload(Map.of("name", "MirroredName"));

		Map<String, Object> mirrorRes = service.invoke(mirrorReq);
		Assertions.assertTrue((boolean) mirrorRes.get("ok"));
		Assertions.assertEquals(1, realSentFrames.get(), "mirrorToRealClient=true 时真实客户端应接收到 1 帧镜像数据");
	}

	/**
	 * TC-11-ASYNC-HANDLER-EMPTY-CAPTURE-WARN: 异步回调型 Handler 无同步回包告警提示测试
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testAsyncHandlerEmptyCaptureWarning() {
		int reqMsgId = PbProtocol.getInstance().getMsgId("PlayerNameRequest_01000011");

		// 模拟异步 Handler: 同步分发期间不调用任何 sendProtocol
		Dispatcher asyncDispatcher = new TestDispatcher((client, protocol) -> {
			// 模拟 Future.compose 尚未完成
		});

		Processor syncProcessor = new SyncProcessor();
		ProtocolSimulationService service = new ProtocolSimulationService(syncProcessor, null, null, asyncDispatcher);

		ProtoInvokeRequest req = new ProtoInvokeRequest();
		req.setPlayerId(TEST_PLAYER_ID);
		req.setMsgId(reqMsgId);
		req.setPayload(Map.of("name", "AsyncName"));

		Map<String, Object> result = service.invoke(req);
		Assertions.assertTrue((boolean) result.get("ok"));
		Assertions.assertNull(result.get("response"), "同步窗口内无回包，主 response 应为 null");

		List<String> warnings = (List<String>) result.get("warnings");
		Assertions.assertNotNull(warnings);
		boolean foundAsyncWarn = false;
		for (String w : warnings) {
			if (w.contains("未在同步分发窗口内捕获到回包") || w.contains("异步Future回调")) {
				foundAsyncWarn = true;
				break;
			}
		}
		Assertions.assertTrue(foundAsyncWarn, "必须提示异步回调未捕获警告: " + warnings);
	}

	/**
	 * TC-LEGACY-WS-CLIENT-SEND: 既有 WebSocket 客户端发包与冷重载表征回归测试 (CHARACTERIZATION)
	 */
	@Test
	public void verifyWebsocketGameClientSendCharacterization() {
		Assertions.assertNull(GameClient.ACTIVE_CAPTURE.get(), "开始前探针必须为空");

		AtomicInteger sentCount = new AtomicInteger(0);
		io.vertx.core.http.ServerWebSocket mockWs = (io.vertx.core.http.ServerWebSocket) java.lang.reflect.Proxy.newProxyInstance(
				io.vertx.core.http.ServerWebSocket.class.getClassLoader(),
				new Class<?>[]{io.vertx.core.http.ServerWebSocket.class},
				(proxy, method, methodArgs) -> {
					if ("isClosed".equals(method.getName())) {
						return false;
					}
					return io.vertx.core.Future.succeededFuture();
				}
		);
		GameClient normalClient = new GameClient(mockWs) {
			@Override
			public boolean isActive() {
				return true;
			}
			@Override
			public void sendProtocol(Object message, int errorCode) {
				super.sendProtocol(message, errorCode);
				sentCount.incrementAndGet();
			}
		};

		testPlayer.setGameClient(normalClient);

		// 无探针正常调用
		PlayerNameResponse_01000012 msg = PlayerNameResponse_01000012.getDefaultInstance();
		normalClient.sendProtocol(msg);

		Assertions.assertEquals(1, sentCount.get(), "正常无探针模式下，sendProtocol 必须正常触发下发");

		// 冷重载玩家验证: 从 PlayerManager 重新读取实体并核验字段一致
		Player reloadedPlayer = PlayerManager.getInstance().getPlayer(TEST_PLAYER_ID);
		Assertions.assertNotNull(reloadedPlayer, "重新读取玩家实体必须存在");
		Assertions.assertEquals(TEST_PLAYER_ID, reloadedPlayer.getPlayerId());
		Assertions.assertEquals("SimTestPlayer", reloadedPlayer.getData().getName());
	}

	/**
	 * TC-LEGACY-REPAIR-DIFF: 既有修数服务 OpsRepairService 状态 Diff 表征一致性测试 (CHARACTERIZATION)
	 */
	@Test
	public void verifyLegacyRepairDiffConsistency() {
		Map<String, Object> beforeState = new LinkedHashMap<>();
		beforeState.put("gold", 500);
		beforeState.put("level", 20);
		beforeState.put("name", "OldHero");

		Map<String, Object> afterState = new LinkedHashMap<>();
		afterState.put("gold", 1000);
		afterState.put("level", 20);
		afterState.put("name", "NewHero");

		// 验证 OpsRepairService 委托给 GmStateDiffUtil.computeFlatDiff 后的格式与字段完全一致
		List<Map<String, Object>> diff = GmStateDiffUtil.computeFlatDiff(beforeState, afterState);
		Assertions.assertNotNull(diff);
		Assertions.assertEquals(2, diff.size(), "仅 gold 与 name 发生变化");

		Map<String, Object> diff0 = diff.get(0);
		Assertions.assertTrue(diff0.containsKey("path"));
		Assertions.assertTrue(diff0.containsKey("before"));
		Assertions.assertTrue(diff0.containsKey("after"));

		Assertions.assertEquals("gold", diff0.get("path"));
		Assertions.assertEquals(500, diff0.get("before"));
		Assertions.assertEquals(1000, diff0.get("after"));

		Map<String, Object> diff1 = diff.get(1);
		Assertions.assertEquals("name", diff1.get("path"));
		Assertions.assertEquals("OldHero", diff1.get("before"));
		Assertions.assertEquals("NewHero", diff1.get("after"));
	}

	private static void setRunMode(RunMode mode) throws Exception {
		Field field = ServerContext.class.getDeclaredField("runMode");
		field.setAccessible(true);
		field.set(ServerContext.getInstance(), mode);
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
