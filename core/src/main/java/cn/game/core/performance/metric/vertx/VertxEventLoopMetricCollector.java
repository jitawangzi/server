package cn.game.core.performance.metric.vertx;

import cn.game.core.performance.metric.AbstractMetricCollector;
import cn.game.core.performance.metric.MetricType;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;

/**
 * Vertx事件循环指标收集器
 * 监控Vertx事件循环队列中待处理任务数量
 */
public class VertxEventLoopMetricCollector extends AbstractMetricCollector {

	private final Vertx vertx;

	// 正常情况下，事件循环队列的预期最大值
	private static final int MAX_EXPECTED_PENDING_TASKS = 1000;

	public VertxEventLoopMetricCollector(Vertx vertx) {
		// 事件循环队列临界阈值较高，因为事件循环处理速度非常快
		super("vertx.eventloop", MetricType.VERTX, 3, 0.6, 0.9);
		this.vertx = vertx;
	}

	@Override
	protected double doCollect() {
		int totalPendingTasks = 0;

		// 获取Vertx内部事件循环组
		EventLoopGroup eventLoopGroup = ((VertxInternal) vertx).getEventLoopGroup();

		// 遍历所有事件循环线程，累计待处理任务数
		for (EventExecutor eventExecutor : eventLoopGroup) {
			if (eventExecutor instanceof SingleThreadEventExecutor) {
				int pendingTasks = ((SingleThreadEventExecutor) eventExecutor).pendingTasks();
				totalPendingTasks += pendingTasks;
			}
		}

		// 标准化到0-1范围
		// 如果任务数超过预期最大值，会达到1.0，但100个任务通常只有0.1
		return normalize(totalPendingTasks, 0, MAX_EXPECTED_PENDING_TASKS);
	}

	/**
	 * 获取事件循环线程数量
	 * @return 事件循环线程数量
	 */
	/*	public int getEventLoopThreadCount() {
			if (vertx instanceof VertxInternal) {
				return ((VertxInternal) vertx).getEventLoopzGroup().executorCount();
			}
			return 0;
		}*/
}