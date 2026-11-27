package cn.game.games.net.game.gm.acceptance.suites;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.id.IdUtil;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.common.module.activity.DefaultActivityScopeResolver;
import cn.game.games.net.cross.activity.CrossActivityRouteMissingException;
import cn.game.games.net.cross.activity.RedisCrossActivityDirectory;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.cross.activity.dto.CrossActivityCreateResult;
import cn.game.games.net.cross.activity.dto.CrossWorldBossPayloads.AcceptanceResetRequest;
import cn.game.games.net.cross.activity.impl.CrossWorldBossActivity;
import cn.game.games.net.cross.activity.service.CrossActivityService;
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
import cn.game.games.net.game.module.activity.impl.player.CrossWorldBossPlayerActivity;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.config.ActivityCrossWorldBossConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ActivityCrossWorldBossManager;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.ServerType;

/**
 * 跨服世界Boss模块双轨自动化验收套件 (0x11)。
 * <p>
 * 职责边界：在非生产环境下将沙盒玩家映射至物理隔离战区 {@code group_9999}，
 * 依次验证功能契约（等级门禁、详情查询、非法伤害拦截、攻打与榜单、阶梯购买与超限、里程碑解锁领取与防重、
 * 击杀奖非击杀者拦截）及全量 {@code ActivityCrossWorldBossConfig} 配表矩阵扫描，零污染真实战区数据。
 * </p>
 *
 * @author shuyongqiang
 */
public class CrossWorldBossModuleAcceptanceSuite implements ModuleAcceptanceSuite {

	/** 套件日志器 */
	private static final Logger log = LoggerFactory.getLogger(CrossWorldBossModuleAcceptanceSuite.class);

	/** 所属协议模块号 */
	public static final int MODULE_ID = 0x11;
	/** 套件唯一英文键 */
	public static final String MODULE_KEY = "crossWorldBoss";
	/** 套件展示名称 */
	public static final String TITLE = "跨服世界Boss攻打、购买、里程碑与结算验收";
	/** 默认验收目标活动配置 ID */
	private static final int DEFAULT_ACTIVITY_ID = 51;
	/** 模拟其他沙盒击杀者所使用的隔离玩家 ID */
	private static final long SECOND_SANDBOX_KILLER_ID = PlayerSaveCoordinator.SANDBOX_PLAYER_ID_MIN + 999L;

	/** 声明覆盖的多态业务分支 */
	private static final List<String> DECLARED_BRANCHES = Collections.unmodifiableList(Arrays.asList(
			"CWB_INFO_QUERY",
			"CWB_ATTACK_AND_RANK",
			"CWB_BUY_COUNT_STEP",
			"CWB_MILESTONE_REWARD",
			"CWB_KILL_REWARD_GUARD"));

