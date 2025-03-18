package cn.game.core.net.process;

import java.util.function.Supplier;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import io.vertx.core.Future;

public interface Processor {

	public void process(NetClient gameClient, IProtocol<?> protocol);

	public void process(long objectId, NetClient gameClient, IProtocol<?> protocol);

	public void process(long objectId, Runnable task);

	public <T> Future<T> process(long objectId, Supplier<T> supplier);

	public void process(Runnable task);
}
