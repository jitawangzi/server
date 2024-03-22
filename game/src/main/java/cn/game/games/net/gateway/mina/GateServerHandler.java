package cn.game.games.net.gateway.mina;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import cn.game.games.core.mina.HandlerState;
import cn.game.games.net.client.GateClient;
import cn.game.games.net.gateway.GateClientManager;
import cn.game.games.net.gateway.zmq.GateTaskPusher;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.util.ByteHelp;
import cn.game.util.Config;
import cn.game.util.GameCrypt;
import cn.game.util.MBeanManager;

public class GateServerHandler extends IoHandlerAdapter {
	private final static Logger log = LoggerFactory.getLogger(GateServerHandler.class);
	private final static Logger minaLog = LoggerFactory.getLogger("minaLog");
	private static Logger sendLog = LoggerFactory.getLogger("gatesendLog");
	private static Logger recvLog = LoggerFactory.getLogger("gaterecvLog");
	/** 读操作空闲等待超时时长 */
	public static final int DEF_SESSION_READIDLETIME = 60;

	/** 写操作空闲等待超时时长 */
	public static final int DEF_SESSION_WRITEIDLETIME = 60;
	public static final String SESSION_READ_IDLE_TIME = "mina.acceptor.config.read.idle";
	/**
	 * 会话读取数据空闲等待时长
	 */
	private int sessionReadIdleTime = DEF_SESSION_READIDLETIME;

	public static final String SESSION_WRITE_IDLE_TIME = "mina.acceptor.config.write.idle";

	/**
	 * 会话写数据空闲等待时长
	 */
	private int sessionWriteIdleTime = DEF_SESSION_WRITEIDLETIME;

	/**
	 * 默认写出操作超时时长
	 */
	public static final int DEF_WRITETIMEOUT_SECOND = 30;

	public static final String WRITE_TIMEOUT = "mina.acceptor.write.timeout";
	/**
	 * 写出操作超时时长
	 */
	private int writeTimeout = DEF_WRITETIMEOUT_SECOND;
	private HandlerState handlerState;
	private GateTaskPusher gateTaskPusher;

