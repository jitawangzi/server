package cn.game.games.net.game.module.activity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.games.net.common.module.activity.ActivityFactory;
import cn.game.games.net.common.module.activity.ActivityHelper;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.util.DateUtil;

public class PlayerActivityManager extends AbstractActivityManager {
	private Player player;
	private Set<Integer> disposableIds = new HashSet<>();

	@Override
	protected Object getOwner() {
		return player;
	}

	@Override
	protected boolean canOpen(ActivityConfig config) {
		if (config.disable) {
			return false;
		}
		if (config.resetType == 0 && disposableIds.contains(config.ID)) {
			return false;
		}
		if (config.isMultiplayer) {
			return false;
		}
		return canOpenNonTimeOpeningActivity(config);
	}

	private boolean canOpenNonTimeOpeningActivity(ActivityConfig activityConfig) {
		if (activityConfig.openType == 0) {
			return false;
		}
		if (activityConfig.openType == ActivityHelper.OPENTYPE_PLAYER_CREATE) {
			return true;
		}
		if (activityConfig.openType == ActivityHelper.OPENTYPE_PLAYER_CREATE_DAYS) {
			int days = DateUtil.diffDays(player.getData().getCreateDate());
			if (days >= activityConfig.openParam) {
				return true;
			}
		}
		if (activityConfig.openType == ActivityHelper.OPENTYPE_PLAYER_LEVEL) {
			int level = player.getLevel();
			if (level >= activityConfig.openParam) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected ActivityBase createActivity(ActivityConfig config) {
		return ActivityFactory.createActivity(config.type);
	}

	@Override
	protected boolean shouldExpire(ActivityBase activity, ActivityConfig config, long now, Collection<Integer> showList) {
		if (config.openType == 0) {
			return !showList.contains(activity.getId());
		} else {
			long endTime = activity.calcEndTime();
			if (endTime > 0) {
				return now >= endTime;
			}
		}
		return false;
	}

	@Override
	protected void afterActivityOpen(ActivityBase activity) {
		ActivityConfig config = ActivityManager.instance().get(activity.getId());
		if (config.resetType == 0) {
			disposableIds.add(config.ID);
		}
	}

	@Override
	protected boolean shouldRefresh(ActivityConfig config) {
		// TODO Auto-generated method stub
		return false;
	}
}