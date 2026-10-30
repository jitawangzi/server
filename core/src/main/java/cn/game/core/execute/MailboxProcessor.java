package cn.game.core.execute;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.execute.error.ErrorPolicy;
import cn.game.core.execute.error.RetryContext;

/**
 * 邮箱处理器，负责处理单个邮箱中的任务
 */
public class MailboxProcessor implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MailboxProcessor.class.getName());

	private final ActorMailbox mailbox;
	private final TaskExecutorService executorService;
	/** 调度时取得的消费权代次，拒绝旧回调或重复 Runnable。 */
	private final long generation;

	/**
	 * 捕获已取得的消费权，调用前须成功设置邮箱处理状态。
	 * @param mailbox 已取得消费权的邮箱
	 * @param executorService 后续调度所属服务
	 * @param config 既有执行配置
	 */
	public MailboxProcessor(ActorMailbox mailbox, TaskExecutorService executorService, TaskExecutionConfig config) {
		this(mailbox, executorService, config, mailbox.processingGeneration());
	}

	/**
	 * 将消费者绑定至调度时的消费权，而非运行时重新读取状态。
	 * @param mailbox 需要处理的邮箱
	 * @param executorService 负责后续调度的服务
	 * @param config 既有构造入口的执行配置
	 * @param generation 本次取得的消费权代次
	 */
	MailboxProcessor(ActorMailbox mailbox, TaskExecutorService executorService, TaskExecutionConfig config, long generation) {
		this.mailbox = mailbox;
		this.executorService = executorService;
		this.generation = generation;
	}

	@Override
	public void run() {
		if (!mailbox.enterProcessing(generation)) { return; }
		long entityId = mailbox.getEntityId();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start processing mailbox for entity {}", entityId);
		}
		DeadlockGuard.enter(mailbox.getEntityId());

		ErrorPolicy delayedRetryPolicy = null;

		try {
			// 处理循环现在在需要延迟重试时，会返回一个策略。
			delayedRetryPolicy = processMailboxTasks();
		} catch (Throwable e) {
			// 在此级别捕获异常是一种安全措施，以防万一。
			// 单个任务的异常应在 processTask 内部处理。
			LOGGER.error("Uncaught exception occurred while processing mailbox for entity {}", entityId, e);
		} finally {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Finished processing mailbox for entity {}", entityId);
			}
			DeadlockGuard.exit(entityId);
			// 包括空队列和异常路径，消费权始终在同一出口归还或交给带代次的延迟回调。
			executorService.finishProcessor(mailbox, generation, delayedRetryPolicy);
		}
	}

	/**
	 * 处理邮箱中的所有任务。
	 * @return 如果需要延迟重试，则返回ErrorPolicy，否则返回null。
	 */
	private ErrorPolicy processMailboxTasks() {
		TaskWrapper<?> taskWrapper;
		while ((taskWrapper = mailbox.pollTask()) != null) {
			ErrorPolicy policy;
			mailbox.beginTaskExecution(generation);
			try {
				policy = processTask(taskWrapper);
			} finally {
				// 连同错误策略和同步结果回调一起计时；任何退出路径均清理诊断状态。
				mailbox.endTaskExecution(generation);
			}
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
		taskWrapper.markStarted();
		Task<T> task = taskWrapper.getTask();
		String taskDescription = task.getDescription();
		long startTime = System.nanoTime();
		TimingMetrics.Span execution = TimingMetrics.start(TimingMetrics.Stage.EXECUTION);
		try {
			T result = task.execute();
			execution.end(TimingMetrics.Outcome.SUCCESS);
			// 任务成功完成，设置结果
			taskWrapper.complete(result);
			mailbox.getStats().completedTasks.incrementAndGet();
		} catch (Throwable e) {
			execution.end(TimingMetrics.Outcome.FAILURE);
			// 任务失败，应用错误处理策略。
			LOGGER.warn("Task {} failed for mailbox {}", taskDescription, mailbox.getEntityId(), e);
			return handleTaskFailure(taskWrapper, e);
		} finally {
			long duration = System.nanoTime() - startTime;
			mailbox.getStats().recordProcessingTime(duration, taskDescription);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Task {} for entity {} processed in {} nanoseconds", taskDescription, mailbox.getEntityId(), duration);
			}
			if (duration > 500_000_000) {
				LOGGER.warn("Task {} for entity {} processed in {} nanoseconds", taskDescription, mailbox.getEntityId(), duration);
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
		ErrorPolicy policy;
		try {
			policy = task.getErrorHandler().apply(context);
			if (policy == null || policy.getType() == null) { throw new IllegalStateException("Error handler returned no policy"); }
		} catch (Throwable handlerFailure) {
			LOGGER.error("Error handler failed for task {} in mailbox {}", task.getDescription(), entityId, handlerFailure);
			if (handlerFailure != e) { handlerFailure.addSuppressed(e); }
			failTask(taskWrapper, handlerFailure);
			return null;
		}

		switch (policy.getType()) {
		case RETRY_HEAD_IMMEDIATELY:
		case RETRY_HEAD_WITH_DELAY:
			if (executorService.isShutdown().getAsBoolean()) {
				failTask(taskWrapper, new RejectedExecutionException("Task executor service is shutting down", e));
				return null;
			}
			taskWrapper.markRetryQueued();
			if (!mailbox.offerTaskFirstRetry(taskWrapper)) {
				taskWrapper.markQueueRejected();
				QueueFullException rejected = new QueueFullException("Retry queue is full for mailbox " + entityId);
				rejected.initCause(e);
				LOGGER.warn("Retry rejected for task {} in mailbox {}", task.getDescription(), entityId, rejected);
				failTask(taskWrapper, rejected);
				return null;
			}
			LOGGER.warn("Retry queued for task {} in mailbox {}, attempt {}, delay {} {}", task.getDescription(), entityId,
					context.getAttemptCount(), policy.getDelay(), policy.getTimeUnit(), e);
			return policy.getType() == ErrorPolicy.PolicyType.RETRY_HEAD_WITH_DELAY ? policy : null;

		case DISCARD:
		default:
			LOGGER.warn("Task [{}] (entity [{}]) permanently failed after {} attempts and will be discarded. Error: {}",
					task.getDescription(), entityId, context.getAttemptCount(), e.getMessage(), e);
			failTask(taskWrapper, e);
			break;
		}
		// 对于“放弃”或“立即重试”，不需要由上层方法进行延迟处理。
		return null;
	}

	/**
	 * 将不可重试的实际结果交还等待者，同时记录失败类型。
	 * @param taskWrapper 当前已经结束执行的任务
	 * @param cause 终态失败原因
	 */
	private void failTask(TaskWrapper<?> taskWrapper, Throwable cause) {
		if (cause instanceof TimeoutException) { mailbox.getStats().timeoutTasks.incrementAndGet(); }
		else { mailbox.getStats().failedTasks.incrementAndGet(); }
		mailbox.setLastError(cause);
		taskWrapper.fail(cause);
	}
}
