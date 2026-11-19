package cn.game.games.net.game.gm.proto;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import cn.game.core.net.client.LogoutType;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.client.GameClient.GameClientState;
import cn.game.protocol.protobuf.PbProtocol;

/**
 * 非生产虚拟客户端 (DC-01, DC-04, EX-04)
 * 用于在无需外部真实 WebSocket 连接的情况下驱动真实 Handler 协议执行，
 * 显式防护 channel == null 空指针，并支持实例级异步登录等待。
 *
 * @author shuyongqiang
 */
public class VirtualGameClient extends GameClient {

	private volatile CompletableFuture<LoginCaptureOutcome> loginFuture;

	public VirtualGameClient() {
		this(0L);
	}

	public VirtualGameClient(long playerId) {
		super(null);
		this.playerId = playerId;
		this.sessionId = "virtual-" + (playerId > 0 ? String.valueOf(playerId) : UUID.randomUUID().toString());
		this.ip = "127.0.0.1";
		this.state = GameClientState.IN_GAME;
	}

	@Override
	public boolean isActive() {
		// 虚拟客户端在内存模拟生命周期内始终视为活跃，防止业务Handler因ws==null误判为掉线
		return true;
	}

	@Override
	public boolean sendProtocol(int msgId, int seq, byte[] data, int errorCode, boolean recored) {
		CompletableFuture<LoginCaptureOutcome> future = this.loginFuture;
		if (future != null && !future.isDone()) {
			if (msgId == PbProtocol.PlayerLoginResponse_01000002) {
				future.complete(new LoginCaptureOutcome(true, errorCode, null, msgId, seq, data));
			} else if (msgId == PbProtocol.PlayerErrorPush_01000099) {
				future.complete(new LoginCaptureOutcome(false, errorCode, "Login error push received", msgId, seq, data));
			}
		}
		return super.sendProtocol(msgId, seq, data, errorCode, recored);
	}

	public void setState(GameClientState state) {
		this.state = state;
	}

	public void setLoginFuture(CompletableFuture<LoginCaptureOutcome> loginFuture) {
		this.loginFuture = loginFuture;
	}

	public CompletableFuture<LoginCaptureOutcome> getLoginFuture() {
		return loginFuture;
	}

	/**
	 * 异步登录拦截结果
	 */
	public static class LoginCaptureOutcome {
		private final boolean ok;
		private final int errorCode;
		private final String errorDesc;
		private final int msgId;
		private final int seq;
		private final byte[] data;

		public LoginCaptureOutcome(boolean ok, int errorCode, String errorDesc, int msgId, int seq, byte[] data) {
			this.ok = ok;
			this.errorCode = errorCode;
			this.errorDesc = errorDesc;
			this.msgId = msgId;
			this.seq = seq;
			this.data = data;
		}

		public boolean isOk() {
			return ok;
		}

		public int getErrorCode() {
			return errorCode;
		}

		public String getErrorDesc() {
			return errorDesc;
		}

		public int getMsgId() {
			return msgId;
		}

		public int getSeq() {
			return seq;
		}

		public byte[] getData() {
			return data;
		}
	}
}
