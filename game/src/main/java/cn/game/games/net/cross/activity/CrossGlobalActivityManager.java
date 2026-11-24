package cn.game.games.net.cross.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.task.SchedulerService;
import cn.game.games.net.cross.activity.service.CrossActivityService;
import cn.game.games.net.game.module.activity.ActivityHelper;
import cn.game.games.net.game.module.activity.GlobalActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;

/**
 * 跨服全局活动管理器
 * 约束：
 * 1. 禁止以 configId 为唯一主键，改以 activityUid 为唯一主运行容器主键（支持同节点托管不同 scopeKey 相同 configId）
 * 2. 辅助索引 scopeIndex: configId + "_" + scopeKey -> activityUid
 * 3. 定时器句柄按 activityUid 管理
 * 
 * @author shuyongqiang
 */
public class CrossGlobalActivityManager extends GlobalActivityManager {

	private static final Logger log = LoggerFactory.getLogger(CrossGlobalActivityManager.class);

	private static final CrossGlobalActivityManager INSTANCE = new CrossGlobalActivityManager();

	/** 主运行容器：activityUid -> CrossActivityBase */
	private final Map<Long, CrossActivityBase> activitiesByUid = new ConcurrentHashMap<>();

	/** 辅助索引：configId + "_" + scopeKey -> activityUid */
	private final Map<String, Long> scopeIndex = new ConcurrentHashMap<>();

	/** 定时器句柄映射（按 activityUid 管理） */
	private final Map<Long, ScheduledFuture<?>> endTasksByUid = new ConcurrentHashMap<>();
	private final Map<Long, ScheduledFuture<?>> destroyTasksByUid = new ConcurrentHashMap<>();

	public static CrossGlobalActivityManager getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean canOpen(ActivityConfig config) {
		if (config == null || config.disable || !config.isCross) {
			return false;
		}
		if (config.openType == 0) {
			return isInOpenTime(config.ID);
		}
		if (config.openType == ActivityHelper.OPENTYPE_SERVER_OPEN_DAY) {
			return true;
		}
		return canOpenExt(config);
	}

	public void addActivity(CrossActivityBase activity) {
		if (activity == null || activity.getUid() <= 0) {
			return;
		}
		activitiesByUid.put(activity.getUid(), activity);
		if (activity.getScopeKey() != null) {
			scopeIndex.put(activity.getId() + "_" + activity.getScopeKey(), activity.getUid());
		}
	}

	public CrossActivityBase getActivity(long uid) {
		return activitiesByUid.get(uid);
	}

	public CrossActivityBase getActivity(int configId, String scopeKey) {
		Long uid = scopeIndex.get(configId + "_" + scopeKey);
		return uid != null ? activitiesByUid.get(uid) : null;
	}

	public CrossActivityBase removeActivity(long uid) {
		CrossActivityBase removed = activitiesByUid.remove(uid);
		if (removed != null && removed.getScopeKey() != null) {
			scopeIndex.remove(removed.getId() + "_" + removed.getScopeKey(), uid);
		}
		cancelEndTask(uid);
		cancelDestroyTask(uid);
		return removed;
	}

	public Collection<CrossActivityBase> getAllActivities() {
		return Collections.unmodifiableCollection(activitiesByUid.values());
	}

	public List<Long> getAllActivityIds() {
		return new ArrayList<>(activitiesByUid.keySet());
	}

	public boolean containsActivity(long uid) {
		return activitiesByUid.containsKey(uid);
	}

	public void scheduleEndTask(CrossActivityBase activity, long remaining) {
		if (activity == null || activity.getUid() <= 0) {
			return;
		}
		long uid = activity.getUid();
		cancelEndTask(uid);
		if (remaining > 0) {
			ScheduledFuture<?> future = SchedulerService.getInstance().scheduleTask(() -> {
				endTasksByUid.remove(uid);
				CrossActivityService.getInstance().runInActivityMailbox(uid, () -> {
					CrossActivityBase act = activitiesByUid.get(uid);
					if (act != null) {
						act.end();
						CrossActivityService.getInstance().markDirty(uid);
					}
					return null;
				});
			}, remaining, TimeUnit.MILLISECONDS);
			endTasksByUid.put(uid, future);
		}
	}

	public void scheduleDestroyTask(CrossActivityBase activity, long remaining) {
		if (activity == null || activity.getUid() <= 0) {
			return;
		}
		long uid = activity.getUid();
		cancelDestroyTask(uid);
		if (remaining > 0) {
			ScheduledFuture<?> future = SchedulerService.getInstance().scheduleTask(() -> {
				destroyTasksByUid.remove(uid);
				CrossActivityService.getInstance().destroyActivity(uid);
			}, remaining, TimeUnit.MILLISECONDS);
			destroyTasksByUid.put(uid, future);
		}
	}

	public void cancelEndTask(long uid) {
		ScheduledFuture<?> future = endTasksByUid.remove(uid);
		if (future != null) {
			future.cancel(false);
		}
	}

	public void cancelDestroyTask(long uid) {
		ScheduledFuture<?> future = destroyTasksByUid.remove(uid);
		if (future != null) {
			future.cancel(false);
		}
	}

	@Override
	public void runDelayEndTask(int id, long remaining) {
		log.warn("runDelayEndTask by configId={} ignored in CrossGlobalActivityManager, use scheduleEndTask(activity, remaining)", id);
	}

	@Override
	public void runDelayDestroyTask(int id, long remaining) {
		log.warn("runDelayDestroyTask by configId={} ignored in CrossGlobalActivityManager, use scheduleDestroyTask(activity, remaining)", id);
	}

	public void clear() {
		for (ScheduledFuture<?> f : endTasksByUid.values()) {
			f.cancel(false);
		}
		for (ScheduledFuture<?> f : destroyTasksByUid.values()) {
			f.cancel(false);
		}
		endTasksByUid.clear();
		destroyTasksByUid.clear();
		activitiesByUid.clear();
		scopeIndex.clear();
	}
}
