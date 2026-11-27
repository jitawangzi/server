package cn.game.games.net.game.gm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.ConfigService;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Player;
import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * GM统一安全策略基线，负责RunMode准入、0x77协议权限与HTTP运维配置读取
 *
 * @author shuyongqiang
 */
public final class GmSecurityPolicy {

	private static final Logger log = LoggerFactory.getLogger(GmSecurityPolicy.class);

	public static final String PROP_HTTP_ENABLED = "gm.http.enabled";
	public static final String PROP_HTTP_PORT = "gm.http.port";
	public static final String PROP_HTTP_HOST = "gm.http.host";
	public static final String PROP_REQUIRE_AUTH = "gm.http.requireAuth";
	public static final String PROP_OPS_TOKEN = "gm.http.opsToken";
	public static final String PROP_TOKEN = "gm.http.token";
	public static final String PROP_RBAC_TOKENS = "gm.http.rbacTokens";
	public static final String ENV_HTTP_ENABLED = "GM_HTTP_ENABLED";
	public static final String ENV_HTTP_PORT = "GM_HTTP_PORT";
	public static final String ENV_HTTP_HOST = "GM_HTTP_HOST";
	public static final String ENV_REQUIRE_AUTH = "GM_HTTP_REQUIRE_AUTH";
	public static final String ENV_OPS_TOKEN = "GM_HTTP_OPS_TOKEN";
	public static final String ENV_TOKEN = "GM_HTTP_TOKEN";
	public static final String ENV_RBAC_TOKENS = "GM_HTTP_RBAC_TOKENS";

	private static final Set<String> READONLY_WHITELIST_ACTIONS = Set.of(
			"config.tables", "config.list", "config.get",
			"gm.inspect", "gm.schema", "gm.help", "gm.audit.list",
			"time.status", "player.time.status", "activity.status");
	public static final int DEFAULT_HTTP_PORT = 18080;
	public static final int RESERVED_IDENTITY_PORT = 17011;

	private GmSecurityPolicy() {
	}

	/**
	 * 获取当前运行模式（若未初始化则Fail-Closed回退为PRODUCTION）
	 *
	 * @return 运行模式
	 */
	public static RunMode currentRunMode() {
		RunMode mode = ServerContext.getInstance().getRunMode();
		return mode != null ? mode : RunMode.PRODUCTION;
	}

	/**
	 * 判断指定安全等级在给定运行模式下是否允许暴露与执行
	 *
	 * @param runMode 当前运行模式
	 * @param tier 指令安全等级
	 * @return true表示允许
	 */
	public static boolean isActionAllowed(RunMode runMode, GmSecurityTier tier) {
		if (tier == null) {
			return false;
		}
		RunMode effectiveMode = runMode != null ? runMode : currentRunMode();
		if (!effectiveMode.isProduction()) {
			return true;
		}
		return tier == GmSecurityTier.OPS_SAFE || tier == GmSecurityTier.OPS_REPAIR;
	}

