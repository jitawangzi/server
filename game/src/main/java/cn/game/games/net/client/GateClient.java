package cn.game.games.net.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.client.AbstractNetClient;

/**
 * 网关保持和客户端的连接
 * 2017年4月5日 下午3:47:42
 * @author SYQ
 */
public class GateClient extends AbstractNetClient {

	public static final String CLIENT_KEY = "CLIENT";
	public static final String PLAYID_KEY = "PLAYID";
	public static final String USERID_KEY = "USERID";
	private static final Logger log = LoggerFactory.getLogger(GameClient.class);
	private static final Logger onlineLog = LoggerFactory.getLogger("onlineLog");

	/** 连接会话 */
//	private IoSession session;

	// -------------------------------------------
	// 帐号信息
	// -------------------------------------------
	private String ip;
	private int port;
	private long playerId;
	private byte[] sessionAddr;
	private String sessionId;

	/** 当前所在逻辑服务器地址 */
	private byte[] gameServerAddr;

	@Override
	public void sendProtocol(Object message) {
//		if (session == null || session.isClosing() || !session.isConnected()) {
//			log.warn("write message[{}]err,session[{}]", message, session);
//		} else {
//			session.write(message);
//		}
	}

	public GateClient() {
//		this.gameCrypt = gameCrypt;
//		this.session = ioSession;
		// this.sessionId = IdUtil.getId(IdType.SESSION) ;
		// this.sessionAddr = ByteHelp.toByteArray(sessionId) ;
		// log.debug("GateClient sessionIdp[{}],
		// sessionAddr[{}]",sessionId,Arrays.toString(sessionAddr));

	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public long getPlayerId() {
		return playerId;
	}

	@Override
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public byte[] getGameServerAddr() {
		return gameServerAddr;
	}

	public void setGameServerAddr(byte[] gameServerAddr) {
		this.gameServerAddr = gameServerAddr;
	}

	public byte[] getSessionAddr() {
		return sessionAddr;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionAddr(byte[] sessionAddr) {
		this.sessionAddr = sessionAddr;
	}

	@Override
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public void sendProtocol(Object message, int errorCode) {
		// TODO Auto-generated method stub
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	@Override
	public String toString() {

		return "GateClient" + "[" + playerId + "]" + "SessionId" + "[" + sessionId + "]";
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
