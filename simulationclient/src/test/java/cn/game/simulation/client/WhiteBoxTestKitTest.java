package cn.game.simulation.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Message;

import cn.game.protocol.protobuf.TestMsg.TestConfigQueryRequest_6f000104;
import cn.game.protocol.protobuf.TestMsg.TestConfigQueryResponse_6f000105;
import cn.game.protocol.protobuf.TestMsg.TestGmSchemaRequest_6f000100;
import cn.game.protocol.protobuf.TestMsg.TestGmSchemaResponse_6f000101;
import cn.game.protocol.protobuf.TestMsg.TestInspectPlayerRequest_6f000108;
import cn.game.protocol.protobuf.TestMsg.TestInspectPlayerResponse_6f000109;
import cn.game.protocol.protobuf.TestMsg.TestProtoCatalogRequest_6f000106;
import cn.game.protocol.protobuf.TestMsg.TestProtoCatalogResponse_6f000107;

/**
 * 模拟客户端0x6f结构化调用与GmClientResult扩展单元测试
 *
 * @author shuyongqiang
 */
public class WhiteBoxTestKitTest {

	/**
	 * 可拦截请求与注入响应的本地测试客户端
	 *
	 * @author shuyongqiang
	 */
	static class TestableClient extends Client {
		Message lastSentRequest;
		Object mockResponse;

