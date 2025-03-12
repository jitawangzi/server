package cn.game.core.net.rpc.vertx;

import cn.game.core.net.rpc.RpcClient;
import cn.game.core.util.AsyncUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

public class VertxRpcClient extends AbstractVerticle implements RpcClient {

	public VertxRpcClient() {
	}

	@Override
	public <T> void request(String addr, T message, Handler<AsyncResult<Message<T>>> replyHandler) {
		if (replyHandler != null) {
			vertx.eventBus().request(addr, message, replyHandler);
		} else {
			vertx.eventBus().send(addr, message);
		}
	}
	@Override
	public <T> Future<Message<T>> request(String addr, T message) {
		return vertx.eventBus().request(addr, message);
	}
	@Override
	public <T> Future<Message<T>> request(String addr, T message, DeliveryOptions options) {
		return vertx.eventBus().request(addr, message, options);
	}

	@Override
	public <T> void send(String addr, T message) {
		vertx.eventBus().send(addr, message);
	}

	@Override
	public boolean checkAllowSync() {
		AsyncUtils.checkEventLoop();
		return true;
	}

	@Override
	public <T> void send(T message) {
		throw new UnsupportedOperationException();
	}


	@Override
	public <T> void broadcast(String addr, T message) {
		vertx.eventBus().publish(addr, message);
	}

}
