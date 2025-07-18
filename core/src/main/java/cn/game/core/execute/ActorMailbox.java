package cn.game.core.execute;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Actor模型的邮箱，用于存储和管理特定实体ID的任务队列
 */
public class ActorMailbox {
    private final long entityId;
	private final Deque<TaskWrapper<?>> taskQueue;
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final MailboxStats stats = new MailboxStats();
    private final int maxQueueSize ;
    private final AtomicReference<Throwable> lastError = new AtomicReference<>(null);
    private final AtomicLong lastAccessTime = new AtomicLong(System.currentTimeMillis());
    
    public ActorMailbox(long entityId, int maxQueueSize) {
        this.entityId = entityId;
		this.maxQueueSize = maxQueueSize <= 0 ? Integer.MAX_VALUE : maxQueueSize;
		// 保持任务的FIFO顺序，有界,两级优先级
		this.taskQueue = new LinkedBlockingDeque<>(this.maxQueueSize);
    }
    
    /**
     * 尝试添加任务到队列尾部
     * @param taskWrapper 要添加的任务包装器
     * @return 如果成功添加返回true，队列已满返回false
     */
    public boolean offerTask(TaskWrapper<?> taskWrapper) {
        return offerTask(taskWrapper, false);
    }
    /**
     * 尝试添加任务到队列
     * @param taskWrapper 要添加的任务包装器
     * @param fast 是否快速添加到队列头部
     * @return 如果成功添加返回true，队列已满返回false
     */
    public boolean offerTask(TaskWrapper<?> taskWrapper,boolean fast) {
    	lastAccessTime.set(System.currentTimeMillis());
    	
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
     * @return 如果成功添加返回true，队列已满返回false
     */
    public boolean offerTaskFirstRetry(TaskWrapper<?> taskWrapper) {
        lastAccessTime.set(System.currentTimeMillis());

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
            lastAccessTime.set(System.currentTimeMillis());
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
     * 设置处理状态
     * @param processing 处理状态
     */
    public void setProcessing(boolean processing) {
        this.processing.set(processing);
    }
    
    /**
     * 尝试将处理状态从预期值设置为新值
     * @param expect 预期的当前值
     * @param update 要设置的新值
     * @return 如果成功则返回true
     */
    public boolean compareAndSetProcessing(boolean expect, boolean update) {
        return this.processing.compareAndSet(expect, update);
    }
    
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