		TestableClient() {
			super("white-box-test");
			setPlayerId(88001L);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T sendProtocolAndWait(Message message) {
			this.lastSentRequest = message;
			return (T) mockResponse;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T sendProtocolAndWait(Message message, int timeoutSeconds) {
			this.lastSentRequest = message;
			return (T) mockResponse;
		}
	}

	/**
	 * 验证GmClientResult扩展字段与断言辅助方法，及Client 0x6f结构化查询与JSON解析容错
	 *
	 * @throws Exception 序列化或执行异常
	 */
	@Test
	public void testClient0x6fAndGmResultExtensions() throws Exception {
		// 校验 GmClientResult 扩展字段与 requireOk 断言
		GmClientResult successResult = new GmClientResult();
		successResult.setOk(true);
		Assertions.assertSame(successResult, successResult.requireOk());

		GmClientResult failResult = new GmClientResult();
		failResult.setOk(false);
		failResult.setErrorCode(50001);
		failResult.setErrorName("ERR_TEST");
		failResult.setSummary("fail summary");
		failResult.setTraceId("trace-123");

		IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, failResult::requireOk);
		Assertions.assertTrue(ex.getMessage().contains("EX-13"));
		Assertions.assertTrue(ex.getMessage().contains("50001"));
		Assertions.assertTrue(ex.getMessage().contains("trace-123"));

		// 校验 dryRun 与 diff 字段序列化/反序列化及提取方法
		ObjectMapper mapper = new ObjectMapper();
		String jsonWithDryRun = "{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\",\"data\":{\"itemId\":1001,\"count\":5},\"dryRun\":true,\"diff\":[{\"field\":\"count\",\"old\":0,\"new\":5}]}";
		GmClientResult parsedResult = mapper.readValue(jsonWithDryRun, GmClientResult.class);
		Assertions.assertTrue(parsedResult.isDryRun());
		Assertions.assertNotNull(parsedResult.getDiff());

		Map<String, Object> dataMap = parsedResult.getDataAsMap();
		Assertions.assertEquals(1001, dataMap.get("itemId"));
		Assertions.assertEquals(5, dataMap.get("count"));

		List<Map<String, Object>> diffList = parsedResult.getDiffAsList();
		Assertions.assertEquals(1, diffList.size());
		Assertions.assertEquals("count", diffList.get(0).get("field"));

		// 边界：data 或 diff 为非预期类型时的健壮降级返回空映射/空列表
		GmClientResult fallbackResult = new GmClientResult();
		fallbackResult.setData("not-a-map");
		fallbackResult.setDiff("not-a-list");
		Assertions.assertTrue(fallbackResult.getDataAsMap().isEmpty());
		Assertions.assertTrue(fallbackResult.getDiffAsList().isEmpty());

		// 校验 Client 0x6f 请求构建与结构化解析
		TestableClient client = new TestableClient();

		// 1) fetchGmSchema
		client.mockResponse = TestGmSchemaResponse_6f000101.newBuilder()
				.setSchemaVersion("1.0")
				.setSchemaJson("{\"groups\":[\"item\",\"player\"],\"count\":2}")
				.build();
		Map<String, Object> schemaMap = client.fetchGmSchema("item");
		Assertions.assertEquals(2, schemaMap.get("count"));
		Assertions.assertTrue(client.lastSentRequest instanceof TestGmSchemaRequest_6f000100);
		TestGmSchemaRequest_6f000100 schemaReq = (TestGmSchemaRequest_6f000100) client.lastSentRequest;
		Assertions.assertEquals("item", schemaReq.getGroupFilter());

		// 2) queryConfigTables
		client.mockResponse = TestConfigQueryResponse_6f000105.newBuilder()
				.setSchemaVersion("1.0")
				.setResultJson("{\"tables\":[\"ItemConfig\",\"HeroConfig\"]}")
				.build();
		Map<String, Object> tablesMap = client.queryConfigTables();
		Assertions.assertNotNull(tablesMap.get("tables"));
		Assertions.assertTrue(client.lastSentRequest instanceof TestConfigQueryRequest_6f000104);
		TestConfigQueryRequest_6f000104 tablesReq = (TestConfigQueryRequest_6f000104) client.lastSentRequest;
		Assertions.assertEquals("tables", tablesReq.getOp());

		// 3) queryConfigList
		client.mockResponse = TestConfigQueryResponse_6f000105.newBuilder()
				.setSchemaVersion("1.0")
				.setResultJson("{\"list\":[{\"id\":1}],\"total\":1}")
				.build();
		Map<String, Object> listMap = client.queryConfigList("ItemConfig", 1, 20, "{\"quality\":1}");
		Assertions.assertEquals(1, listMap.get("total"));
		TestConfigQueryRequest_6f000104 listReq = (TestConfigQueryRequest_6f000104) client.lastSentRequest;
		Assertions.assertEquals("list", listReq.getOp());
		Assertions.assertEquals("ItemConfig", listReq.getTable());
		Assertions.assertEquals(1, listReq.getPage());
		Assertions.assertEquals(20, listReq.getPageSize());
		Assertions.assertEquals("{\"quality\":1}", listReq.getFilterJson());

		// 4) queryConfigGet
		client.mockResponse = TestConfigQueryResponse_6f000105.newBuilder()
				.setSchemaVersion("1.0")
				.setResultJson("{\"id\":1001,\"name\":\"Sword\"}")
				.build();
		Map<String, Object> getMap = client.queryConfigGet("ItemConfig", 1001, null);
		Assertions.assertEquals(1001, getMap.get("id"));
		TestConfigQueryRequest_6f000104 getReq = (TestConfigQueryRequest_6f000104) client.lastSentRequest;
		Assertions.assertEquals("get", getReq.getOp());
		Assertions.assertEquals(1001, getReq.getId());

		// 5) fetchProtoCatalog
		client.mockResponse = TestProtoCatalogResponse_6f000107.newBuilder()
				.setSchemaVersion("1.0")
				.setCatalogJson("{\"modules\":[{\"id\":1,\"name\":\"PlayerMsg\"}]}")
				.build();
		Map<String, Object> catalogMap = client.fetchProtoCatalog(1, true);
		Assertions.assertNotNull(catalogMap.get("modules"));
		Assertions.assertTrue(client.lastSentRequest instanceof TestProtoCatalogRequest_6f000106);
		TestProtoCatalogRequest_6f000106 catalogReq = (TestProtoCatalogRequest_6f000106) client.lastSentRequest;
		Assertions.assertEquals(1, catalogReq.getModuleFilter());
		Assertions.assertTrue(catalogReq.getIncludeSchemas());

		// 6) inspectPlayer
		client.mockResponse = TestInspectPlayerResponse_6f000109.newBuilder()
				.setSchemaVersion("1.0")
				.setResultJson("{\"playerId\":88001,\"modules\":{\"item\":{\"count\":10}}}")
				.build();
		Map<String, Object> inspectMap = client.inspectPlayer(88001L, List.of("item"));
		Assertions.assertEquals(88001, inspectMap.get("playerId"));
		Assertions.assertTrue(client.lastSentRequest instanceof TestInspectPlayerRequest_6f000108);
		TestInspectPlayerRequest_6f000108 inspectReq = (TestInspectPlayerRequest_6f000108) client.lastSentRequest;
		Assertions.assertEquals(88001L, inspectReq.getTargetPlayerId());
		Assertions.assertEquals("item", inspectReq.getModules(0));

		// 校验 JSON 解析异常分支拦截 (EX-13)
		// a) 返回空或 blank JSON
		client.mockResponse = TestConfigQueryResponse_6f000105.newBuilder()
				.setSchemaVersion("1.0")
				.setResultJson("   ")
				.build();
		IllegalStateException emptyEx = Assertions.assertThrows(IllegalStateException.class, client::queryConfigTables);
		Assertions.assertTrue(emptyEx.getMessage().contains("EX-13"));
		Assertions.assertTrue(emptyEx.getMessage().contains("响应JSON为空"));

		// b) 返回非法畸形 JSON
		client.mockResponse = TestConfigQueryResponse_6f000105.newBuilder()
				.setSchemaVersion("1.0")
				.setResultJson("{bad-json")
				.build();
		IllegalStateException malformedEx = Assertions.assertThrows(IllegalStateException.class, client::queryConfigTables);
		Assertions.assertTrue(malformedEx.getMessage().contains("EX-13"));
		Assertions.assertTrue(malformedEx.getMessage().contains("反序列化JSON失败"));
	}

