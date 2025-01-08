package cn.game.games.net.common.module.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;

public abstract class AbstractActivityManager {
	protected Map<Integer, ActivityBase> activities = new ConcurrentHashMap<>();

	// 活动生命周期管理
	public void open(int id, boolean notify) {
		if (!activities.containsKey(id)) {
			ActivityConfig activityConfig = ActivityManager.instance().get(id);
			if (!canOpen(activityConfig)) {
				return;
			}
			ActivityBase activityBase = createActivity(activityConfig);
			if (activityBase != null) {
				beforeActivityOpen(activityBase);
				activities.put(activityConfig.ID, activityBase);
				activityBase.init(activityConfig.ID, null, true);
				if (notify) {
					activityBase.syncActivityInfo();
				}
				afterActivityOpen(activityBase);
			}
		}
	}

	public void shutdown(int id) {
		ActivityBase activityBase = activities.get(id);
		if (activityBase != null) {
			beforeActivityShutdown(activityBase);
			activityBase.shutDown();
			activityBase.syncActivityInfo();
			afterActivityShutdown(activityBase);
		}
	}

	public void destroy(int id, boolean notify) {
		ActivityBase activityBase = activities.remove(id);
		if (activityBase != null) {
			beforeActivityDestroy(activityBase);
			if (notify) {
				activityBase.setState(ActivityState.NONE_VALUE);
				activityBase.syncActivityInfo();
			}
			activityBase.destroy();
			afterActivityDestroy(activityBase);
		}
	}

	// 活动状态检查
	protected void checkExpired() {
		long nowTime = System.currentTimeMillis();
		Collection<Integer> showList = ActivityStateManager.getInstance().getShowIds();
		List<Integer> deleteIds = new ArrayList<>();

		for (ActivityBase activityBase : activities.values()) {
			int cid = activityBase.getId();
			ActivityConfig activityConfig = ActivityManager.instance().get(cid);

			if (shouldExpire(activityBase, activityConfig, nowTime, showList)) {
				deleteIds.add(cid);
			}
		}

		for (Integer id : deleteIds) {
			destroy(id, false);
		}
	}

	// 定时刷新
	protected void refreshByType(int resetType) {
		Collection<ActivityConfig> list = ActivityManager.instance().list();
		for (ActivityConfig activityConfig : list) {
			if (activityConfig.resetType == resetType && shouldRefresh(activityConfig)) {
				reset(activityConfig.ID);
			}
		}
	}

	/** 
	 * 重置某个活动
	 * @param id
	 */
	public void reset(int id) {
		destroy(id, false);
		open(id, false);
	}

	// 抽象方法，由子类实现
	protected abstract Object getOwner(); // 返回活动所属对象(玩家或服务器)

	protected abstract boolean canOpen(ActivityConfig config);

	protected abstract ActivityBase createActivity(ActivityConfig config);

	protected abstract boolean shouldExpire(ActivityBase activity, ActivityConfig config, long now, Collection<Integer> showList);

	protected abstract boolean shouldRefresh(ActivityConfig config);

	// 钩子方法，允许子类在活动生命周期的关键点进行干预
	protected void beforeActivityOpen(ActivityBase activity) {
	}

	protected void afterActivityOpen(ActivityBase activity) {
	}

	protected void beforeActivityShutdown(ActivityBase activity) {
	}

	protected void afterActivityShutdown(ActivityBase activity) {
	}

	protected void beforeActivityDestroy(ActivityBase activity) {
	}

	protected void afterActivityDestroy(ActivityBase activity) {
	}
}