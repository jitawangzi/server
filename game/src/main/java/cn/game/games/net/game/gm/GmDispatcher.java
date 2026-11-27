package cn.game.games.net.game.gm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.execute.DeadlockGuard;
import cn.game.core.execute.MailboxProcessor;
import cn.game.core.net.process.Processor;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerWriteGuard;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.SpringContextLoader;

/**
 * 统一GM注解调度核心：负责注解扫描、别名冲突启动检查、参数绑定、邮箱写保护派发与异常隔离
 *
 * @author shuyongqiang
 */
@Component
public class GmDispatcher implements ApplicationContextAware, SmartInitializingSingleton {

	private static final Logger log = LoggerFactory.getLogger(GmDispatcher.class);
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	private static final Map<Integer, ErrorMsgEnum> ERROR_BY_ID = buildErrorIndex();
	private static final ThreadLocal<Long> CURRENT_MAILBOX_PLAYER_ID = new ThreadLocal<>();

	private static volatile GmDispatcher instance;

	private final Processor explicitProcessor;
	private ApplicationContext applicationContext;
	private final Set<Object> registeredBeans = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<String, RegisteredAction> actionsById = new LinkedHashMap<>();
	private final Map<String, RegisteredAction> lookupIndex = new ConcurrentHashMap<>();
	private final Set<String> groups = new LinkedHashSet<>();
	private final Set<String> conflictedShortNames = ConcurrentHashMap.newKeySet();

	/**
	 * 默认构造器，供Spring容器实例化并注册为全局调度器
	 */
	public GmDispatcher() {
		this(null);
		instance = this;
	}

	/**
	 * 显式注入Processor的构造器，便于单元测试与独立调度
	 *
	 * @param explicitProcessor 自定义邮箱处理器（可为null，默认取ServerContext）
	 */
	public GmDispatcher(Processor explicitProcessor) {
		this.explicitProcessor = explicitProcessor;
		registerBuiltInMetaActions();
	}

	/**
	 * 获取全局默认调度器实例（未启用Spring时按需延迟初始化）
	 *
	 * @return GmDispatcher实例
	 */
	public static GmDispatcher getInstance() {
		GmDispatcher current = instance;
		if (current == null) {
			synchronized (GmDispatcher.class) {
				current = instance;
				if (current == null) {
					current = new GmDispatcher();
					instance = current;
				}
			}
		}
		return current;
	}

	/**
	 * 接收Spring容器上下文引用，确保刷新期即可读取全部@GmGroup Bean
	 *
	 * @param applicationContext Spring上下文
	 * @throws BeansException 上下文异常
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		instance = this;
	}

	/**
	 * Spring全部单例创建完成后自动扫描并注册所有@GmGroup Bean（存在冲突立即快速失败）
	 */
	@Override
	public void afterSingletonsInstantiated() {
		instance = this;
		ApplicationContext ctx = this.applicationContext != null ? this.applicationContext : SpringContextLoader.getContext();
		if (ctx == null) {
			return;
		}
		scanContextBeans(ctx);
		if (registeredBeans.size() <= 1) {
			log.error("Spring容器已就绪但未扫描到任何@GmGroup动作Bean，GM动作注册表为空");
		}
	}

	/**
	 * 供测试或手工初始化时兜底扫描Spring容器中的全部@GmGroup Bean
	 */
	public void initFromSpring() {
		instance = this;
		ApplicationContext ctx = this.applicationContext != null ? this.applicationContext : SpringContextLoader.getContext();
		if (ctx == null) {
			return;
		}
		scanContextBeans(ctx);
		if (registeredBeans.size() <= 1) {
			log.error("initFromSpring扫描完成但未发现任何@GmGroup动作Bean，GM动作注册表为空");
		}
	}

	/**
	 * 判断是否已扫描注册至少一个@GmGroup动作Bean
	 *
	 * @return true表示已存在@GmGroup动作Bean
	 */
	public boolean hasRegisteredGroupBeans() {
		return registeredBeans.size() > 1;
	}

	/**
	 * 热更重扫：重新扫描并更新动作注册表与Schema (DC-17, BR-16)
	 */
	public synchronized void rescan() {
		List<Object> previousBeans = new ArrayList<>();
		for (Object b : registeredBeans) {
			if (!(b instanceof GmMetaActions)) {
				previousBeans.add(b);
			}
		}
		ApplicationContext ctx = this.applicationContext != null ? this.applicationContext : SpringContextLoader.getContext();
		List<Object> contextBeans = new ArrayList<>();
		if (ctx != null) {
			Map<String, Object> beans = ctx.getBeansWithAnnotation(GmGroup.class);
			contextBeans.addAll(beans.values());
		}

		Set<Object> newRegisteredBeans = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<String> newGroups = new LinkedHashSet<>();
		Map<String, RegisteredAction> newActionsById = new LinkedHashMap<>();
		Map<String, RegisteredAction> newLookupIndex = new ConcurrentHashMap<>();
		Set<String> newConflictedShortNames = ConcurrentHashMap.newKeySet();

		registerBeanTo(new GmMetaActions(), newRegisteredBeans, newGroups, newActionsById, newLookupIndex, newConflictedShortNames);

		for (Object bean : previousBeans) {
			registerBeanTo(bean, newRegisteredBeans, newGroups, newActionsById, newLookupIndex, newConflictedShortNames);
		}
		for (Object bean : contextBeans) {
			registerBeanTo(bean, newRegisteredBeans, newGroups, newActionsById, newLookupIndex, newConflictedShortNames);
		}

		synchronized (this) {
			registeredBeans.clear();
			registeredBeans.addAll(newRegisteredBeans);
			groups.clear();
			groups.addAll(newGroups);
			actionsById.clear();
			actionsById.putAll(newActionsById);
			lookupIndex.clear();
			lookupIndex.putAll(newLookupIndex);
			conflictedShortNames.clear();
			conflictedShortNames.addAll(newConflictedShortNames);
		}
		log.info("GmDispatcher 热更重扫完成，已加载分组数: {}, 动作数: {}", groups.size(), actionsById.size());
	}

