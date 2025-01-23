package cn.game.core.net.rpc;

import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.transport.Command;
import cn.game.core.net.transport.Result;
import cn.game.util.Config;
import cn.game.util.KryoUtils;
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
			boolean sync, String targetAddr, long objectId) {
		Command command = new Command(methodName, clazz, args, objectId);
		return send(callType, command, requestCallback, returnType, sync, targetAddr);

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
	private Object sendOld(CallType callType, Command command, Consumer callBackTask, Class<?> returnType, boolean sync,
			String targetAddr) {

		byte[] datas = KryoUtils.serialize(command);
//		String targetAddr = serverType == null ? VxHolder.rpcServiceAddr(serverId) : VxHolder.rpcServiceAddr(serverType);
		// vertx的 Future方式异步
		if (Future.class.isAssignableFrom(returnType) && !sync) {
			long startLong = System.currentTimeMillis();
			if (callType == CallType.LoadBalancer) {
				Future<Message<byte[]>> request = request(targetAddr, datas);
				return request.map(r -> KryoUtils.deserialize(r.body(), Result.class).getResult()).onFailure(t -> {
					log.error(MessageFormat
							.format("request message to targetAddr[{0}] failed ,command[{1}]usetime[{2}]", targetAddr, command,
											(System.currentTimeMillis() - startLong) / 1000),t);
				});
			} else if (callType == CallType.Broadcast) {
				// 广播一般不需要返回值,默认成功
				broadcast(targetAddr, datas);
				return Future.succeededFuture();
			} else if (callType == CallType.PointToPoint) {
				Future<Message<byte[]>> request = request(targetAddr, datas);
				return request.map(r -> KryoUtils.deserialize(r.body(), Result.class).getResult()).onFailure(t -> {
					log.error(MessageFormat.format("request message to targetAddr[{0}] failed ,command[{1}]usetime[{2}]", targetAddr,
							command,
							(System.currentTimeMillis() - startLong) / 1000), t);
				});
			}
		}
		// callBack方式异步。
		if (callBackTask != null && !sync) {
			request(targetAddr, datas, r -> {
				if (r instanceof Throwable) {
					log.error("put message to targetAddr[{}] failed ,command[{}]exception[{}]", targetAddr, command, r);
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
			String errorMsg = MessageFormat.format("远程调用无返回结果: targetAddr[{0}] command[{1}] thread[{2}]", targetAddr,command, Thread.currentThread().getName());
			log.error(errorMsg,e);
			throw new RuntimeException(e);
		}
		Result deserialize = KryoUtils.deserialize(result, Result.class);
		Object result2 = deserialize.getResult();
		if (result2 instanceof Throwable) {
			String errorMsg = MessageFormat.format("远程调用无返回结果: targetAddr[{0}] command[{1}] thread[{2}]", targetAddr, command,
					Thread.currentThread().getName());
			log.error(errorMsg, (Throwable) result2);
			throw new RuntimeException((Throwable) result2);
		}
		return result2;

	}

	/** 
	 * 发送远程调用请求到远端
	 * @param command 具体要执行的方法和参数等
	 * @param callBackTask  数据返回后的回调任务
	 * @param returnType   方法返回值类型
	 * @param sync   是否同步调用
	 * @param serverId  远程的地址。 
	 * @return
	 */
	private Object send(CallType callType, Command command, Consumer callBackTask, Class<?> returnType, boolean sync, String targetAddr) {
		byte[] datas = KryoUtils.serialize(command);
		long startLong = System.currentTimeMillis();

		// 异步调用处理
		if (!sync) {
			// vertx的Future方式
			if (Future.class.isAssignableFrom(returnType)) {
				return handleVertxFuture(callType, command, datas, targetAddr, startLong);
			}
			// JDK的CompletionStage方式
			if (CompletionStage.class.isAssignableFrom(returnType)) {
				return handleCompletionStage(callType, command, datas, targetAddr, startLong);
			}
			// JDK的Future方式
			if (java.util.concurrent.Future.class.isAssignableFrom(returnType)) {
				return handleJdkFuture(callType, command, datas, targetAddr, startLong);
			}
			// callback方式异步
			if (callBackTask != null) {
				request(targetAddr, datas, r -> {
					if (r instanceof Throwable) {
						log.error("put message to targetAddr[{}] failed ,command[{}]exception[{}]", targetAddr, command, r);
					} else {
						callBackTask.accept(KryoUtils.deserialize(r.result().body(), Result.class).getResult());
					}
				});
				return null;
			}
		}

		// 同步调用处理
		return handleSyncCall(command, datas, targetAddr);
	}

	private Future<?> handleVertxFuture(CallType callType, Command command, byte[] datas, String targetAddr, long startLong) {
		if (callType == CallType.LoadBalancer || callType == CallType.PointToPoint) {
			Future<Message<byte[]>> request = request(targetAddr, datas);
			return request.map(r -> KryoUtils.deserialize(r.body(), Result.class).getResult())
					.onFailure(t -> logError(targetAddr, command, startLong, t));
		} else if (callType == CallType.Broadcast) {
			broadcast(targetAddr, datas);
			return Future.succeededFuture();
		}
		return Future.failedFuture(new IllegalArgumentException("Unsupported call type: " + callType));
	}

	private CompletableFuture<?> handleCompletionStage(CallType callType, Command command, byte[] datas, String targetAddr,
			long startLong) {
		if (callType == CallType.LoadBalancer || callType == CallType.PointToPoint) {
			Future<Message<byte[]>> request = request(targetAddr, datas);
			return request.map(r -> KryoUtils.deserialize(r.body(), Result.class).getResult())
					.onFailure(t -> logError(targetAddr, command, startLong, t))
					.toCompletionStage()
					.toCompletableFuture();
		} else if (callType == CallType.Broadcast) {
			broadcast(targetAddr, datas);
			return CompletableFuture.completedFuture(null);
		}
		return CompletableFuture.failedFuture(new IllegalArgumentException("Unsupported call type: " + callType));
	}

	private java.util.concurrent.Future<?> handleJdkFuture(CallType callType, Command command, byte[] datas, String targetAddr,
			long startLong) {
		return handleCompletionStage(callType, command, datas, targetAddr, startLong);
	}

	private Object handleSyncCall(Command command, byte[] datas, String targetAddr) {
		// 下面是同步方式调用，尽量少用
		// vertx中一般只允许在worker线程中调用
		checkAllowSync();

		Future<Message<byte[]>> request = request(targetAddr, datas);
		try {
			Message<byte[]> message = request.toCompletionStage().toCompletableFuture().get(Config.remoteCallTimeOut, TimeUnit.SECONDS);
			Result deserialize = KryoUtils.deserialize(message.body(), Result.class);
			Object result = deserialize.getResult();
			if (result instanceof Throwable) {
				String errorMsg = MessageFormat.format("远程调用异常: targetAddr[{0}] command[{1}] thread[{2}]", targetAddr, command,
						Thread.currentThread().getName());
				log.error(errorMsg, (Throwable) result);
				throw new RuntimeException((Throwable) result);
			}
			return result;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			String errorMsg = MessageFormat.format("远程调用获取结果异常: targetAddr[{0}] command[{1}] thread[{2}]", targetAddr, command,
					Thread.currentThread().getName());
			log.error(errorMsg, e);
			throw new RuntimeException(e);
		}
	}

	private void logError(String targetAddr, Command command, long startLong, Throwable t) {
		log.error(MessageFormat.format("request message to targetAddr[{0}] failed ,command[{1}]usetime[{2}]", targetAddr, command,
				(System.currentTimeMillis() - startLong) / 1000), t);
	}

}
