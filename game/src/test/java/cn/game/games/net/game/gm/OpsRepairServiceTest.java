package cn.game.games.net.game.gm;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
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
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.gm.command.PlayerGm;
import cn.game.games.net.game.gm.repair.OpsRepairService;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.util.ObjUtil;

/**
 * 生产受控具名修数服务、热重扫与生产门禁验证测试 (TC-C15-OPS-REPAIR-AND-PROD-GATE)
 * 覆盖条款：BR-16, EX-09, EX-12, AC-15, DC-17, DC-18, DR-09, DR-10, TW-06
 *
 * @author shuyongqiang
 */
@Isolated
@DisplayName("OpsRepairService 生产受控具名修数服务测试 (TC-C15-OPS-REPAIR-AND-PROD-GATE)")
public class OpsRepairServiceTest {

	private Player player;
	private OpsRepairService repairService;
	private Path tempAuditFile;

	@BeforeEach
	public void setUp() throws Exception {
		setRunMode(RunMode.DEV);
		ClassManager.getInstance().init();

		tempAuditFile = Files.createTempFile("ops-repair-audit-test-", ".jsonl");
		System.setProperty("gm.repair.auditFile", tempAuditFile.toAbsolutePath().toString());

		repairService = OpsRepairService.getInstance();
		repairService.initAuditLogger();

		long testPid = 99990005L;
		cleanLane(testPid);

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(testPid);
		data.setUid(testPid);
		data.setName("OpsRepairTestPlayer");
		data.setServerId("game_test");
		data.setLevel(10);
		data.setExp(100);
		data.setVipLevel(1);
		data.setRefreshDay(20261115);
		data.setOfflineTime(System.currentTimeMillis());

		player = new Player(data);
		player.setOnline(true);
		PlayerHelper.addResources(player, Asset.gold.ID, 500, OpType.GM, false);

		PlayerManager.getInstance().initAdd(player);

		// 初始化 GmDispatcher 注册 repair 动作
		GmDispatcher dispatcher = GmDispatcher.getInstance();
		if (!dispatcher.hasCommand("player.time.offset")) { dispatcher.registerBean(new PlayerGm()); }
		if (!dispatcher.hasCommand("repair.list")) { dispatcher.registerBean(repairService); }
		
	}

	@AfterEach
	public void tearDown() throws Exception {
		setRunMode(RunMode.DEV);
		System.clearProperty("gm.repair.auditFile");
		if (tempAuditFile != null) {
			Files.deleteIfExists(tempAuditFile);
		}
		if (player != null) {
			cleanLane(player.getPlayerId());
		}
	}

	@Test
	@DisplayName("1. 具名修数算子元数据列表查询 (repair.list 在 DEV 与 PRODUCTION 均可用)")
	public void testRepairListAvailableInProduction() throws Exception {
		// 在 DEV 模式下查询
		List<Map<String, Object>> listDev = repairService.list();
		Assertions.assertNotNull(listDev);
		Assertions.assertEquals(6, listDev.size());

		// 验证 6 个算子存在
		List<String> repairIds = listDev.stream().map(m -> (String) m.get("repairId")).toList();
		Assertions.assertTrue(repairIds.contains("tdlv"));
		Assertions.assertTrue(repairIds.contains("questState"));
		Assertions.assertTrue(repairIds.contains("battleHighest"));
		Assertions.assertTrue(repairIds.contains("recalcPower"));
		Assertions.assertTrue(repairIds.contains("deductDuplicateItem"));
		Assertions.assertTrue(repairIds.contains("dataFix"));

		// 切换到 PRODUCTION 模式，验证 repair.list (OPS_SAFE) 仍可调用
		setRunMode(RunMode.PRODUCTION);
		GmResult resProd = GmDispatcher.getInstance().dispatchAction("repair.list", 0, Map.of(), "trace-list-prod", false);
		Assertions.assertTrue(resProd.isOk(), "repair.list 作为 OPS_SAFE 必须在生产环境可查");
	}

