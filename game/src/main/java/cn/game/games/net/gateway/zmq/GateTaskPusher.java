package cn.game.games.net.gateway.zmq;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 * @Description
 * @date 2016年10月26日 下午2:30:30
 * @author SYQ
 */
public class GateTaskPusher implements Runnable {

	private ZContext context;
	private ZMQ.Socket pusher;
	private String gameAddr;
	private String serverId;

	private static Logger log = LoggerFactory.getLogger(GateTaskPusher.class);

	private BlockingQueue<ZMsg> queue = new LinkedBlockingQueue<ZMsg>();

	// public GateTaskPusher(String gameAddr, ZContext context) {
	// this.gameAddr=gameAddr ;
	// this.context = context ;
	// }
	public GateTaskPusher() {
	}

	public void put(ZMsg msg) throws InterruptedException {
		queue.put(msg);
	}

	
	public void init() {
		Thread thread = new Thread(this);
		thread.setName("GateTaskPusher");
		thread.start();

	}

	@Override
	public void run() {

		context = new ZContext();

		pusher = context.createSocket(ZMQ.PAIR);
		String address = String.format("inproc://zctx-pipe-%d", pusher.hashCode());
		pusher.bind(address);

		ZmqGateHandler gateHandler = new ZmqGateHandler(address, gameAddr, serverId, context);
		new Thread(gateHandler, "ZmqGateHandler").start();

		while (!Thread.currentThread().isInterrupted()) {
			ZMsg poll = null;
			try {
				poll = this.queue.poll(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if (poll != null) {
				poll.send(pusher);
			}
		}

		pusher.close();
	}

	public void setGameAddr(String gameAddr) {
		this.gameAddr = gameAddr;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

}
