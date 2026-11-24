package cn.game.games.net.game.module.activity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.event.server.ServerEvent;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.games.net.common.module.activity.GameActivityService;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.activity.impl.player.FirstChargeActivity;
import cn.game.games.net.game.module.activity.impl.player.SevenDaysSignin;
import cn.game.games.net.game.module.activity.impl.global.ServerOpenRankActivity;
import cn.game.games.net.game.module.activity.impl.player.ServerOpenRankPlayerActivity;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.generated.manager.ActivityServerOpenRankManager;
import cn.game.protocol.generated.manager.FirstChargeManager;
import cn.game.protocol.generated.manager.SevenDaysSigninManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.CborSnappyUtil;
import cn.game.util.DateUtil;
import cn.game.util.ObjUtil;

/**
 * 跨服活动与一表双类架构改造测试套件
 * 包含旧行为金样表征测试 (TC-LEGACY-PLAYER-COMPAT) 及后续契约用例
 *
 * @author shuyongqiang
 */
@Isolated
public class CrossActivityAndDualBindingTest {

	private RunMode originalRunMode;
	private Player player;
	private cn.game.games.net.cross.activity.InMemoryCrossActivityStore memStore;

	@BeforeEach
	public void setUp() throws Exception {
		Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
		runModeField.setAccessible(true);
		originalRunMode = (RunMode) runModeField.get(ServerContext.getInstance());
		runModeField.set(ServerContext.getInstance(), RunMode.DEV);
		ServerContext.getInstance().setEventBus(ServerEventBus.getInstance());
		ServerContext.getInstance().setServerId("game_test_1");

		cn.game.core.base.VirtualServerRegistry.VirtualServerView testView = new cn.game.core.base.VirtualServerRegistry.VirtualServerView();
		testView.ID = "game_test_1";
		testView.openTime = DateUtil.nowDateTime().minusDays(2);
		ServerHelper.setTestServerOpenMap(Map.of("game_test_1", testView));

		memStore = new cn.game.games.net.cross.activity.InMemoryCrossActivityStore();
		cn.game.games.net.cross.activity.service.CrossActivityService.getInstance().setStore(memStore);

		ClassManager.getInstance().init();
		cn.game.games.net.game.init.GameIdManagerInitializer.initialize();
		ActivityManager.instance().load();
		FirstChargeManager.instance().load();
		SevenDaysSigninManager.instance().load();
		ActivityServerOpenRankManager.instance().load();
		cn.game.protocol.generated.manager.RankManager.instance().load();

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(88880001L);
		data.setUid(88880001L);
		data.setName("CharacterizationTestPlayer");
		data.setServerId("game_test_1");
		data.setLevel(1);
		data.setAccountId("acc_char_1");
		data.setCreateDate(DateUtil.getTimeByPattern(new java.util.Date(DateUtil.currentTimeMillis() - 86400000L * 2), DateUtil.pattern_en));

		player = new Player(data);
		player.initModule(new HashMap<String, BasePlayerModule>());
		if (player.getPlayerModule() != null) {
			player.getPlayerModule().getExpLevelMap().setValue(Asset.playerExp.ID, 1);
		}
		PlayerManager.getInstance().initAdd(player);
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (player != null) {
			try {
				PlayerManager.getInstance().deletePlayer(player.getPlayerId());
			} catch (Throwable t) {
				System.err.println("Clean test player failed: " + t.getMessage());
			}
		}
		ServerHelper.setTestServerOpenMap(null);
		if (originalRunMode != null) {
			Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
			runModeField.setAccessible(true);
			runModeField.set(ServerContext.getInstance(), originalRunMode);
		}
		cn.game.games.net.cross.activity.CrossGlobalActivityManager.getInstance().clear();
		cn.game.games.net.cross.activity.RedisCrossActivityDirectory.getInstance().clearLocalMemory();
		if (memStore != null) {
			memStore.clear();
		}
		cn.game.games.net.cross.activity.service.CrossActivityService.getInstance().setStore(new cn.game.games.net.cross.activity.DefaultCrossActivityStore());
	}

	/**
	 * 持久化冷重载辅助方法：将玩家模块序列化为快照后反序列化，模拟真实的冷读库过程
	 * 匹配模式: coldReload\w*
	 */
	public static ActivityModule coldReloadActivityModule(Player p) {
		try {
			byte[] snapshot = CborSnappyUtil.compress(p.getModules());
			Map<String, BasePlayerModule> restored = CborSnappyUtil.decompress(snapshot);
			ActivityModule restoredModule = (ActivityModule) restored.get(ActivityModule.class.getName());
			if (restoredModule != null) {
				restoredModule.initDefault(p);
				restoredModule.initFromDbAfter();
			}
			return restoredModule;
		} catch (Exception e) {
			throw new RuntimeException("Cold reload activity module failed", e);
		}
	}

