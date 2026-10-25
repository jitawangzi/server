package cn.game.core.net.process;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import cn.game.core.execute.TaskNotExecutedException;
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
					result.tryFail(failure);
				}
			}, fast);
		} catch (RuntimeException failure) {
			LoggerFactory.getLogger(Processor.class).warn("Task submission failed for {}", objectId, failure);
			result.tryFail(started.get() ? failure : new TaskNotExecutedException(failure));
		}
		return result.future();
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
