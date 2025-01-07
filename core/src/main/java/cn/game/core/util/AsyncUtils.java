package cn.game.core.util;

import java.util.concurrent.TimeUnit;

import io.vertx.core.Future;

public class AsyncUtils {
	/** 
	 * 同步等待Vertx 的Future完成
	 * @param <T>
	 * @param future
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public static <T> T await(Future<T> future, long timeout, TimeUnit unit) {
		// 超时机制
		try {
			return future.toCompletionStage().toCompletableFuture().get(timeout, unit);
		} catch (Exception e) {
			throw new RuntimeException("Error waiting for async result", e);
		}
	}

	/** 
	 * 同步等待Vertx 的Future完成，默认超时 30 秒
	 * @param <T>
	 * @param future
	 * @return
	 */
	public static <T> T await(Future<T> future) {
		return await(future, 30, TimeUnit.SECONDS); // 默认超时 30 秒
	}

	public static <T> T join(Future<T> future) {
		return future.toCompletionStage().toCompletableFuture().join();
	}
}