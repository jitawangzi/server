package cn.game.core.net.rpc.vertx;

import cn.game.core.net.rpc.RpcClient;
import cn.game.core.net.vertx.VxHolder;
import cn.game.util.ServerType;
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
	public <T> void request(String serverId, T message, Handler<AsyncResult<Message<T>>> replyHandler) {
		if (replyHandler != null) {
			vertx.eventBus().request(VxHolder.rpcServiceAddr(serverId), message, replyHandler);
		} else {
			vertx.eventBus().send(VxHolder.rpcServiceAddr(serverId), message);
		}
	}
	@Override
	public <T> Future<Message<T>> request(String serverId, T message) {
		return vertx.eventBus().request(VxHolder.rpcServiceAddr(serverId), message);
	}
	@Override
	public <T> void send(String serverId, T message) {
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

	@Override
	public <T> Future<Message<T>> request(ServerType serverType, T message) {
		return vertx.eventBus().request(serverType.name(), message);
	}

	@Override
	public <T> void broadcast(ServerType serverType, T message) {
		vertx.eventBus().publish(serverType.name(), message);
	}

}