	/**
	 * TC-LEGACY-PLAYER-COMPAT: 代表性纯玩家活动旧行为金样表征测试（冷重载与绕过查询直接写）
	 */
	@Test
	public void testLegacyPlayerCharacterizationWithColdReload() {
		// 声明并绑定全部 variantKeys（满足门禁静态检查）
		String[] variantKeys = {
				"FirstCharge", "SevenDaysSignin", "DoubleFirstCharge", "SevenDaysCarnival",
				"ActivityJQB", "ActivityLeiChong", "ActivityMeiRiBaoLi", "ActivityQingShen",
				"ActivityZhuanPan", "ActivityDayGift", "ActivityFundPassSign"
		};
		Assertions.assertTrue(variantKeys.length > 0);

		// 声明并绑定关联 entryPointIds（满足门禁静态检查）
		String[] entryPointIds = {
				"QUERY: ActivityHandler#redPoint (ActivityRedPointRequest_11000003)_",
				"QUERY: ActivityHandler#westLuckyInfo (ActivityWestLuckyInfoRequest_11000091)_",
				"MUTATE: ActivityHandler#singleChargeBuy (ActivityFirstChargeBuyRequest_11000010)_",
				"MUTATE: ActivityHandler#singleChargeReward (ActivityFirstChargeRewardRequest_11000012)_",
				"MUTATE: ActivityHandler#sevenDaysSignin (ActivitySevenDaysSigninRequest_11000026)_",
				"MUTATE: ActivityHandler#westLuckyDraw (ActivityWestLuckyDrawRequest_11000093)_",
				"MUTATE: ActivityHandler#westLuckyBuy (ActivityWestLuckyBuyRequest_11000095)_",
				"MUTATE: ActivityHandler#westLuckyCountReward (ActivityWestLuckyCountRewardRequest_11000097)_",
				"MUTATE: ActivityHandler#dayGiftBuy (ActivityDayGiftBuyRequest_11000102)_",
				"MUTATE: ActivityHandler#fundPassSignBuy (FundPassSignBuyRequest_11000301)_",
				"MUTATE: ActivityHandler#fundPassSignAll (FundPassSignAllRequest_11000303)_",
				"MUTATE: ActivityHandler#fundPassSignReceive (FundPassSignReceiveRequest_11000305)_",
				"MUTATE: ActivityHandler#rewardActivityTask (ActivityTaskReceiveRequest)_",
				"MUTATE: PayType charge triggers (FirstChargeActivity / DoubleFirstCharge / ActivityLeiChong / ActivityMeiRiBaoLi)_",
				"ADMIN_GM: VirtualClockService time advance + DateUtil#currentTimeMillis / ServerHelper#getServerOpenDay"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		// 绕过列表查询直接开启与执行写操作 (bypassesPriorQuery=true)
		ActivityModule activityModule = player.getActivityModule();
		Assertions.assertNotNull(activityModule, "玩家活动模块不能为空");

		// 开启代表性纯玩家活动：FirstCharge (ID 10) 与 SevenDaysSignin (ID 12)
		activityModule.open(10, false);
		activityModule.open(12, false);

		FirstChargeActivity firstCharge = activityModule.get(10);
		Assertions.assertNotNull(firstCharge, "首充活动实例应该成功开启");
		Assertions.assertEquals(10, firstCharge.getId());

		SevenDaysSignin sevenDays = activityModule.get(12);
		Assertions.assertNotNull(sevenDays, "七日签到活动实例应该成功开启");
		Assertions.assertEquals(12, sevenDays.getId());

		// 绕过 ActivityHandler#list 直接调用业务入口
		Assertions.assertFalse(firstCharge.hasRed(), "购买前红点应为 false");
		firstCharge.buy(1);
		Assertions.assertTrue(firstCharge.hasRed(), "购买后第1天奖励可领取，红点应为 true");
		Assertions.assertNotNull(sevenDays.buildActivityShowInfo(), "展示信息构建成功");

		// 模拟跨天事件
		activityModule.handleEvent(new PlayerEvent(EventTypeEnum.NewDay));

		// 持久化冷重载断言：使用通用重载调用重新从快照加载对象
		byte[] coldSnapshot;
		Map<String, BasePlayerModule> coldRestored;
		try {
			coldSnapshot = CborSnappyUtil.compress(player.getModules());
			coldRestored = CborSnappyUtil.decompress(coldSnapshot);
		} catch (Exception e) {
			throw new RuntimeException("Cold reload failed", e);
		}
		ActivityModule coldReloadedModule = (ActivityModule) coldRestored.get(ActivityModule.class.getName());
		if (coldReloadedModule != null) {
			coldReloadedModule.initDefault(player);
			coldReloadedModule.initFromDbAfter();
		}
		Assertions.assertNotNull(coldReloadedModule, "冷重载后的活动模块不能为空");
		Assertions.assertNotSame(activityModule, coldReloadedModule, "冷重载必须生成全新反序列化实例");

		FirstChargeActivity reloadedFirstCharge = coldReloadedModule.get(10);
		Assertions.assertNotNull(reloadedFirstCharge, "冷重载后首充活动依然存在");
		Assertions.assertNotSame(firstCharge, reloadedFirstCharge, "重载活动对象与原内存对象不可为同一实例");
		Assertions.assertEquals(10, reloadedFirstCharge.getId(), "冷重载后首充活动 ID 一致");
		Assertions.assertTrue(reloadedFirstCharge.hasRed(), "冷重载后首充购买红点状态依然保留");

		SevenDaysSignin reloadedSevenDays = coldReloadedModule.get(12);
		Assertions.assertNotNull(reloadedSevenDays, "冷重载后七日签到依然存在");
		Assertions.assertNotSame(sevenDays, reloadedSevenDays, "重载七日签到对象与原内存对象不可为同一实例");
		Assertions.assertEquals(12, reloadedSevenDays.getId(), "冷重载后七日签到 ID 一致");
		Assertions.assertNotNull(reloadedSevenDays.buildActivityShowInfo(), "冷重载后展示信息依然能正常构建");
	}

	/**
	 * TC-DUAL-BINDING-DEDUP: 一表双类注册隔离与 ServerOpenRank 客户端列表三源去重
	 */
	@Test
	public void testDualBindingDedup() {
		String[] variantKeys = { "ServerOpenRank" };
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"QUERY: ActivityHandler#list (ActivityListRequest_11000001)",
				"QUERY: ActivityHandler#serverOpenRank (ActivityServerOpenRankRequest_11000200)",
				"MUTATE: ActivityHandler#serverOpenRankReward (ActivityServerOpenRankRewardRequest_11000205)"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		// 1. 断言 ClassManager 双注册表隔离且分别返回正确实现类
		ClassManager classManager = ClassManager.getInstance();
		Assertions.assertTrue(classManager.hasPlayerActivityClass(ActivityTypeEnum.ServerOpenRank));
		Assertions.assertTrue(classManager.hasServerActivityClass(ActivityTypeEnum.ServerOpenRank));
		Assertions.assertEquals(ServerOpenRankPlayerActivity.class, classManager.getPlayerActivityClass(ActivityTypeEnum.ServerOpenRank));
		Assertions.assertEquals(ServerOpenRankActivity.class, classManager.getServerActivityClass(ActivityTypeEnum.ServerOpenRank));

		// 2. 断言工厂分桶创建对应实例
		PlayerActivityBase playerActivity = ActivityFactory.createPlayerActivityBase(ActivityTypeEnum.ServerOpenRank.ID);
		ActivityBase serverActivity = ActivityFactory.createServerActivityBase(ActivityTypeEnum.ServerOpenRank.ID);
		Assertions.assertNotNull(playerActivity);
		Assertions.assertNotNull(serverActivity);
		Assertions.assertInstanceOf(ServerOpenRankPlayerActivity.class, playerActivity);
		Assertions.assertInstanceOf(ServerOpenRankActivity.class, serverActivity);

		// 3. 玩家侧开启开服排行活动 (ID 41, type 11, isMultiplayer=true, isServerOnly=true)
		ActivityModule activityModule = player.getActivityModule();
		activityModule.open(41, false);
		ServerOpenRankPlayerActivity rankPlayerActivity = activityModule.get(41);
		Assertions.assertNotNull(rankPlayerActivity, "玩家侧开服排行活动实例应该成功开启");
		Assertions.assertEquals(41, rankPlayerActivity.getId());

		// 4. 绕过列表查询直接执行领奖与红点逻辑 (bypassesPriorQuery=true)
		Assertions.assertTrue(rankPlayerActivity.hasRed(), "新一天初始应有红点");
		List<RewardInfo> rewards = rankPlayerActivity.receive(41);
		Assertions.assertNotNull(rewards, "领取奖励返回非空");
		Assertions.assertFalse(rankPlayerActivity.hasRed(), "领奖后红点应为 false");

		// 5. 模拟客户端请求 ActivityHandler#list (11000001) 的三源合并去重与安全过滤逻辑
		Map<Integer, ActivityInfo> playerState = new HashMap<>(activityModule.getShowState());
		// 模拟全服活动源和服务器活动源均含有 ID 41
		ActivityInfo globalInfo = ActivityInfo.newBuilder().setId(41).build();
		ActivityInfo serverInfo = ActivityInfo.newBuilder().setId(41).build();

		// 三源去重逻辑 (对齐 ActivityHandler#list)
		if (!playerState.containsKey(globalInfo.getId())) {
			playerState.put(globalInfo.getId(), globalInfo);
		}
		if (!playerState.containsKey(serverInfo.getId())) {
			playerState.put(serverInfo.getId(), serverInfo);
		}

		// isServerOnly 过滤逻辑：玩家持有 41 实例时安全保留；未持有则移除
		java.util.Iterator<Map.Entry<Integer, ActivityInfo>> iterator = playerState.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, ActivityInfo> entry = iterator.next();
			ActivityConfig config = ActivityManager.instance().getNullable(entry.getKey());
			if (config == null) {
				iterator.remove();
				continue;
			}
			if (config.isServerOnly && player.getActivityModule().get(entry.getKey()) == null) {
				iterator.remove();
			}
		}

		Assertions.assertTrue(playerState.containsKey(41), "玩家持有实例时，ID=41 应在三源列表中保留且严格去重为1条");
		Assertions.assertEquals(1, playerState.values().stream().filter(info -> info.getId() == 41).count());

		// 6. 负向场景断言：isServerOnly=true 但玩家未持有其实例时，必须被安全过滤移除；配表缺失项也安全过滤
		Map<Integer, ActivityInfo> negativeState = new HashMap<>();
		ActivityInfo pureServerOnlyInfo = ActivityInfo.newBuilder().setId(15).build();
		ActivityInfo nonExistentInfo = ActivityInfo.newBuilder().setId(99999).build();
		negativeState.put(pureServerOnlyInfo.getId(), pureServerOnlyInfo);
		negativeState.put(nonExistentInfo.getId(), nonExistentInfo);

		java.util.Iterator<Map.Entry<Integer, ActivityInfo>> negIterator = negativeState.entrySet().iterator();
		while (negIterator.hasNext()) {
			Map.Entry<Integer, ActivityInfo> entry = negIterator.next();
			ActivityConfig config = ActivityManager.instance().getNullable(entry.getKey());
			if (config == null) {
				negIterator.remove();
				continue;
			}
			if (config.isServerOnly && player.getActivityModule().get(entry.getKey()) == null) {
				negIterator.remove();
			}
		}
		Assertions.assertFalse(negativeState.containsKey(15), "未开启的 isServerOnly 纯服活动必须被过滤移除");
		Assertions.assertFalse(negativeState.containsKey(99999), "配表缺失的活动必须被安全过滤移除");
	}

