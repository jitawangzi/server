package cn.game.games.net.client;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
import cn.game.core.execute.RequestTiming;
import cn.game.core.net.client.AbstractNetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.protocol.bytes.ByteArrayProtocol;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import cn.game.util.HexUtil;
import cn.game.util.config.ConfigUtil;
import cn.game.util.log.LoggerType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.internal.buffer.BufferInternal;

/**   
 * 逻辑服务器中代表的客户端,直接和客户端通讯
 * 2020年8月20日 下午2:44:51
 * @author SYQ
 */
public class GameClient extends AbstractNetClient {
	private static final Logger log = LoggerFactory.getLogger(GameClient.class);
	private static final Logger gamesendLog = LoggerFactory.getLogger("gamesendLog");
	public static final String CLIENT_KEY = "CLIENT";
	public static final String PLAYID_KEY = "PLAYID";

	public static boolean recordMessageLength = ConfigUtil.getBooleanConfig("recordMessageLength"); 
	
	private volatile long lastRecvPacketTime;
	/** 连接会话 */
	private ServerWebSocket channel;

	/** 当前处理的消息序号 */
	private volatile int curMessageSeq;
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
	private final Object recentLock = new Object();
	/** 本连接已登记的正序号请求，覆盖排队、执行及拒绝回包；与响应缓存共用锁。 */
	private final Map<Integer, ProtocolSubmission> pendingRequests = new HashMap<>();
	/** @param request 本次请求身份 @param cachedResponses 已完成请求的快照，防止排队期间缓存淘汰后误报未执行 */
	private record ProtocolSubmission(IProtocol<?> request, List<IProtocol<byte[]>> cachedResponses) {}

	public int packetMaxCountPerSecond = 0;
	public long firstPacketTime = System.currentTimeMillis();

