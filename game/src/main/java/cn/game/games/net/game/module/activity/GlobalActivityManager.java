package cn.game.games.net.game.module.activity;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import cn.game.core.base.ServerContext;
import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.task.SchedulerService;
import cn.game.core.util.IdUtil;
import cn.game.core.zookeeper.server.ValidServerService;
import cn.game.games.cache.entity.GameActivity;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.util.DateUtil;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;

public class GlobalActivityManager extends AbstractActivityManager {
	
	@Override
	protected Object getOwner() {
		return null; // 全局活动没有特定所有者
	}

	@Override
	protected boolean canOpen(ActivityConfig config) {
		if (!config.isMultiplayer || config.disable) {
			return false;
		}
		if (config.openType == 0) {
			return isInOpenTime(config.ID);
		}
		if (config.openType == 10) {
			if (serverId.equals(GLOBAL_SERVER_ID) || StringUtils.isEmpty(serverId)) {
				return false;
			}
			ValidServerService validGameService = ServerContext.getInstance().getValidGameService();
			Map<String, VirtualServerView> validServers = validGameService.getValidServers();

			VirtualServerView virtualServerView = validServers.get(serverId);
			if (virtualServerView != null && virtualServerView.openTime != null
					&& DateUtil.diffDays(virtualServerView.openTime.toLocalDate(), LocalDate.now()) >= config.openParam) {
				return true;
			}
		}
		//
		return false;
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
	protected boolean shouldRefresh(ActivityConfig config) {
		// TODO Auto-generated method stub
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