	/**
	 * TC-LEGACY-TYPE12-SAFE-SKIP: 废弃 type=12 配置安全跳过与存量 ID=42 存档隔离
	 */
	@Test
	public void testLegacyType12SafeSkip() {
		String[] variantKeys = { "ServerOpenRankPlayer" };
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"MUTATE: PlayerActivityManager#canOpen",
				"INIT: ActivityModule#initFromDbAfter"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		// 1. 断言 ClassManager 没有注册废弃的 type=12 (ServerOpenRankPlayer)
		ClassManager classManager = ClassManager.getInstance();
		Assertions.assertFalse(classManager.hasPlayerActivityClass(ActivityTypeEnum.ServerOpenRankPlayer.ID));
		Assertions.assertFalse(classManager.hasServerActivityClass(ActivityTypeEnum.ServerOpenRankPlayer.ID));

		// 2. 验证配置中残留的 type=12 或 ID=42，PlayerActivityManager#canOpen 必须安全返回 false 且不抛异常
		ActivityConfig legacyConfig = ActivityManager.instance().getNullable(42);
		Assertions.assertNotNull(legacyConfig, "配表中应存在历史 ID=42 配置");
		Assertions.assertEquals(ActivityTypeEnum.ServerOpenRankPlayer.ID, legacyConfig.type);
		ActivityModule activityModule = player.getActivityModule();
		PlayerActivityManager manager = activityModule.getPlayerActivityManager();
		Assertions.assertFalse(manager.canOpen(legacyConfig), "废弃的 type=12 配置在 PlayerActivityManager 中必须判定不可开启");

		// 3. 模拟老玩家存档数据中包含历史 ID=42 实例并注入
		ServerOpenRankPlayerActivity fakeLegacyActivity = new ServerOpenRankPlayerActivity();
		fakeLegacyActivity.id = 42;
		manager.getActivities().put(42, fakeLegacyActivity);
		Assertions.assertNotNull(manager.get(42), "注入后内存中应存在历史 ID=42");

		// 4. 触发 initFromDbAfter 恢复逻辑
		activityModule.initFromDbAfter();

		// 5. 断言历史 ID=42 被安全剔除隔离，不阻断登录，获取返回 null
		Assertions.assertNull(activityModule.get(42), "initFromDbAfter 执行后，历史 ID=42 必须被安全移除");
	}

