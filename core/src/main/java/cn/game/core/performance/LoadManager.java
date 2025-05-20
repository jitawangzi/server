package cn.game.core.performance;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.core.Vertx;
import io.vertx.micrometer.backends.BackendRegistries;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HWDiskStore;

public class LoadManager {
	// 配置参数
	private static final int SAMPLE_INTERVAL = 5_000;
	private static final double[] WEIGHTS = { 0.3, 0.2, 0.2, 0.2, 0.1 };

	// 阈值配置
	private static final double WARNING_THRESHOLD = 0.6;
	private static final double CRITICAL_THRESHOLD = 0.8;

	private static final double QUEUE_CRITICAL = 0.9;
	private static final double WORKER_CRITICAL = 0.8;
	private static final double CPU_CRITICAL = 0.9;
	private static final double MEMORY_CRITICAL = 0.8;
	private static final double DISK_CRITICAL = 0.8;

	// 单例实例
	private static final LoadManager INSTANCE = new LoadManager();
	private final StampedLock lock = new StampedLock();
	private final AtomicReference<LoadState> currentState = new AtomicReference<>(LoadState.NORMAL);
	private final AtomicInteger currentScore = new AtomicInteger(0);

	// 性能指标缓存
	private volatile double lastCpuLoad = 0.0;
	private volatile double lastHeapUsage = 0.0;
	private volatile double lastDiskIO = 0.0;
	private volatile double lastQueueSize = 0.0;
	private volatile double lastWorkerUsage = 0.0;

	// 监控组件
	private final SystemInfo systemInfo = new SystemInfo();
	private final CentralProcessor processor = systemInfo.getHardware().getProcessor();
	private MeterRegistry registry;
	private long[] prevCpuTicks;
	private final MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();

	// 滑动平均窗口
	// vertx eventloop队列
	private final MovingAverage queueAvg = new MovingAverage(3);
	// vertx worker池队列
	private final MovingAverage workerAvg = new MovingAverage(3);
	private final MovingAverage cpuAvg = new MovingAverage(3);
	private final MovingAverage heapMemAvg = new MovingAverage(3);
	private final MovingAverage diskAvg = new MovingAverage(3);

	public enum LoadState {
		NORMAL, WARNING, CRITICAL
	}

	private LoadManager() {
	}

	public static LoadManager getInstance() {
		return INSTANCE;
	}

	public void init(Vertx vertx) {
		this.registry = BackendRegistries.getDefaultNow();
		this.prevCpuTicks = processor.getSystemCpuLoadTicks();
		registerMetrics();
		startSampling(vertx);
	}

	private void registerMetrics() {
		Gauge.builder("load.score", currentScore::get).register(registry);
		Gauge.builder("load.state", () -> currentState.get().ordinal()).register(registry);

		// 原始指标缓存监控
		Gauge.builder("cache.cpu", () -> lastCpuLoad).register(registry);
		Gauge.builder("cache.heap", () -> lastHeapUsage).register(registry);
		Gauge.builder("cache.disk", () -> lastDiskIO).register(registry);
		Gauge.builder("cache.queue", () -> lastQueueSize).register(registry);
		Gauge.builder("cache.worker", () -> lastWorkerUsage).register(registry);
	}

	private void startSampling(Vertx vertx) {
		vertx.setPeriodic(SAMPLE_INTERVAL, id -> vertx.executeBlocking(promise -> {
			try {
				updateLoadState();
				promise.complete();
			} catch (Exception e) {
				promise.fail(e);
			}
		}, false));
	}

