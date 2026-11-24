package cn.game.games.net.common.module.activity;

import cn.game.protocol.generated.config.ActivityConfig;

/**
 * 活动作用域解析器
 * 
 * @author shuyongqiang
 */
public interface ActivityScopeResolver {

	String GLOBAL_SCOPE_KEY = "all";

	/**
	 * 根据活动配置和服务器ID解析作用域键
	 * 
	 * @param config 活动配置
	 * @param serverId 服务器ID
	 * @return 作用域键（如 "all" 或 serverId）
	 */
	String resolveScopeKey(ActivityConfig config, String serverId);
}
