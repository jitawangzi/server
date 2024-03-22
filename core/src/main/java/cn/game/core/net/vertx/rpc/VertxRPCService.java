package cn.game.core.net.vertx.rpc;

import org.apache.commons.lang.exception.ExceptionUtils;

import cn.game.core.net.rpc.RPCService;
import cn.game.core.net.transport.Command;
import cn.game.core.net.transport.Result;
import cn.game.core.net.vertx.VxHolder;
import cn.game.util.KryoUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.ReplyFailure;

public class VertxRPCService<T> extends AbstractVerticle implements RPCService<T> {
//	private static final Logger log = LoggerFactory.getLogger(VertxRPCService.class);

	private T wrappedService;

	private String serverId;

	public VertxRPCService(T wrappedService) {
		this.wrappedService = wrappedService;
	}
	public VertxRPCService(T wrappedService, String serverId) {
		this.wrappedService = wrappedService;
		this.serverId = serverId;
	}


	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		init();
		super.start(startPromise);
	}
	@Override
	public void start() {

	}
	@Override
	@SuppressWarnings({ "unchecked" })
	
	public void init() {
		vertx.eventBus().consumer(VxHolder.rpcServiceAddr(serverId), msg -> {
			byte[] datas = (byte[]) msg.body();

			vertx.executeBlocking(promise -> {
				Object result = null;
				try {
					Command command = KryoUtils.deserialize(datas, Command.class);
					result = invoke(command);

				} catch (Throwable e) {
					log.error("", e);
					result = e;
				}
				if (result instanceof Future) {
					((Future<Object>) result).onComplete(r -> {
						if (r.succeeded()) {
							promise.complete(r.result());
						} else {
							promise.fail(r.cause());
						}
					});
				} else if (result instanceof Throwable) {
					promise.fail((Throwable) result);
				} else {
					promise.complete(result);
				}
			}, false, res -> {
				if (res.failed()) {
					msg.fail(ReplyFailure.ERROR.toInt(),
							res.cause().getMessage() + "  " + ExceptionUtils.getFullStackTrace(res.cause()));
				} else {
					Result resp = new Result(res.result());
					byte[] respDatas = KryoUtils.serialize(resp);
					msg.reply(respDatas);
				}
			});
		});
		log.info("vertx  rpc service start ...");

	}

	@Override
	public boolean shutdown() {
		
		log.info("{} start shutdown.... ",this.getClass().getSimpleName());
		return true;
	}

	@Override
	public T getWrappedService() {
		return wrappedService;
	}

}
