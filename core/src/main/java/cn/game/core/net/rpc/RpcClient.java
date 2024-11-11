package cn.game.core.net.rpc;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.transport.Command;
import cn.game.core.net.transport.Result;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.task.TaskManager;
import cn.game.util.Config;
import cn.game.util.KryoUtils;
import cn.game.util.MailUtil;
import cn.game.util.ServerType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

public interface RpcClient {

	static final Logger log = LoggerFactory.getLogger(RpcClient.class);

	/**
	 * @Description
	 * @param methodName方法名
	 * @param clazz方法参数类型
	 * @param returnType
	 *            方法返回类型
	 * @param arg方法参数
	 * @param requestCallback 回调任务
	 * @param callbackMothed回掉方法
	 * @param sync,是否同步
	 * @return 同步情况下返回远程结果，异步情况下，requestCallback 不为null时，返回null，否则返回Future对象
	 */
	default public Object invoke(CallType callType, String methodName, Class<?>[] clazz, Class<?> returnType, Object[] args,
			Consumer<?> requestCallback,
			boolean sync, String serverId, ServerType serverType) {
		Command command = new Command(methodName, clazz, args);
		return send(callType, command, requestCallback, returnType, sync, serverId, serverType);

	}

	/** 
	 * 发送协议给远程服务器，需要有消息返回
	 * @param <T>
	 * @param addr 远程地址
	 * @param message 消息
	 * @param replyHandler 消息返回时的回调处理器
	 */
	public <T> void request(String addr, T message, Handler<AsyncResult<Message<T>>> replyHandler);
	
	/** 
	 * 发送协议给远程服务器，需要有消息返回
	 * @param <T>
	 * @param addr 远程地址
	 * @param message 消息
	 * @return  
	 */
	public <T> Future<Message<T>> request(String addr, T message);

	/** 
	 * 给某地址广播消息
	 * @param <T>
	 * @param addr
	 * @param message
	 */
	public <T> void broadcast(String addr, T message);

	/** 
	 * 给指定地址发送消息，不用返回值。
	 * @param <T>
	 * @param addr
	 * @param message
	 */
	public <T> void send(String addr, T message);

	/** 
	 * 发送消息，不用返回值。  某些类型的消息里包含消息的目的地址
	 * @param <T>
	 * @param message
	 */
	public <T> void send(T message);
	/** 
	 * 是否允许同步请求
	 * @return
	 */
	public boolean checkAllowSync();

	/** 
	 * 发送远程调用请求到远端
	 * @param command 具体要执行的方法和参数等
	 * @param callBackTask  数据返回后的回调任务
	 * @param returnType   方法返回值类型
	 * @param sync   是否同步调用
	 * @param serverId  远程的地址。 
	 * @return
	 */
	private Object send(CallType callType, Command command, Consumer callBackTask, Class<?> returnType, boolean sync, String serverId,
			ServerType serverType) {

		byte[] datas = KryoUtils.serialize(command);
		String targetAddr = serverType == null ? VxHolder.rpcServiceAddr(serverId) : VxHolder.rpcServiceAddr(serverType);
		// vertx的 Future方式异步
		if (Future.class.isAssignableFrom(returnType)) {
			long startLong = System.currentTimeMillis();
			if (callType == CallType.LoadBalancer) {
				if (serverType == null) {
					throw new IllegalArgumentException("serverType can not be null when callType is LoadBalancer");
				}
				Future<Message<byte[]>> request = request(targetAddr, datas);
				return request.map(r -> KryoUtils.deserialize(r.body(), Result.class).getResult()).onFailure(t -> {
					log.error(MessageFormat
									.format("request message to serverType[{0}] failed ,command[{1}]usetime[{2}]", serverType, command,
											(System.currentTimeMillis() - startLong) / 1000),t);
				});
			} else if (callType == CallType.Broadcast) {
				if (serverType == null) {
					throw new IllegalArgumentException("serverType can not be null when callType is Broadcast");
				}
				// 广播一般不需要返回值,默认成功
				broadcast(targetAddr, datas);
				return Future.succeededFuture();
			} else if (callType == CallType.PointToPoint) {
				if (serverId == null) {
					throw new IllegalArgumentException("serverId can not be null when callType is PointToPoint");
				}
				Future<Message<byte[]>> request = request(targetAddr, datas);
				return request.map(r -> KryoUtils.deserialize(r.body(), Result.class).getResult()).onFailure(t -> {
					log.error(MessageFormat.format("request message to serverId[{0}] failed ,command[{1}]usetime[{2}]", serverId, command,
							(System.currentTimeMillis() - startLong) / 1000), t);
				});
			}
		}
		// callBack方式异步。
		if (callBackTask != null || !sync) {
			request(targetAddr, datas, r -> {
				if (r instanceof Throwable) {
					log.error("put message to serverId[{}] failed ,command[{}]exception[{}]", serverId, command, r);
				} else {
					if (callBackTask != null) {
						callBackTask.accept(KryoUtils.deserialize(r.result().body(), Result.class).getResult());
					}
				}
			});
			return null;
		}
		// 下面是同步方式调用，尽量少用
		// vertx中一般只允许在worker线程中调用
		checkAllowSync();

		Future<Message<byte[]>> request = request(targetAddr, datas);
		byte[] result = null;
		try {
			// 默认等待5秒
			Message<byte[]> message = request.toCompletionStage().toCompletableFuture().get(Config.remoteCallTimeOut, TimeUnit.SECONDS);
			result = message.body();
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("sync request message to serverId[{}] failed ,command[{}]exception[{}]", serverId, command, e);
		}
		if (result == null) {
			String errorMsg = MessageFormat.format("远程调用无返回结果：  command[{0}] toServerId[{1}] thread[{2}]", command, serverId, Thread.currentThread().getName());
			log.error(errorMsg);
			TaskManager.getInstance().addBlockTask(() -> {
				try {
					MailUtil.reportException("远程调用无返回结果", errorMsg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			return null;

		}
		Result deserialize = KryoUtils.deserialize(result, Result.class);
		Object result2 = deserialize.getResult();
		if (result2 instanceof Throwable) {
			log.error(" request message to serverId[{}] result failed ,command[{}]exception[{}]", serverId, command, result2);
			throw new RuntimeException((Throwable) result2);
		}
		return result2;

	}

}