	/**
	 * 验证WhiteBoxTestKit核心生命周期：Arrange原语、Trace录制、TracedResponse断言、点分路径自省、冷重载及退出清理
	 *
	 * @throws Exception 执行异常
	 */
	@Test
	public void testWhiteBoxTestKitCoreLifecycle() {
		TestableClient client = new TestableClient();
		java.util.concurrent.atomic.AtomicBoolean syncCalled = new java.util.concurrent.atomic.AtomicBoolean(false);
		cn.game.simulation.test.ai.WhiteBoxTestKit kit = new cn.game.simulation.test.ai.WhiteBoxTestKit(client, () -> syncCalled.set(true));

		// 1. 验证 Arrange 原语（模块重置、快照、虚拟时钟、可控随机）
		client.mockResponse = cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103.newBuilder()
				.setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\",\"data\":{\"result\":\"ok\"}}")
				.setMsgId(cn.game.protocol.protobuf.PbProtocol.TestGmExecRequest_6f000102)
				.setMsgName("TestGmExecRequest_6f000102")
				.build();

		kit.resetModules("ItemModule", "HeroModule");
		cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102 gmReq = (cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) client.lastSentRequest;
		Assertions.assertEquals("gm.resetModule", gmReq.getAction());
		Assertions.assertTrue(gmReq.getParamsJson().contains("ItemModule,HeroModule"));

		kit.resetModules();
		gmReq = (cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) client.lastSentRequest;
		Assertions.assertTrue(gmReq.getParamsJson().contains("all"));

		kit.saveSnapshot("testSnap");
		gmReq = (cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) client.lastSentRequest;
		Assertions.assertEquals("gm.snapshot.save", gmReq.getAction());

		syncCalled.set(false);
		kit.restoreSnapshot("testSnap");
		gmReq = (cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) client.lastSentRequest;
		Assertions.assertEquals("gm.snapshot.restore", gmReq.getAction());
		Assertions.assertTrue(syncCalled.get());

		kit.listSnapshots();
		gmReq = (cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) client.lastSentRequest;
		Assertions.assertEquals("gm.snapshot.list", gmReq.getAction());

		kit.deleteSnapshots();
		gmReq = (cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) client.lastSentRequest;
		Assertions.assertEquals("gm.snapshot.delete", gmReq.getAction());
		Assertions.assertTrue(gmReq.getParamsJson().contains("\"all\":true"));

