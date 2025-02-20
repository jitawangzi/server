package cn.game.core.net.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.formula.functions.T;

import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.vertx.VxContextRegistry;
import io.vertx.core.Context;
import io.vertx.core.impl.ContextInternal;

public abstract class AbstractNetClient implements NetClient
{

	protected long playerId;
	protected String sessionId;
	protected String ip;
//	protected ContextInternal context;
	protected T player;

	// 消息序号: 消息名，消息发送时间，纳秒
	public Map<Integer, Pair<String, Long>> sendMessages = new ConcurrentHashMap<>();
	// 消息序号: 消息名，消息接收时间，纳秒
	public Map<Integer, Pair<String, Long>> recvMessages = new ConcurrentHashMap<>();

	@Override
	public long getPlayerId() {
		return playerId;
	}

	@Override
	public void setPlayerId(long id) {
		this.playerId = id;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}
	@Override
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	@Override
	public void setIp(String ip) {
		this.ip = ip;
	}
	@Override
	public String getIp() {
		return this.ip;
	}
	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public Context getContext() {
		return VxContextRegistry.getInstance().getContext(getPlayerId());
	}
	@Override
	public void setContext(ContextInternal context) {
//		this.context = context;
	}
	@Override
	public void close() {
	}

	@Override
	public boolean needProcess(IProtocol<?> protocol) {
		return true;
	}
	@Override
	public void afterProcess(IProtocol<?> protocol) {
		
	}

}
