package cn.game.core.execute;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.execute.error.ErrorHandler;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.task.SchedulerService;
import cn.game.core.util.AsyncUtils;
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

	// Vertx实例，用于创建Future
	// 这里有点耦合，不过方便初始化
	private final Vertx vertx = VxHolder.vertx;

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
		this.config = config;
		this.mailboxManager = mailboxManager;

		// 设置线程名，不然可能是空字符串
		ThreadFactory factory = Thread.ofVirtual().name("vt-", 0).factory();
		this.executor = Executors.newThreadPerTaskExecutor(factory);

		this.monitor = new ExecutionMonitor(this, config);
		this.monitor.startPeriodicMonitoring(TimeUnit.SECONDS.toMillis(60));

		// 启动定期清理邮箱任务
		startMailboxCleaner();
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
	 * @param priority 任务优先级,默认0，高优先级先执行
	 * @param <T> 结果类型
	 * @return 包含任务结果的Future
	 */
	public <T> Future<T> execute(long entityId, Callable<T> task, boolean fast, String description) {
		return execute(entityId, task, false, description, config.getDefaultTaskTimeoutMs(), ErrorHandler.DISCARD_HANDLER);
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

		// 获取或创建邮箱
		ActorMailbox mailbox = mailboxManager.getOrCreateMailbox(entityId);

		// 将任务添加到邮箱
		boolean offered = mailbox.offerTask(taskWrapper,fast);
		if (!offered) {
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
		return executeAndAwait(entityId, task,false, "Anonymous Task", 0, ErrorHandler.DISCARD_HANDLER);
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
		if (!running.get()) {
			mailbox.setProcessing(false);
			return;
		}

		Runnable processor = new MailboxProcessor(mailbox, this, config);
		// 始终提交到虚拟线程执行器,启用新的虚拟线程执行
		try {
			executor.submit(processor);
		} catch (RejectedExecutionException e) {
			mailbox.setProcessing(false);
			LOGGER.error("Failed to submit mailbox processor", e);
		}
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
			LOGGER.info("Shutting down TaskExecutorService...");

			// 停止邮箱清理任务
			if (mailboxCleanFuture != null) {
				mailboxCleanFuture.cancel(false);
			}

			// 关闭邮箱管理器
			mailboxManager.close();

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
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}

			LOGGER.info("TaskExecutorService shutdown complete");
		}
	}

	/**
	 * 获取Vertx实例
	 * @return Vertx实例
	 */
	public Vertx getVertx() {
		return vertx;
	}

	public BooleanSupplier isShutdown() {
		return () -> !running.get();
	}

	public ConcurrentMap<Long, ActorMailbox> getMailboxes() {
		return mailboxManager.getMailboxes();
	}
}
