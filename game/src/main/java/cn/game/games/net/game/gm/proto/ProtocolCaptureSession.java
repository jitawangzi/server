package cn.game.games.net.game.gm.proto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.game.games.net.client.GameClient;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.HexUtil;

/**
 * 协议模拟单次请求捕获会话 (DC-04, DR-03)
 * 记录单次协议模拟在玩家虚拟线程窗口内的回包捕获上下文与推送包列表。
 *
 * @author shuyongqiang
 */
public class ProtocolCaptureSession {

	private final long targetPlayerId;
	private final GameClient boundClient;
	private final boolean mirrorToRealClient;
	private final AtomicInteger indexCounter = new AtomicInteger(0);
	private final List<CapturedPacket> capturedPackets = Collections.synchronizedList(new ArrayList<>());

	/**
	 * 构造协议捕获会话
	 *
	 * @param targetPlayerId     目标玩家ID
	 * @param boundClient        绑定的客户端实例
	 * @param mirrorToRealClient 是否同步镜像至真实客户端
	 */
	public ProtocolCaptureSession(long targetPlayerId, GameClient boundClient, boolean mirrorToRealClient) {
		this.targetPlayerId = targetPlayerId;
		this.boundClient = boundClient;
		this.mirrorToRealClient = mirrorToRealClient;
	}

	/**
	 * 判定是否匹配当前客户端实例或玩家ID
	 *
	 * @param client 发送方客户端
	 * @return 是否属于本次捕获会话
	 */
	public boolean matchesClient(GameClient client) {
		if (client == null) {
			return false;
		}
		if (this.boundClient != null && this.boundClient == client) {
			return true;
		}
		return client.getPlayerId() > 0 && client.getPlayerId() == this.targetPlayerId;
	}

	/**
	 * 记录捕获到的协议包
	 *
	 * @param msgId     协议号
	 * @param seq       消息序号
	 * @param data      二进制Protobuf载荷
	 * @param errorCode 错误码
	 */
	public void capture(int msgId, int seq, byte[] data, int errorCode) {
		int index = indexCounter.incrementAndGet();
		String msgName = PbProtocol.idNameMap != null ? PbProtocol.idNameMap.get(msgId) : null;
		if (msgName == null) {
			msgName = "UnknownMsg_" + HexUtil.toHexString(msgId);
		}
		CapturedPacket packet = new CapturedPacket(index, msgId, HexUtil.toHexString(msgId), msgName, seq, errorCode, data);
		capturedPackets.add(packet);
	}

	public long getTargetPlayerId() {
		return targetPlayerId;
	}

	public GameClient getBoundClient() {
		return boundClient;
	}

	public boolean isMirrorToRealClient() {
		return mirrorToRealClient;
	}

	public List<CapturedPacket> getCapturedPackets() {
		synchronized (capturedPackets) {
			return new ArrayList<>(capturedPackets);
		}
	}

	/**
	 * 捕获到的单一协议包封装
	 */
	public static class CapturedPacket {
		private final int index;
		private final int msgId;
		private final String msgIdHex;
		private final String msgName;
		private final int seq;
		private final int errorCode;
		private final byte[] data;
		private Object payload;

		public CapturedPacket(int index, int msgId, String msgIdHex, String msgName, int seq, int errorCode, byte[] data) {
			this.index = index;
			this.msgId = msgId;
			this.msgIdHex = msgIdHex;
			this.msgName = msgName;
			this.seq = seq;
			this.errorCode = errorCode;
			this.data = data;
		}

		public int getIndex() {
			return index;
		}

		public int getMsgId() {
			return msgId;
		}

		public String getMsgIdHex() {
			return msgIdHex;
		}

		public String getMsgName() {
			return msgName;
		}

		public int getSeq() {
			return seq;
		}

		public int getErrorCode() {
			return errorCode;
		}

		public byte[] getData() {
			return data;
		}

		public Object getPayload() {
			return payload;
		}

		public void setPayload(Object payload) {
			this.payload = payload;
		}
	}
}
