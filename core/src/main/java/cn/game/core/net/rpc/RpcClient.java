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
import cn.game.core.task.TaskManager;
import cn.game.util.Config;
import cn.game.util.KryoUtils;
import cn.game.util.MailUtil;
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
	default public Object invoke(String methodName, Class<?>[] clazz, Class<?> returnType, Object[] args, Consumer<?> requestCallback,
			boolean sync, String serverId) {
		Command command = new Command(methodName, clazz, args);
		return send(command, requestCallback, returnType, sync, serverId);

	}

	/** 
	 * 发送协议给远程服务器，需要有消息返回
	 * @param <T>
	 * @param message 消息
	 * @param serverId 远程服务器id
	 * @param replyHandler 消息返回时的回调处理器
	 */
	public <T> void request(T message, String serverId, Handler<AsyncResult<Message<T>>> replyHandler);
	/** 
	 * 发送协议给远程服务器，需要有消息返回
	 * @param <T>
	 * @param message 消息
	 * @param serverId 远程服务器id
	 * @return  
	 */
	public <T> Future<Message<T>> request(T message, String serverId);
	/** 
	 * 给指定服务器发送消息
	 * @param <T>
	 * @param message
	 * @param serverId
	 */
	public <T> void send(T message, String serverId);

	/** 
	 * 发送消息，某些类型的消息里包含消息的目的地址
	 * @param <T>
	 * @param message
	 */
	public <T> void send(T message);
	/** 
	 * 是否允许同步请求
	 * @return
	 */
	public boolean checkAllowSync();

	private Object send(Command command, Consumer callBackTask, Class<?> returnType, boolean sync, String serverId) {

		byte[] datas = KryoUtils.serialize(command);
		// Future方式异步
		if (Future.class.isAssignableFrom(returnType)) {
			long startLong = System.currentTimeMillis();
			Future<Message<byte[]>> request = request(datas, serverId);
			return request.map(r -> KryoUtils.deserialize(r.body(), Result.class).getResult()).onFailure(t -> {
				log.error(MessageFormat.format("request message to serverId[{0}] failed ,command[{1}]usetime[{2}]", serverId, command,
						(System.currentTimeMillis() - startLong) / 1000), t);
			});
		}
		// callBack方式异步。
		if (callBackTask != null || !sync) {
			request(datas, serverId, r -> {
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
		// 同步方式
		checkAllowSync();

		Future<Message<byte[]>> request = request(datas, serverId);
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