	private void scanContextBeans(ApplicationContext ctx) {
		if (ctx == null) {
			return;
		}
		Map<String, Object> beans = ctx.getBeansWithAnnotation(GmGroup.class);
		for (Object bean : beans.values()) {
			registerBean(bean);
		}
	}

	/**
	 * 注册单个GM分组Bean，校验命令ID与别名唯一性（冲突时快速失败）
	 *
	 * @param bean 带@GmGroup注解的处理器实例
	 */
	public synchronized void registerBean(Object bean) {
		registerBeanTo(bean, this.registeredBeans, this.groups, this.actionsById, this.lookupIndex, this.conflictedShortNames);
	}

	private void registerBeanTo(
			Object bean,
			Set<Object> targetRegisteredBeans,
			Set<String> targetGroups,
			Map<String, RegisteredAction> targetActionsById,
			Map<String, RegisteredAction> targetLookupIndex,
			Set<String> targetConflictedShortNames) {
		if (bean == null) {
			throw new IllegalArgumentException("GM bean cannot be null");
		}
		Class<?> clazz = bean.getClass();
		GmGroup groupAnno = clazz.getAnnotation(GmGroup.class);
		if (groupAnno == null) {
			throw new IllegalArgumentException("GM处理器缺少@GmGroup注解: " + clazz.getName());
		}
		if (targetRegisteredBeans.contains(bean)) {
			return;
		}
		String groupName = normalizeKey(groupAnno.name());
		if (groupName.isEmpty()) {
			throw new IllegalArgumentException("@GmGroup.name不能为空: " + clazz.getName());
		}
		targetGroups.add(groupName);

		for (Method method : clazz.getDeclaredMethods()) {
			GmAction actionAnno = method.getAnnotation(GmAction.class);
			if (actionAnno == null) {
				continue;
			}
			method.setAccessible(true);
			String actionName = normalizeKey(actionAnno.name().isEmpty() ? method.getName() : actionAnno.name());
			if (actionName.isEmpty()) {
				throw new IllegalArgumentException("GM动作名不能为空: " + clazz.getName() + "#" + method.getName());
			}
			String fullId = groupName + "." + actionName;
			List<String> aliases = new ArrayList<>();
			for (String alias : actionAnno.aliases()) {
				String normAlias = normalizeKey(alias);
				if (!normAlias.isEmpty()) {
					aliases.add(normAlias);
				}
			}

			ParamBindingSpec[] paramSpecs = buildParamSpecs(method);
			List<GmParamSchema> paramSchemas = new ArrayList<>();
			for (ParamBindingSpec spec : paramSpecs) {
				if (!spec.injected) {
					paramSchemas.add(spec.schema);
				}
			}
			GmActionSchema schema = new GmActionSchema(
					fullId,
					groupName,
					actionName,
					Collections.unmodifiableList(aliases),
					actionAnno.description(),
					actionAnno.security(),
					actionAnno.scope(),
					actionAnno.dryRunSupported(),
					actionAnno.sideEffects(),
					actionAnno.idempotent(),
					actionAnno.readOnly(),
					actionAnno.example(),
					Collections.unmodifiableList(paramSchemas));
			RegisteredAction registered = new RegisteredAction(bean, method, actionAnno, schema, paramSpecs);

			// 先做全量冲突预检，避免部分写入脏状态（fullId 与显式 aliases 必须严格全局唯一）
			ensureUniqueKey(fullId, registered, targetLookupIndex);
			for (String alias : aliases) {
				ensureUniqueKey(alias, registered, targetLookupIndex);
			}

			targetActionsById.put(fullId, registered);
			targetLookupIndex.put(fullId, registered);
			for (String alias : aliases) {
				targetLookupIndex.put(alias, registered);
			}

			// 纯动作短名如果发生跨分组同名（如config.list与repair.list），仅降级不作为全局快捷别名，避免歧义，不阻断合法分组动作注册
			if (!actionName.equals(fullId)) {
				if (!targetConflictedShortNames.contains(actionName)) {
					RegisteredAction existing = targetLookupIndex.get(actionName);
					if (existing == null) {
						targetLookupIndex.put(actionName, registered);
					} else if (existing != registered) {
						targetConflictedShortNames.add(actionName);
						if (!targetActionsById.containsKey(actionName)) {
							targetLookupIndex.remove(actionName);
						}
						log.debug("GM动作短名冲突，加入黑名单并降级为必须全名调用: shortName={}, existing={}, incoming={}",
								actionName, existing.schema.getId(), fullId);
					}
				}
			}
		}
		targetRegisteredBeans.add(bean);
	}

