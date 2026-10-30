package cn.game.core.net.rpc;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.RunMode;
import cn.game.core.base.ServerContext;
import cn.game.core.exception.LogicException;
import cn.game.core.net.transport.Command;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.ExceptionHelper;
import cn.game.core.execute.TimingMetrics;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyFailure;

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
	default public Object invoke(CallType callType, Method method, Object[] args,
			String targetAddr, long objectId) {
		Command command = new Command(method.getDeclaringClass().getName(), method.getName(), method.getParameterTypes(), args, objectId);
		return send(callType, command, method.getReturnType(), targetAddr);

	}
	
	/** 
	 * 发送协议给远程服务器，需要有消息返回
	 * @param <T>
	 * @param addr 远程地址
	 * @param message 消息
	 * @return  
	 */
	public <T> Future<Message<T>> request(String addr, T message);

	/** 
	 * 发送协议给远程服务器，需要有消息返回
	 * @param <T>
	 * @param addr 远程地址
	 * @param message 消息
	 * @param options
	 * @return  
	 */
	public <T> Future<Message<T>> request(String addr, T message, DeliveryOptions options);

	/** 
	 * 给某地址广播消息
	 * @param <T>
	 * @param addr
	 * @param message
	 */
	public <T> void broadcast(String addr, T message);

	/** 
	 * 给某地址广播消息
	 * @param <T>
	 * @param addr
	 * @param message
	 */
	public <T> void broadcast(String addr, T message, DeliveryOptions options);

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
	
	
	 <T> void fireAndForget(String addr, T message, DeliveryOptions options);

	 
	 
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
	private Object send(CallType callType, Command command, Class<?> returnType, String targetAddr) {
		long startLong = DateUtil.currentTimeMillis();
		// 先判断异步方式
		// vertx的Future方式
		if (Future.class.isAssignableFrom(returnType)) {
			return handleVertxFuture(callType, command, targetAddr, startLong);
		}
		// JDK的CompletionStage方式
		if (CompletionStage.class.isAssignableFrom(returnType)) {
			return handleCompletionStage(callType, command, targetAddr, startLong);
		}
		// JDK的Future方式
		if (java.util.concurrent.Future.class.isAssignableFrom(returnType)) {
			return handleJdkFuture(callType, command, targetAddr, startLong);
		}

		// 其他情况同步调用处理
		return handleSyncCall(command, targetAddr);
	}

	/**
	 * 将响应体中的远端异常转为失败结果，传输失败保留原始原因。
	 * @param callType 请求或广播方式
	 * @param command 远端调用载荷
	 * @param targetAddr 目标服务地址
	 * @param startLong 请求开始的毫秒时间
	 * @return 请求结果；广播仅代表本地已发送
	 */
	private Future<?> handleVertxFuture(CallType callType, Command command, String targetAddr, long startLong) {
		if (callType == CallType.LoadBalancer || callType == CallType.PointToPoint) {
			TimingMetrics.Span timing = TimingMetrics.start(TimingMetrics.Stage.RPC_WAIT);
			boolean observed = false;
			try {
			Future<Message<Object>> request = request(targetAddr, command, VxHolder.universalOptions);
			Future<Object> result = request.map(r -> decodeResponse(r.body()))
					.onFailure(t -> logError(targetAddr, command, startLong, t));
			timing.observe(result, result);
			observed = true;
			return result;
			} finally { if (!observed) { timing.end(TimingMetrics.Outcome.FAILURE); } }
		} else if (callType == CallType.Broadcast) {
			broadcast(targetAddr, command, VxHolder.universalOptions);
			// 广播没有返回结果
			return Future.succeededFuture();
		}
		return Future.failedFuture(new IllegalArgumentException("Unsupported call type: " + callType));
	}

	/**
	 * 复用相同响应解码规则后适配 JDK 异步接口，不另行解释业务异常。
	 * @param callType 请求或广播方式
	 * @param command 远端调用载荷
	 * @param targetAddr 目标服务地址
	 * @param startLong 请求开始的毫秒时间
	 * @return 保留响应失败原因的 JDK Future
	 */
	private CompletableFuture<?> handleCompletionStage(CallType callType, Command command, String targetAddr,
			long startLong) {
		return handleVertxFuture(callType, command, targetAddr, startLong).toCompletionStage().toCompletableFuture();
	}

	private java.util.concurrent.Future<?> handleJdkFuture(CallType callType, Command command, String targetAddr,
			long startLong) {
		return handleCompletionStage(callType, command, targetAddr, startLong);
	}

	/**
	 * 在允许阻塞的调用线程等待远端结果，并沿用统一的响应异常语义。
	 * @param command 远端调用载荷
	 * @param targetAddr 目标服务地址
	 * @return 正常响应体，业务或传输失败时抛出异常
	 */
	private Object handleSyncCall(Command command, String targetAddr) {
		// 下面是同步方式调用，尽量少用
		// vertx中一般只允许在worker线程中调用
		checkAllowSync();
		long timeout = Config.remoteCallTimeOut;
		if (ServerContext.getInstance().getRunMode() == RunMode.TEST) {
			timeout = 600;
		}
		DeliveryOptions options = new DeliveryOptions().setSendTimeout(timeout * 1000).setCodecName(VxHolder.universalMessageCodec.name());
		TimingMetrics.Span timing = TimingMetrics.start(TimingMetrics.Stage.RPC_WAIT);
		boolean succeeded = false;
		TimingMetrics.Outcome failedOutcome = TimingMetrics.Outcome.FAILURE;
		try {
			Future<Message<Object>> request = request(targetAddr, command, options);
			Message<Object> message = request.toCompletionStage().toCompletableFuture().get(timeout, TimeUnit.SECONDS);
			Object decoded = decodeResponse(message.body());
			succeeded = true;
			return decoded;
		} catch (Exception e) {
		    failedOutcome = TimingMetrics.failureOutcome(e);
		    String scene = MessageFormat.format("远程调用异常: targetAddr[{0}] command[{1}] thread[{2}]",
		            targetAddr, command, Thread.currentThread().getName());
		    ExceptionHelper.rethrowWithPolicy(e, log, scene);
		    return null; // 不会到达

		} catch (Error err) {
		    // 本地真正的 Error：不要改变它的类型，避免影响框架的默认处理
		    log.error("远程调用过程中发生本地致命错误(Error): targetAddr[{}] command[{}] thread[{}]",
		            targetAddr, command, Thread.currentThread().getName(), err);
		    throw err;
		} finally { timing.end(succeeded ? TimingMetrics.Outcome.SUCCESS : failedOutcome); }
		// never reach
	}

	/**
	 * 解码既有 RPC 响应约定：Throwable 响应体表示远端失败，不能作为成功数据返回。
	 * @param result 传输成功后的响应体，可为空
	 * @return 普通响应体；业务异常保留错误码，远端 Error 包装为运行时异常
	 */
	private Object decodeResponse(Object result) {
		if (!(result instanceof Throwable failure)) { return result; }
		LogicException businessFailure = ExceptionHelper.findCause(failure, LogicException.class);
		if (businessFailure != null) { throw businessFailure; }
		if (failure instanceof RuntimeException runtimeFailure) { throw runtimeFailure; }
		if (failure instanceof Error) {
			throw new RuntimeException("remote error: " + failure.getClass().getName(), failure);
		}
		throw new RuntimeException(failure);
	}

	/**
	 * 记录异步调用失败及耗时，保留业务、传输和远端异常的原因链。
	 * @param targetAddr 目标服务地址
	 * @param command 调用载荷
	 * @param startLong 请求开始的毫秒时间
	 * @param t 失败原因
	 */
	private void logError(String targetAddr, Command command, long startLong, Throwable t) {
		log.error(MessageFormat.format("request message to targetAddr[{0}] failed ,command[{1}]usetime[{2}]", targetAddr, command,
				(DateUtil.currentTimeMillis() - startLong) / 1000), t);
	}

}
