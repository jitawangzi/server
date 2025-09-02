package cn.game.games.net.game.module.activity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class ActivityModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay, EventTypeEnum.NewWeek,
			EventTypeEnum.NewMonth, EventTypeEnum.LevelUp };
	/** 已经开始的活动，只是展示的不在这里。  */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Deprecated
	private Map<Integer, ActivityBase> activities = new HashMap<Integer, ActivityBase>();

	/** 开启过的一次性的活动 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Deprecated
	private Set<Integer> disposableIds = new HashSet<>();

	/** 活动数据 */
	private PlayerActivityManager playerActivityManager;

	public Map<Integer, ActivityInfo> getShowState() {
		return playerActivityManager.getShowState();
	}

	@Override
	protected void initAfter() {
		if (playerActivityManager == null) {
			playerActivityManager = new PlayerActivityManager();
			playerActivityManager.setServerId(player.getServerId());
		}
		if (playerActivityManager.getPlayer() == null) {
			playerActivityManager.setPlayer(player);
		}
		// 老数据兼容处理
		if (!activities.isEmpty()) {
			playerActivityManager.setActivities(activities);
		}
		if (!disposableIds.isEmpty()) {
            playerActivityManager.setDisposableIds(disposableIds);
		}
	};

	private void initNewActivity() {
		List<ActivityConfig> openTypeList = ActivityManager.instance().getOpenTypeList(ActivityHelper.OPENTYPE_PLAYER_CREATE);
		if (openTypeList != null) {
			for (ActivityConfig activityConfig : openTypeList) {
				open(activityConfig.ID, false);
			}
		}
	}

	@Override
	public void initFromDbAfter() {
		// 这里注意一个活动，多开启时间的
		checkExpired(); 
//		if (disposableIds.isEmpty()) {// 兼容老数据
//			for (ActivityBase activityBase : activities.values()) {
//				ActivityConfig activityConfig = ActivityManager.instance().getNullable(activityBase.getId());
//				if (activityConfig == null) {
//					continue;
//				}
//				if (activityConfig.resetType == 0) {
//					disposableIds.add(activityBase.getId());
//				}
//			}
//		}
//		checkExpired();
	};


	@Override
	public void onLogin() {
		Iterator<ActivityBase> iterator = playerActivityManager.list().iterator(); 
		while (iterator.hasNext()) {
			ActivityBase activityBase = (ActivityBase) iterator.next();
			activityBase.init(activityBase.getId(), player, false);
		}
		// 可能符合开启条件的新任务。
		playerActivityManager.checkAndOpenActivitys(player);
		playerActivityManager.endTimeTask();
		playerActivityManager.destroyTimeTask();
	}

	/** 
	 * 关闭过期的活动
	 */
	private void checkExpired() {
		playerActivityManager.checkExpired();
//		playerActivityManager.endTimeTask();
	}
	/**
	 * 关闭活动，依然保留活动数据
	 * @param id
	 */
	public void end(int id) {
		playerActivityManager.end(id,true);
	}
	/** 
	 * 彻底销毁活动，删除数据， 不再展示。 
	 * @param id
	 */
	public void destroy(int id) {
		playerActivityManager.destroy(id, true);
	}

	public void destroy(int id, boolean notify) {
		playerActivityManager.destroy(id, notify);
	}
	/** 
	 * 重置某个活动
	 * @param id
	 */
	public void reset(int id) {
		playerActivityManager.reset(id);
	}

	public void open(int id, boolean notify) {
		playerActivityManager.open(id, player, notify);
	}

	@SuppressWarnings("unchecked")
	public <T extends ActivityBase> T get(int id) {
		return (T) playerActivityManager.get(id);
	}

	/** 
	 * 重置活动
	 */
	public void refreshByType(int resetType) {
		Collection<ActivityConfig> list = ActivityManager.instance().list();
		for (ActivityConfig activityConfig : list) {
			if (activityConfig.resetType == resetType) {
				reset(activityConfig.ID);
			}
		}
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE: {
			initNewActivity();
			break;
		}
		case NewDay: {
//			checkExpired();
			refreshByType(1);
			playerActivityManager.newDay();
			checkResetCycleActivity();
			break;
		}
		case NewWeek: {
			refreshByType(2);
			break;
		}
		case NewMonth: {
			refreshByType(3);
			break;
		}
		case LevelUp: {
			int type = event.getIntParameter(0);
			int level = event.getIntParameter(1);
			if (type == Asset.playerExp.ID) {
				List<ActivityConfig> openTypeList = ActivityManager.instance().getOpenTypeList(ActivityHelper.OPENTYPE_PLAYER_LEVEL);
				for (ActivityConfig activityConfig : openTypeList) {
					if (activityConfig.openParam == level) {
						open(activityConfig.ID, true);
					}
				}
			}
			break;
		}
		}
	}
	//检查 并重置 类型 4 周期的活动
	private void checkResetCycleActivity() {
		List<ActivityConfig> cycleList = ActivityManager.instance().list().stream().filter(activityConfig -> activityConfig.resetType == 4).collect(Collectors.toList());
		cycleList.forEach(activityConfig -> {
			playerActivityManager.open(activityConfig.ID, player, true);
		});
	}

//	@Override
//	public Class<?>[] defaultDbMapperClass() {
//		return new Class[] { ActivityMapper.class };
//	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
	}

	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_LOW;
	}

}