	/**
	 * 批量注册GM分组Bean
	 *
	 * @param beans GM处理器集合
	 */
	public void registerBeans(Collection<?> beans) {
		if (beans != null) {
			for (Object bean : beans) {
				registerBean(bean);
			}
		}
	}

	/**
	 * 检查指定命令名或ID是否已注册
	 *
	 * @param command 命令名、别名或group.action
	 * @return true表示存在
	 */
	public boolean hasCommand(String command) {
		if (command == null || command.isBlank()) {
			return false;
		}
		return lookupIndex.containsKey(normalizeKey(command));
	}

	/**
	 * 获取指定命令名、别名或规范动作ID对应的 Schema 元数据（供 HTTP 网关 RBAC 校验与审计使用）
	 *
	 * @param command 命令名、别名或 group.action
	 * @return 动作 Schema，未找到返回 null
	 */
	public GmActionSchema getActionSchema(String command) {
		if (command == null || command.isBlank()) {
			return null;
		}
		RegisteredAction reg = lookupIndex.get(normalizeKey(command));
		return reg != null ? reg.schema : null;
	}

	/**
	 * 获取按当前或指定RunMode裁剪后的GM Schema目录
	 *
	 * @param runMode 运行模式（传null则使用当前ServerContext运行模式）
	 * @return 裁剪后的Schema目录
	 */
	public GmSchemaCatalog getSchemaCatalog(RunMode runMode) {
		return getSchemaCatalog(runMode, null);
	}

	/**
	 * 获取按RunMode裁剪且可选按group过滤的GM Schema目录
	 *
	 * @param runMode 运行模式
	 * @param groupFilter 分组过滤（可为空）
	 * @return Schema目录
	 */
	public GmSchemaCatalog getSchemaCatalog(RunMode runMode, String groupFilter) {
		RunMode effectiveMode = runMode != null ? runMode : GmSecurityPolicy.currentRunMode();
		String normalizedGroup = groupFilter != null && !groupFilter.isBlank() ? normalizeKey(groupFilter) : null;
		List<GmActionSchema> visibleActions = new ArrayList<>();
		Set<String> visibleGroups = new LinkedHashSet<>();
		for (RegisteredAction reg : actionsById.values()) {
			if (!GmSecurityPolicy.isActionAllowed(effectiveMode, reg.schema.getSecurity())) {
				continue;
			}
			if (normalizedGroup != null && !normalizedGroup.equals(reg.schema.getGroup())) {
				continue;
			}
			visibleGroups.add(reg.schema.getGroup());
			visibleActions.add(reg.schema);
		}
		return new GmSchemaCatalog(effectiveMode.name(), new ArrayList<>(visibleGroups), visibleActions);
	}

	/**
	 * 派发原始文本GM命令行（如 "item 100001 5"、"item itemdel 100001 5" 或 "config.list ShopItem"）
	 *
	 * @param player 触发玩家（GLOBAL命令可为null）
	 * @param rawCommand 原始命令文本
	 * @return 结构化执行结果
	 */
	public GmResult dispatchText(Player player, String rawCommand) {
		long startNanos = System.nanoTime();
		String traceId = UUID.randomUUID().toString();
		List<String> tokens;
		try {
			tokens = GmCommandTokenizer.tokenize(rawCommand);
		} catch (IllegalArgumentException e) {
			log.warn("GM命令行分词失败 rawCommand={}, traceId={}", rawCommand, traceId, e);
			return finalizeResult(GmResult.fail(ErrorMsgEnum.gm_cmd_param, e.getMessage()), traceId, startNanos, false);
		}
		if (tokens.isEmpty()) {
			return finalizeResult(GmResult.fail(ErrorMsgEnum.gm_cmd_not_exist, "GM命令不能为空"), traceId, startNanos, false);
		}

		String first = normalizeKey(tokens.get(0));
		int argOffset = 1;
		RegisteredAction action = null;
		// 优先匹配两段式 group.action，避免 "item itemdel 1001 1" 被同名短命令 "item" 抢占
		if (tokens.size() >= 2) {
			String composite = first + "." + normalizeKey(tokens.get(1));
			action = lookupIndex.get(composite);
			if (action != null) {
				argOffset = 2;
			}
		}
		if (action == null) {
			action = lookupIndex.get(first);
		}
		if (action == null) {
			return finalizeResult(GmResult.fail(ErrorMsgEnum.gm_cmd_not_exist, "未知GM命令: " + tokens.get(0)), traceId, startNanos, false);
		}

		List<String> argTokens = tokens.subList(argOffset, tokens.size());
		long playerId = player != null ? player.getPlayerId() : 0L;
		return executeResolvedAction(action, player, playerId, argTokens, null, traceId, false, "text", startNanos);
	}

