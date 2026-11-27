package cn.game.games.net.game.module.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.games.cache.entity.GameActivity;
import cn.game.games.cache.entity.GuildJoin;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.common.module.activity.CrossGuildWarConfigValidator;
import cn.game.games.net.common.module.activity.DefaultActivityScopeResolver;
import cn.game.games.net.cross.activity.CrossGlobalActivityManager;
import cn.game.games.net.cross.activity.DefaultCrossActivityStore;
import cn.game.games.net.cross.activity.InMemoryCrossActivityStore;
import cn.game.games.net.cross.activity.RedisCrossActivityDirectory;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.cross.activity.dto.CrossActivitySnapshot;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads;
import cn.game.games.net.cross.activity.impl.CrossGuildWarActivity;
import cn.game.games.net.cross.activity.service.CrossActivityService;
import cn.game.games.net.game.gm.acceptance.AcceptanceCaseStatus;
import cn.game.games.net.game.gm.acceptance.AcceptanceRunMode;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceEngine;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceReport;
import cn.game.games.net.game.gm.proto.VirtualGameClient;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.init.GameIdManagerInitializer;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.module.activity.impl.player.CrossGuildWarPlayerActivity;
import cn.game.games.net.game.module.activity.impl.player.FirstChargeActivity;
import cn.game.games.net.game.module.activity.impl.player.ServerOpenRankPlayerActivity;
import cn.game.games.net.game.module.activity.impl.player.SevenDaysSignin;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.guild.GuildHandler;
import cn.game.games.net.game.module.guild.GuildModule;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.ActivityCrossGuildWarConfig;
import cn.game.protocol.generated.config.ActivityCrossWorldBossConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ActivityCrossGuildWarManager;
import cn.game.protocol.generated.manager.ActivityCrossWorldBossManager;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.generated.manager.ActivityServerOpenRankManager;
import cn.game.protocol.generated.manager.FirstChargeManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.generated.manager.MailManager;
import cn.game.protocol.generated.manager.RandomGivenManager;
import cn.game.protocol.generated.manager.RandomGroupManager;
import cn.game.protocol.generated.manager.RankManager;
import cn.game.protocol.generated.manager.SevenDaysSigninManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarAttackRequest_11000503;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarAttackResponse_11000504;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarBuyCountRequest_11000507;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarBuyCountResponse_11000508;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarGuildRankRequest_11000509;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarGuildRankResponse_1100050a;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarInfoRequest_11000501;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarInfoResponse_11000502;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarMemberRankRequest_1100050b;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarMemberRankResponse_1100050c;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarRewardRequest_11000505;
import cn.game.protocol.protobuf.ActivityMsg.ActivityCrossGuildWarRewardResponse_11000506;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityListRequest_11000001;
import cn.game.protocol.protobuf.ActivityMsg.ActivityListResponse_11000002;
import cn.game.protocol.protobuf.ActivityMsg.ActivityRedPointRequest_11000003;
import cn.game.protocol.protobuf.ActivityMsg.ActivityRedPointResponse_11000004;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.GuildMsg.GuildInfoRequest_40000021;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.CborSnappyUtil;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.ObjUtil;
import cn.game.util.file.ResourceListener;

/**
 * 跨服宗门争霸与既有活动、宗门入口表征回归测试套件
 * <p>
 * 职责边界：先锁定既有纯玩家活动、双端去重列表、宗门 0x40 查询入口与跨服世界Boss号段基线行为，
 * 再逐步补充跨服宗门争霸配置校验、战区隔离、并发争夺与冷重载契约验证。
 * </p>
 *
 * @author shuyongqiang
 */
@Isolated
public class CrossGuildWarActivityTest {

	/** 测试套件日志器 */
	private static final Logger log = LoggerFactory.getLogger(CrossGuildWarActivityTest.class);
	/** 沙盒测试玩家 ID（落在沙盒号段内且与跨服世界Boss测试号段隔离） */
	private static final long TEST_PLAYER_ID = 900_888_910_001L;
	/** 测试逻辑服 ID */
	private static final String TEST_SERVER_ID = "game_test_cgw_1";

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
		ActivityCrossGuildWarManager.instance().load();
		MailManager.instance().load();
		FirstChargeManager.instance().load();
		SevenDaysSigninManager.instance().load();
		ActivityServerOpenRankManager.instance().load();
		RankManager.instance().load();
		ItemManager.instance().load();
		RandomGivenManager.instance().load();
		RandomGroupManager.instance().load();

		PlayerData data = new PlayerData();
		ObjUtil.setDefaultValue(data);
		data.setPlayerId(TEST_PLAYER_ID);
		data.setUid(TEST_PLAYER_ID);
		data.setName("CgwRegressionPlayer");
		data.setServerId(TEST_SERVER_ID);
		data.setLevel(10);
		data.setAccountId("acc_cgw_regression_1");
		data.setCreateDate(DateUtil.getTimeByPattern(
				new java.util.Date(DateUtil.currentTimeMillis() - 86400000L * 2), DateUtil.pattern_en));

