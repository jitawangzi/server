package cn.game.core.util;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import cn.game.core.net.vertx.VxHolder;
import cn.game.util.Config;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**    
 * 一些vert.x的Future转换器
 * 将其他类型的Future转换为Vert.x的Future
 * 2025年3月19日 11:26:22
 * @author SYQ
 */
public class VertxFutureConverter {
	// 原始类型静态实例,不直接使用（使用泛型方法保证类型安全，方便类型转换）
	/** 应该用不到，同一个类型的 */
	private static final Function<?, ?> vertxFutureConverter = (Function<Future<?>, Future<?>>) future -> future;
	/** completionStage转换器的静态单例实现 */
	private static final Function<?, ?> completionStageConverter = (Function<CompletionStage<?>, Future<?>>) cs -> Future
			.fromCompletionStage(cs);
	/** JDK Future转换器的静态单例实现 */
	private static final Function<?, ?> jdkFutureConverter = (Function<java.util.concurrent.Future<?>, Future<?>>) jdkFuture -> {
		Promise<Object> promise = Promise.promise();

		if (jdkFuture.isDone()) {
			handleCompletedFutureRaw(jdkFuture, promise);
			return promise.future();
		}

		AtomicLong timerId = new AtomicLong();
		timerId.set(VxHolder.vertx.setPeriodic(5, id -> {
			if (jdkFuture.isDone()) {
				VxHolder.vertx.cancelTimer(timerId.get());
				handleCompletedFutureRaw(jdkFuture, promise);
			}
		}));

		VxHolder.vertx.setTimer(Config.remoteCallTimeOutMillisecond, timeoutId -> {
			VxHolder.vertx.cancelTimer(timerId.get());
			if (!promise.future().isComplete()) {
				promise.fail(new TimeoutException("Timeout after " + Config.remoteCallTimeOutMillisecond + "ms"));
			}
		});

		return promise.future();
	};

	// 为静态单例实现提供的辅助方法
	private static void handleCompletedFutureRaw(java.util.concurrent.Future<?> jdkFuture, Promise<Object> promise) {
		try {
			Object result = jdkFuture.get();
			promise.complete(result);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			promise.fail(e);
		} catch (ExecutionException e) {
			promise.fail(e.getCause());
		} catch (CancellationException e) {
			promise.fail("Future cancelled");
		}
	}

	/**
	 * 将JDK Future转换为Vert.x Future的转换器
	 * @param <T> 结果类型
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<java.util.concurrent.Future<T>, Future<T>> jdkFutureConverter() {
		return (Function<java.util.concurrent.Future<T>, Future<T>>) jdkFutureConverter;
	}
	/** 
	 * CompletionStage的转换器（类型安全封装）
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<CompletionStage<T>, Future<T>> completionStageConverter() {
		return (Function<CompletionStage<T>, Future<T>>) completionStageConverter;
	}

	/** 
	 * Vert.x Future的转换器
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<Future<T>, Future<T>> vertxFutureConverter() {
		return (Function<Future<T>, Future<T>>) vertxFutureConverter;
	}

}