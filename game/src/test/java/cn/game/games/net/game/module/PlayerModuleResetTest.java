package cn.game.games.net.game.module;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Item;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.gm.command.PlayerGm;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.util.ObjUtil;

/**
 * 模块 resetForTest 默认反射清理、特例覆写与 gm.resetModule 验证 (TC-C12-MODULE-RESET)
 *
 * @author shuyongqiang
 */
@Isolated
public class PlayerModuleResetTest {

	private RunMode originalRunMode;
	private Player player;
	private PlayerGm playerGm;

	@BeforeEach
	public void setUp() throws Exception {
		Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
		runModeField.setAccessible(true);
		originalRunMode = (RunMode) runModeField.get(ServerContext.getInstance());
		runModeField.set(ServerContext.getInstance(), RunMode.DEV);

		ClassManager.getInstance().init();

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(99990001L);
		data.setUid(99990001L);
		data.setName("ResetTestPlayer");
		data.setServerId("game_test");
		data.setLevel(1);
		data.setAccountId("acc_reset_1");

		player = new Player(data);
		player.initModule(new HashMap<String, BasePlayerModule>());
		PlayerManager.getInstance().initAdd(player);

		playerGm = new PlayerGm();
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (player != null) {
			try {
				PlayerManager.getInstance().deletePlayer(player.getPlayerId());
			} catch (Throwable ignored) {
			}
		}
		if (originalRunMode != null) {
			Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
			runModeField.setAccessible(true);
			runModeField.set(ServerContext.getInstance(), originalRunMode);
		}
	}

	@Test
	public void testModuleResetDefaultAndSpecialized() {
		CurrencyModule currencyModule = player.getCurrencyModule();
		ItemModule itemModule = player.getItemModule();
		Assertions.assertNotNull(currencyModule);
		Assertions.assertNotNull(itemModule);

		int assetId = Asset.diamond.ID;

		// 1. 填充测试数据
		currencyModule.add(assetId, 8888, OpType.None);
		Item item = new Item();
		item.setConfigId(1001);
		item.setCount(10);
		itemModule.initAddCache(item);
		Assertions.assertTrue(currencyModule.getCount(assetId) > 0);
		Assertions.assertNotNull(itemModule.get(1001));

		// 2. 针对单个模块直接调用 resetForTest()
		Map<String, Object> inspectAfterReset = currencyModule.resetForTest();
		Assertions.assertNotNull(inspectAfterReset);
		Assertions.assertEquals(0L, currencyModule.getCount(assetId));
		Assertions.assertNotNull(currencyModule.inspectState());

		// 3. 通过 PlayerGm.resetModule 局部重置指定模块
		Object partialRes = playerGm.resetModule(player, "ItemModule", "");
		Assertions.assertTrue(partialRes instanceof Map);
		Assertions.assertNull(itemModule.get(1001));

		// 4. 重置全量模块
		currencyModule.add(assetId, 500, OpType.None);
		Object allRes = playerGm.resetModule(player, "all", "");
		Assertions.assertTrue(allRes instanceof Map);
		Assertions.assertEquals(0L, currencyModule.getCount(assetId));

		// 5. 传入未知模块名立即拒绝并报错，且多模块下保持事务原子性（无部分重置脏写）
		currencyModule.add(assetId, 1234, OpType.None);
		Assertions.assertThrows(LogicException.class, () -> {
			playerGm.resetModule(player, "CurrencyModule,NonExistentModule", "");
		});
		// 验证 CurrencyModule 数据完全保持不变
		Assertions.assertEquals(1234L, currencyModule.getCount(assetId));

		// 6. JSON 数组格式兼容测试
		Object jsonRes = playerGm.resetModule(player, "[\"CurrencyModule\"]", "");
		Assertions.assertTrue(jsonRes instanceof Map);
		Assertions.assertEquals(0L, currencyModule.getCount(assetId));

		// 7. 验证 IntMapWrapper 容器原地清空与非空保障
		cn.game.games.net.game.module.battle.BattleModule battleModule = player.getModule(cn.game.games.net.game.module.battle.BattleModule.class);
		Assertions.assertNotNull(battleModule);
		battleModule.resetForTest();
		Assertions.assertNotNull(battleModule.getConsecutiveFailures(0));
		Assertions.assertNotNull(battleModule.getDailyCount());

		// 8. 生产环境硬阻断（覆盖 resetForTest、resetModule、rngSeed、rngSequence、rngReset）
		try {
			Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
			runModeField.setAccessible(true);
			runModeField.set(ServerContext.getInstance(), RunMode.PRODUCTION);

			LogicException ex1 = Assertions.assertThrows(LogicException.class, () -> {
				currencyModule.resetForTest();
			});
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.ID, ex1.getErrorCode());

			LogicException ex2 = Assertions.assertThrows(LogicException.class, () -> {
				playerGm.resetModule(player, "all", "");
			});
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.ID, ex2.getErrorCode());

			LogicException ex3 = Assertions.assertThrows(LogicException.class, () -> {
				playerGm.rngSeed(12345L);
			});
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.ID, ex3.getErrorCode());

			LogicException ex4 = Assertions.assertThrows(LogicException.class, () -> {
				playerGm.rngSequence("1,2,3", "", "", 0L);
			});
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.ID, ex4.getErrorCode());

			LogicException ex5 = Assertions.assertThrows(LogicException.class, () -> {
				playerGm.rngReset();
			});
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.ID, ex5.getErrorCode());
		} catch (Exception e) {
			Assertions.fail(e);
		}
	}
}