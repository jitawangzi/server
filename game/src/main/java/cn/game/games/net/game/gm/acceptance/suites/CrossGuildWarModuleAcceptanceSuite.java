package cn.game.games.net.game.gm.acceptance.suites;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.id.IdUtil;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.common.module.activity.DefaultActivityScopeResolver;
import cn.game.games.net.cross.activity.CrossActivityRouteMissingException;
import cn.game.games.net.cross.activity.RedisCrossActivityDirectory;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.cross.activity.dto.CrossActivityCreateResult;
import cn.game.games.net.cross.activity.dto.CrossActivitySnapshot;
import cn.game.games.net.cross.activity.dto.CrossGuildWarPayloads.AcceptanceResetRequest;
import cn.game.games.net.cross.activity.impl.CrossGuildWarActivity;
import cn.game.games.net.cross.activity.service.CrossActivityService;
import cn.game.games.net.cross.activity.service.CrossActivityServiceInterface;
import cn.game.games.net.game.gm.acceptance.AcceptanceContext;
import cn.game.games.net.game.gm.acceptance.AcceptanceFixtureException;
import cn.game.games.net.game.gm.acceptance.ConditionSatisfier;
import cn.game.games.net.game.gm.acceptance.CostProvisioner;
import cn.game.games.net.game.gm.acceptance.InProcessAcceptanceKit;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceEngine;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.SandboxResetter;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ActivityHandler;
import cn.game.games.net.game.module.activity.impl.player.CrossGuildWarPlayerActivity;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.ActivityCrossGuildWarConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ActivityCrossGuildWarManager;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;

/**
 * 跨服宗门争霸模块双轨自动化验收套件 (0x1152)。
 * <p>
 * 职责边界：在非生产环境下将沙盒玩家映射至物理隔离战区 {@code group_9998}（与线上 {@code group_1} 及
 * {@code CrossWorldBoss} {@code group_9999} 彻底隔离），依次验证功能契约与全量 {@code ActivityCrossGuildWarConfig}
 * 配表矩阵扫描，严禁污染真实 Cross Guild。
 * </p>
 *
 * @author shuyongqiang
 */
public class CrossGuildWarModuleAcceptanceSuite implements ModuleAcceptanceSuite {

	/** 套件日志器 */
	private static final Logger log = LoggerFactory.getLogger(CrossGuildWarModuleAcceptanceSuite.class);

	/** 所属独立验收模块号（避开 0x11 CrossWorldBoss 撞车） */
	public static final int MODULE_ID = 0x1152;
	/** 所属协议 Handler 模块号 */
	private static final int PROTOCOL_HANDLER_MODULE_ID = 0x11;
	/** 套件唯一英文键 */
	public static final String MODULE_KEY = "crossGuildWar";
	/** 套件展示名称 */
	public static final String TITLE = "跨服宗门争霸据点争夺、宗门锁定与里程碑验收";
	/** 默认验收目标活动配置 ID */
	private static final int DEFAULT_ACTIVITY_ID = 52;
	/** 沙盒虚拟参战宗门 A ID */
	private static final long SANDBOX_GUILD_ID_A = PlayerSaveCoordinator.SANDBOX_PLAYER_ID_MIN + 81001L;
	/** 沙盒虚拟参战宗门 B ID（用于验证换会锁定拦截） */
	private static final long SANDBOX_GUILD_ID_B = PlayerSaveCoordinator.SANDBOX_PLAYER_ID_MIN + 81002L;

	/** 声明覆盖的多态业务分支 */
	private static final List<String> DECLARED_BRANCHES = Collections.unmodifiableList(Arrays.asList(
			"CGW_INFO_QUERY",
			"CGW_BUY_COUNT_STEP",
			"CGW_ATTACK_AND_RANK",
			"CGW_PERSONAL_REWARD",
			"CGW_GUILD_REWARD"));

	/** 关联核心静态配置表列表 */
	private static final List<String> RELATED_TABLES = Collections.unmodifiableList(Arrays.asList(
			"ActivityConfig",
			"ActivityCrossGuildWarConfig",
			"MailConfig"));

	@Override
	public int getModuleId() {
		return MODULE_ID;
	}

