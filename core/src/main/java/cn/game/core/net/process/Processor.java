package cn.game.core.net.process;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import cn.game.core.execute.TaskNotExecutedException;
import cn.game.core.execute.TaskExecutionException;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public interface Processor {

	public void process(NetClient gameClient, IProtocol<?> protocol);

	public void process(Runnable task);

	// 带objectId的方法，会根据objectId找到对应的线程执行
	public void process(long objectId, NetClient gameClient, IProtocol<?> protocol);

	public void process(long objectId, Runnable task);
	
	/** 
	 * 某些任务可能希望尽快的完成
	 * @param objectId
	 * @param task
	 * @param fast   是否更快的执行这个任务
	 */
	public void process(long objectId, Runnable task,boolean fast);

	/**
	 * 观察无返回值任务的实际完成；调度拒绝与业务执行失败分别传播，不自动重试。
	 * @param objectId 任务归属 ID
	 * @param task 待执行任务
	 * @param fast 是否优先执行
	 * @return 实际结果；开始前拒绝为 TaskNotExecutedException
	 */
	default Future<Void> processAsync(long objectId, Runnable task, boolean fast) {
		Promise<Void> result = Promise.promise();
		AtomicBoolean started = new AtomicBoolean();
		try {
			process(objectId, () -> {
				started.set(true);
				try {
					task.run();
					result.tryComplete();
				} catch (Throwable failure) {
					LoggerFactory.getLogger(Processor.class).error("Task execution failed for {}", objectId, failure);
					result.tryFail(executionFailure(objectId, failure));
				}
			}, fast);
		} catch (RuntimeException failure) {
			LoggerFactory.getLogger(Processor.class).warn("Task submission failed for {}", objectId, failure);
			result.tryFail(started.get() ? executionFailure(objectId, failure) : new TaskNotExecutedException(failure));
		}
		return result.future();
	}

	/**
	 * 已开始的外层任务不能继承内层任务的未执行标记，原始原因仍保留在异常链中。
	 * @param objectId 当前任务归属 ID
	 * @param failure 当前任务执行时的异常
	 * @return 只在内层拒绝标记冲突时包装的执行异常
	 */
	static Throwable executionFailure(long objectId, Throwable failure) {
		return failure instanceof TaskNotExecutedException
				? new TaskExecutionException("Task executed before nested submission failed", failure, objectId, "processor task")
				: failure;
	}

	/** 
	 * 执行逻辑，返回结果，对于如果是eventloop线程，不能执行阻塞逻辑。 
	 * @param <T>
	 * @param objectId
	 * @param supplier
	 * @return
	 */
	public <T> T process(long objectId, Callable<T> supplier);
	
	// 执行异步逻辑，返回结果
	public <T, R> Future<T> process(long objectId, Callable<R> supplier, Function<R, Future<T>> mapper);
	
	public default void close() {}; 

}
