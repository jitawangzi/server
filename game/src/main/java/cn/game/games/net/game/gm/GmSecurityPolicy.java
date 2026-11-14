package cn.game.games.net.game.gm;

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
	public static final String PROP_REQUIRE_AUTH = "gm.http.requireAuth";
	public static final String PROP_OPS_TOKEN = "gm.http.opsToken";
	public static final String ENV_HTTP_ENABLED = "GM_HTTP_ENABLED";
	public static final String ENV_REQUIRE_AUTH = "GM_HTTP_REQUIRE_AUTH";
	public static final String ENV_OPS_TOKEN = "GM_HTTP_OPS_TOKEN";

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
		return val != null && !val.isBlank() ? val.trim() : null;
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