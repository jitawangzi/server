package cn.game.games.net.game.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.task.SchedulerService;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.server.ServerEventBus;
import cn.game.games.net.game.module.activity.ActivityModule;
import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.protocol.generated.manager.ActivityManager;
import cn.game.protocol.protobuf.ActivityMsg.ActivityInfo;
import cn.game.protocol.protobuf.ActivityMsg.ActivityState;
import cn.game.util.DateUtil;
import cn.game.util.Pair;

public class ActivityStateManager {

	private static final Logger log = LoggerFactory.getLogger(ActivityManager.class);

	private static ActivityStateManager instance = new ActivityStateManager();

	// 当前开启的活动id（2状态）,一般是按时间开启的全体活动
	private Set<Integer> activeActivitys = new HashSet<>();

	// 1、2、3 状态的活动id ,只是根据时间开启的活动,
	private Map<Integer, Integer> states = new ConcurrentHashMap<>();

	// 如需统一时区，可在此处配置；目前使用系统默认
	private final TimeZone timeZone = TimeZone.getDefault();

	public static ActivityStateManager getInstance() {
		return instance;
	}

	public void start() {
		Date nowDate = new Date();
		for (ActivityConfig activityConfig : ActivityManager.instance().list()) {
			int id = activityConfig.ID;
			if (activityConfig.disable) {
				continue;
			}

			// 判断是否“根据时间开启可见”
			boolean hasViewCron = activityConfig.viewCron != null && !activityConfig.viewCron.isEmpty();
			boolean hasViewTimes = activityConfig.viewTime != null && !activityConfig.viewTime.isEmpty();

			// 如果既没有 viewTimes 也没有 viewCron，认为“非时间可见”，按原逻辑跳过 view 的时间调度
			if (!hasViewCron && !hasViewTimes) {
				continue;
			}

			// 先处理 view 的定时（优先 cron）
			if (hasViewCron) {
				scheduleNextByCron(id, activityConfig.viewCron, () -> setState(id, ActivityState.VIEW_VALUE));
			} else {
				// 多个 viewTimes，逐个挂任务（支持 period 滚动）
				int period = activityConfig.period;
				int periodPass = getPeriodPass(id);
				for (Date vt : activityConfig.viewTime) {
					if (vt == null)
						continue;
					Date viewDate = DateUtil.changeDateByPeriod(vt, period, periodPass);
					if (nowDate.before(viewDate)) {
						long howLong = DateUtil.howLong(TimeUnit.MILLISECONDS, nowDate, viewDate);
						if (period > 0) {
							SchedulerService.getInstance()
									.scheduleAtFixedRate(() -> setState(id, ActivityState.VIEW_VALUE), howLong, period,
											TimeUnit.MILLISECONDS);
						} else {
							SchedulerService.getInstance()
									.scheduleTask(() -> setState(id, ActivityState.VIEW_VALUE), howLong, TimeUnit.MILLISECONDS);
						}
					}
				}
			}

			// 到这里，当前时刻若已过任一可见点，则设为 VIEW
			boolean shouldBeView = false;
			if (hasViewCron) {
				// 使用 cron 估算：若存在“之前的某个可见触发”，则当前可视
				// 简化：如果 next(viewCron) 在未来，但我们已经是首次循环，仍旧立即置为 VIEW 以保证可见。
				// 如果你需要更严谨的“上一次 view 触发点”判断，可复用 lastFireBefore(viewCron, nowDate)。
				shouldBeView = true;
			} else {
				for (Date vt : activityConfig.viewTime) {
					if (vt == null)
						continue;
					int period = activityConfig.period;
					int periodPass = getPeriodPass(id);
					Date viewDate = DateUtil.changeDateByPeriod(vt, period, periodPass);
					if (!nowDate.before(viewDate)) {
						shouldBeView = true;
						break;
					}
				}
			}
			if (shouldBeView) {
				setState(id, ActivityState.VIEW_VALUE);
			}

			// 下面逻辑（start/end/destroy 调度与初始化）保持与之前一致
			boolean hasAnyCron = hasAnyCron(activityConfig);

			if (hasAnyCron) {
				// 先根据 cron 对 start/ end/ destroy 分别进行懒调度
				scheduleNextByCron(id, activityConfig.startCron, new StartTask(id));
				if (activityConfig.endCron != null && !activityConfig.endCron.isEmpty()) {
					scheduleNextByCron(id, activityConfig.endCron, new EndTask(id));
				}
				if (activityConfig.destroyCron != null && !activityConfig.destroyCron.isEmpty()) {
					scheduleNextByCron(id, activityConfig.destroyCron, new DestroyTask(id));
				}

				// 初始化活动状态（基于 cron 推断）
				ActivityState initState = computeStateByCron(activityConfig, nowDate);
				if (initState != null && initState != ActivityState.NONE) {
					setState(id, initState.getNumber());
					if (initState == ActivityState.START) {
						activeActivitys.add(id);
					}
				}
			} else {
				// 原有逻辑：时间点 + period
				int period = activityConfig.period;
				int periodPass = getPeriodPass(id);

				if (activityConfig.destroyTime.isEmpty() == false) {// 活动是否彻底销毁了
					boolean destroy = true;
					for (int i = 0; i < activityConfig.destroyTime.size(); i++) {
						Date date = activityConfig.destroyTime.get(i);
						date = DateUtil.changeDateByPeriod(date, period, periodPass);
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
						startDate = DateUtil.changeDateByPeriod(startDate, period, periodPass);
						if (nowDate.before(startDate)) {// 活动尚未开启
							long howLong = DateUtil.howLong(TimeUnit.MILLISECONDS, nowDate, startDate);
							if (period > 0) {
								SchedulerService.getInstance()
										.scheduleAtFixedRate(new StartTask(id), howLong, period, TimeUnit.MILLISECONDS);
							} else {
								SchedulerService.getInstance().scheduleTask(new StartTask(id), howLong, TimeUnit.MILLISECONDS);
							}
						}
					}
				}

				if (activityConfig.endTime.size() > 0) {
					for (int i = 0; i < activityConfig.endTime.size(); i++) {
						Date endDate = activityConfig.endTime.get(i);
						endDate = DateUtil.changeDateByPeriod(endDate, period, periodPass);
						if (nowDate.before(endDate)) {
							long howLong = DateUtil.howLong(TimeUnit.MILLISECONDS, nowDate, endDate);
							if (period > 0) {
								SchedulerService.getInstance().scheduleAtFixedRate(new EndTask(id), howLong, period, TimeUnit.MILLISECONDS);
							} else {
								SchedulerService.getInstance().scheduleTask(new EndTask(id), howLong, TimeUnit.MILLISECONDS);
							}
						}
					}
				}

				if (activityConfig.destroyTime.isEmpty() == false) {
					for (int i = 0; i < activityConfig.destroyTime.size(); i++) {
						Date date = activityConfig.destroyTime.get(i);
						date = DateUtil.changeDateByPeriod(date, period, periodPass);
						if (nowDate.before(date)) {
							long howLong = DateUtil.howLong(TimeUnit.MILLISECONDS, nowDate, date);
							if (period > 0) {
								SchedulerService.getInstance()
										.scheduleAtFixedRate(new DestroyTask(id), howLong, period, TimeUnit.MILLISECONDS);
							} else {
								SchedulerService.getInstance().scheduleTask(new DestroyTask(id), howLong, TimeUnit.MILLISECONDS);
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
			}
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

		// 若存在 Cron，改为走 Cron 推断，保持旧接口契约（用 i=0 占位返回）
		if (hasAnyCron(activityConfig)) {
			ActivityState state = computeStateByCron(activityConfig, new Date());
			if (state == ActivityState.START || state == ActivityState.CLOSE) {
				return new Pair<Integer, ActivityState>(0, state);
			}
			return null;
		}

		int period = activityConfig.period;
		Date nowDate = new Date();
		int periodPass = getPeriodPass(id);
		for (int i = 0; i < activityConfig.startTime.size(); i++) {
			Date startDate = activityConfig.startTime.get(i);

			Date endDate = null;
			Date destoryDate = null;
			if (i < activityConfig.endTime.size()) {
				endDate = activityConfig.endTime.get(i);
			}
			if (i < activityConfig.destroyTime.size()) {
				destoryDate = activityConfig.destroyTime.get(i);
			}
			startDate = DateUtil.changeDateByPeriod(startDate, period, periodPass);
			endDate = DateUtil.changeDateByPeriod(endDate, period, periodPass);
			destoryDate = DateUtil.changeDateByPeriod(destoryDate, period, periodPass);

			if (destoryDate != null && nowDate.after(destoryDate)) {
				continue;
			}
			ActivityState state = ActivityState.NONE;

			// VIEW 判断：若配置了任一 viewTimes 且当前时间已达其中任一（考虑 period），则 VIEW；
			// 若未配置 viewTimes（但可能配置了 viewCron，已在 cron 路径处理），这里按“可见”处理以兼容旧逻辑。
			boolean visible = false;
			if (activityConfig.viewTime != null && !activityConfig.viewTime.isEmpty()) {
				for (Date vt : activityConfig.viewTime) {
					if (vt == null)
						continue;
					Date viewDate = DateUtil.changeDateByPeriod(vt, period, periodPass);
					if (!nowDate.before(viewDate)) { // now >= viewDate
						visible = true;
						break;
					}
				}
			} else {
				visible = true;
			}
			if (visible) {
				state = ActivityState.VIEW;
			}

			// 尚未销毁
			if (startDate != null && nowDate.after(startDate)) {
				state = ActivityState.START;
			}
			if (endDate != null && nowDate.after(endDate)) {
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
		int periodSec = activityConfig.period;
		if (periodSec <= 0)
			return 0;
		long periodMs = periodSec * 1000L;

		for (int i = 0; i < activityConfig.startTime.size(); i++) {
			Date date = activityConfig.startTime.get(i);
			if (date == null)
				continue;
			long diff = nowTimeMillis - date.getTime();
			if (diff > periodMs) {
				int tmp = (int) (diff / periodMs);
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
		ActivityConfig cfg = ActivityManager.instance().get(id);

		// Cron 优先：返回“下一次将开始的时间”
		if (hasAnyCron(cfg) && cfg.startCron != null && !cfg.startCron.isEmpty()) {
			Date now = new Date();
			Date next = nextByCrons(cfg.startCron, now);
			return next;
		}

		Pair<Integer, ActivityState> activity = getActivityState(id);
		if (activity == null) {
			return null;
		}
		Date startDate = cfg.startTime.get(activity.first);
		startDate = DateUtil.changeDateByPeriod(startDate, cfg.period, getPeriodPass(id));
		return startDate;
	}

	/** 
	 * 获取本期活动的结束时间(按照时间开启的活动)
	 * @param id
	 * @return
	 */
	public Date getEndDate(int id) {
		ActivityConfig cfg = ActivityManager.instance().get(id);

		// Cron 优先
		if (hasAnyCron(cfg) && cfg.endCron != null && !cfg.endCron.isEmpty()) {
			return nextByCrons(cfg.endCron, new Date());
		}

		Pair<Integer, ActivityState> activity = getActivityState(id);
		if (activity == null) {
			return null;
		}
		if (cfg.endTime.isEmpty()) {
			return null;
		}
		Date endDate = cfg.endTime.get(activity.first);
		endDate = DateUtil.changeDateByPeriod(endDate, cfg.period, getPeriodPass(id));
		return endDate;
	}

	public long getEndTime(int id) {
		Date endDate = getEndDate(id);
		return endDate == null ? 0 : endDate.getTime();
	}

	/** 
	 * 距离活动结束还有多少秒
	 * @param id
	 * @return
	 */
	public int getEndTimeRemaining(int id) {
		long endTime = getEndTime(id);
		return (int) (endTime <= 0 ? 0 : (endTime - System.currentTimeMillis()) / 1000);
	}

	class StartTask implements Runnable {

		private int id;

		public StartTask(int id) {
			this.id = id;
		}

		@Override
		public void run() {
			setState(id, ActivityState.START_VALUE);

			ActivityConfig activityConfig = ActivityManager.instance().get(id);
			if (!activityConfig.isMultiplayer) {
				ConcurrentHashMap<Long, Player> allPlayer = PlayerManager.getInstance().getAllPlayer();
				for (Player player : allPlayer.values()) {
					ServerContext.getInstance().getProcessor().process(player.getPlayerId(), () -> {
						ActivityModule activityModule = player.getActivityModule();
						activityModule.open(id, true);
					});
				}
			} else {
				activeActivitys.add(id);
				ServerEventBus.getInstance().dispatch(ServerEventTypeEnum.ActivityOpenTime, id);
			}

			// 如果未配置 endCron/destroyCron，则按相对时长兜底（兼容老配置）
			boolean hasEndCron = activityConfig.endCron != null && !activityConfig.endCron.isEmpty();
			boolean hasDestroyCron = activityConfig.destroyCron != null && !activityConfig.destroyCron.isEmpty();
			if (!hasEndCron || !hasDestroyCron) {
				scheduleRelativeEndAndDestroy(activityConfig, id);
			}
		}
	}

	class EndTask implements Runnable {

		private int id;

		public EndTask(int id) {
			this.id = id;
		}

		@Override
		public void run() {
			setState(id, ActivityState.CLOSE_VALUE);
			ActivityConfig activityConfig = ActivityManager.instance().get(id);
			if (!activityConfig.isMultiplayer) {

				ConcurrentHashMap<Long, Player> allPlayer = PlayerManager.getInstance().getAllPlayer();
				for (Player player : allPlayer.values()) {
					ServerContext.getInstance().getProcessor().process(player.getPlayerId(), () -> {
						// 关闭活动
						ActivityModule activityModule = player.getActivityModule();
						activityModule.shutdown(id);
					});
				}
			} else {
				ServerEventBus.getInstance().dispatch(ServerEventTypeEnum.ActivityShutDownTime, id);
				activeActivitys.remove(id);
			}

		}
	}

	class DestroyTask implements Runnable {

		private int id;

		public DestroyTask(int id) {
			this.id = id;
		}

		@Override
		public void run() {

			ActivityConfig activityConfig = ActivityManager.instance().get(id);
			if (!activityConfig.isMultiplayer) {
				ConcurrentHashMap<Long, Player> allPlayer = PlayerManager.getInstance().getAllPlayer();
				for (Player player : allPlayer.values()) {
					ServerContext.getInstance().getProcessor().process(player.getPlayerId(), () -> {
						ActivityModule activityModule = player.getActivityModule();
						activityModule.destroy(id);
					});
				}
			} else {
				removeState(id);
				ServerEventBus.getInstance().dispatch(ServerEventTypeEnum.ActivityDestoryTime, id);
			}
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
		return ActivityInfo.newBuilder()
				.setId(id)
				.setStateValue(getState(id))
				.setStartTime(getOpenTimeRemaining(id))
				.setEndTime(getEndTimeRemaining(id))
				.build();
	}

	/**
	 * 开启中的活动id
	 * 
	 * @return
	 */
	public Set<Integer> getOpenIds() {
		return activeActivitys;
	}

	/** 
	 * 活动还有多久开启
	 * 根据时间开启的活动
	 * @param id
	 * @return
	 */
	public int getOpenTimeRemaining(int id) {
		Date startDate = getStartDate(id);
		if (startDate == null) {
			return 0;
		}
		Date now = new Date();
		if (now.after(startDate)) {
			return 0;
		}
		return (int) DateUtil.howLong(TimeUnit.SECONDS, startDate, now);
	}

	/* ===================== 下面是 Cron 优先的辅助方法 ===================== */

	private boolean hasAnyCron(ActivityConfig cfg) {
		return (cfg.startCron != null && !cfg.startCron.isEmpty()) || (cfg.endCron != null && !cfg.endCron.isEmpty())
				|| (cfg.destroyCron != null && !cfg.destroyCron.isEmpty()) || (cfg.viewCron != null && !cfg.viewCron.isEmpty());
	}

	private void scheduleNextByCron(int id, List<CronExpression> crons, Runnable task) {
		if (crons == null || crons.isEmpty())
			return;
		Date now = new Date();
		Date next = nextByCrons(crons, now);
		if (next == null)
			return;

		long delay = next.getTime() - now.getTime();
		if (delay < 0)
			delay = 0; // 容错

		SchedulerService.getInstance().scheduleTask(() -> {
			try {
				task.run();
			} finally {
				// 触发后再排下一次
				scheduleNextByCron(id, crons, task);
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	private Date nextByCrons(List<CronExpression> crons, Date after) {
		Date min = null;
		for (CronExpression ce : crons) {
			try {
				// 如需强制统一时区，可开启下面一行
				// ce.setTimeZone(timeZone);
				Date d = ce.getNextValidTimeAfter(after);
				if (d != null && (min == null || d.before(min))) {
					min = d;
				}
			} catch (Exception e) {
				log.error("Cron compute error", e);
			}
		}
		return min;
	}

	// 基于 Cron 的状态推断：简单规则
	// - 若最近一次 startCron 触发时间 <= now 且 now < 最近一次 endCron 触发时间 => START
	// - 若存在 endCron 且 now >= 最近一次 endCron（但下个 start 未来）=> CLOSE
	// - 否则 VIEW 或 NONE（这里返回 VIEW，让外层在 start() 时已设 VIEW；没有 start 可用时为 VIEW/NONE 等同）
	private ActivityState computeStateByCron(ActivityConfig cfg, Date now) {
		try {
			Date lastStart = lastFireBefore(cfg.startCron, now);
			Date lastEnd = lastFireBefore(cfg.endCron, now);
			Date nextStart = nextByCrons(cfg.startCron, now);
			Date nextEnd = nextByCrons(cfg.endCron, now);

			// 若没有任何 startCron，则无法由 Cron 判定为 START
			if (cfg.startCron == null || cfg.startCron.isEmpty()) {
				return ActivityState.VIEW;
			}

			// 情况A：有 lastStart，且未出现 end 或 end 晚于 now
			boolean started = lastStart != null && (lastEnd == null || lastStart.after(lastEnd));
			if (started) {
				// 若 endCron 未配置，则视为 START 中；若配置了 endCron，且 nextEnd 在未来，则 START
				if (cfg.endCron == null || cfg.endCron.isEmpty()) {
					return ActivityState.START;
				}
				if (nextEnd != null && now.before(nextEnd)) {
					return ActivityState.START;
				}
				// 走到这里，说明应已结束
				return ActivityState.CLOSE;
			}

			// 情况B：尚未开始，返回 VIEW（上层已在 start() 中设为 VIEW）
			// 如果最近一次 end 在现在之前，且 nextStart 在未来，可认为 CLOSE（历史期已结束）
			if (lastEnd != null && (lastStart == null || lastEnd.after(lastStart))) {
				return ActivityState.CLOSE;
			}

			return ActivityState.VIEW;
		} catch (Exception e) {
			log.error("computeStateByCron error for activity {}", cfg.ID, e);
			return ActivityState.VIEW;
		}
	}

	private Date lastFireBefore(List<CronExpression> crons, Date now) {
		if (crons == null || crons.isEmpty())
			return null;
		// 通过回退窗口寻找上一触发点（近似实现）
		long windowMs = TimeUnit.DAYS.toMillis(370);
		Date from = new Date(now.getTime() - windowMs);
		if (from.after(now))
			return null;

		Date candidate = null;
		for (CronExpression ce : crons) {
			try {
				// ce.setTimeZone(timeZone);
				Date d = ce.getNextValidTimeAfter(from);
				Date last = null;
				while (d != null && d.before(now)) {
					last = d;
					d = ce.getNextValidTimeAfter(d);
					if (d != null && d.equals(last))
						break;
				}
				if (last != null && (candidate == null || last.after(candidate))) {
					candidate = last;
				}
			} catch (Exception e) {
				log.error("lastFireBefore cron error", e);
			}
		}
		return candidate;
	}

	/** 
	 * 还有多少毫秒到达活动结束时间
	* type==2（秒）：直接 endDuration 秒
	* type==1（天）：按自然日结算——从 startMillis 所在当天的24:00作为第一天的结束点，
	                 若 endDuration>1，再顺延到后续天的0点。
	 * @param cfg
	 * @param startMillis
	 * @return
	 */
	public long getEffectiveEndDurationMs(ActivityConfig cfg, long startMillis) {
		if (cfg.endDuration <= 0)
			return 0L;

		if (cfg.endDurationType == 2) { // 秒
			return TimeUnit.SECONDS.toMillis(cfg.endDuration);
		} else { // 天：自然日结算
			long targetZero = DateUtil.nextDayStartTime(startMillis, cfg.endDuration);
			return Math.max(0L, targetZero - startMillis);
		}
	}

	/** 
	 * 还有多少毫秒到达活动销毁时间
	 * @param cfg
	 * @param startMillis
	 * @return
	 */
	public long getEffectiveDestroyDurationMs(ActivityConfig cfg, long startMillis) {
		if (cfg.destroyDuration <= 0)
			return 0L;

		if (cfg.destroyDurationType == 2) { // 秒
			return TimeUnit.SECONDS.toMillis(cfg.destroyDuration);
		} else { // 天：自然日结算
			long targetZero = DateUtil.nextDayStartTime(startMillis, cfg.destroyDuration);
			return Math.max(0L, targetZero - startMillis);
		}
	}

	private void scheduleRelativeEndAndDestroy(ActivityConfig cfg, int id) {
		long timeMillis = System.currentTimeMillis();
		long endDelayMs = getEffectiveEndDurationMs(cfg, timeMillis);
		if (endDelayMs > 0) {
			SchedulerService.getInstance().scheduleTask(new EndTask(id), endDelayMs, TimeUnit.MILLISECONDS);
		}
		long destroyDelayMs = getEffectiveDestroyDurationMs(cfg, timeMillis);
		if (destroyDelayMs > 0) {
			SchedulerService.getInstance().scheduleTask(new DestroyTask(id), destroyDelayMs, TimeUnit.MILLISECONDS);
		}
	}
}