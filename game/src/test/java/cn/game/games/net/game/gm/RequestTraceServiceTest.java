package cn.game.games.net.game.gm;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.gm.command.PlayerGm;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;

/**
 * 非生产请求 Trace 录制器、资源流水与 ERROR 采集多维查询单测 (TC-C11-TRACE-RECORDING)
 * 覆盖条款：BR-12, EX-09, AC-11, DC-13, DR-07, TW-05
 *
 * @author shuyongqiang
 */
@Isolated
@DisplayName("RequestTraceService 非生产请求录制与多维查询测试 (TC-C11-TRACE-RECORDING)")
public class RequestTraceServiceTest {

	private Player player;
	private PlayerGm playerGm;
	private RequestTraceService traceService;

	@BeforeEach
	public void setUp() throws Exception {
		ClassManager.getInstance().init();
		traceService = RequestTraceService.getInstance();
		traceService.clear();
		traceService.stop();

		playerGm = new PlayerGm();
		cn.game.games.cache.entity.PlayerData data = new cn.game.games.cache.entity.PlayerData();
		cn.game.util.ObjUtil.setDefaultValue(data);
		data.setPlayerId(99990002L);
		data.setUid(99990002L);
		data.setName("TraceTestPlayer");
		data.setServerId("game_test");
		data.setLevel(1);
		data.setAccountId("acc_trace_2");
		player = new Player(data);
		player.initModule(new java.util.HashMap<>());
		PlayerManager.getInstance().initAdd(player);

		// 确保当前处于 DEV 模式
		setRunMode(RunMode.DEV);
	}

	@AfterEach
	public void tearDown() throws Exception {
		setRunMode(RunMode.DEV);
		traceService.clear();
		traceService.stop();
		if (player != null) {
			PlayerManager.getInstance().deletePlayer(player);
		}
	}

	private void setRunMode(RunMode mode) throws Exception {
		Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
		runModeField.setAccessible(true);
		runModeField.set(ServerContext.getInstance(), mode);
	}

	@Test
	@DisplayName("测试 Trace 启动、停止、状态查询与清空")
	public void testTraceLifecycleAndStatus() {
		// 1. 初始状态为关闭
		Assertions.assertFalse(traceService.isRecording(player.getPlayerId()));
		Map<String, Object> status = traceService.status();
		Assertions.assertEquals(false, status.get("enabled"));
		Assertions.assertEquals(0, ((Number) status.get("bufferedCount")).intValue());

		// 2. 开启录制 (针对特定玩家)
		traceService.start(player.getPlayerId(), 10, true);
		Assertions.assertTrue(traceService.isRecording(player.getPlayerId()));
		Assertions.assertFalse(traceService.isRecording(88888888L)); // 其他玩家不录制

		// 3. 停止录制
		traceService.stop();
		Assertions.assertFalse(traceService.isRecording(player.getPlayerId()));

		// 4. 清空
		traceService.clear();
		Assertions.assertEquals(0, ((Number) traceService.status().get("bufferedCount")).intValue());
	}

	@Test
	@DisplayName("测试请求生命周期、资源流水捕获与异常记录")
	public void testRequestTraceRecordingAndQuery() {
		traceService.start(player.getPlayerId(), 500, true);

		// 模拟请求开始
		RequestTraceService.TraceBuilder builder = traceService.beginTrace(player.getPlayerId(), 0x01000001, "TestRequest", 1);
		Assertions.assertNotNull(builder);

		// 模拟资源流水
		PlayerHelper.addResources(player, Asset.diamond.ID, 100, OpType.GM, false);
		PlayerHelper.delResources(player, Asset.diamond.ID, 30, OpType.GM, false);

		// 模拟错误日志与异常记录
		RequestTraceService.recordErrorEvent("testLogger", "test error log msg", "java.lang.IllegalStateException");
		builder.recordException(new LogicException(ErrorMsgEnum.request_parameter_error.ID, "test param err"));

		// 结束请求
		traceService.endTrace(builder, ErrorMsgEnum.request_parameter_error.ID);

		// 验证查询
		List<RequestTraceService.TraceRecord> records = traceService.query(player.getPlayerId(), 0x01000001, null, 1, null, false, 10);
		Assertions.assertEquals(1, records.size());
		RequestTraceService.TraceRecord rec = records.get(0);
		Assertions.assertEquals(player.getPlayerId(), rec.getPlayerId());
		Assertions.assertEquals(0x01000001, rec.getMsgId());
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.ID, rec.getErrorCode());

