package cn.game.core.execute;

import java.util.Deque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

import cn.game.util.DateUtil;

/**
 * 同一实际邮箱的入队、退役和消费权代次在实例锁下协调，业务任务始终在锁外执行。
 * 已退役实例拒绝新任务；延迟回调携带消费权代次，不能复活已经完成的消费者。
 */
public class ActorMailbox {
    private final long entityId;
	private final Deque<TaskWrapper<?>> taskQueue;
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final MailboxStats stats = new MailboxStats();
    private final int maxQueueSize ;
    private final AtomicReference<Throwable> lastError = new AtomicReference<>(null);
    /** 用于空闲时间和阻塞诊断的毫秒时钟。 */
    private final LongSupplier clock;
    /** 最近一次队列访问时间，单位为毫秒。 */
    private final AtomicLong lastAccessTime;
    /** 已退役实例不能再次入队或获取消费权。 */
    private boolean retired;
    /** 每次取得、交接或释放消费权时推进的代次。 */
    private long processingGeneration;
    /** 实际执行线程；延迟等待期间为空。 */
    private volatile Thread processingThread;
    /** 当前消费者是否正处于单次任务处理区间；与开始时间在实例锁下协调。 */
    private boolean taskExecuting;
    /** 当前任务开始的毫秒时间，零也是合法时钟值，不用作空闲标记。 */
    private long taskStartedAt;
    
    /**
     * 使用项目时间源创建邮箱。
     * @param entityId 实际邮箱 ID
     * @param maxQueueSize 队列容量，非正数表示不限容量
     */
    public ActorMailbox(long entityId, int maxQueueSize) {
        this(entityId, maxQueueSize, DateUtil::currentTimeMillis);
    }

    /**
     * 为管理器创建带统一时钟的邮箱。
     * @param entityId 已完成映射的实际邮箱 ID
     * @param maxQueueSize 队列容量，非正数保持既有不限容量语义
     * @param clock 返回毫秒时间的时钟
     */
    ActorMailbox(long entityId, int maxQueueSize, LongSupplier clock) {
        this.entityId = entityId;
        this.clock = clock;
        this.lastAccessTime = new AtomicLong(clock.getAsLong());
		this.maxQueueSize = maxQueueSize <= 0 ? Integer.MAX_VALUE : maxQueueSize;
		// 保持任务的FIFO顺序，有界,两级优先级
		this.taskQueue = new LinkedBlockingDeque<>(this.maxQueueSize);
    }
    
    /**
     * 尝试添加任务到队列尾部
     * @param taskWrapper 要添加的任务包装器
     * @return 入队成功为 true；队列满或实例已退役为 false
     */
    public boolean offerTask(TaskWrapper<?> taskWrapper) {
        return offerTask(taskWrapper, false);
    }
    /**
     * 尝试添加任务到队列
     * @param taskWrapper 要添加的任务包装器
     * @param fast 是否快速添加到队列头部
     * @return 入队成功为 true；队列满或实例已退役为 false
     */
    public synchronized boolean offerTask(TaskWrapper<?> taskWrapper,boolean fast) {
        if (retired) { return false; }
        lastAccessTime.set(clock.getAsLong());
    	
    	if (taskQueue.size() >= maxQueueSize) {
    		stats.rejectedTasks.incrementAndGet();
    		return false;
    	}
    	
    	boolean result = fast?taskQueue.offerFirst(taskWrapper): taskQueue.offerLast(taskWrapper);
    	if (result) {
    		stats.submittedTasks.incrementAndGet();
    	}
    	return result;
    }
    
    /**
     * 尝试添加任务到队列头部 (用于重试)
     * @param taskWrapper 要添加的任务包装器
     * @return 入队成功为 true；队列满或实例已退役为 false
     */
    public synchronized boolean offerTaskFirstRetry(TaskWrapper<?> taskWrapper) {
        if (retired) { return false; }
        lastAccessTime.set(clock.getAsLong());

        if (taskQueue.size() >= maxQueueSize) {
            stats.rejectedTasks.incrementAndGet();
            // 即使是重试，如果队列满了也只能拒绝
            return false;
        }

        // 重试的任务不计入 submittedTasks
        return taskQueue.offerFirst(taskWrapper);
    }

    /**
     * 获取并移除队列头部的任务
     * @return 队列头部的任务，如果队列为空则返回null
     */
    public TaskWrapper<?> pollTask() {
		TaskWrapper<?> task = taskQueue.pollFirst(); // 从头部获取
        if (task != null) {
            lastAccessTime.set(clock.getAsLong());
        }
        return task;
    }
    
    /**
     * 检查队列是否为空
     * @return 如果队列为空返回true
     */
    public boolean isEmpty() {
        return taskQueue.isEmpty();
    }
    
    /**
     * 兼容的消费权入口；不得从其他线程释放正在运行的消费者，退役实例不再变更。
     * @param processing 处理状态
     */
    public void setProcessing(boolean processing) {
        compareAndSetProcessing(!processing, processing);
    }
    
    /**
     * 尝试将处理状态从预期值设置为新值
     * @param expect 预期的当前值
     * @param update 要设置的新值
     * @return 状态匹配且允许转换时为 true；不能夺走其他运行线程的消费权
     */
    public synchronized boolean compareAndSetProcessing(boolean expect, boolean update) {
        if (retired || processing.get() != expect) { return false; }
        if (!update && processingThread != null && processingThread != Thread.currentThread()) { return false; }
        if (expect != update) {
            processingGeneration++;
            if (!update) { processingThread = null; taskExecuting = false; }
        }
        processing.set(update);
        return true;
    }

