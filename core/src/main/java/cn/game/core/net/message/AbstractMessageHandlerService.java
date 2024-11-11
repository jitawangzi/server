package cn.game.core.net.message;

import cn.game.util.ServerType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;

public abstract class AbstractMessageHandlerService extends AbstractVerticle implements MessageHandlerService {
	protected final String serverId;
	protected final ServerType serverType;

	public AbstractMessageHandlerService(String serverId, ServerType serverType) {
		this.serverId = serverId;
		this.serverType = serverType;
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		init();
		super.start(startPromise);
	}

	@Override
	public void init() {
		Handler<Message<Object>> handler = this::handleMessage;
		initConsumer(handler);
	}

	/** 
	 * 注册处理器监听的地址
	 * @param handler
	 */
	public abstract void initConsumer(Handler<Message<Object>> handler);

	@Override
	public abstract void handleMessage(Message<Object> message);

	@Override
	public boolean shutdown() {
		// 实现通用的关闭逻辑
		return true;
	}
}