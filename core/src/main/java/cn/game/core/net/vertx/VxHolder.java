package cn.game.core.net.vertx;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;

import cn.game.core.async.BlockingCode;
import cn.game.core.base.ServerContext;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.core.net.vertx.codec.CustomMessageCodec;
import cn.game.core.net.vertx.codec.ProtobufMessageCodec;
import cn.game.core.net.vertx.codec.ProtobufProtocolCodec;
import cn.game.util.IpUtil;
import cn.game.util.LockUtil;
import cn.game.util.ServerType;
import cn.game.util.ZkHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

/**
 * 方便获取进程内vertx实例，一般一个进程一个vertx实例 跨进程的通讯，一般也封装在这里
 * 2021年4月12日 上午10:27:07
 * @author SYQ
 */
public class VxHolder {

	private static final Logger log = LoggerFactory.getLogger(VxHolder.class);

	public static Vertx vertx;
	private static final ProtobufMessageCodec protobufMessageCodec = new ProtobufMessageCodec();
	private static final CustomMessageCodec customMessageCodec = new CustomMessageCodec();
	/** 如果直接发protobuf类型的消息，需要指定这个 */
	public static final DeliveryOptions protobufOptions = new DeliveryOptions().setCodecName(protobufMessageCodec.name());
	public static final DeliveryOptions customOptions = new DeliveryOptions().setCodecName(customMessageCodec.name());
	public static final DeliveryOptions defaultOptions = new DeliveryOptions();
	private static List<Verticle> verticles;
	public static ZookeeperClusterManager zookeeperClusterManager;

	private static WebClient httpClient;
//	private static WebClientOptions webClientOption = new WebClientOptions().setSsl(true).setDefaultPort(443).setConnectTimeout(5000);
	private static WebClientOptions webClientOption = new WebClientOptions().setDefaultPort(80).setConnectTimeout(5000);

	private VxHolder() {
	};

