package cn.game.games.net.common.module.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import cn.game.games.cache.entity.Activity;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.manager.ActivityStateManager;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public abstract class AbstractActivityManager {
	protected Map<Integer, ActivityBase> activities = new ConcurrentHashMap<>();

	public ActivityBase get(int id) {
		return activities.get(id);
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
				beforeActivityOpen(activityBase);
				activities.put(activityConfig.ID, activityBase);
				activityBase.init(activityConfig.ID, owner, true);
				if (notify) {
					activityBase.syncActivityInfo();
				}
				afterActivityOpen(activityBase);
			}
		}
	}

	public void shutdown(int id) {
		ActivityBase activityBase = activities.get(id);
		if (activityBase != null) {
			beforeActivityShutdown(activityBase);
			activityBase.shutDown();
			activityBase.syncActivityInfo();
			afterActivityShutdown(activityBase);
		}
	}

	public void destroy(int id, boolean notify) {
		ActivityBase activityBase = activities.remove(id);
		if (activityBase != null) {
			beforeActivityDestroy(activityBase);
			if (notify) {
				activityBase.setState(ActivityState.NONE_VALUE);
				activityBase.syncActivityInfo();
			}
			activityBase.destroy();
			afterActivityDestroy(activityBase);
		}
	}

	// 活动状态检查
	public void checkExpired() {
		List<Integer> deleteIds = new ArrayList<>();

		for (ActivityBase activityBase : activities.values()) {
			int cid = activityBase.getId();
			if (shouldExpire(activityBase)) {
				deleteIds.add(cid);
			}
		}
		for (Integer id : deleteIds) {
			destroy(id, false);
		}
	}

	/** 
	 * 活动结束任务
	 */
	public void endTimeTask() {
		long nowTime = System.currentTimeMillis();
		for (ActivityBase activityBase : activities.values()) {
			int cid = activityBase.getId();
			long endTime = activityBase.calcEndTime();
			if (endTime > 0) {
				long remaining = endTime - nowTime;
				if (remaining > 0) {
					runDestroyTask(cid, remaining);
				}
			}
		}
	}

	/** 
	 * 检查所有活动，能开的就开，主要为了处理新增加的一些活动
	 */
	public void checkAndOpenActivitys(Object owner) {
		Collection<ActivityConfig> list = ActivityManager.instance().list();
		for (ActivityConfig activityConfig : list) {
			if (canOpen(activityConfig)) {
				open(activityConfig.ID, owner, false);
			}
		}
	}

	protected abstract void runDestroyTask(int id, long remaining);

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

	// 抽象方法，由子类实现
	protected abstract Object getOwner(); // 返回活动所属对象(玩家或服务器)

	protected abstract boolean canOpen(ActivityConfig config);

	protected abstract ActivityBase createActivity(ActivityConfig config);

	/** 
	 * 判断一个已经开启的活动，是否过期了
	 * @param activity
	 * @return
	 */
	protected boolean shouldExpire(ActivityBase activity) {
		// 判断按活动时间开启的活动
		boolean inOpenTime = isInOpenTime(activity.getId());
		if (inOpenTime) {
			return false;
		}
		// 非时间开启的活动
		long endTime = activity.calcEndTime();
		if (endTime > 0) {
			return System.currentTimeMillis() >= endTime;
		}
		// 没有结束时间的
		return false;
	}

	/** 
	 * 判断当前的活动，如果是按时间开启的，是否在活动时间内
	 * @return  true，要么不是按活动时间开启的，要么是按活动时间开启的且在开启时间范围内
	 */
	protected boolean isInOpenTime(int id) {
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		if (activityConfig.openType != 0) {
			return true;
		}
		Collection<Integer> showList = ActivityStateManager.getInstance().getShowIds();
		return !showList.contains(id);
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
	public void initAdd(int id) {

		Activity activity = new Activity();
		ActivityBase activityBase = this.activities.get(id);
		activity.setId(id);
		Object owner = getOwner();
		if (owner != null && owner instanceof Player) {
			activity.setPlayerId(((Player) owner).getPlayerId());
		} else {
			activity.setPlayerId(0L);
		}
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
		Object owner = getOwner();
		if (owner != null && owner instanceof Player) {
			activity.setPlayerId(((Player) owner).getPlayerId());
		} else {
			activity.setPlayerId(0L);
		}
		activity.setStat((byte) 0);
		ActivityBase activityBase = this.activities.get(id);
		activity.setParams(activityBase.toSaveString());

		DAO.updateWithBLOBs(activity);
	}

	public void delete(int id) {
//		DAO.execute(ActivityMapper.class, MapperConstant.deleteByPrimaryKey, new Object[] { playerId, id });
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
			Object owner = getOwner();
			if (owner != null && owner instanceof Player) {
				activity.setPlayerId(((Player) owner).getPlayerId());
			} else {
				activity.setPlayerId(0L);
			}
			activity.setStat((byte) 0);
			activity.setParams(saveString);

			DAO.updateWithBLOBs(activity);
		}
	}

	public void setActivities(Map<Integer, ActivityBase> activities) {
		this.activities = activities;
	}

	protected abstract boolean shouldRefresh(ActivityConfig config);

	// 钩子方法，允许子类在活动生命周期的关键点进行干预
	protected void beforeActivityOpen(ActivityBase activity) {
	}

	protected void afterActivityOpen(ActivityBase activity) {
	}

	protected void beforeActivityShutdown(ActivityBase activity) {
	}

	protected void afterActivityShutdown(ActivityBase activity) {
	}

	protected void beforeActivityDestroy(ActivityBase activity) {
	}

	protected void afterActivityDestroy(ActivityBase activity) {
	}
}