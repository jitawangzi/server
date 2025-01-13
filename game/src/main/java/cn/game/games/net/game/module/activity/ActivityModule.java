package cn.game.games.net.game.module.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.game.games.cache.entity.Activity;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.ActivityMapper;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

public class ActivityModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay, EventTypeEnum.NewWeek,
			EventTypeEnum.NewMonth, EventTypeEnum.LevelUp };
	/** 已经开始的活动，只是展示的不在这里。  */
	private Map<Integer, ActivityBase> activities = new HashMap<Integer, ActivityBase>();

	/** 开启过的一次性的活动 */
	private Set<Integer> disposableIds = new HashSet<>();

	@Deprecated
	public int getState(int id) {
		if (activities.containsKey(id)) {
			return activities.get(id).getState();
		}
		return ActivityStateManager.getInstance().getState(id);
	}

	public Map<Integer, ActivityInfo> getShowState() {

		Map<Integer, ActivityInfo> activityInfos = new HashMap<Integer, ActivityMsg.ActivityInfo>();
		for (Integer id : activities.keySet()) {
//			ActivityInfo activityInfo = ActivityInfo.newBuilder().setId(id).setStateValue(getState(id)).build();
			activityInfos.put(id, buildActivityInfo(id));
		}
		return activityInfos;
	}

	private void initNewActivity() {
		List<ActivityConfig> openTypeList = ActivityManager.instance().getOpenTypeList(ActivityHelper.OPENTYPE_PLAYER_CREATE);
		if (openTypeList != null) {
			for (ActivityConfig activityConfig : openTypeList) {
				open(activityConfig.ID, false);
			}
		}
	}

	/** 
	 * 非时间开启的一些新加的活动
	 */
	private void initNonTimeOpeningActivity() {
		Collection<ActivityConfig> list = ActivityManager.instance().list();
		for (ActivityConfig activityConfig : list) {
			if (canOpenNonTimeOpeningActivity(activityConfig)) {
				open(activityConfig.ID, false);
			}
		}
	}

	@Deprecated
	private void initNonTimeNewDayActivity() {

		List<ActivityConfig> openTypeList = ActivityManager.instance().getOpenTypeList(ActivityHelper.OPENTYPE_PLAYER_CREATE_DAYS);
		if (openTypeList != null){
			for (ActivityConfig activityConfig : openTypeList) {
				if (canOpenNonTimeOpeningActivity(activityConfig)) {
					open(activityConfig.ID, false);
				}
			}
		}
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

	/** 
	 * 同步某个活动的状态。 
	 * @param id
	 * @return 
	 */
	public ActivityInfo buildActivityInfo(int id) {
		// 已经开始过的
		ActivityBase activityBase = activities.get(id);
		if (activityBase != null) {
			return activityBase.buildActivityInfo();
		}
		// 尚未开始的
		return ActivityStateManager.getInstance().buildActivityInfo(id);
	}

	/** 
	 * 获取活动的结束时间
	 * @param id
	 * @return
	 */
	public long getEndTime(int id) {
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		List<Date> startTime = activityConfig.startTime;
		Date now = new Date();
		for (Date date : startTime) {
			if (now.before(date)) {
				return (int) DateUtil.howLong(TimeUnit.SECONDS, date, now);
			}
		}
		return 0;
	}

	@Override
	public void initFromDbAfter() {
		// 这里注意一个活动，多开启时间的
		for (ActivityBase activityBase : activities.values()) {
			activityBase.init(activityBase.getId(), player, false);
		}
		if (disposableIds.isEmpty()) {// 兼容老数据
			for (ActivityBase activityBase : activities.values()) {
				ActivityConfig activityConfig = ActivityManager.instance().getNullable(activityBase.getId());
				if (activityConfig == null) {
					continue;
				}
				if (activityConfig.resetType == 0) {
					disposableIds.add(activityBase.getId());
				}
			}
		}
		checkExpired();
	};

	@Override
	public void onLogin() {
		// 可能符合开启条件的新任务。
		Set<Integer> openList = ActivityStateManager.getInstance().getOpenIds();
		for (Integer id : openList) {
			if (!activities.containsKey(id)) {
				open(id, false);
			}
		}
		// 非时间开启的一些新加的活动
		initNonTimeOpeningActivity();
	}

	/** 
	 * 关闭过期的活动
	 */
	private void checkExpired() {
		long nowTime = System.currentTimeMillis();
		Collection<Integer> showList = ActivityStateManager.getInstance().getShowIds();
		// 这里注意一个活动，多开启时间的
		List<Integer> deleteIds = new ArrayList<>();
		for (ActivityBase activityBase : activities.values()) {
			int cid = activityBase.getId();
			ActivityConfig activityConfig = ActivityManager.instance().getNullable(cid);
			if (activityConfig == null) {
				deleteIds.add(cid);
				continue;
			}
			if (activityConfig.openType == 0) { // 按照时间开启的
				// 活动已经彻底关闭了
				if (!showList.contains(cid)) // 活动已经彻底关闭了
				{
					deleteIds.add(cid);
				}
			} else {
				long endTime = activityBase.calcEndTime();
				if (endTime > 0) {
					long remaining = endTime - nowTime;
					if (remaining > 0) {
						player.setTimerTask(remaining, r -> {
							destroy(cid);
						});
					} else {
						deleteIds.add(cid);
					}

				}
			}
		}
		for (Integer id : deleteIds) {
			destroy(id, false);
		}
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class[] { ActivityMapper.class };
	}


	public List<RewardInfo> receive(int id, int subId) {
		ActivityBase activityBase = this.activities.get(id);
		List<RewardInfo> rewards = new ArrayList<>();
		if (activityBase != null) {
			rewards = activityBase.receive(subId);
			rewards.addAll(rewards);
			if (rewards != null && rewards.size() > 0) {
//				update(id);
			}
		}
		return rewards;
	}

	public void delete(int id) {
//		DAO.execute(ActivityMapper.class, MapperConstant.deleteByPrimaryKey, new Object[] { playerId, id });
	}


	/**
	 * 关闭活动，依然保留活动数据
	 * @param id
	 */
	public void shutdown(int id) {
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
//		if (activityConfig.isMultiplayer && player != null) {
//			return ;
//		}
		ActivityBase activityBase = this.activities.get(id);
		if (activityBase != null) {
			activityBase.shutDown();
			activityBase.syncActivityInfo();
//			syncActivityState(Acti);
//			update(id);
		}
	}


	/** 
	 * 彻底销毁活动，删除数据， 不再展示。 
	 * @param id
	 */
	public void destroy(int id) {
		destroy(id, true);
	}

	public void destroy(int id, boolean notify) {
		ActivityBase activityBase = this.activities.remove(id);
		if (activityBase != null) {
			if (notify) {
				activityBase.setState(ActivityState.NONE_VALUE);
				activityBase.syncActivityInfo();
			}
			activityBase.destroy();
		}
	}

	/** 
	 * 重置某个活动
	 * @param id
	 */
	public void reset(int id) {
		destroy(id, false);
		open(id, false);
	}

	public void open(int id, boolean notify) {

		if (!activities.containsKey(id)) {
			// new activity
			ActivityConfig activityConfig = ActivityManager.instance().get(id);
			if (activityConfig.disable) {
				return;
			}
			if (activityConfig.resetType == 0 && disposableIds.contains(id)) {
				return;
			}
//			if (!activityConfig.isMultiplayer && player != null) {
//				return ; 
//			}
			ActivityBase activityBase = ActivityFactory.createActivity(activityConfig.type);
			if (activityBase != null) {
				this.activities.put(activityConfig.ID, activityBase);
				activityBase.init(activityConfig.ID, player, true);
				if (notify) {
					activityBase.syncActivityInfo();
				}
				if (activityConfig.resetType == 0) {
					disposableIds.add(id);
				}
//				initAdd(activityConfig.ID);
			}
		}
	}

	public ActivityBase get(int id) {

		ActivityConfig activityConfig = ActivityManager.instance().get(id);

		// 全局活动
		// if (activityConfig.getType() == ActivityTypeEnum.Login.getId()) {
		// return globalActivities.get(id);
		// }
		return activities.get(id);
	}


	public void newDay() {

		Iterator<Entry<Integer, ActivityBase>> iterator = activities.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<java.lang.Integer, ActivityBase> entry = iterator.next();

			ActivityBase activityBase = entry.getValue();
			if (activityBase != null) {
				boolean newDay = activityBase.newDay();
				if (newDay) {
//					update(entry.getKey());
				}
			}

		}
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

	public Collection<ActivityBase> list() {
		return this.activities.values();
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE: {
			initNewActivity();
			break;
		}
		case NewDay: {
			checkExpired();
			refreshByType(1);
			newDay();
			checkResetCycleActivity();
//			initNonTimeNewDayActivity();
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
			if (!activities.containsKey(activityConfig.ID)) {
				open(activityConfig.ID, true);
			}
		});
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	@Deprecated
	public void initAdd(int id) {

		Activity activity = new Activity();
		ActivityBase activityBase = this.activities.get(id);
		activity.setId(id);
		activity.setPlayerId(playerId);
		activity.setStat((byte) 0);
		if (activityBase != null) {
			activity.setParams(activityBase.toSaveString());
		}
		DAO.insert(activity);
	}

	@Deprecated
	public void update(int id) {

		Activity activity = new Activity();
		activity.setId(id);
		activity.setPlayerId(playerId);
		activity.setStat((byte) 0);
		ActivityBase activityBase = this.activities.get(id);
		activity.setParams(activityBase.toSaveString());

		DAO.updateWithBLOBs(activity);
	}

	@Deprecated
	public void updateAll() {

		for (ActivityBase activityBase : this.activities.values()) {

			String saveString = activityBase.toSaveString();
			if (saveString == null) { // 这个活动不需要保存到数据库
				continue;
			}
			Activity activity = new Activity();
			activity.setId(activityBase.getId());
			activity.setPlayerId(playerId);
			activity.setStat((byte) 0);
			activity.setParams(saveString);

			DAO.updateWithBLOBs(activity);
		}
	}

	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_LOW;
	}

}
