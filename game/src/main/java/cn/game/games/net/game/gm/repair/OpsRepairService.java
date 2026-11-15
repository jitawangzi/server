package cn.game.games.net.game.gm.repair;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.exception.LogicException;
import cn.game.core.net.process.Processor;
import cn.game.core.util.AsyncUtils;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmDispatcher;
import cn.game.games.net.game.gm.GmExecContext;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmResult;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityPolicy;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.gm.PlayerInspectService;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.PlayerWriteGuard;
import cn.game.games.net.game.helper.QuestHelper;
import cn.game.games.net.game.manager.DataFixManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.core.GoodsModule;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.games.net.game.module.currency.CurrencyModule;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.net.game.module.quest.Condition;
import cn.game.games.net.game.module.quest.Quest;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.util.CborSnappyUtil;
import cn.game.util.DateUtil;
import cn.game.util.JsonUtil;
import cn.game.util.LockUtil;
import cn.game.util.ObjUtil;
import cn.game.util.RedisUtil;

/**
 * 生产受控具名修数服务、热更重扫与审计日志系统 (DC-17, BR-16, EX-12, AC-15)
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "repair", description = "生产受控具名数据修复与热更重扫")
public class OpsRepairService {

	private static final Logger log = LoggerFactory.getLogger(OpsRepairService.class);

	public static final String PROP_AUDIT_FILE = "gm.repair.auditFile";
	public static final String DEFAULT_AUDIT_FILE = "logs/ops-repair-audit.jsonl";
	private static final int MAX_IN_MEMORY_AUDIT_LOGS = 200;

	private static volatile OpsRepairService instance;

	private final ConcurrentLinkedDeque<Map<String, Object>> inMemoryAuditLogs = new ConcurrentLinkedDeque<>();
	private final ConcurrentHashMap<Long, ReentrantLock> processLocks = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, RLock> redissonLocks = new ConcurrentHashMap<>();
	private final ReentrantLock auditFileLock = new ReentrantLock();
	private final Map<String, RepairOperator> operators = new LinkedHashMap<>();

	public OpsRepairService() {
		instance = this;
		registerDefaultOperators();
		initAuditLogger();
	}

	public static OpsRepairService getInstance() {
		if (instance == null) {
			synchronized (OpsRepairService.class) {
				if (instance == null) {
					instance = new OpsRepairService();
				}
			}
		}
		return instance;
	}

	public void initAuditLogger() {
		Path auditPath = resolveAuditFilePath();
		try {
			if (auditPath.getParent() != null && !Files.exists(auditPath.getParent())) {
				Files.createDirectories(auditPath.getParent());
			}
			if (!Files.exists(auditPath)) {
				Files.createFile(auditPath);
			}
		} catch (IOException e) {
			log.warn("初始化修数审计日志文件目录失败 path={}", auditPath, e);
		}
	}

	public Path resolveAuditFilePath() {
		String prop = System.getProperty(PROP_AUDIT_FILE);
		if (prop != null && !prop.isBlank()) {
			return Paths.get(prop.trim());
		}
		return Paths.get(DEFAULT_AUDIT_FILE);
	}

	private void registerDefaultOperators() {
		// 1. tdlv
		operators.put("tdlv", new RepairOperator(
				"tdlv",
				"天道修为等级与关联奖励状态一致性修复",
				List.of(),
				List.of("level"),
				true,
				List.of("DevelopModule"),
				(player, params) -> {
					DevelopModule dev = player.getDevelopModule();
					int oldLevel = dev.getHeavenlyDaoLevel();
					int targetLevel = oldLevel;
					if (params.containsKey("level")) {
						targetLevel = ((Number) params.get("level")).intValue();
					}
					if (targetLevel <= 0) {
						throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "天道等级必须大于0: " + targetLevel);
					}
					GmDispatcher.updateHeavenlyDaoLevel(player, targetLevel);
					try {
						player.getAttrModule().calcAllAttr();
						player.getAttrModule().calcPower();
					} catch (Throwable t) {
						log.warn("校准天道等级后重算战力异常 playerId={}", player.getPlayerId(), t);
					}
					return "天道等级校准: " + oldLevel + " -> " + targetLevel;
				}));

		// 2. questState (兼容 fixQuestState)
		RepairOperator questOp = new RepairOperator(
				"questState",
				"扫描任务模块修复进度已满足但未流转至可领奖状态的卡死任务",
				List.of(),
				List.of("questId"),
				true,
				List.of("QuestModule"),
				(player, params) -> {
					QuestModule qm = player.getQuestModule();
					int targetQuestId = params.containsKey("questId") ? ((Number) params.get("questId")).intValue() : 0;
					int fixCount = 0;
					if (targetQuestId > 0) {
						Quest q = qm.get(targetQuestId);
						if (q != null && q.getState() == (byte) QuestHelper.ACCEPTED) {
							q.checkFinish();
							if (q.getState() == (byte) QuestHelper.CAN_GIVEWARD) {
								fixCount++;
							}
						}
					} else {
						for (QuestTypeEnum type : QuestTypeEnum.values()) {
							Map<Integer, Quest> group = qm.getGroup(type);
							if (group != null) {
								for (Quest q : group.values()) {
									if (q.getState() == (byte) QuestHelper.ACCEPTED) {
										q.checkFinish();
										if (q.getState() == (byte) QuestHelper.CAN_GIVEWARD) {
											fixCount++;
										}
									}
								}
							}
						}
					}
					return "修复流转卡死任务数: " + fixCount;
				});
		operators.put("questState", questOp);
		operators.put("fixQuestState", questOp);

		// 3. battleHighest (兼容 unlockStuckStage)
		RepairOperator battleOp = new RepairOperator(
				"battleHighest",
				"主线关卡最高进度校正与异常残留战斗状态清理",
				List.of(),
				List.of("highestBattleId", "dungeonType"),
				true,
				List.of("BattleModule"),
				(player, params) -> {
					BattleModule bm = player.getBattleModule();
					int fixed = 0;
					if (params.containsKey("highestBattleId")) {
						int newHighest = ((Number) params.get("highestBattleId")).intValue();
						bm.setMainBattleHighest(newHighest);
						fixed++;
					}
					bm.setAttackingData(0, 0, 0, 0, 0L, 0L);
					return "战斗状态校准与清理完成, 关卡重设项数: " + fixed;
				});
		operators.put("battleHighest", battleOp);
		operators.put("unlockStuckStage", battleOp);
		operators.put("stuck_battle", battleOp);

		// 4. recalcPower
		operators.put("recalcPower", new RepairOperator(
				"recalcPower",
				"重算玩家全部模块属性与总战力",
				List.of(),
				List.of(),
				true,
				List.of("AttrModule", "PlayerData"),
				(player, params) -> {
					try {
						player.getAttrModule().calcAllAttr();
						player.getAttrModule().calcPower();
					} catch (Throwable t) {
						log.warn("重算战力过程中触发降级计算 playerId={}", player.getPlayerId(), t);
					}
					return "属性与战力重算完成: score=" + player.getData().getFightPower();
				}));

		// 5. deductDuplicateItem
		operators.put("deductDuplicateItem", new RepairOperator(
				"deductDuplicateItem",
				"安全扣回异常多发的道具或货币（下限保护绝不扣为负数）",
				List.of("configId", "count"),
				List.of(),
				true,
				List.of("ItemModule", "CurrencyModule"),
				(player, params) -> {
					int configId = ((Number) params.get("configId")).intValue();
					long count = ((Number) params.get("count")).longValue();
					if (count <= 0) {
						return "扣除数量非正数，跳过扣除";
					}
					GoodsModule gm = player.getGoodsModule(configId);
					long currentCount = gm != null ? gm.getCount(configId) : 0;
					long actualDeduct = Math.min(count, currentCount);
					if (actualDeduct > 0) {
						PlayerHelper.delResources(player, configId, actualDeduct, OpType.GM, false);
					}
					return "安全扣除完成: configId=" + configId + ", 申请=" + count + ", 实际扣除=" + actualDeduct + ", 剩余=" + (currentCount - actualDeduct);
				}));

		// 6. dataFix
		operators.put("dataFix", new RepairOperator(
				"dataFix",
				"兼容调用 DataFixManager 中指定的具名修数项",
				List.of("methodName"),
				List.of(),
				false,
				List.of(),
				(player, params) -> {
					String methodName = (String) params.get("methodName");
					if (methodName == null || methodName.isBlank()) {
						throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "methodName 不能为空");
					}
					DataFixManager dfm = DataFixManager.getInstance();
					try {
						Method method = DataFixManager.class.getMethod(methodName);
						java.lang.annotation.Annotation anno = null;
						for (java.lang.annotation.Annotation a : method.getAnnotations()) {
							if (a.annotationType().getSimpleName().equals("DataFix")) {
								anno = a;
								break;
							}
						}
						if (anno == null) {
							throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标方法未声明 @DataFix 注解: " + methodName);
						}
						Method depMethod = anno.annotationType().getMethod("deprecated");
						boolean isDeprecated = (Boolean) depMethod.invoke(anno);
						if (isDeprecated) {
							throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标修数方法已标记为废弃: " + methodName);
						}
						method.invoke(dfm);
						return "DataFixManager." + methodName + " 执行成功";
					} catch (LogicException le) {
						throw le;
					} catch (Exception e) {
						throw new LogicException(ErrorMsgEnum.unknown.ID, "调用 DataFixManager 方法失败: " + e.getMessage());
					}
				}));
	}

	@GmAction(name = "list", description = "列出所有受控具名修数任务元数据", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL, idempotent = true, example = "repair.list")
	public List<Map<String, Object>> list() {
		List<Map<String, Object>> results = new ArrayList<>();
		for (RepairOperator op : operators.values()) {
			// 去除别名引起的重复
			if (results.stream().anyMatch(m -> op.repairId.equals(m.get("repairId")))) {
				continue;
			}
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("repairId", op.repairId);
			map.put("description", op.description);
			map.put("requiredParams", op.requiredParams);
			map.put("optionalParams", op.optionalParams);
			map.put("supportsDryRun", op.supportsDryRun);
			map.put("affectedModules", op.affectedModules);
			results.add(map);
		}
		return results;
	}

	@GmAction(name = "execute", description = "执行受控具名修数任务（支持 dryRun 零副作用验证）", security = GmSecurityTier.OPS_REPAIR, scope = GmScope.GLOBAL, dryRunSupported = true, example = "repair.execute repairId=tdlv playerId=1001 reason='一致性修复' dryRun=true")
	public GmResult execute(
			@GmParam(name = "repairId", description = "具名修数算子ID", required = true) String repairId,
			@GmParam(name = "playerId", description = "目标玩家ID", required = true) long playerId,
			@GmParam(name = "params", description = "修数入参JSON", defaultValue = "{}") String params,
			@GmParam(name = "reason", description = "修数原因（必填）", required = true) String reason,
			@GmParam(name = "dryRun", description = "是否演练模式（仅生成Diff不持久化）", defaultValue = "false") boolean dryRun,
			@GmParam(name = "operator", description = "操作人标识", defaultValue = "system") String operator) {

		if (repairId == null || repairId.isBlank()) {
			return GmResult.fail(ErrorMsgEnum.request_parameter_error, "repairId 不能为空");
		}
		RepairOperator op = operators.get(repairId.trim());
		if (op == null) {
			return GmResult.fail(ErrorMsgEnum.gm_cmd_not_exist, "未知具名修数算子: " + repairId);
		}
		if (reason == null || reason.isBlank()) {
			return GmResult.fail(ErrorMsgEnum.request_parameter_error, "修数原因 reason 不能为空");
		}
		if (dryRun && !op.supportsDryRun) {
			return GmResult.fail(ErrorMsgEnum.request_parameter_error, "算子 " + repairId + " 不支持 dryRun 演练模式");
		}

		Map<String, Object> parsedParams = parseParams(params);

		for (String req : op.requiredParams) {
			if (!parsedParams.containsKey(req)) {
				return GmResult.fail(ErrorMsgEnum.request_parameter_error, "缺少必填参数: " + req);
			}
		}

		boolean online = PlayerManager.getInstance().isOnline(playerId);
		String traceId = UUID.randomUUID().toString();

		if (online) {
			return executeOnline(op, playerId, parsedParams, reason, dryRun, operator, traceId);
		} else {
			return executeOffline(op, playerId, parsedParams, reason, dryRun, operator, traceId);
		}
	}

	private GmResult executeOnline(RepairOperator op, long playerId, Map<String, Object> params, String reason, boolean dryRun, String operator, String traceId) {
		Player onlinePlayer = PlayerManager.getInstance().getPlayer(playerId);
		if (onlinePlayer == null) {
			return GmResult.fail(ErrorMsgEnum.player_not_online, "玩家已下线");
		}

		Processor processor = null; try { if (cn.game.util.SpringContextLoader.getContext() != null) { processor = ServerContext.getInstance().getProcessor(); } } catch (Throwable ignored) {}
		if (processor != null) {
			try {
				return processor.process(playerId, () -> doExecutePlayer(onlinePlayer, op, params, reason, dryRun, operator, traceId, true));
			} catch (Throwable t) {
				log.error("玩家虚拟线程修数执行异常 playerId={}, repairId={}", playerId, op.repairId, t);
				return GmResult.fail(ErrorMsgEnum.unknown, "在线修数执行异常: " + t.getMessage());
			}
		} else {
			return doExecutePlayer(onlinePlayer, op, params, reason, dryRun, operator, traceId, true);
		}
	}

	private GmResult executeOffline(RepairOperator op, long playerId, Map<String, Object> params, String reason, boolean dryRun, String operator, String traceId) {
		boolean locked = acquireOfflineLock(playerId);
		if (!locked) {
			return GmResult.fail(ErrorMsgEnum.unknown, "获取离线玩家并发锁冲突失败，请稍后重试: " + playerId);
		}

		try {
			// 二次检查是否刚好上线
			if (PlayerManager.getInstance().isOnline(playerId)) {
				releaseOfflineLock(playerId);
				return executeOnline(op, playerId, params, reason, dryRun, operator, traceId);
			}

			Player tempPlayer = null;
			try {
				tempPlayer = AsyncUtils.await(PlayerHelper.loadPlayerFromDb(playerId));
			} catch (Throwable t) {
				log.warn("离线加载玩家失败 playerId={}", playerId, t);
				return GmResult.fail(ErrorMsgEnum.player_not_exist, "加载离线玩家数据失败: " + t.getMessage());
			}

			if (tempPlayer == null) {
				return GmResult.fail(ErrorMsgEnum.player_not_exist, "离线玩家不存在: " + playerId);
			}

			try {
				return doExecutePlayer(tempPlayer, op, params, reason, dryRun, operator, traceId, false);
			} finally {
				try {
					PlayerHelper.clearPlayer(tempPlayer);
				} catch (Throwable t) {
					log.warn("离线玩家临时实例清理警告 playerId={}", playerId, t);
				}
			}
		} finally {
			releaseOfflineLock(playerId);
		}
	}

	private GmResult doExecutePlayer(Player player, RepairOperator op, Map<String, Object> params, String reason, boolean dryRun, String operator, String traceId, boolean isOnline) {
		long playerId = player.getPlayerId();
		Player target = player;

		// 采集前状态快照
		Map<String, Object> beforeState = captureStateSnapshot(player, op.affectedModules);

		if (dryRun && isOnline) {
			// 在线演练：构造沙箱隔离克隆
			target = cloneSandboxPlayer(player);
		}

		String summary;
		List<Map<String, Object>> diff;
		try {
			final Player finalTarget = target;
			if (dryRun) {
				summary = op.handler.apply(finalTarget, params);
			} else {
				summary = PlayerWriteGuard.call(finalTarget, () -> {
					try {
						return op.handler.apply(finalTarget, params);
					} catch (Exception e) {
						if (e instanceof RuntimeException re) throw re;
						throw new RuntimeException(e);
					}
				});
			}
		} catch (Throwable t) {
			log.error("修数算子执行业务异常 repairId={}, playerId={}", op.repairId, playerId, t);
			writeAudit(traceId, operator, reason, op.repairId, playerId, isOnline, dryRun, params, false, "异常: " + t.getMessage(), Collections.emptyList());
			return GmResult.fail(ErrorMsgEnum.unknown, "修数算子执行失败: " + t.getMessage());
		}

		// 采集后状态快照
		Map<String, Object> afterState = captureStateSnapshot(target, op.affectedModules);
		diff = computeStateDiff(beforeState, afterState);

		if (!dryRun) {
			try {
				AsyncUtils.await(PlayerHelper.saveClientCache(playerId));
				AsyncUtils.await(PlayerHelper.saveSimplePlayerToRedis(target));
			} catch (Throwable t) {
				if (GmSecurityPolicy.currentRunMode().isProduction()) {
					log.error("生产修数完成落盘持久化失败 playerId={}, repairId={}", playerId, op.repairId, t);
					writeAudit(traceId, operator, reason, op.repairId, playerId, isOnline, dryRun, params, false, "落盘失败: " + t.getMessage(), diff);
					return GmResult.fail(ErrorMsgEnum.unknown, "修数落盘持久化失败: " + t.getMessage());
				} else {
					log.warn("非生产环境持久化保存降级忽略 playerId={}, repairId={}", playerId, op.repairId, t);
				}
			}
		}

		writeAudit(traceId, operator, reason, op.repairId, playerId, isOnline, dryRun, params, true, summary, diff);

		GmResult result = GmResult.success(summary, Map.of(
				"repairId", op.repairId,
				"playerId", playerId,
				"dryRun", dryRun,
				"summary", summary
		));
		result.withDiff(diff).withDryRun(dryRun).withTraceId(traceId);
		return result;
	}

	private Map<String, Object> captureStateSnapshot(Player player, List<String> affectedModules) {
		Map<String, Object> snapshot = new LinkedHashMap<>();
		if (player == null) {
			return snapshot;
		}
		// 基础属性
		PlayerData data = player.getData();
		if (data != null) {
			snapshot.put("PlayerData.level", data.getLevel());
			snapshot.put("PlayerData.fightPower", data.getFightPower());
		}
		CurrencyModule curMod = player.getCurrencyModule();
		if (curMod != null) {
			snapshot.put("CurrencyModule.gold", curMod.getCount(Asset.gold.ID));
			snapshot.put("CurrencyModule.diamond", curMod.getCount(Asset.diamond.ID));
		}
		DevelopModule dev = player.getDevelopModule();
		if (dev != null) {
			snapshot.put("DevelopModule.heavenlyDaoLevel", dev.getHeavenlyDaoLevel());
		}
		if (player.getModules() != null) {
			for (Map.Entry<String, BasePlayerModule> entry : player.getModules().entrySet()) {
				BasePlayerModule module = entry.getValue();
				if (module != null && (affectedModules.isEmpty() || affectedModules.contains(module.getClass().getSimpleName()))) {
					try {
						Object inspected = module.inspectState();
						snapshot.put(module.getClass().getSimpleName(), inspected);
					} catch (Throwable ignored) {
					}
				}
			}
		}
		return snapshot;
	}

	private List<Map<String, Object>> computeStateDiff(Map<String, Object> before, Map<String, Object> after) {
		List<Map<String, Object>> diffs = new ArrayList<>();
		for (Map.Entry<String, Object> entry : after.entrySet()) {
			String key = entry.getKey();
			Object afterVal = entry.getValue();
			Object beforeVal = before.get(key);
			if (!Objects.equals(beforeVal, afterVal)) {
				Map<String, Object> diffItem = new LinkedHashMap<>();
				diffItem.put("path", key);
				diffItem.put("before", beforeVal);
				diffItem.put("after", afterVal);
				diffs.add(diffItem);
			}
		}
		// 检查是否有被删除的键
		for (Map.Entry<String, Object> entry : before.entrySet()) {
			String key = entry.getKey();
			if (!after.containsKey(key)) {
				Map<String, Object> diffItem = new LinkedHashMap<>();
				diffItem.put("path", key);
				diffItem.put("before", entry.getValue());
				diffItem.put("after", null);
				diffs.add(diffItem);
			}
		}
		return diffs;
	}

	private Player cloneSandboxPlayer(Player original) {
		try {
			PlayerData copyData = new PlayerData();
			ObjUtil.setDefaultValue(copyData);
			if (original.getData() != null) {
				copyData.setPlayerId(original.getPlayerId());
				copyData.setUid(original.getData().getUid());
				copyData.setName(original.getData().getName());
				copyData.setLevel(original.getLevel());
				copyData.setExp(original.getData().getExp());
				copyData.setVipLevel(original.getData().getVipLevel());
				copyData.setRefreshDay(original.getData().getRefreshDay());
				copyData.setServerId(original.getData().getServerId());
			}
			Player sandbox = new Player(copyData);
			sandbox.setOnline(false);

			if (original.getModules() != null) {
				byte[] compressed = CborSnappyUtil.compress(original.getModules());
				Map<String, BasePlayerModule> restored = CborSnappyUtil.decompress(compressed, new TypeReference<Map<String, BasePlayerModule>>() {});
				if (restored != null) {
					for (BasePlayerModule module : restored.values()) {
						if (module != null) {
							module.initDefault(sandbox);
						}
					}
					sandbox.getModules().putAll(restored);
					sandbox.initModule(new HashMap<>(restored));
				}
			}
			return sandbox;
		} catch (Throwable t) {
			log.error("构建沙箱内存克隆失败，终止修数演练 playerId={}", original.getPlayerId(), t);
			throw new LogicException(ErrorMsgEnum.unknown.getId(), "构建沙箱内存克隆失败，终止修数演练: " + t.getMessage());
		}
	}

	@GmAction(name = "auditLog", description = "查询修数审计日志", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL, idempotent = true, example = "repair.auditLog playerId=1001 limit=20")
	public List<Map<String, Object>> auditLog(
			@GmParam(name = "playerId", description = "玩家ID过滤（0表示不过滤）", defaultValue = "0") long playerId,
			@GmParam(name = "limit", description = "返回条数限制", defaultValue = "50") int limit) {

		int max = Math.min(Math.max(limit, 1), 200);
		List<Map<String, Object>> result = new ArrayList<>();
		for (Map<String, Object> logEntry : inMemoryAuditLogs) {
			if (playerId > 0) {
				Object pidObj = logEntry.get("playerId");
				if (pidObj instanceof Number num && num.longValue() != playerId) {
					continue;
				}
			}
			result.add(logEntry);
			if (result.size() >= max) {
				break;
			}
		}
		return result;
	}

	@GmAction(name = "rescan", description = "热更重扫并更新动作注册表与Schema", security = GmSecurityTier.OPS_REPAIR, scope = GmScope.GLOBAL, aliases = {"gm.hotReloadScan"}, example = "repair.rescan")
	public GmResult rescan() {
		long startNs = System.nanoTime();
		GmDispatcher.getInstance().rescan();
		long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
		int actionCount = GmDispatcher.getInstance().getSchemaCatalog(GmSecurityPolicy.currentRunMode()).getActions().size();
		int groupCount = GmDispatcher.getInstance().getSchemaCatalog(GmSecurityPolicy.currentRunMode()).getGroups().size();

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("actionCount", actionCount);
		data.put("groupCount", groupCount);
		data.put("elapsedMs", elapsedMs);

		writeAudit(UUID.randomUUID().toString(), "system", "热更重扫", "rescan", 0, false, false, Collections.emptyMap(), true, "重扫动作数: " + actionCount, Collections.emptyList());

		return GmResult.success("热更重扫完成", data);
	}

	private void writeAudit(String traceId, String operator, String reason, String repairId, long playerId, boolean online, boolean dryRun, Map<String, Object> params, boolean ok, String summary, List<Map<String, Object>> diff) {
		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put("timestamp", DateUtil.nowDateTime().format(DateUtil.formatter));
		entry.put("traceId", traceId);
		entry.put("runMode", GmSecurityPolicy.currentRunMode().name());
		entry.put("operator", operator != null ? operator : "system");
		entry.put("reason", reason != null ? reason : "");
		entry.put("actionName", "repair." + repairId);
		entry.put("repairId", repairId);
		entry.put("playerId", playerId);
		entry.put("online", online);
		entry.put("dryRun", dryRun);
		entry.put("params", params != null ? params : Collections.emptyMap());
		entry.put("ok", ok);
		entry.put("summary", summary != null ? summary : "");
		entry.put("diff", diff != null ? diff : Collections.emptyList());

		// 内存队列
		inMemoryAuditLogs.addFirst(entry);
		while (inMemoryAuditLogs.size() > MAX_IN_MEMORY_AUDIT_LOGS) {
			inMemoryAuditLogs.pollLast();
		}

		// 持久化 JSONL
		auditFileLock.lock();
		try {
			Path auditPath = resolveAuditFilePath();
			String line = JsonUtil.toJsonString(entry) + System.lineSeparator();
			Files.writeString(auditPath, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (Throwable t) {
			log.error("写入修数审计日志文件失败 path={}", resolveAuditFilePath(), t);
		} finally {
			auditFileLock.unlock();
		}
	}

	public boolean acquireOfflineLock(long playerId) {
		ReentrantLock memLock = processLocks.computeIfAbsent(playerId, k -> new ReentrantLock());
		boolean memAcquired = false;
		try {
			memAcquired = memLock.tryLock(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		if (!memAcquired) {
			return false;
		}
		try {
			RLock lock = LockUtil.tryLockSync("lock:repair:" + playerId);
			if (lock != null) {
				redissonLocks.put(playerId, lock);
				return true;
			} else {
				memLock.unlock();
				return false;
			}
		} catch (Throwable t) {
			log.warn("获取修数分布式锁异常，降级使用进程锁 playerId={}", playerId, t);
			return true;
		}
	}

	public boolean tryAcquireOfflineLockOnly(long playerId) {
		ReentrantLock memLock = processLocks.computeIfAbsent(playerId, k -> new ReentrantLock());
		if (!memLock.tryLock()) {
			return false;
		}
		try {
			RLock lock = LockUtil.tryLockNoWait(30, "lock:repair:" + playerId);
			if (lock != null) {
				redissonLocks.put(playerId, lock);
				return true;
			} else {
				memLock.unlock();
				return false;
			}
		} catch (Throwable t) {
			log.warn("尝试获取修数分布式锁异常，降级使用进程锁 playerId={}", playerId, t);
			return true;
		}
	}

	public void releaseOfflineLock(long playerId) {
		RLock lock = redissonLocks.remove(playerId);
		if (lock != null) {
			try {
				if (lock.isHeldByCurrentThread()) {
					LockUtil.unlock(lock);
				}
			} catch (Throwable t) {
				log.warn("释放修数分布式锁异常 playerId={}", playerId, t);
			}
		}
		ReentrantLock memLock = processLocks.get(playerId);
		if (memLock != null && memLock.isHeldByCurrentThread()) {
			memLock.unlock();
		}
	}

	private Map<String, Object> parseParams(String paramsJson) {
		if (paramsJson == null || paramsJson.isBlank()) {
			return new HashMap<>();
		}
		try {
			return JsonUtil.parseObject(paramsJson, new TypeReference<Map<String, Object>>() {});
		} catch (Throwable t) {
			log.warn("修数入参解析JSON异常 raw={}", paramsJson, t);
			return new HashMap<>();
		}
	}

	@FunctionalInterface
	public interface RepairHandler {
		String apply(Player player, Map<String, Object> params) throws Exception;
	}

	public static class RepairOperator {
		public final String repairId;
		public final String description;
		public final List<String> requiredParams;
		public final List<String> optionalParams;
		public final boolean supportsDryRun;
		public final List<String> affectedModules;
		public final RepairHandler handler;

		public RepairOperator(String repairId, String description, List<String> requiredParams, List<String> optionalParams, boolean supportsDryRun, List<String> affectedModules, RepairHandler handler) {
			this.repairId = repairId;
			this.description = description;
			this.requiredParams = requiredParams;
			this.optionalParams = optionalParams;
			this.supportsDryRun = supportsDryRun;
			this.affectedModules = affectedModules;
			this.handler = handler;
		}
	}
}
