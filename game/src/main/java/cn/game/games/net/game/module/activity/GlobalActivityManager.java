package cn.game.games.net.game.module.activity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cn.game.core.id.IdUtil;
import cn.game.core.task.SchedulerService;
import cn.game.games.cache.entity.GameActivity;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

public class GlobalActivityManager extends AbstractActivityManager {

	private final Map<Integer, ScheduledFuture<?>> endTasks = new ConcurrentHashMap<>();
	private static final java.util.Set<Integer> UNREGISTERED_SERVER_WARN_TYPES = java.util.concurrent.ConcurrentHashMap.newKeySet();
	private final Map<Integer, ScheduledFuture<?>> destroyTasks = new ConcurrentHashMap<>();
	
	@Override
	protected Object getOwner() {
		return null; // 全局活动没有特定所有者
	}

	@Override
	public boolean canOpen(ActivityConfig config) {
		if (!config.isMultiplayer) {
			return false;
		}
		if (!ClassManager.getInstance().hasServerActivityClass(config.type)) {
			if (UNREGISTERED_SERVER_WARN_TYPES.add(config.type)) {
				log.warn("跳过未注册全局活动实现类的配置: id={}, type={}", config.ID, config.type);
			}
			return false;
		}
		return super.canOpen(config); 
	}

	@Override
	protected ActivityBase createActivity(ActivityConfig config) {
		return ActivityFactory.createServerActivityBase(config.type);
	}
	
	@Override
	protected void afterActivityDestroy(ActivityBase activity) {
		delete(activity); 
	}

	@Override
	public void end(int id, boolean notify) {
		ScheduledFuture<?> future = endTasks.remove(id);
		if (future != null) {
			future.cancel(false);
		}
		super.end(id, notify);
	}

	@Override
	public void destroy(int id, boolean notify) {
		ScheduledFuture<?> future = destroyTasks.remove(id);
		if (future != null) {
			future.cancel(false);
		}
		super.destroy(id, notify);
	}

	@Override
	public void runDelayDestroyTask(int id, long remaining) {
		ScheduledFuture<?> old = destroyTasks.remove(id);
		if (old != null) {
			old.cancel(false);
		}
		if (remaining > 0) {
			ScheduledFuture<?> future = SchedulerService.getInstance().scheduleTask(() -> {
				destroyTasks.remove(id);
				destroy(id, true);
			}, remaining, TimeUnit.MILLISECONDS);
			destroyTasks.put(id, future);
		}
	}

	@Override
	public void runDelayEndTask(int id, long remaining) {
		ScheduledFuture<?> old = endTasks.remove(id);
		if (old != null) {
			old.cancel(false);
		}
		if (remaining > 0) {
			ScheduledFuture<?> future = SchedulerService.getInstance().scheduleTask(() -> {
				endTasks.remove(id);
				end(id, true);
			}, remaining, TimeUnit.MILLISECONDS);
			endTasks.put(id, future);
		}
	}

	@Override
	protected boolean shouldRefresh(ActivityConfig config) {
		return false;
	}

	@Override
	protected void afterActivityOpen(ActivityBase activity) {
		insert(activity);
	}
	public Future<@Nullable Object> insert(ActivityBase activity) {
		GameActivity insert = new GameActivity();
		long uid = activity.getUid() > 0 ? activity.getUid() : IdUtil.getId();
		insert.setId(uid);
		if (activity.getUid() == 0) {
			activity.setUid(uid);
		}
		insert.setConfigId(activity.getId());
		insert.setState((byte) activity.getState());
		insert.setServerId(serverId);
		insert.setCreateTime(cn.game.util.DateUtil.currentTimeMillis());
		insert.setParams(activity.toSaveString());
		return insert.insert() ; 
	}
	public Future<@Nullable Object> delete(ActivityBase activity) {
		if (activity == null || activity.getUid() <= 0) {
			return Future.succeededFuture();
		}
		GameActivity delete = new GameActivity();
		delete.setId(activity.getUid());
		return doDelete(delete); 
	}

	protected Future<@Nullable Object> doDelete(GameActivity delete) {
		return delete.delete();
	}

}