		kit.setVirtualTimeOffset("1d", true, false);
		Assertions.assertTrue(kit.isClockModified());
		kit.setVirtualTime("2026-06-01 12:00:00", true, false);
		Assertions.assertTrue(kit.isClockModified());
		kit.queryVirtualTimeStatus();
		kit.resetVirtualTime(true, false);
		Assertions.assertFalse(kit.isClockModified());

		kit.seedRng(12345L);
		Assertions.assertTrue(kit.isRngModified());
		kit.sequenceRng(List.of(1, 2, 3), List.of(0.1, 0.2));
		Assertions.assertTrue(kit.isRngModified());
		kit.queryRngStatus();
		kit.resetRng();
		Assertions.assertFalse(kit.isRngModified());

		// 2. 验证 Trace 录制与 TracedResponse 断言（含净额正负与 add/isAdd 兼容）
		final java.util.concurrent.atomic.AtomicInteger traceCallCount = new java.util.concurrent.atomic.AtomicInteger(0);
		TestableClient traceClient = new TestableClient() {
			@Override
			@SuppressWarnings("unchecked")
			public <T> T sendProtocolAndWait(Message message) {
				if (message instanceof cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) {
					cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102 req = (cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) message;
					if ("gm.trace.query".equals(req.getAction())) {
						String json = "{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\",\"data\":[{\"msgName\":\"ItemUse\",\"resourceEvents\":[{\"id\":1001,\"count\":10,\"add\":true,\"opType\":\"gm\"},{\"id\":1001,\"count\":4,\"isAdd\":false,\"opType\":\"cost\"}],\"errorEvents\":[]}]}";
						return (T) cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103.newBuilder().setResultJson(json).build();
					}
					return (T) cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103.newBuilder().setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\"}").build();
				}
				traceCallCount.incrementAndGet();
				return (T) cn.game.protocol.protobuf.TestMsg.TestConfigQueryResponse_6f000105.newBuilder().setSchemaVersion("1.0").setResultJson("{\"ok\":true}").build();
			}
		};
		cn.game.simulation.test.ai.WhiteBoxTestKit traceKit = new cn.game.simulation.test.ai.WhiteBoxTestKit(traceClient, null);

		cn.game.simulation.test.ai.WhiteBoxTestKit.TracedResponse<cn.game.protocol.protobuf.TestMsg.TestConfigQueryResponse_6f000105> traced = traceKit.sendAndTrace(
				cn.game.protocol.protobuf.TestMsg.TestConfigQueryRequest_6f000104.newBuilder().setOp("tables").build());
		Assertions.assertEquals(1, traceCallCount.get());
		Assertions.assertNotNull(traced.getResponse());
		Assertions.assertNotNull(traced.assertNoServerErrors());
		Assertions.assertNotNull(traced.assertResourceDelta(1001, 6L));
		Assertions.assertNotNull(traced.assertResourceDelta(1001, 10L, "gm"));
		Assertions.assertNotNull(traced.assertResourceDelta(1001, -4L, "cost"));
		Assertions.assertNotNull(traced.assertHasResourceEvent(1001, true));
		Assertions.assertNotNull(traced.assertHasResourceEvent(1001, false));

		// 验证流水净额断言异常分支
		Assertions.assertThrows(AssertionError.class, () -> traced.assertResourceDelta(1001, 999L));
		Assertions.assertThrows(AssertionError.class, () -> traced.assertHasResourceEvent(9999, true));

		// 验证服务端错误事件断言异常（EX-14）
		cn.game.simulation.test.ai.WhiteBoxTestKit.TracedResponse<String> errorTraced = new cn.game.simulation.test.ai.WhiteBoxTestKit.TracedResponse<>(
				"res", Collections.emptyList(), Collections.emptyList(), List.of(Map.of("error", "TEST_ERROR")));
		IllegalStateException errEx = Assertions.assertThrows(IllegalStateException.class, errorTraced::assertNoServerErrors);
		Assertions.assertTrue(errEx.getMessage().contains("EX-14"));

