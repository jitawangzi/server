package cn.game.core.net.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;

/**    
 * 主要使用来初始用来处理逻辑的Context/线程
 * 2025年1月23日 15:53:32
 * @author SYQ
 */
public class BusinessLogicVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(BusinessLogicVerticle.class);

	private final int index;

	public BusinessLogicVerticle(int index) {
		this.index = index;
	}

	@Override
	public void start(Promise<Void> startPromise) {
		// 获取 Vert.x 为此 Verticle 分配的 event loop Context
		ContextInternal contextInternal = (ContextInternal) vertx.getOrCreateContext();
		// 注册到全局 ContextRegistry
		VxContextRegistry.getInstance().registerContext(index, contextInternal);

		log.info("BusinessLogicVerticle #{} started on thread: {}", index, Thread.currentThread().getName());
		startPromise.complete();
	}
}