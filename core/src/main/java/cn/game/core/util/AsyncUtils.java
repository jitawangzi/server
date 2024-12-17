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
	 * @throws Exception
	 */
	public static <T> T await(Future<T> future, long timeout, TimeUnit unit) throws Exception {
		// 超时机制
		try {
			return future.toCompletionStage().toCompletableFuture().get(timeout, unit);
		} catch (Exception e) {
			throw new RuntimeException("Error waiting for async result", e);
		}
	}

	public static <T> T await(Future<T> future) throws Exception {
		return await(future, 30, TimeUnit.SECONDS); // 默认超时 30 秒
	}
}