		// 验证资源变动流水 (resourceEvents 与 resourceDeltas 兼容访问)
		Assertions.assertNotNull(rec.getResourceEvents());
		Assertions.assertTrue(rec.getResourceEvents().size() >= 2);
		Assertions.assertEquals(Asset.diamond.ID, rec.getResourceEvents().get(0).getId());
		Assertions.assertEquals(100L, rec.getResourceEvents().get(0).getCount());
		Assertions.assertTrue(rec.getResourceEvents().get(0).isAdd());

		// 验证错误采集 (errorEvents 与 errors 兼容访问)
		Assertions.assertNotNull(rec.getErrorEvents());
		Assertions.assertEquals(2, rec.getErrorEvents().size());
		Assertions.assertTrue(rec.hasErrors());

		// 验证 onlyErrors 过滤
		List<RequestTraceService.TraceRecord> errRecords = traceService.query(0L, 0, null, -1, null, true, 10);
		Assertions.assertEquals(1, errRecords.size());
	}

	@Test
	@DisplayName("测试环形缓冲有界淘汰 (超出 maxRecords 自动淘汰最旧记录，防止 OOM)")
	public void testBoundedBufferEviction() {
		// 设置容量为 10 条 (根据 DC-13 规格下限为 10)
		traceService.start(0L, 10, true);

		for (int i = 1; i <= 15; i++) {
			RequestTraceService.TraceBuilder builder = traceService.beginTrace(1000L + i, 0x01000000 + i, "Msg_" + i, i);
			traceService.endTrace(builder, 0);
		}

		Map<String, Object> status = traceService.status();
		int bufferedCount = ((Number) status.get("bufferedCount")).intValue();
		long totalRecorded = ((Number) status.get("totalRecorded")).longValue();

		// 缓冲区严格维持在 10 条
		Assertions.assertEquals(10, bufferedCount);
		Assertions.assertEquals(15L, totalRecorded);

		// 剩余的应该是最新的 10 条 (6 ~ 15)，最旧的 (1 ~ 5) 已被淘汰
		List<RequestTraceService.TraceRecord> queryAll = traceService.query(0L, 0, null, -1, null, false, 20);
		Assertions.assertEquals(10, queryAll.size());
		// 验证首条是最新的 Msg_15
		Assertions.assertEquals("Msg_15", queryAll.get(0).getMsgName());
	}

	@Test
	@DisplayName("测试 PlayerGm 动作暴露与生产环境硬阻断 (EX-09)")
	public void testGmActionsAndProductionHardBlock() throws Exception {
		// 1. 测试 PlayerGm trace.* 动作
		Object startRes = playerGm.traceStart(player, player.getPlayerId(), 10, true);
		Assertions.assertTrue(startRes instanceof Map);

		Object statusRes = playerGm.traceStatus(player);
		Assertions.assertTrue(statusRes instanceof Map);

		Object stopRes = playerGm.traceStop(player);
		Assertions.assertTrue(stopRes instanceof Map);

		Object clearRes = playerGm.traceClear(player);
		Assertions.assertTrue(clearRes instanceof Map);

		// 2. 生产环境硬阻断
		setRunMode(RunMode.PRODUCTION);

		// 生产模式下 isRecording 恒为 false
		Assertions.assertFalse(traceService.isRecording(player.getPlayerId()));

		// 生产模式下调用 gm.trace.* 必须抛出 LogicException(production_gm_not_allow)
		Assertions.assertThrows(LogicException.class, () -> playerGm.traceStart(player, player.getPlayerId(), 10, true));
		Assertions.assertThrows(LogicException.class, () -> playerGm.traceStop(player));
		Assertions.assertThrows(LogicException.class, () -> playerGm.traceStatus(player));
		Assertions.assertThrows(LogicException.class, () -> playerGm.traceQuery(player, 0L, 0, "", -1, "", false, 10));
		Assertions.assertThrows(LogicException.class, () -> playerGm.traceClear(player));
	}
}
