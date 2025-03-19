package cn.game.core.util;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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

	/** 
	 * 检查当前线程是不是eventLoop线程, 如果不是则抛出异常
	 */
	public static void checkEventLoop() {
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof VertxThread && ((VertxThread) currentThread).isWorker() == false) {
			throw new IllegalStateException(
					"Blocking operation cannot be executed on EventLoop thread. " + "Current thread: " + currentThread.getName());
		}
	}

	/** 
	 * 检查当前线程是不是Vertx线程, 如果不是则抛出异常
	 */
	public static void checkVertxThread() {
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof VertxThread == false) {
			throw new IllegalStateException("this operation must be in vertx thread. " + " Current thread: " + currentThread.getName());
		}
	}

	/** 
	 * 同步等待Vertx 的Future完成,谨慎使用
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

	/**
	 * 在指定context中执行任务，并把任务的结果封装成Future返回。 
	 * 任务的返回值支持同步结果和各种异步结果类型
	 * @param <T> 结果类型
	 * @param <R> 任务返回类型（可以是同步结果或异步结果容器） 
	 * @param context 执行上下文
	 * @param callOnCallerThread 是否在调用者线程执行回调
	 * （true=调用者线程，false=执行线程）
	 * 	注意这个参数只针对调用者线程是vertx的线程才有效。
	 * @param supplier 任务提供者
	 * @param mapper 将任务结果映射到Future<T>的函数，如果为null则直接使用supplier结果作为同步结果
	 * @return 包含结果的Future
	 */
	@SuppressWarnings("unchecked")
	public static <T, R> Future<T> runOnContext(Context context, boolean callOnCallerThread, Supplier<R> supplier,
			Function<R, Future<T>> mapper) {
		Promise<T> promise = Promise.promise();
		if (callOnCallerThread && Thread.currentThread() instanceof VertxThread == false) {
			promise.fail("callOnCallerThread 只针对调用者线程是vertx的线程才有效");
			return promise.future();
		}
		// 捕获调用者上下文
		Context callerContext = Vertx.currentContext();

		context.runOnContext(v -> {
			try {
				R result = supplier.get();
				try {
					if (mapper == null) {
						// 同步结果处理
						if (callOnCallerThread) {
							callerContext.runOnContext(v2 -> promise.complete((T) result));
						} else {
							promise.complete((T) result);
						}
					} else {
						// 异步结果处理
						Future<T> future = mapper.apply(result);
						if (callOnCallerThread) {
							future.onComplete(ar -> {
								callerContext.runOnContext(v2 -> {
									if (ar.succeeded())
										promise.complete(ar.result());
									else
										promise.fail(ar.cause());
								});
							});
						} else {
							future.onComplete(promise);
						}
					}
				} catch (ClassCastException e) {
					handleFailure(promise, new RuntimeException("Type conversion error", e), callerContext, callOnCallerThread);
				} catch (Exception e) {
					handleFailure(promise, new RuntimeException("Error processing result", e), callerContext, callOnCallerThread);
				}
			} catch (Exception e) {
				handleFailure(promise, e, callerContext, callOnCallerThread);
			}
		});

		return promise.future();
	}

	// 默认回调行为的重载（默认在执行线程回调）
	public static <T, R> Future<T> runOnContext(Context context, Supplier<R> supplier, Function<R, Future<T>> mapper) {
		return runOnContext(context, false, supplier, mapper);
	}

	// 辅助方法：统一处理失败情况
	private static <T> void handleFailure(Promise<T> promise, Throwable t, Context callerContext, boolean callOnCallerThread) {
		if (callOnCallerThread && callerContext != null) {
			callerContext.runOnContext(v -> promise.fail(t));
		} else {
			promise.fail(t);
		}
	}

	/** 
	 * 
	 * 智能类型检测的通用方法
	 * 自动检测返回类型并应用合适的转换器
	 * @param <T>
	 * @param context
	 * @param callOnCallerThread
	 * @param supplier
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Future<T> runOnContextAuto(Context context, boolean callOnCallerThread, Supplier<?> supplier) {
		return runOnContext(context, callOnCallerThread, supplier, result -> {
			if (result == null) {
				return Future.succeededFuture(null);
			} else if (result instanceof Future) {
				return (Future<T>) result;
			} else if (result instanceof CompletionStage) {
				return (Future<T>) VertxFutureConverter.completionStageConverter().apply((CompletionStage<Object>) result);
			} else if (result instanceof java.util.concurrent.Future) {
				return (Future<T>) VertxFutureConverter.jdkFutureConverter().apply((java.util.concurrent.Future<Object>) result);
			} else {
				// 默认当作同步结果处理
				return Future.succeededFuture((T) result);
			}
		});
	}

	// 默认回调在执行线程中执行
	public static <T> Future<T> runOnContextAuto(Context context, Supplier<?> supplier) {
		return runOnContextAuto(context, false, supplier);
	}
}