	@Override
	public String getModuleKey() {
		return MODULE_KEY;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public List<String> getDeclaredBranches() {
		return DECLARED_BRANCHES;
	}

	@Override
	public List<String> getRelatedTables() {
		return RELATED_TABLES;
	}

	@Override
	public List<ServerType> getRequiredServerTypes() {
		return List.of(ServerType.Game, ServerType.Cross);
	}

	/**
	 * 执行跨服宗门争霸功能契约验收用例
	 *
	 * @param ctx 验收执行上下文
	 * @throws Exception 验收执行异常时抛出
	 */
	@Override
	public void runContractCases(AcceptanceContext ctx) throws Exception {
		InProcessAcceptanceKit.ensureHandlerRegistered(PROTOCOL_HANDLER_MODULE_ID, ActivityHandler::new);
		Player player = ctx.getSandboxPlayer();
		ConditionSatisfier.satisfyAllFunctionsAndBaseLevel(player, 100, 14, 100);

		ActivityCrossGuildWarConfig defaultWarCfg = resolveTargetConfig();
		if (defaultWarCfg == null) {
			throw new AcceptanceFixtureException("未找到可用的 ActivityCrossGuildWarConfig 配置", false);
		}
		int activityId = defaultWarCfg.ID;
		String originServerId = player.getData().getServerId();
		boolean forcedStartState = ensureActivityStartStateIfNone(activityId);

		try {
			// 验证等级门槛不足拦截
			ctx.executeContractCase("CONTRACT:cgw_level_limit_blocked", "玩家等级低于活动开启门槛时查询详情应返回等级不足拦截", () -> {
				ActivityConfig actCfg = ActivityManager.instance().getNullable(activityId);
				if (actCfg != null && actCfg.levelLimit > 1 && player.getPlayerModule() != null) {
					ensureSandboxCrossGuildWar(player, activityId);
					int originLv = player.getPlayerModule().getExpLevelMap().getValue(Asset.playerExp.ID);
					try {
						player.getPlayerModule().getExpLevelMap().setValue(Asset.playerExp.ID, 1);
						Map<String, Object> res = InProcessAcceptanceKit.invokeProto(
								player.getPlayerId(), PbProtocol.ActivityCrossGuildWarInfoRequest_11000501,
								Map.of("activityId", activityId));
						InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.level_not_enough.ID);
					} finally {
						player.getPlayerModule().getExpLevelMap().setValue(Asset.playerExp.ID, originLv);
					}
				}
			});

			// 验证无宗门出征拦截 411
			ctx.executeContractCase("CONTRACT:cgw_no_guild_blocked", "未加入宗门发起出征应返回 zong_men_not_exist(411)", () -> {
				ensureSandboxCrossGuildWar(player, activityId);
				ConditionSatisfier.clearSandboxGuildContext(player);
				Map<String, Object> res = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
						Map.of("activityId", activityId, "strongholdId", 1, "score", 100L));
				InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.zong_men_not_exist.ID);
			});

