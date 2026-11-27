package cn.game.games.net.game.gm.command;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.game.gm.proto.VirtualGameClient;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.ExpConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.protocol.manual.OpType;
import cn.game.util.ObjUtil;
import io.vertx.core.Vertx;

/**
 * 常用快捷 GM 指令单元测试（精确设资源 item.set、等级/VIP 与差1经验即升级、全功能解锁）
 *
 * @author shuyongqiang
 */
@Isolated
public class QuickGmCommandTest {

	private static final Logger log = LoggerFactory.getLogger(QuickGmCommandTest.class);
	private static final long TEST_PLAYER_ID = 900_000_088_776L;
	private static Vertx previousVertx;
	private static Vertx createdVertx;

	private Player player;
	private PlayerGm playerGm;
	private ItemGm itemGm;

	@BeforeAll
	public static void beforeAll() {
		ClassManager.getInstance().init();
		previousVertx = VxHolder.vertx;
		if (VxHolder.vertx == null) {
			createdVertx = Vertx.vertx();
			VxHolder.vertx = createdVertx;
		}
		try {
			ManagerHelper.init();
		} catch (Throwable t) {
			log.warn("初始化配置表告警", t);
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

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(TEST_PLAYER_ID);
		data.setUid(TEST_PLAYER_ID);
		data.setName("QuickGmTester");
		data.setServerId("game_test_quick_gm");
		data.setLevel(1);

		player = new Player(data);
		player.setOnline(true);
		player.setGameClient(new VirtualGameClient(TEST_PLAYER_ID));
		player.initModule(new HashMap<String, BasePlayerModule>());
		PlayerManager.getInstance().initAdd(player);
		player.getPlayerModule().initLevel();
		player.getFuncModule().initFuncOpenData();

		playerGm = new PlayerGm();
		itemGm = new ItemGm();
	}

	@AfterEach
	public void tearDown() {
		if (player != null) {
			player.cancelAllTimer();
			try {
				PlayerManager.getInstance().deletePlayer(player.getPlayerId());
			} catch (Throwable t) {
				log.debug("清理测试玩家缓存告警 playerId={}", player.getPlayerId(), t);
			}
		}
		System.setProperty(ServerContext.SERVER_RUN_MODE, "DEV");
		ServerContext.getInstance().setRunMode();
	}

	/**
	 * 验证 item.set 能精确设置货币增加、减少与清零
	 */
	@Test
	public void testItemSetExactCurrencyCount() {
		Map<String, Object> r1 = itemGm.set(player, Asset.diamond.ID, 500L);
		Assertions.assertEquals(0L, r1.get("before"));
		Assertions.assertEquals(500L, r1.get("after"));
		Assertions.assertEquals(500L, player.getCurrencyModule().getCount(Asset.diamond.ID));

		Map<String, Object> r2 = itemGm.set(player, Asset.diamond.ID, 99L);
		Assertions.assertEquals(500L, r2.get("before"));
		Assertions.assertEquals(99L, r2.get("after"));
		Assertions.assertEquals(99L, player.getCurrencyModule().getCount(Asset.diamond.ID));

		Map<String, Object> r3 = itemGm.set(player, Asset.diamond.ID, 0L);
		Assertions.assertEquals(0L, r3.get("after"));
		Assertions.assertEquals(0L, player.getCurrencyModule().getCount(Asset.diamond.ID));
	}

	/**
	 * 验证 player.setLevel 支持设目标等级、将经验设为当前等级最大经验 - 1，且再加 1 点经验即触发升级
	 */
	@Test
	public void testSetLevelAndNearLevelUpFlow() {
		Map<String, Object> res = playerGm.setLevel(player, 5, true, -1);
		int setLv = ((Number) res.get("level")).intValue();
		long setExp = ((Number) res.get("exp")).longValue();
		int maxExp = ((Number) res.get("maxExpForLevel")).intValue();

		Assertions.assertEquals(5, setLv);
		Assertions.assertEquals(5, player.getData().getLevel());
		Assertions.assertEquals(5, player.getPlayerModule().getExpLevelMap().getValue(Asset.playerExp.ID));

		ExpConfig curCfg = PlayerHelper.getExpConfig(Asset.playerExp.ID, 5, 0);
		ExpConfig nextCfg = PlayerHelper.getExpConfig(Asset.playerExp.ID, 6, 0);
		if (curCfg != null && curCfg.experience > 0 && nextCfg != null) {
			Assertions.assertEquals(curCfg.experience, maxExp);
			Assertions.assertEquals(curCfg.experience - 1L, setExp);
			Assertions.assertEquals(1L, ((Number) res.get("expToNextLevel")).longValue());
			Assertions.assertEquals(curCfg.experience - 1L, player.getCurrencyModule().getCount(Asset.playerExp.ID));

			// 再加 1 点玩家经验，应走真实 CurrencyModule.addExp 立刻升至 6 级且余经验归 0
			player.getCurrencyModule().add(Asset.playerExp.ID, 1, OpType.Test);
			Assertions.assertEquals(6, player.getPlayerModule().getExpLevelMap().getValue(Asset.playerExp.ID));
			Assertions.assertEquals(0L, player.getCurrencyModule().getCount(Asset.playerExp.ID));
		}

		// 保持当前等级不变（level=0）仅设置差 1 经验即升级
		int lvBefore = player.getPlayerModule().getExpLevelMap().getValue(Asset.playerExp.ID);
		Map<String, Object> keepRes = playerGm.setLevel(player, 0, true, -1);
		Assertions.assertEquals(lvBefore, ((Number) keepRes.get("level")).intValue());
	}

	/**
	 * 验证 player.setVip 支持设目标 VIP 等级、差 1 经验即升级与 +1 VIP 经验真实升级
	 */
	@Test
	public void testSetVipAndNearLevelUpFlow() {
		Map<String, Object> res = playerGm.setVip(player, 2, true, -1);
		int vipLv = ((Number) res.get("vipLevel")).intValue();
		long vipExp = ((Number) res.get("exp")).longValue();

		Assertions.assertEquals(2, vipLv);
		Assertions.assertEquals(2, player.getData().getVipLevel());
		Assertions.assertEquals(2, player.getPlayerModule().getExpLevelMap().getValue(Asset.VIPExp.ID));

		ExpConfig curCfg = PlayerHelper.getExpConfig(Asset.VIPExp.ID, 2, 0);
		ExpConfig nextCfg = PlayerHelper.getExpConfig(Asset.VIPExp.ID, 3, 0);
		if (curCfg != null && curCfg.experience > 0 && nextCfg != null) {
			Assertions.assertEquals(curCfg.experience - 1L, vipExp);
			Assertions.assertEquals(1L, ((Number) res.get("expToNextLevel")).longValue());

			// 再加 1 点 VIP 经验，应真实升至 VIP 3
			player.getCurrencyModule().add(Asset.VIPExp.ID, 1, OpType.Test);
			Assertions.assertEquals(3, player.getPlayerModule().getExpLevelMap().getValue(Asset.VIPExp.ID));
		}
	}

	/**
	 * 验证 player.openAllFunc 能一键解锁全部 InitialUI 功能
	 */
	@Test
	public void testOpenAllFuncUnlocksEveryInitialUi() {
		Map<String, Object> res = playerGm.openAllFunc(player);
		Assertions.assertEquals(InitialUI.values().length, ((Number) res.get("totalFunctions")).intValue());
		for (InitialUI ui : InitialUI.values()) {
			Assertions.assertTrue(player.isFuncOpen(ui), "功能应当已解锁: " + ui.name());
		}
	}

	/**
	 * 验证代码审核修复边界：经验类资源 item.set 拦截、空客户端无 NPE、满级 nearLevelUp 告警、battle.zxgk 越界拦截
	 */
	@Test
	public void testReviewEdgeCasesGuarded() {
		// 1. 经验类资源禁止直接走 item.set
		Assertions.assertThrows(LogicException.class, () -> itemGm.set(player, Asset.playerExp.ID, 100L));
		Assertions.assertThrows(LogicException.class, () -> itemGm.set(player, Asset.VIPExp.ID, 100L));

		// 2. 无 GameClient 时 item.set 增减货币不抛 NPE
		player.setGameClient(null);
		Map<String, Object> setAdd = itemGm.set(player, Asset.gold.ID, 300L);
		Assertions.assertEquals(300L, setAdd.get("after"));
		Map<String, Object> setDel = itemGm.set(player, Asset.gold.ID, 50L);
		Assertions.assertEquals(50L, setDel.get("after"));
		Assertions.assertEquals(-250L, setDel.get("delta"));

		// 3. 满级时 nearLevelUp=true 返回 warning 且不把经验改写成 maxExp-1 误导测试
		Map<String, Object> maxRes = playerGm.setLevel(player, 9999, true, -1);
		if (Boolean.TRUE.equals(maxRes.get("isMaxLevel"))) {
			Assertions.assertEquals(false, maxRes.get("nearLevelUp"));
			Assertions.assertNotNull(maxRes.get("warning"), "满级传 nearLevelUp=true 必须返回明确 warning");
		}

		// 4. battle.zxgk 负数与超上限序号抛出受控 LogicException 而非 IndexOutOfBoundsException
		BattleGm battleGm = new BattleGm();
		Assertions.assertThrows(LogicException.class, () -> battleGm.zxgk(player, -1));
		Assertions.assertThrows(LogicException.class, () -> battleGm.zxgk(player, 999));
	}
}