	public static void init() throws Exception {
		String serverId = ServerContext.getInstance().getServerId();
		ServerType serverType = ServerContext.getInstance().getServerType();
		EventBusOptions eventBusOptions = new EventBusOptions().setHost(IpUtil.defaultAddress());
		Config zkConfig = ConfigService.getConfig("zookeeper");
		String content = zkConfig.getProperty("zk", null);
		JsonObject conf = new JsonObject(content);

		zookeeperClusterManager = new ZookeeperClusterManager(ZkHelper.curator);
		zookeeperClusterManager.setConfig(conf);

		eventBusOptions.setClusterNodeMetadata(
				new JsonObject().put("serverId", serverId).put("serverType", serverType.name()));
		VertxOptions options = new VertxOptions().setClusterManager(zookeeperClusterManager).setEventBusOptions(
				eventBusOptions);
		options.setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true).setJmxEnabled(true).setJmxDomain("vertx-metrics"));
		if (!ServerContext.getInstance().getRunMode().isProduction()) {
			options.setBlockedThreadCheckInterval(Integer.MAX_VALUE);
		}
		options.setInternalBlockingPoolSize(32);
		options.setWorkerPoolSize(options.getEventLoopPoolSize() * 2);

		Future<Vertx> clusteredVertxFuture = Vertx.clusteredVertx(options);
		vertx = clusteredVertxFuture.toCompletionStage().toCompletableFuture().get(3000, TimeUnit.SECONDS);
		vertx.exceptionHandler(e -> {
			log.error("vertx uncaptured exception： ", e);
		});
		httpClient = WebClient.create(vertx, webClientOption);

		vertx.eventBus().registerDefaultCodec(ServiceException.class, new ServiceExceptionMessageCodec());
		vertx.eventBus().registerDefaultCodec(ProtobufProtocol.class, new ProtobufProtocolCodec());
		vertx.eventBus().registerCodec(protobufMessageCodec);
		vertx.eventBus().registerCodec(customMessageCodec);

		deployVerticles();
	}

	private static void deployVerticles() throws Exception {

		if (verticles != null) {

			for (Verticle verticle : verticles) {
				deployVerticleSync(verticle);
			}
		}
	}
	/**
	 * 同步部署Verticle，只在服务器启动时使用
	 * @param verticle
	 * @return
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public static String deployVerticleSync(Verticle verticle) throws Exception {
		String string = vertx.deployVerticle(verticle).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
		log.info("部署Verticle[{}]成功： ", verticle);
		return string;
	}
	public static String deployVerticleSync(Class<? extends Verticle> verticleClass, DeploymentOptions options)
			throws InterruptedException, ExecutionException, TimeoutException {
		String string = vertx.deployVerticle(verticleClass, options).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
		log.info("部署Verticle[{}]成功： ", verticleClass);
		return string;
	}
	public static void deployVerticle(Verticle verticle) {
		Future<String> deployVerticle = vertx.deployVerticle(verticle);
		deployVerticle.onSuccess(r -> {
			log.info("部署Verticle[{}]成功： ", verticle);
		});
	}
	public static Future<String> deployVerticleFuture(Verticle verticle) {
		Future<String> deployVerticle = vertx.deployVerticle(verticle);
		return deployVerticle;
	}


	public static void setVerticles(List<Verticle> verticles) {
		VxHolder.verticles = verticles;
	}

	/**
	 * 给指定服务器发送消息，不需要返回消息
	 * 
	 * @param serverId
	 *            服务器id
	 * @param msgId
	 *            消息id
	 * @param byteArray
	 *            消息序列化后的数据
	 */
	@Deprecated
	public static void sendToRemoteServer(String serverId, int msgId, byte[] byteArray) {
		vertx.eventBus().send(serverId, toBuffer(msgId, byteArray), defaultOptions);
	}
	/**
	 * 给指定服务器发送消息，不需要返回消息
	 * 
	 * @param serverId
	 *            服务器id
	 * @param message
	 */
	public static void sendToRemoteServer(String serverId, com.google.protobuf.Message message) {
		vertx.eventBus().send(serverId, message, protobufOptions);
	}
	/**
	 * 给指定服务器发送消息,需要有返回消息
	 * 
	 * @param <T>
	 * @param serverId
	 * @param message
	 * @return
	 */
	public static <T> Future<Message<T>> requestRemoteServer(String serverId, com.google.protobuf.Message message) {
		return vertx.eventBus().request(serverId, message, protobufOptions);
	}

	/** 
	 * 给某类服务器发送消息，需要有返回，消息会负载到某个节点中。 
	 * @param <T>
	 * @param serverType
	 * @param message
	 * @return
	 */
	public static <T> Future<Message<T>> requestRemoteServer(ServerType serverType,
			com.google.protobuf.Message message) {
		return vertx.eventBus().request(serverType.name(), message, protobufOptions);
	}
	/**
	 * 给指定服务器发送消息,消息返回触发回调
	 * 
	 * @param serverId
	 * @param message
	 * @param replyHandler
	 *            回调消息
	 */
	public static void requestRemoteServer(String serverId, com.google.protobuf.Message message,
			Handler<AsyncResult<Message<com.google.protobuf.Message>>> replyHandler) {
		vertx.eventBus().request(serverId, message, protobufOptions, replyHandler);
	}
	/**
	 * 给某类服务器广播消息
	 * @param serverId
	 * @param msgId
	 * @param byteArray
	 */
	@Deprecated
	public static void broadcastRemoteServer(ServerType serverType, int msgId, byte[] byteArray) {

		vertx.eventBus().publish(serverType.name(), toBuffer(msgId, byteArray));
	}
	/**
	 * 给某类服务器广播消息
	 * 
	 * @param serverType
	 * @param message
	 */
	public static void broadcastRemoteServer(ServerType serverType, com.google.protobuf.Message message) {
		vertx.eventBus().publish(serverType.name(), message, protobufOptions);
	}

	public static Buffer toBuffer(int msgId, byte[] byteArray) {
		CompositeByteBuf compositeBuffer = Unpooled.compositeBuffer(2);
		ByteBuf headerBuf = Unpooled.buffer(4);
		headerBuf.writeInt(msgId);
		ByteBuf bodyBuf = Unpooled.wrappedBuffer(byteArray);
		compositeBuffer.addComponents(headerBuf, bodyBuf);
		compositeBuffer.writerIndex(headerBuf.readableBytes() + bodyBuf.readableBytes());

		return Buffer.buffer(compositeBuffer);
	}

	public static String rpcServiceAddr(String serverId) {
		return serverId + ".rpc.service";
	}
