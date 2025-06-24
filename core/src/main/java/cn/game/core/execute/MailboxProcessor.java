package cn.game.core.execute;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 邮箱处理器，负责处理单个邮箱中的任务
 */
public class MailboxProcessor implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MailboxProcessor.class.getName());
    
    private final ActorMailbox mailbox;
    private final TaskExecutorService executorService;
    private final TaskExecutionConfig config;
    
    public MailboxProcessor(ActorMailbox mailbox, TaskExecutorService executorService, TaskExecutionConfig config) {
        this.mailbox = mailbox;
        this.executorService = executorService;
        this.config = config;
    }
    
    @Override
    public void run() {
        long entityId = mailbox.getEntityId();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing mailbox for entity " + entityId);
		}
        try {
            processMailboxTasks();
        } finally {
            // 在处理完成后，检查队列并重置处理状态
            if (mailbox.isEmpty()) {
                // 队列为空，将处理标志设置为false
                mailbox.setProcessing(false);
                
                // 二次检查：确保在设置标志后没有新任务添加
                if (!mailbox.isEmpty() && mailbox.compareAndSetProcessing(false, true)) {
                    // 有新任务，再次提交处理器
                    executorService.submitProcessor(mailbox);
                }
            } else {
                // 队列未空，继续处理
                executorService.submitProcessor(mailbox);
            }
            
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Finished processing mailbox for entity {}", entityId);
			}
        }
    }
    
    /**
     * 处理邮箱中的所有任务
     */
    private void processMailboxTasks() {
        TaskWrapper<?> taskWrapper;
        while ((taskWrapper = mailbox.pollTask()) != null) {
            processTask(taskWrapper);
        }
    }
    
    /**
     * 处理单个任务
     * @param taskWrapper 要处理的任务包装器
     */
    private <T> void processTask(TaskWrapper<T> taskWrapper) {
        Task<T> task = taskWrapper.getTask();
        long entityId = mailbox.getEntityId();
        String taskDescription = task.getDescription();
        long startTime = System.nanoTime();
        
        try {
            // 确定任务超时时间
            long timeoutMs = task.getTimeoutMs() > 0 ? task.getTimeoutMs() : config.getDefaultTaskTimeoutMs();
            
            // 使用CompletableFuture执行任务并支持超时
            T result;
            if (timeoutMs > 0) {
                result = executeWithTimeout(task, timeoutMs);
            } else {
                // 不需要超时，直接执行
                result = task.execute();
            }
            
            // 任务成功完成，设置结果
            taskWrapper.complete(result);
            mailbox.getStats().completedTasks.incrementAndGet();
            
        } catch (TimeoutException e) {
            // 任务超时
            mailbox.getStats().timeoutTasks.incrementAndGet();
            TaskTimeoutException timeoutException = new TaskTimeoutException(entityId, taskDescription, task.getTimeoutMs());
            mailbox.setLastError(timeoutException);
            taskWrapper.fail(timeoutException);
			LOGGER.warn("Task timeout for entity " + entityId + ": " + taskDescription);
            
        } catch (Exception e) {
            // 任务执行失败
            mailbox.getStats().failedTasks.incrementAndGet();
            TaskExecutionException executionException = new TaskExecutionException(
                "Task execution failed: " + e.getMessage(), 
                e, entityId, taskDescription
            );
            mailbox.setLastError(executionException);
            taskWrapper.fail(executionException);
			LOGGER.warn("Task execution failed for entity " + entityId + ": " + taskDescription, e);
            
        } finally {
            // 记录执行时间统计
            long duration = System.nanoTime() - startTime;
            mailbox.getStats().recordProcessingTime(duration, taskDescription);
            
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Task {} for entity {} processed in {} ns", taskDescription, entityId, duration);
			}
        }
    }
    
    /**
     * 使用超时机制执行任务
     * @param task 要执行的任务
     * @param timeoutMs 超时时间（毫秒）
     * @throws Exception 如果任务执行失败或超时
     */
    private <T> T executeWithTimeout(Task<T> task, long timeoutMs) throws Exception {
		if (timeoutMs > 0) {
			// 当前应该在虚拟线程，使用Future包装任务，并在当前线程等待
			CompletableFuture<T> future = new CompletableFuture<>();
            try {
				T result = task.execute();
				future.complete(result);
            } catch (Exception e) {
				future.completeExceptionally(e);
            }
			try {
				return future.get(timeoutMs, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				// 注意：无法中断已经在当前线程执行的task
				throw e;
            }
		} else {
			return task.execute();
        }
    }
}

