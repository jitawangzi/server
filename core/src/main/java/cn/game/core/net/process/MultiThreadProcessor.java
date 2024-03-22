package cn.game.core.net.process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MultiThreadProcessor implements Processor {

//	private static Logger log = LoggerFactory.getLogger("usetimeLog");
	private static Logger log = LoggerFactory.getLogger(MultiThreadProcessor.class);
	private static ExecutorService executorService = Executors.newFixedThreadPool(100);

	@Autowired
	private Dispatcher dispatcher;

	public MultiThreadProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol protocol) {

		executorService.execute(() -> {

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
		});
	}

}