	/**
	 * 兼容旧GmRegistry数组入参的文本命令派发
	 *
	 * @param player 玩家上下文
	 * @param cmd 命令名
	 * @param params 参数数组
	 * @return 结构化执行结果
	 */
	public GmResult dispatchLegacy(Player player, String cmd, String[] params) {
		long startNanos = System.nanoTime();
		String traceId = UUID.randomUUID().toString();
		RegisteredAction action = cmd != null ? lookupIndex.get(normalizeKey(cmd)) : null;
		if (action == null) {
			return finalizeResult(GmResult.fail(ErrorMsgEnum.gm_cmd_not_exist, "未知GM命令: " + cmd), traceId, startNanos, false);
		}
		List<String> argTokens;
		if (params == null || params.length == 0) {
			argTokens = Collections.emptyList();
		} else if (params[0] != null && params[0].equalsIgnoreCase(cmd)) {
			argTokens = Arrays.asList(params).subList(1, params.length);
		} else {
			argTokens = Arrays.asList(params);
		}
		long playerId = player != null ? player.getPlayerId() : 0L;
		return executeResolvedAction(action, player, playerId, argTokens, null, traceId, false, "legacy", startNanos);
	}

	/**
	 * 结构化参数派发GM动作（供HTTP控制台、Proto测试平台等统一调用）
	 *
	 * @param commandOrId 动作ID（group.action）、动作名或别名
	 * @param playerId 目标玩家ID（GLOBAL动作可传0）
	 * @param params 结构化键值对参数
	 * @param traceId 链路追踪ID（为空时自动生成）
	 * @param dryRun 是否演练
	 * @return 结构化执行结果
	 */
	/**
	 * 分发HTTP网关文本GM命令（source标记为http，支持可选traceId与dryRun）
	 *
	 * @param playerId 目标玩家ID（无玩家时为0）
	 * @param rawCommand 原始命令字符串
	 * @param traceId 调用链跟踪ID（可为空）
	 * @param dryRun 是否试运行
	 * @return 结构化执行结果
	 */
	public GmResult dispatchHttpText(long playerId, String rawCommand, String traceId, boolean dryRun) {
		long startNanos = System.nanoTime();
		String effectiveTraceId = traceId != null && !traceId.isBlank() ? traceId : UUID.randomUUID().toString();
		List<String> tokens;
		try {
			tokens = GmCommandTokenizer.tokenize(rawCommand);
		} catch (IllegalArgumentException e) {
			log.warn("HTTP GM命令行分词失败 rawCommand={}, traceId={}", rawCommand, effectiveTraceId, e);
			return finalizeResult(GmResult.fail(ErrorMsgEnum.gm_cmd_param, e.getMessage()), effectiveTraceId, startNanos, dryRun);
		}
		if (tokens.isEmpty()) {
			return finalizeResult(GmResult.fail(ErrorMsgEnum.gm_cmd_not_exist, "GM命令不能为空"), effectiveTraceId, startNanos, dryRun);
		}

		String first = normalizeKey(tokens.get(0));
		int argOffset = 1;
		RegisteredAction action = null;
		if (tokens.size() >= 2) {
			String composite = first + "." + normalizeKey(tokens.get(1));
			action = lookupIndex.get(composite);
			if (action != null) {
				argOffset = 2;
			}
		}
		if (action == null) {
			action = lookupIndex.get(first);
		}
		if (action == null) {
			return finalizeResult(GmResult.fail(ErrorMsgEnum.gm_cmd_not_exist, "未知GM命令: " + tokens.get(0)), effectiveTraceId, startNanos, dryRun);
		}

		List<String> argTokens = tokens.subList(argOffset, tokens.size());
		Player player = playerId > 0 ? PlayerManager.getInstance().getPlayer(playerId) : null;
		return executeResolvedAction(action, player, playerId, argTokens, null, effectiveTraceId, dryRun, "http", startNanos);
	}

	public GmResult dispatchAction(String commandOrId, long playerId, Map<String, Object> params, String traceId, boolean dryRun) {
		long startNanos = System.nanoTime();
		String effectiveTraceId = traceId != null && !traceId.isBlank() ? traceId : UUID.randomUUID().toString();
		RegisteredAction action = commandOrId != null ? lookupIndex.get(normalizeKey(commandOrId)) : null;
		if (action == null) {
			return finalizeResult(GmResult.fail(ErrorMsgEnum.gm_cmd_not_exist, "未知GM命令: " + commandOrId), effectiveTraceId, startNanos, dryRun);
		}
		Player player = playerId > 0 ? PlayerManager.getInstance().getPlayer(playerId) : null;
		Map<String, Object> safeParams = params != null ? params : Collections.emptyMap();
		return executeResolvedAction(action, player, playerId, null, safeParams, effectiveTraceId, dryRun, "action", startNanos);
	}

