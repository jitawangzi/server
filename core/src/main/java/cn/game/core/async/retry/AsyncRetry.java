package cn.game.core.async.retry;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import cn.game.core.task.SchedulerService;

/**
 * 异步重试工具类
 * 先使用SchedulerService执行重试任务
 */
public class AsyncRetry {
	private static final Duration delay = Duration.ofSeconds(5);
	private static RetryConfig defaultConfig = new RetryConfig.Builder().maxAttempts(3).delay(delay).build();
	/**
	 * 执行异步重试操作
	 * @param operation 要重试的操作
	 * @param config 重试配置
	 * @return 操作结果
	 */
	public static <T> CompletableFuture<AttemptResult<T>> execute(Function<Integer, CompletionStage<T>> operation, RetryConfig config) {

		return executeWithRetryInternal(operation, config, 0);
	}

	/** 
	 * 默认带间隔时间的重试操作
	 * @param <T>
	 * @param operation
	 * @return
	 */
	public static <T> CompletableFuture<AttemptResult<T>> executeWithDelay(Function<Integer, CompletionStage<T>> operation) {

		return executeWithRetryInternal(operation, defaultConfig, 0);
	}

	/** 
	 * 延迟执行任务
	 * @param <T>
	 * @param task
	 * @param delay
	 * @return
	 */
	private static <T> CompletableFuture<T> delayedExecution(Supplier<CompletableFuture<T>> task, Duration delay) {
		CompletableFuture<T> future = new CompletableFuture<>();

		SchedulerService.getInstance().scheduleTask(() -> task.get().thenAccept(future::complete).exceptionally(ex -> {
			future.completeExceptionally(ex);
			return null;
		}), delay.toMillis(), TimeUnit.MILLISECONDS);

		return future;
	}

	private static <T> CompletableFuture<AttemptResult<T>> executeWithRetryInternal(Function<Integer, CompletionStage<T>> operation,
			RetryConfig config, int attempt) {

		return CompletableFuture.supplyAsync(() -> attempt).thenCompose(currentAttempt -> {
			if (currentAttempt >= config.maxAttempts) {
				return CompletableFuture.completedFuture(
						new AttemptResult.Builder<T>().success(false).message("Max attempts reached").attemptCount(currentAttempt).build());
			}

			return operation.apply(currentAttempt)
					.thenApply(result -> new AttemptResult.Builder<T>().success(true).data(result).attemptCount(currentAttempt + 1).build())
					.exceptionally(throwable -> new AttemptResult.Builder<T>().success(false)
							.error(throwable)
							.attemptCount(currentAttempt + 1)
							.build())
					.thenCompose(result -> {
						boolean shouldRetry = !result.isSuccess() || !config.resultPredicate.test(result.getData());

						if (shouldRetry) {
							return delayedExecution(() -> executeWithRetryInternal(operation, config, currentAttempt + 1), config.delay);
						}
						return CompletableFuture.completedFuture(result);
					});
		});
	}

}