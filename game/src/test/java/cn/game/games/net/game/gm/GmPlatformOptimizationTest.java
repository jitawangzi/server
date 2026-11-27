package cn.game.games.net.game.gm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.process.AbstractProcessor;
import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.gm.command.ActivityGm;
import cn.game.games.net.game.gm.proto.ProtocolSimulationService;
import cn.game.games.net.game.gm.proto.ProtocolSimulationService.ProtoInvokeRequest;
import cn.game.games.net.game.gm.proto.VirtualGameClient;
import cn.game.games.net.game.gm.web.ConfigHealthService;
import cn.game.games.net.game.gm.web.CrossClusterDiagnosticsService;
import cn.game.games.net.game.gm.web.OpsPlayerQueryService;
import cn.game.games.net.game.gm.web.ServerDiagnosticsService;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.protocol.protobuf.PlayerMsg.PlayerNameResponse_01000012;
import cn.game.util.ObjUtil;
import io.vertx.core.Vertx;

/**
 * GM 与测试平台全链路优化单元测试：覆盖 RBAC 权限矩阵、JSONL 审计日志、并发防刷模拟、跨快照 Diff、活动生命周期调试及运维诊断服务
 *
 * @author shuyongqiang
 */
@Isolated
public class GmPlatformOptimizationTest {

	private static final Logger log = LoggerFactory.getLogger(GmPlatformOptimizationTest.class);
	private static final long TEST_PLAYER_ID = 900_000_099_123L;

	private static Vertx previousVertx;
	private static Vertx createdVertx;

	private Player testPlayer;

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

	@BeforeAll
	public static void beforeAll() {
		ClassManager.getInstance().init();
		previousVertx = VxHolder.vertx;
		if (VxHolder.vertx == null) {
			createdVertx = Vertx.vertx();
			VxHolder.vertx = createdVertx;
		}
		if (ServerContext.getInstance().getEventBus() == null) {
			ServerContext.getInstance().setEventBus(ServerEventBus.getInstance());
		}
		try {
			ManagerHelper.init();
		} catch (Throwable t) {
			log.warn("测试环境加载部分配表告警（非阻断）", t);
		}
	}

	@AfterAll
	public static void afterAll() {
		VxHolder.vertx = previousVertx;
		if (createdVertx != null) {
			try {
				createdVertx.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.warn("关闭测试 Vertx 告警", e);
			}
			createdVertx = null;
		}
	}

	@BeforeEach
	public void setUp() {
		System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
		ServerContext.getInstance().setRunMode();
		System.clearProperty(GmSecurityPolicy.PROP_RBAC_TOKENS);
		System.clearProperty(GmSecurityPolicy.PROP_OPS_TOKEN);
		System.clearProperty(GmSecurityPolicy.PROP_REQUIRE_AUTH);
		System.clearProperty(GmAuditLogService.PROP_AUDIT_FILE);
		GmAuditLogService.getInstance().clearMemoryForTest();

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(TEST_PLAYER_ID);
		data.setUid(TEST_PLAYER_ID);
		data.setAccountId("acc_opt_tester");
		data.setName("OptTester");
		data.setServerId("game_test_opt");
		data.setLevel(12);

		testPlayer = new Player(data);
		testPlayer.setOnline(true);
		testPlayer.setIslogouting(false);
		testPlayer.setGameClient(new VirtualGameClient(TEST_PLAYER_ID));
		testPlayer.initModule(new HashMap<String, BasePlayerModule>());
		PlayerManager.getInstance().initAdd(testPlayer);
	}

	@AfterEach
	public void tearDown() {
		GameClient.endThreadLocalCapture();
		if (RequestTraceService.getInstance().getCurrentBuilder() != null) {
			RequestTraceService.getInstance().endForcedTrace(RequestTraceService.getInstance().getCurrentBuilder(), 0);
		}
		if (testPlayer != null) {
			testPlayer.cancelAllTimer();
			try {
				PlayerManager.getInstance().deletePlayer(testPlayer.getPlayerId());
			} catch (Throwable t) {
				log.debug("清理测试玩家告警 playerId={}", testPlayer.getPlayerId(), t);
			}
		}
		System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
		ServerContext.getInstance().setRunMode();
		PlayerSnapshotService.getInstance().delete(TEST_PLAYER_ID, null, true);
		System.clearProperty(GmSecurityPolicy.PROP_RBAC_TOKENS);
		System.clearProperty(GmSecurityPolicy.PROP_OPS_TOKEN);
		System.clearProperty(GmSecurityPolicy.PROP_REQUIRE_AUTH);
		System.clearProperty(GmAuditLogService.PROP_AUDIT_FILE);
		GmAuditLogService.getInstance().clearMemoryForTest();
	}

