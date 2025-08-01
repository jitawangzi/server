package cn.game.core.net.process;

import java.util.concurrent.Callable;
import java.util.function.Function;

import cn.game.core.execute.TaskExecutorService;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.vertx.VxContextRegistry;
import io.vertx.core.Future;

public class IdVirtualThreadProcessor extends AbstractProcessor {

	public IdVirtualThreadProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol<?> protocol) {
		TaskExecutorService.getInstance().submitTask(netClient.getPlayerId(), () -> super.process(netClient, protocol));
	}

	@Override
	public void process(long objectId, Runnable task) {
		TaskExecutorService.getInstance().submitTask(objectId, task);
	}
	
	@Override
	public void process(long objectId, Runnable task,boolean fast) {
		TaskExecutorService.getInstance().submitTask(objectId, task,fast);
	}

	@Override
	public void process(long objectId, NetClient netClient, IProtocol<?> protocol) {
		TaskExecutorService.getInstance().submitTask(objectId, () -> super.process(netClient, protocol));

	}

	@Override
	public <T> T process(long objectId, Callable<T> supplier) {
		try {
			return TaskExecutorService.getInstance().executeAndAwait(objectId, supplier,false,"", 10000);
		} catch (Exception e) {
			throw new RuntimeException("Error processing callable for objectId: " + objectId, e);
		}
	}
	

	@Override
	public <T, R> Future<T> process(long objectId, Callable<R> supplier, Function<R, Future<T>> mapper) {
		Future<R> execute = TaskExecutorService.getInstance().execute(objectId, supplier,false,"", 10000);
		if (mapper != null) {
			return execute.compose(mapper);
		}
		return execute.map(r -> (T) r) ; 
	}
}
