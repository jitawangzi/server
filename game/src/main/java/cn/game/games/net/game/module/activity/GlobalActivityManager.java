package cn.game.games.net.game.module.activity;

import java.util.concurrent.TimeUnit;

import cn.game.core.task.SchedulerService;
import cn.game.core.util.IdUtil;
import cn.game.games.cache.entity.GameActivity;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

public class GlobalActivityManager extends AbstractActivityManager {
	
	@Override
	protected Object getOwner() {
		return null; // 全局活动没有特定所有者
	}

	@Override
	protected boolean canOpen(ActivityConfig config) {
		if (!config.isMultiplayer) {
			return false;
		}
		return super.canOpen(config); 
	}
	
	@Override
	protected void afterActivityDestroy(ActivityBase activity) {
		delete(activity); 
	}

	@Override
	public void runDestroyTask(int id, long remaining) {
		SchedulerService.getInstance().scheduleTask(() -> {
			destroy(id, true);
		}, remaining, TimeUnit.MILLISECONDS);
	}
	@Override
	public void runEndTask(int id, long remaining) {
		SchedulerService.getInstance().scheduleTask(() -> {
			shutdown(id);
		}, remaining, TimeUnit.MILLISECONDS);
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
		insert.setId(IdUtil.getId());
		if (activity.uid == 0) {
			activity.uid = insert.getId();
		}
		insert.setConfigId(activity.getId());
		insert.setState((byte) activity.getState());
		insert.setServerId(serverId);
		insert.setCreateTime(System.currentTimeMillis());
		insert.setParams(activity.toSaveString());
		return insert.insert() ; 
	}
	public Future<@Nullable Object> delete(ActivityBase activity) {
		GameActivity delete = new GameActivity();
		delete.setId(activity.getId());
		return delete.delete() ; 
	}

}