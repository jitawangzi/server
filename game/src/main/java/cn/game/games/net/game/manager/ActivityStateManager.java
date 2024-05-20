package cn.game.games.net.game.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.task.TaskManager;
import cn.game.games.cache.entity.Activity;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.AbstractGameEventRegistration;
import cn.game.games.net.data.mapper.ActivityMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.module.activity.ActivityModule;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.util.DateUtil;
import cn.game.util.Pair;

public class ActivityStateManager extends AbstractGameEventRegistration {

	private static final Logger log = LoggerFactory.getLogger(ActivityManager.class);

	private static ActivityStateManager instance = new ActivityStateManager();

	// 当前开启的活动id（2状态）,一般是按时间开启的全体活动
	private Set<Integer> activeActivitys = new HashSet<>();

	// 1、2、3 状态的活动id ,只是根据时间开启的活动,
	private Map<Integer, Integer> states = new ConcurrentHashMap<>();
	/** 全体活动 */
	public ActivityModule activityModule = new ActivityModule();

	public static ActivityStateManager getInstance() {
		return instance;
	}

	public boolean isOpen(int id) {

		return this.activeActivitys.contains(id);
	}

//	public void open(int id) {
//
//		if (!activeActivitys.contains(id)) {
//			activeActivitys.add(id);
//			activityOp.open(id);
//		}
//	}

	public void close(int id) {

		activeActivitys.remove(Integer.valueOf(id));
		activityModule.shutdown(id);
	}

	public void start() {

		Date nowDate = new Date();
		for (ActivityConfig activityConfig : ActivityManager.instance().list()) {
			int id = activityConfig.ID;
			if (activityConfig.disable) {
				continue;
			}
			if (activityConfig.viewTime == null) { // 不根据时间开启
				continue;
			}
			if (activityConfig.viewTime != null && nowDate.before(activityConfig.viewTime)) { // 暂时未达可见时间
				long howLong = DateUtil.howLong(TimeUnit.MILLISECONDS, nowDate, activityConfig.viewTime);
				TaskManager.getInstance().scheduleGeneral(new ViewTask(id), howLong);
				continue;
			}

			setState(id, ActivityState.VIEW_VALUE);

			int period = activityConfig.period;
			int periodPass = getPeriodPass(id);

			if (activityConfig.destroyTime.isEmpty() == false) {// 活动是否彻底销毁了
				boolean destroy = true;
				for (int i = 0; i < activityConfig.destroyTime.size(); i++) {
					Date date = activityConfig.destroyTime.get(i);
					date = changeDateByPeriod(date, period, periodPass);
					if (nowDate.before(date)) {
						destroy = false;
						break;
					}
				}
				if (destroy) {
					setState(id, ActivityState.NONE_VALUE);
					continue;
				}
			}
			// 更改活动状态的定时任务
			if (activityConfig.startTime.size() > 0) {
				for (int i = 0; i < activityConfig.startTime.size(); i++) {
					Date startDate = activityConfig.startTime.get(i);
					startDate = changeDateByPeriod(startDate, period, periodPass);
					if (nowDate.before(startDate)) {// 活动尚未开启

						long howLong = DateUtil.howLong(TimeUnit.MILLISECONDS, nowDate, startDate);
						if (period > 0) {
							TaskManager.getInstance().scheduleGeneralAtFixedRate(new StartTask(id), howLong, period);
						} else {
							TaskManager.getInstance().scheduleGeneral(new StartTask(id), howLong);
						}
//						continue;
					} else {
//						setState(id, ActivityState.START_VALUE);

					}
				}
			}

			if (activityConfig.endTime.size() > 0) {
				for (int i = 0; i < activityConfig.endTime.size(); i++) {
					Date endDate = activityConfig.endTime.get(i);
					endDate = changeDateByPeriod(endDate, period, periodPass);
					if (nowDate.after(endDate)) {
//						int state = getState(id);
//						if (state != ActivityState.START_VALUE) {
//							setState(id, ActivityState.CLOSE_VALUE);
//						}
//						continue;
					} else {
						long howLong = DateUtil.howLong(TimeUnit.MILLISECONDS, nowDate, endDate);
						if (period > 0) {
							TaskManager.getInstance().scheduleGeneralAtFixedRate(new EndTask(id), howLong, period);
						} else {
							TaskManager.getInstance().scheduleGeneral(new EndTask(id), howLong);
						}
//						setState(id, ActivityState.START_VALUE);
					}
				}

			}

			if (activityConfig.destroyTime.isEmpty() == false) {
				for (int i = 0; i < activityConfig.destroyTime.size(); i++) {
					Date date = activityConfig.destroyTime.get(i);
					date = changeDateByPeriod(date, period, periodPass);
					if (nowDate.before(date)) {
						long howLong = DateUtil.howLong(TimeUnit.MILLISECONDS, nowDate, date);
						if (period > 0) {
							TaskManager.getInstance().scheduleGeneralAtFixedRate(new DestroyTask(id), howLong, period);
						} else {
							TaskManager.getInstance().scheduleGeneral(new DestroyTask(id), howLong);
						}
					}
				}
			}
			// 初始化活动状态
			Pair<Integer, ActivityState> activityState = getActivityState(id);
			if (activityState != null) {
				ActivityState second = activityState.second;
				setState(id, second.getNumber());
				if (second == ActivityState.START) {
					activeActivitys.add(id);
				}
			}
//			System.err.println("活动id : " + id + " 活动状态 : " + getState(id));
		}
	}

