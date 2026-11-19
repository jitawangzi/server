package cn.game.games.net.game.gm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.ServerContext;
import cn.game.core.net.process.Processor;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * 玩家内存与独立分表只读自省服务，提供受控模块快照且不触发任何状态修改或写库操作
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "gm", description = "GM平台元信息与只读自省")
public class PlayerInspectService {

	/** 自省服务日志记录器 */
	private static final Logger log = LoggerFactory.getLogger(PlayerInspectService.class);

	/** 跨线程投递玩家邮箱等待自省结果的最大超时秒数 */
	private static final long MAILBOX_TIMEOUT_SECONDS = 5L;

	/** 解析JSON数组格式modules过滤参数的共享Mapper */
	private static final ObjectMapper FILTER_JSON_MAPPER = new ObjectMapper();

	private static final PlayerInspectService INSTANCE = new PlayerInspectService();

	public static PlayerInspectService getInstance() {
		return INSTANCE;
	}

	/** 是否允许在缺少邮箱处理器时直读玩家模块（仅限单测隔离场景显式开启，生产默认禁用） */
	private final boolean allowDirectReadWithoutProcessor;

	/**
	 * 默认构造器，生产与常规运行态严格要求经玩家邮箱串行读取
	 */
	public PlayerInspectService() {
		this(false);
	}

	/**
	 * 构造玩家自省服务实例，支持在单元测试中显式控制无Processor时的直读开关
	 *
	 * @param allowDirectReadWithoutProcessor 是否允许缺少邮箱处理器时直读模块
	 */
	public PlayerInspectService(boolean allowDirectReadWithoutProcessor) {
		this.allowDirectReadWithoutProcessor = allowDirectReadWithoutProcessor;
	}

	/**
	 * GM结构化自省动作入口，按在线玩家邮箱串行只读抓取模块状态快照
	 *
	 * @param player 目标在线玩家
	 * @param modules 逗号分隔的模块类名或简名过滤条件
	 * @param scope 自省作用域（仅支持player）
	 * @return 自省结果
	 */
	@GmAction(
			name = "inspect",
			description = "只读自省玩家各模块内存与独立分表状态快照",
			security = GmSecurityTier.OPS_SAFE,
			scope = GmScope.PLAYER,
			sideEffects = "无副作用，仅只读内存快照",
			idempotent = true,
			example = "gm.inspect modules=MailModule,FriendModule scope=player")
	public GmResult inspect(
			Player player,
			@GmParam(name = "modules", required = false, description = "逗号分隔或JSON数组格式的模块类名或简名过滤，留空返回全部模块") String modules,
			@GmParam(name = "scope", required = false, defaultValue = "player", description = "自省作用域，目前仅支持player") String scope) {
		Set<String> filterSet;
		try {
			filterSet = parseCsvFilters(modules);
		} catch (IllegalArgumentException e) {
			return GmResult.fail(ErrorMsgEnum.request_parameter_error, e.getMessage());
		}
		return inspectSnapshot(player, filterSet, scope);
	}

	/**
	 * 按玩家ID执行只读模块自省；非player作用域直接返回不支持错误且不回退
	 *
	 * @param targetPlayerId 目标玩家ID
	 * @param moduleFilters 模块过滤集合
	 * @param scope 作用域
	 * @param explicitProcessor 可选邮箱处理器（为空时自动读取ServerContext）
	 * @return 自省结果
	 */
	public GmResult inspectPlayer(long targetPlayerId, Collection<String> moduleFilters, String scope, Processor explicitProcessor) {
		GmResult scopeError = validateScope(scope);
		if (scopeError != null) {
			return scopeError;
		}
		if (targetPlayerId <= 0) {
			return GmResult.fail(ErrorMsgEnum.request_parameter_error, "target_player_id必须大于0");
		}
		Player targetPlayer = PlayerManager.getInstance().getPlayer(targetPlayerId);
		if (targetPlayer == null) {
			return GmResult.fail(ErrorMsgEnum.player_not_online, "目标玩家不在线或未加载: " + targetPlayerId);
		}
		return inspectPlayer(targetPlayer, moduleFilters, scope, explicitProcessor);
	}