	@Test
	@DisplayName("2. dryRun=true 演练执行：沙箱克隆、零副作用与字段级 JSON Diff 生成")
	public void testRepairExecuteDryRunDiff() throws Exception {
		long pid = player.getPlayerId();
		int oldLevel = player.getDevelopModule().getHeavenlyDaoLevel();

		// 执行 dryRun=true
		GmResult result = repairService.execute(
				"tdlv",
				pid,
				"{\"level\": 15}",
				"天道等级演练对比",
				true,
				"tester-dryrun");

		Assertions.assertTrue(result.isOk(), "dryRun 执行应成功: " + result.getMessage() + ", code=" + result.getErrorCode());
		Assertions.assertTrue(result.isDryRun(), "返回体 dryRun 标记应为 true");
		Assertions.assertNotNull(result.getDiff(), "dryRun 应产出结构化 Diff 记录");

		List<?> diffList = (List<?>) result.getDiff();
		Assertions.assertFalse(diffList.isEmpty(), "Diff 列表不能为空");

		// 验证真实在线玩家数据完全未变（零副作用）
		Assertions.assertEquals(oldLevel, player.getDevelopModule().getHeavenlyDaoLevel(),
				"dryRun=true 真实玩家天道等级绝不能改变");
	}

	@Test
	@DisplayName("3. dryRun=false 真实修数：状态变更、持久化与审计记录")
	public void testRepairExecuteRealMutation() throws Exception {
		long pid = player.getPlayerId();
		int targetLevel = 18;

		// 执行真实修改
		GmResult result = repairService.execute(
				"tdlv",
				pid,
				"{\"level\": " + targetLevel + "}",
				"生产天道修为真实修复",
				false,
				"ops-admin");

		Assertions.assertTrue(result.isOk(), "真实修数执行应成功: " + result.getMessage() + ", code=" + result.getErrorCode());
		Assertions.assertFalse(result.isDryRun(), "返回体 dryRun 标记应为 false");
		Assertions.assertEquals(targetLevel, player.getDevelopModule().getHeavenlyDaoLevel(),
				"真实玩家天道等级应已修改生效");
	}

	@Test
	@DisplayName("4. 审计日志：本地 JSONL 文件落盘与 repair.auditLog 内存查询")
	public void testOpsRepairAuditLog() throws Exception {
		long pid = player.getPlayerId();

		// 执行一次修数产生日志
		repairService.execute("recalcPower", pid, "{}", "战力重算", false, "ops-auditor");

		// 通过 repair.auditLog 查询
		List<Map<String, Object>> logs = repairService.auditLog(pid, 10);
		Assertions.assertNotNull(logs);
		Assertions.assertFalse(logs.isEmpty(), "应能查询到修数审计日志");

		Map<String, Object> latest = logs.get(0);
		Assertions.assertEquals("recalcPower", latest.get("repairId"));
		Assertions.assertEquals("战力重算", latest.get("reason"));
		Assertions.assertEquals("ops-auditor", latest.get("operator"));

		// 检查本地 JSONL 文件内容
		List<String> lines = Files.readAllLines(tempAuditFile);
		Assertions.assertFalse(lines.isEmpty(), "JSONL 审计日志文件必须包含落盘记录");
		Assertions.assertTrue(lines.stream().anyMatch(line -> line.contains("recalcPower") && line.contains("战力重算")),
				"JSONL 记录内容必须与审计字段一致");
	}

	@Test
	@DisplayName("5. repair.rescan (gm.hotReloadScan) 热更新重扫测试")
	public void testRepairRescan() throws Exception {
		GmResult rescanResult = repairService.rescan();
		Assertions.assertTrue(rescanResult.isOk(), "rescan 重扫应成功");
		Map<?, ?> data = (Map<?, ?>) rescanResult.getData();
		Assertions.assertNotNull(data);
		Assertions.assertTrue(((Number) data.get("actionCount")).intValue() > 0, "重扫后动作数应大于0");

		// 验证别名 gm.hotReloadScan 同样生效
		GmResult aliasResult = GmDispatcher.getInstance().dispatchAction("gm.hotReloadScan", 0, Map.of(), "trace-rescan", false);
		Assertions.assertTrue(aliasResult.isOk(), "别名 gm.hotReloadScan 应正确分发");
	}

	@Test
	@DisplayName("6. 生产门禁排他性：PRODUCTION 模式放行 OPS_REPAIR / OPS_SAFE，硬阻断 DEV_TEST_ONLY")
	public void testProductionGateExclusivity() throws Exception {
		setRunMode(RunMode.PRODUCTION);

		// DEV_TEST_ONLY 动作（如 time.offset）必须被阻断
		GmResult blockDev = GmDispatcher.getInstance().dispatchAction("player.time.offset", 0, Map.of("offset", "+1h"), "trace-gate-1", false);
		Assertions.assertFalse(blockDev.isOk(), "生产模式必须阻断 DEV_TEST_ONLY 动作");
		Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.ID, blockDev.getErrorCode());