	public GateServerHandler() {

		handlerState = new HandlerState();
		MBeanManager.registerMBean(handlerState, "net.gateway.mina:type=HandlerState,name=HandlerState");

	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		minaLog.debug("SESSION CREATE IoSession [{}]", session);
		handlerState.addTotalCreateSession();

	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		synchronized (session) {
			minaLog.debug("SESSION OPENED IoSession [{}]", session);
			handlerState.addTotalOpendSession();
			if (this.sessionReadIdleTime > 0) {
				// 会话的读超时时间
				session.getConfig().setIdleTime(IdleStatus.READER_IDLE, sessionReadIdleTime);
			} else {
				session.getConfig().setIdleTime(IdleStatus.READER_IDLE, 0);
			}

			if (sessionWriteIdleTime > 0) {
				// 会话的写超时时间
				session.getConfig().setIdleTime(IdleStatus.WRITER_IDLE, sessionWriteIdleTime);
			} else {
				session.getConfig().setIdleTime(IdleStatus.WRITER_IDLE, 0);
			}

			if (this.writeTimeout > 0) {
				session.getConfig().setWriteTimeout(writeTimeout);
			} else {
				session.getConfig().setWriteTimeout(0);
			}
		}
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		handlerState.addTotalPacketReceived();

		IoBuffer in = (IoBuffer) message;
		if (!in.hasRemaining()) {
			return;
		}

		ZMsg msg = new ZMsg();
		int length = in.getInt();
		int msgID = in.getInt();
		// System.err.println("网关服收到消息："+Integer.toHexString(msgId));
		msg.add(ByteHelp.toByteArrayB(msgID));
		// msg.add(ByteHelp.toByteArrayB(checkId));
		byte[] data = new byte[in.remaining()];
		in.get(data);
		msg.add(data);
		// ZMsg msg = (ZMsg) message;
		GateClient client = (GateClient) session.getAttribute(GateClient.CLIENT_KEY);
		// int msgID = ByteHelp.makeIntB(msg.getFirst().getData());
		if (client == null) {
			if (msgID == PbProtocol.PlayerLoginRequest_01000001) {

				client = new GateClient(GameCrypt.gameCrypt, session);

				PlayerMsg.PlayerLoginRequest_01000001 login = PlayerMsg.PlayerLoginRequest_01000001
						.parseFrom(msg.getLast().getData());
				// zmq发给后端服务器，需要知道玩家所在服务器id
//				client.setGameServerAddr(login.getServerId().getBytes());
				client.setSessionId(login.getSessionId());
				client.setSessionAddr(ByteHelp.toByteArray(Long.valueOf(login.getSessionId())));

				InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
				client.setIp(isa.getAddress().getHostAddress());
				session.setAttribute(GateClient.CLIENT_KEY, client);
				GateClientManager.getInstance().addGateClient(client);
				log.debug("客户端创建新session，id ：{}", client.getSessionId());

			} else {
				log.warn("session[{}]新连接，但是没有先发登录请求，msgID[{}]", session, msgID);

				// WebSocketCodecPacket resp = WebSocketCodecPacket
				// .buildPacket(IoBuffer.wrap(PlayerNeedLoginResponse_01000041.getDefaultInstance().toByteArray()),
				// PbProtocol.PlayerNeedLoginResponse_01000041, 0);
				// session.write(resp);
				return;
			}

		}

		// ProtobufProtocol protocol = new ProtobufProtocol(msgID,
		// msg.getLast().getData()) ;

		msg.addFirst(client.getSessionAddr());
		// msg.addFirst("") ;
		msg.addFirst(client.getGameServerAddr());
		gateTaskPusher.put(msg);

		if (Config.recordRecvData) {
			if (msgID != PbProtocol.PlayerHeartbeatRequest_01000005) {
				Message parseFrom = PbProtocol.getInstance().parseFrom(msgID, data);
				recvLog.info("opType[recv]msgName[{}]msgValuse[{}]Session[{}]", parseFrom.getClass().getSimpleName(),
						TextFormat.shortDebugString(parseFrom), session);
			}
		}

	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		handlerState.addTotalPacketSend();
		super.messageSent(session, message);
		if (sendLog.isInfoEnabled()) {
			GateClient client = (GateClient) session.getAttribute(GateClient.CLIENT_KEY);
			if (client != null) {
				// if (message instanceof ZMsg) {
				// ZMsg msg = (ZMsg) message;
				// String msgId =
				// StringUtils.toHexString(ByteHelp.makeIntB(msg.poll().getData()));
				// sendLog.info("opType[send] gate send message[{}] to
				// ClientSession[{}]Session[{}]", msgId,
				// client.getSessionId(), session);
				// } else if (message instanceof WebSocketCodecPacket) {
				//
				// WebSocketCodecPacket msg = (WebSocketCodecPacket) message;
				// sendLog.info("opType[send] gate send message[{}] to
				// ClientSession[{}]Session[{}]", msg.getMsgId(),
				// client.getSessionId(), session);
				//
				// }

			}
		}
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		minaLog.debug("SESSION CLOSED IoSession [{}]", session);
		handlerState.addTotalCloseSession();

		synchronized (session) {
			GateClient gateClient = (GateClient) session.getAttribute(GateClient.CLIENT_KEY);
			if (gateClient != null) {
				GateClientManager.getInstance().removeGateClient(gateClient);
			}

		}
		super.sessionClosed(session);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		handlerState.addTotalExceptionCaught();
		if (cause instanceof IOException && cause.getMessage().equals("远程主机强迫关闭了一个现有的连接。")) {
			return;
		}
		minaLog.error("SESSION IoSession [{}] exceptionCaught [{}]", session, cause);
		super.exceptionCaught(session, cause);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		super.sessionIdle(session, status);
		if (status == IdleStatus.READER_IDLE) {
			handlerState.addTotalReadIdle();
		} else if (status == IdleStatus.WRITER_IDLE) {
			handlerState.addTotalWriteIdle();
		} else if (status == IdleStatus.BOTH_IDLE) {
			handlerState.addTotalReadIdle();
			handlerState.addTotalWriteIdle();
		}
		session.close(true);
	}

	public void setSessionReadIdleTime(int sessionReadIdleTime) {
		this.sessionReadIdleTime = sessionReadIdleTime;
	}

	public void setSessionWriteIdleTime(int sessionWriteIdleTime) {
		this.sessionWriteIdleTime = sessionWriteIdleTime;
	}

	public void setWriteTimeout(int writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	public void setGateTaskPusher(GateTaskPusher gateTaskPusher) {
		this.gateTaskPusher = gateTaskPusher;
	}

}