	/**
	 * 对已定位玩家实例执行只读模块自省，必要时通过玩家邮箱串行读取以保证内存可见性
	 *
	 * @param targetPlayer 目标玩家实例
	 * @param moduleFilters 模块过滤集合
	 * @param scope 作用域
	 * @param explicitProcessor 可选邮箱处理器
	 * @return 自省结果
	 */
	public GmResult inspectPlayer(Player targetPlayer, Collection<String> moduleFilters, String scope, Processor explicitProcessor) {
		GmResult scopeError = validateScope(scope);
		if (scopeError != null) {
			return scopeError;
		}
		if (targetPlayer == null) {
			return GmResult.fail(ErrorMsgEnum.player_not_online, "目标玩家不存在或未在线");
		}
		long playerId = targetPlayer.getPlayerId();
		if (GmDispatcher.isCurrentMailboxPlayer(playerId)) {
			return inspectSnapshot(targetPlayer, moduleFilters, scope);
		}
		Processor processor = resolveProcessor(explicitProcessor);
		if (processor == null) {
			if (allowDirectReadWithoutProcessor) {
				return inspectSnapshot(targetPlayer, moduleFilters, scope);
			}
			log.warn("缺少玩家邮箱处理器且未启用无处理器直读测试开关，拒绝跨线程读取 playerId={}", playerId);
			return GmResult.fail(ErrorMsgEnum.unknown, "邮箱处理器未就绪，禁止跨线程无保护读取玩家模块状态");
		}
		CompletableFuture<GmResult> future = new CompletableFuture<>();
		try {
			processor.process(playerId, () -> {
				try {
					future.complete(inspectSnapshot(targetPlayer, moduleFilters, scope));
				} catch (Throwable e) {
					log.warn("玩家邮箱内只读自省异常 playerId={}", playerId, e);
					future.complete(GmResult.fail(ErrorMsgEnum.unknown, "玩家模块自省异常: " + e.getMessage()));
				}
			});
			return future.get(MAILBOX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.warn("等待玩家邮箱只读自省结果失败 playerId={}", playerId, e);
			return GmResult.fail(ErrorMsgEnum.unknown, "等待玩家自省超时或中断: " + e.getMessage());
		}
	}

	/**
	 * 直接采集玩家模块的只读状态Map（供协议模拟 withDiff 前后快照对比使用）
	 *
	 * @param player        目标玩家
	 * @param moduleFilters 可选模块过滤集合
	 * @return 模块名 -> 状态Map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> captureModulesSnapshot(Player player, Collection<String> moduleFilters) {
		GmResult res = inspectSnapshot(player, moduleFilters, "player");
		if (res != null && res.isOk() && res.getData() instanceof Map<?, ?> map) {
			Object modulesObj = map.get("modules");
			if (modulesObj instanceof Map<?, ?> m) {
				return (Map<String, Object>) m;
			}
		}
		return Collections.emptyMap();
	}

	/**
	 * 直接在当前受控线程对玩家已装载模块做只读快照收集与单模块异常隔离
	 *
	 * @param player 目标玩家
	 * @param moduleFilters 可选模块过滤集合
	 * @param scope 作用域
	 * @return 自省结果
	 */
	public GmResult inspectSnapshot(Player player, Collection<String> moduleFilters, String scope) {
		GmResult scopeError = validateScope(scope);
		if (scopeError != null) {
			return scopeError;
		}
		if (player == null) {
			return GmResult.fail(ErrorMsgEnum.player_not_online, "目标玩家不存在或未在线");
		}
		long startNs = System.nanoTime();
		Map<String, String> orderedFilters = extractOrderedFilters(moduleFilters);
		Set<String> matchedFilters = new LinkedHashSet<>();
		Map<String, BasePlayerModule> sortedModules = new TreeMap<>();
		if (player.getModules() != null) {
			sortedModules.putAll(player.getModules());
		}

		Map<String, Object> moduleSnapshots = new LinkedHashMap<>();
		for (Map.Entry<String, BasePlayerModule> entry : sortedModules.entrySet()) {
			BasePlayerModule module = entry.getValue();
			if (module == null) {
				continue;
			}
			String fullName = module.getClass().getName();
			String simpleName = module.getClass().getSimpleName();
			if (!matchesFilter(orderedFilters.keySet(), fullName, simpleName, matchedFilters)) {
				continue;
			}
			try {
				moduleSnapshots.put(simpleName, module.inspectState());
			} catch (Throwable e) {
				log.warn("只读自省单模块异常 playerId={} module={}", player.getPlayerId(), simpleName, e);
				Map<String, Object> errorNode = new LinkedHashMap<>();
				errorNode.put("_inspectError", e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage()));
				moduleSnapshots.put(simpleName, errorNode);
			}
		}

		List<String> unknownModules = new ArrayList<>();
		for (Map.Entry<String, String> filterEntry : orderedFilters.entrySet()) {
			if (!matchedFilters.contains(filterEntry.getKey())) {
				String originalName = filterEntry.getValue();
				unknownModules.add(originalName);
				if (!moduleSnapshots.containsKey(originalName)) {
					Map<String, Object> unknownNode = new LinkedHashMap<>();
					unknownNode.put("_inspectError", "未知模块: " + originalName);
					moduleSnapshots.put(originalName, unknownNode);
				}
			}
		}

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("playerId", player.getPlayerId());
		payload.put("scope", "player");
		payload.put("moduleCount", moduleSnapshots.size());
		payload.put("hasUnknownModule", !unknownModules.isEmpty());
		if (!unknownModules.isEmpty()) {
			payload.put("unknownModules", unknownModules);
		}
		payload.put("modules", moduleSnapshots);

		long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
		if (!unknownModules.isEmpty()) {
			GmResult failResult = GmResult.fail(ErrorMsgEnum.request_parameter_error,
					"存在未知模块: " + String.join(",", unknownModules)).withElapsedMs(elapsedMs);
			failResult.setData(payload);
			return failResult;
		}
		return GmResult.success("只读自省完成，返回模块数=" + moduleSnapshots.size(), payload).withElapsedMs(elapsedMs);
	}

