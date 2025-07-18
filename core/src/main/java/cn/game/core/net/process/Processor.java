package cn.game.core.net.process;

import java.util.concurrent.Callable;
import java.util.function.Function;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import io.vertx.core.Future;

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
	 * 执行逻辑，返回结果，对于如果是eventloop线程，不能执行阻塞逻辑。 
	 * @param <T>
	 * @param objectId
	 * @param supplier
	 * @return
	 */
	public <T> T process(long objectId, Callable<T> supplier);

	// 执行异步逻辑，返回结果
	public <T, R> Future<T> process(long objectId, Callable<R> supplier, Function<R, Future<T>> mapper);

}
