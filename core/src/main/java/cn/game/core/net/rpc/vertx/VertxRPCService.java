package cn.game.core.net.rpc.vertx;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.execute.TaskNotExecutedException;
import cn.game.core.net.message.AbstractMessageHandlerService;
import cn.game.core.net.process.Processor;
import cn.game.core.net.rpc.RPCService;
import cn.game.core.net.rpc.RPCServiceImpl;
import cn.game.core.net.transport.Command;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.ExceptionHelper;
import cn.game.util.GameUtil;
import cn.game.util.ServerType;
import cn.game.protocol.manual.ErrorMsgEnum;
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
		// 回复 Promise 在入队之前建立，未执行拒绝与实际业务结果共用一个终态出口。
		Promise<Object> promise = Promise.promise();
		// 业务 Future 的失败也进入同一出口，须以本次调用的开始事实判定拒绝归属。
		AtomicBoolean invocationStarted = new AtomicBoolean();
		promise.future().onComplete(r -> {
				if (r.failed() || r.cause() != null|| r.result() instanceof Exception) {
					String errString = r.result() == null ? "" : ((Exception) r.result()).getMessage();
					if (r.cause() != null) {
						LogicException cause = ExceptionHelper.findCause(r.cause(), LogicException.class);
						if (cause != null) {
							message.reply(cause, VxHolder.universalOptions);
						} else {
							log.error("Error invoking RPC method", r.cause());
							int code = !invocationStarted.get() && r.cause() instanceof TaskNotExecutedException
									? ErrorMsgEnum.system_overload.getId() : ReplyFailure.ERROR.toInt();
							message.fail(code, r.cause().getMessage());
						}
					} else {
						if (r.result() != null && r.result() instanceof Exception) {
							log.warn("Error invoking RPC method", r.result());
						}
						message.fail(ReplyFailure.ERROR.toInt(), errString);
					}
				} else {
				    DeliveryOptions deliveryOptions = traceId == null? VxHolder.universalOptions: new DeliveryOptions(VxHolder.universalOptions).addHeader("trace-id", traceId);
					message.reply(r.result(), deliveryOptions);
					if (ServerContext.getInstance().getRunMode().isTest()) {
						// 发送成功后的确认
						LOGGER.debug("VertxRPCService回复发送成功: replyAddress={}, traceId={}", replyAddress, traceId);
						// 验证发送状态
						vertx.setTimer(500, id -> {
							LOGGER.debug("VertxRPCService回复发送后验证(500ms): replyAddress={}, traceId={}, 集群节点数={}", replyAddress, traceId, VxHolder.getClusterNodeCount());
						});
					}
				}
		});
		processor.processAsync(objectId, () -> {
			invocationStarted.set(true);
			Object result;
			try {
				result = invokeCommand(command);
			} catch (Exception e) {
				log.warn("RPC invocation failed for object {}, command {}", objectId, command, e);
				result = new RPCServiceImpl.RPCException("Error invoking RPC method", e);
			}
			rpcService.handleResult(result, promise);
		}, true).onFailure(promise::tryFail);
	}

	/**
	 * 调用已解析的业务命令，默认沿用既有服务查找与反射入口。
	 * @param command 请求中的业务方法与参数
	 * @return 业务结果或异步结果
	 * @throws Exception 业务查找或执行失败
	 */
	protected Object invokeCommand(Command command) throws Exception {
		return rpcService.invokeWithCache(command);
	}

	@Override
	public void initConsumer(Handler<Message<Object>> handler) {
		vertx.eventBus().consumer(VxHolder.rpcServiceAddr(rpcService.serverId), handler);
		if (serverType != null) {
			vertx.eventBus().consumer(VxHolder.rpcServiceAddr(rpcService.serverType), handler);
		}
	}
}
