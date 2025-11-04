package cn.game.games.net.common.module.activity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.zookeeper.server.ValidServerService;
import cn.game.games.cache.entity.GameActivity;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ActivityFactory;
import cn.game.games.net.game.module.activity.ActivityHelper;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.DateUtil;

public abstract class AbstractActivityManager {
	protected transient Logger log = LoggerFactory.getLogger(this.getClass());
	public static final String GLOBAL_SERVER_ID = "serverAll"; // 全局服ID

	/** 进行中的活动，同id只能有一个活动 */
	protected Map<Integer, ActivityBase> activities = new ConcurrentHashMap<>();
	/** 活动属于哪一个服 */
	protected String serverId = GLOBAL_SERVER_ID; 
	
	public <T extends ActivityBase> T get(int id) {
		return (T) activities.get(id);
	}

	public Collection<ActivityBase> list() {
		return activities.values();
	}
	// 活动生命周期管理
	public void open(int id, Object owner, boolean notify) {
		if (!activities.containsKey(id)) {
			ActivityConfig activityConfig = ActivityManager.instance().get(id);
			if (!canOpen(activityConfig)) {
				return;
			}
			ActivityBase activityBase = createActivity(activityConfig);
			if (activityBase != null) {
				activityBase.setServerId(serverId);
				ActivityBase old = activities.putIfAbsent(activityConfig.ID, activityBase); 
				if (old == null) {
					beforeActivityOpen(activityBase);
					activityBase.init(activityConfig.ID, owner, true);
					if (notify) {
						activityBase.syncActivityInfo();
					}
					afterActivityOpen(activityBase);
					
					checkExpired(id);
					// 
//					endTimeTask(activityBase);
//					destroyTimeTask(activityBase);
				}
			}
		}
	}
	public void initFromDb(ActivityConfig config, String saveString,Object owner) {
		ActivityBase activityBase = ActivityFactory.createActivityBase(config,saveString); 
		int id = activityBase.getId();
		ActivityBase oldValue = activities.putIfAbsent(id, activityBase);
		if (oldValue == null) {
			activityBase.init(id, owner, false);
			afterLoad();
		}
	}
	public void checkAndInitFromDb(ActivityConfig config, String saveString,Object owner) {
		ActivityBase activityBase = ActivityFactory.createActivityBase(config,saveString); 
		int expireState = expireState(activityBase); 
		if (expireState == 0) {
			int id = activityBase.getId();
			ActivityBase oldValue = activities.putIfAbsent(id, activityBase);
			if (oldValue == null) {
				activityBase.init(id, owner, false);
				afterLoad();
			}
		}else if (expireState == 1) {
			if (activityBase.getState() != ActivityState.CLOSE_VALUE) {
				activityBase.end();
			}
		}else if (expireState == 2) {
			if (activityBase.getState() != ActivityState.NONE_VALUE) {
				activityBase.destroy();;
			}
		}
	}

	public void destroy(int id, boolean notify) {
		ActivityBase activityBase = activities.remove(id);
		if (activityBase != null) {
			if (activityBase.getState() == ActivityState.NONE_VALUE) {
				return;
			}
			beforeActivityDestroy(activityBase);
			if (notify) {
				activityBase.setState(ActivityState.NONE_VALUE);
				activityBase.syncActivityInfo();
			}
			activityBase.destroy();
			afterActivityDestroy(activityBase);
		}
	}
	public void end(int id, boolean notify) {
		ActivityBase activityBase = activities.get(id);
		if (activityBase != null) {
			if (activityBase.getState() == ActivityState.CLOSE_VALUE) {
				return;
			}
			beforeActivityEnd(activityBase);
			if (notify) {
				activityBase.setState(ActivityState.CLOSE_VALUE);
				activityBase.syncActivityInfo();
			}
			activityBase.end();
			afterActivityEnd(activityBase);
		}
	}

