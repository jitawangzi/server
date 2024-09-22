package cn.game.core.net.rpc.vertx;

import cn.game.core.net.rpc.RPCService;
import cn.game.core.net.rpc.RPCServiceImpl;
import cn.game.core.net.transport.Command;
import cn.game.core.net.transport.Result;
import cn.game.core.net.vertx.VxHolder;
import cn.game.util.KryoUtils;
import cn.game.util.ServerType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyFailure;

public class VertxRPCService<T> extends AbstractVerticle implements RPCService<T> {
	private RPCServiceImpl<T> rpcService;

	public VertxRPCService(T wrappedService, String serverId, ServerType serverType) {
		this.rpcService = new RPCServiceImpl<>(wrappedService, serverId, serverType, null);
	}
	public VertxRPCService(T wrappedService, String serverId) {
		this.rpcService = new RPCServiceImpl<>(wrappedService, serverId, null, null);
	}
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		rpcService.vertx = getVertx(); // Set vertx instance
		init();
		super.start(startPromise);
	}

	@Override
	public void init() {

		Handler<Message<T>> handler = msg -> {
			byte[] datas = (byte[]) msg.body();

			vertx.executeBlocking(promise -> {
				Object result = null;
				try {
					Command command = KryoUtils.deserialize(datas, Command.class);
					result = rpcService.invokeWithCache(command);
				} catch (Throwable e) {
					log.error("Error invoking RPC method", e);
					result = new RPCServiceImpl.RPCException("Error invoking RPC method", e);
				}
				rpcService.handleResult(result, promise);
			}, false, res -> {
				if (res.failed()) {
					msg.fail(ReplyFailure.ERROR.toInt(), res.cause().getMessage());
				} else {
					Result resp = new Result(res.result());
					byte[] respDatas = KryoUtils.serialize(resp);
					msg.reply(respDatas);
				}
			});
		};
		vertx.eventBus().consumer(VxHolder.rpcServiceAddr(rpcService.serverId), handler);
		if (rpcService.serverType != null) {
			vertx.eventBus().consumer(VxHolder.rpcServiceAddr(rpcService.serverType.name()), handler);
		}
		log.info("Vertx RPC service started for serverId: {}", rpcService.serverId);
	}

	@Override
	public void start() {
	}

	@Override
	public T getWrappedService() {
		return rpcService.getWrappedService();
	}

	@Override
	public boolean shutdown() {
		return rpcService.shutdown();
	}
}