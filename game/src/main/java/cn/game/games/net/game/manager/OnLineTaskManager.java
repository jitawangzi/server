package cn.game.games.net.game.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.task.TaskManager;


/**
 * 在线玩家的定时任务，下线会自动清除所有任务
 * 2020年9月22日 上午10:49:40
 * @author SYQ
 */
public class OnLineTaskManager {

	private static final Logger log = LoggerFactory.getLogger(OnLineTaskManager.class);

	private ConcurrentMap<Long, List<ScheduledFuture<?>>> futures = new ConcurrentHashMap<>();

	private static OnLineTaskManager instance = new OnLineTaskManager() ; 

	public static OnLineTaskManager getInstance() {
		return instance ; 
	}

	/**
	 * 给玩家增加一个定时任务
	 * @param playerId
	 * @param r
	 * @param delay
	 */
	public void addScheduledTask(long playerId, Runnable r, long delay) {
		List<ScheduledFuture<?>> list = futures.get(playerId);
		if (list == null) {
			list = new ArrayList<ScheduledFuture<?>>();
			List<ScheduledFuture<?>> putIfAbsent = futures.putIfAbsent(playerId, list);
			if (putIfAbsent != null) {
				list = putIfAbsent;
			}
		}
		ScheduledFuture<?> scheduleGeneral = TaskManager.getInstance().scheduleGeneral(r, delay);
		list.add(scheduleGeneral);
	}
	/**
	 * 给玩家增加一个周期任务
	 * @param playerId
	 * @param r
	 *            任务
	 * @param delay
	 *            多久后第一次执行(毫秒)
	 * @param period
	 *            任务时间间隔 (毫秒)
	 */
	public void addScheduledTasksAtFixedRate(long playerId, Runnable r, long delay, long period) {
		List<ScheduledFuture<?>> list = futures.get(playerId);
		if (list == null) {
			list = new ArrayList<ScheduledFuture<?>>();
			List<ScheduledFuture<?>> putIfAbsent = futures.putIfAbsent(playerId, list);
			if (putIfAbsent != null) {
				list = putIfAbsent;
			}
		}
		ScheduledFuture<?> scheduleGeneral = TaskManager.getInstance().scheduleGeneralAtFixedRate(r, delay, period);
		list.add(scheduleGeneral);
	}

	/**
	 * 移除一个玩家所有的定时任务
	 * @param playerId
	 */
	public void removeScheduledTask(long playerId) {
		List<ScheduledFuture<?>> list = this.futures.remove(playerId);
		if (list != null) {
			for (ScheduledFuture<?> scheduledFuture : list) {
				scheduledFuture.cancel(true);
			}
		}
	}

}