	private Processor resolveProcessor(Processor explicitProcessor) {
		if (explicitProcessor != null) {
			return explicitProcessor;
		}
		try {
			return ServerContext.getInstance().getProcessor();
		} catch (Exception e) {
			log.debug("获取ServerContext默认邮箱处理器未就绪，按null处理", e);
			return null;
		}
	}

	private GmResult validateScope(String scope) {
		if (scope == null || scope.isBlank() || "player".equalsIgnoreCase(scope.trim())) {
			return null;
		}
		String trimmed = scope.trim();
		if ("global".equalsIgnoreCase(trimmed)) {
			return GmResult.fail(ErrorMsgEnum.gm_cmd_not_exist, "scope=global 预留待后续批次支持，当前仅支持 player");
		}
		return GmResult.fail(ErrorMsgEnum.request_parameter_error, "当前只读自省仅支持player作用域，暂不支持scope=" + trimmed);
	}

	private static Set<String> parseCsvFilters(String csv) {
		Set<String> result = new LinkedHashSet<>();
		if (csv == null || csv.isBlank()) {
			return result;
		}
		String trimmed = csv.trim();
		if (trimmed.startsWith("[")) {
			try {
				List<Object> items = FILTER_JSON_MAPPER.readValue(trimmed, new TypeReference<List<Object>>() {
				});
				if (items != null) {
					for (Object item : items) {
						if (item != null) {
							String itemStr = String.valueOf(item).trim();
							if (!itemStr.isBlank()) {
								result.add(itemStr);
							}
						}
					}
				}
				return result;
			} catch (Exception e) {
				if (!trimmed.contains("\"") && trimmed.endsWith("]")) {
					String inner = trimmed.substring(1, trimmed.length() - 1).trim();
					if (!inner.isEmpty()) {
						for (String part : inner.split(",")) {
							if (part != null && !part.isBlank()) {
								result.add(part.trim());
							}
						}
					}
					return result;
				}
				log.warn("解析modules过滤JSON数组失败 modules={}", csv, e);
				throw new IllegalArgumentException("modules过滤参数JSON数组格式非法: " + e.getMessage(), e);
			}
		}
		for (String part : trimmed.split(",")) {
			if (part != null && !part.isBlank()) {
				result.add(part.trim());
			}
		}
		return result;
	}

	private static Map<String, String> extractOrderedFilters(Collection<String> filters) {
		Map<String, String> result = new LinkedHashMap<>();
		if (filters == null) {
			return result;
		}
		for (String item : filters) {
			if (item != null && !item.isBlank()) {
				String trimmed = item.trim();
				result.putIfAbsent(trimmed.toLowerCase(), trimmed);
			}
		}
		return result;
	}

	private static boolean matchesFilter(Set<String> filters, String fullName, String simpleName, Set<String> matchedFilters) {
		if (filters.isEmpty()) {
			return true;
		}
		boolean matched = false;
		String simpleKey = simpleName.toLowerCase();
		String fullKey = fullName.toLowerCase();
		if (filters.contains(simpleKey)) {
			matchedFilters.add(simpleKey);
			matched = true;
		}
		if (filters.contains(fullKey)) {
			matchedFilters.add(fullKey);
			matched = true;
		}
		return matched;
	}
}