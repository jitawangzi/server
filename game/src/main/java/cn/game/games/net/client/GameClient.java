package cn.game.games.net.client;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;
import com.google.protobuf.TextFormat;

import cn.game.core.base.ServerContext;
import cn.game.core.net.client.AbstractNetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.bytes.BaseByteProtocol;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.Config;
import cn.game.util.HexUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

/**   
 * @Description 逻辑服务器中代表的客户端,直接和客户端通讯
 * @date 2020年8月20日 下午2:44:51
 * @author SYQ
 */
public class GameClient extends AbstractNetClient {
	private static final Logger log = LoggerFactory.getLogger(GameClient.class);
	private static final Logger gamesendLog = LoggerFactory.getLogger("gamesendLog");
	public static final String CLIENT_KEY = "CLIENT";
	public static final String PLAYID_KEY = "PLAYID";

	private long lastRecvPacketTime;
	/** 连接会话 */
	private ServerWebSocket channel;

	/** 当前处理的消息序号 */
	private int curMessageSeq;
	private static final int MAX_RECENT_MESSAGES = 10;
	// 每秒最多处理10个消息
	/** 发送给玩家最近的几条消息，如果客户端没有收到某个包，重新请求时会下发 ， 
	 * key: 消息序号 value: 消息序号对应的返回包
	 * 断线重连时需要保留。*/
	Map<Integer, List<IProtocol<byte[]>>> recentMessages = new LinkedHashMap<Integer, List<IProtocol<byte[]>>>(MAX_RECENT_MESSAGES + 1,
			0.75f, true) {
		/**  */
		private static final long serialVersionUID = -5578130449212611692L;

		@Override
		public boolean removeEldestEntry(Map.Entry<Integer, List<IProtocol<byte[]>>> eldest) {
			return size() > MAX_RECENT_MESSAGES;
		}
	};

	public int packetMaxCountPerSecond = 0;
	public long firstPacketTime = System.currentTimeMillis();

	/** 
	 * 客户端重连时的属性复制
	 * @param client
	 */
	public void copy(GameClient client) {
		this.playerId = client.getPlayerId();
		this.recentMessages = client.getRecentMessages();
		this.context = client.getContext();
//		this.curMessageSeq = client.getCurMessageSeq();
//		this.lastRecvPacketTime = client.lastRecvPacketTime;
	}
	/** 
	 * 客户端登录时的属性复制
	 * @param client
	 */
	public void copyClintLoign(GameClient client) {
		this.playerId = client.getPlayerId();
		this.context = client.getContext();
	}

	/** 客户端状态 */
	public static enum GameClientState {
		CONNECTED/* 建立连接 */, OFFCONNECTED/* 连接已断开 */, AUTHED/* 已授权 */, IN_GAME/* 游戏中 */, MANAGER /* 管理 */, LOGOUT/* 退出 */;
	}

	/** 客户端当前状态 */
	public GameClientState state;

	public GameClient(ServerWebSocket ws) {
		this.channel = ws;
	}

	/**
	 * @Description 连接是否活跃
	 * @return
	 */
	@Override
	public boolean isActive() {
		if (channel == null) {
			log.warn("client[{}]  channel is null ", this);
			return false;
		}
//		if (!channel.isActive()) {
//			log.warn("client[{}]  channel is not  active", this);
//			return false;
//		}
//		if (!channel.isWritable()) {
//			log.warn("client[{}]  channel is not  writable", this);
//			return false;
//		}
		if (channel.isClosed()) {
			log.warn("client[{}]  channel is closed", this);
			return false;
		}
		return true;
	}

