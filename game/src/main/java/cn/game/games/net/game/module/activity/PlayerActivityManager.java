package cn.game.games.net.game.module.activity;

import java.util.HashSet;
import java.util.Set;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.util.DateUtil;

public class PlayerActivityManager extends AbstractActivityManager {
	private transient Player player;
	private Set<Integer> disposableIds = new HashSet<>();

	public PlayerActivityManager(Player player) {
		this.player = player;
	}
	public PlayerActivityManager() {
	}

	@Override
	protected Object getOwner() {
		return player;
	}

	@Override
	protected boolean canOpen(ActivityConfig config) {
		if (config.isMultiplayer) {
			return false;
		}
		if (config.resetType == 0 && disposableIds.contains(config.ID)) {
			return false;
		}
		return super.canOpen(config);
	}

	@Override
	public boolean canOpenExt(ActivityConfig activityConfig) {
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
	protected void afterActivityOpen(ActivityBase activity) {
		ActivityConfig config = ActivityManager.instance().get(activity.getId());
		if (config.resetType == 0) {
			disposableIds.add(config.ID);
		}
	}

	@Override
	public void runDelayDestroyTask(int id, long remaining) {
		player.setTimerTask(remaining, r -> {
			destroy(id, true);
		});
	}
	@Override
	public void runDelayEndTask(int id, long remaining) {
		player.setTimerTask(remaining, r -> {
			end(id);
		});
	}
	@Override
	protected boolean shouldRefresh(ActivityConfig config) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDisposableIds(Set<Integer> disposableIds) {
		this.disposableIds = disposableIds;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

}