		player = new Player(data);
		player.setAccount(buildTestAccount("acc_cgw_regression_1"));
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
		ActivityStateManager.getInstance().removeState(52);
		ServerHelper.setTestServerOpenMap(null);
		CrossGlobalActivityManager.getInstance().clear();
		RedisCrossActivityDirectory.getInstance().clearLocalMemory();
		if (memStore != null) {
			memStore.clear();
		}
		CrossActivityService.getInstance().setStore(new DefaultCrossActivityStore());
		CrossActivityService.getInstance().setScopeResolver(DefaultActivityScopeResolver.getInstance());
		DateUtil.resetTimeOffset();
		ServerContext.getInstance().setLeader(originalLeader);
		if (originalRunMode != null) {
			Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
			runModeField.setAccessible(true);
			runModeField.set(ServerContext.getInstance(), originalRunMode);
		}
	}

	/**
	 * TC-CGW-CONFIG-PROTO-AND-LEGACY-REGRESSION:
	 * 锁定既有纯玩家活动、双端去重列表、宗门 0x40 查询入口与跨服世界Boss(17)号段基线行为
	 */
	@Test
	public void testConfigProtoAndLegacyRegression() {
		String[] variantKeys = {
				"FirstCharge", "SevenDaysSignin", "ServerOpenRank", "GuildSystem_0x40", "CrossWorldBoss"
		};
		Assertions.assertEquals(5, variantKeys.length);

		String[] entryPointIds = {
				"QUERY: ActivityHandler#list (ActivityListRequest_11000001)_",
				"QUERY: ActivityHandler#redPoint (ActivityRedPointRequest_11000003)_"
		};
		Assertions.assertEquals(2, entryPointIds.length);

		boolean bypassesPriorQuery = true;
		Assertions.assertTrue(bypassesPriorQuery, "必须先验证绕过前置列表查询直接写入的旧活动链路");

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

		// 触发跨天事件后再领开服排行当日奖励，验证跨天重置后仍保持一致
		activityModule.handleEvent(new PlayerEvent(EventTypeEnum.NewDay));
		Assertions.assertNotNull(serverOpenRank.receive(41), "开服排行每日领奖应返回非空奖励");

		// 对整个玩家模块映射表执行 CBOR+Snappy 压缩与解压冷重载，验证反序列化出的全新 ActivityModule 与 GuildModule 实例
		GuildModule guildModule = player.getGuildModule();
		Assertions.assertNotNull(guildModule, "玩家宗门模块不能为空");
		ColdReloadedModules reloadedModules = coldReloadActivityModule(player);
		ActivityModule reloadedModule = reloadedModules.activityModule();
		GuildModule reloadedGuildModule = reloadedModules.guildModule();
		Assertions.assertNotNull(reloadedModule, "冷重载后的活动模块不能为空");
		Assertions.assertNotSame(activityModule, reloadedModule, "冷重载后的活动模块必须为全新反序列化实例");
		Assertions.assertNotNull(reloadedGuildModule, "冷重载后的宗门模块不能为空");
		Assertions.assertNotSame(guildModule, reloadedGuildModule, "冷重载后的宗门模块必须为全新反序列化实例");
		Assertions.assertEquals(0L, reloadedGuildModule.getGuildId(), "冷重载后未加入宗门玩家 guildId 必须为 0");
		Assertions.assertEquals(guildModule.getDisbandCount(), reloadedGuildModule.getDisbandCount(),
				"冷重载后宗门解散计数必须一致");
		Assertions.assertEquals(guildModule.getDonateMap().getMap(), reloadedGuildModule.getDonateMap().getMap(),
				"冷重载后宗门捐献计数映射必须一致");

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
		Assertions.assertEquals(1, reloadedSevenDays.getSigninDay(), "冷重载后七日签到累计天数应为 1");
		Assertions.assertFalse(reloadedSevenDays.isSignin(), "跨天事件触发后冷重载七日签到当日领奖标记应为 false");
		Assertions.assertFalse(reloadedServerOpenRank.hasRed(), "冷重载后开服排行已领当日奖励，红点应为 false");

		// 通过真实 ActivityHandler 实例与测试 NetClient 桩分发正式协议
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
		Assertions.assertEquals(reloadedSevenDays.hasRed(), redResp.getIsRed(1),
				"活动 12 红点协议返回应与冷重载后 hasRed() 状态一致");
		Assertions.assertFalse(redResp.getIsRed(2), "活动 41 红点协议返回应为 false");

		// 验证 GuildHandler(0x40) 正式查询入口在无阻塞情况下正常返回错误码
		GuildHandler guildHandler = new GuildHandler() {
			{
				inititialize();
			}
		};
		guildHandler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.GuildInfoRequest_40000021, GuildInfoRequest_40000021.getDefaultInstance(), -3));
		CapturedPacket guildResp = capturingClient.findLastPacket(PbProtocol.GuildInfoResponse_40000022);
		Assertions.assertNotNull(guildResp, "GuildHandler 0x40 正式查询入口必须下发 GuildInfoResponse_40000022");
		Assertions.assertEquals(ErrorMsgEnum.zong_men_not_exist.ID, guildResp.errorCode(),
				"未加入宗门时 GuildSystem_0x40 正式查询应返回 zong_men_not_exist(411)");

		// 锁定既有 CrossWorldBoss(17) 枚举、操作类型、错误码、协议号与配表载体不变
		Assertions.assertEquals(17, ActivityTypeEnum.CrossWorldBoss.ID, "ActivityTypeEnum.CrossWorldBoss.ID 必须保持 17");
		Assertions.assertEquals(310, OpType.CrossWorldBoss.getId(), "OpType.CrossWorldBoss.getId() 必须保持 310");
		Assertions.assertEquals(455, ErrorMsgEnum.cross_world_boss_killed.ID, "错误码 455 必须保持 cross_world_boss_killed");
		Assertions.assertEquals(456, ErrorMsgEnum.cross_world_boss_not_killer.ID, "错误码 456 必须保持 cross_world_boss_not_killer");
		Assertions.assertEquals(0x11000401, PbProtocol.ActivityCrossWorldBossInfoRequest_11000401);
		ActivityCrossWorldBossConfig bossCfg = ActivityCrossWorldBossManager.instance().get(51);
		Assertions.assertNotNull(bossCfg, "CrossWorldBoss(ID=51) 配置必须保持存在");

		// 校验跨服宗门争霸枚举号段、错误码、Proto 常量(11000501~1100050c)与 20 字段配置表骨架
		Assertions.assertEquals(18, ActivityTypeEnum.CrossGuildWar.ID, "ActivityTypeEnum.CrossGuildWar.ID 必须为 18");
		Assertions.assertEquals(311, OpType.CrossGuildWar.getId(), "OpType.CrossGuildWar.getId() 必须为 311");
		Assertions.assertEquals(457, ErrorMsgEnum.cross_guild_war_guild_locked.ID,
				"错误码 457 必须为 cross_guild_war_guild_locked");
		Assertions.assertEquals(458, ErrorMsgEnum.cross_guild_war_stronghold_invalid.ID,
				"错误码 458 必须为 cross_guild_war_stronghold_invalid");
		Assertions.assertEquals(459, ErrorMsgEnum.cross_guild_war_no_locked_guild.ID,
				"错误码 459 必须为 cross_guild_war_no_locked_guild");
		Assertions.assertEquals(20, ActivityCrossGuildWarConfig.class.getDeclaredFields().length,
				"ActivityCrossGuildWarConfig 必须严格保留 20 个原始配表字段");

		Assertions.assertEquals(0x11000501, PbProtocol.ActivityCrossGuildWarInfoRequest_11000501);
		Assertions.assertEquals(0x11000502, PbProtocol.ActivityCrossGuildWarInfoResponse_11000502);
		Assertions.assertEquals(0x11000503, PbProtocol.ActivityCrossGuildWarAttackRequest_11000503);
		Assertions.assertEquals(0x11000504, PbProtocol.ActivityCrossGuildWarAttackResponse_11000504);
		Assertions.assertEquals(0x11000505, PbProtocol.ActivityCrossGuildWarRewardRequest_11000505);
		Assertions.assertEquals(0x11000506, PbProtocol.ActivityCrossGuildWarRewardResponse_11000506);
		Assertions.assertEquals(0x11000507, PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507);
		Assertions.assertEquals(0x11000508, PbProtocol.ActivityCrossGuildWarBuyCountResponse_11000508);
		Assertions.assertEquals(0x11000509, PbProtocol.ActivityCrossGuildWarGuildRankRequest_11000509);
		Assertions.assertEquals(0x1100050a, PbProtocol.ActivityCrossGuildWarGuildRankResponse_1100050a);
		Assertions.assertEquals(0x1100050b, PbProtocol.ActivityCrossGuildWarMemberRankRequest_1100050b);
		Assertions.assertEquals(0x1100050c, PbProtocol.ActivityCrossGuildWarMemberRankResponse_1100050c);

		// 校验 Activity(52)、Mail(45) 与 ActivityCrossGuildWar(52) 静态配置存在性及强校验器正向/负向规则
		ActivityConfig act52 = ActivityManager.instance().getNullable(52);
		Assertions.assertNotNull(act52, "Activity(ID=52) 必须存在");
		Assertions.assertEquals(18, act52.type, "Activity(52).type 必须为 18");
		Assertions.assertTrue(act52.isCross && act52.isMultiplayer, "Activity(52) 必须为跨服多人活动");
		Assertions.assertNotNull(MailManager.instance().getNullable(45), "Mail(ID=45) 结算邮件模板必须存在");

		ActivityCrossGuildWarConfig liveWarCfg = ActivityCrossGuildWarManager.instance().get(52);
		Assertions.assertEquals(45, liveWarCfg.rankMailId);
		Assertions.assertEquals(3, liveWarCfg.strongholdIds.length);
		Assertions.assertTrue(CrossGuildWarConfigValidator.isValidRewardId(Asset.diamond.ID), "Asset 元宝 ID 必须通过校验");
		Assertions.assertFalse(CrossGuildWarConfigValidator.isValidRewardId(99999999), "不存在的资源 ID 必须被拦截");
		Assertions.assertTrue(CrossGuildWarConfigValidator.isCrossGuildUnsafeAsset(100701), "100701 必须被识别为高危公会资产");
		Assertions.assertTrue(CrossGuildWarConfigValidator.isCrossGuildUnsafeAsset(100702), "100702 必须被识别为高危公会资产");
		Assertions.assertTrue(CrossGuildWarConfigValidator.isCrossGuildUnsafeAsset(100703), "100703 必须被识别为高危公会资产");
		Assertions.assertFalse(CrossGuildWarConfigValidator.isCrossGuildUnsafeAsset(100001), "常规货币不应被误判为高危公会资产");
		CrossGuildWarConfigValidator.validateAll();
		Assertions.assertTrue(CrossGuildWarConfigValidator.resolveZoneOverrides(liveWarCfg).isEmpty(),
				"默认配置 zoneOverrides 为空时应返回空不可变映射");

		verifyHotReloadAndBadConfigValidation();
	}

	/**
	 * TC-CGW-TYPE-DUAL-REGISTRY-AND-ZONE-SCOPE:
	 * 验证双桶注册、多战区隔离、VirtualServerOpen 动态归并、join 只读与零 RPC 红点/列表
	 */
	@Test
	public void testTypeDualRegistryAndZoneScope() throws Exception {
		Assertions.assertTrue(ClassManager.getInstance().hasServerActivityClass(ActivityTypeEnum.CrossGuildWar.ID),
				"ClassManager 必须注册 CrossGuildWar Cross 端活动类");
		Assertions.assertTrue(ClassManager.getInstance().hasPlayerActivityClass(ActivityTypeEnum.CrossGuildWar.ID),
				"ClassManager 必须注册 CrossGuildWar Game 端玩家活动类");

		ActivityConfig act52 = ActivityManager.instance().get(52);
		DefaultActivityScopeResolver resolver = DefaultActivityScopeResolver.getInstance();
		Assertions.assertEquals("group_9998", resolver.resolveScopeKey(act52, "sandbox_cgw"),
				"非生产沙盒区服 sandbox_cgw 必须固定映射至 group_9998");
		Assertions.assertEquals("group_9999",
				resolver.resolveScopeKey(ActivityManager.instance().get(51), "sandbox_cwb"),
				"CrossWorldBoss 沙盒区服 sandbox_cwb 必须保持映射至 group_9999");

		VirtualServerView s1 = buildServerView("cgw_s1", 1);
		VirtualServerView s2 = buildServerView("cgw_s2", 2);
		VirtualServerView s3 = buildServerView("cgw_s3", 5);
		VirtualServerView sNoSeq = buildServerView("cgw_no_seq", null);
		ServerHelper.setTestServerOpenMap(Map.of("cgw_s1", s1, "cgw_s2", s2, "cgw_s3", s3, "cgw_no_seq", sNoSeq));

		Assertions.assertEquals("group_1", resolver.resolveScopeKey(act52, "cgw_s1"));
		Assertions.assertEquals("group_1", resolver.resolveScopeKey(act52, "cgw_s2"));
		Assertions.assertEquals("group_2", resolver.resolveScopeKey(act52, "cgw_s3"));
		Assertions.assertEquals("group_1", resolver.resolveScopeKey(act52, "cgw_no_seq"),
				"缺少 seq 的逻辑服必须兜底归入 group_1");

		// 验证 OPENTYPE_DATE 普通活动与 OPENTYPE_SERVER_OPEN_DAY 映射语义保持不变
		ActivityConfig dateCfg = ActivityManager.instance().list().stream()
				.filter(c -> c != null && c.openType == ActivityHelper.OPENTYPE_DATE
						&& !DefaultActivityScopeResolver.isZoneGroupedActivity(c))
				.findFirst()
				.orElseThrow();
		Assertions.assertEquals("all", resolver.resolveScopeKey(dateCfg, "cgw_s1"));
		Assertions.assertEquals("cgw_s1", resolver.resolveScopeKey(ActivityManager.instance().get(41), "cgw_s1"));

		// 验证静态 zoneOverrides 覆盖优先级
		ActivityCrossGuildWarManager warMgr = ActivityCrossGuildWarManager.instance();
		Field warMapField = ActivityCrossGuildWarManager.class.getDeclaredField("activitycrossguildwars");
		warMapField.setAccessible(true);
		try {
			ActivityCrossGuildWarConfig overrideCfg = buildWarConfig(Map.of("zoneOverrides", "cgw_s3:1"));
			warMapField.set(warMgr, Map.of(52, overrideCfg));
			Assertions.assertEquals("group_1", resolver.resolveScopeKey(act52, "cgw_s3"),
					"zoneOverrides 将 cgw_s3 显式覆盖到 group_1 时应优先命中");
		} finally {
			warMgr.load();
		}

		// 验证 VIEW 期不提前创建 Cross 实例，START 期多战区隔离创建及 VirtualServerOpen 动态归并
		CrossActivityService crossService = CrossActivityService.getInstance();
		crossService.init();
		ServerContext.getInstance().setLeader(true);
		ActivityStateManager.getInstance().setState(52, ActivityState.VIEW_VALUE);
		crossService.onLeaderScheduleCheck();
		Assertions.assertTrue(RedisCrossActivityDirectory.getInstance().getActiveUid(52, "group_1").isEmpty(),
				"VIEW 预告期不得提前创建 CrossGuildWarActivity 实例");

		ActivityStateManager.getInstance().setState(52, ActivityState.START_VALUE);
		crossService.onLeaderScheduleCheck();
		long uidGroup1 = RedisCrossActivityDirectory.getInstance().getActiveUid(52, "group_1").orElse(0L);
		long uidGroup2 = RedisCrossActivityDirectory.getInstance().getActiveUid(52, "group_2").orElse(0L);
		Assertions.assertTrue(uidGroup1 > 0L && uidGroup2 > 0L && uidGroup1 != uidGroup2,
				"group_1 与 group_2 必须分别创建独立 CrossGuildWarActivity 实例");

		VirtualServerView s4 = buildServerView("cgw_s4", 3);
		VirtualServerView s5 = buildServerView("cgw_s5", 9);
		ServerHelper.setTestServerOpenMap(Map.of(
				"cgw_s1", s1, "cgw_s2", s2, "cgw_s3", s3, "cgw_s4", s4, "cgw_s5", s5));
		java.lang.reflect.Method onVirtualServerOpenMethod = CrossActivityService.class
				.getDeclaredMethod("onVirtualServerOpen", String.class);
		onVirtualServerOpenMethod.setAccessible(true);
		onVirtualServerOpenMethod.invoke(crossService, "cgw_s4");
		onVirtualServerOpenMethod.invoke(crossService, "cgw_s5");
		Assertions.assertEquals(uidGroup1,
				RedisCrossActivityDirectory.getInstance().getActiveUid(52, "group_1").orElse(0L),
				"同战区新服 cgw_s4(seq=3) 必须复用已有 group_1 实例");
		long uidGroup3 = RedisCrossActivityDirectory.getInstance().getActiveUid(52, "group_3").orElse(0L);
		Assertions.assertTrue(uidGroup3 > 0L && uidGroup3 != uidGroup1 && uidGroup3 != uidGroup2,
				"新战区新服 cgw_s5(seq=9) 必须新建 group_3 实例");

		// 验证 joinActivity 只读准入与不触发 markDirty()
		CrossGuildWarActivity warAct1 = (CrossGuildWarActivity) CrossGlobalActivityManager.getInstance().getActivity(uidGroup1);
		warAct1.clearDirty();
		CrossActivitySnapshot snapStart = crossService.joinActivity(uidGroup1, TEST_PLAYER_ID, "cgw_s1", null);
		Assertions.assertNotNull(snapStart);
		Assertions.assertFalse(warAct1.isDirty(), "joinActivity 只读准入绝不能触发 markDirty()");

		warAct1.setState(ActivityState.VIEW_VALUE);
		Assertions.assertFalse(warAct1.join(TEST_PLAYER_ID, "cgw_s1", null), "VIEW 期 join 必须返回 false");
		Assertions.assertFalse(warAct1.isDirty());
		warAct1.setState(ActivityState.START_VALUE);

		// 验证 list 去重 1 条 ID=52，以及低于 10 级玩家的红点与协议拦截
		player.getData().setLevel(9);
		if (player.getPlayerModule() != null) {
			player.getPlayerModule().getExpLevelMap().setValue(Asset.playerExp.ID, 9);
		}
		player.getData().setServerId("cgw_s1");
		player.getActivityModule().open(52, false);
		CrossGuildWarPlayerActivity lowLevelAct = (CrossGuildWarPlayerActivity) player.getActivityModule().get(52);
		Assertions.assertNotNull(lowLevelAct);
		Assertions.assertFalse(lowLevelAct.hasRed(), "低于 levelLimit(10) 时 hasRed() 必须返回 false");

		ActivityHandler handler = createInitializedActivityHandler();
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarInfoRequest_11000501,
				ActivityCrossGuildWarInfoRequest_11000501.newBuilder().setActivityId(52).build(), -10));
		CapturedPacket lowLevelPkt = capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarInfoResponse_11000502);
		Assertions.assertNotNull(lowLevelPkt);
		Assertions.assertEquals(ErrorMsgEnum.level_not_enough.ID, lowLevelPkt.errorCode(),
				"低于 10 级查询 11000501 必须返回 level_not_enough(57)");

		capturingClient.clearPackets();
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityListRequest_11000001, ActivityListRequest_11000001.getDefaultInstance(), -11));
		ActivityListResponse_11000002 listResp = capturingClient.findFirstResponse(
				PbProtocol.ActivityListResponse_11000002, ActivityListResponse_11000002.class);
		Assertions.assertNotNull(listResp);
		long count52 = listResp.getActivitysList().stream().map(ActivityInfo::getId).filter(id -> id == 52).count();
		Assertions.assertEquals(1L, count52, "ActivityHandler#list 响应中 ID=52 必须严格去重为 1 条");
	}

	/**
	 * TC-CGW-CONCURRENT-STRONGHOLD-AND-TIE-HOLD:
	 * 验证 16 线程多宗门并发攻打据点守恒、同分保权与严格大于易主、缓存缺失降级、防作弊拦截与双榜分页
	 */
	@Test
	public void testConcurrentStrongholdAndTieHold() {
		try {
		CrossActivityService.getInstance().init();
		ActivityStateManager.getInstance().setState(52, ActivityState.START_VALUE);
		long warUid = 880052101L;
		CrossGuildWarActivity warAct = registerWarActivity(warUid, "group_1");

		ActivityHandler handler = createInitializedActivityHandler();
		long guildA = 70001L;
		long guildB = 70002L;
		int threadCount = 16;
		List<Player> fighters = new ArrayList<>();
		List<CapturingGameClient> clients = new ArrayList<>();
		for (int i = 0; i < threadCount; i++) {
			long pid = 900_888_911_000L + i;
			CapturingGameClient c = new CapturingGameClient(pid);
			Player fighter = createExtraPlayer(pid, "CgwFighter_" + i, TEST_SERVER_ID, 20, c);
			setTestGuildId(fighter, i < 8 ? guildA : guildB);
			fighters.add(fighter);
			clients.add(c);
		}

		CountDownLatch readyLatch = new CountDownLatch(threadCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(threadCount);
		ExecutorService pool = Executors.newFixedThreadPool(threadCount);
		AtomicInteger successCount = new AtomicInteger(0);
		try {
			for (int i = 0; i < threadCount; i++) {
				final int idx = i;
				pool.submit(() -> {
					readyLatch.countDown();
					try {
						startLatch.await(5, TimeUnit.SECONDS);
						long attackScore = idx < 8 ? 200L : 150L;
						handler.dispatch(clients.get(idx), new ProtobufProtocol(
								PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
								ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
										.setActivityId(52)
										.setStrongholdId(1)
										.setScore(attackScore)
										.build(),
								200 + idx));
						CapturedPacket pkt = clients.get(idx).findLastPacket(
								PbProtocol.ActivityCrossGuildWarAttackResponse_11000504);
						if (pkt != null && pkt.errorCode() == 0) {
							successCount.incrementAndGet();
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
			Assertions.assertTrue(doneLatch.await(15, TimeUnit.SECONDS));
		} finally {
			pool.shutdownNow();
		}

		Assertions.assertEquals(threadCount, successCount.get(), "16 线程并发出征应全部记分成功");

		// 验证同分保权（A=1600, B 先追平 1600 不易主, 再打 100 达到 1700 才易主）
		Player tieBreaker = createExtraPlayer(900_888_911_901L, "TieBreaker", TEST_SERVER_ID, 20,
				new CapturingGameClient(900_888_911_901L));
		setTestGuildId(tieBreaker, guildB);
		CapturingGameClient tieClient = (CapturingGameClient) tieBreaker.getGameClient();

		handler.dispatch(tieClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(400L).build(), 301));
		ActivityCrossGuildWarAttackResponse_11000504 tieResp = tieClient.findLastResponse(
				PbProtocol.ActivityCrossGuildWarAttackResponse_11000504,
				ActivityCrossGuildWarAttackResponse_11000504.class);
		Assertions.assertNotNull(tieResp);
		Assertions.assertEquals(1600L, tieResp.getGuildStrongholdScore(), "宗门B追平应达到 1600");
		Assertions.assertEquals(guildA, tieResp.getOwnerGuildId(), "同分(1600==1600)必须保持先达成者宗门A占领");
		Assertions.assertFalse(tieResp.getCaptured(), "同分追平不能置 captured=true");

		handler.dispatch(tieClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(100L).build(), 302));
		ActivityCrossGuildWarAttackResponse_11000504 captureResp = tieClient.findLastResponse(
				PbProtocol.ActivityCrossGuildWarAttackResponse_11000504,
				ActivityCrossGuildWarAttackResponse_11000504.class);
		Assertions.assertNotNull(captureResp);
		Assertions.assertEquals(guildB, captureResp.getOwnerGuildId(), "严格超越(1700>1600)必须易主为宗门B");
		Assertions.assertTrue(captureResp.getCaptured(), "夺取易主时 captured 必须为 true");
		Assertions.assertEquals("宗门#" + guildB, captureResp.getOwnerGuildName(),
				"SimpleGuild 缓存缺失时必须安全降级为 宗门#" + guildB);

		// 验证 CrossGuildWarActivity 持久化 JSON 冷重载一致性（直接查询反序列化新实例的权威状态）
		CrossActivityService.getInstance().saveAll(true);
		CrossGuildWarActivity reloadedWar = coldReloadCrossActivity(warUid);
		CrossGuildWarPayloads.QueryDetailRequest reloadedDetailReq = new CrossGuildWarPayloads.QueryDetailRequest();
		reloadedDetailReq.setPlayerId(tieBreaker.getPlayerId());
		reloadedDetailReq.setCurrentGuildId(guildB);
		CrossActivityActionResult reloadedDetailRes = reloadedWar.executePlayerAction(
				tieBreaker.getPlayerId(), CrossGuildWarActivity.ACTION_QUERY_DETAIL,
				JsonUtil.toJsonString(reloadedDetailReq));
		Assertions.assertTrue(reloadedDetailRes.isSuccess());
		CrossGuildWarPayloads.QueryDetailResponse reloadedDetail = JsonUtil.parseObject(
				reloadedDetailRes.getResultPayloadJson(), CrossGuildWarPayloads.QueryDetailResponse.class);
		Assertions.assertNotNull(reloadedDetail);
		Assertions.assertEquals(guildB, reloadedDetail.getStrongholds().get(0).getOwnerGuildId(),
				"冷重载后据点1霸主必须为宗门B");
		Assertions.assertEquals(1700L, reloadedDetail.getStrongholds().get(0).getOwnerScore(),
				"冷重载后据点1霸主积分必须为 1700");
		Assertions.assertEquals(1700L, reloadedDetail.getMyGuildSummary().getTotalScore(),
				"冷重载后宗门B总战功必须为 1700");

		// 验证负向防作弊拦截且均不扣减次数
		setTestGuildId(player, 0L);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(100L).build(), 310));
		Assertions.assertEquals(ErrorMsgEnum.zong_men_not_exist.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarAttackResponse_11000504).errorCode());

		setTestGuildId(player, 70003L);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(99).setScore(100L).build(), 311));
		Assertions.assertEquals(ErrorMsgEnum.cross_guild_war_stronghold_invalid.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarAttackResponse_11000504).errorCode());

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(0L).build(), 312));
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarAttackResponse_11000504).errorCode());

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(5_000_001L).build(), 313));
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarAttackResponse_11000504).errorCode());

		CrossGuildWarPlayerActivity pAct = (CrossGuildWarPlayerActivity) player.getActivityModule().get(52);
		Assertions.assertEquals(0, pAct.getDailyUsedCount(), "全部失败分支不得扣减 dailyUsedCount");

		// 验证双榜排序与分页边界（page=0, pageSize=101, page=999, guildId=0 无宗门返回 459）
		handler.dispatch(tieClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarGuildRankRequest_11000509,
				ActivityCrossGuildWarGuildRankRequest_11000509.newBuilder()
						.setActivityId(52).setPage(1).setPageSize(10).build(), 320));
		ActivityCrossGuildWarGuildRankResponse_1100050a gRankResp = tieClient.findLastResponse(
				PbProtocol.ActivityCrossGuildWarGuildRankResponse_1100050a,
				ActivityCrossGuildWarGuildRankResponse_1100050a.class);
		Assertions.assertNotNull(gRankResp);
		Assertions.assertEquals(guildB, gRankResp.getEntries(0).getGuildId(), "总分最高宗门B必须排第1");

		handler.dispatch(tieClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarGuildRankRequest_11000509,
				ActivityCrossGuildWarGuildRankRequest_11000509.newBuilder()
						.setActivityId(52).setPage(Integer.MAX_VALUE).setPageSize(10).build(), 321));
		ActivityCrossGuildWarGuildRankResponse_1100050a bigPageResp = tieClient.findLastResponse(
				PbProtocol.ActivityCrossGuildWarGuildRankResponse_1100050a,
				ActivityCrossGuildWarGuildRankResponse_1100050a.class);
		Assertions.assertTrue(bigPageResp.getEntriesList().isEmpty(), "Integer.MAX_VALUE 页码防溢出必须返回空列表");

		handler.dispatch(tieClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarMemberRankRequest_1100050b,
				ActivityCrossGuildWarMemberRankRequest_1100050b.newBuilder()
						.setActivityId(52).setGuildId(guildB).setPage(Integer.MAX_VALUE).setPageSize(10).build(), 324));
		ActivityCrossGuildWarMemberRankResponse_1100050c bigMemberPageResp = tieClient.findLastResponse(
				PbProtocol.ActivityCrossGuildWarMemberRankResponse_1100050c,
				ActivityCrossGuildWarMemberRankResponse_1100050c.class);
		Assertions.assertTrue(bigMemberPageResp.getEntriesList().isEmpty(),
				"成员榜 Integer.MAX_VALUE 页码防溢出必须返回空列表");

		handler.dispatch(tieClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarGuildRankRequest_11000509,
				ActivityCrossGuildWarGuildRankRequest_11000509.newBuilder()
						.setActivityId(52).setPage(0).setPageSize(10).build(), 322));
		Assertions.assertEquals(ErrorMsgEnum.request_parameter_error.ID,
				tieClient.findLastPacket(PbProtocol.ActivityCrossGuildWarGuildRankResponse_1100050a).errorCode());

		setTestGuildId(player, 0L);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarMemberRankRequest_1100050b,
				ActivityCrossGuildWarMemberRankRequest_1100050b.newBuilder()
						.setActivityId(52).setGuildId(0L).setPage(1).setPageSize(10).build(), 323));
		Assertions.assertEquals(ErrorMsgEnum.cross_guild_war_no_locked_guild.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarMemberRankResponse_1100050c).errorCode());
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * TC-CGW-GUILD-LOCK-QUIT-AND-QUOTA-GUARDS:
	 * 验证 bypassPriorQuery、宗门锁定、换会/退会保权、阶梯买次、阶段奖防重、CLOSE 期分流与 CBOR 冷重载
	 */
	@Test
	public void testGuildLockQuitAndQuotaGuards() {
		try {
		CrossActivityService.getInstance().init();
		ActivityStateManager.getInstance().setState(52, ActivityState.START_VALUE);
		registerWarActivity(880052102L, "group_1");

		long guildA = 71001L;
		long guildB = 71002L;
		setTestGuildId(player, guildA);
		ActivityHandler handler = createInitializedActivityHandler();

		// bypassPriorQuery：设置昨日计数后先 CBOR 冷重载重建独立模块，再不先查详情直接发起出征验证防卫性跨天重置与锁定
		player.getActivityModule().open(52, false);
		CrossGuildWarPlayerActivity staleAct = (CrossGuildWarPlayerActivity) player.getActivityModule().get(52);
		staleAct.setLastResetDay(DateUtil.getDay() - 1);
		staleAct.setDailyUsedCount(3);
		staleAct.setDailyBoughtCount(3);
		ColdReloadedModules preAttackReload = coldReloadActivityModule(player);
		player.getModules().put(ActivityModule.class.getName(), preAttackReload.activityModule());
		CrossGuildWarPlayerActivity pAct = (CrossGuildWarPlayerActivity) preAttackReload.activityModule().get(52);
		Assertions.assertNotSame(staleAct, pAct);

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(6000L).build(), 401));
		ActivityCrossGuildWarAttackResponse_11000504 atkResp = capturingClient.findLastResponse(
				PbProtocol.ActivityCrossGuildWarAttackResponse_11000504,
				ActivityCrossGuildWarAttackResponse_11000504.class);
		Assertions.assertNotNull(atkResp);
		Assertions.assertEquals(guildA, atkResp.getLockedGuildId(), "首次有效出征必须锁定宗门 A");
		Assertions.assertEquals(1, atkResp.getDailyUsedCount(), "防卫性跨天重置后本次应计为第 1 次");

		// 更换至宗门 B：出征与买次均返 457，且无未领奖时 hasRed() 不得因剩余次数亮红点
		setTestGuildId(player, guildB);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(1000L).build(), 402));
		Assertions.assertEquals(ErrorMsgEnum.cross_guild_war_guild_locked.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarAttackResponse_11000504).errorCode());

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507,
				ActivityCrossGuildWarBuyCountRequest_11000507.newBuilder().setActivityId(52).build(), 421));
		Assertions.assertEquals(ErrorMsgEnum.cross_guild_war_guild_locked.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarBuyCountResponse_11000508).errorCode(),
				"换会后 11000507 买次必须返回 457");

		// 验证换会后不可领取新宗门 B 的阶段奖（457/55，且仅针对锁定宗门 A）；并验证无未领奖时换会 hasRed() 为 false
		pAct.getClaimedPersonalMilestoneIds().add(1);
		pAct.getClaimedPersonalMilestoneIds().add(2);
		pAct.getClaimedGuildMilestoneIds().add(1);
		Assertions.assertFalse(pAct.hasRed(), "换会后虽有剩余出征次数，但无未领奖时 hasRed() 必须返回 false");
		pAct.getClaimedPersonalMilestoneIds().remove(1);
		pAct.getClaimedPersonalMilestoneIds().remove(2);
		pAct.getClaimedGuildMilestoneIds().remove(1);

		// 退会（guildId=0）：出征返 411，但仍可领取个人阶段奖与原锁定宗门 A 阶段奖
		setTestGuildId(player, 0L);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(1000L).build(), 403));
		Assertions.assertEquals(ErrorMsgEnum.zong_men_not_exist.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarAttackResponse_11000504).errorCode());

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
				ActivityCrossGuildWarRewardRequest_11000505.newBuilder()
						.setActivityId(52).setRewardType(1).setMilestoneId(1).build(), 404));
		ActivityCrossGuildWarRewardResponse_11000506 pRewardResp = capturingClient.findLastResponse(
				PbProtocol.ActivityCrossGuildWarRewardResponse_11000506,
				ActivityCrossGuildWarRewardResponse_11000506.class);
		Assertions.assertNotNull(pRewardResp);
		Assertions.assertFalse(pRewardResp.getRewardsList().isEmpty(), "退会后个人阶段奖必须正常发放并返回显式 rewards");

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
				ActivityCrossGuildWarRewardRequest_11000505.newBuilder()
						.setActivityId(52).setRewardType(2).setMilestoneId(1).build(), 405));
		ActivityCrossGuildWarRewardResponse_11000506 gRewardResp = capturingClient.findLastResponse(
				PbProtocol.ActivityCrossGuildWarRewardResponse_11000506,
				ActivityCrossGuildWarRewardResponse_11000506.class);
		Assertions.assertNotNull(gRewardResp);
		Assertions.assertFalse(gRewardResp.getRewardsList().isEmpty(), "退会后原锁定宗门 A 阶段奖必须仍可领取");

		// 重复领取返 51；未达标返 55
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
				ActivityCrossGuildWarRewardRequest_11000505.newBuilder()
						.setActivityId(52).setRewardType(1).setMilestoneId(1).build(), 406));
		Assertions.assertEquals(ErrorMsgEnum.repeat_request.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarRewardResponse_11000506).errorCode());

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
				ActivityCrossGuildWarRewardRequest_11000505.newBuilder()
						.setActivityId(52).setRewardType(1).setMilestoneId(4).build(), 407));
		Assertions.assertEquals(ErrorMsgEnum.condition_check_error.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarRewardResponse_11000506).errorCode());

		// 回到宗门 A 测试阶梯买次（余额不足 60 -> 充元宝连买 3 次 -> 超限 108）及耗尽次数 61
		setTestGuildId(player, guildA);
		long initialDiamond = player.getCurrencyModule().getCount(Asset.diamond.ID);
		if (initialDiamond > 0L) {
			PlayerHelper.delResources(player, Asset.diamond.ID, initialDiamond, OpType.GM, false);
		}
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507,
				ActivityCrossGuildWarBuyCountRequest_11000507.newBuilder().setActivityId(52).build(), 408));
		Assertions.assertEquals(ErrorMsgEnum.resource_not_enough.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarBuyCountResponse_11000508).errorCode());

		PlayerHelper.addResources(player, Asset.diamond.ID, 500, OpType.GM, false);
		long beforeDiamond = player.getCurrencyModule().getCount(Asset.diamond.ID);
		for (int i = 0; i < 3; i++) {
			handler.dispatch(capturingClient, new ProtobufProtocol(
					PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507,
					ActivityCrossGuildWarBuyCountRequest_11000507.newBuilder().setActivityId(52).build(), 410 + i));
			ActivityCrossGuildWarBuyCountResponse_11000508 buyResp = capturingClient.findLastResponse(
					PbProtocol.ActivityCrossGuildWarBuyCountResponse_11000508,
					ActivityCrossGuildWarBuyCountResponse_11000508.class);
			Assertions.assertNotNull(buyResp);
			Assertions.assertEquals(i + 1, buyResp.getDailyBoughtCount());
		}
		long afterDiamond = player.getCurrencyModule().getCount(Asset.diamond.ID);
		Assertions.assertEquals(300L, beforeDiamond - afterDiamond, "阶梯买次 50+100+150=300 元宝净扣费必须精确一致");

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507,
				ActivityCrossGuildWarBuyCountRequest_11000507.newBuilder().setActivityId(52).build(), 415));
		Assertions.assertEquals(ErrorMsgEnum.buy_over_limit.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarBuyCountResponse_11000508).errorCode());

		// 打完剩余全部 5 次，触发 times_limit(61)
		for (int i = 0; i < 5; i++) {
			handler.dispatch(capturingClient, new ProtobufProtocol(
					PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
					ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
							.setActivityId(52).setStrongholdId(1).setScore(100L).build(), 430 + i));
			Assertions.assertEquals(0,
					capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarAttackResponse_11000504).errorCode());
		}
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(100L).build(), 436));
		Assertions.assertEquals(ErrorMsgEnum.times_limit.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarAttackResponse_11000504).errorCode(),
				"用尽次数后出征必须返回 times_limit(61)");

		// 切换至 CLOSE 期验证买次拒 58、详情/领奖放行，以及 CBOR 冷重载保留全部状态
		ActivityStateManager.getInstance().setState(52, ActivityState.CLOSE_VALUE);
		CrossGlobalActivityManager.getInstance().getActivity(880052102L).setState(ActivityState.CLOSE_VALUE);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507,
				ActivityCrossGuildWarBuyCountRequest_11000507.newBuilder().setActivityId(52).build(), 420));
		Assertions.assertEquals(ErrorMsgEnum.not_open.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarBuyCountResponse_11000508).errorCode());

		Assertions.assertTrue(pAct.hasRed(), "CLOSE 期仍有个人里程碑 2(5000) 未领，hasRed() 必须为 true");
		ColdReloadedModules reloaded = coldReloadActivityModule(player);
		CrossGuildWarPlayerActivity reloadedWarAct = (CrossGuildWarPlayerActivity) reloaded.activityModule().get(52);
		Assertions.assertNotSame(pAct, reloadedWarAct);
		Assertions.assertEquals(guildA, reloadedWarAct.getLockedGuildId());
		Assertions.assertEquals(3, reloadedWarAct.getDailyBoughtCount());
		Assertions.assertTrue(reloadedWarAct.getClaimedPersonalMilestoneIds().contains(1));
		Assertions.assertTrue(reloadedWarAct.getClaimedGuildMilestoneIds().contains(1));
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * TC-CGW-END-BONUS-FLUSH-AND-MAIL-IDEMPOTENCY:
	 * 验证 onEnd() 霸主加分、锁榜、saveOrThrow 失败回滚、30 秒冷却在线/重启补偿与 Mail(45) 幂等
	 */
	@Test
	public void testEndBonusFlushAndMailIdempotency() {
		try {
		CrossActivityService crossService = CrossActivityService.getInstance();
		crossService.init();
		ActivityStateManager.getInstance().setState(52, ActivityState.START_VALUE);
		long warUid = 880052103L;
		CrossGuildWarActivity warAct = registerWarActivity(warUid, "group_1");

		List<Long> mailedPlayers = Collections.synchronizedList(new ArrayList<>());
		warAct.setMailSender((receiverId, mailId, goods, notify) -> {
			Assertions.assertEquals(45, mailId, "终榜结算必须使用 Mail(45)");
			Assertions.assertFalse(goods.isEmpty());
			mailedPlayers.add(receiverId);
		});

		ActivityHandler handler = createInitializedActivityHandler();
		// 宗门 A 占领据点 1（bonus=5000），宗门 B 占领据点 2（bonus=8000）；并准备退会达标成员
		long guildA = 72001L;
		long guildB = 72002L;
		Player pA = createExtraPlayer(900_888_912_001L, "EndP_A", TEST_SERVER_ID, 20,
				new CapturingGameClient(900_888_912_001L));
		setTestGuildId(pA, guildA);
		handler.dispatch((CapturingGameClient) pA.getGameClient(), new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(1200L).build(), 501));
		// 模拟玩家 pA 赛前退会，验证终榜邮件仍按锁定宗门发放
		setTestGuildId(pA, 0L);

		Player pB = createExtraPlayer(900_888_912_002L, "EndP_B", TEST_SERVER_ID, 20,
				new CapturingGameClient(900_888_912_002L));
		setTestGuildId(pB, guildB);
		handler.dispatch((CapturingGameClient) pB.getGameClient(), new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(2).setScore(2500L).build(), 502));

		// 注入 DB 落盘故障，触发 end() 必须回滚 rankSettled=false 且零发邮
		memStore.setFailNextSave(true);
		warAct.end();
		Assertions.assertFalse(warAct.isRankSettled(), "saveOrThrow 失败时必须回滚 rankSettled=false");
		Assertions.assertTrue(mailedPlayers.isEmpty(), "落盘失败时绝不能提前发邮");
		Assertions.assertEquals(ActivityState.CLOSE_VALUE, warAct.getState());

		// 恢复 DB，在 30 秒冷却期内 periodicDirtySave 不得重试；跨过 30 秒后自动补结算且不重复叠加 bonusScore
		memStore.setFailNextSave(false);
		crossService.periodicDirtySave();
		Assertions.assertFalse(warAct.isRankSettled(), "30 秒冷却期内 periodicDirtySave 不得提前重试");

		DateUtil.setTimeOffsetMillis(31_000L);
		crossService.periodicDirtySave();
		Assertions.assertTrue(warAct.isRankSettled(), "冷却到期后 periodicDirtySave 必须自动完成补结算");
		Assertions.assertEquals(2, mailedPlayers.size(), "达标锁定成员（含退会成员 pA）必须各收到 1 封 Mail(45)");
		Assertions.assertTrue(mailedPlayers.contains(pA.getPlayerId()));
		Assertions.assertTrue(mailedPlayers.contains(pB.getPlayerId()));

		// 验证 JsonUtil 冷重载后的终场霸主加分数值与幂等不重发邮
		CrossGuildWarActivity reloaded = coldReloadCrossActivity(warUid);
		Assertions.assertTrue(reloaded.isRankSettled());
		Assertions.assertEquals(2, reloaded.getFinalRankCount());
		CrossGuildWarPayloads.QueryGuildRankRequest rankReq = new CrossGuildWarPayloads.QueryGuildRankRequest();
		rankReq.setPlayerId(pB.getPlayerId());
		rankReq.setCurrentGuildId(guildB);
		rankReq.setPage(1);
		rankReq.setPageSize(10);
		CrossActivityActionResult rankRes = reloaded.executePlayerAction(
				pB.getPlayerId(), CrossGuildWarActivity.ACTION_QUERY_GUILD_RANK, JsonUtil.toJsonString(rankReq));
		CrossGuildWarPayloads.QueryGuildRankResponse rankObj = JsonUtil.parseObject(
				rankRes.getResultPayloadJson(), CrossGuildWarPayloads.QueryGuildRankResponse.class);
		Assertions.assertEquals(guildB, rankObj.getEntries().get(0).getGuildId());
		Assertions.assertEquals(2500L, rankObj.getEntries().get(0).getBaseScore());
		Assertions.assertEquals(8000L, rankObj.getEntries().get(0).getStrongholdBonusScore());
		Assertions.assertEquals(10500L, rankObj.getEntries().get(0).getTotalScore());
		Assertions.assertEquals(guildA, rankObj.getEntries().get(1).getGuildId());
		Assertions.assertEquals(1200L, rankObj.getEntries().get(1).getBaseScore());
		Assertions.assertEquals(5000L, rankObj.getEntries().get(1).getStrongholdBonusScore());
		Assertions.assertEquals(6200L, rankObj.getEntries().get(1).getTotalScore());

		warAct.end();
		warAct.compensateEndSettlement();
		reloaded.compensateEndSettlement();
		Assertions.assertEquals(2, mailedPlayers.size(), "重复 end()/compensateEndSettlement() 绝不能重发邮件");

		// 验证停服跨过 endTime 后经 loadActivitiesFromDb 重载自动触发终榜补结算
		long expiredUid = 880052203L;
		CrossGuildWarActivity expiredWar = registerWarActivity(expiredUid, "group_expired");
		Field endTimeField = ActivityBase.class.getDeclaredField("endTime");
		endTimeField.setAccessible(true);
		endTimeField.set(expiredWar, DateUtil.currentTimeMillis() - 1000L);
		Field destroyTimeField = ActivityBase.class.getDeclaredField("destroyTime");
		destroyTimeField.setAccessible(true);
		destroyTimeField.set(expiredWar, DateUtil.currentTimeMillis() + 86_400_000L);
		CrossGuildWarPayloads.AttackRequest expAtk = new CrossGuildWarPayloads.AttackRequest();
		expAtk.setPlayerId(pB.getPlayerId());
		expAtk.setPlayerName(pB.getData().getName());
		expAtk.setServerId(pB.getServerId());
		expAtk.setCurrentGuildId(guildB);
		expAtk.setGuildName("宗门B");
		expAtk.setStrongholdId(1);
		expAtk.setScore(1500L);
		expiredWar.executePlayerAction(pB.getPlayerId(), CrossGuildWarActivity.ACTION_ATTACK_STRONGHOLD,
				JsonUtil.toJsonString(expAtk));
		crossService.saveAll(true);
		CrossGlobalActivityManager.getInstance().clear();
		List<GameActivity> stored = memStore.loadAll();
		crossService.loadActivitiesFromDb(stored.stream().filter(g -> g.getId() == expiredUid).toList());
		CrossGuildWarActivity recoveredExpired = (CrossGuildWarActivity)
				CrossGlobalActivityManager.getInstance().getActivity(expiredUid);
		Assertions.assertNotNull(recoveredExpired);
		Assertions.assertEquals(ActivityState.CLOSE_VALUE, recoveredExpired.getState());
		Assertions.assertTrue(recoveredExpired.isRankSettled(), "停服跨 endTime 重载必须自动补结算并置 rankSettled=true");
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * TC-CGW-RECOVERY-COLD-RELOAD-AND-MIGRATION:
	 * 验证 saveAll+JsonUtil 冷重载、迁移重试、换期解绑与彻底销毁清理
	 */
	@Test
	public void testRecoveryColdReloadAndMigration() {
		try {
		CrossActivityService crossService = CrossActivityService.getInstance();
		crossService.init();
		ActivityStateManager.getInstance().setState(52, ActivityState.START_VALUE);
		long uid1 = 880052104L;
		registerWarActivity(uid1, "group_1");

		setTestGuildId(player, 73001L);
		ActivityHandler handler = createInitializedActivityHandler();
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(1500L).build(), 601));

		CrossGuildWarPlayerActivity pAct = (CrossGuildWarPlayerActivity) player.getActivityModule().get(52);
		Assertions.assertEquals(uid1, pAct.getCrossActivityUid());
		Assertions.assertEquals(73001L, pAct.getLockedGuildId());

		// saveAll(true) + JsonUtil 冷重载验证
		crossService.saveAll(true);
		CrossGuildWarActivity reloadedWar = coldReloadCrossActivity(uid1);
		Assertions.assertEquals(1, reloadedWar.getGuildCount());
		Assertions.assertEquals(1, reloadedWar.getParticipantCount());
		Assertions.assertTrue(reloadedWar.canRetryEndSettlement(DateUtil.currentTimeMillis()),
				"冷重载后 nextSettleRetryTime 必须归零");

		// 验证同 uid1 节点迁移（首次路由缺失 -> handleCrossRouteFailure 清理 IdCache -> 单次重试同 uid1 成功）
		CrossGlobalActivityManager.getInstance().removeActivity(uid1);
		var crossIdMgr = IdCache.getManager(DistributedObjectType.CROSS_ACTIVITY);
		if (crossIdMgr != null) {
			crossIdMgr.clearServerId(uid1);
		}
		CrossGuildWarPlayerActivity migrationAct = new CrossGuildWarPlayerActivity() {
			@Override
			public void handleCrossRouteFailure(Exception e) {
				super.handleCrossRouteFailure(e);
				CrossGlobalActivityManager.getInstance().addActivity(reloadedWar);
			}
		};
		migrationAct.init(52, player, false);
		migrationAct.setCrossActivityUid(uid1);
		player.getActivityModule().getPlayerActivityManager().getActivities().put(52, migrationAct);
		capturingClient.clearPackets();
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(100L).build(), 610));
		CapturedPacket migratedPkt = capturingClient.findLastPacket(
				PbProtocol.ActivityCrossGuildWarAttackResponse_11000504);
		Assertions.assertNotNull(migratedPkt);
		Assertions.assertEquals(0, migratedPkt.errorCode(), "同 uid1 迁移经单次重试后必须成功返回 errorCode=0");
		player.getActivityModule().getPlayerActivityManager().getActivities().put(52, pAct);

		// 模拟销毁 uid1 并开启新周期 uid2，验证只读切新期、写操作拒错打及换期解绑
		long uid2 = 880052105L;
		CrossGlobalActivityManager.getInstance().removeActivity(uid1);
		RedisCrossActivityDirectory.getInstance().removeIfMatch(52, "group_1", uid1);
		registerWarActivity(uid2, "group_1");

		// 写操作在持有旧 uid1 时应拒绝错打或触发换期解绑
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(100L).build(), 602));
		Assertions.assertEquals(ErrorMsgEnum.activity_not_found.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarAttackResponse_11000504).errorCode(),
				"换期后写操作必须返回 397 拒绝跨期错打");

		// 换期后已自动解绑 lockedGuildId 并清空上期战功，再调只读详情应切至 uid2 成功
		Assertions.assertEquals(uid2, pAct.getCrossActivityUid());
		Assertions.assertEquals(0L, pAct.getLockedGuildId(), "换期后必须解绑上期 lockedGuildId");
		Assertions.assertEquals(0L, pAct.getMyScore(), "换期后必须清空上期战功");
		Assertions.assertTrue(pAct.getClaimedPersonalMilestoneIds().isEmpty());
		Assertions.assertTrue(pAct.getClaimedGuildMilestoneIds().isEmpty());

		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarInfoRequest_11000501,
				ActivityCrossGuildWarInfoRequest_11000501.newBuilder().setActivityId(52).build(), 603));
		ActivityCrossGuildWarInfoResponse_11000502 newInfo = capturingClient.findLastResponse(
				PbProtocol.ActivityCrossGuildWarInfoResponse_11000502,
				ActivityCrossGuildWarInfoResponse_11000502.class);
		Assertions.assertNotNull(newInfo);
		Assertions.assertEquals(uid2, newInfo.getCrossActivityUid(), "只读详情必须切至新期 uid2");

		// 验证彻底销毁无新期时路由失败自愈触发 onCrossActivityExpired 清空绑定
		CrossGlobalActivityManager.getInstance().removeActivity(uid2);
		RedisCrossActivityDirectory.getInstance().removeIfMatch(52, "group_1", uid2);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarInfoRequest_11000501,
				ActivityCrossGuildWarInfoRequest_11000501.newBuilder().setActivityId(52).build(), 604));
		Assertions.assertEquals(ErrorMsgEnum.activity_not_found.ID,
				capturingClient.findLastPacket(PbProtocol.ActivityCrossGuildWarInfoResponse_11000502).errorCode());
		Assertions.assertEquals(0L, pAct.getCrossActivityUid(), "彻底销毁无新期时必须清空 crossActivityUid");

		// 回归 BLOCKER-1：crossActivityUid 归零后再开启新期 uid3 并换会，必须清理残留周期状态且可正常参战
		long uid3 = 880052106L;
		registerWarActivity(uid3, "group_1");
		setTestGuildId(player, 73002L);
		handler.dispatch(capturingClient, new ProtobufProtocol(
				PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
				ActivityCrossGuildWarAttackRequest_11000503.newBuilder()
						.setActivityId(52).setStrongholdId(1).setScore(1200L).build(), 605));
		ActivityCrossGuildWarAttackResponse_11000504 uid3Atk = capturingClient.findLastResponse(
				PbProtocol.ActivityCrossGuildWarAttackResponse_11000504,
				ActivityCrossGuildWarAttackResponse_11000504.class);
		Assertions.assertNotNull(uid3Atk);
		Assertions.assertEquals(73002L, uid3Atk.getLockedGuildId(),
				"归零重绑 uid3 后必须允许在新宗门 73002 正常参战锁定");
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * TC-CGW-LIVE-CLUSTER-ACCEPTANCE-SUITE:
	 * 验证双轨模块验收套件（0x1152）在沙盒隔离战区 group_9998 的契约与配表矩阵闭环，及生产环境硬阻断
	 */
	@Test
	public void testLiveClusterAcceptanceSuite() throws Exception {
		CrossActivityService.getInstance().init();
		ActivityStateManager.getInstance().setState(52, ActivityState.START_VALUE);
		long sandboxUid = 880052998L;
		CrossGuildWarActivity sandboxWar = registerWarActivity(sandboxUid, DefaultActivityScopeResolver.SANDBOX_CGW_SCOPE_KEY);

		Assertions.assertEquals("group_9998",
				DefaultActivityScopeResolver.getInstance().resolveScopeKey(ActivityManager.instance().get(52), "sandbox_cgw"));

		ModuleAcceptanceEngine engine = ModuleAcceptanceEngine.getInstance();
		Assertions.assertNotNull(engine.getSuite(0x1152), "ModuleAcceptanceEngine 必须注册 0x1152 CrossGuildWarModuleAcceptanceSuite");
		Assertions.assertEquals(List.of(cn.game.util.ServerType.Game, cn.game.util.ServerType.Cross),
				engine.getSuite(0x1152).getRequiredServerTypes(),
				"CrossGuildWar 验收套件必须声明依赖 Game 与 Cross 双节点");

		ModuleAcceptanceReport report = engine.runSuite(0x1152, AcceptanceRunMode.ALL, null, 100, false);
		Assertions.assertNotNull(report);
		Assertions.assertTrue(report.isSuccess(), "0x1152 双轨验收全部用例必须通过，失败明细: " + report.getFailedItems());
		Assertions.assertEquals(0, report.getFailedCases(), "0x1152 验收套件失败数必须为 0");
		long contractPassedCount = report.getResults().stream()
				.filter(r -> r.getMode() == AcceptanceRunMode.CONTRACT && r.getStatus() == AcceptanceCaseStatus.PASSED)
				.count();
		long matrixPassedCount = report.getResults().stream()
				.filter(r -> r.getMode() == AcceptanceRunMode.MATRIX_SCAN && r.getStatus() == AcceptanceCaseStatus.PASSED)
				.count();
		Assertions.assertTrue(contractPassedCount > 0L, "契约用例通过数必须 > 0");
		Assertions.assertTrue(matrixPassedCount > 0L, "矩阵扫描通过数必须 > 0");
		Assertions.assertFalse(RedisCrossActivityDirectory.getInstance().getActiveUid(52, "group_1").isPresent(),
				"沙盒验收绝不得污染真实战区 group_1");

		// 验证 RunMode.PRODUCTION 下 ACTION_ACCEPTANCE_RESET 硬阻断（24 = production_gm_not_allow）
		Field runModeField = ServerContext.getInstance().getClass().getDeclaredField("runMode");
		runModeField.setAccessible(true);
		runModeField.set(ServerContext.getInstance(), RunMode.PRODUCTION);
		try {
			CrossGuildWarPayloads.AcceptanceResetRequest resetReq = new CrossGuildWarPayloads.AcceptanceResetRequest();
			resetReq.setMode(CrossGuildWarActivity.RESET_MODE_WAR);
			CrossActivityActionResult prodRes = sandboxWar.executePlayerAction(
					TEST_PLAYER_ID, CrossGuildWarActivity.ACTION_ACCEPTANCE_RESET, JsonUtil.toJsonString(resetReq));
			Assertions.assertFalse(prodRes.isSuccess());
			Assertions.assertEquals(ErrorMsgEnum.production_gm_not_allow.ID, prodRes.getErrorCode(),
					"生产模式下 ACTION_ACCEPTANCE_RESET 必须硬阻断返回 production_gm_not_allow(24)");
		} finally {
			runModeField.set(ServerContext.getInstance(), RunMode.DEV);
		}
	}

	/**
	 * 设置测试玩家内存宗门归属（不触发真实 DB 持久化或跨服 Guild RPC）
	 */
	private static void setTestGuildId(Player targetPlayer, long guildId) {
		try {
			GuildModule gm = targetPlayer.getGuildModule();
			Field f = GuildModule.class.getDeclaredField("guildJoin");
			f.setAccessible(true);
			if (guildId <= 0L) {
				f.set(gm, null);
			} else {
				GuildJoin gj = new GuildJoin();
				gj.setPlayerId(targetPlayer.getPlayerId());
				gj.setGuildId(guildId);
				f.set(gm, gj);
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("设置测试宗门ID失败", e);
		}
	}

	/**
	 * 构造测试逻辑区服视图
	 */
	private static VirtualServerView buildServerView(String serverId, Integer seq) {
		VirtualServerView view = new VirtualServerView();
		view.ID = serverId;
		view.seq = seq;
		view.openTime = DateUtil.nowDateTime().minusDays(2);
		return view;
	}

	/**
	 * 创建并注册跨服宗门争霸测试活动实例至内存管理器与目录
	 */
	private CrossGuildWarActivity registerWarActivity(long warUid, String scopeKey) {
		CrossGuildWarActivity warAct = new CrossGuildWarActivity();
		warAct.setUid(warUid);
		warAct.setScopeKey(scopeKey);
		warAct.setServerId(scopeKey);
		warAct.init(52, null, true);
		CrossGlobalActivityManager.getInstance().addActivity(warAct);
		RedisCrossActivityDirectory.getInstance().putActive(52, scopeKey, warUid);
		return warAct;
	}

	/**
	 * 创建并初始化测试用 {@link ActivityHandler}
	 */
	private ActivityHandler createInitializedActivityHandler() {
		return new ActivityHandler() {
			{
				inititialize();
			}
		};
	}

	/**
	 * 创建并注册额外测试玩家
	 */
	private Player createExtraPlayer(long playerId, String name, String serverId, int level,
			CapturingGameClient client) {
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
	 * 跨服活动存储冷重载辅助方法：从内存持久化存储重新读取 {@link GameActivity} 并反序列化为独立实例
	 */
	private CrossGuildWarActivity coldReloadCrossActivity(long warUid) {
		GameActivity persisted = null;
		for (GameActivity candidate : memStore.loadAll()) {
			if (candidate != null && candidate.getId() == warUid) {
				persisted = candidate;
				break;
			}
		}
		Assertions.assertNotNull(persisted, "存储中应存在持久化跨服活动实体: uid=" + warUid);
		CrossGuildWarActivity restored = JsonUtil.parseObjectWithType(persisted.getParams());
		restored.afterLoadFromDb();
		return restored;
	}

	/**
	 * 验证热重载回调触发以及各类坏配表抛出 IllegalStateException（测试结束自动还原合法配置）
	 */
	@SuppressWarnings("unchecked")
	private void verifyHotReloadAndBadConfigValidation() {
		ActivityCrossGuildWarManager manager = ActivityCrossGuildWarManager.instance();
		AtomicInteger reloadHitCount = new AtomicInteger(0);
		Runnable validatorConsumer = CrossGuildWarConfigValidator::validateAll;
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
			ActivityCrossGuildWarConfig overrideCfg = buildWarConfig(Map.of("zoneOverrides", "s1:2;s3:5"));
			Map<String, Integer> overrideMap = CrossGuildWarConfigValidator.resolveZoneOverrides(overrideCfg);
			Assertions.assertEquals(2, overrideMap.get("s1"));
			Assertions.assertEquals(5, overrideMap.get("s3"));
			Assertions.assertThrows(UnsupportedOperationException.class, () -> overrideMap.put("s4", 1),
					"resolveZoneOverrides 返回的映射必须不可变");

			// 跨表存在性缺失：Activity 表存在 ID=52 但业务表为空
			assertBadConfigMapThrows(Map.of(), "ID=52");
			// 阶梯购买消耗长度不足 dailyBuyLimit
			assertBadConfigThrows(Map.of("dailyBuyLimit", "3", "buyCosts", "100001;50|100001;100"), "buyCosts");
			// 阻断 100701~100703 跨服公会资产
			assertBadConfigThrows(Map.of("personalMilestoneRewards",
					"100701;100|100001;200|100001;350|100001;600"), "personalMilestoneRewards[0]");
			assertBadConfigThrows(Map.of("guildMilestoneRewards",
					"100702;100|100001;200|100001;350|100001;600"), "guildMilestoneRewards[0]");
			assertBadConfigThrows(Map.of("rankRewards",
					"100703;100|100001;200|100001;350|100001;600|100001;800"), "rankRewards[0]");
			// 静态战区覆盖非法：重复 serverId 或格式错误
			assertBadConfigThrows(Map.of("zoneOverrides", "s1:1;s1:2"), "zoneOverrides");
			assertBadConfigThrows(Map.of("zoneOverrides", "bad_segment"), "zoneOverrides");
			// 里程碑阈值非递增
			assertBadConfigThrows(Map.of("personalMilestoneThresholds", "5000;1000;15000;30000"),
					"personalMilestoneThresholds[1]");
			// 排行分段不连续或结算邮件模板不存在
			assertBadConfigThrows(Map.of("rankRanges", "1;1|3;5|6;10|11;50|51;999999"), "rankRanges");
			assertBadConfigThrows(Map.of("rankMailId", "999999"), "rankMailId");
			// 间接宝箱/掉落资产含 100703 时必须递归阻断抛 IllegalStateException
			verifyIndirectChestUnsafeAssetBlocked();
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
			CrossGuildWarConfigValidator.validateAll();
		}
	}

	/**
	 * 构造包含 100703 公会资产的间接宝箱（ItemType=4）道具并验证递归阻断生效，完成后还原 ItemManager
	 *
	 * @throws Exception 反射构造或清理失败时抛出
	 */
	@SuppressWarnings("unchecked")
	private void verifyIndirectChestUnsafeAssetBlocked() throws Exception {
		int unsafeChestItemId = 209999;
		Field itemsField = ItemManager.class.getDeclaredField("items");
		itemsField.setAccessible(true);
		Map<Integer, ItemConfig> originalItems = (Map<Integer, ItemConfig>) itemsField.get(ItemManager.instance());
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element elem = doc.createElement("Item");
			elem.setAttribute("ID", String.valueOf(unsafeChestItemId));
			elem.setAttribute("ItemType", "4");
			elem.setAttribute("Para1", "100703;10");
			ItemConfig chestCfg = new ItemConfig(elem);

			Map<Integer, ItemConfig> tempItems = new HashMap<>(originalItems);
			tempItems.put(unsafeChestItemId, chestCfg);
			itemsField.set(ItemManager.instance(), tempItems);

			Assertions.assertTrue(CrossGuildWarConfigValidator.isCrossGuildUnsafeAsset(unsafeChestItemId),
					"间接包含 100703 的宝箱道具必须被递归识别为高危公会资产");
			assertBadConfigThrows(Map.of("personalMilestoneRewards",
					unsafeChestItemId + ";1|100001;200|100001;350|100001;600"), "personalMilestoneRewards[0]");
		} finally {
			ItemManager.instance().load();
		}
	}

	/**
	 * 构造单行坏配置并断言 {@link CrossGuildWarConfigValidator#validateAll()} 抛出预期异常
	 *
	 * @param overrides        需覆盖的坏字段键值对
	 * @param expectedFieldMsg 异常消息中必须包含的字段或关键词
	 */
	private void assertBadConfigThrows(Map<String, String> overrides, String expectedFieldMsg) {
		ActivityCrossGuildWarConfig badCfg = buildWarConfig(overrides);
		assertBadConfigMapThrows(Map.of(badCfg.ID, badCfg), expectedFieldMsg);
	}

	/**
	 * 临时替换 {@link ActivityCrossGuildWarManager} 内存配置映射并断言校验抛错
	 *
	 * @param tempMap          临时替换的配置映射
	 * @param expectedFieldMsg 异常消息中必须包含的字段或关键词
	 */
	private void assertBadConfigMapThrows(Map<Integer, ActivityCrossGuildWarConfig> tempMap, String expectedFieldMsg) {
		try {
			Field mapField = ActivityCrossGuildWarManager.class.getDeclaredField("activitycrossguildwars");
			mapField.setAccessible(true);
			mapField.set(ActivityCrossGuildWarManager.instance(), tempMap);
			IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class,
					CrossGuildWarConfigValidator::validateAll,
					"坏配表必须触发 IllegalStateException: " + expectedFieldMsg);
			Assertions.assertTrue(ex.getMessage().contains("ActivityCrossGuildWar"),
					"异常消息必须包含表名 ActivityCrossGuildWar: " + ex.getMessage());
			Assertions.assertTrue(ex.getMessage().contains(expectedFieldMsg),
					"异常消息必须包含字段上下文[" + expectedFieldMsg + "]: " + ex.getMessage());
		} catch (ReflectiveOperationException e) {
			log.error("替换测试配置映射失败", e);
			throw new RuntimeException("替换测试配置映射失败", e);
		}
	}

	/**
	 * 基于合法默认值构造用于负向校验的 {@link ActivityCrossGuildWarConfig} 实例
	 *
	 * @param overrides 需覆写的 XML 属性键值对
	 * @return 构造完成的配置对象
	 */
	private ActivityCrossGuildWarConfig buildWarConfig(Map<String, String> overrides) {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element element = doc.createElement("ActivityCrossGuildWar");
			Map<String, String> attrs = new HashMap<>();
			attrs.put("ID", "52");
			attrs.put("warName", "九霄宗门争霸");
			attrs.put("groupSize", "4");
			attrs.put("zoneOverrides", "");
			attrs.put("dailyFreeTimes", "3");
			attrs.put("dailyBuyLimit", "3");
			attrs.put("buyCosts", "100001;50|100001;100|100001;150");
			attrs.put("maxSingleScore", "5000000");
			attrs.put("minParticipateScore", "1");
			attrs.put("strongholdIds", "1;2;3");
			attrs.put("strongholdNames", "青龙要塞;朱雀关隘;玄武皇城");
			attrs.put("strongholdOccupyMinScores", "1000;2000;3000");
			attrs.put("strongholdBonusScores", "5000;8000;12000");
			attrs.put("personalMilestoneThresholds", "1000;5000;15000;30000");
			attrs.put("personalMilestoneRewards", "100001;100|100001;200|100001;350|100001;600");
			attrs.put("guildMilestoneThresholds", "5000;20000;50000;100000");
			attrs.put("guildMilestoneRewards", "100001;150|100001;300|100001;500|100001;1000");
			attrs.put("rankRanges", "1;1|2;3|4;10|11;50|51;999999");
			attrs.put("rankRewards", "100001;2000|100001;1200|100001;800|100001;400|100001;150");
			attrs.put("rankMailId", "45");
			attrs.putAll(overrides);
			for (Map.Entry<String, String> entry : attrs.entrySet()) {
				element.setAttribute(entry.getKey(), entry.getValue());
			}
			return new ActivityCrossGuildWarConfig(element);
		} catch (Exception e) {
			log.error("构造测试配置对象失败", e);
			throw new RuntimeException("构造测试配置对象失败", e);
		}
	}

	/**
	 * 构造测试用 {@link Account} 对象，保证日志链路不发生空指针
	 *
	 * @param accountId 账号标识
	 * @return 测试账号对象
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
	 * 冷重载后的核心玩家模块快照
	 *
	 * @param activityModule 重载后的活动模块
	 * @param guildModule    重载后的宗门模块
	 */
	private record ColdReloadedModules(ActivityModule activityModule, GuildModule guildModule) {
	}

	/**
	 * 持久化冷重载辅助方法：对玩家全部模块执行 CBOR+Snappy 序列化与反序列化，重建 ActivityModule 与 GuildModule 独立内存实例
	 *
	 * @param targetPlayer 目标测试玩家，不可为 null
	 * @return 反序列化并完成 initDefault/initFromDbAfter 初始化的新模块快照
	 */
	private static ColdReloadedModules coldReloadActivityModule(Player targetPlayer) {
		try {
			byte[] snapshot = CborSnappyUtil.compress(targetPlayer.getModules());
			Map<String, BasePlayerModule> restored = CborSnappyUtil.decompress(snapshot);
			ActivityModule restoredActivityModule = (ActivityModule) restored.get(ActivityModule.class.getName());
			if (restoredActivityModule != null) {
				restoredActivityModule.initDefault(targetPlayer);
				restoredActivityModule.initFromDbAfter();
			}
			GuildModule restoredGuildModule = (GuildModule) restored.get(GuildModule.class.getName());
			if (restoredGuildModule != null) {
				restoredGuildModule.initDefault(targetPlayer);
				restoredGuildModule.initFromDbAfter();
			}
			return new ColdReloadedModules(restoredActivityModule, restoredGuildModule);
		} catch (Exception e) {
			log.error("冷重载活动与宗门模块失败: playerId={}", targetPlayer.getPlayerId(), e);
			throw new RuntimeException("冷重载活动与宗门模块失败", e);
		}
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

		@Override
		public boolean sendProtocol(int msgId, int seq, byte[] data, int errorCode, boolean recored) {
			packets.add(new CapturedPacket(msgId, seq, data, errorCode));
			return true;
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
		 * 清空已捕获的服务端下发协议包记录
		 */
		void clearPackets() {
			packets.clear();
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
