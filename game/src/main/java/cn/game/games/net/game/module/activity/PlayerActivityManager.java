package cn.game.games.net.game.module.activity;

import java.util.HashSet;
import java.util.Set;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.common.module.activity.AbstractActivityManager;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.util.DateUtil;

public class PlayerActivityManager extends AbstractActivityManager {
	private static final java.util.Set<Integer> UNREGISTERED_WARN_TYPES = java.util.concurrent.ConcurrentHashMap.newKeySet();
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
		if (!ClassManager.getInstance().hasPlayerActivityClass(config.type)) {
			if (UNREGISTERED_WARN_TYPES.add(config.type)) {
				log.warn("跳过未注册玩家活动实现类的配置: id={}, type={}", config.ID, config.type);
			}
			return false;
		}
		// 多周期跨服多人活动（如跨服世界Boss）每期销毁后老玩家需在新周期重开，豁免一次性禁用拦截
		if (config.resetType == 0 && !(config.isCross && config.isMultiplayer) && disposableIds.contains(config.ID)) {
			return false;
		}
		return super.canOpen(config);
	}

	@Override
	protected ActivityBase createActivity(ActivityConfig config) {
		return ActivityFactory.createPlayerActivityBase(config.type);
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
		if (activityConfig.openType == ActivityHelper.OPENTYPE_PLAYER_VIP_LEVEL) {
			return player.getVipLevel() >= activityConfig.openParam ; 
		}
		return false;
	}

	@Override
	protected void afterActivityOpen(ActivityBase activity) {
		ActivityConfig config = ActivityManager.instance().get(activity.getId());
		// 多周期跨服多人活动不写入 disposableIds，避免第 1 期销毁后永久锁死第 2 期重开
		if (config.resetType == 0 && !(config.isCross && config.isMultiplayer)) {
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
			end(id,true);
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

	/**
	 * 从一次性活动记录中移除指定活动 ID，便于 GM/测试重置并重开一次性活动
	 *
	 * @param activityId 活动配置 ID
	 * @return true表示原本存在并已移除
	 */
	public boolean clearDisposableIdForGm(int activityId) {
		return disposableIds != null && disposableIds.remove(activityId);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

}