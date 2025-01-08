package cn.game.games.net.game.module.activity;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.games.net.common.module.activity.ActivityFactory;
import cn.game.protocol.generated.config.ActivityConfig;

public class GlobalActivityManager extends AbstractActivityManager {
	private static GlobalActivityManager INSTANCE;
	private Map<String, MultiPlayerActivityBase> activityInstances = new ConcurrentHashMap<>();

	@Override
	protected Object getOwner() {
		return null; // 全局活动没有特定所有者
	}

	@Override
	protected boolean canOpen(ActivityConfig config) {
		return config.isMultiplayer && !config.disable;
	}

	@Override
	protected ActivityBase createActivity(ActivityConfig config) {
		return ActivityFactory.createMultiPlayerActivity(config.type);
	}

	@Override
	protected boolean shouldExpire(ActivityBase activity, ActivityConfig config, long now, Collection<Integer> showList) {
		if (activity instanceof MultiPlayerActivityBase) {
			MultiPlayerActivityBase multiActivity = (MultiPlayerActivityBase) activity;
			return multiActivity.shouldExpire(now);
		}
		return false;
	}

	@Override
	protected void afterActivityOpen(ActivityBase activity) {
		if (activity instanceof MultiPlayerActivityBase) {
			MultiPlayerActivityBase multiActivity = (MultiPlayerActivityBase) activity;
			activityInstances.put(multiActivity.getActivityInstanceId(), multiActivity);
		}
	}

	@Override
	protected void afterActivityDestroy(ActivityBase activity) {
		if (activity instanceof MultiPlayerActivityBase) {
			MultiPlayerActivityBase multiActivity = (MultiPlayerActivityBase) activity;
			activityInstances.remove(multiActivity.getActivityInstanceId());
		}
	}

	@Override
	protected boolean shouldRefresh(ActivityConfig config) {
		// TODO Auto-generated method stub
		return false;
	}
}