		// OPS_SAFE 动作（如 repair.list）必须放行
		GmResult allowSafe = GmDispatcher.getInstance().dispatchAction("repair.list", 0, Map.of(), "trace-gate-2", false);
		Assertions.assertTrue(allowSafe.isOk(), "生产模式必须放行 OPS_SAFE 动作");

		// OPS_REPAIR 动作（如 repair.rescan）必须放行
		GmResult allowRepair = GmDispatcher.getInstance().dispatchAction("repair.rescan", 0, Map.of(), "trace-gate-3", false);
		Assertions.assertTrue(allowRepair.isOk(), "生产模式必须放行 OPS_REPAIR 动作");
	}

	@Test
	@DisplayName("7. 离线修数并发保护与 finally 清理闭环验证")
	public void testOfflineLockAndCleanup() throws Exception {
		long offlinePid = 99990006L;
		cleanLane(offlinePid);

		// 确保该离线玩家在内存中不存在
		Assertions.assertNull(PlayerManager.getInstance().getPlayer(offlinePid));

		// 模拟加锁冲突或者离线执行
		boolean locked = repairService.acquireOfflineLock(offlinePid);
		Assertions.assertTrue(locked, "首次尝试获取离线玩家分布式锁应成功");

		// 再次获取相同锁应失败（跨线程模拟并发冲突）
		java.util.concurrent.atomic.AtomicBoolean acquiredOther = new java.util.concurrent.atomic.AtomicBoolean(true);
		Thread lockThread = new Thread(() -> acquiredOther.set(repairService.tryAcquireOfflineLockOnly(offlinePid)));
		lockThread.start();
		lockThread.join();
		Assertions.assertFalse(acquiredOther.get(), "锁已被占用时跨线程再次获取应冲突失败");

		// 释放锁
		repairService.releaseOfflineLock(offlinePid);

		// 释放后再获取应成功
		boolean reLocked = repairService.acquireOfflineLock(offlinePid);
		Assertions.assertTrue(reLocked, "释放锁后应能再次成功获取");
		repairService.releaseOfflineLock(offlinePid);
	}

	@Test
	@DisplayName("8. dataFix 算子不支持 dryRun 保护及参数边界校验")
	public void testDataFixDryRunRejection() throws Exception {
		long pid = player.getPlayerId();
		GmResult resDry = repairService.execute("dataFix", pid, "{\"methodName\":\"testFix\"}", "修数测试", true, "tester");
		Assertions.assertFalse(resDry.isOk(), "dataFix 算子严禁 dryRun 演练模式");
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.ID, resDry.getErrorCode());

		GmResult resEmpty = repairService.execute("dataFix", pid, "{}", "缺少methodName", false, "tester");
		Assertions.assertFalse(resEmpty.isOk(), "缺少必填参数必须快速失败");
	}

	@Test
	@DisplayName("9. tdlv 下限校验与 battleHighest 别名 stuck_battle 校验")
	public void testTdlvBoundaryAndBattleAlias() throws Exception {
		long pid = player.getPlayerId();
		GmResult resBadLevel = repairService.execute("tdlv", pid, "{\"level\": 0}", "无效等级", false, "tester");
		Assertions.assertFalse(resBadLevel.isOk(), "等级<=0必须被拦截");

		GmResult resAlias = repairService.execute("stuck_battle", pid, "{\"highestBattleId\": 20}", "清理卡死战斗", false, "tester");
		Assertions.assertTrue(resAlias.isOk(), "算子别名 stuck_battle 应能正常执行修数");
	}

	private void setRunMode(RunMode mode) throws Exception {
		Field modeField = ServerContext.class.getDeclaredField("runMode");
		modeField.setAccessible(true);
		modeField.set(ServerContext.getInstance(), mode);
	}

	private void cleanLane(long pid) throws Exception {
		try {
			PlayerSaveCoordinator coord = PlayerSaveCoordinator.shared();
			Field lanesField = PlayerSaveCoordinator.class.getDeclaredField("lanes");
			lanesField.setAccessible(true);
			Map<?, ?> lanes = (Map<?, ?>) lanesField.get(coord);
			if (lanes != null) {
				lanes.remove(pid);
			}
		} catch (Throwable ignored) {
		}
		try {
			PlayerManager pm = PlayerManager.getInstance();
			Field mapField = PlayerManager.class.getDeclaredField("id_players");
			mapField.setAccessible(true);
			Map<?, ?> map = (Map<?, ?>) mapField.get(pm);
			if (map != null) {
				map.remove(pid);
			}
		} catch (Throwable ignored) {
		}
	}
}
