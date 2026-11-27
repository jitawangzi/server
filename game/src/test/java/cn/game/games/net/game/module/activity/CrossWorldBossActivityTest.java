package cn.game.games.net.game.module.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.protobuf.Message;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.games.cache.entity.GameActivity;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.common.module.activity.CrossWorldBossConfigValidator;
import cn.game.games.net.common.module.activity.DefaultActivityScopeResolver;
import cn.game.games.net.cross.activity.CrossGlobalActivityManager;
import cn.game.games.net.cross.activity.DefaultCrossActivityStore;
import cn.game.games.net.cross.activity.InMemoryCrossActivityStore;
import cn.game.games.net.cross.activity.RedisCrossActivityDirectory;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.cross.activity.dto.CrossActivitySnapshot;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.AcceptanceResetRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.AttackRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.AttackResponse;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.ClaimKillRewardRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.ClaimMilestoneRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.QueryRankRequest;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.QueryRankResponse;
import cn.game.games.net.cross.activity.impl.CrossWorldBossActivity;
import cn.game.games.net.cross.activity.impl.CrossWorldBossActivity.CrossBossDamageRecord;
import cn.game.games.net.cross.activity.service.CrossActivityService;
import cn.game.games.net.cross.data.CrossActivityDataLoader;
import cn.game.games.net.game.gm.acceptance.AcceptanceRunMode;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceEngine;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceReport;
import cn.game.games.net.game.gm.acceptance.SandboxExecutionGuard;
import cn.game.games.net.game.gm.acceptance.suites.CrossWorldBossModuleAcceptanceSuite;
import cn.game.games.net.game.gm.proto.VirtualGameClient;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.init.GameIdManagerInitializer;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.activity.impl.player.CrossWorldBossPlayerActivity;
import cn.game.games.net.game.module.activity.impl.player.FirstChargeActivity;
import cn.game.games.net.game.module.activity.impl.player.ServerOpenRankPlayerActivity;
import cn.game.games.net.game.module.activity.impl.player.SevenDaysSignin;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.ActivityCrossWorldBossConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ActivityCrossWorldBossManager;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.generated.manager.ActivityServerOpenRankManager;
import cn.game.protocol.generated.manager.FirstChargeManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.MailManager;
import cn.game.protocol.generated.manager.RankManager;
import cn.game.protocol.generated.manager.SevenDaysSigninManager;
import cn.game.protocol.generated.manager.WorldBossRewardManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossAttackRequest_11000403;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossAttackResponse_11000404;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossBuyCountRequest_11000407;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossBuyCountResponse_11000408;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossInfoRequest_11000401;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossInfoResponse_11000402;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossRankRequest_11000409;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossRankResponse_1100040a;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossRewardRequest_11000405;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossWorldBossRewardResponse_11000406;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityListRequest_11000001;
import cn.game.protocol.protobuf.ActivityMsg.ActivityListResponse_11000002;
import cn.game.protocol.protobuf.ActivityMsg.ActivityRedPointRequest_11000003;
import cn.game.protocol.protobuf.ActivityMsg.ActivityRedPointResponse_11000004;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.CborSnappyUtil;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.ObjUtil;
import cn.game.util.ServerType;
import cn.game.util.file.ResourceListener;

/**
 * 跨服世界Boss与既有活动系统表征回归测试套件
 * <p>
 * 职责边界：先锁定既有纯玩家活动、双端去重列表与单服世界Boss基线行为，
 * 后续再逐步补充跨服世界Boss配置校验、战区隔离、并发击杀与冷重载契约验证。
 * </p>
 *
 * @author shuyongqiang
 */
@Isolated
public class CrossWorldBossActivityTest {

	/** 测试套件日志器 */
	private static final Logger log = LoggerFactory.getLogger(CrossWorldBossActivityTest.class);
	/** 沙盒测试玩家 ID（落在 PlayerSaveCoordinator.SANDBOX_PLAYER_ID_MIN 沙盒号段内且避开 ModuleAcceptanceEngine 占用段） */
	private static final long TEST_PLAYER_ID = 900_888_900_001L;
	/** 测试逻辑服 ID */
	private static final String TEST_SERVER_ID = "game_test_cwb_1";

	/** 备份的服务器初始运行模式，测试结束后恢复 */
	private RunMode originalRunMode;
	/** 备份的服务器初始 Leader 状态 */
	private boolean originalLeader;
	/** 当前测试用例构造的在线玩家实例 */
	private Player player;
	/** 用例内额外创建的待清理玩家 ID 列表 */
	private final List<Long> extraPlayerIds = new ArrayList<>();
	/** 跨服活动内存存储测试桩 */
	private InMemoryCrossActivityStore memStore;
	/** 拦截服务端下发协议包的虚拟客户端桩 */
	private CapturingGameClient capturingClient;

	/**
	 * 初始化单测运行上下文、静态配表及在线测试玩家实例
	 *
	 * @throws Exception 反射恢复或初始化失败时抛出
	 */
	@BeforeEach
	public void setUp() throws Exception {
		Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
		runModeField.setAccessible(true);
		originalRunMode = (RunMode) runModeField.get(ServerContext.getInstance());
		runModeField.set(ServerContext.getInstance(), RunMode.DEV);
		originalLeader = ServerContext.getInstance().isLeader();
		ServerContext.getInstance().setEventBus(ServerEventBus.getInstance());
		ServerContext.getInstance().setServerId(TEST_SERVER_ID);

		VirtualServerView testView = new VirtualServerView();
		testView.ID = TEST_SERVER_ID;
		testView.openTime = DateUtil.nowDateTime().minusDays(2);
		ServerHelper.setTestServerOpenMap(Map.of(TEST_SERVER_ID, testView));

		memStore = new InMemoryCrossActivityStore();
		CrossActivityService.getInstance().setStore(memStore);

		ClassManager.getInstance().init();
		GameIdManagerInitializer.initialize();
		GlobalConst.instance().load();
		ActivityManager.instance().load();
		ActivityCrossWorldBossManager.instance().load();
		MailManager.instance().load();
		FirstChargeManager.instance().load();
		SevenDaysSigninManager.instance().load();
		ActivityServerOpenRankManager.instance().load();
		RankManager.instance().load();
		ItemManager.instance().load();
		WorldBossRewardManager.instance().load();

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(TEST_PLAYER_ID);
		data.setUid(TEST_PLAYER_ID);
		data.setName("CwbRegressionPlayer");
		data.setServerId(TEST_SERVER_ID);
		data.setLevel(10);
		data.setAccountId("acc_cwb_regression_1");
		data.setCreateDate(DateUtil.getTimeByPattern(
				new java.util.Date(DateUtil.currentTimeMillis() - 86400000L * 2), DateUtil.pattern_en));

		player = new Player(data);
		player.setAccount(buildTestAccount("acc_cwb_regression_1"));
		player.initModule(new HashMap<String, BasePlayerModule>());
		if (player.getPlayerModule() != null) {
			player.getPlayerModule().getExpLevelMap().setValue(Asset.playerExp.ID, 10);
		}
		capturingClient = new CapturingGameClient(TEST_PLAYER_ID);
		player.setGameClient(capturingClient);
		player.setOnline(true);
		player.setIslogouting(false);
		PlayerManager.getInstance().initAdd(player);
	}

	/**
	 * 清理测试玩家、虚拟服映射与跨服活动内存态，避免污染后续用例
	 *
	 * @throws Exception 清理运行模式失败时抛出
	 */
	@AfterEach
	public void tearDown() throws Exception {
		for (Long extraId : extraPlayerIds) {
			try {
				if (PlayerManager.getInstance().getPlayer(extraId) != null) {
					PlayerManager.getInstance().deletePlayer(extraId);
				}
			} catch (Throwable t) {
				log.warn("清理额外测试玩家失败: playerId={}", extraId, t);
			}
		}
		extraPlayerIds.clear();
		if (player != null) {
			try {
				PlayerManager.getInstance().deletePlayer(player.getPlayerId());
				Assertions.assertNull(PlayerManager.getInstance().getPlayer(player.getPlayerId()),
						"清理后玩家缓存必须被彻底移除");
			} catch (Throwable t) {
				log.warn("清理测试玩家失败: playerId={}", player.getPlayerId(), t);
				throw t;
			}
		}
		ActivityStateManager.getInstance().removeState(51);
		ServerHelper.setTestServerOpenMap(null);
		CrossGlobalActivityManager.getInstance().clear();
		RedisCrossActivityDirectory.getInstance().clearLocalMemory();
		if (memStore != null) {
			memStore.clear();
		}
		CrossActivityService.getInstance().setStore(new DefaultCrossActivityStore());
		CrossActivityService.getInstance().setScopeResolver(DefaultActivityScopeResolver.getInstance());
		CrossWorldBossActivity.resetMailSender();
		DateUtil.resetTimeOffset();
		ServerContext.getInstance().setLeader(originalLeader);
		if (originalRunMode != null) {
			Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
			runModeField.setAccessible(true);
			runModeField.set(ServerContext.getInstance(), originalRunMode);
		}
	}

