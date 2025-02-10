package cn.game.core.net.process;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.vertx.VxContextRegistry;

public class IdEventLoopProcessor extends AbstractProcessor {

	public IdEventLoopProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol protocol) {
		VxContextRegistry.getInstance().submitTask(netClient.getPlayerId(), r -> super.process(netClient, protocol));
	}

	@Override
	public void process(long objectId, Runnable task) {
		VxContextRegistry.getInstance().submitTask(objectId, task);
	}
}