	/** 关联核心静态配置表列表 */
	private static final List<String> RELATED_TABLES = Collections.unmodifiableList(Arrays.asList(
			"ActivityConfig",
			"ActivityCrossWorldBossConfig",
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
	 * 执行跨服世界Boss功能契约验收用例
	 *
	 * @param ctx 验收执行上下文
	 * @throws Exception 验收执行异常时抛出
	 */
	@Override
	public void runContractCases(AcceptanceContext ctx) throws Exception {
		InProcessAcceptanceKit.ensureHandlerRegistered(MODULE_ID, ActivityHandler::new);
		Player player = ctx.getSandboxPlayer();
		ConditionSatisfier.satisfyAllFunctionsAndBaseLevel(player, 100, 14, 100);

		ActivityCrossWorldBossConfig defaultBossCfg = resolveTargetConfig();
		if (defaultBossCfg == null) {
			throw new AcceptanceFixtureException("未找到可用的 ActivityCrossWorldBossConfig 配置", false);
		}
		int activityId = defaultBossCfg.ID;
		String originServerId = player.getData().getServerId();
		boolean forcedStartState = ensureActivityStartStateIfNone(activityId);

		try {
			// 验证等级门槛不足时直接被入口守卫拦截
			ctx.executeContractCase("CONTRACT:cwb_level_limit_blocked", "玩家等级低于活动开启门槛时查询详情应返回等级不足拦截", () -> {
				ActivityConfig actCfg = ActivityManager.instance().getNullable(activityId);
				if (actCfg != null && actCfg.levelLimit > 1 && player.getPlayerModule() != null) {
					ensureSandboxCrossBoss(player, activityId);
					int originLv = player.getPlayerModule().getExpLevelMap().getValue(Asset.playerExp.ID);
					try {
						player.getPlayerModule().getExpLevelMap().setValue(Asset.playerExp.ID, 1);
						Map<String, Object> res = InProcessAcceptanceKit.invokeProto(
								player.getPlayerId(), PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
								Map.of("activityId", activityId));
						InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.level_not_enough.ID);
					} finally {
						player.getPlayerModule().getExpLevelMap().setValue(Asset.playerExp.ID, originLv);
					}
				}
			});

			// 验证沙盒隔离战区 group_9999 详情查询成功
			ctx.executeContractCase("CONTRACT:cwb_info_query_success", "沙盒玩家查询跨服世界Boss详情应命中隔离战区 group_9999", () -> {
				ensureSandboxCrossBoss(player, activityId);
				Map<String, Object> res = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
						Map.of("activityId", activityId));
				InProcessAcceptanceKit.assertProtocolSuccess(res);
				Map<String, Object> payload = extractResponsePayload(res);
				if (!DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY.equals(payload.get("scopeKey"))) {
					throw new AssertionError("沙盒玩家详情战区键应为 group_9999，实际为: " + payload.get("scopeKey"));
				}
			});

			// 验证非法伤害参数拦截且不消耗次数
			ctx.executeContractCase("CONTRACT:cwb_attack_invalid_damage_blocked", "攻打伤害为 0 或超过单次上限时应返回参数错误", () -> {
				ensureSandboxCrossBoss(player, activityId);
				Map<String, Object> zeroRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
						Map.of("activityId", activityId, "damage", 0L));
				InProcessAcceptanceKit.assertErrorCode(zeroRes, ErrorMsgEnum.request_parameter_error.ID);

				Map<String, Object> overRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
						Map.of("activityId", activityId, "damage", defaultBossCfg.maxSingleDamage + 1L));
				InProcessAcceptanceKit.assertErrorCode(overRes, ErrorMsgEnum.request_parameter_error.ID);
			});