//
//	public static String reqServerAddr(String serverId) {
//		return serverId + ".req";
//	}
//	public static String subServerAddr(String serverType) {
//		return serverType + ".sub";
//	}

	/** 
	 * http 请求
	 * @param method
	 * @param requestURI
	 * @param body
	 * @param successHandler
	 * @param failedHandler
	 */
	public static void request(HttpMethod method, String requestURI, JsonObject param, Handler<JsonObject> successHandler,
			Handler<Throwable> failedHandler) {
		HttpRequest<Buffer> request = httpClient.requestAbs(method, requestURI)
//				.expect(ResponsePredicate.JSON),
				.expect(ResponsePredicate.SC_SUCCESS);

		Future<HttpResponse<Buffer>> responseFutrue = null;
		if (method == HttpMethod.GET) {
			if (param != null && !param.isEmpty()) {
				param.stream().forEach(entry -> request.addQueryParam(entry.getKey(), entry.getValue().toString()));
			}
			responseFutrue = request.send();
		}else if (method == HttpMethod.POST) {
			responseFutrue = request.sendJsonObject(param);
		} 
		else {
			throw new IllegalArgumentException("没有实现的http方法:  "+ method);
		}
		responseFutrue.onSuccess(response -> successHandler.handle(response.bodyAsJsonObject()))
				.onFailure(err -> failedHandler.handle(err));
	}

	/** 
	 * 发送http get 请求
	 * @param requestURI     请求地址
	 * @param successHandler 成功后的处理器
	 * @param failedHandler  失败的处理器
	 */
	public static void get(String requestURI, Handler<JsonObject> successHandler, Handler<Throwable> failedHandler) {

		httpClient.getAbs(requestURI)
				.expect(ResponsePredicate.SC_SUCCESS)
//				.expect(ResponsePredicate.JSON)
				.send()
				.onSuccess(response -> successHandler.handle(response.bodyAsJsonObject()))
				.onFailure(err -> failedHandler.handle(err));
	}

	public static Future<HttpResponse<Buffer>> get(String requestURI) {
		return httpClient.getAbs(requestURI).expect(ResponsePredicate.SC_SUCCESS).expect(ResponsePredicate.JSON).send();
	}
	/** 
	 * 发送http post 请求
	 * @param requestURI     请求地址
	 * @param successHandler 成功后的处理器
	 * @param failedHandler  失败的处理器
	 */
	public static void post(String requestURI, Handler<JsonObject> successHandler, Handler<Throwable> failedHandler, Object body) {

		httpClient.postAbs(requestURI)
				.expect(ResponsePredicate.SC_SUCCESS)
//				.expect(ResponsePredicate.JSON)
				.sendJson(body)
				.onSuccess(response -> successHandler.handle(response.bodyAsJsonObject()))
				.onFailure(err -> failedHandler.handle(err));
	}


	public static ZookeeperClusterManager getZookeeperClusterManager() {
		return zookeeperClusterManager;
	}

	/** 
	 * 获取Redisson分布式锁，进行后续的逻辑，可以用compose方法组合多个Future
	 * 如果是在eventloop线程调用方法，则逻辑运行在当前eventloop线程。 
	 * 如果是在其他线程调用方法，则逻辑运行在某个eventloop线程。
	 * @param <T>
	 * @param waitTime 获取锁的等待时间
	 * @param leaseTime	锁最大持有时间
	 * @param unit
	 * @param operations  获取锁后的一些操作
	 * @param lockKeys 锁的key，支持多个key。
	 * @return
	 */
	public static <T> Future<T> runWithLock(long waitTime, long leaseTime, TimeUnit unit, Supplier<Future<T>> operations, String... lockKeys) {
		Promise<T> promise = Promise.promise();
		RLock lock = LockUtil.initLock(lockKeys);
		Context context = VxHolder.vertx.getOrCreateContext();
		long threadId = Thread.currentThread().getId();

		lock.tryLockAsync(waitTime, leaseTime, unit).whenComplete((locked, throwable) -> {
			if (throwable != null) {
				log.error("Error acquiring lock for keys: " + Arrays.toString(lockKeys), throwable);
				promise.fail(throwable);
				return;
			}
			if (!locked) {
	            log.warn("Failed to acquire lock for keys: " + Arrays.toString(lockKeys));
	            promise.fail("Failed to acquire lock");
				return;
			}
			log.debug("Lock acquired for keys: {}", Arrays.toString(lockKeys));
			context.runOnContext(v -> {
				Future<T> operationFuture;
				try {
					operationFuture = operations.get();
				} catch (Exception e) {
					log.error("Error getting operation future for keys: " + Arrays.toString(lockKeys), e);
					promise.fail(e);
					releaseLock(lock, threadId, lockKeys);
					return;
				}
				operationFuture.onComplete(result -> {
					if (result.succeeded()) {
						promise.complete(result.result());
					} else {
						promise.fail(result.cause());
					}
					releaseLock(lock, threadId, lockKeys);
				});
				// 使用 leaseTime 作为业务逻辑的最大执行时间
				VxHolder.vertx.setTimer(unit.toMillis(leaseTime), id -> {
					if (!promise.future().isComplete()) {
						log.warn("Operation timed out for keys: " + Arrays.toString(lockKeys));
						promise.fail("Operation timed out");
						// 注意：这里不需要手动释放锁，因为 leaseTime 到期后锁会自动释放
					}
				});
			});
		});
		return promise.future();
	}

	/** 
	 * 重载方法，使用默认的等待时间和租约时间
	 * @param <T>
	 * @param operations
	 * @param lockKey
	 * @return
	 */
	public static <T> Future<T> runWithLock(Supplier<Future<T>> operations, String... lockKey) {
		return runWithLock(5, 30, TimeUnit.SECONDS, operations, lockKey);
	}

	private static void releaseLock(RLock lock, long threadId, String... lockKeys) {
		lock.unlockAsync(threadId).whenComplete((unlocked, unlockThrowable) -> {
			if (unlockThrowable != null) {
				log.error("Error unlocking for keys: " + Arrays.toString(lockKeys), unlockThrowable);
			}
		});
	}

	public static <T> Future<T> toVertxFuture(CompletionStage<T> future) {
		return Future.fromCompletionStage(future);
	}

	/** 
	 * worker线程池中执行阻塞代码，带超时
	 * @param <T>
	 * @param blockingCode 阻塞逻辑
	 * @param timeoutMs 超时时间（ms）
	 * @param ordered 是否按顺序执行，一般为false
	 * @return 异步执行结果
	 */
	public static <T> Future<T> executeBlockingWithTimeout(BlockingCode<T> blockingCode, long timeoutMs, boolean ordered) {

		return executeBlockingWithTimeoutInternal(promise -> {
			try {
				T result = blockingCode.execute();
				promise.complete(result);
			} catch (Throwable e) {
				promise.fail(e);
			}
		}, timeoutMs, ordered);
	}

	/** 
	 * 默认30秒超时，执行阻塞逻辑
	 * @param <T>
	 * @param blockingCode
	 * @param ordered
	 * @return
	 */
	public static <T> Future<T> executeBlockingWithTimeout(BlockingCode<T> blockingCode) {
		return executeBlockingWithTimeout(blockingCode, 30000, false);
	}

	/** 
	 * 对executeBlocking的超时封装
	 * @param <T>
	 * @param blockingHandler
	 * @param timeoutMs
	 * @param ordered
	 * @return
	 */
	private static <T> Future<T> executeBlockingWithTimeoutInternal(Handler<Promise<T>> blockingHandler, long timeoutMs,
			boolean ordered) {

		Promise<T> promise = Promise.promise();

		// 执行阻塞操作
		Future<T> executionFuture = vertx.executeBlocking(blockingHandler, ordered);
		
		// 设置超时定时器
		long timerId = vertx.setTimer(timeoutMs, id -> {
			if (!promise.future().isComplete()) {
				promise.fail(new TimeoutException("Operation timed out after " + timeoutMs + " ms"));
			}
		});
		
		// 处理执行结果
		executionFuture.onComplete(ar -> {
			vertx.cancelTimer(timerId);
			if (ar.succeeded()) {
				promise.complete(ar.result());
			} else {
				promise.fail(ar.cause());
			}
		});

		return promise.future();
	}
}
