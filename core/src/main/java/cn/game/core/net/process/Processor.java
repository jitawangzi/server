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

	// 执行同步逻辑，返回结果
	public <T> Future<T> process(long objectId, Callable<T> supplier);

	// 执行异步逻辑，返回结果
	public <T, R> Future<T> process(long objectId, Callable<R> supplier, Function<R, Future<T>> mapper);

}
