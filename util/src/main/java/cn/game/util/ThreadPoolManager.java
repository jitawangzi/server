package cn.game.util;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程池
 * 
 * @author SYQ
 */
public class ThreadPoolManager {
	private static Logger log = LoggerFactory.getLogger("threadpoolLog");

	private static ThreadPoolManager _instance;
	/** 定时执行线程池 */
	private ScheduledThreadPoolExecutor _generalScheduledThreadPool;
	/** 游戏主逻辑线程池 */
	private ThreadPoolExecutor _generalPacketsThreadPool;

	private static final long MAX_DELAY = Long.MAX_VALUE / 1000000 / 2;

	public static ThreadPoolManager getInstance() {
		if (_instance == null) {
			_instance = new ThreadPoolManager();
			// _instance.scheduleGeneralAtFixedRate(_instance.new
			// ThreadPoolInfo(), 0, 60000);

		}
		return _instance;
	}

	public ThreadPoolManager() {
		_generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.generalScheduled,
				new PriorityThreadFactory("GeneralSTPool", Thread.NORM_PRIORITY));

		// if (Config.generalPacketWorkQueueSize > 0)
		// {
		// // 设置队列LinkedBlockingQueue 的容量
		// _generalPacketsThreadPool = new
		// ThreadPoolExecutor(Config.generalPacketMin, Config.generalPacketMax,
		// Config.generalPacketKeepAliveTime, TimeUnit.SECONDS, new
		// LinkedBlockingQueue<Runnable>(
		// Config.generalPacketWorkQueueSize), new PriorityThreadFactory("Normal
		// Packet Pool",
		// Thread.NORM_PRIORITY + 1), new
		// ThreadPoolExecutor.DiscardOldestPolicy()/* 抛弃旧的任务 */);
		// } else
		// {
		// // 不设置队列LinkedBlockingQueue 的容量，容量为int最大值
		// _generalPacketsThreadPool = new
		// ThreadPoolExecutor(Config.generalPacketMin, Config.generalPacketMax,
		// Config.generalPacketKeepAliveTime, TimeUnit.SECONDS, new
		// LinkedBlockingQueue<Runnable>(),
		// new PriorityThreadFactory("Normal Packet Pool", Thread.NORM_PRIORITY
		// + 1));
		// }

		_generalPacketsThreadPool = (ThreadPoolExecutor) Executors
				.newCachedThreadPool(new PriorityThreadFactory("Normal Packet Pool", Thread.NORM_PRIORITY + 1));
	}

	public static long validateDelay(long delay) {
		if (delay < 0) {
			delay = 0;
		} else if (delay > MAX_DELAY) {
			delay = MAX_DELAY;
		}
		return delay;
	}

	/**
	 * 延时执行
	 * 
	 * @param r
	 *            待执行的任务
	 * @param delay
	 *            (ms)
	 *            延时时间
	 * @return
	 */
	public ScheduledFuture<?> scheduleGeneral(Runnable r, long delay) {
		try {
			delay = ThreadPoolManager.validateDelay(delay);
			return _generalScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) {
			return null; /* shutdown, ignore */
		}
	}

	/**
	 * 移除定时任务
	 * 
	 * @param task
	 */
	public boolean removeScheduleTask(Runnable task) {
		return _generalScheduledThreadPool.remove(task);
	}

	/**
	 * 创建并执行一个在给定初始延迟后首次启用的定期操作，后续操作具有给定的周期；
	 * 也就是将在 initialDelay 后开始执行，然后在 initialDelay+period 后执行，
	 * 接着在 initialDelay + 2 * period 后执行，依此类推。
	 * 
	 * @param command
	 *            重复执行的任务
	 * @param initialDelay
	 *            最初执行时间
	 * @param period
	 *            延时时间
	 * @return
	 */
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable command, long initialDelay, long period) {
		try {
			period = ThreadPoolManager.validateDelay(period);
			initialDelay = ThreadPoolManager.validateDelay(initialDelay);
			return _generalScheduledThreadPool.scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) {
			return null; /* shutdown, ignore */
		}
	}

	/**
	 * 返回通用线程池状态信息
	 * 
	 * @return
	 */
	public String[] getState() {
		return new String[] { " +- Normal Packet Pool:", " |- ActiveThreads:   " + _generalPacketsThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + _generalPacketsThreadPool.getCorePoolSize(),
				" |- MaximumPoolSize: " + _generalPacketsThreadPool.getMaximumPoolSize(),
				" |- LargestPoolSize: " + _generalPacketsThreadPool.getLargestPoolSize(),
				" |- PoolSize:        " + _generalPacketsThreadPool.getPoolSize(),
				" |- CompletedTasks:  " + _generalPacketsThreadPool.getCompletedTaskCount(),
				" |- QueuedTasks:     " + _generalPacketsThreadPool.getQueue().size(), " +- Scheduled Pool:",
				" |- ActiveThreads:   " + _generalScheduledThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + _generalScheduledThreadPool.getCorePoolSize(),
				" |- LargestPoolSize: " + _generalScheduledThreadPool.getLargestPoolSize(),
				" |- PoolSize:        " + _generalScheduledThreadPool.getPoolSize(),
				" |- MaximumPoolSize: " + _generalScheduledThreadPool.getMaximumPoolSize(),
				" |- CompletedTasks:  " + _generalScheduledThreadPool.getCompletedTaskCount(), " |- ScheduledTasks:  "
						+ (_generalScheduledThreadPool.getTaskCount() - _generalScheduledThreadPool.getCompletedTaskCount()) };
	}

	class ThreadPoolInfo implements Runnable {
		private StringBuffer buf = new StringBuffer();

		public void run() {
			if (buf.length() > 0) {
				buf.delete(0, buf.length());
			}
			buf.append("\r\n----------------------------------\r\n");
			for (String s : getState()) {
				buf.append(s).append("\r\n");
			}
			buf.append("\r\n----------------------------------\r\n");
			log.info(buf.toString());
		}
	}

	public void executePacket(Runnable task) {
		_generalPacketsThreadPool.execute(task);
	}

}