	// 活动状态检查
	public void checkExpired() {
		List<Integer> endIds = new ArrayList<>();
		List<Integer> destoryIds = new ArrayList<>();

		for (ActivityBase activityBase : activities.values()) {
			int cid = activityBase.getId();
			int expireState = expireState(activityBase); 
			if (expireState == 1) {
				endIds.add(cid);
			}else if (expireState == 2) {
				destoryIds.add(cid);
			}
		}
		// 先结束，再销毁
		for (Integer id : endIds) {
			end(id, false);
		}
		for (Integer id : destoryIds) {
			destroy(id, false);
		}
	}
	public void checkExpired(int id) {
		ActivityBase activityBase = activities.get(id); 
		if (activityBase == null) {
			return ; 
		}
		int expireState = expireState(activityBase); 
		if (expireState == 1) {
			end(id, false);
		}else if (expireState == 2) {
			destroy(id, false);
		}
	}

	/** 
	 * 活动结束任务
	 */
	public void endTimeTask() {
//		long nowTime = System.currentTimeMillis();
		for (ActivityBase activityBase : activities.values()) {
//			int cid = activityBase.getId();
			endTimeTask(activityBase);
		}
	}

	private void endTimeTask(ActivityBase activityBase) {
		long nowTime = System.currentTimeMillis();
		int cid = activityBase.getId();
		long endTime = activityBase.getEndTime();
		if (endTime > 0) {
			long remaining = endTime - nowTime;
			runDelayEndTask(cid, remaining);
		}
	}
	public void destroyTimeTask() {
		for (ActivityBase activityBase : activities.values()) {
			destroyTimeTask(activityBase);
		}
	}
	private void destroyTimeTask(ActivityBase activityBase) {
		long nowTime = System.currentTimeMillis();
		int cid = activityBase.getId();
		long destroyTime = activityBase.getDestroyTime();
		if (destroyTime > 0) {
			long remaining = destroyTime - nowTime;
			runDelayDestroyTask(cid, remaining);
		}
	}

	/** 
	 * 检查所有活动，能开的就开，主要为了处理新增加的一些活动
	 */
	public void checkAndOpenActivitys(Object owner) {
		Collection<ActivityConfig> list = ActivityManager.instance().list();
		for (ActivityConfig activityConfig : list) {
			open(activityConfig.ID, owner, false);
		}
	}

	protected abstract void runDelayDestroyTask(int id, long remaining);
	protected abstract void runDelayEndTask(int id, long remaining);

	// 定时刷新
	protected void refreshByType(int resetType) {
		Collection<ActivityConfig> list = ActivityManager.instance().list();
		for (ActivityConfig activityConfig : list) {
			if (activityConfig.resetType == resetType && shouldRefresh(activityConfig)) {
				reset(activityConfig.ID);
			}
		}
	}

	/** 
	 * 重置某个活动
	 * @param id
	 */
	public void reset(int id) {
		destroy(id, false);
		open(id, getOwner(), false);
	}

