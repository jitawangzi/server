package cn.game.core.execute;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.execute.error.ErrorHandler;
import cn.game.core.execute.error.ErrorPolicy;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.task.SchedulerService;
import cn.game.core.util.AsyncUtils;
import cn.game.util.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * 任务执行服务，管理所有邮箱并协调任务执行
 * 支持虚拟线程，提供同步接口使用能力
 * 跨ID的逻辑，可能有死锁风险，需要用异步api，不能用同步等待返回结果。
 * 除非非常肯定不会产生死锁
 */
public class TaskExecutorService implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorService.class);

	// 邮箱管理器，可以是OneToOneMailboxManager或SharedMailboxManager
	private final MailboxManager mailboxManager;

	// 虚拟线程执行器
	private final ExecutorService executor;

	// 配置
	private final TaskExecutionConfig config;

	// 执行监控
	private final ExecutionMonitor monitor;

	// 服务状态
	private final AtomicBoolean running = new AtomicBoolean(true);

	/** 延迟重试的调度入口；测试可使用自有调度器，生产仍使用既有调度服务。 */
	private final RetryScheduler retryScheduler;

	/** 只提交延迟动作，不在调用线程等待或执行该动作。 */
	@FunctionalInterface
	interface RetryScheduler {
		/**
		 * 安排延迟动作，拒绝时向调用方抛出异常。
		 * @param action 到期后执行的动作
		 * @param delay 等待时长
		 * @param unit 等待时长的单位
		 */
		void schedule(Runnable action, long delay, TimeUnit unit);
	}

	// 邮箱定期清理参数
	private static final long MAILBOX_IDLE_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(30); // 邮箱最大空闲时间
	private static final long MAILBOX_CLEAN_INTERVAL_MS = TimeUnit.MINUTES.toMillis(10); // 清理周期

	// 邮箱清理任务Future
	private ScheduledFuture<?> mailboxCleanFuture;

	// 单例相关
	private static volatile TaskExecutorService instance;
	private static final Object lock = new Object();

	/**
	 * 使用指定配置和邮箱管理器创建任务执行服务
	 * @param config 任务执行配置
	 * @param mailboxManager 邮箱管理器
	 */
	public TaskExecutorService(TaskExecutionConfig config, MailboxManager mailboxManager) {
		this(config, mailboxManager,
				Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("vt-", 0).factory()),
				(action, delay, unit) -> SchedulerService.getInstance().scheduleTask(action, delay, unit),
				DateUtil::currentTimeMillis, true);
	}

	/**
	 * 使用隔离资源创建服务，供确定性时序测试使用，不启动全局定时任务。
	 * @param config 执行配置
	 * @param mailboxManager 本服务的邮箱管理器
	 * @param executor 本服务负责关闭的执行器
	 * @param retryScheduler 由调用者管理的重试调度器
	 * @param clock 监控使用的毫秒时钟
	 */
	TaskExecutorService(TaskExecutionConfig config, MailboxManager mailboxManager, ExecutorService executor,
			RetryScheduler retryScheduler, LongSupplier clock) {
		this(config, mailboxManager, executor, retryScheduler, clock, false);
	}

	/**
	 * 统一装配资源，业务任务始终在执行器中运行。
	 * @param config 执行配置
	 * @param mailboxManager 邮箱管理器
	 * @param executor 本服务负责关闭的执行器
	 * @param retryScheduler 延迟调度入口
	 * @param clock 监控的毫秒时钟
	 * @param background 是否启动生产监控和空闲清理
	 */
	private TaskExecutorService(TaskExecutionConfig config, MailboxManager mailboxManager, ExecutorService executor,
			RetryScheduler retryScheduler, LongSupplier clock, boolean background) {
		this.config = config;
		this.mailboxManager = mailboxManager;
		this.executor = executor;
		this.retryScheduler = retryScheduler;
		this.monitor = new ExecutionMonitor(this, config, clock);
		if (background) {
			this.monitor.startPeriodicMonitoring(TimeUnit.SECONDS.toMillis(60));
			startMailboxCleaner();
		}
	}

	/**
	 * 使用指定配置创建任务执行服务，默认使用一对一邮箱管理器
	 * @param config 任务执行配置
	 */
	public TaskExecutorService(TaskExecutionConfig config) {
		this(config, new OneToOneMailboxManager(config.getMaxQueueSize()));
	}

	/**
	 * 创建任务执行服务（使用默认配置）
	 */
	public TaskExecutorService() {
		this(TaskExecutionConfig.getDefault());
	}

	/**
	 * 启动定期邮箱清理任务
	 */
	private void startMailboxCleaner() {
		this.mailboxCleanFuture = SchedulerService.getInstance()
				.scheduleAtFixedRate(this::cleanIdleMailboxes, MAILBOX_CLEAN_INTERVAL_MS, TimeUnit.MILLISECONDS);
		LOGGER.info("Scheduled mailbox idle cleaner: every {} ms, idle timeout {} ms", MAILBOX_CLEAN_INTERVAL_MS, MAILBOX_IDLE_TIMEOUT_MS);
	}

	/**
	 * 定期清理长时间未活跃且队列为空的邮箱
	 */
	private void cleanIdleMailboxes() {
		mailboxManager.cleanIdleMailboxes(MAILBOX_IDLE_TIMEOUT_MS);
	}

	/**
	 * 获取全局单例（自定义参数）
	 * @return 单例
	 */
	public static TaskExecutorService getInstance() {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new TaskExecutorService(TaskExecutionConfig.getDefault());
				}
			}
		}
		return instance;
	}

	/**
	 * 允许重置单例（重设参数），线程安全
	 * @param config 配置
	 * @param mailboxManager 邮箱管理器
	 */
	public static void resetInstance(TaskExecutionConfig config, MailboxManager mailboxManager) {
		synchronized (lock) {
			if (instance != null) {
				instance.close(); // 关闭旧的
			}
			instance = new TaskExecutorService(config, mailboxManager);
		}
	}

	/**
	 * 允许重置单例（重设参数），线程安全，使用默认的一对一邮箱管理器
	 * @param config 配置
	 */
	public static void resetInstance(TaskExecutionConfig config) {
		resetInstance(config, new OneToOneMailboxManager(config.getMaxQueueSize()));
	}

	/**
	 * 执行任务并返回结果Future
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param <T> 结果类型
	 * @return 包含任务结果的Future
	 */
	public <T> Future<T> execute(long entityId, Callable<T> task) {
		return execute(entityId, task, false, "Anonymous Task", config.getDefaultTaskTimeoutMs(), ErrorHandler.DISCARD_HANDLER);
	}

	/**
	 * 执行任务并返回结果Future
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param description 任务描述
	 * @param <T> 结果类型
	 * @return 包含任务结果的Future
	 */
	public <T> Future<T> execute(long entityId, Callable<T> task, String description) {
		return execute(entityId, task, false, description, config.getDefaultTaskTimeoutMs(), ErrorHandler.DISCARD_HANDLER);
	}

	/**
	 * 执行任务并返回结果Future
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param description 任务描述
	 * @param fast 是否插入队列头部
	 * @param <T> 结果类型
	 * @return 包含任务结果的Future
	 */
	public <T> Future<T> execute(long entityId, Callable<T> task, boolean fast, String description) {
		return execute(entityId, task, fast, description, config.getDefaultTaskTimeoutMs(), ErrorHandler.DISCARD_HANDLER);
	}

	/**
	 * 执行任务并返回结果Future
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param description 任务描述
	 * @param fast 任务优先级,是否需要快速执行
	 * @param timeoutMs 超时时间（毫秒）
	 * @param <T> 结果类型
	 * @return 包含任务结果的Future
	 */
	public <T> Future<T> execute(long entityId, Callable<T> task, boolean fast, String description, long timeoutMs) {
		return execute(entityId, task, fast, description, timeoutMs, ErrorHandler.DISCARD_HANDLER);
	}

	/**
	 * 执行任务并返回结果Future
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param description 任务描述
	 * @param fast 是否需要快速执行
	 * @param timeoutMs 超时时间（毫秒）
	 * @param errorHandler 错误处理策略
	 * @param <T> 结果类型
	 * @return 包含任务结果的Future
	 */
	public <T> Future<T> execute(long entityId, Callable<T> task, boolean fast, String description, long timeoutMs, ErrorHandler errorHandler) {
		if (!running.get()) {
			return Future.failedFuture(new IllegalStateException("Task executor service is shutting down"));
		}
		// 例外：entityId==0，直接并发执行
		/*	if (entityId == 0) {
				Promise<T> resultPromise = Promise.promise();
				try {
					executor.submit(() -> {
						try {
							T result = task.call();
							resultPromise.complete(result);
						} catch (Throwable e) {
							// 对于并发任务，不应用重试逻辑，直接失败
							resultPromise.fail(e);
						}
					});
				} catch (RejectedExecutionException e) {
					resultPromise.fail(e);
				}
				if (timeoutMs > 0) {
					return resultPromise.future().timeout(timeoutMs, TimeUnit.MILLISECONDS);
				}
				return resultPromise.future();
			}*/

		// 正常邮箱串行逻辑
		// 创建任务和结果Promise
		Promise<T> resultPromise = Promise.promise();
		Task<T> wrappedTask = DefaultTask.<T>builder()
				.action(task)
				.description(description)
				.fast(fast)
				.timeoutMs(timeoutMs)
				.errorHandler(errorHandler)
				.build();

		TaskWrapper<T> taskWrapper = new TaskWrapper<>(wrappedTask, resultPromise);

		// 获取与入队在实际邮箱键下协调，执行消费者放在协调区域外。
		ActorMailbox mailbox = mailboxManager.enqueueTask(entityId, taskWrapper, fast);
		if (mailbox == null) {
			return Future.failedFuture(new QueueFullException("Task queue is full for entity " + entityId));
		}

		// 尝试启动处理器
		if (mailbox.compareAndSetProcessing(false, true)) {
			submitProcessor(mailbox);
		}

		// 使用 Vert.x 的超时机制
		if (timeoutMs > 0) {
			return resultPromise.future().timeout(timeoutMs, TimeUnit.MILLISECONDS);
		}
		return resultPromise.future();
	}


	/**
	 * 在虚拟线程中执行任务并等待结果（同步方法）
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param <T> 结果类型
	 * @return 任务结果
	 * @throws Exception 如果任务执行失败
	 */
	public <T> T executeAndAwait(long entityId, Callable<T> task) throws Exception {
		return executeAndAwait(entityId, task,false, "Anonymous Task", 0, ErrorHandler.DISCARD_HANDLER);
	}

	/**
	 * 在虚拟线程中执行任务并等待结果（同步方法）
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param fast 任务优先级,是否需要快速执行
	 * @param <T> 结果类型
	 * @return 任务结果
	 * @throws Exception 如果任务执行失败
	 */
	public <T> T executeAndAwait(long entityId, Callable<T> task,boolean fast) throws Exception {
		return executeAndAwait(entityId, task,fast, "Anonymous Task", 0, ErrorHandler.DISCARD_HANDLER);
	}

	/**
	 * 在虚拟线程中执行任务并等待结果（同步方法）
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param description 任务描述
	 * @param priority 任务优先级,默认0，高优先级先执行
	 * @param timeoutMs 超时时间（毫秒）
	 * @param <T> 结果类型
	 * @return 任务结果
	 * @throws Exception 如果任务执行失败
	 */
	public <T> T executeAndAwait(long entityId, Callable<T> task, boolean fast, String description, long timeoutMs) throws Exception {
		return executeAndAwait(entityId, task, fast, description, timeoutMs, ErrorHandler.DISCARD_HANDLER);
	}

	/**
	 * 在虚拟线程中执行任务并等待结果（同步方法）
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param description 任务描述
	 * @param priority 任务优先级,默认0，高优先级先执行
	 * @param timeoutMs 超时时间（毫秒）
	 * @param errorHandler 错误处理策略
	 * @param <T> 结果类型
	 * @return 任务结果
	 * @throws Exception 如果任务执行失败
	 */
	public <T> T executeAndAwait(long entityId, Callable<T> task, boolean fast, String description, long timeoutMs, ErrorHandler errorHandler) throws Exception {
		// 不能在eventloop中执行
		AsyncUtils.checkEventLoop();

		// 检查死锁风险
		DeadlockGuard.checkCrossIdSyncWait(entityId, description);

		Future<T> future = execute(entityId, task, fast, description, timeoutMs, errorHandler);
		return AsyncUtils.await(future, timeoutMs, TimeUnit.MILLISECONDS);
	}

	/**
	 * 提交任务但不返回结果
	 * @param entityId 实体ID
	 * @param task 任务
	 * @return 如果成功提交返回true
	 */
	public boolean submitTask(long entityId, Runnable task) {
		return submitTask(entityId, task,false, "Anonymous Task", ErrorHandler.DISCARD_HANDLER);
	}
	public boolean submitTask(long entityId, Runnable task,boolean fast) {
		return submitTask(entityId, task,fast, "Anonymous Task", ErrorHandler.DISCARD_HANDLER);
	}

	/**
	 * 提交任务但不返回结果
	 * @param entityId 实体ID
	 * @param task 任务
	 * @param description 任务描述
	 * @return 如果成功提交返回true
	 */
	public boolean submitTask(long entityId, Runnable task, String description) {
		return submitTask(entityId, task,false, description, ErrorHandler.DISCARD_HANDLER);
	}

	/**
	 * 提交任务但不返回结果
	 * @param entityId 实体ID
	 * @param task 任务
	 * @param fast 是否需要快速执行这个任务
	 * @param description 任务描述
	 * @param errorHandler 错误处理策略
	 * @return 如果成功提交返回true
	 */
	public boolean submitTask(long entityId, Runnable task,boolean fast, String description, ErrorHandler errorHandler) {
		try {
			execute(entityId, () -> {
				task.run();
				return null;
			},fast, description, config.getDefaultTaskTimeoutMs(), errorHandler);
			return true;
		} catch (Exception e) {
			LOGGER.error("Failed to submit task: " + e.getMessage(), e);
			return false;
		}
	}


	/**
	 * 提交邮箱处理器
	 * @param mailbox 要处理的邮箱
	 */
	public void submitProcessor(ActorMailbox mailbox) {
		submitProcessor(mailbox, mailbox.processingGeneration());
	}

	/**
	 * 只提交仍持有消费权的代次；拒绝时完成未开始任务，不伪报运行任务已取消。
	 * @param mailbox 待运行邮箱
	 * @param generation 调度时持有的消费权代次
	 */
	private void submitProcessor(ActorMailbox mailbox, long generation) {
		if (!mailbox.ownsProcessing(generation)) { return; }
		if (!running.get()) {
			rejectProcessor(mailbox, generation, new RejectedExecutionException("Task executor service is shutting down"));
			return;
		}
		try {
			executor.execute(new MailboxProcessor(mailbox, this, config, generation));
		} catch (RejectedExecutionException e) {
			LOGGER.error("Failed to submit mailbox processor", e);
			rejectProcessor(mailbox, generation, e);
		}
	}

	/**
	 * 消费者退出后的唯一调度入口，延迟等待期间保留消费权并携带新代次。
	 * @param mailbox 已结束本次执行的邮箱
	 * @param generation 本次消费者代次
	 * @param delayedPolicy 延迟策略；空值表示正常释放后检查新入队任务
	 */
	void finishProcessor(ActorMailbox mailbox, long generation, ErrorPolicy delayedPolicy) {
		long delayedGeneration = mailbox.finishProcessing(generation, delayedPolicy != null);
		if (delayedGeneration != 0) {
			if (!running.get()) {
				rejectProcessor(mailbox, delayedGeneration, new RejectedExecutionException("Task executor service is shutting down"));
				return;
			}
			try {
				retryScheduler.schedule(() -> submitProcessor(mailbox, delayedGeneration),
						delayedPolicy.getDelay(), delayedPolicy.getTimeUnit());
			} catch (RuntimeException e) {
				LOGGER.error("Failed to schedule mailbox retry for {}", mailbox.getEntityId(), e);
				rejectProcessor(mailbox, delayedGeneration, e);
			}
		} else if (!mailbox.isEmpty() && mailbox.compareAndSetProcessing(false, true)) {
			submitProcessor(mailbox);
		}
	}

	/**
	 * 拒绝未开始的消费者，锁外完成结果，避免业务回调在邮箱协调区域内执行。
	 * @param mailbox 对应邮箱
	 * @param generation 被拒绝的代次
	 * @param cause 拒绝原因
	 */
	private void rejectProcessor(ActorMailbox mailbox, long generation, Throwable cause) {
		for (TaskWrapper<?> pending : mailbox.drainPending(generation, false)) { pending.fail(cause); }
		mailbox.setLastError(cause);
	}

	/**
	 * 获取邮箱管理器
	 * @return 邮箱管理器
	 */
	public MailboxManager getMailboxManager() {
		return mailboxManager;
	}

	/**
	 * 获取邮箱
	 * @param entityId 实体ID
	 * @return 邮箱，如果不存在返回null
	 */
	public ActorMailbox getMailbox(long entityId) {
		return mailboxManager.getMailbox(entityId);
	}

	/**
	 * 移除邮箱
	 * @param entityId 实体ID
	 * @return 如果邮箱存在并被移除返回true
	 */
	public boolean removeMailbox(long entityId) {
		return mailboxManager.removeMailbox(entityId);
	}

	/**
	 * 获取监控器
	 * @return 执行监控器
	 */
	public ExecutionMonitor getMonitor() {
		return monitor;
	}

	/**
	 * 关闭执行服务
	 */
	@Override
	public void close() {
		if (running.compareAndSet(true, false)) {
			LOGGER.info("Shutting down TaskExecutorService..." + Thread.currentThread().getName());
			monitor.stopPeriodicMonitoring();

			// 停止邮箱清理任务
			if (mailboxCleanFuture != null) {
				mailboxCleanFuture.cancel(false);
			}

			// 未开始任务明确失败；正在执行的任务仍由真实执行结果收尾。
			RejectedExecutionException closed = new RejectedExecutionException("Task executor service is shutting down");
			for (ActorMailbox mailbox : mailboxManager.getMailboxes().values()) {
				for (TaskWrapper<?> pending : mailbox.drainPending(0, true)) { pending.fail(closed); }
			}

			// 等待所有任务完成或超时
			executor.shutdown();
			try {
				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
					executor.shutdownNow();
					if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
						LOGGER.error("Executor did not terminate");
					}
				}
			} catch (InterruptedException e) {
				LOGGER.warn("Interrupted while waiting for mailbox consumers to stop", e);
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
			mailboxManager.close();

			LOGGER.info("TaskExecutorService shutdown complete " + Thread.currentThread().getName());
		}
	}

	/**
	 * 获取Vertx实例
	 * @return Vertx实例
	 */
	public Vertx getVertx() {
		return VxHolder.vertx;
	}

	public BooleanSupplier isShutdown() {
		return () -> !running.get();
	}

	public ConcurrentMap<Long, ActorMailbox> getMailboxes() {
		return mailboxManager.getMailboxes();
	}
}
