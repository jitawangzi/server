package cn.game.core.net.rpc.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.net.message.AbstractMessageHandlerService;
import cn.game.core.net.process.Processor;
import cn.game.core.net.rpc.RPCService;
import cn.game.core.net.rpc.RPCServiceImpl;
import cn.game.core.net.transport.Command;
import cn.game.core.net.vertx.VxHolder;
import cn.game.util.ServerType;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyFailure;

/**    
 * 注意不要处理阻塞逻辑
 * 2025年1月23日 16:39:11
 * @author SYQ
 * @param <T>
 */
public class VertxRPCService<T> extends AbstractMessageHandlerService implements RPCService<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(VertxRPCService.class);
	private RPCServiceImpl<T> rpcService;

	public VertxRPCService(T wrappedService, String serverId, ServerType serverType, Processor processor) {
		super(serverId, serverType, processor);
		this.rpcService = new RPCServiceImpl<>(wrappedService, serverId, serverType);
	}

	@Override
	public void handleMessage(Message<Object> message) {
		Command command = (Command) message.body();
		long objectId = command.getObjectId();
	    String traceId = message.headers().get("trace-id");
		String replyAddress = message.replyAddress();

		if (log.isDebugEnabled()) {
			log.debug("Received RPC command: {}, objectId: {},replyAddress: {} , traceId: {}", command, objectId, replyAddress,traceId == null ? "null" : traceId);
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
				    DeliveryOptions deliveryOptions = traceId == null? VxHolder.universalOptions: new DeliveryOptions(VxHolder.universalOptions).addHeader("trace-id", traceId);
					message.reply(r.result(), deliveryOptions);
					if (ServerContext.getInstance().getRunMode().isTest()) {
						// 发送成功后的确认
						LOGGER.info("VertxRPCService回复发送成功: replyAddress={}, traceId={}", replyAddress, traceId);
						// 验证发送状态
						vertx.setTimer(500, id -> {
							LOGGER.info("VertxRPCService回复发送后验证(500ms): replyAddress={}, traceId={}, 集群节点数={}", replyAddress, traceId, VxHolder.getClusterNodeCount());
						});
					}
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