	/**
	 * 共享玩家天道修为变更逻辑（供PlayerGm与0x77 GmHandler统一复用）
	 *
	 * @param player 玩家对象
	 * @param level 目标等级
	 * @return true表示数值发生变更
	 */
	public static boolean updateHeavenlyDaoLevel(Player player, int level) {
		if (player == null) {
			return false;
		}
		if (player.getDevelopModule().getHeavenlyDaoLevel() != level) {
			player.getDevelopModule().setHeavenlyDaoLevel(level);
			return true;
		}
		return false;
	}
	private GmResult executeResolvedAction(RegisteredAction action, Player player, long playerId, List<String> textArgs,
			Map<String, Object> namedParams, String traceId, boolean dryRun, String source, long startNanos) {
		RunMode runMode = GmSecurityPolicy.currentRunMode();
		if (!GmSecurityPolicy.isActionAllowed(runMode, action.schema.getSecurity())) {
			return finalizeResult(
					GmResult.fail(ErrorMsgEnum.production_gm_not_allow, "当前运行模式禁止执行该GM指令: " + action.schema.getId()),
					traceId, startNanos, dryRun);
		}
		if ("text".equals(source) || "legacy".equals(source)) {
			try {
				GmSecurityPolicy.checkChatGmAccess(player, runMode, action.schema.getSecurity());
			} catch (LogicException e) {
				log.warn("GM聊天权限校验失败 cmd={}, playerId={}, traceId={}", action.schema.getId(), playerId, traceId, e);
				return finalizeResult(toErrorResult(e), traceId, startNanos, dryRun);
			}
		}
		if (dryRun && !action.schema.isDryRunSupported()) {
			return finalizeResult(
					GmResult.fail(ErrorMsgEnum.request_parameter_error,
							"该 GM 指令不支持 dryRun 演练模式，已拒绝执行以防误改数据: " + action.schema.getId()),
					traceId, startNanos, true);
		}

		boolean requiresPlayer = action.schema.getScope() == GmScope.PLAYER || action.needsPlayer;
		Player targetPlayer = player;
		if (requiresPlayer && targetPlayer == null && playerId > 0) {
			targetPlayer = PlayerManager.getInstance().getPlayer(playerId);
		}
		if (requiresPlayer && targetPlayer == null) {
			return finalizeResult(
					GmResult.fail(ErrorMsgEnum.player_not_online, "目标玩家不在线: " + playerId),
					traceId, startNanos, dryRun);
		}

		GmExecContext context = new GmExecContext(
				targetPlayer,
				targetPlayer != null ? targetPlayer.getPlayerId() : playerId,
				action.schema.getId(),
				traceId,
				dryRun,
				runMode,
				source);

		Object[] boundArgs;
		try {
			boundArgs = bindMethodArguments(action, context, textArgs, namedParams);
		} catch (LogicException e) {
			log.warn("GM参数绑定失败 cmd={}, traceId={}", action.schema.getId(), traceId, e);
			return finalizeResult(toErrorResult(e), traceId, startNanos, dryRun);
		} catch (Throwable e) {
			log.warn("GM参数解析异常 cmd={}, traceId={}", action.schema.getId(), traceId, e);
			return finalizeResult(GmResult.fail(ErrorMsgEnum.gm_cmd_param, "参数解析失败: " + e.getMessage()), traceId, startNanos, dryRun);
		}

		GmResult result;
		// 仅 PLAYER 作用域动作进入玩家邮箱与 PlayerWriteGuard；GLOBAL 动作（如 config/proto/gm.schema）直接执行避免锁耦合
		if (action.schema.getScope() == GmScope.PLAYER && targetPlayer != null) {
			long targetId = targetPlayer.getPlayerId();
			Processor proc = resolveProcessor();
			if (proc != null && !isOnPlayerMailbox(targetId)) {
				final Player finalPlayer = targetPlayer;
				try {
					result = proc.process(targetId, () -> runInPlayerWriteScope(finalPlayer, action, boundArgs, context));
				} catch (Throwable e) {
					log.error("GM指令队列派发异常 cmd={}, playerId={}, traceId={}", action.schema.getId(), targetId, traceId, e);
					result = GmResult.fail(ErrorMsgEnum.unknown, "GM指令派发异常: " + e.getMessage());
				}
			} else {
				result = runInPlayerWriteScope(targetPlayer, action, boundArgs, context);
			}
		} else {
			result = invokeSafely(action, boundArgs, context);
		}
		if (result == null) {
			result = GmResult.success("ok", null);
		}
		result.addWarnings(context.getWarnings());
		return finalizeResult(result, traceId, startNanos, dryRun);
	}

	private GmResult runInPlayerWriteScope(Player player, RegisteredAction action, Object[] boundArgs, GmExecContext context) {
		long targetId = player.getPlayerId();
		Long previous = CURRENT_MAILBOX_PLAYER_ID.get();
		CURRENT_MAILBOX_PLAYER_ID.set(targetId);
		try {
			return PlayerWriteGuard.call(player, () -> invokeSafely(action, boundArgs, context));
		} finally {
			if (previous == null) {
				CURRENT_MAILBOX_PLAYER_ID.remove();
			} else {
				CURRENT_MAILBOX_PLAYER_ID.set(previous);
			}
		}
	}

	private GmResult invokeSafely(RegisteredAction action, Object[] boundArgs, GmExecContext context) {
		try {
			Object ret = action.method.invoke(action.bean, boundArgs);
			if (ret instanceof GmResult gmResult) {
				return gmResult;
			}
			return GmResult.success("ok", ret);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException() != null ? e.getTargetException() : e;
			if (target instanceof LogicException logicEx) {
				log.warn("GM指令业务拒绝 cmd={}, playerId={}, traceId={}, code={}",
						action.schema.getId(), context.getPlayerId(), context.getTraceId(), logicEx.getErrorCode(), logicEx);
				return toErrorResult(logicEx);
			}
			log.error("GM指令执行异常 cmd={}, playerId={}, traceId={}",
					action.schema.getId(), context.getPlayerId(), context.getTraceId(), target);
			return GmResult.fail(ErrorMsgEnum.unknown, "GM指令执行异常: " + target.getMessage());
		} catch (LogicException e) {
			log.warn("GM指令业务拒绝 cmd={}, playerId={}, traceId={}",
					action.schema.getId(), context.getPlayerId(), context.getTraceId(), e);
			return toErrorResult(e);
		} catch (Throwable e) {
			log.error("GM指令反射调用异常 cmd={}, playerId={}, traceId={}",
					action.schema.getId(), context.getPlayerId(), context.getTraceId(), e);
			return GmResult.fail(ErrorMsgEnum.unknown, "GM指令执行异常: " + e.getMessage());
		}
	}

