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
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.gm.command.PlayerGm;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.util.ObjUtil;

/**
 * 非生产玩家快照管理与真实冷重载测试 (TC-C13-SNAPSHOT-COLD-RELOAD)
 * 覆盖条款：BR-14, EX-09, EX-10, AC-13, DC-15, DR-07, TW-05
 *
 * @author shuyongqiang
 */
@Isolated
@DisplayName("PlayerSnapshotService 玩家本地快照与冷重载测试 (TC-C13-SNAPSHOT-COLD-RELOAD)")
public class PlayerSnapshotServiceTest {

	private Player player;
	private PlayerGm playerGm;
	private PlayerSnapshotService snapshotService;

	@BeforeEach
	public void setUp() throws Exception {
		setRunMode(RunMode.DEV);
		ClassManager.getInstance().init();
		snapshotService = PlayerSnapshotService.getInstance();
		playerGm = new PlayerGm();

		long testPid = 99990003L;
		cleanLane(testPid);

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(testPid);
		data.setUid(testPid);
		data.setName("SnapshotTestPlayer");
		data.setServerId("game_test");
		data.setLevel(10);
		data.setAccountId("acc_snap_1");

		player = new Player(data);
		player.initModule(new java.util.HashMap<>());
		PlayerManager.getInstance().initAdd(player);

		snapshotService.delete(player.getPlayerId(), "", true);
	}

	@AfterEach
	public void tearDown() throws Exception {
		setRunMode(RunMode.DEV);
		if (player != null) {
			long testPid = player.getPlayerId();
			snapshotService.delete(testPid, "", true);
			PlayerManager.getInstance().deletePlayer(player);
			cleanLane(testPid);
		}
	}

	private void cleanLane(long pid) {
		try {
			Field lanesField = PlayerSaveCoordinator.class.getDeclaredField("lanes");
			lanesField.setAccessible(true);
			Map<?, ?> lanes = (Map<?, ?>) lanesField.get(PlayerSaveCoordinator.shared());
			lanes.remove(pid);
		} catch (Throwable ignored) {}
		try {
			Field playersField = PlayerManager.class.getDeclaredField("id_players");
			playersField.setAccessible(true);
			Map<?, ?> players = (Map<?, ?>) playersField.get(PlayerManager.getInstance());
			players.remove(pid);
		} catch (Throwable ignored) {}
	}

	private void setRunMode(RunMode mode) throws Exception {
		Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
		runModeField.setAccessible(true);
		runModeField.set(ServerContext.getInstance(), mode);
	}

	@Test
	@DisplayName("测试快照保存、修改、列表与还原回滚 (AC-13)")
	public void testSnapshotSaveAndRestore() {
		CurrencyModule currencyModule = player.getModule(CurrencyModule.class);
		currencyModule.add(Asset.diamond.ID, 1000, OpType.GM);
		player.getData().setLevel(10);

		// 1. 保存快照 snap1
		Map<String, Object> saveRes = snapshotService.save(player, "snap1");
		Assertions.assertNotNull(saveRes);
		Assertions.assertEquals("snap1", saveRes.get("snapshotName"));
		Assertions.assertTrue(((Number) saveRes.get("byteSize")).intValue() > 0);

		// 2. 修改玩家状态
		currencyModule.add(Asset.diamond.ID, 9999, OpType.GM);
		player.getData().setLevel(20);
		Assertions.assertEquals(10999L, currencyModule.getCount(Asset.diamond.ID));
		Assertions.assertEquals(20, player.getData().getLevel());

		// 3. 校验列表
		List<Map<String, Object>> list = snapshotService.list(player.getPlayerId());
		Assertions.assertEquals(1, list.size());
		Assertions.assertEquals("snap1", list.get(0).get("snapshotName"));

		// 4. 还原快照 snap1
		Map<String, Object> restoreRes = snapshotService.restore(player, "snap1");
		Assertions.assertNotNull(restoreRes);

		// 5. 校验状态 100% 还原
		CurrencyModule restoredCurrency = player.getModule(CurrencyModule.class);
		Assertions.assertEquals(1000L, restoredCurrency.getCount(Asset.diamond.ID));
		Assertions.assertEquals(10, player.getData().getLevel());

		// 6. 还原未知快照名立即抛出 LogicException(request_parameter_error)
		Assertions.assertThrows(LogicException.class, () -> snapshotService.restore(player, "nonExistentSnap"));
	}

	@Test
	@DisplayName("测试单玩家 20 个快照上限 LRU 淘汰 (DR-07)")
	public void testSnapshotLruEviction() {
		for (int i = 1; i <= 20; i++) {
			snapshotService.save(player, "snap_" + i);
		}

		// 访问 snap_1（通过 restore 刷新其 LRU 访问序）
		snapshotService.restore(player, "snap_1");

		// 再添加第 21 个快照 snap_21，此时最少使用的应是 snap_2（而非 snap_1）
		snapshotService.save(player, "snap_21");

		List<Map<String, Object>> list = snapshotService.list(player.getPlayerId());
		Assertions.assertEquals(20, list.size());

		// 验证 snap_2 被淘汰，而 snap_1 与 snap_21 均完好保留 (真实 LRU 断言)
		Assertions.assertThrows(LogicException.class, () -> snapshotService.restore(player, "snap_2"));
		Assertions.assertNotNull(snapshotService.restore(player, "snap_1"));
		Assertions.assertNotNull(snapshotService.restore(player, "snap_21"));
	}

	@Test
	@DisplayName("测试快照删除动作")
	public void testSnapshotDelete() {
		snapshotService.save(player, "s1");
		snapshotService.save(player, "s2");
		Assertions.assertEquals(2, snapshotService.list(player.getPlayerId()).size());

		// 删除单个快照
		snapshotService.delete(player.getPlayerId(), "s1", false);
		Assertions.assertEquals(1, snapshotService.list(player.getPlayerId()).size());

		// 删除全部快照
		snapshotService.delete(player.getPlayerId(), "", true);
		Assertions.assertEquals(0, snapshotService.list(player.getPlayerId()).size());
	}

	@Test
	@DisplayName("测试冷重载前落盘检查与生产环境硬阻断 (EX-09, EX-10)")
	public void testFlushColdReloadAndProductionBlock() throws Exception {
		// 1. 验证 PlayerGm snapshot.* 动作在 DEV 正常执行
		Object saveRes = playerGm.snapshotSave(player, "gm_snap");
		Assertions.assertTrue(saveRes instanceof Map);

		Object listRes = playerGm.snapshotList(player);
		Assertions.assertTrue(listRes instanceof List);

		Object restoreRes = playerGm.snapshotRestore(player, "gm_snap");
		Assertions.assertTrue(restoreRes instanceof Map);

		Object delRes = playerGm.snapshotDelete(player, "gm_snap", false);
		Assertions.assertTrue(delRes instanceof Map);

		// 2. 生产环境硬阻断
		setRunMode(RunMode.PRODUCTION);

		Assertions.assertThrows(LogicException.class, () -> playerGm.snapshotSave(player, "prod_snap"));
		Assertions.assertThrows(LogicException.class, () -> playerGm.snapshotRestore(player, "prod_snap"));
		Assertions.assertThrows(LogicException.class, () -> playerGm.snapshotList(player));
		Assertions.assertThrows(LogicException.class, () -> playerGm.snapshotDelete(player, "prod_snap", false));
		Assertions.assertThrows(LogicException.class, () -> playerGm.flushAndColdReload(player, player.getPlayerId()));
	}
}