	/**
	 * 校验聊天框GM文本入口执行权限，生产环境严格校验玩家GM白名单（null玩家默认拒绝）
	 *
	 * @param player 触发玩家
	 * @param runMode 当前运行模式
	 * @param tier 目标动作安全等级
	 */
	public static void checkChatGmAccess(Player player, RunMode runMode, GmSecurityTier tier) {
		RunMode effectiveMode = runMode != null ? runMode : currentRunMode();
		if (!isActionAllowed(effectiveMode, tier)) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow);
		}
		if (effectiveMode.isProduction() && (player == null || player.getData() == null || !player.getData().getIsGm())) {
			throw new LogicException(ErrorMsgEnum.gm_not);
		}
	}

	/**
	 * 校验0x77协议入口访问权限，生产环境严格校验player.getData().getIsGm()（null玩家默认拒绝）
	 *
	 * @param player 请求所属玩家
	 * @param runMode 当前运行模式
	 * @return true表示放行
	 */
	public static boolean check0x77Access(Player player, RunMode runMode) {
		RunMode effectiveMode = runMode != null ? runMode : currentRunMode();
		if (!effectiveMode.isProduction()) {
			return true;
		}
		if (player == null || player.getData() == null || !player.getData().getIsGm()) {
			throw new LogicException(ErrorMsgEnum.gm_not);
		}
		return true;
	}

	/**
	 * 判断HTTP GM网关是否启用（非生产默认启用，生产默认关闭除非显式开启）
	 *
	 * @param runMode 运行模式
	 * @return true表示允许启用
	 */
	public static boolean isHttpGmEnabled(RunMode runMode) {
		RunMode effectiveMode = runMode != null ? runMode : currentRunMode();
		String explicit = readConfigValue(PROP_HTTP_ENABLED, ENV_HTTP_ENABLED);
		if (explicit != null && !explicit.isBlank()) {
			return Boolean.parseBoolean(explicit.trim());
		}
		return !effectiveMode.isProduction();
	}

	/**
	 * 判断HTTP GM接口是否强制要求鉴权（生产环境始终建议开启，未配置Apollo时安全回退）
	 *
	 * @return true表示需要Token鉴权
	 */
	public static boolean isRequireAuth() {
		String explicit = readConfigValue(PROP_REQUIRE_AUTH, ENV_REQUIRE_AUTH);
		if (explicit != null && !explicit.isBlank()) {
			return Boolean.parseBoolean(explicit.trim());
		}
		return currentRunMode().isProduction();
	}

	/**
	 * 读取HTTP运维鉴权Token
	 *
	 * @return 配置的Token字符串，未配置时返回null
	 */
	public static String getOpsToken() {
		String val = readConfigValue(PROP_OPS_TOKEN, ENV_OPS_TOKEN);
		if (val == null || val.isBlank()) {
			val = readConfigValue(PROP_TOKEN, ENV_TOKEN);
		}
		return val != null && !val.isBlank() ? val.trim() : null;
	}

	/**
	 * 解析HTTP GM网关监听端口，若配置为17011或非法端口范围则抛出异常以硬阻断冲突 (EX-07)
	 *
	 * @return 监听端口号（允许0供测试动态分配）
	 */
	public static int resolveHttpPort() {
		String val = readConfigValue(PROP_HTTP_PORT, ENV_HTTP_PORT);
		int port = DEFAULT_HTTP_PORT;
		if (val != null && !val.isBlank()) {
			try {
				port = Integer.parseInt(val.trim());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("HTTP GM控制台端口配置非法: " + val, e);
			}
		}
		if (port == RESERVED_IDENTITY_PORT) {
			throw new IllegalArgumentException("HTTP GM控制台端口不能为 17011 (已被 RuntimeIdentity 预留)");
		}
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("HTTP GM控制台端口超出有效范围 [0, 65535]: " + port);
		}
		return port;
	}

	/**
	 * 解析HTTP GM网关监听主机地址，生产模式下强制收敛绑定回环地址 127.0.0.1 (EX-08)
	 *
	 * @param runMode 当前运行模式
	 * @return 监听主机IP
	 */
	public static String resolveHttpHost(RunMode runMode) {
		RunMode effectiveMode = runMode != null ? runMode : currentRunMode();
		String configured = readConfigValue(PROP_HTTP_HOST, ENV_HTTP_HOST);
		if (effectiveMode.isProduction()) {
			if (configured == null || configured.isBlank()
					|| "0.0.0.0".equals(configured.trim())
					|| (!"127.0.0.1".equals(configured.trim()) && !"localhost".equalsIgnoreCase(configured.trim()))) {
				log.warn("生产模式下GM HTTP控制台强制收敛绑定回环地址 127.0.0.1 (原配置={})", configured);
				return "127.0.0.1";
			}
			return configured.trim();
		}
		return (configured != null && !configured.isBlank()) ? configured.trim() : "0.0.0.0";
	}

	/**
	 * 判断指定运行模式下HTTP GM接口是否强制要求鉴权
	 *
	 * @param runMode 当前运行模式
	 * @return true表示需要Token鉴权
	 */
	public static boolean isRequireAuth(RunMode runMode) {
		RunMode effectiveMode = runMode != null ? runMode : currentRunMode();
		if (effectiveMode.isProduction()) {
			return true;
		}
		String explicit = readConfigValue(PROP_REQUIRE_AUTH, ENV_REQUIRE_AUTH);
		if (explicit != null && !explicit.isBlank()) {
			return Boolean.parseBoolean(explicit.trim());
		}
		return false;
	}

	/**
	 * 常量时间校验请求携带的Token并返回鉴权主体与 RBAC 角色（优先匹配 rbacTokens，兼容旧 opsToken 默认 ADMIN）
	 *
	 * @param providedToken 请求端传入的Token
	 * @param remoteIp 来源IP
	 * @return 鉴权成功返回主体信息，失败返回 null
	 */
	public static GmAuthPrincipal authenticateToken(String providedToken, String remoteIp) {
		if (providedToken == null || providedToken.isBlank()) {
			return null;
		}
		byte[] providedBytes = providedToken.trim().getBytes(StandardCharsets.UTF_8);

		// 1. 优先匹配多账号 RBAC Token（格式：operator:ROLE:token,operator2:ROLE2:token2）
		String rbacSpec = readConfigValue(PROP_RBAC_TOKENS, ENV_RBAC_TOKENS);
		if (rbacSpec != null && !rbacSpec.isBlank()) {
			String[] entries = rbacSpec.split("[,;]");
			for (String rawEntry : entries) {
				if (rawEntry == null || rawEntry.isBlank()) {
					continue;
				}
				String[] parts = rawEntry.trim().split(":", 3);
				if (parts.length != 3) {
					log.warn("忽略格式非法的 rbacTokens 配置条目（格式应为 operator:ROLE:token）");
					continue;
				}
				String operator = parts[0].trim();
				GmRole role = GmRole.parseOrNull(parts[1]);
				String entryToken = parts[2].trim();
				if (operator.isEmpty() || role == null || entryToken.isEmpty()) {
					log.warn("忽略无效的 rbacTokens 条目: operator={}, rawRole={}", operator, parts[1]);
					continue;
				}
				if (MessageDigest.isEqual(entryToken.getBytes(StandardCharsets.UTF_8), providedBytes)) {
					return new GmAuthPrincipal(operator, role, remoteIp, true);
				}
			}
		}

		// 2. 兼容既有单一运维 opsToken（授予 ADMIN 角色，仍受 RunMode 约束）
		String serverToken = getOpsToken();
		if (serverToken != null && !serverToken.isBlank()) {
			if (MessageDigest.isEqual(serverToken.getBytes(StandardCharsets.UTF_8), providedBytes)) {
				return new GmAuthPrincipal("ops_token", GmRole.ADMIN, remoteIp, true);
			}
		}
		if ((rbacSpec == null || rbacSpec.isBlank()) && (serverToken == null || serverToken.isBlank())) {
			log.warn("服务端未配置GM HTTP鉴权Token (gm.http.opsToken / gm.http.rbacTokens), 拒绝访问");
		}
		return null;
	}

	/**
	 * 判断指定 RBAC 角色是否有权执行目标安全等级与动作 ID 的 GM 指令
	 *
	 * @param role 角色
	 * @param tier 动作安全等级
	 * @param actionId 动作规范 ID
	 * @return true表示允许
	 */
	public static boolean isRoleAllowedForAction(GmRole role, GmSecurityTier tier, String actionId) {
		return isRoleAllowedForAction(role, tier, actionId, false);
	}

	/**
	 * 判断指定 RBAC 角色是否有权执行目标安全等级、动作 ID 及显式只读标记的 GM 指令
	 *
	 * @param role 角色
	 * @param tier 动作安全等级
	 * @param actionId 动作规范 ID
	 * @param readOnly `@GmAction(readOnly = true)` 显式只读标记
	 * @return true表示允许
	 */
	public static boolean isRoleAllowedForAction(GmRole role, GmSecurityTier tier, String actionId, boolean readOnly) {
		if (role == null || tier == null) {
			return false;
		}
		if (role == GmRole.ADMIN) {
			return true;
		}
		if (role == GmRole.REPAIR) {
			return tier == GmSecurityTier.OPS_SAFE || tier == GmSecurityTier.OPS_REPAIR;
		}
		if (role == GmRole.OPERATOR) {
			return tier == GmSecurityTier.OPS_SAFE;
		}
		// READONLY：仅放行显式 readOnly=true 或明确只读白名单/后缀的 OPS_SAFE 动作
		if (tier != GmSecurityTier.OPS_SAFE) {
			return false;
		}
		if (readOnly) {
			return true;
		}
		String norm = actionId != null ? actionId.trim().toLowerCase(Locale.ROOT) : "";
		if (READONLY_WHITELIST_ACTIONS.contains(norm)) {
			return true;
		}
		return norm.endsWith(".list")
				|| norm.endsWith(".get")
				|| norm.endsWith(".status")
				|| norm.endsWith(".tables")
				|| norm.endsWith(".inspect")
				|| norm.endsWith(".schema")
				|| norm.endsWith(".help");
	}

	/**
	 * 常量时间校验请求携带的Token是否与配置的服务端Token一致 (DR-06)
	 *
	 * @param providedToken 请求端传入的Token
	 * @return true表示鉴权通过
	 */
	public static boolean validateToken(String providedToken) {
		return authenticateToken(providedToken, "") != null;
	}

	/**
	 * 从HTTP请求头提取Token (支持 X-GM-Token 与 Authorization: Bearer <token>)
	 *
	 * @param authHeader Authorization请求头
	 * @param gmTokenHeader X-GM-Token请求头
	 * @return 提取到的Token或null
	 */
	public static String extractToken(String authHeader, String gmTokenHeader) {
		if (gmTokenHeader != null && !gmTokenHeader.isBlank()) {
			return gmTokenHeader.trim();
		}
		if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return authHeader.substring(7).trim();
		}
		return null;
	}

	private static String readConfigValue(String propKey, String envKey) {
		String sys = System.getProperty(propKey);
		if (sys != null) {
			return sys;
		}
		String env = System.getenv(envKey);
		if (env != null) {
			return env;
		}
		if (!isApolloConfigured()) {
			return null;
		}
		try {
			return ConfigService.getAppConfig().getProperty(propKey, null);
		} catch (Throwable e) {
			log.warn("读取Apollo GM安全配置失败, key={}", propKey, e);
			return null;
		}
	}

	private static boolean isApolloConfigured() {
		return hasText(System.getProperty("apollo.meta"))
				|| hasText(System.getProperty("apollo.configService"))
				|| hasText(System.getProperty("env"))
				|| hasText(System.getenv("APOLLO_META"));
	}

	private static boolean hasText(String s) {
		return s != null && !s.isBlank();
	}
}