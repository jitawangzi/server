package cn.game.core.net.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import cn.game.core.task.TaskManager;

public class NettyProcessor implements Processor {

	private static Logger log = LoggerFactory.getLogger("usetimeLog");
	@Autowired
	private Dispatcher dispatcher;

	public NettyProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol protocol) {

		long playerId = netClient == null ? 0 : netClient.getPlayerId();
		Runnable runnable = () -> {

			long start = System.nanoTime();
			try {
				dispatcher.dispatch(netClient, protocol);
			} catch (Throwable e) {
				e.printStackTrace();
				log.error(netClient + "run msg" + "0x" + Integer.toHexString(protocol.getMsgID()) + "err", e);
			}
			if (log.isDebugEnabled()) {
				log.debug("{} run msg[{}] use time[{}]ms", netClient, "0x" + Integer.toHexString(protocol.getMsgID()), (System
						.nanoTime() - start) / 1000000f);
			}
		};
		int module = protocol.getMsgID() >> 24;

		if (module == 0x51) {
			TaskManager.getInstance().addMainTask(runnable);
		} else {
			TaskManager.getInstance().addPlayerTask(runnable, playerId);
		}
	}
}
