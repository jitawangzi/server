package cn.game.games.net.client;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;
import com.google.protobuf.TextFormat;

import cn.game.core.net.client.AbstractNetClient;
import cn.game.core.net.websocket.WebSocketCodecPacket;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.Config;

/**   
 * @Description 逻辑服务器中代表的客户端,直接和客户端通讯
 * @date 2020年8月20日 下午2:44:51
 * @author SYQ
 */
@Deprecated
public class MinaGameClient extends AbstractNetClient {
	private static final Logger log = LoggerFactory.getLogger(MinaGameClient.class);
	private static final Logger gamesendLog = LoggerFactory.getLogger("gamesendLog");
	public static final String CLIENT_KEY = "CLIENT";
	public static final String PLAYID_KEY = "PLAYID";
	private long playerId;

	private String sessionId;
	private long lastRecvPacketTime;
	private String ip;
	private int port;
	/** 连接会话 */
	private IoSession session;

	public void copy(MinaGameClient client) {
		this.playerId = client.getPlayerId();
		this.lastRecvPacketTime = client.lastRecvPacketTime;
		this.sessionId = client.sessionId;
	}

	/** 客户端状态 */
	public static enum GameClientState {
		CONNECTED/* 建立连接 */, OFFCONNECTED/* 连接已断开 */, AUTHED/* 已授权 */, IN_GAME/* 游戏中 */, MANAGER /* 管理 */, LOGOUT/* 退出 */;
	}

	/** 客户端当前状态 */
	public GameClientState state;

	public MinaGameClient(IoSession session, String sessionId) {
//		this.sessionId = session.getId();
		this.sessionId = sessionId;
		this.session = session ; 
		log.debug("GameClient sessionId[{}]", sessionId);
	}
	public MinaGameClient(IoSession session) {
		this.session = session;
		log.debug("GameClient sessionId[{}]", sessionId);
	}

	private boolean sendProtocol(MessageLite message, int errorCode) {

		if (message == null) {
			throw new IllegalArgumentException("message can not be null ");
		}
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		
		WebSocketCodecPacket resp = WebSocketCodecPacket.buildPacket(IoBuffer.wrap(message.toByteArray()), msgId, errorCode);
		if (session == null || session.isClosing() || !session.isConnected()) {
			log.warn("player[{}] write message[{}] err,session[{}]", playerId,TextFormat.shortDebugString((Message) message), session);
		} else {
			session.write(resp);
		}
		if (Config.recordSendData) {
			if (msgId != PbProtocol.PlayerHeartbeatResponse_01000006) {
				gamesendLog.info("opType[send]{}code[{}]msgId[{}]msgName[{}]msgValue[{}]", this, errorCode, msgId, message.getClass()
						.getSimpleName(),
						TextFormat.shortDebugString((Message) message));
			}
		}
		return true;
	}
	/**
	 * @Title: isIdleTimeOut
	 * @Description: 空闲超时
	 * @return
	 * @return boolean 返回类型
	 */
	public boolean isIdleTimeOut(long timeOut) {
		long time = System.currentTimeMillis() - lastRecvPacketTime - timeOut;
		return time > 0;
	}

	@Override
	public long getPlayerId() {
		return playerId;
	}

	@Override
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	public void setLastRecvPacketTime(long lastRecvPacketTime) {
		this.lastRecvPacketTime = lastRecvPacketTime;
	}

	@Override
	public String toString() {

		return "GameClient" + "[" + playerId + "]" + "SessionId" + "[" + sessionId + "]" + "name" + "["
				+ ("") + "]";
	}

	@Override
	public void sendProtocol(Object message) {
		sendProtocol(message, 0);
	}

	@Override
	public String getIp() {
		return ip;
	}

	@Override
	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	@Override
	public void sendProtocol(Object message, int errorCode) {

		if (message instanceof Message) {
			sendProtocol((Message) message, errorCode);
		} else if (message instanceof Builder) {
			sendProtocol(((Builder) message).build(), errorCode);
		} else {
			throw new IllegalArgumentException("not support message ：" + message);
		}
	}
	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

}
