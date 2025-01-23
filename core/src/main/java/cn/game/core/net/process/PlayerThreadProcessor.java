package cn.game.core.net.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.core.net.vertx.VxContextRegistry;
import io.vertx.core.Handler;

public class PlayerThreadProcessor implements Processor {

	private static Logger log = LoggerFactory.getLogger("usetimeLog");
	@Autowired
	private Dispatcher dispatcher;

	public PlayerThreadProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol protocol) {

		Handler<Void> action = (v) -> {

			long start = System.nanoTime();
			try {
				dispatcher.dispatch(netClient, protocol);
			} catch (Throwable e) {
				log.error(netClient + "run msg" + "0x" + Integer.toHexString(protocol.getMsgID()) + "err", e);
			}
			if (log.isDebugEnabled()) {
				log.debug("{} run msg[{}] use time[{}]ms", netClient, "0x" + Integer.toHexString(protocol.getMsgID()), (System
						.nanoTime() - start) / 1000000f);
			}
		};
		VxContextRegistry.getInstance().submitTask(netClient.getPlayerId(), action);
	}
}