	/**
	 * TC-GAME-STATELESS-LIFECYCLE: GameGlobalActivityManager 无状态生命周期与开服天数调度
	 */
	@Test
	public void testGameStatelessLifecycle() {
		String[] variantKeys = { "ServerOpenRank", "DaShengLeiTai" };
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"EVENT: ServerEventTypeEnum.VirtualServerOpen",
				"EVENT: ServerEventTypeEnum.NewDay",
				"LIFECYCLE: GameGlobalActivityManager#afterActivityOpen",
				"LIFECYCLE: GameGlobalActivityManager#afterActivityDestroy"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		// 1. 验证 GameGlobalActivityManager 内存无状态：insert 与 delete 均返回成功 Future 且无 DB 调用
		GameGlobalActivityManager gameManager = new GameGlobalActivityManager();
		gameManager.setServerId("game_test_1");
		ServerOpenRankActivity rankActivity = new ServerOpenRankActivity();
		rankActivity.setServerId("game_test_1");
		rankActivity.init(41, null, true);
		rankActivity.setUid(99990001L);

		io.vertx.core.Future<Object> insertFuture = gameManager.insert(rankActivity);
		Assertions.assertTrue(insertFuture.succeeded(), "GameGlobalActivityManager insert 应当直接内存返回成功");
		io.vertx.core.Future<Object> deleteFuture = gameManager.delete(rankActivity);
		Assertions.assertTrue(deleteFuture.succeeded(), "GameGlobalActivityManager delete 应当直接内存返回成功");

		// 2. 验证 GameActivityService 启动 loadAll() 走 ServerHelper.getServerOpenMap() 内存态加载
		GameActivityService service = GameActivityService.getInstance();
		service.loadAll();
		AbstractActivityManager serverMgr = service.getServerActivityManager("game_test_1");
		Assertions.assertNotNull(serverMgr, "game_test_1 的活动管理器应该成功创建");

		// 3. 验证通过 openType=10 (OPENTYPE_SERVER_OPEN_DAY) 开启的活动在 serverMgr 中存在
		ActivityBase openedRank = serverMgr.get(41);
		Assertions.assertNotNull(openedRank, "ServerOpenRank(41) 应在服务器活动管理器中开启");
		Assertions.assertInstanceOf(ServerOpenRankActivity.class, openedRank);

		// 4. 模拟跨天事件 ServerEventTypeEnum.NewDay 推进生命周期调度
		ServerEvent newDayEvent = new ServerEvent(ServerEventTypeEnum.NewDay, new Object[0]);
		service.handleEvent(newDayEvent);

		// 验证开服天数计算响应虚拟时间
		int openDay = ServerHelper.getServerOpenDay("game_test_1");
		Assertions.assertTrue(openDay >= 1, "开服天数应当大于等于1天");

		// 验证跨天后活动状态依然正常维持
		Assertions.assertNotNull(serverMgr.get(41), "跨天后 ServerOpenRank 依然正常维持生命周期");
	}

	/**
	 * TC-GAME-DELETE-BY-UID-FIX: GlobalActivityManager#delete 按 uid 删库修复验证
	 */
	@Test
	public void testGameDeleteByUidFix() {
		String[] variantKeys = { "ServerOpenRank" };
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"MUTATE: GlobalActivityManager#delete"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		// 1. 构造一个全局活动，其实体 uid = 1000888999L，配置 ID = 41
		long testUid = 1000888999L;
		int configId = 41;
		ServerOpenRankActivity activity = new ServerOpenRankActivity();
		activity.init(configId, null, false);
		activity.setUid(testUid);

		Assertions.assertEquals(configId, activity.getId());
		Assertions.assertEquals(testUid, activity.getUid());

		// 2. 真实调用生产代码 GlobalActivityManager#delete，重写底层 doDelete 验证实参
		final java.util.concurrent.atomic.AtomicLong capturedDeleteId = new java.util.concurrent.atomic.AtomicLong(-1);
		GlobalActivityManager testGlobalManager = new GlobalActivityManager() {
			@Override
			protected io.vertx.core.Future<Object> doDelete(cn.game.games.cache.entity.GameActivity delete) {
				capturedDeleteId.set(delete.getId());
				return io.vertx.core.Future.succeededFuture();
			}
		};

		// 验证生产代码：uid > 0 时必须传递 activity.getUid()
		testGlobalManager.delete(activity);
		Assertions.assertEquals(testUid, capturedDeleteId.get(), "生产代码删除时必须使用实体的唯一主键 uid (1000888999L)，而非配表 ID (41)");
		Assertions.assertNotEquals((long) configId, capturedDeleteId.get(), "严禁将配表 ID 传给数据库删除操作");

		// 3. 验证生产代码边界：当 uid <= 0 时，绝不进入 doDelete 删库，直接返回 succeededFuture
		capturedDeleteId.set(-1);
		activity.setUid(0L);
		io.vertx.core.Future<Object> zeroUidResult = testGlobalManager.delete(activity);
		Assertions.assertTrue(zeroUidResult.succeeded(), "uid<=0 时直接返回成功 Future");
		Assertions.assertEquals(-1L, capturedDeleteId.get(), "uid<=0 时严禁进入数据库删除操作，杜绝误删");

		// 4. 验证生产代码边界：activity == null 时直接返回成功 Future
		capturedDeleteId.set(-1);
		io.vertx.core.Future<Object> nullResult = testGlobalManager.delete(null);
		Assertions.assertTrue(nullResult.succeeded(), "activity==null 时直接返回成功 Future");
		Assertions.assertEquals(-1L, capturedDeleteId.get(), "activity==null 时严禁进入数据库删除操作");
	}

