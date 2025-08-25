package cn.game.games.net.cross.activity;

import cn.game.games.net.game.module.activity.GlobalActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;

public class CrossGlobalActivityManager extends GlobalActivityManager {
	
	@Override
	protected boolean canOpen(ActivityConfig config) {
		return super.canOpen(config) && config.isCross;
	}
	
}