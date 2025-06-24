package cn.game.core.net.process;

import java.util.concurrent.Callable;
import java.util.function.Function;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.vertx.VxContextRegistry;
import io.vertx.core.Future;

public class IdEventLoopProcessor extends AbstractProcessor {

	public IdEventLoopProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol<?> protocol) {
		VxContextRegistry.getInstance().submitTask(netClient.getPlayerId(), r -> super.process(netClient, protocol));
	}

	@Override
	public void process(long objectId, Runnable task) {
		VxContextRegistry.getInstance().submitTask(objectId, task);
	}

	@Override
	public void process(long objectId, NetClient netClient, IProtocol<?> protocol) {
		VxContextRegistry.getInstance().submitTask(objectId, r -> super.process(netClient, protocol));
	}

	@Override
	public <T, R> Future<T> process(long objectId, Callable<R> supplier, Function<R, Future<T>> mapper) {
		return VxContextRegistry.getInstance().submitTaskWithResult(objectId, supplier, mapper);

	}
}
