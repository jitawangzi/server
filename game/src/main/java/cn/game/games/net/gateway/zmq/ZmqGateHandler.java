package cn.game.games.net.gateway.zmq;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZLoop;
import org.zeromq.ZLoop.IZLoopHandler;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import cn.game.core.net.websocket.WebSocketCodecPacket;
import cn.game.games.net.client.GateClient;
import cn.game.games.net.gateway.GateClientManager;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.ByteHelp;
import cn.game.util.Config;

public class ZmqGateHandler implements Runnable {

	private ZContext context;
	private ZMQ.Socket pair;
	private ZMQ.Socket router;
	private String gameAddr;
	private String pairAddr;
	private String serverId;
	private static Logger log = LoggerFactory.getLogger(ZmqGateHandler.class);
	private static Logger sendLog = LoggerFactory.getLogger("gatesendLog");

	public ZmqGateHandler(String pairAddr, String gameAddr, String serverId, ZContext context) {
		this.context = context;
		this.pairAddr = pairAddr;
		this.gameAddr = gameAddr;
		this.serverId = serverId;

	}

	public void connectGameServer(String addr, String id) throws Exception {

		this.router.connect(addr);
		String string = new String(id.getBytes("utf-8"));
		for (int i = 0; i < 10; i++) {
			router.sendMore(string);
			router.send("");

			byte[] recv = router.recv(1);
			if (recv == null || recv.length == 0) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} else {
				log.info("gate router connect gameServer[{}] succ", new String(recv));
				return;
			}

		}
		throw new IllegalArgumentException("connect game addr:" + addr + " id:" + serverId + "+ error ");

	}

	/**
	 * @Description 处理别的服务器发送给网关服的消息
	 * 2017年3月27日 下午5:55:42
	 * @author SYQ
	 */
	class GateReceiveHandler implements IZLoopHandler {

		@Override
		public int handle(ZLoop arg0, PollItem arg1, Object arg2) {
			ZMsg msg = ZMsg.recvMsg(arg1.getSocket());
			ZFrame addr = msg.pop();
			ZFrame empty = msg.pop();
			ZFrame sessionFrame = msg.pop();
			if (sessionFrame == null || !sessionFrame.hasData()) {
				return 0;
			}
//			long sessionId = ByteHelp.makeLong(sessionFrame.getData());
			String sessionId = String.valueOf(sessionFrame.getData());

			int msgID = ByteHelp.makeIntB(msg.pop().getData());
			int errorCode = ByteHelp.makeIntB(msg.pop().getData());
			GateClient gateClient = GateClientManager.getInstance().getGateClient(sessionId);
			if (gateClient != null) {
				// if (msgID==PbProtocol.GateBroadcast_7e000001) {
				// try {
				//
				// GateBroadcast_7e000001 broadbast =
				// GateBroadcast_7e000001.parseFrom(msg.getLast().getData()) ;
				// int sessionIdsCount = broadbast.getSessionIdsCount();
				// if (sessionIdsCount==0) {
				// GateClientManager.getInstance().broadbast(msg);
				// }else{
				// GateClientManager.getInstance().broadbast(msg,
				// broadbast.getSessionIdsList());
				// }
				//
				// } catch (Exception e) {
				// log.error("",e);
				// }
				// }else {
				// gateClient.sendProtocol(msg);
				// }
				byte[] data = msg.pop().getData();
				WebSocketCodecPacket resp = WebSocketCodecPacket.buildPacket(IoBuffer.wrap(data), msgID, errorCode);
				gateClient.sendProtocol(resp);

				if (Config.recordSendData) {
					try {

						if (msgID != PbProtocol.PlayerHeartbeatResponse_01000006) {
							Message parseFrom = PbProtocol.getInstance().parseFrom(msgID, resp.getPacket().array());
							sendLog.info("opType[send][{}]code[{}]msgName[{}]msgValue[{}]", gateClient, errorCode,
									parseFrom.getClass().getSimpleName(), TextFormat.shortDebugString(parseFrom));
							if (errorCode > 0) {
								System.err.println("msgName :" + parseFrom.getClass().getSimpleName() + "  errorCode: " + errorCode);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				log.error("gate client is null, send message[{}]err sessionId[{}]", msgID, sessionId);
			}

			return 0;
		}

	}

	/**
	 * @Description 网关收到客户端消息后，转发给后端的服务器
	 * 2017年3月27日 下午5:56:27
	 * @author SYQ
	 */
	class GateSendHandler implements IZLoopHandler {

		@Override
		public int handle(ZLoop arg0, PollItem arg1, Object arg2) {
			ZMsg recvMsg = ZMsg.recvMsg(arg1.getSocket());
			recvMsg.send((Socket) arg2);
			return 0;
		}
	}

	@Override
	public void run() {
		this.pair = context.createSocket(ZMQ.PAIR);
		this.pair.connect(pairAddr);
		this.router = context.createSocket(ZMQ.ROUTER);

		try {
			connectGameServer(gameAddr, serverId);
		} catch (Throwable e) {
			log.error("connect game addr:" + gameAddr + " id:" + serverId + "+ error ");
			System.exit(-1);
		}

		ZLoop zLoop = new ZLoop();

		PollItem item = new PollItem(router, Poller.POLLIN);
		GateReceiveHandler handler = new GateReceiveHandler();
		zLoop.addPoller(item, handler, null);

		PollItem item2 = new PollItem(pair, Poller.POLLIN);
		GateSendHandler handler2 = new GateSendHandler();
		zLoop.addPoller(item2, handler2, router);

		zLoop.start();

	}
}