	/** 
	 * 客户端重连时的属性复制
	 * @param client
	 */
	public void copy(GameClient client) {
		this.playerId = client.getPlayerId();
		this.recentMessages = client.getRecentMessages();
//		this.context = client.getContext();
		this.lastRecvPacketTime = client.lastRecvPacketTime;
//		this.curMessageSeq = client.getCurMessageSeq();
	}
	/** 
	 * 客户端登录时的属性复制
	 * @param client
	 */
	public void copyClintLoign(GameClient client) {
		this.playerId = client.getPlayerId();
//		this.context = client.getContext();
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
	 * 连接是否活跃
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

	private boolean sendProtocol(MessageLite message, int seq, int errorCode, boolean record) {

		if (message == null) { 
			throw new IllegalArgumentException("message can not be null "); 
		}
		int msgId = PbProtocol.getInstance().getMsgId(message.getClass().getSimpleName());
		boolean flag = sendProtocol(msgId, seq > 0 ? seq : curMessageSeq, message.toByteArray(), errorCode, record);
		if (flag) {
			if (msgId != PbProtocol.PlayerHeartbeatResponse_01000006) {
				if (LoggerType.Net.logger.isInfoEnabled()) {
					LoggerType.Net.logger.info("opType[send]{}errorCode[{}]msgId[{}]msgName[{}]msgValue[{}]seq[{}]", this, errorCode,
							HexUtil.toHexString(msgId), message.getClass().getSimpleName(), TextFormat.shortDebugString((Message) message),
							curMessageSeq);
				}
			}
			if (ServerContext.getInstance().getRunMode().isPressure() && ServerContext.getInstance().isPressureDev()) {
				recvMessages.putIfAbsent(curMessageSeq, Pair.of(message.getClass().getSimpleName(), System.nanoTime()));
			}
			return true;
		}
		log.warn("GameClient[{}] write message[{}] err", this, TextFormat.shortDebugString((Message) message));
		return false;

	}

	/** 
	 * 最终的发消息方法， 这个方法一般不要直接调用，不会记录数据发送日志。 
	 * 这里不方便记录消息名字、消息内容
	 * @param msgId
	 * @param seq
	 * @param data
	 * @param errorCode
	 * @param recored
	 * @return
	 */
	public boolean sendProtocol(int msgId, int seq, byte[] data, int errorCode, boolean recored) {
		RequestTiming timing = RequestTiming.response(this, seq, msgId);
		boolean observed = false;
		try {

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
			BufferInternal buffer = BufferInternal.buffer(compositeBuffer);
			io.vertx.core.Future<Void> write = channel.writeBinaryMessage(buffer);
			if (timing != null) { timing.written(write, errorCode); observed = true; }
//			if (msgId == PbProtocol.PlayerLoginResponse_01000002) {
//				log.error("player[{}] sendProtocol[{}] seq[{}] errorCode[{}]data length[{}] First 20 bytes[{}]", playerId, msgId, seq, errorCode, data.length,
//						ByteHelp.toString(data, 20));
//			}
			// 调试用
			if (recordMessageLength) {
				LoggerType.Stdout.logger.info("opType[sendWithLength]{}errorCode[{}]msgId[{}]msgLength[{}]seq[{}]", this, errorCode,
						HexUtil.toHexString(msgId),buffer.length(),curMessageSeq);
			}
			
			if (recored && seq > 0) {
				// 记录seq对应下发的数据，相同的seq直接返回老数据
				IProtocol<byte[]> protocol = new ByteArrayProtocol(msgId, data, seq, errorCode);
				synchronized (recentLock) {
					List<IProtocol<byte[]>> list = this.recentMessages.get(seq);
					if (list == null) {
						list = new ArrayList<>();
						this.recentMessages.put(seq, list);
					}
					list.add(protocol);
				}
			}

			return true;
		}
		return false;
		} finally { if (timing != null && !observed) { timing.failed(); } }
	}
//	private boolean sendProtocol(int msgId, byte[] data, int errorCode) {
//		return sendProtocol(msgId, curMessageSeq, data, errorCode, true);
//	}

//	private boolean sendProtocol(int msgId, byte[] data, int errorCode, boolean record) {
//		return sendProtocol(msgId, curMessageSeq, data, errorCode, record);
//	}
//
//	private boolean sendProtocol(int msgId, byte[] data) {
//		return sendProtocol(msgId, data, 0);
//	}
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
		return "PlayerId[" + playerId + "]";
//		return toDetailString();
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
		sendProtocol(message, curMessageSeq, errorCode, true);
	}

	/**
	 * 本连接的正序号在入队前去重；已有缓存仍正常入队，保留原执行入口的限流顺序。
	 * @param request 原请求，零或负序号保留既有业务检查
	 * @return 是否登记成功并允许提交
	 */
	@Override
	public boolean beginProtocolSubmission(IProtocol<?> request) {
		int seq = request.getSeq();
		if (seq <= 0) { return true; }
		synchronized (recentLock) {
			if (pendingRequests.containsKey(seq)) { return false; }
			List<IProtocol<byte[]>> cached = recentMessages.get(seq);
			List<IProtocol<byte[]>> snapshot = null;
			if (cached != null && !cached.isEmpty()) {
				snapshot = new ArrayList<>(cached);
			} else {
				int current = curMessageSeq;
				if (current > 0 && seq <= current) { return false; }
			}
			pendingRequests.put(seq, new ProtocolSubmission(request, snapshot));
			return true;
		}
	}

	/** @param request 只释放身份匹配的原登记，迟到清理不能删除新请求 */
	@Override
	public void endProtocolSubmission(IProtocol<?> request) {
		if (request.getSeq() <= 0) { return; }
		synchronized (recentLock) {
			ProtocolSubmission pending = pendingRequests.get(request.getSeq());
			if (pending != null && pending.request() == request) { pendingRequests.remove(request.getSeq()); }
		}
	}

	/**
	 * 队列外拒绝使用原请求序号；已有业务结果重放而不改报未执行，也不写入业务缓存。
	 * @param request 从未进入业务执行的原请求，保留零或负序号
	 * @param errorCode 拒绝原因对应的协议错误码
	 */
	@Override
	public void rejectProtocol(IProtocol<?> request, int errorCode) {
		if (request.getSeq() > 0) {
			List<IProtocol<byte[]>> snapshot = null;
			synchronized (recentLock) {
				ProtocolSubmission pending = pendingRequests.get(request.getSeq());
				if (pending != null && pending.request() != request) { return; }
				if (pending != null) { snapshot = pending.cachedResponses(); }
				if (snapshot == null) {
					List<IProtocol<byte[]>> cached = recentMessages.get(request.getSeq());
					if (cached != null && !cached.isEmpty()) { snapshot = new ArrayList<>(cached); }
				}
				int current = curMessageSeq;
				if (snapshot == null && current > 0 && request.getSeq() <= current) { return; }
			}
			if (snapshot != null) {
				for (IProtocol<byte[]> response : snapshot) {
					sendProtocol(response.getMsgID(), response.getSeq(), response.getData(), response.getErrorCode(), false);
				}
				return;
			}
		}
		sendProtocol(PbProtocol.PlayerErrorPush_01000099, request.getSeq(),
				PlayerErrorPush_01000099.getDefaultInstance().toByteArray(), errorCode, false);
	}

	private void sendProtocol(Object message, int seq, int errorCode, boolean record) {
		if (message instanceof Message) {
			sendProtocol((Message) message, seq, errorCode, record);
		} else if (message instanceof Builder) {
			sendProtocol(((Builder) message).build(), seq, errorCode, record);
		} else {
			throw new IllegalArgumentException("not support message ：" + message);
		}
	}

	@Override
	public void close() {
		channel.close().onSuccess(r -> {
			log.info("client[{}] close success", this);
		}).onFailure(e -> {
			log.info("client[{" + this + "}] close fail", e);
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
//				GameClientManager.getInstance().logout(this);
				log.warn("GameClient[{}] Requested too frequently,seq[{}]", toDetailString(), protocol.getSeq());
				sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), protocol.getSeq(), ErrorMsgEnum.requests_too_frequent.getId(), false);
				return false;
			} else {
				packetMaxCountPerSecond = 0;
				firstPacketTime = now;
			}
		}
		int seq = protocol.getSeq();
		if (seq < 0) {
			return true;
		}
		List<IProtocol<byte[]>> snapshot = null;
		synchronized (recentLock) {
			List<IProtocol<byte[]>> list = this.recentMessages.get(seq);
//		// 这个seq的消息处理过了
			if (list != null && !list.isEmpty()) {
				snapshot = new ArrayList<>(list);
			}
		}
		if (snapshot != null) {
			for (IProtocol<byte[]> send : snapshot) {
				sendProtocol(send.getMsgID(), send.getSeq(), send.getData(), send.getErrorCode(), false);
				log.info("GameClient[{}]send processed messages msgId[{}] seq[{}]", this, send.getMsgID(), send.getSeq());
			}
			return false;
		}
		// 正在处理中,这个时候客户端不应该重复发请求,也有可能是服务端没有返回对应seq的包，注意观察上下文日志
		if (curMessageSeq > 0 && seq == curMessageSeq) {
			log.warn("GameClient[{}] msgId[{}] seq[{}] is processing", this, protocol.getMsgID(), seq);
			return false;
		}
		// seq小于当前处理的消息序号，不处理
		if (curMessageSeq > 0 && seq < curMessageSeq) {
			log.warn("GameClient[{}] msgId[{}] seq[{}] is less than curMessageSeq[{}]", this, protocol.getMsgID(), seq, curMessageSeq);
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
		synchronized (recentLock) {
			this.recentMessages.clear();
		}
	}
}