	private boolean sendProtocol(MessageLite message, int errorCode) {

		if (message == null) { 
			throw new IllegalArgumentException("message can not be null "); 
		}
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		boolean flag = sendProtocol(msgId, message.toByteArray(), errorCode);
		if (flag) {
			if (Config.recordSendData) {
				if (msgId != PbProtocol.PlayerHeartbeatResponse_01000006) {
					gamesendLog.info("opType[send]{}errorCode[{}]msgId[{}]msgName[{}]msgValue[{}]seq[{}]", this,
							errorCode,
							HexUtil.toHexString(msgId),
							message.getClass().getSimpleName(), TextFormat.shortDebugString((Message) message),
							curMessageSeq);
				}
			}
			if (ServerContext.getInstance().getRunMode().isPressure()) {
				recvMessages.put(curMessageSeq, Pair.of(message.getClass().getSimpleName(), System.nanoTime()));
			}
			return true;
		}
		log.warn("player[{}] write message[{}] err,sessionId[{}],session[{}]", playerId, TextFormat.shortDebugString((Message) message),
				sessionId, channel);
		return false;

	}
	public boolean sendProtocol(int msgId, int seq, byte[] data, int errorCode, boolean recored) {

//		ProtobufProtocol protobufProtocol = new ProtobufProtocol(msgId, data, errorCode);
		if (isActive()) {
			CompositeByteBuf compositeBuffer = Unpooled.compositeBuffer(2);
			ByteBuf headerBuf = Unpooled.buffer(16);
			headerBuf.writeInt(data.length + 16);
			headerBuf.writeInt(seq);
			headerBuf.writeInt(msgId);
			headerBuf.writeInt(errorCode);
			ByteBuf bodyBuf = Unpooled.wrappedBuffer(data);
			compositeBuffer.addComponents(headerBuf, bodyBuf);
			compositeBuffer.writerIndex(headerBuf.readableBytes() + bodyBuf.readableBytes());
			
			channel.writeBinaryMessage(Buffer.buffer(compositeBuffer));
			
			if (recored && seq > 0) {
				// 记录seq对应下发的数据，相同的seq直接返回老数据
				IProtocol<byte[]> protocol = new BaseByteProtocol(msgId, data, seq, errorCode);
				List<IProtocol<byte[]>> list = this.recentMessages.get(seq);
				if (list == null) {
					list = new ArrayList<>();
					this.recentMessages.put(seq, list);
				}
				list.add(protocol);
			}
			return true;
		}
		return false;
	}
	/** 
	 * 客户端默认发送协议方法，记录当前的数据包序列。 
	 * @param msgId
	 * @param data
	 * @param errorCode
	 * @return
	 */
	public boolean sendProtocol(int msgId, byte[] data, int errorCode) {

		return sendProtocol(msgId, curMessageSeq, data, errorCode, true);
	}
	public boolean sendProtocol(int msgId, byte[] data) {

		return sendProtocol(msgId, data, 0);
	}
	/**
	 * @Title: isIdleTimeOut
	 * @Description: 空闲超时
	 * @return
	 */
	public boolean isIdleTimeOut(long timeOut) {
		long time = System.currentTimeMillis() - lastRecvPacketTime - timeOut;
		return time > 0;
	}

	public void setLastRecvPacketTime(long lastRecvPacketTime) {
		this.lastRecvPacketTime = lastRecvPacketTime;
	}

	@Override
	public String toString() {

		return "GameClient" + "[" + playerId + "]" + "sessionId" + "[" + sessionId + "]" + "name" + "["
				+ ("") + "]";
	}
	public String toDetailString() {
		
		return MessageFormat.format("GameClient:playerId[{0,number,#}]sessionId[{1}]connectionId[{2}]", playerId,
				sessionId,
				channel.binaryHandlerID());
	}

	@Override
	public void sendProtocol(Object message) {
		sendProtocol(message, 0);
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
	public void close() {
		channel.close().onSuccess(r -> {
			log.info("client[{}] close success", this);
		}).onFailure(r -> {
			log.info("client[{}] close fail", this);
		});
	}

	public ServerWebSocket getChannel() {
		return channel;
	}

	@Override
	public boolean needProcess(IProtocol<?> protocol) {
		if (packetMaxCountPerSecond++ >= 10) {
			long now = System.currentTimeMillis();
			if (now - firstPacketTime < 1000) {
//				// 超过消息数量，关闭连接
				GameClientManager.getInstance().removeGameClient(this);
				log.warn("GameClient[{}] Requested too frequently, force disconnect,seq[{}]", toDetailString(), protocol.getSeq());
			} else {
				packetMaxCountPerSecond = 0;
				firstPacketTime = now;
			}
		}
		int seq = protocol.getSeq();
		if (seq < 0) {
			return true;
		}
		List<IProtocol<byte[]>> list = this.recentMessages.get(seq);
		if (list != null && !list.isEmpty()) {
			for (IProtocol<byte[]> send : list) {
				sendProtocol(send.getMsgID(), send.getSeq(), send.getData(), send.getErrorCode(), false);
			}
			return false;
		}
		this.curMessageSeq = seq;
		return true;
	}
	@Override
	public void afterProcess(IProtocol<?> protocol) {
//		if (curMessageSeq!=0) {
//			curMessageSeq = 0;
//		}
	}

	public Map<Integer, List<IProtocol<byte[]>>> getRecentMessages() {
		return recentMessages;
	}

	public int getCurMessageSeq() {
		return curMessageSeq;
	}
	public void clearRecentMessages() {
		this.recentMessages.clear();
	}


}
