package cn.game.core.net.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;

public class DirectProcessor implements Processor {

	private static Logger log = LoggerFactory.getLogger(DirectProcessor.class);

	@Autowired
	private Dispatcher dispatcher;

	public DirectProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol protocol) {

		long start = System.currentTimeMillis();
		try {
			dispatcher.dispatch(netClient, protocol);
		} catch (Throwable e) {
			e.printStackTrace();
			log.error(netClient + "run msg" + "0x" + Integer.toHexString(protocol.getMsgID()) + "err", e);
		}
		if (log.isDebugEnabled()) {
			log.debug("{} run msg[{}] use time[{}]ms", netClient, "0x" + Integer.toHexString(protocol.getMsgID()),
					System.currentTimeMillis() - start);
		}

	}

}
