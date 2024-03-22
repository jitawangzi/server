package cn.game.games.net.game.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import cn.game.core.net.process.Processor;
import cn.game.core.net.protocol.bytes.BaseByteProtocol;
import cn.game.games.core.mina.HandlerState;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.client.GameGateClient;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.util.ByteHelp;
import cn.game.util.MBeanManager;

/**
 * @Description 负责zmq消息接收和发送
 * @date 2017年4月1日 上午11:36:06
 * @author SYQ
 */
public class ZmqAcceptor implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(ZmqAcceptor.class);
	private ZContext context;
	private String serverId;
	private String addr;
	private Processor zmqProcessor;
	private volatile boolean ready = false;
	private HandlerState handlerState;

	public ZmqAcceptor(ZContext context, String serverId, String addr) {
		this.context = context;
		this.serverId = serverId;
		this.addr = addr;
		zmqProcessor = new DefaultZmqProcessor(context);
		handlerState = new HandlerState();
		MBeanManager.registerMBean(handlerState, "net.game.zmq:type=HandlerState,name=GameHandlerState");
	}

	public void waitGateConnect() {
		while (!ready) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {

		Socket router = context.createSocket(ZMQ.ROUTER);
		router.setIdentity(serverId.getBytes());
		router.bind(addr);

		log.info("server[{}] router bind addr[{}]", serverId, addr);
		Socket pair = context.createSocket(ZMQ.PAIR);
		String addr = String.format("inproc://zctx-pipe-%d", pair.hashCode());
		pair.bind(addr);

		new Thread(ZmqPairSender.getInstance().init(context, addr), serverId + " zmq Sender").start();

		PollItem[] items = { new PollItem(router, ZMQ.Poller.POLLIN), new PollItem(pair, ZMQ.Poller.POLLIN), };

		while (!Thread.currentThread().isInterrupted()) {

			if (ZMQ.poll(items, 1000) == -1)
				break; // Interrupted

			if (items[0].isReadable()) {
				// 有消息进来，执行
				ZMsg msg = ZMsg.recvMsg(router);
				ZFrame msgAddr = msg.pop();
				// msg.pop();
				ZFrame sessionFrame = msg.pop();
				ZFrame idFrame = msg.pop();
				// ZFrame checkID = msg.pop() ;
				ZFrame dataFrame = msg.pop();

				if (dataFrame == null || !dataFrame.hasData()) {
					ZMsg response = new ZMsg();
					response.add(msgAddr);
					response.add("test response");
					log.info("gameServer收到GateServer[{}]空包，测试链接", new String(msgAddr.getData()));
					response.send(router);
					ready = true;
					continue;
				}
				// log.debug("worker : " + Thread.currentThread().getName() +
				// "收到数据包：" + new String(dataFrame.getData()));
				handlerState.addTotalPacketReceived();
				// 不用了，随便写一下
				BaseByteProtocol protocol = new BaseByteProtocol(ByteHelp.makeIntB(idFrame.getData()), dataFrame.getData());
//				long sessionId = ByteHelp.makeLong(sessionFrame.getData());
				String sessionId = String.valueOf(sessionFrame.getData());

				GameClient gameClient = GameClientManager.getInstance().getGameClient(sessionId);
				GameGateClient client = null ; 
				if (gameClient == null) {
					// TODO 如果不是登陆请求，则返回,Gate也应该有过期机制。
					// TODO  这里注释掉了暂时不正确，以后修改一下，
//					gameClient = new GameClient(msgAddr.getData(), sessionId);
					GameClientManager.getInstance().addGameClientSession(gameClient);
				}
				gameClient.setLastRecvPacketTime(System.currentTimeMillis());
				zmqProcessor.process(gameClient, protocol);

			}
			if (items[1].isReadable()) {
				// 有消息要发送出去
				ZMsg msg = ZMsg.recvMsg(pair);
				msg.send(router);
				handlerState.addTotalPacketSend();
			}

		}

	}

}