    /** @return 当前消费权代次，仅供处理器捕获和校验。 */
    synchronized long processingGeneration() { return processingGeneration; }

    /**
     * 绑定本代次的实际消费者，拒绝重复提交和过期回调。
     * @param generation 调度时捕获的消费权代次
     * @return 是否取得本次实际执行权
     */
    synchronized boolean enterProcessing(long generation) {
        if (!ownsProcessing(generation) || processingThread != null) { return false; }
        processingThread = Thread.currentThread();
        return true;
    }

    /**
     * 结束本次执行；延迟重试保留消费权但更换代次，阻止新入队提前唤醒。
     * @param generation 当前消费者代次
     * @param delayed 是否需要等待延迟重试
     * @return 延迟回调的新代次；零表示已释放消费权
     */
    synchronized long finishProcessing(long generation, boolean delayed) {
        if (!ownsProcessing(generation) || processingThread != Thread.currentThread()) { return 0; }
        taskExecuting = false;
        processingThread = null;
        processingGeneration++;
        if (delayed && !taskQueue.isEmpty()) { return processingGeneration; }
        processing.set(false);
        return 0;
    }

    /**
     * 检查调度动作仍持有有效代次。
     * @param generation 调度动作捕获的代次
     * @return 该动作是否仍有消费权
     */
    synchronized boolean ownsProcessing(long generation) {
        return !retired && processing.get() && processingGeneration == generation;
    }

    /**
     * 取出未开始的任务，调用者必须在实例锁外完成其 Promise。
     * @param generation 被拒绝的消费权代次；关停时不以此限制
     * @param shutdown 是否为停止受理后的全队列收尾
     * @return 需要明确失败的未执行任务，不包括正在执行的任务
     */
    synchronized List<TaskWrapper<?>> drainPending(long generation, boolean shutdown) {
        if (!shutdown && (!ownsProcessing(generation) || processingThread != null)) { return List.of(); }
        List<TaskWrapper<?>> pending = new ArrayList<>();
        TaskWrapper<?> task;
        while ((task = taskQueue.pollFirst()) != null) { pending.add(task); }
        if (processingThread == null) {
            taskExecuting = false;
            processing.set(false);
            processingGeneration++;
        }
        return pending;
    }

    /**
     * 在管理器的实际邮箱键协调区域中退役空闲实例。
     * @param now 当前毫秒时间
     * @param idleTimeoutMs 空闲门槛；负数表示显式移除时不检查时长
     * @return 是否已标记退役
     */
    synchronized boolean retireIfIdle(long now, long idleTimeoutMs) {
        if (retired || processing.get() || !taskQueue.isEmpty()) { return false; }
        if (idleTimeoutMs >= 0 && now - lastAccessTime.get() <= idleTimeoutMs) { return false; }
        retired = true;
        processingGeneration++;
        return true;
    }

    /** @param generation 只有当前代次的实际消费者可以记录本次任务开始，不改变消费权 */
    synchronized void beginTaskExecution(long generation) {
        if (ownsProcessing(generation) && processingThread == Thread.currentThread()) {
            taskStartedAt = clock.getAsLong();
            taskExecuting = true;
        }
    }

    /** @param generation 清除本次任务记录，旧代次或其他线程不得结束新任务的诊断区间 */
    synchronized void endTaskExecution(long generation) {
        if (ownsProcessing(generation) && processingThread == Thread.currentThread()) { taskExecuting = false; }
    }

    /** @return 同一次采样的线程和任务开始时间，空闲及延迟重试等待返回空值 */
    synchronized TaskExecutionSnapshot taskExecutionSnapshot() {
        return taskExecuting && processingThread != null ? new TaskExecutionSnapshot(processingThread, taskStartedAt) : null;
    }

    /** @param thread 采样时的执行线程 @param startedAt 同一活动任务的毫秒开始时间 */
    record TaskExecutionSnapshot(Thread thread, long startedAt) {}
    
    /**
     * 获取当前处理状态
     * @return 当前处理状态
     */
    public boolean isProcessing() {
        return processing.get();
    }
    
    /**
     * 获取实体ID
     * @return 实体ID
     */
    public long getEntityId() {
        return entityId;
    }
    
    /**
     * 获取邮箱统计信息
     * @return 统计信息
     */
    public MailboxStats getStats() {
        return stats;
    }
    
    /**
     * 设置最后发生的错误
     * @param error 错误信息
     */
    public void setLastError(Throwable error) {
        lastError.set(error);
    }
    
    /**
     * 获取最后发生的错误
     * @return 错误信息
     */
    public Throwable getLastError() {
        return lastError.get();
    }
    
    /**
     * 获取最后访问时间
     * @return 最后访问时间（毫秒时间戳）
     */
    public long getLastAccessTime() {
        return lastAccessTime.get();
    }
    
    /**
     * 获取当前队列大小
     * @return 队列大小
     */
    public int getQueueSize() {
        return taskQueue.size();
    }
    
    /**
     * 获取最大队列大小
     * @return 最大队列大小
     */
    public int getMaxQueueSize() {
        return maxQueueSize;
    }
}