	public boolean isStart(int id) {
		return activeActivitys.contains(id);
	}

	/**
	 * 获取同一周期内，多次活动的活动索引及状态,同一时间只有一个活动有效,只会返回start，和end状态
	 * 
	 * @param id
	 * @return null 如果没有活跃的活动 <第几个开启--状态>
	 */
	private Pair<Integer, ActivityState> getActivityState(int id) {
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		int period = activityConfig.period;
		Date nowDate = new Date();
		int periodPass = getPeriodPass(id);
		for (int i = 0; i < activityConfig.startTime.size(); i++) {
			Date startDate = activityConfig.startTime.get(i);
			Date endDate = activityConfig.endTime.get(i);
			Date destoryDate = activityConfig.destroyTime.get(i);
			startDate = changeDateByPeriod(startDate, period, periodPass);
			endDate = changeDateByPeriod(endDate, period, periodPass);
			destoryDate = changeDateByPeriod(destoryDate, period, periodPass);
			if (destoryDate != null && nowDate.after(destoryDate)) {
				continue;
			}
			ActivityState state = ActivityState.NONE;
			if (activityConfig.viewTime == null || nowDate.after(activityConfig.viewTime)) {
				state = ActivityState.VIEW;
			}
			// 尚未销毁
			if (nowDate.after(startDate)) {
				state = ActivityState.START;
			}
			if (nowDate.after(endDate)) {
				state = ActivityState.CLOSE;
			}
			if (state == ActivityState.START || state == ActivityState.CLOSE) { 
				return new Pair<Integer, ActivityState>(i, state); 
			}
		}
		return null;

	}

	/**
	 * 当前时间距离活动配置的开始时间，过了多少个活动周期
	 * 
	 * @param id
	 *            活动id
	 * @return
	 */
	public int getPeriodPass(int id) {
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		long nowTimeMillis = System.currentTimeMillis();
		int periodPass = 0;
		int period = activityConfig.period;
		for (int i = 0; i < activityConfig.startTime.size(); i++) {
			Date date = activityConfig.startTime.get(i);
			if (period > 0 && nowTimeMillis - date.getTime() > period) {
				int tmp = (int) ((nowTimeMillis - date.getTime()) / 1000 / period);
				if (tmp > periodPass) {
					periodPass = tmp;
				}
			}
		}
		return periodPass;
	}

	/**
	 * 获取本期活动的开启时间（有的活动有多个开启时间）(按照时间开启的活动)
	 * 
	 * @param id
	 * @return null 如果活动都关闭了
	 */
	public Date getStartDate(int id) {
		Pair<Integer, ActivityState> activity = getActivityState(id); 
		if (activity == null) {
			return null ; 
		}
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		Date startDate = activityConfig.startTime.get(activity.first);
		startDate = changeDateByPeriod(startDate, activityConfig.period, getPeriodPass(id));
		return startDate;
	}

	/** 
	 * 获取本期活动的结束时间(按照时间开启的活动)
	 * @param id
	 * @return
	 */
	public Date getEndDate(int id) {
		Pair<Integer, ActivityState> activity = getActivityState(id);
		if (activity == null) {
			return null;
		}
		ActivityConfig activityConfig = ActivityManager.instance().get(id);
		Date endDate = activityConfig.endTime.get(activity.first);
		endDate = changeDateByPeriod(endDate, activityConfig.period, getPeriodPass(id));
		return endDate;
	}

	public Date changeDateByPeriod(Date date, int period, int periodPass) {
		if (periodPass == 0) {
			return date ; 
		}
		date = new Date(date.getTime() + period * periodPass * 1000);
		return date;
	}

	class StartTask implements Runnable {

		private int id;

		public StartTask(int id) {

			this.id = id;
		}

		@Override
		public void run() {

			TaskManager.getInstance().addMainTask(() -> {

				setState(id, ActivityState.START_VALUE);

				ActivityConfig activityConfig = ActivityManager.instance().get(id);
				if (!activityConfig.isMultiplayer) {
					ConcurrentHashMap<Long, Player> allPlayer = PlayerManager.getInstance().getAllPlayer();
					for (Player player : allPlayer.values()) {
						player.getGameClient().getContext().runOnContext(r -> {

							ActivityModule activityModule = player.getActivityModule();
							activityModule.open(id);
						});
					}
				} else {
					activityModule.open(id);
					activeActivitys.add(id);
				}
			});
		}
	}

