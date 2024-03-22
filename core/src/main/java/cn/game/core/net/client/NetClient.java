package cn.game.core.net.client;

import cn.game.core.net.protocol.IProtocol;
import io.vertx.core.Context;
import io.vertx.core.impl.ContextInternal;

public interface NetClient {

	/** 
	 * 是否需要处理这个消息，比如重复消息不应该在处理
	 * @param message
	 * @return
	 */
	boolean needProcess(IProtocol<?> protocol);

	void afterProcess(IProtocol<?> protocol);

	void sendProtocol(Object message);

	void sendProtocol(Object message, int errorCode);

	long getPlayerId();

	void setPlayerId(long id);
	
	String getSessionId();

	void setSessionId(String sessionId);
	
	void setIp(String ip);
	
	String getIp();

	public Context getContext();

	public void setContext(ContextInternal context);

	/**
	 * @Description 连接是否活跃
	 * @return
	 */
	boolean isActive();

	void close();
}
