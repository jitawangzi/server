package cn.game.core.net.rpc.vertx;

import cn.game.core.net.message.AbstractMessageHandlerService;
import cn.game.core.net.process.Processor;
import cn.game.core.net.rpc.RPCService;
import cn.game.core.net.rpc.RPCServiceImpl;
import cn.game.core.net.transport.Command;
import cn.game.core.net.vertx.VxHolder;
import cn.game.util.ServerType;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyFailure;

/**    
 * 注意不要处理阻塞逻辑
 * 2025年1月23日 16:39:11
 * @author SYQ
 * @param <T>
 */
public class VertxRPCService<T> extends AbstractMessageHandlerService implements RPCService<T> {
	private RPCServiceImpl<T> rpcService;

	public VertxRPCService(T wrappedService, String serverId, ServerType serverType, Processor processor) {
		super(serverId, serverType, processor);
		this.rpcService = new RPCServiceImpl<>(wrappedService, serverId, serverType);
	}

	@Override
	public void handleMessage(Message<Object> message) {
		Command command = (Command) message.body();
		long objectId = command.getObjectId();
		if (log.isDebugEnabled()) {
			log.debug("Received RPC command: {}, objectId: {},replyAddress: {}", command, objectId, message.replyAddress());
		}
		Command commandFinal = command;
		processor.process(objectId, () -> {
			Object result = null;
			Promise<Object> promise = Promise.promise();
			try {
				result = rpcService.invokeWithCache(commandFinal);
			} catch (Throwable e) {
				log.error("Error invoking RPC method", e);
				// 异常包装，稍后 reply
				result = new RPCServiceImpl.RPCException("Error invoking RPC method", e);
			}
			rpcService.handleResult(result, promise);
			promise.future().onComplete(r -> {
				if (r.failed() || r.result() instanceof Throwable) {
					String errString = r.result() == null ? "" : ((Throwable) r.result()).getMessage();
					message.fail(ReplyFailure.ERROR.toInt(), errString);
				} else {
					message.reply(r.result(), VxHolder.customOptions);
				}
			});
		},true);
	}

	@Override
	public void initConsumer(Handler<Message<Object>> handler) {
		vertx.eventBus().consumer(VxHolder.rpcServiceAddr(rpcService.serverId), handler);
		if (serverType != null) {
			vertx.eventBus().consumer(VxHolder.rpcServiceAddr(rpcService.serverType), handler);
		}
	}
}