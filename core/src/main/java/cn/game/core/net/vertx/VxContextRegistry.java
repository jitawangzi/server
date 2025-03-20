package cn.game.core.net.vertx;

import java.util.concurrent.Callable;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.util.AsyncUtils;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * 对象ID → EventLoop Context 的映射管理器
 */
public class VxContextRegistry {

	private static final Logger log = LoggerFactory.getLogger(VxContextRegistry.class);

	private static final VxContextRegistry INSTANCE = new VxContextRegistry();

	public static VxContextRegistry getInstance() {
		return INSTANCE;
	}

	// 存放所有已分配的 event loop Context
	private volatile Context[] contexts;
	// eventLoop数量
	private volatile int contextCount;

	private VxContextRegistry() {
	}

	/**
	 * 初始化 registry，用于保存 contexts
	 * @param capacity 需要多少个context(与部署的Verticle数量一致)
	 */
	public synchronized void init(int capacity) {
		// 如果已经初始化，直接返回
		if (contexts != null && contexts.length == capacity) {
			return;
		}
		contexts = new Context[capacity];
		contextCount = capacity;
	}

	/**
	 * 注册某个 context 到指定 index 上
	 * @param index  对应的数组下标
	 * @param context 该Verticle的event loop context
	 */
	public void registerContext(int index, Context context) {
		contexts[index] = context;
		log.debug("Registered context for index={}, thread={}", index, Thread.currentThread().getName());
	}

	/**
	 * 根据对象id，获取它应该对应的 context
	 */
	public Context getContext(long objectId) {
		if (objectId == 0) {
			return null;
		}
		int index = (int) (objectId % contextCount);
		return contexts[index];
	}

	/**
	 * 将任务投递到 (objectId) 对应 context
	 */
	public void submitTask(long objectId, Runnable task) {
		// 当 objectId == 0 时，直接在“当前线程”执行。
		// 需要注意， 如果是服务器之间的消息通讯产生的任务，可能导致分配到一个context中。
		if (objectId == 0) {
			task.run();
			return;
		}
		// 否则，正常走 (id % n) → Context 逻辑
		Context ctx = getContext(objectId);
		if (ctx == null) {
			log.warn("No context found for objectId={}, skipping task", objectId);
			return;
		}
		// 异步投递到对应的 event loop 上
		ctx.runOnContext(v -> task.run());
	}

	/** 
	 * 将任务投递到 (objectId) 对应 context，执行supplier逻辑并返回结果
	 * @param <T>
	 * @param objectId
	 * @param supplier
	 * @return
	 */
	public <T> Future<T> submitTaskWithResult(long objectId, Callable<T> supplier) {
		return submitTaskWithResult(objectId, supplier, null);
	}

	/** 
	 * 将任务投递到 (objectId) 对应 context，执行supplier逻辑并返回结果
	 * @param <T>
	 * @param objectId
	 * @param supplier
	 * @return
	 */
	public <T, R> Future<T> submitTaskWithResult(long objectId, Callable<R> supplier, Function<R, Future<T>> mapper) {
		// 当 objectId == 0 时，直接在“当前线程”执行。
		Context ctx = null;
		if (objectId == 0) {
			ctx = VxHolder.vertx.getOrCreateContext();
		}
		ctx = getContext(objectId);
		if (ctx == null) {
			log.warn("No context found for objectId={}, skipping task", objectId);
			return Future.failedFuture("No context found for objectId=" + objectId);
		}
		return AsyncUtils.runOnContext(ctx, supplier, mapper);
	}

	/**
	 * 将任务投递到 (objectId) 对应 context
	 */
	public void submitTask(long objectId, Handler<Void> action) {
		submitTask(objectId, () -> action.handle(null));
	}

}