			// 验证沙盒隔离战区 group_9998 详情查询成功
			ctx.executeContractCase("CONTRACT:cgw_info_query_success", "沙盒玩家查询跨服宗门争霸详情应命中隔离战区 group_9998", () -> {
				ensureSandboxCrossGuildWar(player, activityId);
				ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);
				Map<String, Object> res = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarInfoRequest_11000501,
						Map.of("activityId", activityId));
				InProcessAcceptanceKit.assertProtocolSuccess(res);
				Map<String, Object> payload = extractResponsePayload(res);
				if (!DefaultActivityScopeResolver.SANDBOX_CGW_SCOPE_KEY.equals(payload.get("scopeKey"))) {
					throw new AssertionError("沙盒玩家详情战区键应为 group_9998，实际为: " + payload.get("scopeKey"));
				}
			});

			// 验证非法据点与越界战功拦截
			ctx.executeContractCase("CONTRACT:cgw_attack_invalid_args_blocked", "非法据点ID(458)与越界战功(52)应被拦截", () -> {
				ensureSandboxCrossGuildWar(player, activityId);
				ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);
				Map<String, Object> badStronghold = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
						Map.of("activityId", activityId, "strongholdId", 999, "score", 100L));
				InProcessAcceptanceKit.assertErrorCode(badStronghold, ErrorMsgEnum.cross_guild_war_stronghold_invalid.ID);

				Map<String, Object> zeroScore = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
						Map.of("activityId", activityId, "strongholdId", 1, "score", 0L));
				InProcessAcceptanceKit.assertErrorCode(zeroScore, ErrorMsgEnum.request_parameter_error.ID);

				Map<String, Object> overScore = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
						Map.of("activityId", activityId, "strongholdId", 1, "score", defaultWarCfg.maxSingleScore + 1L));
				InProcessAcceptanceKit.assertErrorCode(overScore, ErrorMsgEnum.request_parameter_error.ID);
			});

			// 验证正常出征据点占领与双榜查询
			ctx.executeContractCase("CONTRACT:cgw_attack_and_dual_ranks", "合法出征占领据点后应能分页查询宗门榜与成员贡献榜", () -> {
				ensureSandboxCrossGuildWar(player, activityId);
				ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);
				long occupyMin = (defaultWarCfg.strongholdOccupyMinScores != null
						&& defaultWarCfg.strongholdOccupyMinScores.length > 0)
						? defaultWarCfg.strongholdOccupyMinScores[0] : 1000L;
				long hitScore = Math.min(defaultWarCfg.maxSingleScore, Math.max(1L, occupyMin));
				int targetStronghold = (defaultWarCfg.strongholdIds != null && defaultWarCfg.strongholdIds.length > 0)
						? defaultWarCfg.strongholdIds[0] : 1;

				Map<String, Object> atkRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
						Map.of("activityId", activityId, "strongholdId", targetStronghold, "score", hitScore));
				InProcessAcceptanceKit.assertProtocolSuccess(atkRes);

				Map<String, Object> guildRankRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarGuildRankRequest_11000509,
						Map.of("activityId", activityId, "page", 1, "pageSize", 10));
				InProcessAcceptanceKit.assertProtocolSuccess(guildRankRes);
				Map<String, Object> gRankPayload = extractResponsePayload(guildRankRes);
				Number gTotal = (Number) gRankPayload.get("totalCount");
				if (gTotal == null || gTotal.intValue() < 1) {
					throw new AssertionError("出征后宗门榜总数应 >= 1，实际为: " + gTotal);
				}

				Map<String, Object> memberRankRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarMemberRankRequest_1100050b,
						Map.of("activityId", activityId, "guildId", 0L, "page", 1, "pageSize", 10));
				InProcessAcceptanceKit.assertProtocolSuccess(memberRankRes);
				Map<String, Object> mRankPayload = extractResponsePayload(memberRankRes);
				Number mTotal = (Number) mRankPayload.get("totalCount");
				if (mTotal == null || mTotal.intValue() < 1) {
					throw new AssertionError("出征后成员榜总数应 >= 1，实际为: " + mTotal);
				}
			});

			// 验证换会锁定拦截 457
			ctx.executeContractCase("CONTRACT:cgw_guild_locked_blocked", "锁定宗门 A 后换入宗门 B 出征应返回 457", () -> {
				ensureSandboxCrossGuildWar(player, activityId);
				ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);
				Map<String, Object> firstAtk = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
						Map.of("activityId", activityId, "strongholdId", 1, "score", 100L));
				InProcessAcceptanceKit.assertProtocolSuccess(firstAtk);

				ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_B);
				Map<String, Object> lockedAtk = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
						Map.of("activityId", activityId, "strongholdId", 1, "score", 100L));
				InProcessAcceptanceKit.assertErrorCode(lockedAtk, ErrorMsgEnum.cross_guild_war_guild_locked.ID);
			});

			// 验证阶梯买次：余额不足 60、逐档购买成功、超限 108
			ctx.executeContractCase("CONTRACT:cgw_buy_count_guards_and_step", "买次应校验余额不足(60)、阶梯扣费与每日上限(108)", () -> {
				ensureSandboxCrossGuildWar(player, activityId);
				if (defaultWarCfg.dailyBuyLimit > 0 && defaultWarCfg.buyCosts != null && defaultWarCfg.buyCosts.length > 0) {
					SandboxResetter.resetModules(player, "currency", "item");
					ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);
					Map<String, Object> poorRes = InProcessAcceptanceKit.invokeProto(
							player.getPlayerId(), PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507,
							Map.of("activityId", activityId));
					InProcessAcceptanceKit.assertErrorCode(poorRes, ErrorMsgEnum.resource_not_enough.ID);

					for (int step = 0; step < defaultWarCfg.dailyBuyLimit; step++) {
						int[] costPair = defaultWarCfg.buyCosts[Math.min(step, defaultWarCfg.buyCosts.length - 1)];
						CostProvisioner.provisionCostArray(player, costPair, 1);
						Map<String, Object> buyRes = InProcessAcceptanceKit.invokeProto(
								player.getPlayerId(), PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507,
								Map.of("activityId", activityId));
						InProcessAcceptanceKit.assertProtocolSuccess(buyRes);
					}

					Map<String, Object> overRes = InProcessAcceptanceKit.invokeProto(
							player.getPlayerId(), PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507,
							Map.of("activityId", activityId));
					InProcessAcceptanceKit.assertErrorCode(overRes, ErrorMsgEnum.buy_over_limit.ID);
				}
			});

			// 验证个人与宗门阶段奖：未解锁拦截 55、推进后领取显式发奖、重复拦截 51、未锁定领宗门奖拦截 459
			ctx.executeContractCase("CONTRACT:cgw_milestone_reward_flow", "阶段奖应校验达标、锁定资格(459)、显式发奖与防重(51)", () -> {
				long uid = ensureSandboxCrossGuildWar(player, activityId);
				ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);

				Map<String, Object> noLockRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
						Map.of("activityId", activityId, "rewardType", CrossGuildWarActivity.REWARD_TYPE_GUILD, "milestoneId", 1));
				InProcessAcceptanceKit.assertErrorCode(noLockRes, ErrorMsgEnum.cross_guild_war_no_locked_guild.ID);

				Map<String, Object> lockedPersonalRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
						Map.of("activityId", activityId, "rewardType", CrossGuildWarActivity.REWARD_TYPE_PERSONAL, "milestoneId", 1));
				InProcessAcceptanceKit.assertErrorCode(lockedPersonalRes, ErrorMsgEnum.condition_check_error.ID);

				long setScore = Math.max(
						defaultWarCfg.personalMilestoneThresholds[0],
						Math.max(defaultWarCfg.guildMilestoneThresholds[0], defaultWarCfg.minParticipateScore));
				resetSandboxWar(uid, player.getPlayerId(), CrossGuildWarActivity.RESET_MODE_SET_SCORES,
						SANDBOX_GUILD_ID_A, "沙盒宗门A", 1, setScore);
				ensurePlayerActivityLockedGuild(player, activityId, SANDBOX_GUILD_ID_A, setScore);

				Map<String, Object> claimPersonal = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
						Map.of("activityId", activityId, "rewardType", CrossGuildWarActivity.REWARD_TYPE_PERSONAL, "milestoneId", 1));
				InProcessAcceptanceKit.assertProtocolSuccess(claimPersonal);
				InProcessAcceptanceKit.assertHasRewardEvent(claimPersonal);

				Map<String, Object> claimGuild = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
						Map.of("activityId", activityId, "rewardType", CrossGuildWarActivity.REWARD_TYPE_GUILD, "milestoneId", 1));
				InProcessAcceptanceKit.assertProtocolSuccess(claimGuild);
				InProcessAcceptanceKit.assertHasRewardEvent(claimGuild);

				Map<String, Object> repeatPersonal = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
						Map.of("activityId", activityId, "rewardType", CrossGuildWarActivity.REWARD_TYPE_PERSONAL, "milestoneId", 1));
				InProcessAcceptanceKit.assertErrorCode(repeatPersonal, ErrorMsgEnum.repeat_request.ID);
			});
		} finally {
			ConditionSatisfier.clearSandboxGuildContext(player);
			cleanupSandboxAfterSuite(player, activityId, originServerId, forcedStartState);
		}
	}

	/**
	 * 遍历 {@link ActivityCrossGuildWarManager} 全量配置执行购买阶梯、全据点与全里程碑矩阵扫描
	 *
	 * @param ctx 验收执行上下文
	 * @throws Exception 扫描执行异常时抛出
	 */
	@Override
	public void runMatrixScan(AcceptanceContext ctx) throws Exception {
		InProcessAcceptanceKit.ensureHandlerRegistered(PROTOCOL_HANDLER_MODULE_ID, ActivityHandler::new);
		Player player = ctx.getSandboxPlayer();
		ConditionSatisfier.satisfyAllFunctionsAndBaseLevel(player, 100, 14, 100);

		if (ActivityCrossGuildWarManager.instance() == null) {
			log.warn("ActivityCrossGuildWarManager 实例为空，跳过配表矩阵扫描");
			return;
		}
		Collection<ActivityCrossGuildWarConfig> configs = ActivityCrossGuildWarManager.instance().list();
		String originServerId = player.getData().getServerId();
		Map<Integer, Boolean> touchedActivities = new LinkedHashMap<>();

		try {
			for (ActivityCrossGuildWarConfig warCfg : configs) {
				if (ctx.isCircuitBroken()) {
					break;
				}
				if (warCfg == null) {
					continue;
				}
				ActivityConfig actCfg = ActivityManager.instance().getNullable(warCfg.ID);
				if (actCfg == null || actCfg.disable) {
					continue;
				}
				boolean forcedForRow = ensureActivityStartStateIfNone(warCfg.ID);
				touchedActivities.put(warCfg.ID, Boolean.TRUE.equals(touchedActivities.get(warCfg.ID)) || forcedForRow);

				// 扫描详情查询分支
				ctx.executeMatrixRow("MATRIX_CGW_INFO_" + warCfg.ID, "ActivityCrossGuildWarConfig", warCfg.ID,
						"CGW_INFO_QUERY",
						() -> {
							ensureSandboxCrossGuildWar(player, warCfg.ID);
							ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);
						},
						() -> InProcessAcceptanceKit.invokeProto(
								player.getPlayerId(), PbProtocol.ActivityCrossGuildWarInfoRequest_11000501,
								Map.of("activityId", warCfg.ID)),
						result -> {
							InProcessAcceptanceKit.assertProtocolSuccess(result);
							Map<String, Object> payload = extractResponsePayload(result);
							if (!DefaultActivityScopeResolver.SANDBOX_CGW_SCOPE_KEY.equals(payload.get("scopeKey"))) {
								throw new AssertionError("矩阵详情战区应为 group_9998，实际为: " + payload.get("scopeKey"));
							}
						});

				// 扫描全部阶梯购买档位
				if (warCfg.dailyBuyLimit > 0 && warCfg.buyCosts != null && warCfg.buyCosts.length > 0) {
					int totalSteps = Math.min(warCfg.dailyBuyLimit, warCfg.buyCosts.length);
					for (int step = 0; step < totalSteps; step++) {
						if (ctx.isCircuitBroken()) {
							break;
						}
						final int stepIndex = step;
						final int[] costPair = warCfg.buyCosts[stepIndex];
						ctx.executeMatrixRow("MATRIX_CGW_BUY_" + warCfg.ID + "_" + stepIndex,
								"ActivityCrossGuildWarConfig", warCfg.ID, "CGW_BUY_COUNT_STEP",
								() -> {
									ensureSandboxCrossGuildWar(player, warCfg.ID);
									ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);
									CrossGuildWarPlayerActivity playerAct = ensurePlayerActivityBound(player, warCfg.ID);
									playerAct.setDailyBoughtCount(stepIndex);
									CostProvisioner.provisionCostArray(player, costPair, 1);
									if (!PlayerHelper.isEnough(player, costPair)) {
										throw new AcceptanceFixtureException("造数后购买出征次数资源不足: activityId=" + warCfg.ID, true);
									}
								},
								() -> InProcessAcceptanceKit.invokeProto(
										player.getPlayerId(), PbProtocol.ActivityCrossGuildWarBuyCountRequest_11000507,
										Map.of("activityId", warCfg.ID)),
								result -> {
									InProcessAcceptanceKit.assertProtocolSuccess(result);
									if (costPair != null) {
										for (int j = 0; j + 1 < costPair.length; j += 2) {
											if (costPair[j] > 0 && costPair[j + 1] > 0) {
												InProcessAcceptanceKit.assertResourceDelta(result, costPair[j], -costPair[j + 1]);
											}
										}
									}
								});
					}
				}

				// 扫描全部可争夺据点占领分支
				if (warCfg.strongholdIds != null) {
					for (int i = 0; i < warCfg.strongholdIds.length; i++) {
						if (ctx.isCircuitBroken()) {
							break;
						}
						final int strongholdId = warCfg.strongholdIds[i];
						long minOccupy = (warCfg.strongholdOccupyMinScores != null
								&& i < warCfg.strongholdOccupyMinScores.length)
								? warCfg.strongholdOccupyMinScores[i] : 1000L;
						final long attackScore = Math.min(warCfg.maxSingleScore, Math.max(1L, minOccupy));
						ctx.executeMatrixRow("MATRIX_CGW_STRONGHOLD_" + warCfg.ID + "_" + strongholdId,
								"ActivityCrossGuildWarConfig", warCfg.ID, "CGW_ATTACK_AND_RANK",
								() -> {
									ensureSandboxCrossGuildWar(player, warCfg.ID);
									ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);
								},
								() -> InProcessAcceptanceKit.invokeProto(
										player.getPlayerId(), PbProtocol.ActivityCrossGuildWarAttackRequest_11000503,
										Map.of("activityId", warCfg.ID, "strongholdId", strongholdId, "score", attackScore)),
								result -> {
									InProcessAcceptanceKit.assertProtocolSuccess(result);
									Map<String, Object> payload = extractResponsePayload(result);
									if (!Boolean.TRUE.equals(payload.get("captured"))) {
										throw new AssertionError("矩阵据点首占 captured 应为 true: strongholdId=" + strongholdId);
									}
								});
					}
				}

				// 扫描全部个人里程碑奖励档位
				if (warCfg.personalMilestoneThresholds != null) {
					for (int i = 0; i < warCfg.personalMilestoneThresholds.length; i++) {
						if (ctx.isCircuitBroken()) {
							break;
						}
						final int milestoneId = i + 1;
						final long targetScore = warCfg.personalMilestoneThresholds[i];
						ctx.executeMatrixRow("MATRIX_CGW_PERSONAL_" + warCfg.ID + "_" + milestoneId,
								"ActivityCrossGuildWarConfig", warCfg.ID, "CGW_PERSONAL_REWARD",
								() -> {
									long uid = ensureSandboxCrossGuildWar(player, warCfg.ID);
									ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);
									resetSandboxWar(uid, player.getPlayerId(), CrossGuildWarActivity.RESET_MODE_SET_SCORES,
											SANDBOX_GUILD_ID_A, "沙盒宗门A", 1, targetScore);
									ensurePlayerActivityLockedGuild(player, warCfg.ID, SANDBOX_GUILD_ID_A, targetScore);
								},
								() -> InProcessAcceptanceKit.invokeProto(
										player.getPlayerId(), PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
										Map.of("activityId", warCfg.ID, "rewardType", CrossGuildWarActivity.REWARD_TYPE_PERSONAL,
												"milestoneId", milestoneId)),
								result -> {
									InProcessAcceptanceKit.assertProtocolSuccess(result);
									InProcessAcceptanceKit.assertHasRewardEvent(result);
								});
					}
				}

				// 扫描全部宗门里程碑奖励档位
				if (warCfg.guildMilestoneThresholds != null) {
					for (int i = 0; i < warCfg.guildMilestoneThresholds.length; i++) {
						if (ctx.isCircuitBroken()) {
							break;
						}
						final int milestoneId = i + 1;
						final long targetScore = Math.max(warCfg.minParticipateScore, warCfg.guildMilestoneThresholds[i]);
						ctx.executeMatrixRow("MATRIX_CGW_GUILD_" + warCfg.ID + "_" + milestoneId,
								"ActivityCrossGuildWarConfig", warCfg.ID, "CGW_GUILD_REWARD",
								() -> {
									long uid = ensureSandboxCrossGuildWar(player, warCfg.ID);
									ConditionSatisfier.setSandboxGuildContext(player, SANDBOX_GUILD_ID_A);
									resetSandboxWar(uid, player.getPlayerId(), CrossGuildWarActivity.RESET_MODE_SET_SCORES,
											SANDBOX_GUILD_ID_A, "沙盒宗门A", 1, targetScore);
									ensurePlayerActivityLockedGuild(player, warCfg.ID, SANDBOX_GUILD_ID_A, targetScore);
								},
								() -> InProcessAcceptanceKit.invokeProto(
										player.getPlayerId(), PbProtocol.ActivityCrossGuildWarRewardRequest_11000505,
										Map.of("activityId", warCfg.ID, "rewardType", CrossGuildWarActivity.REWARD_TYPE_GUILD,
												"milestoneId", milestoneId)),
								result -> {
									InProcessAcceptanceKit.assertProtocolSuccess(result);
									InProcessAcceptanceKit.assertHasRewardEvent(result);
								});
					}
				}
			}
		} finally {
			ConditionSatisfier.clearSandboxGuildContext(player);
			if (touchedActivities.isEmpty()) {
				player.getData().setServerId(originServerId);
			} else {
				for (Map.Entry<Integer, Boolean> entry : touchedActivities.entrySet()) {
					cleanupSandboxAfterSuite(player, entry.getKey(), originServerId, Boolean.TRUE.equals(entry.getValue()));
				}
			}
		}
	}

	/**
	 * 确保沙盒玩家归属于物理隔离战区 {@code group_9998} 并初始化重置沙盒跨服宗门争霸实例
	 */
	private long ensureSandboxCrossGuildWar(Player player, int activityId) {
		if (!ModuleAcceptanceEngine.getInstance().isLiveServerRuntime()) {
			CrossActivityService.getInstance().init();
		}
		player.getData().setServerId(DefaultActivityScopeResolver.SANDBOX_CGW_SERVER_ID);

		long uid = resolveOrCreateSandboxCrossUid(player, activityId);
		if (uid <= 0L) {
			throw new AcceptanceFixtureException("创建或解析沙盒跨服宗门争霸实例失败: activityId=" + activityId, false);
		}
		resetSandboxWar(uid, player.getPlayerId(), CrossGuildWarActivity.RESET_MODE_WAR, 0L, null, 0, 0L);
		resetSandboxPlayerActivityState(player, activityId, uid);
		return uid;
	}

	/**
	 * 查找或通过负载均衡代理创建 {@code group_9998} 沙盒跨服实例；若目录或路由失效则按标准签名彻底清理重建
	 */
	private long resolveOrCreateSandboxCrossUid(Player player, int activityId) {
		Optional<Long> existingOpt = RedisCrossActivityDirectory.getInstance()
				.getActiveUid(activityId, DefaultActivityScopeResolver.SANDBOX_CGW_SCOPE_KEY);
		if (existingOpt.isPresent() && existingOpt.get() > 0L) {
			long uid = existingOpt.get();
			boolean healthy = true;
			if (ModuleAcceptanceEngine.getInstance().isLiveServerRuntime()) {
				String routeServerId = IdCache.getServerId(DistributedObjectType.CROSS_ACTIVITY, uid);
				if (StringUtils.isEmpty(routeServerId)) {
					healthy = false;
				}
			}
			if (healthy) {
				try {
					CrossActivityServiceInterface proxy = ServerHelper.getCrossActivityProxy(uid);
					CrossActivitySnapshot snap = proxy.queryActivitySnapshot(uid, player.getPlayerId());
					if (snap != null) {
						return uid;
					}
					healthy = false;
				} catch (CrossActivityRouteMissingException e) {
					log.warn("沙盒跨服宗门争霸路由已失效，清理旧目录与路由并重建: activityId={}, oldUid={}", activityId, uid, e);
					healthy = false;
				} catch (Exception e) {
					log.warn("沙盒跨服宗门争霸活性探测异常，清理旧目录与路由并重建: activityId={}, oldUid={}", activityId, uid, e);
					healthy = false;
				}
			}
			if (!healthy && uid > 0L) {
				cleanupDeadSandboxRoute(activityId, uid);
			}
		}

		CrossActivityCreateResult createRes = ServerHelper.getCrossActivityBalanceProxy()
				.createOrEnsureActivity(IdUtil.getId(), activityId,
						DefaultActivityScopeResolver.SANDBOX_CGW_SCOPE_KEY,
						DefaultActivityScopeResolver.SANDBOX_CGW_SERVER_ID);
		if (createRes == null || !createRes.isSuccess() || createRes.getActivityUid() <= 0L) {
			throw new AcceptanceFixtureException("创建沙盒跨服宗门争霸失败: activityId=" + activityId
					+ ", msg=" + (createRes != null ? createRes.getErrorMsg() : "null"), false);
		}
		return createRes.getActivityUid();
	}

	/**
	 * 清理失效沙盒目录与本地/Redis路由缓存（遵循设计审查修正后的非静态 IdCache 清理签名）
	 */
	private void cleanupDeadSandboxRoute(int activityId, long uid) {
		if (uid <= 0L) {
			return;
		}
		RedisCrossActivityDirectory.getInstance()
				.removeIfMatch(activityId, DefaultActivityScopeResolver.SANDBOX_CGW_SCOPE_KEY, uid);
		var idManager = IdCache.getManager(DistributedObjectType.CROSS_ACTIVITY);
		if (idManager != null) {
			idManager.clearServerId(uid);
			idManager.invalidateServerId(uid);
		}
		if (RedisUtil.getRedis() != null) {
			RedisUtil.delete(IdCache.generateRedisKey(DistributedObjectType.CROSS_ACTIVITY, uid));
		}
	}

	/**
	 * 向 Cross 沙盒战区实例发送受控验收重置指令
	 */
	private void resetSandboxWar(long uid, long playerId, String mode, long guildId,
			String guildName, int strongholdId, long score) {
		AcceptanceResetRequest req = new AcceptanceResetRequest();
		req.setMode(mode);
		req.setTargetPlayerId(playerId);
		req.setGuildId(guildId);
		req.setGuildName(guildName);
		req.setStrongholdId(strongholdId);
		req.setScore(score);
		CrossActivityActionResult res = ServerHelper.getCrossActivityProxy(uid).executePlayerAction(
				uid, playerId, CrossGuildWarActivity.ACTION_ACCEPTANCE_RESET, JsonUtil.toJsonString(req));
		if (res == null || !res.isSuccess()) {
			throw new AcceptanceFixtureException("执行沙盒跨服宗门争霸重置失败: uid=" + uid + ", mode=" + mode
					+ ", errCode=" + (res != null ? res.getErrorCode() : -1), false);
		}
	}

	/**
	 * 确保沙盒玩家本地已挂载 {@link CrossGuildWarPlayerActivity}
	 */
	private CrossGuildWarPlayerActivity ensurePlayerActivityBound(Player player, int activityId) {
		ActivityBase raw = player.getActivityModule().get(activityId);
		if (raw == null) {
			player.getActivityModule().open(activityId, false);
			raw = player.getActivityModule().get(activityId);
		}
		if (!(raw instanceof CrossGuildWarPlayerActivity playerAct)) {
			throw new AcceptanceFixtureException("沙盒玩家未能开启 CrossGuildWarPlayerActivity: activityId=" + activityId, false);
		}
		return playerAct;
	}

	/**
	 * 同步设定沙盒玩家本地锁定宗门与战功（匹配 {@code set_scores_for_test} 后的领奖校验）
	 */
	private void ensurePlayerActivityLockedGuild(Player player, int activityId, long lockedGuildId, long myScore) {
		CrossGuildWarPlayerActivity playerAct = ensurePlayerActivityBound(player, activityId);
		playerAct.setLockedGuildId(lockedGuildId);
		playerAct.setMyScore(myScore);
		playerAct.setMyGuildTotalScore(myScore);
	}

	/**
	 * 重置沙盒玩家本地的跨服宗门争霸进度，保证每条用例互不串扰
	 */
	private void resetSandboxPlayerActivityState(Player player, int activityId, long uid) {
		CrossGuildWarPlayerActivity playerAct = ensurePlayerActivityBound(player, activityId);
		playerAct.setCrossActivityUid(uid);
		playerAct.setState(ActivityState.START_VALUE);
		playerAct.setDailyUsedCount(0);
		playerAct.setDailyBoughtCount(0);
		playerAct.setLastResetDay(DateUtil.getDay());
		playerAct.setLockedGuildId(0L);
		playerAct.setMyScore(0L);
		playerAct.setMyGuildTotalScore(0L);
		playerAct.setMyGuildRank(0);
		playerAct.getClaimedPersonalMilestoneIds().clear();
		playerAct.getClaimedGuildMilestoneIds().clear();
		playerAct.getCachedUnlockedPersonalMilestoneIds().clear();
		playerAct.getCachedUnlockedGuildMilestoneIds().clear();
		playerAct.setCachedSnapshotSynced(false);
	}

	/**
	 * 若离线单测尚未初始化 {@link ActivityStateManager}，则临时置为 START 并在结束时恢复
	 */
	private boolean ensureActivityStartStateIfNone(int activityId) {
		if (ActivityStateManager.getInstance().getState(activityId) == ActivityState.NONE_VALUE) {
			ActivityStateManager.getInstance().setState(activityId, ActivityState.START_VALUE);
			return true;
		}
		return false;
	}

	/**
	 * 套件退出时清理沙盒战区与沙盒玩家本地活动状态，并还原原逻辑服 ID 与活动全局状态
	 */
	private void cleanupSandboxAfterSuite(Player player, int activityId, String originServerId, boolean forcedStartState) {
		try {
			Optional<Long> activeOpt = RedisCrossActivityDirectory.getInstance()
					.getActiveUid(activityId, DefaultActivityScopeResolver.SANDBOX_CGW_SCOPE_KEY);
			if (activeOpt.isPresent()) {
				long uid = activeOpt.get();
				try {
					resetSandboxWar(uid, player.getPlayerId(), CrossGuildWarActivity.RESET_MODE_WAR, 0L, null, 0, 0L);
				} catch (Exception e) {
					log.warn("清理沙盒跨服宗门争霸状态失败: activityId={}, uid={}", activityId, uid, e);
				}
			}
		} catch (Exception e) {
			log.warn("查询沙盒跨服宗门争霸目录清理失败: activityId={}", activityId, e);
		} finally {
			if (player.getActivityModule() != null) {
				player.getActivityModule().getPlayerActivityManager().getActivities().remove(activityId);
			}
			player.getData().setServerId(originServerId);
			if (forcedStartState) {
				ActivityStateManager.getInstance().removeState(activityId);
			}
		}
	}

	/**
	 * 解析默认目标活动配置，优先取 {@code ID=52}，否则取配置表首行
	 */
	private ActivityCrossGuildWarConfig resolveTargetConfig() {
		ActivityCrossGuildWarConfig cfg = ActivityCrossGuildWarManager.instance().getNullable(DEFAULT_ACTIVITY_ID);
		if (cfg != null) {
			return cfg;
		}
		Collection<ActivityCrossGuildWarConfig> list = ActivityCrossGuildWarManager.instance().list();
		return list != null && !list.isEmpty() ? list.iterator().next() : null;
	}

	/**
	 * 从 {@link InProcessAcceptanceKit#invokeProto} 返回字典中安全提取响应 payload 映射
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> extractResponsePayload(Map<String, Object> invokeRes) {
		if (invokeRes == null) {
			return Collections.emptyMap();
		}
		Object respObj = invokeRes.get("response");
		if (respObj instanceof Map<?, ?> respMap) {
			Object payloadObj = respMap.get("payload");
			if (payloadObj instanceof Map<?, ?> payloadMap) {
				return (Map<String, Object>) payloadMap;
			}
		}
		return Collections.emptyMap();
	}
}
