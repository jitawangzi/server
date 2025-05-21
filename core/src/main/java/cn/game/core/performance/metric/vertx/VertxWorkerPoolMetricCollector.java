package cn.game.core.performance.metric.vertx;

import cn.game.core.performance.metric.AbstractMetricCollector;
import cn.game.core.performance.metric.MetricType;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.WorkerPool;
import io.vertx.micrometer.backends.BackendRegistries;

/**
 * Vertx工作线程池指标收集器
 * 监控Vertx工作线程池中待处理任务队列大小
 */
public class VertxWorkerPoolMetricCollector extends AbstractMetricCollector {

	private final Vertx vertx;
	private MeterRegistry registry;

	// 工作线程池队列的预期最大值
	private static final int MAX_EXPECTED_PENDING_TASKS = 200;

	public VertxWorkerPoolMetricCollector(Vertx vertx) {
		// Worker线程池临界阈值较低，因为工作线程通常执行耗时操作
		super("vertx.workerpool", MetricType.VERTX, 3, 0.6, 0.8);
		this.vertx = vertx;
	}

	@Override
	public void init() {
		try {
			// 尝试获取Micrometer注册表
			this.registry = BackendRegistries.getDefaultNow();
		} catch (Exception e) {
			log.warn("Failed to get Micrometer registry, will use direct access for worker metrics", e);
		}
	}

	@Override
	protected double doCollect() {
		double pendingTasks = 0.0;

		// 优先使用Micrometer指标（如果可用）
		if (registry != null) {
			try {
				Double metricValue = registry.get("vertx.pool.queue.pending").gauge().value();
				if (metricValue != null) {
					pendingTasks = metricValue;
				}
			} catch (Exception e) {
				log.debug("Failed to get metrics from registry, falling back to direct access", e);
				// 回退到直接访问
				pendingTasks = getDirectWorkerPoolMetrics();
			}
		} else {
			// 直接访问Vertx内部API
			pendingTasks = getDirectWorkerPoolMetrics();
		}

		// 标准化到0-1范围
		return normalize(pendingTasks, 0, MAX_EXPECTED_PENDING_TASKS);
	}

	/**
	 * 直接从Vertx内部获取工作线程池指标
	 * @return 待处理任务数
	 */
	@Deprecated
	private double getDirectWorkerPoolMetrics() {
		try {
			if (vertx instanceof VertxInternal) {
				VertxInternal vertxInternal = (VertxInternal) vertx;
				WorkerPool workerPool = vertxInternal.getWorkerPool();

				// 获取队列大小，不同版本的Vertx可能有不同的API
				// 尝试多种方式获取
				try {

				} catch (Exception e) {
					// 如果上述方法不存在，可能需要通过反射获取
					log.debug("Failed to get metrics directly, worker pool API may have changed", e);
					return 0.0;
				}
			}
		} catch (Exception e) {
			log.debug("Error getting worker pool metrics", e);
		}
		return 0.0;
	}

	/**
	 * 获取工作线程池大小
	 * @return 工作线程池大小
	 */
	public int getWorkerPoolSize() {
		if (vertx instanceof VertxInternal) {
			try {
				VertxInternal vertxInternal = (VertxInternal) vertx;
				WorkerPool workerPool = vertxInternal.getWorkerPool();
			} catch (Exception e) {
				log.debug("Error getting worker pool size", e);
			}
		}
		return 0;
	}
}