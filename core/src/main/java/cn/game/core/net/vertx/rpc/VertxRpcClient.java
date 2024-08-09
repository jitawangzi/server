package cn.game.core.net.vertx.rpc;

import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.vertx.VxHolder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.VertxThread;

public class VertxRpcClient extends AbstractVerticle implements RpcClient {

	public VertxRpcClient() {
	}

	@Override
	public <T> void request(T message, String serverId, Handler<AsyncResult<Message<T>>> replyHandler) {
		if (replyHandler != null) {
			vertx.eventBus().request(VxHolder.rpcServiceAddr(serverId), message, replyHandler);
		} else {
			vertx.eventBus().send(VxHolder.rpcServiceAddr(serverId), message);
		}
	}
	@Override
	public <T> Future<Message<T>> request(T message, String serverId) {
		return vertx.eventBus().request(VxHolder.rpcServiceAddr(serverId), message);
	}
	@Override
	public <T> void send(T message, String serverId) {
		vertx.eventBus().send(VxHolder.rpcServiceAddr(serverId), message);
	}

	@Override
	public boolean checkAllowSync() {
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof VertxThread) {
			if (!((VertxThread) currentThread).isWorker()) {
				throw new UnsupportedOperationException("sync request can not run in vertx loop thread ");
			}
		}
		return true;
	}

	@Override
	public <T> void send(T message) {
		throw new UnsupportedOperationException();
	}

}
