package cn.game.games.net.game.module.activity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cn.game.core.task.TaskManager;
import cn.game.games.cache.entity.Activity;
import cn.game.games.cache.entity.ClimbingTower;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.ActivityMapper;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public class ActivityModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };
	private Map<Integer, ActivityBase> activities = new HashMap<Integer, ActivityBase>();


	public int initLoadData(List<Activity> list) {

//		Player player = PlayerManager.getInstance().getPlayer(playerId);
		Collection<Integer> showList = ActivityStateManager.getInstance().getShowIds();
		List<Integer> openList = ActivityStateManager.getInstance().getOpenIds();
		// 这里注意一个活动，多开启时间的
		if (list != null) {
			for (Activity activity : list) {
				ActivityConfig activityConfig = ActivityManager.instance().get(activity.getId());
				// 活动已经彻底关闭了
				if (!showList.contains(activity.getId())) // 活动已经彻底关闭了
				{
					delete(activity.getId());
				} else { // init from db
					ActivityBase activityBase = ActivityFactory.initActivityBase(activityConfig, activity.getParams(), player);
					if (activityBase != null) {
						this.activities.put(activityConfig.ID, activityBase);
					}
				}
			}
		}
		for (Integer integer : openList) {
			if (!activities.containsKey(integer)) {
				open(integer);
			}
		}
		//重载已开启过的爬塔活动数据
//		reloadClimbingTowerData();

		return 0;
	}

	public int getState(int id) {
		if (activities.containsKey(id)) {
			return ActivityState.START_VALUE;
		}
		return ActivityStateManager.getInstance().getState(id);
	}

	public Map<Integer, ActivityInfo> getShowState() {

		Map<Integer, ActivityInfo> activityInfos = new HashMap<Integer, ActivityMsg.ActivityInfo>();
		for (Integer id : activities.keySet()) {
			ActivityInfo activityInfo = ActivityInfo.newBuilder().setId(id).setStateValue(getState(id)).build();
			activityInfos.put(id, activityInfo);
		}
		return activityInfos;
	}

	protected void initFromDb(ListIterator<?> iterator) {

		List<Activity> list = iterator == null ? null : (List<Activity>) iterator.next();
//		Player player = PlayerManager.getInstance().getPlayer(playerId);

		Collection<Integer> showList = ActivityStateManager.getInstance().getShowIds();
		List<Integer> openList = ActivityStateManager.getInstance().getOpenIds();
		// 这里注意一个活动，多开启时间的
		if (list != null) {
			for (Activity activity : list) {
				ActivityConfig activityConfig = ActivityManager.instance().get(activity.getId());
				// 活动已经彻底关闭了
				if (!showList.contains(activity.getId())) // 活动已经彻底关闭了
				{
					delete(activity.getId());
				} else { // init from db
					ActivityBase activityBase = ActivityFactory.initActivityBase(activityConfig, activity.getParams(),
							player);
					if (activityBase != null) {
						this.activities.put(activityConfig.ID, activityBase);
					}
				}
			}
		}
		for (Integer integer : openList) {
			if (!activities.containsKey(integer)) {
				open(integer);
			}
		}
		// 重载已开启过的爬塔活动数据
//		reloadClimbingTowerData();
	}

	private void initNewActivity() {
		List<ActivityConfig> openTypeList = ActivityManager.instance().getOpenTypeList(1);
		if (openTypeList != null) {
			for (ActivityConfig activityConfig : openTypeList) {
				open(activityConfig.ID);
			}
		}
	}
	@Override
	public void initFromDbAfter() {

	};

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class[] { ActivityMapper.class };
	}

	public void reloadClimbingTowerData() {
		ActivityBase activityBase = get(GameConstants.TOWER_ACTIVITYID);
		if (activityBase == null) {
			return;
		}
		ClimbingTowerActivity cta = (ClimbingTowerActivity) activityBase;
		Map<Integer, Multimap<Integer, ClimbingTower>> groupInfo = cta.getGroupInfo();
		if (groupInfo.size() == 0) { //没有分组 重新加载分组数据
			Map<Long, ClimbingTower> playerClimbingTowerData = ActivityStateManager.getInstance().getPlayerClimbingTowerData();
			playerClimbingTowerData.values().forEach(data -> {
				Integer groupId = data.getGroupId();
				if (groupId != 0) {
					Integer level = data.getLevel();
					Multimap<Integer, ClimbingTower> integerListMap = groupInfo.get(level);
					if(integerListMap == null){
						integerListMap = ArrayListMultimap.create();
						groupInfo.put(level, integerListMap);
					}
					integerListMap.put(groupId, data);
				}
			});

			//活动开启了，设置玩家可以参加活动的状态
			int activityState = ActivityStateManager.getInstance().getState(GameConstants.TOWER_ACTIVITYID);
			if (activityState == ActivityMsg.ActivityState.START_VALUE) {
				if (!cta.isReady()) {//设置玩家是否可以参加活动
					LocalDateTime now = LocalDateTime.now();
					if (now.getHour() >= GameConstants.TOWER_STARHOUR) {
						cta.setReady(true);
					} else {
						LocalDateTime end = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), GameConstants.TOWER_STARHOUR, GameConstants.TOWER_STARMINITE, 0);

						Duration duration = Duration.between(now, end);
						long millis = duration.toMillis();//相差毫秒数
						TaskManager.getInstance().scheduleGeneral(new Runnable() {
							@Override
							public void run() {
								cta.setReady(true);
							}
						}, millis);
					}
				}
			}
		}

	}

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


	public void update(int id) {

		Activity activity = new Activity();
		activity.setId(id);
		activity.setPlayerId(playerId);
		activity.setStat((byte) 0);
		ActivityBase activityBase = this.activities.get(id);
		activity.setParams(activityBase.toSaveString());

		DAO.updateWithBLOBs(activity);
	}
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


	public List<RewardInfo> receive(int id, int subId) {
		ActivityBase activityBase = this.activities.get(id);
		List<RewardInfo> rewards = null;
		if (activityBase != null) {
			rewards = activityBase.receive(subId);
			if (rewards != null && rewards.size() > 0) {
//				update(id);
			}
		}
		return rewards;
	}

	public void delete(int id) {
//		DAO.execute(ActivityMapper.class, MapperConstant.deleteByPrimaryKey, new Object[] { playerId, id });
	}

	public void refresh() {

		initLoadData(null);
	}


	public void end(int id) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		if (activityConfig.isMultiplayer && player != null) {
			return ;
		}
		ActivityBase activityBase = this.activities.get(id);
		if (activityBase != null) {
			activityBase.shutDown();
//			update(id);
		}
	}


	public void destroy(int id) {
//		Player player = PlayerManager.getInstance().getPlayer(playerId);
//		ActivityConfig activityConfig = ActivityManager.instance().get(id);
//		if (!activityConfig.isMultiplayer && player != null) {
//			return ;
//		}

		ActivityBase activityBase = this.activities.remove(id);
		if (activityBase != null) {
			activityBase.destroy();
			delete(id);
		}
	}


	public void open(int id) {

		if (!activities.containsKey(id)) {
			// new activity
			ActivityConfig activityConfig = ActivityManager.instance().get(id);
//			if (!activityConfig.isMultiplayer && player != null) {
//				return ; 
//			}
			ActivityBase activityBase = ActivityFactory.createActivity(activityConfig.type);
			if (activityBase != null) {
				this.activities.put(activityConfig.ID, activityBase);
				activityBase.init(activityConfig.ID, player, true);
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

	@Override
	public void init() {

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
		}
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

}
