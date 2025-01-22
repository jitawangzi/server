package cn.game.core.util;

import java.util.concurrent.TimeUnit;

import io.vertx.core.Future;
import io.vertx.core.impl.VertxThread;

/**    
 * 异步转同步工具类
 * 注意不要在vert.x的EventLoop线程中阻塞等待结果，会让线程无法处理其他事件， 
 * 也包括本Future可能的事件。出现“卡死”现象
 * 
 * 2025年1月20日 15:40:19
 * @author SYQ
 */
public class AsyncUtils {

	public static void checkEventLoop() {
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof VertxThread && ((VertxThread) currentThread).isWorker() == false) {
			throw new IllegalStateException(
					"Blocking operation cannot be executed on EventLoop thread. " + "Current thread: " + currentThread.getName());
		}
	}

	/** 
	 * 同步等待Vertx 的Future完成
	 * @param <T>
	 * @param future
	 * @param timeout
	 * @param unit
	 * @return
	 */
	public static <T> T await(Future<T> future, long timeout, TimeUnit unit) {
		checkEventLoop();
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
		checkEventLoop();
		return await(future, 30, TimeUnit.SECONDS); // 默认超时 30 秒
	}

	public static <T> T join(Future<T> future) {
		checkEventLoop();
		return future.toCompletionStage().toCompletableFuture().join();
	}
}