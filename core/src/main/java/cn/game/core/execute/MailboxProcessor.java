package cn.game.core.execute;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.execute.error.ErrorPolicy;
import cn.game.core.execute.error.RetryContext;
import cn.game.core.task.SchedulerService;

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
		if (mailbox.isEmpty()) {
			return;
		}
		long entityId = mailbox.getEntityId();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start processing mailbox for entity {}", entityId);
		}
		DeadlockGuard.enter(mailbox.getEntityId());

		ErrorPolicy delayedRetryPolicy = null;

		try {
			// 处理循环现在在需要延迟重试时，会返回一个策略。
			delayedRetryPolicy = processMailboxTasks();
		} catch (Exception e) {
			// 在此级别捕获异常是一种安全措施，以防万一。
			// 单个任务的异常应在 processTask 内部处理。
			LOGGER.error("Uncaught exception occurred while processing mailbox for entity {}", entityId, e);
			mailbox.setProcessing(false);
		} finally {
			if (delayedRetryPolicy != null) {
				// 请求了延迟重试。任务已经返回到队列头部。
				// 释放处理锁，并安排下一次运行。
				mailbox.setProcessing(false);
				SchedulerService.getInstance().scheduleTask(() -> {
					if (executorService.isShutdown().getAsBoolean()) {
						return;
					}
					// 尝试获取处理锁，成功后提交新的处理器
					if (mailbox.compareAndSetProcessing(false, true)) {
						executorService.submitProcessor(mailbox);
					}
				}, delayedRetryPolicy.getDelay(), delayedRetryPolicy.getTimeUnit());

			} else if (mailbox.isEmpty()) {
				// 队列为空，且没有待处理的延迟。释放锁。
				mailbox.setProcessing(false);

				// 双重检查以防止在 isEmpty() 检查之后、setProcessing(false) 之前添加新任务时发生竞态条件。
				if (!mailbox.isEmpty() && mailbox.compareAndSetProcessing(false, true)) {
					executorService.submitProcessor(mailbox);
				}
			} else {
				// 队列不为空，且没有请求延迟。
				// 这意味着可能发生了立即重试，或者有新任务到达。应立即继续处理。
				executorService.submitProcessor(mailbox);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Finished processing mailbox for entity {}", entityId);
			}
			DeadlockGuard.exit(entityId);
		}
	}

	/**
	 * 处理邮箱中的所有任务。
	 * @return 如果需要延迟重试，则返回ErrorPolicy，否则返回null。
	 */
	private ErrorPolicy processMailboxTasks() {
		TaskWrapper<?> taskWrapper;
		while ((taskWrapper = mailbox.pollTask()) != null) {
			ErrorPolicy policy = processTask(taskWrapper);
			// 如果收到延迟重试信号，则停止当前处理循环，并向上传递该信号。
			if (policy != null && policy.getType() == ErrorPolicy.PolicyType.RETRY_HEAD_WITH_DELAY) {
				return policy;
			}
		}
		return null; // 正常处理完成，没有需要延迟的任务。
	}

	/**
	 * 处理单个任务。
	 * @param taskWrapper 要处理的任务包装器
	 * @return 如果需要延迟重试，则返回ErrorPolicy，否则返回null。
	 */
	private <T> ErrorPolicy processTask(TaskWrapper<T> taskWrapper) {
		Task<T> task = taskWrapper.getTask();
		String taskDescription = task.getDescription();
		long startTime = System.nanoTime();
		try {
			T result = task.execute();
			// 任务成功完成，设置结果
			taskWrapper.complete(result);
			mailbox.getStats().completedTasks.incrementAndGet();
		} catch (Throwable e) {
			// 任务失败，应用错误处理策略。
			return handleTaskFailure(taskWrapper, e);
		} finally {
			long duration = System.nanoTime() - startTime;
			mailbox.getStats().recordProcessingTime(duration, taskDescription);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Task {} for entity {} processed in {} nanoseconds", taskDescription, mailbox.getEntityId(), duration);
			}
		}
		return null; // 任务成功，无需延迟。
	}

	/**
	 * 根据任务的ErrorHandler策略处理失败的任务。
	 * @param taskWrapper 失败任务的包装器。
	 * @param e 导致失败的异常。
	 * @return 如果需要延迟，则返回ErrorPolicy，否则返回null。
	 */
	private <T> ErrorPolicy handleTaskFailure(TaskWrapper<T> taskWrapper, Throwable e) {
		taskWrapper.incrementAttempts();
		Task<?> task = taskWrapper.getTask();
		long entityId = mailbox.getEntityId();

		RetryContext context = new RetryContext(task, e, taskWrapper.getAttempts());
		ErrorPolicy policy = task.getErrorHandler().apply(context);

		switch (policy.getType()) {
		case RETRY_HEAD_IMMEDIATELY:
			LOGGER.warn("Task [{}] (entity [{}]) failed, will retry immediately (attempt {}). Error: {}", task.getDescription(), entityId,
					context.getAttemptCount(), e.getMessage());
			mailbox.offerTaskFirst(taskWrapper);
			break;

		case RETRY_HEAD_WITH_DELAY:
			LOGGER.warn("Task [{}] (entity [{}]) failed, will retry in {} {} (attempt {}). Error: {}", task.getDescription(), entityId,
					policy.getDelay(), policy.getTimeUnit().toString().toLowerCase(), context.getAttemptCount(), e.getMessage());
			mailbox.offerTaskFirst(taskWrapper);
			return policy; // 返回策略以表示需要延迟。

		case DISCARD:
		default:
			if (e instanceof TimeoutException) {
				// 任务超时
				mailbox.getStats().timeoutTasks.incrementAndGet();
			} else {
				// 任务执行失败
				mailbox.getStats().failedTasks.incrementAndGet();
			}
			LOGGER.warn("Task [{}] (entity [{}]) permanently failed after {} attempts and will be discarded. Error: {}",
					task.getDescription(), entityId, context.getAttemptCount(), e.getMessage(), e);
			taskWrapper.fail(e); // 使用原始异常使Promise失败
			mailbox.setLastError(e);
			break;
		}
		// 对于“放弃”或“立即重试”，不需要由上层方法进行延迟处理。
		return null;
	}
}