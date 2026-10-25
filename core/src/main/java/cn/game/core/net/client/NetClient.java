package cn.game.core.net.client;

import cn.game.core.net.protocol.IProtocol;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;
import io.vertx.core.Context;
import io.vertx.core.internal.ContextInternal;

public interface NetClient {

	/** 
	 * 是否需要处理这个消息，比如重复消息不应该再次处理
	 * @param message
	 * @return
	 */
	boolean needProcess(IProtocol<?> protocol);

	void afterProcess(IProtocol<?> protocol);

	/**
	 * 入队前登记本次请求；实现可重放已完成结果或抑制在途重发，不执行正常业务检查。
	 * @param request 本次独立请求对象
	 * @return true 表示允许提交，必须在终态调用 endProtocolSubmission
	 */
	default boolean beginProtocolSubmission(IProtocol<?> request) { return true; }

	/**
	 * 释放本次登记，拒绝时在回复发送调用结束后执行；不能误删另一请求的登记。
	 * @param request 原登记请求对象
	 */
	default void endProtocolSubmission(IProtocol<?> request) {}

	void sendProtocol(Object message);

	void sendProtocol(Object message, int errorCode);

	/**
	 * 在业务尚未开始时回复拒绝；有请求序号的实现必须绑定原请求，且不得记录业务成功缓存。
	 * @param request 未执行的原请求
	 * @param errorCode 既有协议错误码
	 */
	default void rejectProtocol(IProtocol<?> request, int errorCode) {
		sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), errorCode);
	}

	long getPlayerId();

	void setPlayerId(long id);
	
	String getSessionId();

	void setSessionId(String sessionId);
	
	void setIp(String ip);
	
	String getIp();

	/**
	 * 连接是否活跃
	 * @return
	 */
	boolean isActive();

	void close();
}
