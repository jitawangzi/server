package cn.game.games.net.game.module.activity;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import cn.game.core.base.ServerContext;
import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.task.SchedulerService;
import cn.game.core.zookeeper.server.ValidServerService;
import cn.game.games.cache.entity.Activity;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.util.DateUtil;

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
			if (StringUtils.isEmpty(serverId)) {
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
		if (activity instanceof MultiPlayerActivityBase) {
			MultiPlayerActivityBase multiActivity = (MultiPlayerActivityBase) activity;
		}
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

	public void insert(ActivityBase activity) {
		Activity insert = new Activity();
		insert.setId(activity.getId());
		Object owner = getOwner();
		if (owner != null && owner instanceof Player) {
			insert.setPlayerId(((Player) owner).getPlayerId());
		} else {
			insert.setPlayerId(0L);
		}
		insert.setStat((byte) 0);
		insert.setParams(activity.toSaveString());
		DAO.insert(insert);
	}
}