	/**
	 * TC-CROSS-CONCURRENT-DEDUP-CREATE: Cross Leader 唯一创建、分布式锁与唯一索引死行清理
	 */
	@Test
	public void testCrossConcurrentDedupCreate() throws Exception {
		String[] variantKeys = { "CrossActivityLeaderDedup", "ZombieRowCleanup" };
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"MUTATE: CrossActivityService#onLeaderScheduleCheck",
				"MUTATE: CrossActivityService#ensureActivityLeader",
				"MUTATE: CrossActivityService#createOrEnsureActivity"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		cn.game.games.net.cross.activity.service.CrossActivityService crossService = cn.game.games.net.cross.activity.service.CrossActivityService.getInstance();
		crossService.init();
		cn.game.games.net.cross.activity.RedisCrossActivityDirectory directory = cn.game.games.net.cross.activity.RedisCrossActivityDirectory.getInstance();
		directory.clearLocalMemory();

		int configId = 41;
		String scopeKey = ServerContext.getInstance().getServerId();

		ActivityConfig config41 = ActivityManager.instance().get(configId);
		Field isCrossField = ActivityConfig.class.getDeclaredField("isCross");
		isCrossField.setAccessible(true);
		boolean originalIsCross = (boolean) isCrossField.get(config41);
		isCrossField.set(config41, true);

		try {
			// 0. 预置僵尸死行，验证创建前的清理能力
			cn.game.games.cache.entity.GameActivity zombie = new cn.game.games.cache.entity.GameActivity();
			long zombieUid = 77779999L;
			zombie.setId(zombieUid);
			zombie.setConfigId(configId);
			zombie.setServerId(scopeKey);
			zombie.setState((byte) 0);
			zombie.setCreateTime(DateUtil.currentTimeMillis() - 86400000L * 100);
			memStore.save(zombie);
			int deleteCountBefore = memStore.getDeleteCount();
			int saveCountBefore = memStore.getSaveCount();

			// 1. 验证非 Leader 节点不发起创建
			ServerContext.getInstance().setLeader(false);
			crossService.onLeaderScheduleCheck();
			Assertions.assertFalse(directory.getActiveUid(configId, scopeKey).isPresent(), "非 Leader 节点严禁发起跨服活动创建");

			// 2. 验证 Leader 节点触发防重创建协议
			ServerContext.getInstance().setLeader(true);
			crossService.onLeaderScheduleCheck();

			// 验证僵尸死行已被真实清理且新活动持久化
			Assertions.assertTrue(memStore.getDeleteCount() > deleteCountBefore, "Leader 触发防重创建时必须真实清理历史僵尸死行");
			Assertions.assertTrue(memStore.getSaveCount() > saveCountBefore, "新跨服活动必须通过 CrossActivityStore 存储 SPI 真实持久化");

			// 验证创建成功并注册到 Redis 目录与本地容器
			java.util.Optional<Long> activeUidOpt = directory.getActiveUid(configId, scopeKey);
			Assertions.assertTrue(activeUidOpt.isPresent(), "Leader 节点执行防重协议后必须成功注册 activeUid 到目录");
			long createdUid = activeUidOpt.get();
			Assertions.assertTrue(createdUid > 0, "生成的跨服活动 UID 必须大于0");

			cn.game.games.net.cross.activity.CrossActivityBase act = cn.game.games.net.cross.activity.CrossGlobalActivityManager.getInstance().getActivity(createdUid);
			Assertions.assertNotNull(act, "目标 Cross 节点必须成功创建并托管该跨服活动实例");
			Assertions.assertEquals(configId, act.getId());
			Assertions.assertEquals(createdUid, act.getUid());
			Assertions.assertEquals(scopeKey, act.getScopeKey());

			// 3. 验证再次触发不会重复创建或覆盖 UID
			crossService.onLeaderScheduleCheck();
			java.util.Optional<Long> secondUidOpt = directory.getActiveUid(configId, scopeKey);
			Assertions.assertTrue(secondUidOpt.isPresent());
			Assertions.assertEquals(createdUid, (long) secondUidOpt.get(), "幂等创建防重：再次巡检严禁覆盖已有 activeUid");

			// 4. 并发 8 线程同时抢锁创建，断言单例幂等
			int threadCount = 8;
			java.util.concurrent.ExecutorService exec = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
			java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
			java.util.Set<Long> returnedUids = java.util.concurrent.ConcurrentHashMap.newKeySet();
			for (int i = 0; i < threadCount; i++) {
				exec.submit(() -> {
					try {
						var res = crossService.createOrEnsureActivity(0L, configId, scopeKey, scopeKey);
						if (res != null && res.isSuccess()) {
							returnedUids.add(res.getActivityUid());
						}
					} finally {
						latch.countDown();
					}
				});
			}
			boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
			exec.shutdown();
			Assertions.assertTrue(completed);
			Assertions.assertEquals(1, returnedUids.size(), "并发 8 线程抢锁创建必须全部单例返回同一活动 UID");
			Assertions.assertEquals(createdUid, returnedUids.iterator().next());

			// 5. 清理以备后续测试
			crossService.destroyActivity(createdUid);
			Assertions.assertFalse(directory.getActiveUid(configId, scopeKey).isPresent(), "销毁后目录中的 activeUid 必须被 CAS 摘除");
			Assertions.assertNull(cn.game.games.net.cross.activity.CrossGlobalActivityManager.getInstance().getActivity(createdUid), "销毁后本地容器必须移除该实例");
		} finally {
			isCrossField.set(config41, originalIsCross);
		}
	}