	class EndTask implements Runnable {

		private int id;

		public EndTask(int id) {

			this.id = id;
		}

		@Override
		public void run() {

			TaskManager.getInstance().addMainTask(() -> {
				setState(id, ActivityState.CLOSE_VALUE);
				ActivityConfig activityConfig = ActivityManager.instance().get(id);
				if (!activityConfig.isMultiplayer) {

					ConcurrentHashMap<Long, Player> allPlayer = PlayerManager.getInstance().getAllPlayer();
					for (Player player : allPlayer.values()) {

						player.getGameClient().getContext().runOnContext(r -> {
							ActivityModule activityModule = player.getActivityModule();
							activityModule.shutdown(id);
						});
					}
				} else {
					activityModule.shutdown(id);
					activeActivitys.remove(id);
				}
			});
		}
	}

	class DestroyTask implements Runnable {

		private int id;

		public DestroyTask(int id) {

			this.id = id;
		}

		@Override
		public void run() {

			// close(id);
//			TaskManager.getInstance().addMainTask(() -> {

			ActivityConfig activityConfig = ActivityManager.instance().get(id);
			if (!activityConfig.isMultiplayer) {
				ConcurrentHashMap<Long, Player> allPlayer = PlayerManager.getInstance().getAllPlayer();
				for (Player player : allPlayer.values()) {
					player.getGameClient().getContext().runOnContext(r -> {
						ActivityModule activityModule = player.getActivityModule();
						activityModule.destroy(id);
					});
				}
			} else {
				removeState(id);
				activityModule.destroy(id);
			}
//			});
		}
	}

	class ViewTask implements Runnable {

		private int id;

		public ViewTask(int id) {

			this.id = id;
		}

		@Override
		public void run() {

			// if (!willOpenActivitys.contains(id)) {
			// willOpenActivitys.add(id);
			// }
			// ActivityConfig activityConfig = getActivityConfig(id);
//			TaskManager.getInstance().addMainTask(() -> {
//				setState(id, ActivityState.VIEW_VALUE);
//			});

			setState(id, ActivityState.VIEW_VALUE);

		}
	}

	public void setState(int id, int state) {
		this.states.put(id, state);
	}

	/**
	 * 获取一个活动的具体状态
	 * 
	 * @param id
	 * @return
	 */
	public int getState(int id) {
		Integer state = this.states.get(id);
		return state == null ? ActivityState.NONE_VALUE : state;
	}

	public void removeState(int id) {
		this.states.remove(id);
	}

	/**
	 * 客户端能看到的活动id
	 * 
	 * @return
	 */
	public Collection<Integer> getShowIds() {
		return this.states.keySet();
	}

	public Collection<ActivityInfo> getShowState() {

		List<ActivityInfo> activityInfos = new ArrayList<>();
		for (Integer id : getShowIds()) {
			activityInfos.add(buildActivityInfo(id));
		}
		return activityInfos;
	}

	public ActivityInfo buildActivityInfo(int id) {
		return ActivityInfo.newBuilder().setId(id).setStateValue(getState(id)).setStartTime(getOpenTimeRemaining(id)).build();
	}

	/**
	 * 开启中的活动id
	 * 
	 * @return
	 */
	public List<Integer> getOpenIds() {

		List<Integer> list = new ArrayList<>();
		for (Entry<Integer, Integer> e : this.states.entrySet()) {
			if (e.getValue() == ActivityState.START_VALUE) {
				list.add(e.getKey());
			}
		}
		return list;
	}

	/** 
	 * 活动还有多久开启
	 * @param id
	 * @return
	 */
	public int getOpenTimeRemaining(int id) {
		Date startDate = getStartDate(id);
		if (startDate == null) {
			return 0;
		}
		Date now = new Date();
		return (int) DateUtil.howLong(TimeUnit.SECONDS, startDate, now);
	}

	public void initGlobal() {
//		DataGameServerInterface dataGameServerInterfaceSync = GameServer.getInstance().getDataGameServerInterfaceSync();

		//查询所有玩家爬塔数据
//		List<ClimbingTower> climbingTowers = (List<ClimbingTower>) dataGameServerInterfaceSync.exec(ClimbingTowerMapper.class,
//				MapperConstant.selectAll, null);
//		climbingTowers.forEach(e -> this.playerClimbingTowerData.put(e.getPlayerId(), e));

		@SuppressWarnings("unchecked")
		List<Activity> activities = (List<Activity>) DAO.executeSync(ActivityMapper.class,
				MapperConstant.selectByPlayerId, 0L);

//		activityOp.initLoadData(activities);

	}

}