	private Object[] bindMethodArguments(RegisteredAction action, GmExecContext context,
			List<String> textArgs, Map<String, Object> namedParams) {
		ParamBindingSpec[] specs = action.paramSpecs;
		Object[] result = new Object[specs.length];

		if (namedParams != null) {
			Map<String, Object> lowerCaseNamed = new HashMap<>();
			for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
				if (entry.getKey() != null) {
					lowerCaseNamed.put(normalizeKey(entry.getKey()), entry.getValue());
				}
			}
			for (int i = 0; i < specs.length; i++) {
				ParamBindingSpec spec = specs[i];
				if (spec.injected) {
					result[i] = resolveInjectedArg(spec.javaType, context, textArgs);
					continue;
				}
				String key = normalizeKey(spec.schema.getName());
				Object rawVal = namedParams.containsKey(spec.schema.getName())
						? namedParams.get(spec.schema.getName())
						: lowerCaseNamed.get(key);
				result[i] = convertOrDefault(spec, rawVal);
			}
			return result;
		}

		List<String> safeTextArgs = textArgs != null ? textArgs : Collections.emptyList();
		Set<String> declaredNames = new LinkedHashSet<>();
		for (ParamBindingSpec spec : specs) {
			if (!spec.injected) {
				declaredNames.add(normalizeKey(spec.schema.getName()));
			}
		}

		Map<String, String> namedOverrides = new HashMap<>();
		List<String> positional = new ArrayList<>();
		for (String token : safeTextArgs) {
			int eqIdx = token != null ? token.indexOf('=') : -1;
			if (eqIdx > 0) {
				String candidateName = normalizeKey(token.substring(0, eqIdx));
				if (declaredNames.contains(candidateName)) {
					namedOverrides.put(candidateName, token.substring(eqIdx + 1));
					continue;
				}
			}
			positional.add(token);
		}

