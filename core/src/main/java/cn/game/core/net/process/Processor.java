package cn.game.core.net.process;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;

public interface Processor {

	public void process(NetClient gameClient, IProtocol<?> protocol);

	public void process(long objectId, NetClient gameClient, IProtocol<?> protocol);

	public void process(long objectId, Runnable task);

	public void process(Runnable task);
}