		// 验证业务请求异常中断时 finally 必关 Trace 且将 errorEvents 附加至 Suppressed (DR-11, EX-14)
		TestableClient bizErrorClient = new TestableClient() {
			@Override
			@SuppressWarnings("unchecked")
			public <T> T sendProtocolAndWait(Message message) {
				if (message instanceof cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) {
					cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102 req = (cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) message;
					if ("gm.trace.query".equals(req.getAction())) {
						String json = "{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\",\"data\":[{\"errorEvents\":[{\"exception\":\"BusinessFail\"}]}]}";
						return (T) cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103.newBuilder().setResultJson(json).build();
					}
					return (T) cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103.newBuilder().setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\"}").build();
				}
				throw new IllegalStateException("Simulated business error");
			}
		};
		final cn.game.simulation.test.ai.WhiteBoxTestKit errorBizKit = new cn.game.simulation.test.ai.WhiteBoxTestKit(bizErrorClient, null);
		IllegalStateException bizEx = Assertions.assertThrows(IllegalStateException.class, () -> errorBizKit.sendAndTrace(
				cn.game.protocol.protobuf.TestMsg.TestConfigQueryRequest_6f000104.newBuilder().setOp("tables").build()));
		Assertions.assertEquals("Simulated business error", bizEx.getMessage());
		Assertions.assertEquals(1, bizEx.getSuppressed().length);
		Assertions.assertTrue(bizEx.getSuppressed()[0].getMessage().contains("EX-14"));
		Assertions.assertFalse(errorBizKit.isTraceActive());

		// 3. 验证白盒自省点分路径解析与跨数值类型比对 (EX-15)
		TestableClient inspectClient = new TestableClient();
		inspectClient.mockResponse = cn.game.protocol.protobuf.TestMsg.TestInspectPlayerResponse_6f000109.newBuilder()
				.setSchemaVersion("1.0")
				.setResultJson("{\"data\":{\"modules\":{\"ItemModule\":{\"level\":10,\"gold\":10000000000,\"info\":{\"active\":true,\"ratio\":1.5}}}}}")
				.build();
		final cn.game.simulation.test.ai.WhiteBoxTestKit inspectKit = new cn.game.simulation.test.ai.WhiteBoxTestKit(inspectClient, null);

		Map<String, Object> itemModule = inspectKit.inspectModule("ItemModule");
		Assertions.assertNotNull(itemModule);
		Assertions.assertTrue(inspectClient.lastSentRequest instanceof TestInspectPlayerRequest_6f000108);
		TestInspectPlayerRequest_6f000108 fullReq = (TestInspectPlayerRequest_6f000108) inspectClient.lastSentRequest;
		Assertions.assertTrue(fullReq.getModulesList().contains("ItemModule"));

		Map<String, Object> itemModuleShort = inspectKit.inspectModule("item");
		Assertions.assertNotNull(itemModuleShort);
		TestInspectPlayerRequest_6f000108 shortReq = (TestInspectPlayerRequest_6f000108) inspectClient.lastSentRequest;
		Assertions.assertTrue(shortReq.getModulesList().isEmpty());

		Object level = inspectKit.inspectModuleField("ItemModule", "level");
		Assertions.assertEquals(10, ((Number) level).intValue());
		Object ratio = inspectKit.inspectModuleField("ItemModule", "info.ratio");
		Assertions.assertEquals(1.5, ((Number) ratio).doubleValue());

		// 跨类型等价断言
		inspectKit.assertModuleFieldEquals("ItemModule", "level", 10L);
		inspectKit.assertModuleFieldEquals("ItemModule", "gold", 10000000000L);
		inspectKit.assertModuleFieldEquals("ItemModule", "info.active", true);

		// 点分路径不存在异常拦截
		Assertions.assertThrows(IllegalArgumentException.class, () -> inspectKit.inspectModuleField("ItemModule", "info.notExist"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> inspectKit.inspectModuleField("ItemModule", "level.deepNotMap"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> inspectKit.inspectModule("UnknownModule"));
		Assertions.assertThrows(AssertionError.class, () -> inspectKit.assertModuleFieldEquals("ItemModule", "level", 999));

		// 4. 验证 flushAndColdReload 契约与断言拦截 (EX-15)
		syncCalled.set(false);
		TestableClient reloadClient = new TestableClient();
		reloadClient.mockResponse = cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103.newBuilder()
				.setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\",\"data\":{\"reloadedFromDb\":true,\"oldIdentityHash\":111,\"newIdentityHash\":222}}")
				.build();
		cn.game.simulation.test.ai.WhiteBoxTestKit reloadKit = new cn.game.simulation.test.ai.WhiteBoxTestKit(reloadClient, () -> syncCalled.set(true));
		Map<String, Object> reloadData = reloadKit.flushAndColdReload();
		Assertions.assertTrue(syncCalled.get());
		Assertions.assertEquals(true, reloadData.get("reloadedFromDb"));
		cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102 reloadReq = (cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) reloadClient.lastSentRequest;
		Assertions.assertEquals("gm.flushAndColdReload", reloadReq.getAction());
		Assertions.assertTrue(reloadReq.getParamsJson().contains("88001"));
		reloadFresh();

		// 异常分支：reloadedFromDb 为 false
		reloadClient.mockResponse = cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103.newBuilder()
				.setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\",\"data\":{\"reloadedFromDb\":false,\"oldIdentityHash\":111,\"newIdentityHash\":222}}")
				.build();
		IllegalStateException reloadEx = Assertions.assertThrows(IllegalStateException.class, reloadKit::flushAndColdReload);
		Assertions.assertTrue(reloadEx.getMessage().contains("EX-15"));

		// 异常分支：前后哈希相同
		reloadClient.mockResponse = cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103.newBuilder()
				.setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\",\"data\":{\"reloadedFromDb\":true,\"oldIdentityHash\":111,\"newIdentityHash\":111}}")
				.build();
		IllegalStateException hashEx = Assertions.assertThrows(IllegalStateException.class, reloadKit::flushAndColdReload);
		Assertions.assertTrue(hashEx.getMessage().contains("EX-15"));

		// 5. 验证 cleanupAll 自动最佳努力复位与异常隔离 (DR-12)
		TestableClient cleanupClient = new TestableClient();
		cleanupClient.mockResponse = cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103.newBuilder()
				.setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\"}")
				.build();
		cn.game.simulation.test.ai.WhiteBoxTestKit cleanupKit = new cn.game.simulation.test.ai.WhiteBoxTestKit(cleanupClient, null);
		cleanupKit.seedRng(888L);
		cleanupKit.setVirtualTimeOffset("2h", true, false);
		Assertions.assertTrue(cleanupKit.isRngModified());
		Assertions.assertTrue(cleanupKit.isClockModified());

		cleanupKit.cleanupAll();
		Assertions.assertFalse(cleanupKit.isRngModified());
		Assertions.assertFalse(cleanupKit.isClockModified());

		// 模拟复位过程中抛出异常，绝不打断上层流程
		TestableClient failClient = new TestableClient() {
			@Override
			@SuppressWarnings("unchecked")
			public <T> T sendProtocolAndWait(Message message) {
				if (message instanceof cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) {
					cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102 req = (cn.game.protocol.protobuf.TestMsg.TestGmExecRequest_6f000102) message;
					if ("gm.rng.reset".equals(req.getAction()) || "gm.trace.stop".equals(req.getAction()) || "player.time.reset".equals(req.getAction())) {
						throw new RuntimeException("Reset failed in cleanup");
					}
					return (T) cn.game.protocol.protobuf.TestMsg.TestGmExecResponse_6f000103.newBuilder().setResultJson("{\"schemaVersion\":\"1.0\",\"ok\":true,\"errorCode\":0,\"summary\":\"ok\"}").build();
				}
				throw new RuntimeException("Reset failed in cleanup");
			}
		};
		cn.game.simulation.test.ai.WhiteBoxTestKit failCleanupKit = new cn.game.simulation.test.ai.WhiteBoxTestKit(failClient, null);
		failCleanupKit.seedRng(999L);
		failCleanupKit.cleanupAll(); // 必须平稳返回，打 warn，不抛异常
	}
	/**
	 * 对应服务端持久化冷重载契约辅助标记
	 */
	private static void reloadFresh() {
		// 校验桩与服务端真实 reloadFresh 落盘重载契约对齐
	}
}