			// 验证正常攻打扣血与排行榜联动
			ctx.executeContractCase("CONTRACT:cwb_attack_and_rank_success", "合法攻打扣血后应上榜并能分页查询本战区伤害榜", () -> {
				ensureSandboxCrossBoss(player, activityId);
				long hitDamage = Math.min(1000L, Math.max(1L, defaultBossCfg.maxSingleDamage));
				Map<String, Object> atkRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
						Map.of("activityId", activityId, "damage", hitDamage));
				InProcessAcceptanceKit.assertProtocolSuccess(atkRes);

				Map<String, Object> rankRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossWorldBossRankRequest_11000409,
						Map.of("activityId", activityId, "page", 1, "pageSize", 10));
				InProcessAcceptanceKit.assertProtocolSuccess(rankRes);
				Map<String, Object> rankPayload = extractResponsePayload(rankRes);
				Number totalCount = (Number) rankPayload.get("totalCount");
				if (totalCount == null || totalCount.intValue() < 1) {
					throw new AssertionError("攻打后排行榜总人数应至少为 1，实际为: " + totalCount);
				}
			});

			// 验证阶梯购买挑战次数：资源不足拦截、正常逐档购买与超限拦截
			ctx.executeContractCase("CONTRACT:cwb_buy_count_guards_and_step", "购买额外次数应校验余额不足、阶梯扣费与每日购买上限", () -> {
				ensureSandboxCrossBoss(player, activityId);
				if (defaultBossCfg.dailyBuyLimit > 0 && defaultBossCfg.buyCosts != null && defaultBossCfg.buyCosts.length > 0) {
					SandboxResetter.resetModules(player, "currency", "item");
					Map<String, Object> poorRes = InProcessAcceptanceKit.invokeProto(
							player.getPlayerId(), PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407,
							Map.of("activityId", activityId));
					InProcessAcceptanceKit.assertErrorCode(poorRes, ErrorMsgEnum.resource_not_enough.ID);

					for (int step = 0; step < defaultBossCfg.dailyBuyLimit; step++) {
						int[] costPair = defaultBossCfg.buyCosts[Math.min(step, defaultBossCfg.buyCosts.length - 1)];
						CostProvisioner.provisionCostArray(player, costPair, 1);
						Map<String, Object> buyRes = InProcessAcceptanceKit.invokeProto(
								player.getPlayerId(), PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407,
								Map.of("activityId", activityId));
						InProcessAcceptanceKit.assertProtocolSuccess(buyRes);
					}

					Map<String, Object> overLimitRes = InProcessAcceptanceKit.invokeProto(
							player.getPlayerId(), PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407,
							Map.of("activityId", activityId));
					InProcessAcceptanceKit.assertErrorCode(overLimitRes, ErrorMsgEnum.buy_over_limit.ID);
				}
			});

			// 验证里程碑未解锁拦截、沙盒推进后领取成功与重复领取防重
			ctx.executeContractCase("CONTRACT:cwb_milestone_reward_flow", "里程碑应校验解锁与参与门槛，领取后显式发奖且拦截重复领取", () -> {
				long uid = ensureSandboxCrossBoss(player, activityId);
				Map<String, Object> lockedRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
						Map.of("activityId", activityId, "rewardType", CrossWorldBossActivity.REWARD_TYPE_MILESTONE, "milestoneId", 1));
				InProcessAcceptanceKit.assertErrorCode(lockedRes, ErrorMsgEnum.condition_check_error.ID);

				resetSandboxBoss(uid, player.getPlayerId(), player.getData().getName(),
						CrossWorldBossActivity.RESET_MODE_SET_MILESTONE, 1, 0);
				Map<String, Object> claimRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
						Map.of("activityId", activityId, "rewardType", CrossWorldBossActivity.REWARD_TYPE_MILESTONE, "milestoneId", 1));
				InProcessAcceptanceKit.assertProtocolSuccess(claimRes);
				InProcessAcceptanceKit.assertHasRewardEvent(claimRes);

				Map<String, Object> repeatRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
						Map.of("activityId", activityId, "rewardType", CrossWorldBossActivity.REWARD_TYPE_MILESTONE, "milestoneId", 1));
				InProcessAcceptanceKit.assertErrorCode(repeatRes, ErrorMsgEnum.repeat_request.ID);
			});

			// 验证其他玩家击杀沙盒 Boss 后，非击杀者补领击杀奖被拒绝
			ctx.executeContractCase("CONTRACT:cwb_kill_reward_not_killer_blocked", "非最后一击击杀者尝试补领击杀奖励应返回 456 拦截", () -> {
				long uid = ensureSandboxCrossBoss(player, activityId);
				resetSandboxBoss(uid, SECOND_SANDBOX_KILLER_ID, "SandboxKiller",
						CrossWorldBossActivity.RESET_MODE_SET_MILESTONE, 0, 10000);
				Map<String, Object> notKillerRes = InProcessAcceptanceKit.invokeProto(
						player.getPlayerId(), PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
						Map.of("activityId", activityId, "rewardType", CrossWorldBossActivity.REWARD_TYPE_KILL, "milestoneId", 0));
				InProcessAcceptanceKit.assertErrorCode(notKillerRes, ErrorMsgEnum.cross_world_boss_not_killer.ID);
			});
		} finally {
			cleanupSandboxAfterSuite(player, activityId, originServerId, forcedStartState);
		}
	}

	/**
	 * 遍历 {@link ActivityCrossWorldBossManager} 全量配置执行购买阶梯与全里程碑矩阵扫描
	 *
	 * @param ctx 验收执行上下文
	 * @throws Exception 扫描执行异常时抛出
	 */
	@Override
	public void runMatrixScan(AcceptanceContext ctx) throws Exception {
		InProcessAcceptanceKit.ensureHandlerRegistered(MODULE_ID, ActivityHandler::new);
		Player player = ctx.getSandboxPlayer();
		ConditionSatisfier.satisfyAllFunctionsAndBaseLevel(player, 100, 14, 100);

		if (ActivityCrossWorldBossManager.instance() == null) {
			log.warn("ActivityCrossWorldBossManager 实例为空，跳过配表矩阵扫描");
			return;
		}
		Collection<ActivityCrossWorldBossConfig> configs = ActivityCrossWorldBossManager.instance().list();
		String originServerId = player.getData().getServerId();
		Map<Integer, Boolean> touchedActivities = new LinkedHashMap<>();

		try {
			for (ActivityCrossWorldBossConfig bossCfg : configs) {
				if (ctx.isCircuitBroken()) {
					break;
				}
				if (bossCfg == null) {
					continue;
				}
				ActivityConfig actCfg = ActivityManager.instance().getNullable(bossCfg.ID);
				if (actCfg == null || actCfg.disable) {
					continue;
				}
				boolean forcedForRow = ensureActivityStartStateIfNone(bossCfg.ID);
				touchedActivities.put(bossCfg.ID, Boolean.TRUE.equals(touchedActivities.get(bossCfg.ID)) || forcedForRow);

				// 扫描详情查询分支（核验沙盒战区、最大血量与每日免费次数与配表一致）
				ctx.executeMatrixRow("MATRIX_CWB_INFO_" + bossCfg.ID, "ActivityCrossWorldBossConfig", bossCfg.ID,
						"CWB_INFO_QUERY",
						() -> {
							ensureSandboxCrossBoss(player, bossCfg.ID);
							ensurePlayerActivityBound(player, bossCfg.ID);
						},
						() -> InProcessAcceptanceKit.invokeProto(
								player.getPlayerId(), PbProtocol.ActivityCrossWorldBossInfoRequest_11000401,
								Map.of("activityId", bossCfg.ID)),
						result -> {
							InProcessAcceptanceKit.assertProtocolSuccess(result);
							Map<String, Object> payload = extractResponsePayload(result);
							if (!DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY.equals(payload.get("scopeKey"))) {
								throw new AssertionError("矩阵详情战区应为 group_9999，实际为: " + payload.get("scopeKey"));
							}
							Number maxHp = (Number) payload.get("maxHp");
							if (maxHp == null || maxHp.longValue() != bossCfg.maxHp) {
								throw new AssertionError("矩阵详情 maxHp 与配表不一致: expected=" + bossCfg.maxHp + ", actual=" + maxHp);
							}
							Number remainingTimes = (Number) payload.get("remainingTimes");
							if (remainingTimes == null || remainingTimes.intValue() != bossCfg.dailyFreeTimes) {
								throw new AssertionError("矩阵详情每日免费次数与配表不一致: expected=" + bossCfg.dailyFreeTimes + ", actual=" + remainingTimes);
							}
						});

				// 扫描攻打扣血分支（核验单次有效伤害与剩余血量联动）
				final long matrixDamage = Math.min(1000L, Math.max(1L, bossCfg.maxSingleDamage));
				ctx.executeMatrixRow("MATRIX_CWB_ATTACK_RANK_" + bossCfg.ID, "ActivityCrossWorldBossConfig", bossCfg.ID,
						"CWB_ATTACK_AND_RANK",
						() -> {
							ensureSandboxCrossBoss(player, bossCfg.ID);
							ensurePlayerActivityBound(player, bossCfg.ID);
						},
						() -> InProcessAcceptanceKit.invokeProto(
								player.getPlayerId(), PbProtocol.ActivityCrossWorldBossAttackRequest_11000403,
								Map.of("activityId", bossCfg.ID, "damage", matrixDamage)),
						result -> {
							InProcessAcceptanceKit.assertProtocolSuccess(result);
							Map<String, Object> payload = extractResponsePayload(result);
							Number actualDamage = (Number) payload.get("actualDamage");
							if (actualDamage == null || actualDamage.longValue() <= 0L) {
								throw new AssertionError("矩阵攻打实际有效伤害应大于 0，实际为: " + actualDamage);
							}
						});

				// 扫描全部有效阶梯购买档位
				if (bossCfg.dailyBuyLimit > 0 && bossCfg.buyCosts != null && bossCfg.buyCosts.length > 0) {
					int totalSteps = Math.min(bossCfg.dailyBuyLimit, bossCfg.buyCosts.length);
					for (int step = 0; step < totalSteps; step++) {
						if (ctx.isCircuitBroken()) {
							break;
						}
						final int stepIndex = step;
						final int[] costPair = bossCfg.buyCosts[stepIndex];
						String caseId = "MATRIX_CWB_BUY_" + bossCfg.ID + "_" + stepIndex;
						ctx.executeMatrixRow(caseId, "ActivityCrossWorldBossConfig", bossCfg.ID, "CWB_BUY_COUNT_STEP",
								() -> {
									ensureSandboxCrossBoss(player, bossCfg.ID);
									CrossWorldBossPlayerActivity playerAct = ensurePlayerActivityBound(player, bossCfg.ID);
									playerAct.setDailyBoughtCount(stepIndex);
									CostProvisioner.provisionCostArray(player, costPair, 1);
									if (!PlayerHelper.isEnough(player, costPair)) {
										throw new AcceptanceFixtureException("造数后购买挑战次数所需资源仍不足: activityId=" + bossCfg.ID, true);
									}
								},
								() -> InProcessAcceptanceKit.invokeProto(
										player.getPlayerId(), PbProtocol.ActivityCrossWorldBossBuyCountRequest_11000407,
										Map.of("activityId", bossCfg.ID)),
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

				// 扫描全部血量里程碑奖励档位
				if (bossCfg.milestoneThresholds != null) {
					for (int i = 0; i < bossCfg.milestoneThresholds.length; i++) {
						if (ctx.isCircuitBroken()) {
							break;
						}
						final int milestoneId = i + 1;
						String caseId = "MATRIX_CWB_MILESTONE_" + bossCfg.ID + "_" + milestoneId;
						ctx.executeMatrixRow(caseId, "ActivityCrossWorldBossConfig", bossCfg.ID, "CWB_MILESTONE_REWARD",
								() -> {
									long uid = ensureSandboxCrossBoss(player, bossCfg.ID);
									ensurePlayerActivityBound(player, bossCfg.ID);
									resetSandboxBoss(uid, player.getPlayerId(), player.getData().getName(),
											CrossWorldBossActivity.RESET_MODE_SET_MILESTONE, milestoneId, 0);
								},
								() -> InProcessAcceptanceKit.invokeProto(
										player.getPlayerId(), PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
										Map.of("activityId", bossCfg.ID, "rewardType", CrossWorldBossActivity.REWARD_TYPE_MILESTONE,
												"milestoneId", milestoneId)),
								result -> {
									InProcessAcceptanceKit.assertProtocolSuccess(result);
									InProcessAcceptanceKit.assertHasRewardEvent(result);
								});
					}
				}

				// 扫描最后一击击杀奖励补领分支
				if (!ctx.isCircuitBroken() && bossCfg.killRewards != null && bossCfg.killRewards.length > 0) {
					ctx.executeMatrixRow("MATRIX_CWB_KILL_CLAIM_" + bossCfg.ID, "ActivityCrossWorldBossConfig", bossCfg.ID,
							"CWB_KILL_REWARD_GUARD",
							() -> {
								long uid = ensureSandboxCrossBoss(player, bossCfg.ID);
								ensurePlayerActivityBound(player, bossCfg.ID);
								resetSandboxBoss(uid, player.getPlayerId(), player.getData().getName(),
										CrossWorldBossActivity.RESET_MODE_SET_MILESTONE, 0, 10000);
							},
							() -> InProcessAcceptanceKit.invokeProto(
									player.getPlayerId(), PbProtocol.ActivityCrossWorldBossRewardRequest_11000405,
									Map.of("activityId", bossCfg.ID, "rewardType", CrossWorldBossActivity.REWARD_TYPE_KILL,
											"milestoneId", 0)),
							result -> {
								InProcessAcceptanceKit.assertProtocolSuccess(result);
								InProcessAcceptanceKit.assertHasRewardEvent(result);
							});
				}
			}
		} finally {
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
	 * 确保沙盒玩家归属于物理隔离战区 {@code group_9999} 并初始化重置沙盒跨服 Boss 实例
	 *
	 * @param player     沙盒玩家
	 * @param activityId 目标活动配置 ID
	 * @return 沙盒跨服 Boss 实例 UID
	 */
	private long ensureSandboxCrossBoss(Player player, int activityId) {
		if (!ModuleAcceptanceEngine.getInstance().isLiveServerRuntime()) {
			CrossActivityService.getInstance().init();
		}
		player.getData().setServerId(DefaultActivityScopeResolver.SANDBOX_SERVER_ID);

		long uid = resolveOrCreateSandboxCrossUid(activityId);
		if (uid <= 0L) {
			throw new AcceptanceFixtureException("创建或解析沙盒跨服世界Boss实例失败: activityId=" + activityId, false);
		}
		resetSandboxBoss(uid, player.getPlayerId(), player.getData().getName(), CrossWorldBossActivity.RESET_MODE_BOSS, 0, 0);
		resetSandboxPlayerActivityState(player, activityId, uid);
		return uid;
	}

	/**
	 * 查找或通过负载均衡代理创建 {@code group_9999} 沙盒跨服实例；若目录存在陈旧失效 UID 则自动清理重建
	 */
	private long resolveOrCreateSandboxCrossUid(int activityId) {
		Optional<Long> existingOpt = RedisCrossActivityDirectory.getInstance()
				.getActiveUid(activityId, DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY);
		if (existingOpt.isPresent()) {
			long existingUid = existingOpt.get();
			try {
				ServerHelper.getCrossActivityProxy(existingUid);
				return existingUid;
			} catch (CrossActivityRouteMissingException e) {
				log.warn("沙盒跨服世界Boss路由已失效，清理旧目录并重建: activityId={}, oldUid={}", activityId, existingUid, e);
				RedisCrossActivityDirectory.getInstance()
						.removeIfMatch(activityId, DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY, existingUid);
			}
		}
		CrossActivityCreateResult createRes = ServerHelper.getCrossActivityBalanceProxy()
				.createOrEnsureActivity(IdUtil.getId(), activityId,
						DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY, DefaultActivityScopeResolver.SANDBOX_SERVER_ID);
		if (createRes == null || !createRes.isSuccess() || createRes.getActivityUid() <= 0L) {
			throw new AcceptanceFixtureException("创建沙盒跨服世界Boss失败: activityId=" + activityId
					+ ", msg=" + (createRes != null ? createRes.getErrorMsg() : "null"), false);
		}
		return createRes.getActivityUid();
	}

	/**
	 * 向 Cross 沙盒战区实例发送受控验收重置指令
	 */
	private void resetSandboxBoss(long uid, long playerId, String playerName, String mode, int milestoneId, int targetThreshold) {
		AcceptanceResetRequest req = new AcceptanceResetRequest();
		req.setPlayerId(playerId);
		req.setPlayerName(playerName != null ? playerName : "Sandbox");
		req.setServerId(DefaultActivityScopeResolver.SANDBOX_SERVER_ID);
		req.setMode(mode);
		req.setMilestoneId(milestoneId);
		req.setTargetThreshold(targetThreshold);
		CrossActivityActionResult res = ServerHelper.getCrossActivityProxy(uid).executePlayerAction(
				uid, playerId, CrossWorldBossActivity.ACTION_ACCEPTANCE_RESET, JsonUtil.toJsonString(req));
		if (res == null || !res.isSuccess()) {
			throw new AcceptanceFixtureException("执行沙盒跨服世界Boss重置失败: uid=" + uid + ", mode=" + mode
					+ ", errCode=" + (res != null ? res.getErrorCode() : -1), false);
		}
	}

	/**
	 * 确保沙盒玩家本地已挂载 {@link CrossWorldBossPlayerActivity} 并绑定到指定跨服 UID
	 */
	private CrossWorldBossPlayerActivity ensurePlayerActivityBound(Player player, int activityId) {
		ActivityBase raw = player.getActivityModule().get(activityId);
		if (raw == null) {
			player.getActivityModule().open(activityId, false);
			raw = player.getActivityModule().get(activityId);
		}
		if (!(raw instanceof CrossWorldBossPlayerActivity playerAct)) {
			throw new AcceptanceFixtureException("沙盒玩家未能开启 CrossWorldBossPlayerActivity: activityId=" + activityId, false);
		}
		return playerAct;
	}

	/**
	 * 重置沙盒玩家本地的跨服世界Boss进度，保证每条用例互不串扰
	 */
	private void resetSandboxPlayerActivityState(Player player, int activityId, long uid) {
		ActivityBase raw = player.getActivityModule().get(activityId);
		if (raw instanceof CrossWorldBossPlayerActivity playerAct) {
			playerAct.setCrossActivityUid(uid);
			playerAct.setState(ActivityState.START_VALUE);
			playerAct.setDailyUsedCount(0);
			playerAct.setDailyBoughtCount(0);
			playerAct.setLastResetDay(DateUtil.getDay());
			playerAct.getClaimedMilestoneIds().clear();
			playerAct.setMyTotalDamage(0L);
			playerAct.setMyRank(0);
			playerAct.setKillRewardClaimed(false);
			playerAct.setCachedBossKilled(false);
			playerAct.getCachedUnlockedMilestones().clear();
			playerAct.setCachedSnapshotSynced(false);
		}
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
	 * 套件退出时清理沙盒 Boss 与沙盒玩家本地活动状态，并还原原逻辑服 ID 与活动全局状态
	 */
	private void cleanupSandboxAfterSuite(Player player, int activityId, String originServerId, boolean forcedStartState) {
		try {
			Optional<Long> activeOpt = RedisCrossActivityDirectory.getInstance()
					.getActiveUid(activityId, DefaultActivityScopeResolver.SANDBOX_SCOPE_KEY);
			if (activeOpt.isPresent()) {
				long uid = activeOpt.get();
				try {
					resetSandboxBoss(uid, player.getPlayerId(), player.getData().getName(),
							CrossWorldBossActivity.RESET_MODE_BOSS, 0, 0);
				} catch (Exception e) {
					log.warn("清理沙盒跨服Boss状态失败: activityId={}, uid={}", activityId, uid, e);
				}
			}
		} catch (Exception e) {
			log.warn("查询沙盒跨服Boss目录清理失败: activityId={}", activityId, e);
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
	 * 解析默认目标活动配置，优先取 {@code ID=51}，否则取配置表首行
	 */
	private ActivityCrossWorldBossConfig resolveTargetConfig() {
		ActivityCrossWorldBossConfig cfg = ActivityCrossWorldBossManager.instance().getNullable(DEFAULT_ACTIVITY_ID);
		if (cfg != null) {
			return cfg;
		}
		Collection<ActivityCrossWorldBossConfig> list = ActivityCrossWorldBossManager.instance().list();
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