	@Deprecated
	public int getState(int id) {
		if (activities.containsKey(id)) {
			return activities.get(id).getState();
		}
		return ActivityStateManager.getInstance().getState(id);
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

	@Deprecated
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

	protected ActivityBase createActivity(ActivityConfig config) {
		return ActivityFactory.createActivityBase(config.type);

	}

	/** 
	 * 判断一个已经开启的活动的状态
	 * @param activity
	 * @return  0 还在活动中，不过期；  1 活动已经结束 ；  2 活动已经销毁
	 */
	protected int expireState(ActivityBase activity) {
		// 判断按活动时间开启的活动
		ActivityConfig activityConfig = ActivityManager.instance().get(activity.getId());
//		if (activityConfig.openType == ActivityHelper.OPENTYPE_DATE) {
//			boolean ret =  !isInOpenTime(activity.getId());
//		}
//		if (activityConfig.openType == ActivityHelper.OPENTYPE_SERVER_OPEN_DAY) {
//			if (serverId.equals(GLOBAL_SERVER_ID) || StringUtils.isEmpty(serverId)) {
//				return false;
//			}
//			ValidServerService validGameService = ServerContext.getInstance().getValidGameService();
//			Map<String, VirtualServerView> validServers = validGameService.getValidServers();
//
//			VirtualServerView virtualServerView = validServers.get(serverId);
//			if (virtualServerView != null && virtualServerView.openTime != null
//					&& DateUtil.diffDays(virtualServerView.openTime.toLocalDate(), LocalDate.now()) + 1 >= activityConfig.openParam) {
//				return true;
//			}
//		}
		long now = System.currentTimeMillis(); 
		int state = 0 ; 
		long endTime = activity.getEndTime();
		long destroyTime = activity.getDestroyTime();
		if (endTime > 0) {
			if (now > endTime) {
				state = 1; // 活动已经结束
			}
		}
		if (destroyTime > 0) {
			if (now > destroyTime) {
				state = 2; // 活动应该销毁了
			}
		}
		// 没结束，或者没有结束时间
		return state;
	}

	/** 
	 * 判断当前的按时间开启的活动，是否在活动时间内
	 * view、start、end 状态的活动
	 * @param id
	 * @return
	 */
	protected boolean isInOpenTime(int id) {
		Collection<Integer> openList = ActivityStateManager.getInstance().getShowIds();
		return openList.contains(id);
	}
	public boolean canOpenExt(ActivityConfig activityConfig) {
		return false;
	}

	public Map<Integer, ActivityInfo> getShowState() {

		Map<Integer, ActivityInfo> activityInfos = new HashMap<Integer, ActivityMsg.ActivityInfo>();
		for (Integer id : activities.keySet()) {
			activityInfos.put(id, buildActivityInfo(id));
		}
		return activityInfos;
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
	@Deprecated
	public void updateAll() {

		for (ActivityBase activityBase : this.activities.values()) {

			String saveString = activityBase.toSaveString();
			if (saveString == null) { // 这个活动不需要保存到数据库
				continue;
			}
			GameActivity activity = new GameActivity();
			activity.setId(activityBase.getId());
			Object owner = getOwner();
			if (owner != null && owner instanceof Player) {
//				activity.setPlayerId(((Player) owner).getPlayerId());
			} else {
//				activity.setPlayerId(0L);
			}
			activity.setState((byte) activityBase.getState());
			activity.setParams(saveString);

			DAO.update(activity);
		}
	}

	public void setActivities(Map<Integer, ActivityBase> activities) {
		this.activities = activities;
	}

	// 抽象方法，由子类实现

	/** 
	 * 返回活动所属对象(玩家或服务器)
	 * @return
	 */
	protected abstract Object getOwner();

	/** 
	 * 检查某个活动是否可以开启
	 * 对于end状态的活动，也认为可以开启。在开启之后，执行end检查 
	 * @param config
	 * @return
	 */
	protected boolean canOpen(ActivityConfig config) {
		if (config.disable) {
			return false;
		}
		if (config.openType == 0) {
			return isInOpenTime(config.ID);
		}
		if (config.openType == ActivityHelper.OPENTYPE_SERVER_OPEN_DAY) {

			if (serverId.equals(GLOBAL_SERVER_ID) || StringUtils.isEmpty(serverId)) {
				return false;
			}
			Map<String, VirtualServerView> validServers = ServerHelper.getServerOpenMap();

			VirtualServerView virtualServerView = validServers.get(serverId);
			if (virtualServerView != null && virtualServerView.openTime != null) {
				int days =  DateUtil.diffDays(virtualServerView.openTime.toLocalDate(), LocalDate.now()) + 1; 
				return days >= config.openParam && days < config.openParam + config.destroyDuration;
			}
		}
		return canOpenExt(config);
	}

	protected abstract boolean shouldRefresh(ActivityConfig config);

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	// 钩子方法，允许子类在活动生命周期的关键点进行干预
	protected void afterLoad() {
		
	}
	protected void beforeActivityOpen(ActivityBase activity) {
	}

	protected void afterActivityOpen(ActivityBase activity) {
	}

	protected void beforeActivityEnd(ActivityBase activity) {
	}

	protected void afterActivityEnd(ActivityBase activity) {
	}

	protected void beforeActivityDestroy(ActivityBase activity) {
	}

	protected void afterActivityDestroy(ActivityBase activity) {
	}


}