	/**
	 * TC-CWB-CONFIG-PROTO-AND-LEGACY-REGRESSION:
	 * 锁定既有纯玩家活动、双端去重列表与单服世界Boss基线行为（未先调用列表查询直接写入、跨天冷重载与正式协议分发）
	 */
	@Test
	public void testConfigProtoAndLegacyRegression() {
		String[] variantKeys = {
				"FirstCharge", "SevenDaysSignin", "ServerOpenRank", "WorldBossBattle_SingleServer"
		};
		Assertions.assertTrue(variantKeys.length > 0);

		String[] entryPointIds = {
				"QUERY: ActivityHandler#list (ActivityListRequest_11000001)_",
				"QUERY: ActivityHandler#redPoint (ActivityRedPointRequest_11000003)_"
		};
		Assertions.assertTrue(entryPointIds.length > 0);

		// 未先调用 ActivityHandler#list 查询接口前直接开启并执行写操作，验证绕过前置查询的写路径与持久化闭环
		ActivityModule activityModule = player.getActivityModule();
		Assertions.assertNotNull(activityModule, "玩家活动模块不能为空");

		activityModule.open(10, false);
		activityModule.open(12, false);
		activityModule.open(41, false);

		FirstChargeActivity firstCharge = activityModule.get(10);
		SevenDaysSignin sevenDays = activityModule.get(12);
		ServerOpenRankPlayerActivity serverOpenRank = activityModule.get(41);
		Assertions.assertNotNull(firstCharge, "首充活动(ID=10)应成功开启");
		Assertions.assertNotNull(sevenDays, "七日签到活动(ID=12)应成功开启");
		Assertions.assertNotNull(serverOpenRank, "开服排行活动(ID=41)应成功开启");

		firstCharge.buy(1);
		Assertions.assertNotNull(sevenDays.receive(0), "七日签到首次领奖应返回非空奖励");

		// 触发跨天事件以验证跨天重置后仍保持一致；ServerOpenRankPlayerActivity#newDay 会重置每日领奖标记，故跨天后再执行当日领奖
		activityModule.handleEvent(new PlayerEvent(EventTypeEnum.NewDay));
		Assertions.assertNotNull(serverOpenRank.receive(41), "开服排行每日领奖应返回非空奖励");

		// 对整个玩家模块映射表执行 CBOR+Snappy 压缩与解压冷重载，确保验证反序列化出的全新实例
		ActivityModule reloadedModule = coldReloadActivityModule(player);
		Assertions.assertNotNull(reloadedModule, "冷重载后的活动模块不能为空");
		Assertions.assertNotSame(activityModule, reloadedModule, "冷重载后的活动模块必须为全新反序列化实例");

		FirstChargeActivity reloadedFirstCharge = reloadedModule.get(10);
		SevenDaysSignin reloadedSevenDays = reloadedModule.get(12);
		ServerOpenRankPlayerActivity reloadedServerOpenRank = reloadedModule.get(41);
		Assertions.assertNotNull(reloadedFirstCharge, "冷重载后首充活动(ID=10)应存在");
		Assertions.assertNotNull(reloadedSevenDays, "冷重载后七日签到(ID=12)应存在");
		Assertions.assertNotNull(reloadedServerOpenRank, "冷重载后开服排行(ID=41)应存在");

		Assertions.assertNotSame(firstCharge, reloadedFirstCharge, "冷重载首充实例不能与原内存对象相同");
		Assertions.assertNotSame(sevenDays, reloadedSevenDays, "冷重载七日签到实例不能与原内存对象相同");
		Assertions.assertNotSame(serverOpenRank, reloadedServerOpenRank, "冷重载开服排行实例不能与原内存对象相同");

		Assertions.assertTrue(reloadedFirstCharge.hasRed(), "冷重载后首充购买红点应为 true");
		Assertions.assertEquals(12, reloadedSevenDays.getId(), "冷重载后七日签到活动 ID 应保持 12");
		Assertions.assertFalse(reloadedServerOpenRank.hasRed(), "冷重载后开服排行已领当日奖励，红点应为 false");

		// 通过真实 ActivityHandler 实例与测试 NetClient 桩分发正式协议，捕获真实响应包而非仅调用静态辅助方法
		ActivityHandler activityHandler = new ActivityHandler();
		activityHandler.inititialize();

		activityHandler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityListRequest_11000001, ActivityListRequest_11000001.getDefaultInstance(), -1));
		ActivityListResponse_11000002 listResp = capturingClient.findFirstResponse(
				PbProtocol.ActivityListResponse_11000002, ActivityListResponse_11000002.class);
		Assertions.assertNotNull(listResp, "ActivityHandler#list 必须下发 ActivityListResponse_11000002");
		long count41 = listResp.getActivitysList().stream().map(ActivityInfo::getId).filter(id -> id == 41).count();
		Assertions.assertEquals(1L, count41, "活动列表响应中 ID=41 必须存在且严格去重为 1 条");

		ActivityRedPointRequest_11000003 redReq = ActivityRedPointRequest_11000003.newBuilder()
				.addIds(10)
				.addIds(12)
				.addIds(41)
				.build();
		activityHandler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityRedPointRequest_11000003, redReq, -2));
		ActivityRedPointResponse_11000004 redResp = capturingClient.findFirstResponse(
				PbProtocol.ActivityRedPointResponse_11000004, ActivityRedPointResponse_11000004.class);
		Assertions.assertNotNull(redResp, "ActivityHandler#redPoint 必须下发 ActivityRedPointResponse_11000004");
		Assertions.assertEquals(3, redResp.getIsRedCount(), "红点响应数组长度应与请求活动 ID 数量一致");
		Assertions.assertTrue(redResp.getIsRed(0), "活动 10 红点协议返回应为 true");
		Assertions.assertFalse(redResp.getIsRed(2), "活动 41 红点协议返回应为 false");

		// 锁定单服世界 Boss 配表、操作日志类型与协议号段基线，确保后续跨服改造零侵入
		Assertions.assertFalse(WorldBossRewardManager.instance().list().isEmpty(), "单服 WorldBossRewardManager 配置列表不能为空");
		Assertions.assertEquals(171, OpType.WorldBoss.getId(), "单服 OpType.WorldBoss.getId() 必须保持 171");
		Assertions.assertNotEquals(0, PbProtocol.BattleWorldBossInfoRequest_13000301,
				"单服世界Boss信息协议号 13000301 必须保持非零注册");

		// 校验跨服世界Boss枚举号段、错误码、Proto 常量与 17 字段配置表骨架
		Assertions.assertEquals(17, ActivityTypeEnum.CrossWorldBoss.ID, "ActivityTypeEnum.CrossWorldBoss.ID 必须为 17");
		Assertions.assertEquals(310, OpType.CrossWorldBoss.getId(), "OpType.CrossWorldBoss.getId() 必须为 310");
		Assertions.assertEquals(455, ErrorMsgEnum.cross_world_boss_killed.ID, "错误码 455 必须为 cross_world_boss_killed");
		Assertions.assertEquals(456, ErrorMsgEnum.cross_world_boss_not_killer.ID, "错误码 456 必须为 cross_world_boss_not_killer");
		Assertions.assertEquals(17, ActivityCrossWorldBossConfig.class.getDeclaredFields().length,
				"ActivityCrossWorldBossConfig 必须严格保留 17 个原始配表字段");

		Assertions.assertEquals(0x11000401, PbProtocol.ActivityCrossWorldBossInfoRequest_11000401);
		Assertions.assertEquals(0x11000402, PbProtocol.ActivityCrossWorldBossInfoResponse_11000402);
		Assertions.assertEquals(0x11000403, PbProtocol.ActivityCrossWorldBossAttackRequest_11000403);
		Assertions.assertEquals(0x11000404, PbProtocol.ActivityCrossWorldBossAttackResponse_11000404);
		Assertions.assertEquals(0x11000405, PbProtocol.ActivityCrossWorldBossRewardRequest_11000405);
		Assertions.assertEquals(0x11000406, PbProtocol.ActivityCrossWorldBossRewardResponse_11000406);
		Assertions.assertEquals(0x11000407, PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407);
		Assertions.assertEquals(0x11000408, PbProtocol.ActivityCrossWorldBossBuyCountResponse_11000408);
		Assertions.assertEquals(0x11000409, PbProtocol.ActivityCrossWorldBossRankRequest_11000409);
		Assertions.assertEquals(0x1100040a, PbProtocol.ActivityCrossWorldBossRankResponse_1100040a);

		// 验证正向全表校验、热重载回调与版本化 zoneOverrides 缓存
		ActivityCrossWorldBossConfig liveBossCfg = ActivityCrossWorldBossManager.instance().get(51);
		Assertions.assertEquals(100_000_000L, liveBossCfg.maxHp);
		Assertions.assertEquals(44, liveBossCfg.rankMailId);
		Assertions.assertTrue(CrossWorldBossConfigValidator.isValidRewardId(Asset.diamond.ID), "Asset 元宝 ID 必须通过资源合法性校验");
		Assertions.assertTrue(CrossWorldBossConfigValidator.isValidRewardId(200001), "Item 道具 ID 必须通过资源合法性校验");
		Assertions.assertFalse(CrossWorldBossConfigValidator.isValidRewardId(99999999), "不存在的资源 ID 必须被拦截");
		CrossWorldBossConfigValidator.validateAll();
		Assertions.assertTrue(CrossWorldBossConfigValidator.resolveZoneOverrides(liveBossCfg).isEmpty(),
				"默认配置 zoneOverrides 为空时应返回空不可变映射");

		verifyHotReloadAndBadConfigValidation();
	}

	/**
	 * 验证热重载回调触发以及 6 类坏配表抛出 IllegalStateException（测试结束自动还原合法配置）
	 */
	@SuppressWarnings("unchecked")
	private void verifyHotReloadAndBadConfigValidation() {
		ActivityCrossWorldBossManager manager = ActivityCrossWorldBossManager.instance();
		AtomicInteger reloadHitCount = new AtomicInteger(0);
		Runnable validatorConsumer = CrossWorldBossConfigValidator::validateAll;
		Runnable reloadSpy = reloadHitCount::incrementAndGet;
		long versionBefore = manager.currentVersion();
		List<Runnable> registeredConsumers = null;
		try {
			Field consumersField = ResourceListener.class.getDeclaredField("reloadConsumers");
			consumersField.setAccessible(true);
			registeredConsumers = (List<Runnable>) consumersField.get(manager);

			manager.addReloadConsumer(validatorConsumer);
			manager.addReloadConsumer(reloadSpy);
			manager.reload();
			Assertions.assertEquals(versionBefore + 1, manager.currentVersion(), "reload() 后配置版本号必须递增 1");
			Assertions.assertEquals(1, reloadHitCount.get(), "reload() 必须触发已注册的热重载回调");

			// 校验带静态覆盖配置的不可变解析结果
			ActivityCrossWorldBossConfig overrideCfg = buildBossConfig(Map.of("zoneOverrides", "s1:2;s3:5"));
			Map<String, Integer> overrideMap = CrossWorldBossConfigValidator.resolveZoneOverrides(overrideCfg);
			Assertions.assertEquals(2, overrideMap.get("s1"));
			Assertions.assertEquals(5, overrideMap.get("s3"));
			Assertions.assertThrows(UnsupportedOperationException.class, () -> overrideMap.put("s4", 1),
					"resolveZoneOverrides 返回的映射必须不可变");

			// 跨表存在性缺失：Activity 表存在 ID=51 但业务表为空
			assertBadConfigMapThrows(Map.of(), "ID=51");
			// 数值边界非法：maxHp <= 0 与 maxSingleDamage > maxHp
			assertBadConfigThrows(Map.of("maxHp", "0"), "maxHp");
			assertBadConfigThrows(Map.of("maxSingleDamage", "100000001"), "maxSingleDamage");
			// 静态战区覆盖非法：重复 serverId 或格式错误
			assertBadConfigThrows(Map.of("zoneOverrides", "s1:1;s1:2"), "zoneOverrides");
			assertBadConfigThrows(Map.of("zoneOverrides", "bad_segment"), "zoneOverrides");
			// 购买消耗物品 ID 不存在或维度错误
			assertBadConfigThrows(Map.of("buyCosts", "99999999;10"), "buyCosts");
			// 里程碑非递增或越界 (> 10000)
			assertBadConfigThrows(Map.of("milestoneThresholds", "5000;2000;8000;10000"), "milestoneThresholds");
			assertBadConfigThrows(Map.of("milestoneThresholds", "2000;5000;8000;10001"), "milestoneThresholds");
			// 排行分段不连续或结算邮件模板不存在
			assertBadConfigThrows(Map.of("rankRanges", "1;1|3;5|6;10|11;50|51;999999"), "rankRanges");
			assertBadConfigThrows(Map.of("rankMailId", "999999"), "rankMailId");
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			log.error("验证热重载与坏配表校验失败", e);
			throw new RuntimeException("验证热重载与坏配表校验失败", e);
		} finally {
			if (registeredConsumers != null) {
				registeredConsumers.remove(reloadSpy);
				registeredConsumers.remove(validatorConsumer);
			}
			manager.load();
			CrossWorldBossConfigValidator.validateAll();
		}
	}

	/**
	 * TC-CWB-END-SETTLE-MAIL-IDEMPOTENCY:
	 * 验证 CrossWorldBossActivity 同分先达成排名排序、saveOrThrow 故障回滚 rankSettled=false 且零发邮、
	 * 恢复后先持久化 rankSettled=true 再异步发送 2 封 mailId=44（未达门槛 P3 发 0 封）、重复 end() 幂等、
	 * CLOSE 期允许 query_rank / 拒绝 attack(not_open=58)、以及战区分组解析与诊断快照扩展字段。
	 */
	@Test
	public void testEndSettleMailIdempotency() {
		// 验证战区分组解析器四级优先级与去重合并
		ActivityConfig actCfg = ActivityManager.instance().get(51);
		Assertions.assertNotNull(actCfg, "活动 51 配置必须存在");
		DefaultActivityScopeResolver resolver = DefaultActivityScopeResolver.getInstance();
		Assertions.assertTrue(DefaultActivityScopeResolver.isZoneGroupedActivity(actCfg), "活动 51 必须识别为战区分组活动");
		Assertions.assertEquals(DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY,
				resolver.resolveScopeKey(actCfg, DefaultActivityScopeResolver.SANDBOX_SERVER_ID),
				"非生产模式下 sandbox_cwb 必须固定映射到 group_9999");

		VirtualServerView s1 = new VirtualServerView("s1", "S1", 1, 1000, 1, DateUtil.nowDateTime());
		VirtualServerView s2 = new VirtualServerView("s2", "S2", 2, 1000, 2, DateUtil.nowDateTime());
		VirtualServerView s3 = new VirtualServerView("s3", "S3", 5, 1000, 5, DateUtil.nowDateTime());
		VirtualServerView sNoSeq = new VirtualServerView("s_no_seq", "NoSeq", 9, 1000, null, DateUtil.nowDateTime());
		ServerHelper.setTestServerOpenMap(Map.of("s1", s1, "s2", s2, "s3", s3, "s_no_seq", sNoSeq));
		Assertions.assertEquals("group_1", resolver.resolveScopeKey(actCfg, "s1"));
		Assertions.assertEquals("group_1", resolver.resolveScopeKey(actCfg, "s2"));
		Assertions.assertEquals("group_2", resolver.resolveScopeKey(actCfg, "s3"));
		Assertions.assertEquals("group_1", resolver.resolveScopeKey(actCfg, "s_no_seq"));
		Set<String> activeScopes = resolver.resolveActiveScopeKeys(actCfg, List.of(s1, s2, s3));
		Assertions.assertEquals(Set.of("group_1", "group_2"), activeScopes, "战区集合应去重合并为 group_1 与 group_2");
		Assertions.assertEquals(Set.of("group_1"), resolver.resolveActiveScopeKeys(actCfg, List.of()),
				"空区服列表必须兜底返回 group_1");

		// 初始化 CrossWorldBossActivity 权威实例并注册到 CrossGlobalActivityManager
		long bossUid = 990005101L;
		CrossWorldBossActivity bossAct = new CrossWorldBossActivity();
		bossAct.setUid(bossUid);
		bossAct.setScopeKey("group_1");
		bossAct.setServerId("group_1");
		bossAct.init(51, null, true);
		CrossGlobalActivityManager.getInstance().addActivity(bossAct);
		CrossActivityService crossService = CrossActivityService.getInstance();

		// 校验只读 join 与快照 extra 诊断字段
		Assertions.assertTrue(bossAct.join(900_888_900_101L, "s1", null), "START 期 join 准入应返回 true");
		bossAct.clearDirty();
		Assertions.assertTrue(bossAct.join(900_888_900_101L, "s1", null));
		Assertions.assertFalse(bossAct.isDirty(), "CrossWorldBossActivity#join 只读校验绝不能触发 markDirty");

		long p1Id = 900_888_900_101L;
		long p2Id = 900_888_900_102L;
		long p3Id = 900_888_900_103L;

		Map<Long, List<Integer>> sentMailIdsByPlayer = new ConcurrentHashMap<>();
		Map<Long, List<Goods>> sentGoodsByPlayer = new ConcurrentHashMap<>();
		AtomicInteger totalMailCalls = new AtomicInteger(0);
		List<Boolean> rankSettledInStoreWhenMailSent = new ArrayList<>();

		try {
			CrossWorldBossActivity.setMailSender((receiverId, mailId, goods, notify) -> {
				totalMailCalls.incrementAndGet();
				sentMailIdsByPlayer.computeIfAbsent(receiverId, k -> new ArrayList<>()).add(mailId);
				sentGoodsByPlayer.put(receiverId, goods != null ? new ArrayList<>(goods) : new ArrayList<>());
				// 验证发邮瞬间持久化存储中的 rankSettled 已提前落盘为 true
				GameActivity persisted = memStore.getStore().get(bossUid);
				boolean settledPersisted = false;
				if (persisted != null && persisted.getParams() != null) {
					CrossWorldBossActivity parsed = JsonUtil.parseObjectWithType(persisted.getParams());
					settledPersisted = parsed != null && parsed.isRankSettled();
				}
				rankSettledInStoreWhenMailSent.add(settledPersisted);
			});

			// 录入 3 名玩家伤害：P1(t1) 与 P2(t2>t1) 同为 1000 达标伤害，P3 伤害为 0 低于 minParticipateDamage(1)
			// 同时校验写入动作在 effectivePlayerId <= 0 时严格拒绝且不扣减 Boss 血量
			AttackRequest invalidPlayerAtk = new AttackRequest();
			invalidPlayerAtk.setPlayerId(0L);
			invalidPlayerAtk.setDamage(1000L);
			CrossActivityActionResult invalidPlayerAtkRes = crossService.executePlayerAction(
					bossUid, 0L, CrossWorldBossActivity.ACTION_ATTACK, JsonUtil.toJsonString(invalidPlayerAtk));
			Assertions.assertFalse(invalidPlayerAtkRes.isSuccess(), "playerId<=0 攻打必须被拒绝");
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.ID, invalidPlayerAtkRes.getErrorCode());
			Assertions.assertEquals(100_000_000L, bossAct.getCurrentHp(), "被拒绝攻打不得扣减 Boss 血量");

			AttackRequest atkP1 = new AttackRequest();
			atkP1.setPlayerId(p1Id);
			atkP1.setPlayerName("BossSlayer1");
			atkP1.setServerId("s1");
			atkP1.setDamage(1000L);
			CrossActivityActionResult r1 = crossService.executePlayerAction(
					bossUid, p1Id, CrossWorldBossActivity.ACTION_ATTACK, JsonUtil.toJsonString(atkP1));
			Assertions.assertTrue(r1.isSuccess(), "P1 攻打必须成功");
			AttackResponse r1Resp = JsonUtil.parseObject(r1.getResultPayloadJson(), AttackResponse.class);
			Assertions.assertEquals(1, r1Resp.getMyRank());

			DateUtil.addTimeOffsetMillis(1000L);
			AttackRequest atkP2 = new AttackRequest();
			atkP2.setPlayerId(p2Id);
			atkP2.setPlayerName("BossSlayer2");
			atkP2.setServerId("s2");
			atkP2.setDamage(1000L);
			CrossActivityActionResult r2 = crossService.executePlayerAction(
					bossUid, p2Id, CrossWorldBossActivity.ACTION_ATTACK, JsonUtil.toJsonString(atkP2));
			Assertions.assertTrue(r2.isSuccess(), "P2 攻打必须成功");
			AttackResponse r2Resp = JsonUtil.parseObject(r2.getResultPayloadJson(), AttackResponse.class);
			Assertions.assertEquals(2, r2Resp.getMyRank(), "同伤害下先达成的 P1 应排第1，后达成的 P2 应排第2");

			CrossBossDamageRecord p3Rec = new CrossBossDamageRecord();
			p3Rec.setPlayerId(p3Id);
			p3Rec.setPlayerName("ZeroDamageObserver");
			p3Rec.setServerId("s2");
			p3Rec.setDamage(0L);
			p3Rec.setUpdateTime(DateUtil.currentTimeMillis());
			crossService.runInActivityMailbox(bossUid, () -> {
				bossAct.getDamageMap().put(p3Id, p3Rec);
				return null;
			});

			CrossActivitySnapshot snapshot = crossService.queryActivitySnapshot(bossUid, p1Id);
			Assertions.assertNotNull(snapshot);
			Assertions.assertEquals(100_000_000L, ((Number) snapshot.getExtra().get("maxHp")).longValue());
			Assertions.assertEquals(99_998_000L, ((Number) snapshot.getExtra().get("currentHp")).longValue());
			Assertions.assertEquals(false, snapshot.getExtra().get("killed"));
			Assertions.assertEquals(0L, ((Number) snapshot.getExtra().get("killerPlayerId")).longValue());
			Assertions.assertEquals(false, snapshot.getExtra().get("rankSettled"));
			Assertions.assertEquals(3, ((Number) snapshot.getExtra().get("participantCount")).intValue());

			// 注入 DB 落盘失败，验证 onEnd() 回滚 rankSettled=false 且零发邮
			memStore.setFailNextSave(true);
			crossService.runInActivityMailbox(bossUid, () -> {
				bossAct.end();
				return null;
			});
			Assertions.assertEquals(ActivityState.CLOSE_VALUE, bossAct.getState());
			Assertions.assertFalse(bossAct.isRankSettled(), "DB 持久化失败时必须回滚 rankSettled 为 false");
			Assertions.assertEquals(0, totalMailCalls.get(), "DB 持久化失败时绝不允许发出任何结算邮件");

			// 关闭故障注入后重试 end()，验证先持久化 rankSettled=true 后再异步发送 2 封 mailId=44 邮件
			memStore.setFailNextSave(false);
			crossService.runInActivityMailbox(bossUid, () -> {
				bossAct.end();
				return null;
			});
			Assertions.assertTrue(bossAct.isRankSettled(), "恢复落盘后 rankSettled 必须为 true");
			Assertions.assertEquals(2, totalMailCalls.get(), "仅 2 名达标玩家应收到排名邮件");
			Assertions.assertEquals(List.of(44), sentMailIdsByPlayer.get(p1Id), "P1 应收到 1 封 mailId=44 邮件");
			Assertions.assertEquals(List.of(44), sentMailIdsByPlayer.get(p2Id), "P2 应收到 1 封 mailId=44 邮件");
			Assertions.assertNull(sentMailIdsByPlayer.get(p3Id), "未达参与门槛的 P3 不得收到结算邮件");
			Assertions.assertEquals(List.of(true, true), rankSettledInStoreWhenMailSent,
					"发邮前存储中 rankSettled 必须已持久化为 true");
			Assertions.assertFalse(sentGoodsByPlayer.get(p1Id).isEmpty(), "第1名邮件附件不能为空");
			Assertions.assertEquals(2000, sentGoodsByPlayer.get(p1Id).get(0).getCount(), "P1 应获得第1名档位奖励数量");
			Assertions.assertEquals(1200, sentGoodsByPlayer.get(p2Id).get(0).getCount(), "P2 应获得第2名档位奖励数量");

			// 验证锁定终榜顺序（P1 第1，P2 第2，P3 第3）
			Assertions.assertEquals(3, bossAct.getFinalRankList().size());
			Assertions.assertEquals(p1Id, bossAct.getFinalRankList().get(0).getPlayerId());
			Assertions.assertEquals(p2Id, bossAct.getFinalRankList().get(1).getPlayerId());
			Assertions.assertEquals(p3Id, bossAct.getFinalRankList().get(2).getPlayerId());

			// 重复调用 end() 及冷重载反序列化恢复，断言严格幂等零重发
			crossService.runInActivityMailbox(bossUid, () -> {
				bossAct.end();
				return null;
			});
			Assertions.assertEquals(2, totalMailCalls.get(), "重复 end() 必须幂等跳过，严禁重发邮件");

			GameActivity persistedEntity = memStore.getStore().get(bossUid);
			Assertions.assertNotNull(persistedEntity);
			CrossWorldBossActivity reloaded = JsonUtil.parseObjectWithType(persistedEntity.getParams());
			reloaded.setUid(bossUid);
			reloaded.setScopeKey("group_1");
			reloaded.afterLoadFromDb();
			Assertions.assertTrue(reloaded.isRankSettled());
			Assertions.assertEquals(2, totalMailCalls.get(), "冷重载恢复已结算实例不得重复发邮");

			// 验证 CLOSE 期允许 query_rank，但拒绝 attack（返回 not_open=58）
			QueryRankRequest rankReq = new QueryRankRequest();
			rankReq.setPlayerId(p1Id);
			rankReq.setPage(1);
			rankReq.setPageSize(10);
			CrossActivityActionResult rankRes = crossService.executePlayerAction(
					bossUid, p1Id, CrossWorldBossActivity.ACTION_QUERY_RANK, JsonUtil.toJsonString(rankReq));
			Assertions.assertTrue(rankRes.isSuccess(), "CLOSE 期必须允许查询排行榜");
			QueryRankResponse rankPayload = JsonUtil.parseObject(rankRes.getResultPayloadJson(), QueryRankResponse.class);
			Assertions.assertEquals(3, rankPayload.getTotalCount());
			Assertions.assertEquals(1, rankPayload.getMyRank());
			Assertions.assertEquals(p1Id, rankPayload.getEntries().get(0).getPlayerId());
			Assertions.assertEquals(p2Id, rankPayload.getEntries().get(1).getPlayerId());

			// 验证极大页码 query_rank 不会触发整型溢出，必须成功返回空列表
			QueryRankRequest maxPageReq = new QueryRankRequest();
			maxPageReq.setPlayerId(p1Id);
			maxPageReq.setPage(Integer.MAX_VALUE);
			maxPageReq.setPageSize(100);
			CrossActivityActionResult maxPageRes = crossService.executePlayerAction(
					bossUid, p1Id, CrossWorldBossActivity.ACTION_QUERY_RANK, JsonUtil.toJsonString(maxPageReq));
			Assertions.assertTrue(maxPageRes.isSuccess(), "极大页码查询排行榜不得整型溢出崩溃");
			QueryRankResponse maxPagePayload = JsonUtil.parseObject(maxPageRes.getResultPayloadJson(), QueryRankResponse.class);
			Assertions.assertEquals(3, maxPagePayload.getTotalCount());
			Assertions.assertTrue(maxPagePayload.getEntries().isEmpty(), "越界页码应返回空条目列表");

			// 验证领奖与击杀奖确认写入动作防御 effectivePlayerId <= 0
			bossAct.getUnlockedMilestoneIds().add(1);
			ClaimMilestoneRequest invalidMilestoneReq = new ClaimMilestoneRequest();
			invalidMilestoneReq.setPlayerId(0L);
			invalidMilestoneReq.setMilestoneId(1);
			CrossActivityActionResult invalidMilestoneRes = crossService.executePlayerAction(
					bossUid, 0L, CrossWorldBossActivity.ACTION_CLAIM_MILESTONE, JsonUtil.toJsonString(invalidMilestoneReq));
			Assertions.assertFalse(invalidMilestoneRes.isSuccess(), "playerId<=0 领取里程碑必须被拒绝");
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.ID, invalidMilestoneRes.getErrorCode());

			ClaimKillRewardRequest invalidKillReq = new ClaimKillRewardRequest();
			invalidKillReq.setPlayerId(0L);
			invalidKillReq.setConfirmOnly(false);
			CrossActivityActionResult invalidKillRes = crossService.executePlayerAction(
					bossUid, 0L, CrossWorldBossActivity.ACTION_CLAIM_KILL_REWARD, JsonUtil.toJsonString(invalidKillReq));
			Assertions.assertFalse(invalidKillRes.isSuccess(), "playerId<=0 领取击杀奖励必须被拒绝");
			Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.ID, invalidKillRes.getErrorCode());

			// 验证沙盒战区回退血量时同步清理旧击杀者与高阶里程碑，避免状态矛盾
			long sandboxBossUid = 990009999L;
			CrossWorldBossActivity sandboxAct = new CrossWorldBossActivity();
			sandboxAct.setUid(sandboxBossUid);
			sandboxAct.setScopeKey(DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY);
			sandboxAct.setServerId(DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY);
			sandboxAct.init(51, null, true);
			sandboxAct.setKilled(true);
			sandboxAct.setKillerPlayerId(p1Id);
			sandboxAct.setKillerName("OldKiller");
			sandboxAct.setKillRewardClaimed(true);
			sandboxAct.getUnlockedMilestoneIds().addAll(List.of(1, 2, 3, 4));
			CrossGlobalActivityManager.getInstance().addActivity(sandboxAct);

			AcceptanceResetRequest rollbackReq = new AcceptanceResetRequest();
			rollbackReq.setPlayerId(p1Id);
			rollbackReq.setMode(CrossWorldBossActivity.RESET_MODE_SET_MILESTONE);
			rollbackReq.setMilestoneId(1);
			CrossActivityActionResult rollbackRes = crossService.executePlayerAction(
					sandboxBossUid, p1Id, CrossWorldBossActivity.ACTION_ACCEPTANCE_RESET, JsonUtil.toJsonString(rollbackReq));
			Assertions.assertTrue(rollbackRes.isSuccess(), "沙盒战区回退目标里程碑必须成功");
			Assertions.assertFalse(sandboxAct.isKilled(), "回退至未击杀血量后 killed 必须为 false");
			Assertions.assertEquals(0L, sandboxAct.getKillerPlayerId(), "回退至未击杀血量后必须清理旧击杀者");
			Assertions.assertFalse(sandboxAct.isKillRewardClaimed(), "回退至未击杀血量后必须重置击杀领奖标志");
			Assertions.assertEquals(List.of(1), sandboxAct.getUnlockedMilestoneIds(), "回退后仅应保留已达标的里程碑 1");

			CrossActivityActionResult closeAttackRes = crossService.executePlayerAction(
					bossUid, p1Id, CrossWorldBossActivity.ACTION_ATTACK, JsonUtil.toJsonString(atkP1));
			Assertions.assertFalse(closeAttackRes.isSuccess(), "CLOSE 期必须拒绝继续攻打");
			Assertions.assertEquals(ErrorMsgEnum.not_open.ID, closeAttackRes.getErrorCode(),
					"CLOSE 期攻打必须返回 not_open(58)");

			// 补充通过真实 ActivityHandler 发起 CLOSE 期 11000409 查榜与 11000403/11000407 拒绝 58 校验
			CrossActivityService.getInstance().init();
			ActivityStateManager.getInstance().setState(51, ActivityState.CLOSE_VALUE);
			RedisCrossActivityDirectory.getInstance().putActive(51, "group_1", bossUid);
			CapturingGameClient p1Client = new CapturingGameClient(p1Id);
			createExtraPlayer(p1Id, "BossSlayer1", "s1", 10, p1Client);
			ActivityHandler handler = createInitializedHandler();

			handler.dispatch(p1Client, new ProtobufProtocol(
					PbProtocol.ActivityCrossWorldBossRankRequest_11000409,
					ActivityCrossWorldBossRankRequest_11000409.newBuilder()
							.setActivityId(51)
							.setPage(1)
							.setPageSize(10)
							.build(),
					1));
			ActivityCrossWorldBossRankResponse_1100040a handlerRankResp = p1Client.findLastResponse(
					PbProtocol.ActivityCrossWorldBossRankResponse_1100040a,
					ActivityCrossWorldBossRankResponse_1100040a.class);
			Assertions.assertNotNull(handlerRankResp, "CLOSE 期通过 ActivityHandler 查询排行榜必须成功");
			Assertions.assertEquals(3, handlerRankResp.getTotalCount());
			Assertions.assertEquals(p1Id, handlerRankResp.getEntries(0).getPlayerId());

			p1Client.clearPackets();
			handler.dispatch(p1Client, new ProtobufProtocol(
					PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
					ActivityCrossWorldBossAttackRequest_11000403.newBuilder()
							.setActivityId(51)
							.setDamage(100L)
							.build(),
					2));
			CapturedPacket closeAtkPacket = p1Client.findLastPacket(PbProtocol.ActivityCrossWorldBossAttackResponse_11000404);
			Assertions.assertNotNull(closeAtkPacket);
			Assertions.assertEquals(ErrorMsgEnum.not_open.ID, closeAtkPacket.errorCode(),
					"CLOSE 期通过 ActivityHandler 发起攻打必须返回 not_open(58)");

			p1Client.clearPackets();
			handler.dispatch(p1Client, new ProtobufProtocol(
					PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407,
					ActivityCrossWorldBossBuyCountRequest_11000407.newBuilder()
							.setActivityId(51)
							.build(),
					3));
			CapturedPacket closeBuyPacket = p1Client.findLastPacket(PbProtocol.ActivityCrossWorldBossBuyCountResponse_11000408);
			Assertions.assertNotNull(closeBuyPacket);
			Assertions.assertEquals(ErrorMsgEnum.not_open.ID, closeBuyPacket.errorCode(),
					"CLOSE 期通过 ActivityHandler 发起买次必须返回 not_open(58)");
		} finally {
			CrossWorldBossActivity.resetMailSender();
			DateUtil.resetTimeOffset();
		}
	}

	/**
	 * TC-CWB-TYPE-DUAL-REGISTRY-AND-ZONE-SCOPE:
	 * 验证一表双类双桶注册、VIEW 期阻断提前创建、多战区隔离、zoneOverrides 优先覆盖、
	 * VirtualServerOpen 动态归并/新开、join 只读无脏标，以及 list/redPoint 零同步跨服 RPC。
	 */
	@Test
	public void testTypeDualRegistryAndZoneScope() throws Exception {
		// 校验 ClassManager 双桶注册与工厂实例分桶创建
		ClassManager classManager = ClassManager.getInstance();
		Assertions.assertEquals(CrossWorldBossActivity.class,
				classManager.getServerActivityClass(ActivityTypeEnum.CrossWorldBoss),
				"serverActivityClass 桶必须注册 CrossWorldBossActivity");
		Assertions.assertEquals(CrossWorldBossPlayerActivity.class,
				classManager.getPlayerActivityClass(ActivityTypeEnum.CrossWorldBoss),
				"playerActivityClass 桶必须注册 CrossWorldBossPlayerActivity");
		Assertions.assertInstanceOf(CrossWorldBossActivity.class,
				ActivityFactory.createServerActivityBase(ActivityTypeEnum.CrossWorldBoss.ID));
		Assertions.assertInstanceOf(CrossWorldBossPlayerActivity.class,
				ActivityFactory.createPlayerActivityBase(ActivityTypeEnum.CrossWorldBoss.ID));

		// 构造测试逻辑服视图并验证普通全局活动回归与 zoneOverrides 去重覆盖
		ActivityConfig actCfg = ActivityManager.instance().get(51);
		VirtualServerView s1 = new VirtualServerView("s1", "S1", 1, 1000, 1, DateUtil.nowDateTime());
		VirtualServerView s2 = new VirtualServerView("s2", "S2", 2, 1000, 2, DateUtil.nowDateTime());
		VirtualServerView s3 = new VirtualServerView("s3", "S3", 5, 1000, 5, DateUtil.nowDateTime());
		VirtualServerView sNoSeq = new VirtualServerView("s_no_seq", "NoSeq", 9, 1000, null, DateUtil.nowDateTime());
		Map<String, VirtualServerView> baseServers = new HashMap<>();
		baseServers.put("s1", s1);
		baseServers.put("s2", s2);
		baseServers.put("s3", s3);
		baseServers.put("s_no_seq", sNoSeq);
		ServerHelper.setTestServerOpenMap(baseServers);

		DefaultActivityScopeResolver resolver = DefaultActivityScopeResolver.getInstance();
		Assertions.assertEquals("group_1", resolver.resolveScopeKey(sNoSeq == null ? null : actCfg, "s_no_seq"),
				"缺 seq 逻辑服必须兜底归入 group_1");

		Document actDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element actElem = actDoc.createElement("Activity");
		actElem.setAttribute("ID", "999");
		actElem.setAttribute("type", String.valueOf(ActivityTypeEnum.FirstCharge.ID));
		actElem.setAttribute("openType", "0");
		actElem.setAttribute("isCross", "true");
		actElem.setAttribute("isMultiplayer", "true");
		ActivityConfig globalDateCfg = new ActivityConfig(actElem);
		Assertions.assertEquals(DefaultActivityScopeResolver.GLOBAL_SCOPE_KEY,
				resolver.resolveScopeKey(globalDateCfg, "s1"),
				"非战区分组全局活动必须回归返回 all");

		// 临时注入 zoneOverrides='s3:1' 校验静态覆盖优先于 seq 分桶并去重合并为 [group_1]
		ActivityCrossWorldBossManager cfgMgr = ActivityCrossWorldBossManager.instance();
		Field mapField = ActivityCrossWorldBossManager.class.getDeclaredField("activitycrossworldbosss");
		mapField.setAccessible(true);
		try {
			ActivityCrossWorldBossConfig overrideCfg = buildBossConfig(Map.of("zoneOverrides", "s3:1"));
			mapField.set(cfgMgr, Map.of(51, overrideCfg));
			Assertions.assertEquals("group_1", resolver.resolveScopeKey(actCfg, "s3"),
					"zoneOverrides='s3:1' 必须优先将 s3 归入 group_1");
			Assertions.assertEquals(Set.of("group_1"), resolver.resolveActiveScopeKeys(actCfg, List.of(s1, s2, s3)),
					"静态覆盖后活跃战区集合必须去重合并为 [group_1]");
		} finally {
			cfgMgr.load();
			CrossWorldBossConfigValidator.validateAll();
		}

		// 验证 VIEW 期触发 Leader 巡检与开放事件不提前创建战斗实例
		CrossActivityService crossService = CrossActivityService.getInstance();
		crossService.init();
		ServerContext.getInstance().setLeader(true);
		ActivityStateManager.getInstance().setState(51, ActivityState.VIEW_VALUE);
		crossService.onLeaderScheduleCheck();
		RedisCrossActivityDirectory directory = RedisCrossActivityDirectory.getInstance();
		Assertions.assertFalse(directory.getActiveUid(51, "group_1").isPresent(),
				"VIEW 预告期绝不允许提前创建 Boss 战斗实例");

		// 验证 list 与 redPoint 在无 Cross 实例/路由时零同步跨服 RPC 且正常响应
		CapturingGameClient lowClient = new CapturingGameClient(900_888_900_201L);
		Player lowPlayer = createExtraPlayer(900_888_900_201L, "LowLevelPlayer", "s1", 5, lowClient);
		ActivityHandler handler = createInitializedHandler();

		handler.dispatch(lowClient, new ProtobufProtocol(
				PbProtocol.ActivityListRequest_11000001, ActivityListRequest_11000001.getDefaultInstance(), 1));
		ActivityListResponse_11000002 viewListResp = lowClient.findLastResponse(
				PbProtocol.ActivityListResponse_11000002, ActivityListResponse_11000002.class);
		Assertions.assertNotNull(viewListResp);
		List<ActivityInfo> view51List = viewListResp.getActivitysList().stream().filter(i -> i.getId() == 51).toList();
		Assertions.assertEquals(1, view51List.size(), "VIEW 期列表必须去重下发 1 条 ID=51");
		Assertions.assertEquals(ActivityState.VIEW_VALUE, view51List.get(0).getStateValue());

		// VIEW 预告期请求详情应返回 not_open(58)
		handler.dispatch(lowClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
				ActivityCrossWorldBossInfoRequest_11000401.newBuilder().setActivityId(51).build(), 2));
		CapturedPacket viewInfoPkt = lowClient.findLastPacket(PbProtocol.ActivityCrossWorldBossInfoResponse_11000402);
		Assertions.assertNotNull(viewInfoPkt);
		Assertions.assertEquals(ErrorMsgEnum.not_open.ID, viewInfoPkt.errorCode(),
				"VIEW 预告期请求详情必须返回 not_open(58)");

		CrossWorldBossPlayerActivity lowAct = new CrossWorldBossPlayerActivity();
		lowAct.init(51, lowPlayer, false);
		Assertions.assertFalse(lowAct.hasRed(), "低于 10 级玩家 hasRed() 必须返回 false");

		// 进入 START 期并触发 Leader 巡检，验证低等级拦截 57 与 S1/S2 共享 group_1、S3 独立 group_2
		ActivityStateManager.getInstance().setState(51, ActivityState.START_VALUE);
		crossService.onLeaderScheduleCheck();

		handler.dispatch(lowClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
				ActivityCrossWorldBossInfoRequest_11000401.newBuilder().setActivityId(51).build(), 3));
		CapturedPacket lowInfoPkt = lowClient.findLastPacket(PbProtocol.ActivityCrossWorldBossInfoResponse_11000402);
		Assertions.assertNotNull(lowInfoPkt);
		Assertions.assertEquals(ErrorMsgEnum.level_not_enough.ID, lowInfoPkt.errorCode(),
				"低于 10 级玩家在 START 期请求详情必须返回 level_not_enough(57)");

		long uid1 = directory.getActiveUid(51, "group_1").orElse(0L);
		long uid2 = directory.getActiveUid(51, "group_2").orElse(0L);
		Assertions.assertTrue(uid1 > 0L, "START 期 group_1 必须成功创建实例");
		Assertions.assertTrue(uid2 > 0L, "START 期 group_2 必须成功创建实例");
		Assertions.assertNotEquals(uid1, uid2, "不同战区必须持有完全独立的跨服实例 UID");

		// 触发 VirtualServerOpen 新服动态加入：S4(seq=3) 复用 group_1，S5(seq=9) 新建 group_3
		VirtualServerView s4 = new VirtualServerView("s4", "S4", 3, 1000, 3, DateUtil.nowDateTime());
		VirtualServerView s5 = new VirtualServerView("s5", "S5", 9, 1000, 9, DateUtil.nowDateTime());
		baseServers.put("s4", s4);
		baseServers.put("s5", s5);
		ServerHelper.setTestServerOpenMap(baseServers);
		ServerEventBus.getInstance().dispatch(ServerEventTypeEnum.VirtualServerOpen, "s4");
		ServerEventBus.getInstance().dispatch(ServerEventTypeEnum.VirtualServerOpen, "s5");
		Assertions.assertEquals(uid1, directory.getActiveUid(51, "group_1").orElse(0L),
				"S4(seq=3) 加入不得覆盖或重复创建 group_1");
		long uid3 = directory.getActiveUid(51, "group_3").orElse(0L);
		Assertions.assertTrue(uid3 > 0L, "S5(seq=9) 新服开启必须动态拉起 group_3 战区实例");

		// 验证 queryActivitySnapshot 诊断字段与 join 只读无脏标
		CrossWorldBossActivity bossGroup1 = (CrossWorldBossActivity) CrossGlobalActivityManager.getInstance().getActivity(uid1);
		Assertions.assertNotNull(bossGroup1);
		bossGroup1.clearDirty();
		CrossActivitySnapshot snapStart = crossService.joinActivity(uid1, 900_888_900_202L, "s1", null);
		Assertions.assertNotNull(snapStart);
		Assertions.assertEquals(6, snapStart.getExtra().size(), "快照 extra 必须包含 6 个核心诊断字段");
		Assertions.assertTrue(bossGroup1.join(900_888_900_202L, "s1", null), "START 期 join 必须返回 true");
		Assertions.assertFalse(bossGroup1.isDirty(), "join 只读准入不得标脏");

		bossGroup1.setState(ActivityState.CLOSE_VALUE);
		Assertions.assertTrue(bossGroup1.join(900_888_900_202L, "s1", null), "CLOSE 期 join 必须返回 true");
		bossGroup1.setState(ActivityState.VIEW_VALUE);
		Assertions.assertFalse(bossGroup1.join(900_888_900_202L, "s1", null), "VIEW 非开放态 join 必须返回 false");
		bossGroup1.setState(ActivityState.START_VALUE);
		Assertions.assertNull(crossService.joinActivity(999_999_888L, 900_888_900_202L, "s1", null),
				"不存在的跨服实例 joinActivity 必须返回 null");
		Assertions.assertFalse(bossGroup1.isDirty(), "join 校验全程不得标脏");

		// 验证 S1/S2/S3 玩家调用 list、redPoint（绑定无效缓存校验零 RPC）与 11000401 详情
		CapturingGameClient c1 = new CapturingGameClient(900_888_900_202L);
		CapturingGameClient c2 = new CapturingGameClient(900_888_900_203L);
		CapturingGameClient c3 = new CapturingGameClient(900_888_900_204L);
		Player pS1 = createExtraPlayer(900_888_900_202L, "ZonePlayer1", "s1", 15, c1);
		Player pS2 = createExtraPlayer(900_888_900_203L, "ZonePlayer2", "s2", 15, c2);
		Player pS3 = createExtraPlayer(900_888_900_204L, "ZonePlayer3", "s3", 15, c3);

		pS1.getActivityModule().open(51, false);
		CrossWorldBossPlayerActivity pS1Act = pS1.getActivityModule().get(51);
		Assertions.assertNotNull(pS1Act);
		Assertions.assertTrue(pS1Act.hasRed(), "满级有次数玩家开局红点应为 true");

		handler.dispatch(c1, new ProtobufProtocol(
				PbProtocol.ActivityListRequest_11000001, ActivityListRequest_11000001.getDefaultInstance(), 10));
		ActivityListResponse_11000002 startListResp = c1.findLastResponse(
				PbProtocol.ActivityListResponse_11000002, ActivityListResponse_11000002.class);
		Assertions.assertEquals(1L, startListResp.getActivitysList().stream().filter(i -> i.getId() == 51).count(),
				"list 返回中 ID=51 必须严格去重为 1 条");

		handler.dispatch(c1, new ProtobufProtocol(
				PbProtocol.ActivityRedPointRequest_11000003,
				ActivityRedPointRequest_11000003.newBuilder().addIds(51).build(), 11));
		ActivityRedPointResponse_11000004 redResp = c1.findLastResponse(
				PbProtocol.ActivityRedPointResponse_11000004, ActivityRedPointResponse_11000004.class);
		Assertions.assertTrue(redResp.getIsRed(0), "redPoint 协议必须返回 true 且不触发 Cross RPC");

		handler.dispatch(c1, new ProtobufProtocol(PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
				ActivityCrossWorldBossInfoRequest_11000401.newBuilder().setActivityId(51).build(), 12));
		handler.dispatch(c2, new ProtobufProtocol(PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
				ActivityCrossWorldBossInfoRequest_11000401.newBuilder().setActivityId(51).build(), 13));
		handler.dispatch(c3, new ProtobufProtocol(PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
				ActivityCrossWorldBossInfoRequest_11000401.newBuilder().setActivityId(51).build(), 14));

		ActivityCrossWorldBossInfoResponse_11000402 info1 = c1.findLastResponse(
				PbProtocol.ActivityCrossWorldBossInfoResponse_11000402, ActivityCrossWorldBossInfoResponse_11000402.class);
		ActivityCrossWorldBossInfoResponse_11000402 info2 = c2.findLastResponse(
				PbProtocol.ActivityCrossWorldBossInfoResponse_11000402, ActivityCrossWorldBossInfoResponse_11000402.class);
		ActivityCrossWorldBossInfoResponse_11000402 info3 = c3.findLastResponse(
				PbProtocol.ActivityCrossWorldBossInfoResponse_11000402, ActivityCrossWorldBossInfoResponse_11000402.class);

		Assertions.assertEquals("group_1", info1.getScopeKey());
		Assertions.assertEquals("group_1", info2.getScopeKey());
		Assertions.assertEquals("group_2", info3.getScopeKey());
		Assertions.assertEquals(uid1, info1.getCrossActivityUid());
		Assertions.assertEquals(uid1, info2.getCrossActivityUid());
		Assertions.assertEquals(uid2, info3.getCrossActivityUid());
		Assertions.assertNotNull(pS2.getActivityModule().get(51));
		Assertions.assertNotNull(pS3.getActivityModule().get(51));
	}

	/**
	 * TC-CWB-CONCURRENT-KILLSHOT-AND-NO-WASTE:
	 * 验证 16 线程并发攻打低血 Boss（1000 HP，单次 500）仅有 1 名唯一击杀者当场直发击杀奖、
	 * 其余未有效扣血者返回 455 且绝不扣减次数、冷重载 Cross 实例保留击杀状态，以及非法伤害/状态/等级拦截。
	 */
	@Test
	public void testConcurrentKillshotAndNoWaste() {
		try {
		CrossActivityService crossService = CrossActivityService.getInstance();
		crossService.init();
		ActivityStateManager.getInstance().setState(51, ActivityState.START_VALUE);

		long bossUid = 880051101L;
		CrossWorldBossActivity bossAct = new CrossWorldBossActivity();
		bossAct.setUid(bossUid);
		bossAct.setScopeKey("group_1");
		bossAct.setServerId("group_1");
		bossAct.init(51, null, true);
		Field hpField = CrossWorldBossActivity.class.getDeclaredField("currentHp");
		hpField.setAccessible(true);
		hpField.set(bossAct, 1000L);
		CrossGlobalActivityManager.getInstance().addActivity(bossAct);
		RedisCrossActivityDirectory.getInstance().putActive(51, "group_1", bossUid);

		int threadCount = 16;
		List<Player> fighters = new ArrayList<>();
		List<CapturingGameClient> clients = new ArrayList<>();
		ActivityHandler handler = createInitializedHandler();

		for (int i = 0; i < threadCount; i++) {
			long pid = 900_888_901_000L + i;
			CapturingGameClient c = new CapturingGameClient(pid);
			Player fighter = createExtraPlayer(pid, "Fighter_" + i, TEST_SERVER_ID, 20, c);
			fighter.getActivityModule().open(51, false);
			fighters.add(fighter);
			clients.add(c);
		}

		CountDownLatch readyLatch = new CountDownLatch(threadCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(threadCount);
		ExecutorService pool = Executors.newFixedThreadPool(threadCount);
		AtomicInteger killShotCount = new AtomicInteger(0);
		AtomicInteger killedRejectCount = new AtomicInteger(0);
		AtomicInteger successAttackCount = new AtomicInteger(0);
		List<Long> killerIds = Collections.synchronizedList(new ArrayList<>());

		try {
			for (int i = 0; i < threadCount; i++) {
				final int idx = i;
				pool.submit(() -> {
					readyLatch.countDown();
					try {
						startLatch.await(5, TimeUnit.SECONDS);
						handler.dispatch(clients.get(idx), new ProtobufProtocol(
								PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
								ActivityCrossWorldBossAttackRequest_11000403.newBuilder()
										.setActivityId(51)
										.setDamage(500L)
										.build(),
								100 + idx));
						CapturedPacket pkt = clients.get(idx).findLastPacket(
								PbProtocol.ActivityCrossWorldBossAttackResponse_11000404);
						Assertions.assertNotNull(pkt);
						if (pkt.errorCode() == 0) {
							successAttackCount.incrementAndGet();
							ActivityCrossWorldBossAttackResponse_11000404 resp = (ActivityCrossWorldBossAttackResponse_11000404)
									PbProtocol.getInstance().parseFrom(
											PbProtocol.ActivityCrossWorldBossAttackResponse_11000404, pkt.data());
							if (resp.getIsKillShot()) {
								killShotCount.incrementAndGet();
								killerIds.add(fighters.get(idx).getPlayerId());
								Assertions.assertFalse(resp.getKillRewardsList().isEmpty(),
										"唯一击杀者当场响应必须携带 killRewards");
							}
						} else if (pkt.errorCode() == ErrorMsgEnum.cross_world_boss_killed.ID) {
							killedRejectCount.incrementAndGet();
						} else {
							Assertions.fail("并发攻打出现非预期错误码: " + pkt.errorCode());
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						doneLatch.countDown();
					}
				});
			}
			Assertions.assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
			startLatch.countDown();
			Assertions.assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
		} finally {
			pool.shutdownNow();
		}

		Assertions.assertEquals(0L, bossAct.getCurrentHp(), "击杀后 Boss 血量必须严格为 0，不能为负");
		Assertions.assertTrue(bossAct.isKilled(), "Boss 必须处于已击杀状态");
		Assertions.assertEquals(1000L, bossAct.getTotalDamage(), "战区累计有效伤害必须严格等于击杀前剩余血量 1000");
		Assertions.assertEquals(2, successAttackCount.get(), "1000 血量被 500 伤害击杀，应恰好 2 次攻打有效扣血");
		Assertions.assertEquals(1, killShotCount.get(), "16 线程并发击杀有且仅有 1 人获得 isKillShot=true");
		Assertions.assertEquals(14, killedRejectCount.get(), "其余 14 名玩家必须收到 cross_world_boss_killed(455)");

		long winnerId = killerIds.get(0);
		for (Player fighter : fighters) {
			CrossWorldBossPlayerActivity pAct = fighter.getActivityModule().get(51);
			if (fighter.getPlayerId() == winnerId) {
				Assertions.assertEquals(1, pAct.getDailyUsedCount());
				Assertions.assertTrue(pAct.isKillRewardClaimed());
				Assertions.assertTrue(fighter.getCurrencyModule().getCount(Asset.diamond.ID) >= 1000L,
						"击杀者资产必须真实到账最后一击钻石奖励");
			} else if (pAct.getMyTotalDamage() == 0L) {
				Assertions.assertEquals(0, pAct.getDailyUsedCount(), "被 455 拒绝的玩家绝不能白扣挑战次数");
			}
		}

		// 从持久化存储冷重载 CrossWorldBossActivity 核对击杀归属已即时落库
		CrossWorldBossActivity reloadedBoss = coldReloadCrossActivity(bossUid);
		Assertions.assertTrue(reloadedBoss.isKilled());
		Assertions.assertEquals(winnerId, reloadedBoss.getKillerPlayerId());

		// 校验非法伤害、VIEW/CLOSE 状态与低等级拦截均不扣次
		ActivityCrossWorldBossConfig bossCfg = ActivityCrossWorldBossManager.instance().get(51);
		CapturingGameClient guardClient = new CapturingGameClient(900_888_901_100L);
		Player guardPlayer = createExtraPlayer(900_888_901_100L, "GuardTester", TEST_SERVER_ID, 20, guardClient);
		guardPlayer.getActivityModule().open(51, false);
		CrossWorldBossPlayerActivity guardAct = guardPlayer.getActivityModule().get(51);

		handler.dispatch(guardClient, new ProtobufProtocol(PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
				ActivityCrossWorldBossAttackRequest_11000403.newBuilder().setActivityId(51).setDamage(0L).build(), 1));
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.ID,
				guardClient.findLastPacket(PbProtocol.ActivityCrossWorldBossAttackResponse_11000404).errorCode());

		handler.dispatch(guardClient, new ProtobufProtocol(PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
				ActivityCrossWorldBossAttackRequest_11000403.newBuilder().setActivityId(51)
						.setDamage(bossCfg.maxSingleDamage + 1L).build(), 2));
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.ID,
				guardClient.findLastPacket(PbProtocol.ActivityCrossWorldBossAttackResponse_11000404).errorCode());

		ActivityStateManager.getInstance().setState(51, ActivityState.VIEW_VALUE);
		handler.dispatch(guardClient, new ProtobufProtocol(PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
				ActivityCrossWorldBossAttackRequest_11000403.newBuilder().setActivityId(51).setDamage(100L).build(), 3));
		Assertions.assertEquals(ErrorMsgEnum.not_open.ID,
				guardClient.findLastPacket(PbProtocol.ActivityCrossWorldBossAttackResponse_11000404).errorCode());

		ActivityStateManager.getInstance().setState(51, ActivityState.CLOSE_VALUE);
		handler.dispatch(guardClient, new ProtobufProtocol(PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
				ActivityCrossWorldBossAttackRequest_11000403.newBuilder().setActivityId(51).setDamage(100L).build(), 4));
		Assertions.assertEquals(ErrorMsgEnum.not_open.ID,
				guardClient.findLastPacket(PbProtocol.ActivityCrossWorldBossAttackResponse_11000404).errorCode());

		ActivityStateManager.getInstance().setState(51, ActivityState.START_VALUE);
		guardPlayer.getPlayerModule().getExpLevelMap().setValue(Asset.playerExp.ID, 5);
		handler.dispatch(guardClient, new ProtobufProtocol(PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
				ActivityCrossWorldBossAttackRequest_11000403.newBuilder().setActivityId(51).setDamage(100L).build(), 5));
		Assertions.assertEquals(ErrorMsgEnum.level_not_enough.ID,
				guardClient.findLastPacket(PbProtocol.ActivityCrossWorldBossAttackResponse_11000404).errorCode());
		Assertions.assertEquals(0, guardAct.getDailyUsedCount(), "所有被拦截攻打请求绝不得扣减挑战次数");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * TC-CWB-DAILY-RESET-BUY-AND-REWARD-GUARDS:
	 * 验证绕过查询直接写操作（bypassPriorQuery=true）防卫跨天重置、阶梯购买与死 Boss/超限拦截、
	 * 里程碑领取/门槛/防重，以及击杀奖励断线补领（非击杀者 456、击杀者补领成功、再领 51）。
	 */
	@Test
	public void testDailyResetBuyAndRewardGuards() throws Exception {
		CrossActivityService crossService = CrossActivityService.getInstance();
		crossService.init();
		ActivityStateManager.getInstance().setState(51, ActivityState.START_VALUE);

		long bossUid = 880051201L;
		CrossWorldBossActivity bossAct = new CrossWorldBossActivity();
		bossAct.setUid(bossUid);
		bossAct.setScopeKey("group_1");
		bossAct.setServerId("group_1");
		bossAct.init(51, null, true);
		CrossGlobalActivityManager.getInstance().addActivity(bossAct);
		RedisCrossActivityDirectory.getInstance().putActive(51, "group_1", bossUid);

		// 构造昨日已耗尽次数的玩家，在未先调 11000401 或 list 前直接调用 11000403
		player.getActivityModule().open(51, false);
		CrossWorldBossPlayerActivity playerAct = player.getActivityModule().get(51);
		playerAct.setLastResetDay(DateUtil.getDay() - 1);
		playerAct.setDailyUsedCount(6);
		playerAct.setDailyBoughtCount(3);
		playerAct.setCrossActivityUid(0L);

		ActivityHandler handler = createInitializedHandler();
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
				ActivityCrossWorldBossAttackRequest_11000403.newBuilder()
						.setActivityId(51)
						.setDamage(5000L)
						.build(),
				1));
		ActivityCrossWorldBossAttackResponse_11000404 firstAtk = capturingClient.findLastResponse(
				PbProtocol.ActivityCrossWorldBossAttackResponse_11000404,
				ActivityCrossWorldBossAttackResponse_11000404.class);
		Assertions.assertNotNull(firstAtk, "绕过查询直接攻打应触发防卫性跨天与绑定并成功");
		Assertions.assertEquals(1, playerAct.getDailyUsedCount(), "昨日次数清零后今日第 1 次攻打后已用应为 1");
		Assertions.assertEquals(0, playerAct.getDailyBoughtCount(), "昨日购买次数必须同步清零");
		Assertions.assertEquals(DateUtil.getDay(), playerAct.getLastResetDay());
		Assertions.assertEquals(bossUid, playerAct.getCrossActivityUid());

		// 用光今日剩余 2 次免费次数后触发次数上限拦截
		for (int i = 0; i < 2; i++) {
			handler.dispatch(capturingClient, new ProtobufProtocol(
					PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
					ActivityCrossWorldBossAttackRequest_11000403.newBuilder().setActivityId(51).setDamage(5000L).build(),
					2 + i));
			Assertions.assertEquals(0, capturingClient.findLastPacket(
					PbProtocol.ActivityCrossWorldBossAttackResponse_11000404).errorCode());
		}
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
				ActivityCrossWorldBossAttackRequest_11000403.newBuilder().setActivityId(51).setDamage(5000L).build(),
				5));
		Assertions.assertEquals(ErrorMsgEnum.times_limit.ID, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossAttackResponse_11000404).errorCode(),
				"免费次数耗尽后再次攻打必须返回 times_limit(61)");

		// 买次校验：钻石不足返回 60，补足后按 50/100/150 阶梯扣费，超限返回 108
		capturingClient.clearPackets();
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407,
				ActivityCrossWorldBossBuyCountRequest_11000407.newBuilder().setActivityId(51).build(), 6));
		Assertions.assertEquals(ErrorMsgEnum.resource_not_enough.ID, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossBuyCountResponse_11000408).errorCode(),
				"钻石不足购买次数必须返回 resource_not_enough(60)");

		PlayerHelper.addResources(player, Asset.diamond.ID, 500, OpType.GM, false);
		long diamondBeforeBuy = player.getCurrencyModule().getCount(Asset.diamond.ID);
		int[] expectedStepCosts = { 50, 100, 150 };
		long runningDiamond = diamondBeforeBuy;
		for (int step = 0; step < expectedStepCosts.length; step++) {
			handler.dispatch(capturingClient, new ProtobufProtocol(
					PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407,
					ActivityCrossWorldBossBuyCountRequest_11000407.newBuilder().setActivityId(51).build(), 10 + step));
			ActivityCrossWorldBossBuyCountResponse_11000408 buyResp = capturingClient.findLastResponse(
					PbProtocol.ActivityCrossWorldBossBuyCountResponse_11000408,
					ActivityCrossWorldBossBuyCountResponse_11000408.class);
			Assertions.assertEquals(step + 1, buyResp.getDailyBoughtCount());
			Assertions.assertEquals(step + 1, buyResp.getRemainingTimes());
			runningDiamond -= expectedStepCosts[step];
			Assertions.assertEquals(runningDiamond, player.getCurrencyModule().getCount(Asset.diamond.ID),
					"第 " + (step + 1) + " 次购买必须按阶梯精确扣费");
		}
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407,
				ActivityCrossWorldBossBuyCountRequest_11000407.newBuilder().setActivityId(51).build(), 15));
		Assertions.assertEquals(ErrorMsgEnum.buy_over_limit.ID, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossBuyCountResponse_11000408).errorCode(),
				"达到每日购买上限后必须返回 buy_over_limit(108)");

		// 里程碑未解锁拦截、解锁后领取显式奖励及重复领取拦截
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
				ActivityCrossWorldBossRewardRequest_11000405.newBuilder()
						.setActivityId(51)
						.setRewardType(CrossWorldBossActivity.REWARD_TYPE_MILESTONE)
						.setMilestoneId(1)
						.build(),
				20));
		Assertions.assertEquals(ErrorMsgEnum.condition_check_error.ID, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossRewardResponse_11000406).errorCode(),
				"未解锁里程碑领奖必须返回 condition_check_error(55)");

		AcceptanceResetRequest milestoneSetup = new AcceptanceResetRequest();
		milestoneSetup.setPlayerId(player.getPlayerId());
		milestoneSetup.setPlayerName(player.getData().getName());
		milestoneSetup.setServerId(player.getServerId());
		milestoneSetup.setMode(CrossWorldBossActivity.RESET_MODE_SET_MILESTONE);
		milestoneSetup.setMilestoneId(1);
		Assertions.assertTrue(crossService.executePlayerAction(bossUid, player.getPlayerId(),
				CrossWorldBossActivity.ACTION_ACCEPTANCE_RESET, JsonUtil.toJsonString(milestoneSetup)).isSuccess());

		long diamondBeforeMilestone = player.getCurrencyModule().getCount(Asset.diamond.ID);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
				ActivityCrossWorldBossRewardRequest_11000405.newBuilder()
						.setActivityId(51)
						.setRewardType(CrossWorldBossActivity.REWARD_TYPE_MILESTONE)
						.setMilestoneId(1)
						.build(),
				21));
		ActivityCrossWorldBossRewardResponse_11000406 msResp = capturingClient.findLastResponse(
				PbProtocol.ActivityCrossWorldBossRewardResponse_11000406,
				ActivityCrossWorldBossRewardResponse_11000406.class);
		Assertions.assertFalse(msResp.getRewardsList().isEmpty(), "里程碑领奖必须显式返回 rewards 明细");
		Assertions.assertTrue(player.getCurrencyModule().getCount(Asset.diamond.ID) > diamondBeforeMilestone,
				"里程碑奖励资产必须真实增加");

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
				ActivityCrossWorldBossRewardRequest_11000405.newBuilder()
						.setActivityId(51)
						.setRewardType(CrossWorldBossActivity.REWARD_TYPE_MILESTONE)
						.setMilestoneId(1)
						.build(),
				22));
		Assertions.assertEquals(ErrorMsgEnum.repeat_request.ID, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossRewardResponse_11000406).errorCode(),
				"重复领取同一里程碑必须返回 repeat_request(51)");

		// 模拟跨天事件与 NewDay 重置，验证死 Boss 购买次数拦截零扣费，以及击杀奖励断线补领双保险闭环
		DateUtil.addTimeOffsetMillis(86_400_000L);
		player.getActivityModule().handleEvent(new PlayerEvent(EventTypeEnum.NewDay));
		Assertions.assertEquals(0, playerAct.getDailyUsedCount());
		Assertions.assertEquals(0, playerAct.getDailyBoughtCount());

		crossService.runInActivityMailbox(bossUid, () -> {
			bossAct.setKilled(true);
			bossAct.setKillerPlayerId(player.getPlayerId());
			bossAct.setKillRewardClaimed(false);
			return null;
		});
		playerAct.setKillRewardClaimed(false);

		long diamondBeforeDeadBuy = player.getCurrencyModule().getCount(Asset.diamond.ID);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407,
				ActivityCrossWorldBossBuyCountRequest_11000407.newBuilder().setActivityId(51).build(), 30));
		Assertions.assertEquals(ErrorMsgEnum.cross_world_boss_killed.ID, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossBuyCountResponse_11000408).errorCode(),
				"Boss 已击杀时买次必须返回 455");
		Assertions.assertEquals(diamondBeforeDeadBuy, player.getCurrencyModule().getCount(Asset.diamond.ID),
				"死 Boss 拦截购买绝不得扣除钻石");

		// 非击杀者补领返回 456
		CapturingGameClient otherClient = new CapturingGameClient(900_888_900_999L);
		createExtraPlayer(900_888_900_999L, "NonKiller", TEST_SERVER_ID, 20, otherClient);
		handler.dispatch(otherClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
				ActivityCrossWorldBossRewardRequest_11000405.newBuilder()
						.setActivityId(51)
						.setRewardType(CrossWorldBossActivity.REWARD_TYPE_KILL)
						.setMilestoneId(0)
						.build(),
				31));
		Assertions.assertEquals(ErrorMsgEnum.cross_world_boss_not_killer.ID, otherClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossRewardResponse_11000406).errorCode(),
				"非击杀者补领击杀奖励必须返回 cross_world_boss_not_killer(456)");

		// 击杀者掉线兜底补领成功 1 次，再次补领返回 51
		long diamondBeforeKillReclaim = player.getCurrencyModule().getCount(Asset.diamond.ID);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
				ActivityCrossWorldBossRewardRequest_11000405.newBuilder()
						.setActivityId(51)
						.setRewardType(CrossWorldBossActivity.REWARD_TYPE_KILL)
						.setMilestoneId(0)
						.build(),
				32));
		ActivityCrossWorldBossRewardResponse_11000406 killReclaimResp = capturingClient.findLastResponse(
				PbProtocol.ActivityCrossWorldBossRewardResponse_11000406,
				ActivityCrossWorldBossRewardResponse_11000406.class);
		Assertions.assertFalse(killReclaimResp.getRewardsList().isEmpty());
		Assertions.assertTrue(killReclaimResp.getKillRewardClaimed());
		Assertions.assertTrue(playerAct.isKillRewardClaimed());
		Assertions.assertTrue(bossAct.isKillRewardClaimed());
		Assertions.assertTrue(player.getCurrencyModule().getCount(Asset.diamond.ID) > diamondBeforeKillReclaim);

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
				ActivityCrossWorldBossRewardRequest_11000405.newBuilder()
						.setActivityId(51)
						.setRewardType(CrossWorldBossActivity.REWARD_TYPE_KILL)
						.setMilestoneId(0)
						.build(),
				33));
		Assertions.assertEquals(ErrorMsgEnum.repeat_request.ID, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossRewardResponse_11000406).errorCode(),
				"击杀奖励补领完成后再重领必须返回 repeat_request(51)");
	}

	/**
	 * TC-CWB-RECOVERY-COLD-RELOAD-AND-MIGRATION:
	 * 验证 Cross 停服强保存与重启恢复（含跨 endTime 自动补结算幂等）、玩家 CBOR+Snappy 冷重载、
	 * 同 uid 节点迁移单次重试、换期瞬间写拒错打(397)/读切新期、disposableIds 豁免重开与彻底销毁归零。
	 */
	@Test
	public void testRecoveryColdReloadAndMigration() {
		try {
		CrossActivityService crossService = CrossActivityService.getInstance();
		crossService.init();
		ActivityStateManager.getInstance().setState(51, ActivityState.START_VALUE);
		RedisCrossActivityDirectory directory = RedisCrossActivityDirectory.getInstance();

		long uid1 = 880051301L;
		CrossWorldBossActivity boss1 = new CrossWorldBossActivity();
		boss1.setUid(uid1);
		boss1.setScopeKey("group_1");
		boss1.setServerId("group_1");
		boss1.init(51, null, true);
		CrossGlobalActivityManager.getInstance().addActivity(boss1);
		directory.putActive(51, "group_1", uid1);

		// 玩家参与第 1 期攻打并领取里程碑，随后执行停服 saveAll(true) 与玩家冷重载
		player.getActivityModule().open(51, false);
		CrossWorldBossPlayerActivity pAct = player.getActivityModule().get(51);
		Assertions.assertEquals(uid1, pAct.getCrossActivityUid());

		ActivityHandler handler = createInitializedHandler();
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
				ActivityCrossWorldBossAttackRequest_11000403.newBuilder().setActivityId(51).setDamage(5000L).build(), 1));
		Assertions.assertEquals(0, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossAttackResponse_11000404).errorCode());

		pAct.getClaimedMilestoneIds().add(1);
		pAct.setKillRewardClaimed(true);
		int preShutdownUsed = pAct.getDailyUsedCount();
		long preShutdownHp = boss1.getCurrentHp();
		boss1.markDirty();

		crossService.saveAll(true);
		Assertions.assertFalse(boss1.isDirty(), "saveAll(true) 后必须清除 Cross 实例脏标");

		// 玩家侧 CBOR+Snappy 冷重载为全新对象，验证字段 100% 保留
		ActivityModule reloadedModule = coldReloadActivityModule(player);
		CrossWorldBossPlayerActivity reloadedPlayerAct = reloadedModule.get(51);
		Assertions.assertNotNull(reloadedPlayerAct);
		Assertions.assertNotSame(pAct, reloadedPlayerAct, "冷重载必须生成全新玩家活动实例");
		Assertions.assertEquals(uid1, reloadedPlayerAct.getCrossActivityUid());
		Assertions.assertEquals(preShutdownUsed, reloadedPlayerAct.getDailyUsedCount());
		Assertions.assertTrue(reloadedPlayerAct.getClaimedMilestoneIds().contains(1));
		Assertions.assertEquals(5000L, reloadedPlayerAct.getMyTotalDamage());
		Assertions.assertTrue(reloadedPlayerAct.isKillRewardClaimed());

		// Cross 侧清空内存并通过 CrossActivityDataLoader 重建恢复
		CrossGlobalActivityManager.getInstance().clear();
		Assertions.assertNull(CrossGlobalActivityManager.getInstance().getActivity(uid1));
		List<GameActivity> savedList = memStore.loadAll();
		new CrossActivityDataLoader().processData(savedList);
		CrossWorldBossActivity recoveredBoss1 = (CrossWorldBossActivity) CrossGlobalActivityManager.getInstance().getActivity(uid1);
		Assertions.assertNotNull(recoveredBoss1);
		Assertions.assertEquals(preShutdownHp, recoveredBoss1.getCurrentHp(), "Cross 冷重载后 Boss 剩余血量必须完全保留");
		Assertions.assertEquals(5000L, recoveredBoss1.getTotalDamage());

		// 验证停服跨过 endTime 重载时自动补结算且 rankSettled 置 true 幂等
		long expiredUid = 880051302L;
		CrossWorldBossActivity expiredBoss = new CrossWorldBossActivity();
		expiredBoss.setUid(expiredUid);
		expiredBoss.setScopeKey("group_expired");
		expiredBoss.setServerId("group_expired");
		expiredBoss.init(51, null, true);
		Field endTimeField = ActivityBase.class.getDeclaredField("endTime");
		endTimeField.setAccessible(true);
		endTimeField.set(expiredBoss, DateUtil.currentTimeMillis() - 1000L);
		Field destroyTimeField = ActivityBase.class.getDeclaredField("destroyTime");
		destroyTimeField.setAccessible(true);
		destroyTimeField.set(expiredBoss, DateUtil.currentTimeMillis() + 86_400_000L);
		AttackRequest expiredAtk = new AttackRequest();
		expiredAtk.setPlayerId(player.getPlayerId());
		expiredAtk.setPlayerName(player.getData().getName());
		expiredAtk.setServerId(player.getServerId());
		expiredAtk.setDamage(1000L);
		CrossGlobalActivityManager.getInstance().addActivity(expiredBoss);
		crossService.executePlayerAction(expiredUid, player.getPlayerId(),
				CrossWorldBossActivity.ACTION_ATTACK, JsonUtil.toJsonString(expiredAtk));
		crossService.saveAll(true);
		CrossGlobalActivityManager.getInstance().clear();

		AtomicInteger endMailCount = new AtomicInteger(0);
		CrossWorldBossActivity.setMailSender((rid, mailId, goods, notify) -> endMailCount.incrementAndGet());
		try {
			new CrossActivityDataLoader().processData(memStore.loadAll());
			CrossWorldBossActivity recoveredExpired = (CrossWorldBossActivity)
					CrossGlobalActivityManager.getInstance().getActivity(expiredUid);
			Assertions.assertNotNull(recoveredExpired);
			Assertions.assertEquals(ActivityState.CLOSE_VALUE, recoveredExpired.getState());
			Assertions.assertTrue(recoveredExpired.isRankSettled(), "跨过 endTime 重载必须自动触发结算并锁定 rankSettled");
			Assertions.assertEquals(1, endMailCount.get(), "跨过 endTime 自动补结算应向达标玩家发送 1 封邮件");
			crossService.destroyActivity(expiredUid);
		} finally {
			CrossWorldBossActivity.resetMailSender();
		}

		// 验证同 uid1 节点迁移（首次路由缺失 -> handleCrossRouteFailure 清理 IdCache -> 重试 1 次成功）
		CrossGlobalActivityManager.getInstance().clear();
		directory.putActive(51, "group_1", uid1);
		var crossIdMgr = IdCache.getManager(DistributedObjectType.CROSS_ACTIVITY);
		if (crossIdMgr != null) {
			crossIdMgr.clearServerId(uid1);
		}
		CrossWorldBossPlayerActivity migrationAct = new CrossWorldBossPlayerActivity() {
			@Override
			public void handleCrossRouteFailure(Exception e) {
				super.handleCrossRouteFailure(e);
				CrossGlobalActivityManager.getInstance().addActivity(recoveredBoss1);
			}
		};
		migrationAct.init(51, player, false);
		migrationAct.setCrossActivityUid(uid1);
		player.getActivityModule().getPlayerActivityManager().getActivities().put(51, migrationAct);
		capturingClient.clearPackets();
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
				ActivityCrossWorldBossAttackRequest_11000403.newBuilder().setActivityId(51).setDamage(1000L).build(), 2));
		CapturedPacket migratedAtkPkt = capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossAttackResponse_11000404);
		Assertions.assertNotNull(migratedAtkPkt);
		Assertions.assertEquals(0, migratedAtkPkt.errorCode(), "同 uid 迁移经单次重试后必须成功返回 errorCode=0");

		// 销毁第 1 期 uid1 并开启第 2 期 uid2，验证换期写入拒打 397、只读切新期刷新与 disposableIds 豁免重开
		crossService.destroyActivity(uid1, 51, "group_1");
		long uid2 = 880051303L;
		CrossWorldBossActivity boss2 = new CrossWorldBossActivity();
		boss2.setUid(uid2);
		boss2.setScopeKey("group_1");
		boss2.setServerId("group_1");
		boss2.init(51, null, true);
		CrossGlobalActivityManager.getInstance().addActivity(boss2);
		directory.putActive(51, "group_1", uid2);

		CrossWorldBossPlayerActivity staleWriteAct = new CrossWorldBossPlayerActivity();
		staleWriteAct.init(51, player, false);
		staleWriteAct.setCrossActivityUid(uid1);
		staleWriteAct.getClaimedMilestoneIds().add(1);
		staleWriteAct.setMyTotalDamage(5000L);
		player.getActivityModule().getPlayerActivityManager().getActivities().put(51, staleWriteAct);

		capturingClient.clearPackets();
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
				ActivityCrossWorldBossAttackRequest_11000403.newBuilder().setActivityId(51).setDamage(1000L).build(), 3));
		Assertions.assertEquals(ErrorMsgEnum.activity_not_found.ID, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossAttackResponse_11000404).errorCode(),
				"换期瞬间旧写请求必须返回 397 绝不跨期错打第 2 期 Boss");
		Assertions.assertEquals(0L, boss2.getTotalDamage(), "第 2 期 Boss 不得受到旧期请求伤害");

		// 换期瞬间只读 11000401 自动切换至 uid2 并清空旧期个人数据
		staleWriteAct.setCrossActivityUid(uid1);
		staleWriteAct.getClaimedMilestoneIds().add(1);
		staleWriteAct.setMyTotalDamage(5000L);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
				ActivityCrossWorldBossInfoRequest_11000401.newBuilder().setActivityId(51).build(), 4));
		ActivityCrossWorldBossInfoResponse_11000402 period2Info = capturingClient.findLastResponse(
				PbProtocol.ActivityCrossWorldBossInfoResponse_11000402,
				ActivityCrossWorldBossInfoResponse_11000402.class);
		Assertions.assertEquals(uid2, period2Info.getCrossActivityUid(), "换期只读请求必须切到新期 uid2");
		Assertions.assertTrue(staleWriteAct.getClaimedMilestoneIds().isEmpty(), "换期后旧期已领里程碑必须被清空");
		Assertions.assertEquals(0L, staleWriteAct.getMyTotalDamage(), "换期后旧期个人伤害必须被清空");

		// 验证第 1 期销毁后因 disposableIds 豁免，老玩家仍可重新开启第 2 期实例
		player.getActivityModule().destroy(51, false);
		Assertions.assertNull(player.getActivityModule().get(51));
		player.getActivityModule().open(51, false);
		CrossWorldBossPlayerActivity reopenedAct = player.getActivityModule().get(51);
		Assertions.assertNotNull(reopenedAct, "disposableIds 豁免后老玩家必须能重开第 2 期活动");
		Assertions.assertEquals(uid2, reopenedAct.getCrossActivityUid());

		// 彻底销毁第 2 期且无新一期，验证 onCrossActivityExpired 将 crossActivityUid 归零为 0L 并返回 397
		crossService.destroyActivity(uid2, 51, "group_1");
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
				ActivityCrossWorldBossInfoRequest_11000401.newBuilder().setActivityId(51).build(), 5));
		Assertions.assertEquals(ErrorMsgEnum.activity_not_found.ID, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossInfoResponse_11000402).errorCode(),
				"无新一期彻底销毁后请求必须返回 activity_not_found(397)");
		Assertions.assertEquals(0L, reopenedAct.getCrossActivityUid(),
				"无新一期销毁后 onCrossActivityExpired 必须将 crossActivityUid 归零");

		// 验证在线玩家跨过 destroyTime 且全局状态为 NONE 时，列表与红点均过滤为不可见/false
		ActivityStateManager.getInstance().removeState(51);
		destroyTimeField.set(reopenedAct, DateUtil.currentTimeMillis() - 1000L);
		Assertions.assertFalse(reopenedAct.hasRed(), "跨过 destroyTime 且全局状态 NONE 时红点必须为 false");
		Assertions.assertEquals(ActivityState.NONE_VALUE, reopenedAct.buildActivityInfo().getStateValue(),
				"跨过 destroyTime 且全局状态 NONE 时列表展示状态必须收敛为 NONE");
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
				ActivityCrossWorldBossInfoRequest_11000401.newBuilder().setActivityId(51).build(), 6));
		Assertions.assertEquals(ErrorMsgEnum.activity_not_found.ID, capturingClient.findLastPacket(
				PbProtocol.ActivityCrossWorldBossInfoResponse_11000402).errorCode(),
				"跨过 destroyTime 且全局状态 NONE 时入口必须返回 activity_not_found(397)");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * TC-CWB-LIVE-CLUSTER-ACCEPTANCE-SUITE:
	 * 验证 {@link CrossWorldBossModuleAcceptanceSuite} 注册元信息、{@code sandbox_cwb -> group_9999}
	 * 物理隔离、全量 CONTRACT 与 MATRIX_SCAN 通过，以及 {@link RunMode#PRODUCTION} 下验收重置硬阻断 50。
	 */
	@Test
	public void testLiveClusterAcceptanceSuite() throws Exception {
		ModuleAcceptanceEngine engine = ModuleAcceptanceEngine.getInstance();
		Assertions.assertInstanceOf(CrossWorldBossModuleAcceptanceSuite.class, engine.getSuite(0x11),
				"ModuleAcceptanceEngine 必须注册 0x11 CrossWorldBossModuleAcceptanceSuite");
		Assertions.assertEquals(List.of(ServerType.Game, ServerType.Cross),
				engine.getSuite(0x11).getRequiredServerTypes(),
				"CrossWorldBoss 验收套件必须声明依赖 Game 与 Cross 双节点");
		Assertions.assertEquals(DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY,
				DefaultActivityScopeResolver.getInstance().resolveScopeKey(
						ActivityManager.instance().get(51), DefaultActivityScopeResolver.SANDBOX_SERVER_ID),
				"非生产模式下 sandbox_cwb 必须固定映射至 group_9999");

		long sandboxPlayerId = PlayerSaveCoordinator.SANDBOX_PLAYER_ID_MIN + 0x11;
		try {
			ModuleAcceptanceReport report = engine.runSuite(0x11, AcceptanceRunMode.ALL, null, 100, false);
			Assertions.assertTrue(report.isSuccess(), "双轨验收全部用例必须通过，失败明细: " + report.getFailedItems());
			Assertions.assertEquals(0, report.getFailedCases());
			long contractPassedCount = report.getResults().stream()
					.filter(r -> r.getMode() == AcceptanceRunMode.CONTRACT)
					.count();
			long matrixPassedCount = report.getResults().stream()
					.filter(r -> r.getMode() == AcceptanceRunMode.MATRIX_SCAN)
					.count();
			Assertions.assertTrue(contractPassedCount > 0L, "CONTRACT 轨道必须有通过用例");
			Assertions.assertTrue(matrixPassedCount > 0L, "MATRIX_SCAN 轨道必须有通过用例");
			Assertions.assertFalse(RedisCrossActivityDirectory.getInstance().getActiveUid(51, "group_1").isPresent(),
					"沙盒验收绝不得污染真实战区 group_1");

			// 切换 RunMode.PRODUCTION 验证 ACTION_ACCEPTANCE_RESET 生产硬阻断
			Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
			runModeField.setAccessible(true);
			runModeField.set(ServerContext.getInstance(), RunMode.PRODUCTION);
			long sandboxUid = RedisCrossActivityDirectory.getInstance()
					.getActiveUid(51, DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY).orElse(0L);
			Assertions.assertTrue(sandboxUid > 0L);
			AcceptanceResetRequest resetReq = new AcceptanceResetRequest();
			resetReq.setPlayerId(sandboxPlayerId);
			resetReq.setMode(CrossWorldBossActivity.RESET_MODE_BOSS);
			CrossActivityActionResult prodBlockRes = CrossActivityService.getInstance().executePlayerAction(
					sandboxUid, sandboxPlayerId, CrossWorldBossActivity.ACTION_ACCEPTANCE_RESET,
					JsonUtil.toJsonString(resetReq));
			Assertions.assertFalse(prodBlockRes.isSuccess(), "生产模式下验收重置必须被硬阻断");
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.ID, prodBlockRes.getErrorCode(),
					"生产模式下验收重置必须返回 production_gm_not_allow(50)");
		} finally {
			Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
			runModeField.setAccessible(true);
			runModeField.set(ServerContext.getInstance(), RunMode.DEV);
			Player sandboxPlayer = PlayerManager.getInstance().getPlayer(sandboxPlayerId);
			if (sandboxPlayer != null) {
				SandboxExecutionGuard.releaseSandboxPlayer(sandboxPlayer);
			}
		}
	}

	/**
	 * 创建并初始化已注册 Invoker 的 {@link ActivityHandler} 实例
	 */
	private ActivityHandler createInitializedHandler() {
		ActivityHandler handler = new ActivityHandler();
		handler.inititialize();
		return handler;
	}

	/**
	 * 创建额外在线测试玩家并加入待清理列表，避免跨用例缓存污染
	 */
	private Player createExtraPlayer(long playerId, String name, String serverId, int level, CapturingGameClient client) {
		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(playerId);
		data.setUid(playerId);
		data.setName(name);
		data.setServerId(serverId);
		data.setLevel(level);
		data.setAccountId("acc_" + playerId);
		data.setCreateDate(DateUtil.getTimeByPattern(
				new java.util.Date(DateUtil.currentTimeMillis() - 86400000L * 2), DateUtil.pattern_en));

		Player p = new Player(data);
		p.setAccount(buildTestAccount("acc_" + playerId));
		p.initModule(new HashMap<String, BasePlayerModule>());
		if (p.getPlayerModule() != null) {
			p.getPlayerModule().getExpLevelMap().setValue(Asset.playerExp.ID, level);
		}
		p.setGameClient(client);
		p.setOnline(true);
		p.setIslogouting(false);
		PlayerManager.getInstance().initAdd(p);
		extraPlayerIds.add(playerId);
		return p;
	}

	/**
	 * 构造单行坏配置并断言 {@link CrossWorldBossConfigValidator#validateAll()} 抛出预期异常
	 *
	 * @param overrides        需覆盖的坏字段键值对
	 * @param expectedFieldMsg 异常消息中必须包含的字段或关键词
	 */
	private void assertBadConfigThrows(Map<String, String> overrides, String expectedFieldMsg) {
		ActivityCrossWorldBossConfig badCfg = buildBossConfig(overrides);
		assertBadConfigMapThrows(Map.of(badCfg.ID, badCfg), expectedFieldMsg);
	}

	/**
	 * 临时替换 {@link ActivityCrossWorldBossManager} 内存配置映射并断言校验抛错
	 *
	 * @param tempMap          临时替换的配置映射
	 * @param expectedFieldMsg 异常消息中必须包含的字段或关键词
	 */
	private void assertBadConfigMapThrows(Map<Integer, ActivityCrossWorldBossConfig> tempMap, String expectedFieldMsg) {
		try {
			Field mapField = ActivityCrossWorldBossManager.class.getDeclaredField("activitycrossworldbosss");
			mapField.setAccessible(true);
			mapField.set(ActivityCrossWorldBossManager.instance(), tempMap);
			IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class,
					CrossWorldBossConfigValidator::validateAll,
					"坏配表必须触发 IllegalStateException: " + expectedFieldMsg);
			Assertions.assertTrue(ex.getMessage().contains("ActivityCrossWorldBoss"),
					"异常消息必须包含表名 ActivityCrossWorldBoss: " + ex.getMessage());
			Assertions.assertTrue(ex.getMessage().contains(expectedFieldMsg),
					"异常消息必须包含字段上下文[" + expectedFieldMsg + "]: " + ex.getMessage());
		} catch (ReflectiveOperationException e) {
			log.error("替换测试配置映射失败", e);
			throw new RuntimeException("替换测试配置映射失败", e);
		}
	}

	/**
	 * 基于合法默认值构造用于负向校验的 {@link ActivityCrossWorldBossConfig} 实例
	 *
	 * @param overrides 需覆写的 XML 属性键值对
	 * @return 构造完成的配置对象
	 */
	private ActivityCrossWorldBossConfig buildBossConfig(Map<String, String> overrides) {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element element = doc.createElement("ActivityCrossWorldBoss");
			Map<String, String> attrs = new HashMap<>();
			attrs.put("ID", "51");
			attrs.put("bossName", "测试跨服Boss");
			attrs.put("bossModelId", "1001");
			attrs.put("maxHp", "100000000");
			attrs.put("groupSize", "4");
			attrs.put("zoneOverrides", "");
			attrs.put("dailyFreeTimes", "3");
			attrs.put("dailyBuyLimit", "3");
			attrs.put("buyCosts", "100001;50|100001;100|100001;150");
			attrs.put("maxSingleDamage", "5000000");
			attrs.put("minParticipateDamage", "1");
			attrs.put("milestoneThresholds", "2000;5000;8000;10000");
			attrs.put("milestoneRewards", "100001;100|100001;200|100001;300|100001;500");
			attrs.put("killRewards", "100001;1000|100002;100000");
			attrs.put("rankRanges", "1;1|2;3|4;10|11;50|51;999999");
			attrs.put("rankRewards", "100001;2000|100001;1200|100001;800|100001;400|100001;150");
			attrs.put("rankMailId", "44");
			attrs.putAll(overrides);
			for (Map.Entry<String, String> entry : attrs.entrySet()) {
				element.setAttribute(entry.getKey(), entry.getValue());
			}
			return new ActivityCrossWorldBossConfig(element);
		} catch (Exception e) {
			log.error("构造测试配置对象失败", e);
			throw new RuntimeException("构造测试配置对象失败", e);
		}
	}

	/**
	 * 构造测试用 {@link Account} 对象，保证 {@link cn.game.games.core.log.GameLogger} 日志链路不发生空指针
	 */
	private static Account buildTestAccount(String accountId) {
		Account account = new Account();
		account.accountId = accountId;
		account.sdkPayChannel = "0010";
		account.platform = 1;
		account.version = "1.0.0";
		account.adChannel = "test";
		account.openid = "openid_" + accountId;
		return account;
	}

	/**
	 * 持久化冷重载辅助方法：对玩家全部模块执行 CBOR+Snappy 序列化与反序列化，重建独立内存实例
	 *
	 * @param targetPlayer 目标测试玩家，不可为 null
	 * @return 反序列化并完成 initDefault/initFromDbAfter 初始化的新 ActivityModule 实例
	 */
	private static ActivityModule coldReloadActivityModule(Player targetPlayer) {
		try {
			byte[] snapshot = CborSnappyUtil.compress(targetPlayer.getModules());
			Map<String, BasePlayerModule> restored = CborSnappyUtil.decompress(snapshot);
			ActivityModule restoredModule = (ActivityModule) restored.get(ActivityModule.class.getName());
			if (restoredModule != null) {
				restoredModule.initDefault(targetPlayer);
				restoredModule.initFromDbAfter();
			}
			return restoredModule;
		} catch (Exception e) {
			log.error("冷重载活动模块失败: playerId={}", targetPlayer.getPlayerId(), e);
			throw new RuntimeException("冷重载活动模块失败", e);
		}
	}

	/**
	 * 跨服活动存储冷重载辅助方法：从内存持久化存储重新读取 {@link GameActivity} 并反序列化为独立实例
	 *
	 * @param bossUid 跨服 Boss 实例 UID
	 * @return 从存储重新装载并反序列化的 {@link CrossWorldBossActivity} 实例
	 */
	private CrossWorldBossActivity coldReloadCrossActivity(long bossUid) {
		GameActivity persisted = null;
		for (GameActivity candidate : memStore.loadAll()) {
			if (candidate != null && candidate.getId() == bossUid) {
				persisted = candidate;
				break;
			}
		}
		Assertions.assertNotNull(persisted, "存储中应存在持久化跨服活动实体: uid=" + bossUid);
		return JsonUtil.parseObjectWithType(persisted.getParams());
	}

	/**
	 * 捕获协议响应包快照记录
	 *
	 * @param msgId     下发协议号
	 * @param seq       请求序号
	 * @param data      序列化载荷字节
	 * @param errorCode 错误码（0 表示成功）
	 */
	private record CapturedPacket(int msgId, int seq, byte[] data, int errorCode) {
	}

	/**
	 * 测试专用虚拟客户端桩：拦截并记录服务端下发协议包，供正式 Handler 分发路径断言
	 */
	private static class CapturingGameClient extends VirtualGameClient {

		/** 已捕获的服务端下发响应包列表 */
		private final List<CapturedPacket> packets = new ArrayList<>();

		/**
		 * 构造测试捕获客户端
		 *
		 * @param playerId 绑定测试玩家 ID
		 */
		CapturingGameClient(long playerId) {
			super(playerId);
		}

		@Override
		public boolean needProcess(IProtocol<?> protocol) {
			return true;
		}

		/**
		 * 拦截底层发送调用并存入本地响应列表
		 *
		 * @param msgId     协议号
		 * @param seq       消息序号
		 * @param data      协议体字节
		 * @param errorCode 错误码
		 * @param recored   是否记录缓存标志
		 * @return 恒返回 true 表示测试通道写入成功
		 */
		@Override
		public boolean sendProtocol(int msgId, int seq, byte[] data, int errorCode, boolean recored) {
			packets.add(new CapturedPacket(msgId, seq, data, errorCode));
			return true;
		}

		/**
		 * 清空已捕获的服务端下发协议包记录，便于同一用例内分段验证多轮请求
		 */
		void clearPackets() {
			packets.clear();
		}

		/**
		 * 按协议号查找首条响应并反序列化为强类型 Protobuf 对象
		 *
		 * @param expectedMsgId 期望响应协议号
		 * @param type          期望响应类型
		 * @param <T>           Protobuf Message 子类型
		 * @return 匹配并成功解析的响应对象，未找到返回 null
		 */
		<T extends Message> T findFirstResponse(int expectedMsgId, Class<T> type) {
			for (CapturedPacket packet : packets) {
				if (packet.msgId() == expectedMsgId) {
					Assertions.assertEquals(0, packet.errorCode(),
							"协议响应错误码应为 0, msgId=" + expectedMsgId);
					Message parsed = PbProtocol.getInstance().parseFrom(expectedMsgId, packet.data());
					return type.cast(parsed);
				}
			}
			return null;
		}

		/**
		 * 按协议号查找最后一条响应并反序列化为强类型 Protobuf 对象
		 *
		 * @param expectedMsgId 期望响应协议号
		 * @param type          期望响应类型
		 * @param <T>           Protobuf Message 子类型
		 * @return 匹配并成功解析的响应对象，未找到返回 null
		 */
		<T extends Message> T findLastResponse(int expectedMsgId, Class<T> type) {
			for (int i = packets.size() - 1; i >= 0; i--) {
				CapturedPacket packet = packets.get(i);
				if (packet.msgId() == expectedMsgId) {
					Assertions.assertEquals(0, packet.errorCode(),
							"协议响应错误码应为 0, msgId=" + expectedMsgId);
					Message parsed = PbProtocol.getInstance().parseFrom(expectedMsgId, packet.data());
					return type.cast(parsed);
				}
			}
			return null;
		}

		/**
		 * 按协议号查找最后一条捕获数据包（允许校验非零业务错误码）
		 *
		 * @param expectedMsgId 期望协议号
		 * @return 匹配的捕获数据包，未找到返回 null
		 */
		CapturedPacket findLastPacket(int expectedMsgId) {
			for (int i = packets.size() - 1; i >= 0; i--) {
				CapturedPacket packet = packets.get(i);
				if (packet.msgId() == expectedMsgId) {
					return packet;
				}
			}
			return null;
		}
	}
}