	/**
	 * 验证多账号 RBAC 令牌认证、分级动作权限校验及 JSONL 操作审计落盘筛选
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testRbacAuthenticationAndAuditPersistence(@TempDir Path tempDir) throws Exception {
		Path auditFile = tempDir.resolve("gm-audit-unit.jsonl");
		System.setProperty(GmAuditLogService.PROP_AUDIT_FILE, auditFile.toString());
		System.setProperty(GmSecurityPolicy.PROP_RBAC_TOKENS,
				"alice:READONLY:tok_ro, bob:OPERATOR:tok_op, carol:REPAIR:tok_rep, dave:ADMIN:tok_adm");
		System.setProperty(GmSecurityPolicy.PROP_OPS_TOKEN, "legacy_master_tok");

		GmAuthPrincipal ro = GmSecurityPolicy.authenticateToken("tok_ro", "127.0.0.1");
		GmAuthPrincipal op = GmSecurityPolicy.authenticateToken("tok_op", "127.0.0.1");
		GmAuthPrincipal rep = GmSecurityPolicy.authenticateToken("tok_rep", "127.0.0.1");
		GmAuthPrincipal adm = GmSecurityPolicy.authenticateToken("tok_adm", "127.0.0.1");
		GmAuthPrincipal legacy = GmSecurityPolicy.authenticateToken("legacy_master_tok", "127.0.0.1");

		Assertions.assertNotNull(ro);
		Assertions.assertEquals(GmRole.READONLY, ro.getRole());
		Assertions.assertEquals("alice", ro.getOperator());
		Assertions.assertNotNull(op);
		Assertions.assertEquals(GmRole.OPERATOR, op.getRole());
		Assertions.assertNotNull(rep);
		Assertions.assertEquals(GmRole.REPAIR, rep.getRole());
		Assertions.assertNotNull(adm);
		Assertions.assertEquals(GmRole.ADMIN, adm.getRole());
		Assertions.assertNotNull(legacy);
		Assertions.assertEquals(GmRole.ADMIN, legacy.getRole());
		Assertions.assertNull(GmSecurityPolicy.authenticateToken("wrong_token", "127.0.0.1"));

		Assertions.assertTrue(GmSecurityPolicy.isRoleAllowedForAction(GmRole.READONLY, GmSecurityTier.OPS_SAFE, "gm.inspect"));
		Assertions.assertFalse(GmSecurityPolicy.isRoleAllowedForAction(GmRole.READONLY, GmSecurityTier.OPS_SAFE, "gm.flushAndColdReload"));
		Assertions.assertTrue(GmSecurityPolicy.isRoleAllowedForAction(GmRole.READONLY, GmSecurityTier.OPS_SAFE, "custom.readAction", true),
				"@GmAction(readOnly = true) 应当显式授权 READONLY 角色");
		Assertions.assertFalse(GmSecurityPolicy.isRoleAllowedForAction(GmRole.READONLY, GmSecurityTier.DEV_TEST_ONLY, "mail.send"));
		Assertions.assertTrue(GmSecurityPolicy.isRoleAllowedForAction(GmRole.OPERATOR, GmSecurityTier.OPS_SAFE, "gm.flushAndColdReload"));
		Assertions.assertFalse(GmSecurityPolicy.isRoleAllowedForAction(GmRole.OPERATOR, GmSecurityTier.OPS_REPAIR, "ops.repair"));
		Assertions.assertTrue(GmSecurityPolicy.isRoleAllowedForAction(GmRole.REPAIR, GmSecurityTier.OPS_REPAIR, "ops.repair"));
		Assertions.assertFalse(GmSecurityPolicy.isRoleAllowedForAction(GmRole.REPAIR, GmSecurityTier.DEV_TEST_ONLY, "player.super"),
				"REPAIR 角色仅允许 OPS_SAFE + OPS_REPAIR，严禁越权执行 DEV_TEST_ONLY");
		Assertions.assertTrue(GmSecurityPolicy.isRoleAllowedForAction(GmRole.ADMIN, GmSecurityTier.DEV_TEST_ONLY, "player.super"));

		GmAuditLogService audit = GmAuditLogService.getInstance();
		audit.record(op, "/gm-console/api/execute", "item.set", TEST_PLAYER_ID, false, Map.of("id", 100001), true, 0, "ok", 1, 15L);
		audit.record(ro, "/gm-console/api/execute", "mail.send", TEST_PLAYER_ID, false, Map.of(), false, ErrorMsgEnum.gm_not.getId(), "denied", 0, 1L);

		Assertions.assertTrue(Files.exists(auditFile), "审计 JSONL 文件应当已持久化写入");
		List<String> lines = Files.readAllLines(auditFile);
		Assertions.assertEquals(2, lines.size());

		Map<String, Object> failedQuery = audit.query(null, null, TEST_PLAYER_ID, true, 10);
		List<Map<String, Object>> failedRows = (List<Map<String, Object>>) failedQuery.get("records");
		Assertions.assertEquals(1, failedRows.size());
		Assertions.assertEquals("alice", failedRows.get(0).get("operator"));
		Assertions.assertEquals("mail.send", failedRows.get(0).get("action"));
	}

	/**
	 * 验证并发突发连发模拟测试对多成功漏洞的告警以及对一次性锁拦截场景的错误码统计
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testConcurrentBurstProtocolAntiDupeAndAggregateDiff() {
		int reqMsgId = PbProtocol.getInstance().getMsgId("PlayerNameRequest_01000011");

		// 1. 模拟存在重复成功漏洞（无并发/限购拦截），应触发 warnings 防刷警告
		Dispatcher alwaysOkDispatcher = new TestDispatcher((client, protocol) -> {
			testPlayer.getData().setLevel(testPlayer.getData().getLevel() + 1);
			client.sendProtocol(PlayerNameResponse_01000012.getDefaultInstance());
		});
		ProtocolSimulationService leakySvc = new ProtocolSimulationService(new SyncProcessor(), null, null, alwaysOkDispatcher);

		ProtoInvokeRequest req = new ProtoInvokeRequest();
		req.setPlayerId(TEST_PLAYER_ID);
		req.setMsgId(reqMsgId);
		req.setPayload(Map.of("name", "BurstTest"));

		Map<String, Object> leakyRes = leakySvc.invokeConcurrent(req, 4);
		Assertions.assertEquals(Boolean.TRUE, leakyRes.get("ok"));
		Assertions.assertEquals(4, leakyRes.get("successCount"));
		Assertions.assertEquals(0, leakyRes.get("failCount"));
		List<String> leakyWarnings = (List<String>) leakyRes.get("warnings");
		Assertions.assertFalse(leakyWarnings.isEmpty(), "多次并发成功必须触发防刷提示警告");

		// 2. 模拟严格一次性锁：仅 1 次成功，其余 4 次均返回 repeat_request 错误
		AtomicBoolean claimed = new AtomicBoolean(false);
		AtomicInteger totalCalls = new AtomicInteger(0);
		Dispatcher guardedDispatcher = new TestDispatcher((client, protocol) -> {
			totalCalls.incrementAndGet();
			if (claimed.compareAndSet(false, true)) {
				testPlayer.getData().setName("ClaimedOnce");
				client.sendProtocol(PlayerNameResponse_01000012.getDefaultInstance());
			} else {
				PlayerErrorPush_01000099 errPush = PlayerErrorPush_01000099.newBuilder()
						.setError(ErrorMsgEnum.repeat_request.getDesc())
						.build();
				client.sendProtocol(errPush, ErrorMsgEnum.repeat_request.getId());
			}
		});
		ProtocolSimulationService guardedSvc = new ProtocolSimulationService(new SyncProcessor(), null, null, guardedDispatcher);

		Map<String, Object> guardedRes = guardedSvc.invokeConcurrent(req, 5);
		Assertions.assertEquals(5, totalCalls.get());
		Assertions.assertEquals(1, guardedRes.get("successCount"));
		Assertions.assertEquals(4, guardedRes.get("failCount"));
		Map<String, Integer> errCounts = (Map<String, Integer>) guardedRes.get("errorCodeCounts");
		Assertions.assertEquals(4, errCounts.get(String.valueOf(ErrorMsgEnum.repeat_request.getId())));
		List<String> guardedWarnings = (List<String>) guardedRes.get("warnings");
		Assertions.assertTrue(guardedWarnings.isEmpty(), "仅 1 次成功不应产出防刷警告");
	}

	/**
	 * 验证跨步骤内存快照 Diff 对比及 GmDispatcher 元动作暴露
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testSnapshotDiffAndMetaActions() {
		PlayerSnapshotService snapSvc = PlayerSnapshotService.getInstance();
		snapSvc.save(testPlayer, "snap_a");

		testPlayer.getData().setLevel(88);
		testPlayer.getData().setFightPower(99999);
		snapSvc.save(testPlayer, "snap_b");

		Map<String, Object> diffVsSnapB = snapSvc.diffSnapshots(testPlayer, "snap_a", "snap_b", "_scalars");
		Assertions.assertEquals("snap_a", diffVsSnapB.get("baseSnapshot"));
		Assertions.assertEquals("snap_b", diffVsSnapB.get("targetSnapshot"));
		Assertions.assertTrue(((Number) diffVsSnapB.get("diffCount")).intValue() >= 2);

		List<Map<String, Object>> diffs = (List<Map<String, Object>>) diffVsSnapB.get("diffs");
		boolean foundLevel = diffs.stream().anyMatch(d -> "_scalars.level".equals(d.get("path")) && d.get("after") instanceof Number n && n.intValue() == 88);
		boolean foundFight = diffs.stream().anyMatch(d -> "_scalars.fightPower".equals(d.get("path")) && d.get("after") instanceof Number n && n.intValue() == 99999);
		Assertions.assertTrue(foundLevel && foundFight, "快照 Diff 必须准确包含等级与战力字段变更: " + diffs);

		// 验证通过 CURRENT 对比实时状态
		testPlayer.getData().setVipLevel(9);
		Map<String, Object> diffVsCurrent = snapSvc.diffSnapshots(testPlayer, "snap_a", "__CURRENT__", "_scalars");
		List<Map<String, Object>> currDiffs = (List<Map<String, Object>>) diffVsCurrent.get("diffs");
		boolean foundVip = currDiffs.stream().anyMatch(d -> "_scalars.vipLevel".equals(d.get("path")) && Integer.valueOf(9).equals(d.get("after")));
		Assertions.assertTrue(foundVip, "与 __CURRENT__ 对比必须实时捕捉当前内存中最新字段变动");

		// 验证 GmDispatcher 元动作注册完整 & dryRun 强拦截不支持演练的指令
		GmDispatcher dispatcher = GmDispatcher.getInstance();
		Assertions.assertTrue(dispatcher.hasCommand("gm.snapshot.diff"));
		Assertions.assertTrue(dispatcher.hasCommand("gm.audit.list"));

		if (!dispatcher.hasCommand("player.setLevel")) {
			dispatcher.registerBean(new cn.game.games.net.game.gm.command.PlayerGm());
		}
		int beforeLv = testPlayer.getData().getLevel();
		GmResult dryReject = dispatcher.dispatchAction("player.setLevel", TEST_PLAYER_ID, Map.of("level", 99), null, true);
		Assertions.assertFalse(dryReject.isOk(), "不支持 dryRun 的指令传入 dryRun=true 必须拒绝执行");
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), dryReject.getErrorCode());
		Assertions.assertEquals(beforeLv, testPlayer.getData().getLevel(), "dryRun 拦截后玩家状态绝不能被修改");

		// 验证 FeatureWorkbenchService 通用动作收录 gm.inspect
		if (!dispatcher.hasCommand("gm.inspect")) {
			dispatcher.registerBean(new PlayerInspectService());
		}
		Map<String, Object> workbenchCatalog = new cn.game.games.net.game.gm.web.FeatureWorkbenchService().buildCatalog("");
		List<Map<String, Object>> commonActions = (List<Map<String, Object>>) workbenchCatalog.get("commonActions");
		boolean hasGmInspect = commonActions != null
				&& commonActions.stream().anyMatch(a -> "gm.inspect".equals(a.get("id")));
		Assertions.assertTrue(hasGmInspect, "FeatureWorkbenchService commonActions 必须收录 gm.inspect");

		// 验证 OpsRepairService 安全参数解析与认证操作人透传
		cn.game.games.net.game.gm.repair.OpsRepairService repairSvc = cn.game.games.net.game.gm.repair.OpsRepairService.getInstance();
		cn.game.games.net.game.gm.repair.OpsRepairService.setAuthenticatedOperator("http_alice");
		try {
			GmResult badParamRes = repairSvc.execute("tdlv", TEST_PLAYER_ID, "{\"level\":\"abc\"}", "bad param test", true, "spoofed_user");
			Assertions.assertFalse(badParamRes.isOk(), "非数字字符串参数应返回可控业务错误而非 ClassCastException");
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.getId(), badParamRes.getErrorCode());

			GmResult okRepairRes = repairSvc.execute("recalcPower", TEST_PLAYER_ID, "{}", "valid dryRun test", true, "spoofed_user");
			Assertions.assertTrue(okRepairRes.isOk());
			List<Map<String, Object>> repairLogs = repairSvc.auditLog(0, 10);
			Assertions.assertFalse(repairLogs.isEmpty());
			Assertions.assertEquals("http_alice", repairLogs.get(0).get("operator"), "修数审计操作人应优先取 HTTP 认证身份");
		} finally {
			cn.game.games.net.game.gm.repair.OpsRepairService.clearAuthenticatedOperator();
		}
	}

	/**
	 * 验证活动生命周期调试 GM 指令与周期事件分发
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testActivityGmLifecycleAndPeriodicEvents() {
		ActivityGm activityGm = new ActivityGm();
		Map<String, Object> statusMap = activityGm.status(TEST_PLAYER_ID, "", false);
		Assertions.assertNotNull(statusMap.get("nowStr"), "activity.status 必须返回当前服务端时间 nowStr");
		List<Map<String, Object>> rows = (List<Map<String, Object>>) statusMap.get("activities");
		Assertions.assertNotNull(rows);

		if (ActivityManager.instance() != null && !ActivityManager.instance().list().isEmpty()) {
			ActivityConfig target = null;
			for (ActivityConfig cfg : ActivityManager.instance().list()) {
				if (cfg != null && !cfg.disable) {
					target = cfg;
					break;
				}
			}
			if (target != null && testPlayer.getActivityModule() != null) {
				Map<String, Object> openRes = activityGm.open(testPlayer, target.ID, true);
				Assertions.assertEquals(target.ID, openRes.get("activityId"));
				Map<String, Object> endRes = activityGm.end(testPlayer, target.ID);
				Assertions.assertEquals(target.ID, endRes.get("activityId"));
				Map<String, Object> destroyRes = activityGm.destroy(testPlayer, target.ID);
				Assertions.assertEquals(target.ID, destroyRes.get("activityId"));
			}
		}

		Map<String, Object> newDayRes = activityGm.triggerEvent("NewDay", 0);
		Assertions.assertEquals("NewDay", newDayRes.get("eventType"));
		Assertions.assertEquals(Boolean.TRUE, newDayRes.get("dispatched"));

		Assertions.assertThrows(LogicException.class, () -> activityGm.triggerEvent("ActivityOpenTime", 0),
				"触发活动开启生命周期事件但未传 activityId 必须拦截");
	}

	/**
	 * 验证运维级服务：ServerDiagnosticsService、OpsPlayerQueryService、ConfigHealthService 与 CrossClusterDiagnosticsService
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testOpsDiagnosticsServicesAndGracefulDegradation() {
		// 1. 运行健康快照（包括 PRODUCTION 模式脱敏Trace状态）
		ServerDiagnosticsService healthSvc = new ServerDiagnosticsService();
		Map<String, Object> devHealth = healthSvc.buildHealthSnapshot();
		Assertions.assertEquals("DEV", devHealth.get("runMode"));
		Map<String, Object> playersStat = (Map<String, Object>) devHealth.get("players");
		Assertions.assertTrue(((Number) playersStat.get("onlineCount")).intValue() >= 1);

		// 2. 玩家检索、只读详单与离线降级发邮件
		OpsPlayerQueryService playerQuerySvc = new OpsPlayerQueryService();
		Map<String, Object> searchRes = playerQuerySvc.searchPlayers("OptTester", 10);
		List<Map<String, Object>> found = (List<Map<String, Object>>) searchRes.get("players");
		Assertions.assertFalse(found.isEmpty());
		Assertions.assertEquals(TEST_PLAYER_ID, ((Number) found.get(0).get("playerId")).longValue());

		Map<String, Object> detailRes = playerQuerySvc.getPlayerDetail(TEST_PLAYER_ID);
		Assertions.assertEquals(Boolean.TRUE, detailRes.get("found"));
		Assertions.assertEquals(Boolean.TRUE, detailRes.get("consistent"));

		Map<String, Object> mailRes = playerQuerySvc.sendOpsMail(TEST_PLAYER_ID, "Ops补偿", "测试内容", 100001, 50);
		Assertions.assertEquals(Boolean.TRUE, mailRes.get("ok"));
		Map<String, Object> invalidMailRes = playerQuerySvc.sendOpsMail(TEST_PLAYER_ID, "非法附件", "测试", 999999999, 1);
		Assertions.assertEquals(Boolean.FALSE, invalidMailRes.get("ok"), "非法 itemId 附件必须拒绝发送");
		Map<String, Object> offlineMailRes = playerQuerySvc.sendOpsMail(999_888_777L, "离线测试", "内容", 100001, 10);
		Assertions.assertEquals(Boolean.FALSE, offlineMailRes.get("ok"), "无 DB 时离线邮件必须在发包前提前返回明确失败");

		// 3. 配表摘要、完整性校验与版本号同步递增
		ConfigHealthService cfgSvc = new ConfigHealthService();
		Map<String, Object> cfgSummary = cfgSvc.getSummary();
		Assertions.assertTrue(((Number) cfgSummary.get("tableCount")).intValue() >= 0);
		Map<String, Object> valRes = cfgSvc.validateIntegrity();
		Assertions.assertNotNull(valRes.get("checkedTables"));
		long verBefore = cn.game.util.file.WatchServiceManager.getInstance().resourceVersion();
		long verAfter = cn.game.util.file.WatchServiceManager.getInstance().bumpResourceVersion();
		Assertions.assertEquals(verBefore + 1, verAfter);

		// 4. 跨服概览与非生产/生产边界校验
		CrossClusterDiagnosticsService clusterSvc = new CrossClusterDiagnosticsService();
		Map<String, Object> clusterOverview = clusterSvc.getClusterOverview();
		Assertions.assertNotNull(clusterOverview.get("rankTypes"));
		Map<String, Object> rankRes = clusterSvc.executeDevAction("rank.top", Map.of("rankType", 1, "limit", 5));
		Assertions.assertEquals(Boolean.TRUE, rankRes.get("ok"));

		System.setProperty(ServerContext.SERVER_RUN_MODE, "PRODUCTION");
		ServerContext.getInstance().setRunMode();
		Map<String, Object> prodSearch = playerQuerySvc.searchPlayers("", 10);
		Assertions.assertTrue(String.valueOf(prodSearch.get("dbNote")).contains("精确"));
		Map<String, Object> prodHealth = healthSvc.buildHealthSnapshot();
		Map<String, Object> prodTrace = (Map<String, Object>) prodHealth.get("trace");
		Assertions.assertEquals(Boolean.TRUE, prodTrace.get("productionMuted"));
		Assertions.assertThrows(IllegalStateException.class, () -> clusterSvc.executeDevAction("rank.top", Map.of()));
	}
}