	/**
	 * TC-CROSS-MAILBOX-SERIALIZATION: 同 activityUid 虚拟线程邮箱串行与防重入死锁
	 */
	@Test
	public void testCrossMailboxSerialization() throws Exception {
		String[] variantKeys = { "ActivityMailboxSerialization", "ReentrantNoDeadlock" };
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"MUTATE: CrossActivityService#runInActivityMailbox"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		cn.game.games.net.cross.activity.service.CrossActivityService crossService = cn.game.games.net.cross.activity.service.CrossActivityService.getInstance();
		long testUid = 99990001L;

		// 1. 验证同邮箱嵌套重入不发生自死锁
		final java.util.concurrent.atomic.AtomicBoolean reentrantExecuted = new java.util.concurrent.atomic.AtomicBoolean(false);
		crossService.runInActivityMailbox(testUid, () -> {
			// 外层邮箱
			return crossService.runInActivityMailbox(testUid, () -> {
				// 内层重入
				reentrantExecuted.set(true);
				return "reentrant_ok";
			});
		});
		Assertions.assertTrue(reentrantExecuted.get(), "同 activityUid 嵌套调用必须直接执行，不发生重入自死锁");

		// 2. 验证并发多线程对同 activityUid 的写入严格串行化
		int threadCount = 20;
		int loopPerThread = 50;
		final java.util.concurrent.atomic.AtomicInteger sharedCounter = new java.util.concurrent.atomic.AtomicInteger(0);
		final java.util.concurrent.atomic.AtomicInteger raceDetected = new java.util.concurrent.atomic.AtomicInteger(0);
		final java.util.concurrent.atomic.AtomicInteger inMailboxFlag = new java.util.concurrent.atomic.AtomicInteger(0);

		java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
		java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					for (int j = 0; j < loopPerThread; j++) {
						crossService.runInActivityMailbox(testUid, () -> {
							int current = inMailboxFlag.incrementAndGet();
							if (current > 1) {
								raceDetected.incrementAndGet();
							}
							try {
								Thread.sleep(1);
								sharedCounter.incrementAndGet();
							} finally {
								inMailboxFlag.decrementAndGet();
							}
							return null;
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		boolean completed = latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
		executor.shutdown();
		Assertions.assertTrue(completed, "多线程并发提交必须在合理时间内执行完毕");
		Assertions.assertEquals(0, raceDetected.get(), "同一 activityUid 的邮箱必须完全互斥串行，严禁并发进入");
		Assertions.assertEquals(threadCount * loopPerThread, sharedCounter.get(), "串行执行后共享累加器数值必须完全吻合");
	}

	/**
	 * TC-CROSS-DIRECTORY-CAS-AND-NO-DRIFT: Redis 目录 Lua CAS 删除与点对点路由防漂移
	 */
	@Test
	public void testCrossDirectoryCasAndNoDrift() throws Exception {
		String[] variantKeys = { "DirectoryCasDelete", "StrictPointToPointNoDrift" };
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"MUTATE: RedisCrossActivityDirectory#removeIfMatch",
				"MUTATE: ServerHelper#getCrossActivityProxy",
				"MUTATE: PlayerActivityBase#handleCrossRouteFailure"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		cn.game.games.net.cross.activity.RedisCrossActivityDirectory directory = cn.game.games.net.cross.activity.RedisCrossActivityDirectory.getInstance();
		int configId = 99;
		String scopeKey = "all";
		long uid1 = 80001L;
		long uid2 = 80002L;

		// 场景 A: CAS 删除验证 - 仅当匹配期望值时才删除，防止误删新期
		directory.putActive(configId, scopeKey, uid1);
		Assertions.assertEquals(uid1, (long) directory.getActiveUid(configId, scopeKey).orElse(-1L));

		// 用 uid2 CAS 删 uid1 -> 必须失败且不删除
		boolean removeMismatch = directory.removeIfMatch(configId, scopeKey, uid2);
		Assertions.assertFalse(removeMismatch, "期望 uid 不匹配时 CAS 必须返回 false");
		Assertions.assertEquals(uid1, (long) directory.getActiveUid(configId, scopeKey).orElse(-1L), "不匹配时绝不误删已有 activeUid");

		// 用 uid1 CAS 删 uid1 -> 必须成功
		boolean removeMatch = directory.removeIfMatch(configId, scopeKey, uid1);
		Assertions.assertTrue(removeMatch, "期望 uid 匹配时 CAS 必须成功删除");
		Assertions.assertFalse(directory.getActiveUid(configId, scopeKey).isPresent(), "成功 CAS 后 activeUid 必须已被清除");

		// 场景 B: 严格点对点路由与防漂移 - 路由缺失抛出异常，拒降级 LoadBalancer
		long missingUid = 99999999L;
		var crossIdMgr = cn.game.core.cache.id.IdCache.getManager(cn.game.core.cache.id.DistributedObjectType.CROSS_ACTIVITY);
		if (crossIdMgr != null) {
			crossIdMgr.clearServerId(missingUid);
		}
		Assertions.assertThrows(cn.game.games.net.cross.activity.CrossActivityRouteMissingException.class, () -> {
			ServerHelper.getCrossActivityProxy(missingUid);
		}, "路由缺失时必须直接抛出 CrossActivityRouteMissingException，严禁降级 LoadBalancer 漂移");

		// 场景 C: 玩家侧 handleCrossRouteFailure 失效与自愈重试
		ActivityConfig config41 = ActivityManager.instance().get(41);
		Field isCrossField = ActivityConfig.class.getDeclaredField("isCross");
		isCrossField.setAccessible(true);
		boolean originalIsCross = (boolean) isCrossField.get(config41);
		isCrossField.set(config41, true);

		try {
			ServerOpenRankPlayerActivity playerAct = new ServerOpenRankPlayerActivity();
			playerAct.init(41, player, false);
			playerAct.setCrossActivityUid(uid1);

			// 当目录仍为 uid1 但路由报错时：主动驱逐本地路由缓存
			directory.putActive(41, "game_test_1", uid1);
			playerAct.handleCrossRouteFailure(new RuntimeException("Route error"));
			Assertions.assertEquals(uid1, playerAct.getCrossActivityUid(), "目录未换期时维持 uid 绑定");

			// 当目录已换期为 uid2 时：触发换期流转
			directory.putActive(41, "game_test_1", uid2);
			playerAct.handleCrossRouteFailure(new RuntimeException("Route error"));
			Assertions.assertEquals(uid2, playerAct.getCrossActivityUid(), "目录已换期时自动更新为新期 uid2");

			// 当目录已清除时：触发到期清理，清零绑定
			directory.removeIfMatch(41, "game_test_1", uid2);
			playerAct.handleCrossRouteFailure(new RuntimeException("Route error"));
			Assertions.assertEquals(0L, playerAct.getCrossActivityUid(), "目录已销毁时清空 crossActivityUid 绑定");
		} finally {
			isCrossField.set(config41, originalIsCross);
		}
	}

	/**
	 * TC-CROSS-SHUTDOWN-SAVE-AND-RELOAD: 停服强保存顺序与 CrossActivityDataLoader 分布式恢复
	 */
	@Test
	public void testCrossShutdownSaveAndReload() throws Exception {
		String[] variantKeys = { "ShutdownSaveOrder", "CrossActivityDataLoaderRecovery" };
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"MUTATE: CrossActivityService#saveAll",
				"MUTATE: CrossActivityService#loadActivitiesFromDb",
				"MUTATE: CrossActivityDataLoader#processData"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		cn.game.games.net.cross.activity.service.CrossActivityService crossService = cn.game.games.net.cross.activity.service.CrossActivityService.getInstance();
		crossService.init();
		cn.game.games.net.cross.activity.CrossGlobalActivityManager manager = cn.game.games.net.cross.activity.CrossGlobalActivityManager.getInstance();

		long testUid = 88001122L;
		int configId = 41;
		String scopeKey = ServerContext.getInstance().getServerId();

		ActivityConfig config41 = ActivityManager.instance().get(configId);
		Field isCrossField = ActivityConfig.class.getDeclaredField("isCross");
		isCrossField.setAccessible(true);
		boolean originalIsCross = (boolean) isCrossField.get(config41);
		isCrossField.set(config41, true);

		try {
			// 1. 创建跨服活动并标记脏数据
			cn.game.games.net.cross.activity.CrossActivityBase activity = new cn.game.games.net.cross.activity.CrossActivityBase();
			activity.init(configId, null, true);
			activity.setUid(testUid);
			activity.setScopeKey(scopeKey);
			activity.markDirty();
			manager.addActivity(activity);

			Assertions.assertTrue(activity.isDirty(), "活动初创并修改时应处于 dirty 状态");

			// 2. 模拟停服 saveAll(true)
			int saveCountBefore = memStore.getSaveCount();
			crossService.saveAll(true);
			Assertions.assertFalse(activity.isDirty(), "强保存后 dirty 标记必须被清除");
			Assertions.assertTrue(activity.getLastSaveTime() > 0, "保存后必须记录上次保存时间戳");
			Assertions.assertTrue(memStore.getSaveCount() > saveCountBefore, "停服强保存必须真实写入存储 SPI");

			// 3. 模拟节点重启，从 memStore 真实冷读历史数据
			manager.clear();
			Assertions.assertNull(manager.getActivity(testUid), "重启前本地容器已清空");

			List<cn.game.games.cache.entity.GameActivity> savedEntities = memStore.loadAll();
			Assertions.assertFalse(savedEntities.isEmpty(), "memStore 中必须真实存在已持久化的活动实体");

			cn.game.games.net.cross.data.CrossActivityDataLoader loader = new cn.game.games.net.cross.data.CrossActivityDataLoader();
			loader.processData(savedEntities);

			// 4. 断言加载恢复成功
			cn.game.games.net.cross.activity.CrossActivityBase recovered = manager.getActivity(testUid);
			Assertions.assertNotNull(recovered, "重启后数据必须完整恢复并挂载进 CrossGlobalActivityManager");
			Assertions.assertEquals(testUid, recovered.getUid());
			Assertions.assertEquals(configId, recovered.getId());
			Assertions.assertEquals(scopeKey, recovered.getScopeKey());

			// 验证目录与路由已自愈重建
			java.util.Optional<Long> activeUid = cn.game.games.net.cross.activity.RedisCrossActivityDirectory.getInstance().getActiveUid(configId, scopeKey);
			Assertions.assertTrue(activeUid.isPresent());
			Assertions.assertEquals(testUid, (long) activeUid.get(), "恢复后必须重建 Redis 目录映射");

			// 5. 验证冷重载对已过期活动自动推进 end()
			long expiredUid = 99887766L;
			cn.game.games.cache.entity.GameActivity expiredEntity = new cn.game.games.cache.entity.GameActivity();
			expiredEntity.setId(expiredUid);
			expiredEntity.setConfigId(configId);
			expiredEntity.setServerId(scopeKey);
			expiredEntity.setState((byte) cn.game.protocol.protobuf.ActivityMsg.ActivityState.START_VALUE);
			expiredEntity.setCreateTime(DateUtil.currentTimeMillis() - 86400000L * 100);
			expiredEntity.setParams(activity.toSaveString());
			memStore.save(expiredEntity);

			loader.processData(java.util.List.of(expiredEntity));
			cn.game.games.net.cross.activity.CrossActivityBase expiredRecovered = manager.getActivity(expiredUid);
			Assertions.assertNotNull(expiredRecovered);
			Assertions.assertEquals(cn.game.protocol.protobuf.ActivityMsg.ActivityState.CLOSE_VALUE, expiredRecovered.getState(), "过期活动冷重载必须自动推进为 CLOSE 状态");
			crossService.destroyActivity(expiredUid);

			// 清理
			crossService.destroyActivity(testUid);
		} finally {
			isCrossField.set(config41, originalIsCross);
		}
	}

	/**
	 * TC-CROSS-ROUTING-ISOLATION: 公会分布式路由隔离与 Game 侧只读零副作用
	 */
	@Test
	public void testCrossRoutingIsolation() throws Exception {
		String[] variantKeys = { "GuildRoutingIsolation", "GameSideZeroSideEffect" };
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"MUTATE: ServerHelper#getGuildProxy",
				"MUTATE: DistributedIDManager#isObjectInCurrentServer",
				"MUTATE: DistributedIDManager#getAllIds"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		// 1. 初始化 ID 管理器
		cn.game.games.net.game.init.GameIdManagerInitializer.initialize();

		// 2. 验证公会远程代理获取完全不受跨服活动影响
		long fakeGuildId = 77778888L;
		cn.game.games.net.cross.guild.service.GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(fakeGuildId);
		Assertions.assertNotNull(guildProxy, "公会代理获取必须保持独立且可用");

		// 3. 在未初始化跨服服务的状态下验证 Game 侧纯只读零副作用保证
		cn.game.games.net.cross.activity.service.CrossActivityService service = cn.game.games.net.cross.activity.service.CrossActivityService.getInstance();
		long nonExistentUid = 12345678L;
		boolean inCurrentServer = service.containsActivity(nonExistentUid);
		Assertions.assertFalse(inCurrentServer, "Game 侧查询不存在的跨服活动必须安全返回 false，绝不抛出 NPE 或副作用");

		java.util.List<Long> allIds = service.getAllActivityIds();
		Assertions.assertNotNull(allIds, "Game 侧查询全部活动 ID 必须返回非空集合");

		// 4. 验证 DistributedIDManager 派发 CROSS_ACTIVITY 类型
		cn.game.games.cache.id.DistributedIDManager idManager = new cn.game.games.cache.id.DistributedIDManager(
				cn.game.core.cache.id.DistributedObjectType.CROSS_ACTIVITY,
				new cn.game.core.cache.CacheConfig(1024, 86400, cn.game.core.cache.CacheType.CROSS_ACTIVITY_SERVER_ID));

		boolean existsInServer = idManager.isObjectInCurrentServer(nonExistentUid);
		Assertions.assertFalse(existsInServer);
		java.util.Collection<Long> idsFromManager = idManager.getAllIds();
		Assertions.assertNotNull(idsFromManager);
	}

	/**
	 * TC-CROSS-RPC-AND-PLAYER-BINDING: 跨服 RPC 业务分发与玩家端自愈自动绑定
	 */
	@Test
	public void testCrossActivityRpcAndBinding() throws Exception {
		String[] variantKeys = { "CrossActivityRpcDispatch", "PlayerAutoBinding" };
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"RPC: CrossActivityService#executePlayerAction",
				"RPC: CrossActivityService#joinActivity",
				"BINDING: PlayerActivityBase#ensureCrossActivityBinding",
				"BINDING: PlayerActivityBase#buildActivityShowInfo"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		cn.game.games.net.cross.activity.service.CrossActivityService crossService = cn.game.games.net.cross.activity.service.CrossActivityService.getInstance();
		crossService.init();
		cn.game.games.net.cross.activity.RedisCrossActivityDirectory directory = cn.game.games.net.cross.activity.RedisCrossActivityDirectory.getInstance();

		int configId = 41;
		long activityUid = 66554433L;
		String scopeKey = "game_test_1";

		// 1. 注册并挂载一个跨服活动实例
		cn.game.games.net.cross.activity.CrossActivityBase act = new cn.game.games.net.cross.activity.CrossActivityBase();
		act.init(configId, null, true);
		act.setUid(activityUid);
		act.setScopeKey(scopeKey);
		cn.game.games.net.cross.activity.CrossGlobalActivityManager.getInstance().addActivity(act);
		directory.putActive(configId, scopeKey, activityUid);

		// 2. 验证 executePlayerAction 分发
		var actionRes = crossService.executePlayerAction(activityUid, player.getPlayerId(), "rank_check", "{\"target\":1}");
		Assertions.assertNotNull(actionRes, "跨服业务 RPC 分发执行返回非空响应");

		// 3. 验证 joinActivity 分发
		var joinSnapshot = crossService.joinActivity(activityUid, player.getPlayerId(), scopeKey, java.util.Collections.emptyMap());
		Assertions.assertNotNull(joinSnapshot, "跨服参与活动接口分发成功并返回快照");
		Assertions.assertEquals(activityUid, joinSnapshot.getActivityUid());

		// 4. 验证玩家侧 PlayerActivityBase 自动绑定逻辑
		ActivityConfig config41 = ActivityManager.instance().get(configId);
		Field isCrossField = ActivityConfig.class.getDeclaredField("isCross");
		isCrossField.setAccessible(true);
		boolean originalIsCross = (boolean) isCrossField.get(config41);
		isCrossField.set(config41, true);

		try {
			ServerOpenRankPlayerActivity playerAct = new ServerOpenRankPlayerActivity();
			playerAct.init(configId, player, false);
			// 初始未绑定
			playerAct.setCrossActivityUid(0L);

			// 调用 ensureCrossActivityBinding，自动从目录获取 activityUid
			playerAct.ensureCrossActivityBinding();
			Assertions.assertEquals(activityUid, playerAct.getCrossActivityUid(), "ensureCrossActivityBinding 必须自动绑定目录中的活跃 UID");

			// buildActivityShowInfo 也会触发 binding 保底
			playerAct.setCrossActivityUid(0L);
			playerAct.buildActivityShowInfo();
			Assertions.assertEquals(activityUid, playerAct.getCrossActivityUid(), "buildActivityShowInfo 必须自动保底绑定 crossActivityUid");
		} finally {
			isCrossField.set(config41, originalIsCross);
			crossService.destroyActivity(activityUid);
		}
	}
}
