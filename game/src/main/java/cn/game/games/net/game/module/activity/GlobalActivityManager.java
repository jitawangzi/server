package cn.game.games.net.game.module.activity;

import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.games.net.common.module.activity.ActivityFactory;
import cn.game.protocol.generated.config.ActivityConfig;

public class GlobalActivityManager extends AbstractActivityManager {
	@Override
	protected Object getOwner() {
		return null; // 全局活动没有特定所有者
	}

	@Override
	protected boolean canOpen(ActivityConfig config) {
		if (config.openType == 0) {
			return isInOpenTime(config.ID);
		}
		return config.isMultiplayer && !config.disable;
	}

	@Override
	protected ActivityBase createActivity(ActivityConfig config) {
		return ActivityFactory.createMultiPlayerActivity(config.type);
	}

	@Override
	protected boolean shouldExpire(ActivityBase activity) {
		if (activity instanceof MultiPlayerActivityBase) {
			MultiPlayerActivityBase multiActivity = (MultiPlayerActivityBase) activity;
			return multiActivity.shouldExpire(0);
		}
		return false;
	}

	@Override
	protected void afterActivityOpen(ActivityBase activity) {
		if (activity instanceof MultiPlayerActivityBase) {
			MultiPlayerActivityBase multiActivity = (MultiPlayerActivityBase) activity;
		}
	}

	@Override
	protected void afterActivityDestroy(ActivityBase activity) {
		if (activity instanceof MultiPlayerActivityBase) {
			MultiPlayerActivityBase multiActivity = (MultiPlayerActivityBase) activity;
		}
	}

	@Override
	public void runDestroyTask(int id, long remaining) {
		// TODO
	}

	@Override
	protected boolean shouldRefresh(ActivityConfig config) {
		// TODO Auto-generated method stub
		return false;
	}
}