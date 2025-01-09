package cn.game.games.net.game.module.activity;

import cn.game.games.cache.entity.Activity;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.games.util.DAO;
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