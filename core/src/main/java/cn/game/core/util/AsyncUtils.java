package cn.game.core.util;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import io.vertx.core.Future;
import io.vertx.core.impl.VertxThread;

/**    
 * 异步相关工具类
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

	/**
	* 通用跨服务调用处理器，按顺序调用多个远程接口实例，累积得到结果，适用于数据分布在多个服务器实例中的场景
	* @param interfaces 远程接口实例列表
	* @param callExecutor 远程调用执行函数（需自行处理异常）
	* @param accumulator 结果累积函数
	* @param stoppingCondition 停止条件判断
	* @param initialValue 初始累积值
	* @param <T> 远程接口类型
	* @param <R> 单个调用结果类型
	* @param <A> 累积结果类型
	* @return 包含最终累积结果的Future
	*/
	public static <T, R, A> Future<A> sequentialCollect(List<T> interfaces, Function<T, Future<R>> callExecutor,
			BiFunction<R, A, A> accumulator, Predicate<A> stoppingCondition, A initialValue) {

		return new SequentialCollector<>(interfaces, callExecutor, accumulator, stoppingCondition, initialValue).execute();
	}

	private static class SequentialCollector<T, R, A> {
		private final Iterator<T> interfaceIterator;
		private final Function<T, Future<R>> callExecutor;
		private final BiFunction<R, A, A> accumulator;
		private final Predicate<A> stoppingCondition;
		private A currentAccumulation;

		SequentialCollector(List<T> interfaces, Function<T, Future<R>> callExecutor, BiFunction<R, A, A> accumulator,
				Predicate<A> stoppingCondition, A initialValue) {
			this.interfaceIterator = interfaces.iterator();
			this.callExecutor = callExecutor;
			this.accumulator = accumulator;
			this.stoppingCondition = stoppingCondition;
			this.currentAccumulation = initialValue;
		}

		Future<A> execute() {
			return processNext();
		}

		private Future<A> processNext() {
			// 终止条件：没有更多接口或满足停止条件
			if (!interfaceIterator.hasNext() || stoppingCondition.test(currentAccumulation)) {
				return Future.succeededFuture(currentAccumulation);
			}

			T nextInterface = interfaceIterator.next();
			return callExecutor.apply(nextInterface).compose(this::handleResult).compose(ignored -> processNext());
		}

		private Future<Void> handleResult(R result) {
			currentAccumulation = accumulator.apply(result, currentAccumulation);
			return Future.succeededFuture();
		}
	}

}