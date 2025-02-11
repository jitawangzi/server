package cn.game.games.net.cross.activity;

import cn.game.games.net.common.module.activity.ActivityService;
import cn.game.games.net.game.module.activity.GlobalActivityManager;

public class CrossActivityService extends ActivityService {

	public GlobalActivityManager defaultGlobalActivityManager() {
		return new CrossGlobalActivityManager();
	}
}
