package cn.game.games.net.common.module.activity;

import cn.game.games.net.game.module.activity.ActivityHelper;
import cn.game.protocol.generated.config.ActivityConfig;

/**
 * 默认活动作用域解析器
 * 策略：
 * - openType == ActivityHelper.OPENTYPE_SERVER_OPEN_DAY (10) -> serverId (校验非空)
 * - openType == ActivityHelper.OPENTYPE_DATE (0) / 其他 -> "all"
 * 
 * @author shuyongqiang
 */
public class DefaultActivityScopeResolver implements ActivityScopeResolver {

	private static final DefaultActivityScopeResolver INSTANCE = new DefaultActivityScopeResolver();

	public static DefaultActivityScopeResolver getInstance() {
		return INSTANCE;
	}

	@Override
	public String resolveScopeKey(ActivityConfig config, String serverId) {
		if (config == null) {
			return GLOBAL_SCOPE_KEY;
		}
		if (config.openType == ActivityHelper.OPENTYPE_SERVER_OPEN_DAY) {
			if (serverId == null || serverId.trim().isEmpty()) {
				throw new IllegalArgumentException("serverId cannot be empty for OPENTYPE_SERVER_OPEN_DAY activity: " + config.ID);
			}
			return serverId.trim();
		}
		return GLOBAL_SCOPE_KEY;
	}
}
