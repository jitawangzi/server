package cn.game.core.task;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public class SchedulerService {
	private static Logger log = LoggerFactory.getLogger(SchedulerService.class);

	private static final SchedulerService INSTANCE = new SchedulerService();
	private final ThreadPoolTaskScheduler taskScheduler;

	private SchedulerService() {
		this.taskScheduler = new ThreadPoolTaskScheduler();
		this.taskScheduler.setPoolSize(5);
		this.taskScheduler.setThreadNamePrefix("SpringTaskScheduler-");
		this.taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		this.taskScheduler.setAwaitTerminationSeconds(60);
		this.taskScheduler.setErrorHandler(t -> log.error("Unexpected error occurred in spring scheduled task: " + t.getMessage()));
		this.taskScheduler.initialize();
	}

	public static SchedulerService getInstance() {
		return INSTANCE;
	}

	/** 
	 * 立即执行一次任务
	 * @param task
	 */
	public void executeTask(Runnable task) {
		taskScheduler.execute(new ErrorHandlingRunnable(task));
	}

	/** 
	 * 延迟执行一次任务
	 * @param task
	 * @param delay
	 * @param timeUnit
	 * @return
	 */
	public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit timeUnit) {
		return taskScheduler
				.schedule(new ErrorHandlingRunnable(task), new java.util.Date(System.currentTimeMillis() + timeUnit.toMillis(delay)));
	}

	/** 
	 * 使用 cron 表达式执行任务
	 * @param task
	 * @param cronExpression
	 * @return
	 */
	public ScheduledFuture<?> scheduleCronTask(Runnable task, String cronExpression) {
		return taskScheduler.schedule(new ErrorHandlingRunnable(task), new CronTrigger(cronExpression));
	}

	/** 
	 * 立即执行一次，然后以固定频率执行任务，以任务开始时间点为基准
	 * @param task
	 * @param period
	 * @param timeUnit
	 * @return
	 */
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period, TimeUnit timeUnit) {
		return taskScheduler.scheduleAtFixedRate(new ErrorHandlingRunnable(task), toDuration(period, timeUnit));
	}

	/**
	 * 先等待初始延迟，然后执行一次，之后以固定频率执行任务，以任务开始时间点为基准
	 * @param task 要执行的任务
	 * @param initialDelay 初始延迟时间
	 * @param period 固定频率的周期
	 * @param timeUnit 时间单位
	 * @return ScheduledFuture<?> 可用于控制任务的执行
	 */
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit timeUnit) {
		return taskScheduler
				.scheduleAtFixedRate(new ErrorHandlingRunnable(task),
						new java.util.Date(System.currentTimeMillis() + timeUnit.toMillis(initialDelay)), period);
	}

	/** 
	 * 立即执行一次，之后固定延迟执行任务，以任务结束时间点为基准
	 * @param task
	 * @param delay
	 * @param timeUnit
	 * @return
	 */
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay, TimeUnit timeUnit) {
		return taskScheduler.scheduleWithFixedDelay(new ErrorHandlingRunnable(task), toDuration(delay, timeUnit));
	}


	// 关闭调度器
	public void shutdown() {
		taskScheduler.shutdown();
	}

	/** 
	 * 取消任务
	 * @param scheduledTask
	 */
	public void cancelTask(ScheduledFuture<?> scheduledTask) {
		if (scheduledTask != null && !scheduledTask.isCancelled()) {
			scheduledTask.cancel(false);
		}
	}

	private Duration toDuration(long time, TimeUnit timeUnit) {
		return Duration.ofMillis(timeUnit.toMillis(time));
	}
	private class ErrorHandlingRunnable implements Runnable {
		private final Runnable task;

		public ErrorHandlingRunnable(Runnable task) {
			this.task = task;
		}

		@Override
		public void run() {
			try {
				task.run();
			} catch (Exception e) {
				log.error("Error occurred while executing task: " + e.getMessage(), e);
				// 可以在这里添加更多的错误处理逻辑，比如发送警报邮件等
			}
		}
	}

	public static void main(String[] args) {
//		SchedulerService.getInstance().scheduleTask(() -> System.out.println("scheduleTask Hello World!"), 5, TimeUnit.SECONDS);
//		SchedulerService.getInstance().scheduleCronTask(() -> System.out.println("scheduleCronTask Hello World!"), "0/5 * * * * ?");
//		SchedulerService
//				.getInstance()
//				.scheduleAtFixedRate(() -> System.out.println("scheduleAtFixedRate Hello World!"), 5, TimeUnit.SECONDS);
//		SchedulerService
//				.getInstance()
//				.scheduleWithFixedDelay(() -> System.out.println("scheduleWithFixedDelay Hello World!"), 5, TimeUnit.SECONDS);
	}
}