package cn.game.core.task;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.ErrorHandler;

/**    
 * 定时任务调度器，主要可以处理cron表达式类型的任务
 * 2025年1月16日 16:08:30
 * @author SYQ
 */
public class SchedulerService {
	private static Logger log = LoggerFactory.getLogger(SchedulerService.class);
	/** 保持 Spring 调度入口既有的异常记录策略，覆盖一次性任务的 Throwable。 */
	private static final ErrorHandler SCHEDULE_ERROR_HANDLER =
			t -> log.error("Unexpected error occurred in spring scheduled task: ", t);

	private static final SchedulerService INSTANCE = new SchedulerService();
	private final ThreadPoolTaskScheduler taskScheduler;

	private SchedulerService() {
		this(new ThreadPoolTaskScheduler());
	}

	/**
	 * 装配独立调度资源，允许测试控制墙钟而不修改全局单例。
	 * @param taskScheduler 本服务负责初始化和关闭的调度器
	 */
	SchedulerService(ThreadPoolTaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
		this.taskScheduler.setPoolSize(5);
		this.taskScheduler.setThreadNamePrefix("SpringTaskScheduler-");
		this.taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		this.taskScheduler.setAwaitTerminationSeconds(60);
		this.taskScheduler.setErrorHandler(SCHEDULE_ERROR_HANDLER);
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
	 * 相对等待后执行一次任务，不早于指定延迟；调度拥塞可以导致晚执行。
	 * @param task 到期动作，异常沿用调度服务的日志策略
	 * @param delay 相对等待时长，零或负值按底层执行器契约立即受理
	 * @param timeUnit 等待时长单位，保留底层执行器支持的纳秒精度
	 * @return 可取消或等待实际执行完成的句柄
	 */
	public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit timeUnit) {
		// 相对等待直接交给单调计时的执行器，避免墙钟校准和毫秒截断缩短重试间隔。
		return taskScheduler.getScheduledExecutor().schedule(
				TaskUtils.decorateTaskWithErrorHandler(new ErrorHandlingRunnable(task), SCHEDULE_ERROR_HANDLER, false),
				delay, timeUnit);
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
	 * 先等待初始延迟，然后执行一次，之后以固定延迟执行任务（以任务结束时间点为基准）
	 * @param task 要执行的任务
	 * @param initialDelay 初始延迟时间
	 * @param delay 固定延迟时间
	 * @param timeUnit 时间单位
	 * @return ScheduledFuture<?> 可用于控制任务的执行
	 */
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit timeUnit) {
		PeriodicTrigger trigger = new PeriodicTrigger(delay, timeUnit);
		// 设置初始延迟
		trigger.setInitialDelay(initialDelay);
		// 关键配置：设置为 false 表示使用固定延迟模式（任务结束后间隔）
		trigger.setFixedRate(false);
		return taskScheduler.schedule(new ErrorHandlingRunnable(task), trigger);
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
}
