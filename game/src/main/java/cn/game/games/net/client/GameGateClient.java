package cn.game.games.net.client;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;
import com.google.protobuf.TextFormat;

import cn.game.core.net.client.AbstractNetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.zmq.ZmqPairSender;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.ByteHelp;
import cn.game.util.Config;

/**
 * @Description game---gate客户端
 * 2017年3月27日 下午2:36:37
 * @author SYQ
 */
public class GameGateClient extends AbstractNetClient {
	private static final Logger log = LoggerFactory.getLogger(GameGateClient.class);
	private static final Logger gamesendLog = LoggerFactory.getLogger("gamesendLog");
	public static final String CLIENT_KEY = "CLIENT";
	public static final String PLAYID_KEY = "PLAYID";
	private long playerId;

	/** 当前所在逻辑服务器id */
	private int serverId;

	/** 网关服务器地址 */
	private byte[] gateServerAddr;
	private String sessionId;
	private byte[] sessionAddr;
	private long lastRecvPacketTime;
	private Player player;
	private String ip;
	private int port;

	/** 客户端状态 */
	public static enum GameClientState {
		CONNECTED/* 建立连接 */, OFFCONNECTED/* 连接已断开 */, AUTHED/* 已授权 */, IN_GAME/* 游戏中 */, MANAGER /* 管理 */, LOGOUT/* 退出 */;
	}

	/** 客户端当前状态 */
	public GameClientState state;

	public GameGateClient(byte[] gateServerAddr, String sessionId) {

		this.gateServerAddr = gateServerAddr;
		this.sessionId = sessionId;
		this.sessionAddr = sessionId.getBytes();
		log.debug("GameClient sessionIdp[{}], sessionAddr[{}]", sessionId, Arrays.toString(sessionAddr));

	}

	public GameGateClient(String sessionId) {
		this.sessionId = sessionId;
		log.debug("GameClient sessionId[{}]", sessionId);
	}

	private boolean sendProtocol(MessageLite message, int errorCode) {

		if (message == null) {
			throw new IllegalArgumentException("message can not be null ");
		}
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());

		send(msgId, errorCode, message.toByteArray());
		if (Config.recordSendData) {
			if (msgId != PbProtocol.PlayerHeartbeatResponse_01000006) {
				gamesendLog.info("opType[send]{}errorCode[{}]msgName[{}]msgValue[{}]", this, errorCode,
						message.getClass().getSimpleName(),
						TextFormat.shortDebugString((Message) message));
			}
		}

		return true;
	}

	private boolean send(int msgId, int errorCode, byte[] datas) {

		ZMsg msg = new ZMsg();
		msg.add(gateServerAddr);
		msg.add("");
		msg.add(sessionAddr);
		msg.add(ByteHelp.toByteArrayB(msgId));
		msg.add(ByteHelp.toByteArrayB(errorCode));
		msg.add(datas);

		try {
			ZmqPairSender.getInstance().put(msg);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(this + "send message to gate error", e);
			return false;
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

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
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

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public byte[] getSessionAddr() {
		return sessionAddr;
	}

	public void setLastRecvPacketTime(long lastRecvPacketTime) {
		this.lastRecvPacketTime = lastRecvPacketTime;
	}

	@Override
	public String toString() {

		return "GameClient" + "[" + playerId + "]" + "SessionId" + "[" + sessionId + "]" + "name" + "["
				+ (player == null ? "" : player.getData().getName() == null ? "" : player.getData().getName()) + "]";
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
