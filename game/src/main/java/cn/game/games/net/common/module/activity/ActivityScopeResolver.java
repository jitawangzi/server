package cn.game.games.net.common.module.activity;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
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

	/**
	 * 解析指定活动当前应开启的全部去重作用域集合（用于 Cross Leader 多战区巡检调度）
	 *
	 * @param config  活动配置
	 * @param servers 当前有效逻辑区服视图集合
	 * @return 去重后的有序 scopeKey 集合
	 */
	default Set<String> resolveActiveScopeKeys(ActivityConfig config, Collection<VirtualServerView> servers) {
		return Collections.singleton(resolveScopeKey(config, null));
	}
}