		int posCursor = 0;
		for (int i = 0; i < specs.length; i++) {
			ParamBindingSpec spec = specs[i];
			if (spec.injected) {
				result[i] = resolveInjectedArg(spec.javaType, context, safeTextArgs);
				continue;
			}
			String key = normalizeKey(spec.schema.getName());
			Object rawVal;
			if (namedOverrides.containsKey(key)) {
				rawVal = namedOverrides.get(key);
			} else if (posCursor < positional.size()) {
				rawVal = positional.get(posCursor++);
			} else {
				rawVal = null;
			}
			result[i] = convertOrDefault(spec, rawVal);
		}
		return result;
	}

	private Object resolveInjectedArg(Class<?> javaType, GmExecContext context, List<String> textArgs) {
		if (Player.class.isAssignableFrom(javaType)) {
			return context.getPlayer();
		}
		if (GmExecContext.class.isAssignableFrom(javaType)) {
			return context;
		}
		if (String[].class.equals(javaType)) {
			return textArgs != null ? textArgs.toArray(new String[0]) : new String[0];
		}
		return null;
	}

	private Object convertOrDefault(ParamBindingSpec spec, Object rawVal) {
		if (rawVal == null || (rawVal instanceof String s && s.isEmpty() && spec.javaType != String.class)) {
			String def = spec.schema.getDefaultValue();
			if (def != null && !def.isEmpty()) {
				rawVal = def;
			} else if (!spec.schema.isRequired()) {
				return defaultValueForType(spec.javaType);
			} else if (rawVal == null) {
				throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(), "缺少必填参数: " + spec.schema.getName());
			}
		}
		return convertValue(spec.schema.getName(), spec.javaType, rawVal);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object convertValue(String paramName, Class<?> targetType, Object rawVal) {
		if (rawVal == null) {
			return defaultValueForType(targetType);
		}
		if (targetType.isInstance(rawVal)) {
			return rawVal;
		}
		String str = String.valueOf(rawVal).trim();
		try {
			if (targetType == int.class || targetType == Integer.class) {
				if (rawVal instanceof Number n) {
					return n.intValue();
				}
				return Integer.parseInt(str);
			}
			if (targetType == long.class || targetType == Long.class) {
				if (rawVal instanceof Number n) {
					return n.longValue();
				}
				return Long.parseLong(str);
			}
			if (targetType == double.class || targetType == Double.class) {
				if (rawVal instanceof Number n) {
					return n.doubleValue();
				}
				return Double.parseDouble(str);
			}
			if (targetType == float.class || targetType == Float.class) {
				if (rawVal instanceof Number n) {
					return n.floatValue();
				}
				return Float.parseFloat(str);
			}
			if (targetType == boolean.class || targetType == Boolean.class) {
				if (rawVal instanceof Boolean b) {
					return b;
				}
				if ("true".equalsIgnoreCase(str) || "1".equals(str)) {
					return true;
				}
				if ("false".equalsIgnoreCase(str) || "0".equals(str)) {
					return false;
				}
				throw new IllegalArgumentException("无法解析布尔值: " + str);
			}
			if (targetType == String.class) {
				return String.valueOf(rawVal);
			}
			if (targetType.isEnum()) {
				Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
				for (Enum<?> constant : enumType.getEnumConstants()) {
					if (constant.name().equalsIgnoreCase(str)) {
						return constant;
					}
				}
				throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(),
						"参数 " + paramName + " 枚举值非法: " + str + "，可选值: " + Arrays.toString(enumType.getEnumConstants()));
			}
			if (rawVal instanceof String jsonStr) {
				return JSON_MAPPER.readValue(jsonStr, targetType);
			}
			return JSON_MAPPER.convertValue(rawVal, targetType);
		} catch (LogicException e) {
			throw e;
		} catch (Exception e) {
			log.warn("GM参数类型转换失败: paramName={}, targetType={}, rawVal={}", paramName, targetType.getSimpleName(), rawVal, e);
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.getId(),
					"参数 " + paramName + " 类型转换失败(" + targetType.getSimpleName() + "): " + rawVal);
		}
	}

	private Object defaultValueForType(Class<?> type) {
		if (type == int.class) {
			return 0;
		}
		if (type == long.class) {
			return 0L;
		}
		if (type == double.class) {
			return 0.0d;
		}
		if (type == float.class) {
			return 0.0f;
		}
		if (type == boolean.class) {
			return false;
		}
		return null;
	}

	private ParamBindingSpec[] buildParamSpecs(Method method) {
		Parameter[] params = method.getParameters();
		ParamBindingSpec[] specs = new ParamBindingSpec[params.length];
		for (int i = 0; i < params.length; i++) {
			Parameter p = params[i];
			Class<?> pType = p.getType();
			boolean injected = Player.class.isAssignableFrom(pType)
					|| GmExecContext.class.isAssignableFrom(pType)
					|| String[].class.equals(pType);
			if (injected) {
				specs[i] = new ParamBindingSpec(pType, true, null);
				continue;
			}
			GmParam anno = p.getAnnotation(GmParam.class);
			String annoName = anno != null ? anno.name().trim() : "";
			String name;
			if (!annoName.isEmpty()) {
				name = annoName;
			} else {
				if (!p.isNamePresent()) {
					throw new IllegalStateException("GM方法参数缺少-parameters形参名且未显式指定@GmParam(name): "
							+ method.getDeclaringClass().getName() + "#" + method.getName() + " index=" + i);
				}
				name = p.getName();
			}
			String desc = anno != null ? anno.description() : "";
			boolean required = anno == null || anno.required();
			String defVal = anno != null ? anno.defaultValue() : "";
			String example = anno != null ? anno.example() : "";
			String ref = anno != null ? anno.ref() : "";
			GmParamType paramType = resolveParamType(pType, anno);
			List<String> options = Collections.emptyList();
			if (pType.isEnum()) {
				Object[] constants = pType.getEnumConstants();
				List<String> values = new ArrayList<>(constants.length);
				for (Object c : constants) {
					values.add(((Enum<?>) c).name());
				}
				options = Collections.unmodifiableList(values);
			}
			GmParamSchema schema = new GmParamSchema(
					name,
					pType.getSimpleName(),
					paramType,
					required,
					defVal,
					desc,
					example,
					ref,
					options);
			specs[i] = new ParamBindingSpec(pType, false, schema);
		}
		return specs;
	}

	private GmParamType resolveParamType(Class<?> javaType, GmParam anno) {
		if (anno != null && anno.type() != GmParamType.AUTO) {
			return anno.type();
		}
		if (javaType == int.class || javaType == Integer.class) {
			return GmParamType.INT;
		}
		if (javaType == long.class || javaType == Long.class) {
			return GmParamType.LONG;
		}
		if (javaType == double.class || javaType == Double.class || javaType == float.class || javaType == Float.class) {
			return GmParamType.DOUBLE;
		}
		if (javaType == boolean.class || javaType == Boolean.class) {
			return GmParamType.BOOLEAN;
		}
		if (javaType.isEnum()) {
			return GmParamType.ENUM;
		}
		if (javaType == String.class) {
			return GmParamType.STRING;
		}
		return GmParamType.JSON;
	}

	private void ensureUniqueKey(String key, RegisteredAction incoming, Map<String, RegisteredAction> targetLookupIndex) {
		RegisteredAction existing = targetLookupIndex.get(key);
		if (existing != null && existing != incoming) {
			throw new IllegalStateException("GM命令或别名重复冲突: key=" + key
					+ ", existing=" + existing.schema.getId()
					+ ", incoming=" + incoming.schema.getId());
		}
	}

	/**
	 * 判断当前执行线程是否已处于指定玩家的邮箱串行上下文中（同时兼容GmDispatcher与MailboxProcessor）
	 *
	 * @param playerId 玩家ID
	 * @return true表示当前处于目标玩家邮箱线程
	 */
	public static boolean isCurrentMailboxPlayer(long playerId) {
		return isOnPlayerMailbox(playerId);
	}

	private static boolean isOnPlayerMailbox(long playerId) {
		Long current = CURRENT_MAILBOX_PLAYER_ID.get();
		if (current != null && current.longValue() == playerId) {
			return true;
		}
		Long mailboxEntityId = MailboxProcessor.currentEntityId();
		if (mailboxEntityId != null && mailboxEntityId.longValue() == playerId) {
			return true;
		}
		return Long.valueOf(playerId).equals(DeadlockGuard.getCurrentId());
	}

	private Processor resolveProcessor() {
		if (explicitProcessor != null) {
			return explicitProcessor;
		}
		try {
			return ServerContext.getInstance().getProcessor();
		} catch (Throwable e) {
			log.warn("无法从ServerContext获取Processor，回退为当前线程执行", e);
			return null;
		}
	}

	private GmResult toErrorResult(LogicException e) {
		int code = e.getErrorCode();
		ErrorMsgEnum enumVal = ERROR_BY_ID.get(code);
		String errorName = enumVal != null ? enumVal.name() : String.valueOf(code);
		String msg = e.getErrorMessage() != null && !e.getErrorMessage().isEmpty()
				? e.getErrorMessage()
				: (enumVal != null ? enumVal.getDesc() : "error:" + code);
		return GmResult.fail(code, errorName, msg);
	}

	private GmResult finalizeResult(GmResult result, String traceId, long startNanos, boolean dryRun) {
		long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
		result.withTraceId(traceId).withElapsedMs(elapsedMs).withDryRun(dryRun);
		return result;
	}

	private String normalizeKey(String key) {
		return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
	}

	private void registerBuiltInMetaActions() {
		registerBean(new GmMetaActions());
	}

	private static Map<Integer, ErrorMsgEnum> buildErrorIndex() {
		Map<Integer, ErrorMsgEnum> map = new HashMap<>();
		for (ErrorMsgEnum item : ErrorMsgEnum.values()) {
			map.put(item.getId(), item);
		}
		return Collections.unmodifiableMap(map);
	}

	@GmGroup(name = "gm", description = "GM平台元信息与Schema目录")
	private final class GmMetaActions {

		@GmAction(name = "schema", description = "获取按当前RunMode过滤后的GM指令Schema目录", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL, idempotent = true, readOnly = true, example = "gm.schema")
		public GmSchemaCatalog schema(
				@GmParam(description = "可选分组过滤", required = false) String group) {
			return getSchemaCatalog(GmSecurityPolicy.currentRunMode(), group);
		}

		@GmAction(name = "snapshot.diff", description = "对比两个命名快照或对比快照与当前玩家状态的字段差异", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, idempotent = true, readOnly = true, example = "gm.snapshot.diff base=qa_1 target=__CURRENT__")
		public Map<String, Object> snapshotDiff(
				Player player,
				@GmParam(name = "base", description = "基准快照名称", defaultValue = "qa_quick") String base,
				@GmParam(name = "target", description = "目标快照名称(__CURRENT__表示当前实时状态)", required = false, defaultValue = "__CURRENT__") String target,
				@GmParam(name = "modules", description = "可选模块过滤(逗号分隔)", required = false, defaultValue = "") String modules) {
			return PlayerSnapshotService.getInstance().diffSnapshots(player, base, target, modules);
		}

		@GmAction(name = "audit.list", description = "查询最近GM控制台操作审计日志", security = GmSecurityTier.OPS_SAFE, scope = GmScope.GLOBAL, idempotent = true, readOnly = true, example = "gm.audit.list limit=50")
		public Map<String, Object> auditList(
				@GmParam(name = "operator", description = "操作人过滤", required = false) String operator,
				@GmParam(name = "keyword", description = "动作或端点关键字", required = false) String keyword,
				@GmParam(name = "playerId", description = "目标玩家ID过滤", required = false, defaultValue = "0") long playerId,
				@GmParam(name = "onlyFailed", description = "是否仅看失败记录", required = false, defaultValue = "false") boolean onlyFailed,
				@GmParam(name = "limit", description = "返回条数上限", required = false, defaultValue = "50") int limit) {
			return GmAuditLogService.getInstance().query(operator, keyword, playerId > 0 ? playerId : null, onlyFailed, limit);
		}
	}

	private static final class RegisteredAction {
		final Object bean;
		final Method method;
		final GmAction annotation;
		final GmActionSchema schema;
		final ParamBindingSpec[] paramSpecs;
		final boolean needsPlayer;

		RegisteredAction(Object bean, Method method, GmAction annotation, GmActionSchema schema, ParamBindingSpec[] paramSpecs) {
			this.bean = bean;
			this.method = method;
			this.annotation = annotation;
			this.schema = schema;
			this.paramSpecs = paramSpecs;
			boolean hasPlayerParam = false;
			for (ParamBindingSpec spec : paramSpecs) {
				if (spec.injected && Player.class.isAssignableFrom(spec.javaType)) {
					hasPlayerParam = true;
					break;
				}
			}
			this.needsPlayer = hasPlayerParam;
		}
	}

	private static final class ParamBindingSpec {
		final Class<?> javaType;
		final boolean injected;
		final GmParamSchema schema;

		ParamBindingSpec(Class<?> javaType, boolean injected, GmParamSchema schema) {
			this.javaType = javaType;
			this.injected = injected;
			this.schema = schema;
		}
	}
}