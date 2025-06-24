package cn.game.core.execute;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.AsyncUtils;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * 任务执行服务，管理所有邮箱并协调任务执行
 */
public class TaskExecutorService implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(TaskExecutorService.class.getName());
    
    // 邮箱映射
    private final ConcurrentMap<Long, ActorMailbox> mailboxes = new ConcurrentHashMap<>();
    
    // 虚拟线程执行器
    private final ExecutorService executor;
    
    // 配置
    private final TaskExecutionConfig config;
    
    // 执行监控
    private final ExecutionMonitor monitor;
    
    // 服务状态
    private final AtomicBoolean running = new AtomicBoolean(true);
    
    // Vertx实例，用于创建Future
	private final Vertx vertx = VxHolder.vertx;
    
	// 单例相关
	private static volatile TaskExecutorService instance;
	private static final Object lock = new Object();

	public TaskExecutorService(TaskExecutionConfig config) {
		this.config = config;
		this.executor = Executors.newVirtualThreadPerTaskExecutor();
		this.monitor = new ExecutionMonitor(this, config);
		this.monitor.startPeriodicMonitoring(TimeUnit.SECONDS.toMillis(30));
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
	 */
	public static void resetInstance(TaskExecutionConfig config) {
		synchronized (lock) {
			if (instance != null) {
				instance.close(); // 关闭旧的
			}
			instance = new TaskExecutorService(config);
		}
	}

    /**
     * 创建任务执行服务（使用默认配置）
     */
	public TaskExecutorService() {
		this(TaskExecutionConfig.getDefault());
    }
    
    /**
     * 执行任务并返回结果Future
     * @param entityId 实体ID
     * @param task 要执行的任务
     * @param <T> 结果类型
     * @return 包含任务结果的Future
     */
    public <T> Future<T> execute(long entityId, Callable<T> task) {
        return execute(entityId, task, "Anonymous Task");
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
        return execute(entityId, task, description, 0, 0);
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
    public <T> Future<T> execute(long entityId, Callable<T> task, String description, int priority) {
        return execute(entityId, task, description, priority, 0);
    }
    
    /**
	 * 执行任务并返回结果Future
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param description 任务描述
	 * @param priority 任务优先级,默认0，高优先级先执行
	 * @param timeoutMs 超时时间（毫秒）
	 * @param <T> 结果类型
	 * @return 包含任务结果的Future
	 */
    public <T> Future<T> execute(long entityId, Callable<T> task, String description, int priority, long timeoutMs) {
        if (!running.get()) {
            return Future.failedFuture(new IllegalStateException("Task executor service is shutting down"));
        }
        
        // 创建任务和结果Promise
        Promise<T> resultPromise = Promise.promise();
        Task<T> wrappedTask = DefaultTask.<T>builder()
            .action(task)
            .description(description)
            .priority(priority)
            .timeoutMs(timeoutMs)
            .build();
        
        TaskWrapper<T> taskWrapper = new TaskWrapper<>(wrappedTask, resultPromise);
        
        // 获取或创建邮箱
        ActorMailbox mailbox = getOrCreateMailbox(entityId);
        
        // 将任务添加到邮箱
        boolean offered = mailbox.offerTask(taskWrapper);
        if (!offered) {
            return Future.failedFuture(new QueueFullException("Task queue is full for entity " + entityId));
        }
        
        // 尝试启动处理器
        if (mailbox.compareAndSetProcessing(false, true)) {
            submitProcessor(mailbox);
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
		return executeAndAwait(entityId, task, 0);
    }
    
    /**
	 * 在虚拟线程中执行任务并等待结果（同步方法）
	 * @param entityId 实体ID
	 * @param task 要执行的任务
	 * @param priority 任务优先级,默认0，高优先级先执行
	 * @param <T> 结果类型
	 * @return 任务结果
	 * @throws Exception 如果任务执行失败
	 */
	public <T> T executeAndAwait(long entityId, Callable<T> task, int priority) throws Exception {
		return executeAndAwait(entityId, task, "Anonymous Task", priority, 0);
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
    public <T> T executeAndAwait(long entityId, Callable<T> task, String description, int priority, long timeoutMs) throws Exception {
		// 不能在eventloop中执行
		AsyncUtils.checkEventLoop();
		Future<T> future = execute(entityId, task, description, priority, timeoutMs);
		return AsyncUtils.await(future, timeoutMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 提交任务但不返回结果
     * @param entityId 实体ID
     * @param task 任务
     * @return 如果成功提交返回true
     */
    public boolean submitTask(long entityId, Runnable task) {
        return submitTask(entityId, task, "Anonymous Task");
    }
    
    /**
     * 提交任务但不返回结果
     * @param entityId 实体ID
     * @param task 任务
     * @param description 任务描述
     * @return 如果成功提交返回true
     */
    public boolean submitTask(long entityId, Runnable task, String description) {
        try {
            execute(entityId, () -> {
                task.run();
                return null;
            }, description);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to submit task: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 提交邮箱处理器
     * @param mailbox 要处理的邮箱
     */
    void submitProcessor(ActorMailbox mailbox) {
        if (!running.get()) {
            mailbox.setProcessing(false);
            return;
        }
        
		Runnable processor = new MailboxProcessor(mailbox, this, config);
		if (Thread.currentThread().isVirtual()) {
			// 已经在虚拟线程里，直接运行
			processor.run();
		} else {
			// 提交到虚拟线程执行器,启用新的虚拟线程执行
			try {
				executor.submit(processor);
			} catch (RejectedExecutionException e) {
				mailbox.setProcessing(false);
				LOGGER.log(Level.SEVERE, "Failed to submit mailbox processor", e);
			}
        }
    }
    
    /**
     * 获取或创建邮箱
     * @param entityId 实体ID
     * @return 邮箱
     */
    private ActorMailbox getOrCreateMailbox(long entityId) {
        return mailboxes.computeIfAbsent(entityId, 
            id -> new ActorMailbox(id, config.getMaxQueueSize()));
    }
    
    /**
     * 获取邮箱
     * @param entityId 实体ID
     * @return 邮箱，如果不存在返回null
     */
    public ActorMailbox getMailbox(long entityId) {
        return mailboxes.get(entityId);
    }
    
    /**
     * 移除邮箱
     * @param entityId 实体ID
     * @return 如果邮箱存在并被移除返回true
     */
    public boolean removeMailbox(long entityId) {
        ActorMailbox mailbox = mailboxes.get(entityId);
        if (mailbox != null && mailbox.isEmpty() && !mailbox.isProcessing()) {
            return mailboxes.remove(entityId) != null;
        }
        return false;
    }
    
    /**
     * 获取所有邮箱
     * @return 邮箱映射
     */
    ConcurrentMap<Long, ActorMailbox> getMailboxes() {
        return mailboxes;
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
            
            // 等待所有任务完成或超时
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        LOGGER.severe("Executor did not terminate");
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
}