	private synchronized void updateLoadState() {
		long stamp = lock.writeLock();
		try {
			// 1. 收集原始指标
			double[] rawMetrics = collectRawMetrics();

			// 2. 更新缓存
			updateCache(rawMetrics);

			// 3. 计算滑动平均
			double[] avgMetrics = calculateAverageMetrics();

			// 4. 计算综合评分
			double score = calculateScore(avgMetrics);

			// 5. 评估状态（综合评分 + 独立指标）
			LoadState newState = evaluateCompositeState(score, rawMetrics);

			// 6. 更新状态
			currentScore.set((int) (score * 100));
			LoadState oldState = currentState.getAndSet(newState);

			// 7. 执行降级策略
			if (oldState != newState) {
				executeDegrade(newState, rawMetrics);
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	private double[] collectRawMetrics() {
		return new double[] { getEventLoopQueueMetric(), getWorkerPoolMetric(), getCpuUsage(), getHeapMemoryUsage(),
				getRawDiskUsage() };
	}

	private void updateCache(double[] metrics) {
		lastQueueSize = metrics[0];
		lastWorkerUsage = metrics[1];
		lastCpuLoad = metrics[2];
		lastHeapUsage = metrics[3];
		lastDiskIO = metrics[4];
	}

	private double[] calculateAverageMetrics() {
		return new double[] { queueAvg.next(lastQueueSize), workerAvg.next(lastWorkerUsage), cpuAvg.next(lastCpuLoad),
				heapMemAvg.next(lastHeapUsage), diskAvg.next(lastDiskIO) };
	}

	private double calculateScore(double[] metrics) {
		double score = 0;
		for (int i = 0; i < metrics.length; i++) {
			score += metrics[i] * WEIGHTS[i];
		}
		return Math.min(score, 1.0);
	}

	private LoadState evaluateCompositeState(double score, double[] rawMetrics) {
		// 独立指标检查优先
		if (rawMetrics[0] >= QUEUE_CRITICAL)
			return LoadState.CRITICAL;
		if (rawMetrics[1] >= WORKER_CRITICAL)
			return LoadState.CRITICAL;
		if (rawMetrics[2] >= CPU_CRITICAL)
			return LoadState.CRITICAL;
		if (rawMetrics[3] >= MEMORY_CRITICAL)
			return LoadState.CRITICAL;
		if (rawMetrics[4] >= DISK_CRITICAL)
			return LoadState.CRITICAL;

		// 综合评分检查
		if (score >= CRITICAL_THRESHOLD)
			return LoadState.CRITICAL;
		if (score >= WARNING_THRESHOLD)
			return LoadState.WARNING;
		return LoadState.NORMAL;
	}

	private void executeDegrade(LoadState state, double[] rawMetrics) {
		DegradeStrategy.recoverAll();

		// 综合状态限制
		switch (state) {

		case CRITICAL:
			DegradeStrategy.limit(LoadLimitTypeEnum.Login);
			DegradeStrategy.limit(LoadLimitTypeEnum.GlobalChat);
			break;
		case WARNING:
			DegradeStrategy.limit(LoadLimitTypeEnum.GlobalChat);
			break;
		default:
			break;
		}

		// 独立指标专项限制
		if (rawMetrics[0] >= QUEUE_CRITICAL) {
			DegradeStrategy.limit(LoadLimitTypeEnum.NoBlockingOperation);
		}
		if (rawMetrics[1] >= WORKER_CRITICAL) {
			DegradeStrategy.limit(LoadLimitTypeEnum.BlockingOperation);
		}
		if (rawMetrics[2] >= CPU_CRITICAL) {
		}
	}

	/** 
	 * Vert.x eventloop核心指标采集
	 * @return
	 */
	private double getEventLoopQueueMetric() {
		Double value = registry.find("vertx.eventloop.queue.size").gauge().value();
		return normalize(value != null ? value : 0.0, 0, 1000);
	}

	/** 
	 * Vert.x worker核心指标采集
	 * @return
	 */
	private double getWorkerPoolMetric() {
		Double active = registry.find("vertx.worker.pool.active").gauge().value();
		Double queued = registry.find("vertx.worker.queue.size").gauge().value();
		double usage = (active != null ? active : 0.0) + (queued != null ? queued : 0.0);
		return normalize(usage, 0, 200);
	}

	/** 
	 * OSHI cpu硬件指标采集
	 * @return
	 */
	private double getCpuUsage() {
		long[] newTicks = processor.getSystemCpuLoadTicks();
		double load = processor.getSystemCpuLoadBetweenTicks(prevCpuTicks);
		prevCpuTicks = newTicks;
		load = Double.isNaN(load) ? 0 : Math.min(load, 1.0);
		return load;
	}

	/** 
	 * OSHI 硬盘指标采集
	 * @return
	 */
	private double getRawDiskUsage() {
		return systemInfo.getHardware().getDiskStores().stream().mapToDouble(HWDiskStore::getTransferTime).sum() / 10_000.0;
	}

	/** 
	 * JMX 内存指标采集
	 * @return
	 */
	private double getHeapMemoryUsage() {
		MemoryUsage usage = memoryMxBean.getHeapMemoryUsage();
		if (usage.getMax() <= 0)
			return 0.0;
		return (double) usage.getUsed() / usage.getMax();
	}

	private double normalize(double value, double min, double max) {
		return Math.max(0, Math.min(1, (value - min) / (max - min)));
	}

	public LoadState getCurrentState() {
		return currentState.get();
	}

	public int getCurrentScore() {
		return currentScore.get();
	}
}