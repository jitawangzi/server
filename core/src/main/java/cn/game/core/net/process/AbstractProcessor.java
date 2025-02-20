package cn.game.core.net.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.controller.Dispatcher;

public abstract class AbstractProcessor implements Processor {

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	protected Logger usetimeLog = LoggerFactory.getLogger("usetimeLog");
	@Autowired
	protected Dispatcher dispatcher;

	public AbstractProcessor() {
	}

	@Override
	public void process(final NetClient netClient, final IProtocol<?> protocol) {
		long start = System.nanoTime();
		try {
			dispatcher.dispatch(netClient, protocol);
		} catch (Throwable e) {
			log.error(netClient + "run msg" + "0x" + Integer.toHexString(protocol.getMsgID()) + "err", e);
		}
		if (usetimeLog.isDebugEnabled()) {
			usetimeLog.debug("{} run msg[{}] use time[{}]ms", netClient, "0x" + Integer.toHexString(protocol.getMsgID()),
					(System.nanoTime() - start) / 1000000f);
		}
	}

	@Override
	public void process(long objectId, Runnable task) {
		throw new UnsupportedOperationException("not support processor" + this.getClass().getName() + " objectId:task");
	}

	@Override
	public void process(Runnable task) {
		throw new UnsupportedOperationException("not support processor" + this.getClass().getName() + " task");
	}

	@Override
	public void process(long objectId, NetClient gameClient, IProtocol<?> protocol) {
		throw new UnsupportedOperationException("not support processor" + this.getClass().getName() + " objectId:task:protocol");
	}
}
