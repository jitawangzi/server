package cn.game.core.performance;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.core.Vertx;
import io.vertx.micrometer.backends.BackendRegistries;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;

/**
 * 生产级负载管理器（线程安全、低开销）
 */
public class LoadManager {

	// 采样间隔 5秒
	private static final int SAMPLE_INTERVAL = 5_000;

	// 权重分配：队列、Worker线程池、CPU、内存、磁盘
	private static final double[] WEIGHTS = { 0.3, 0.2, 0.3, 0.15, 0.05 }; // 提高CPU权重

	// 阈值配置
	private static final double WARNING_THRESHOLD = 0.6;
	private static final double CRITICAL_THRESHOLD = 0.8;
	private static final double SINGLE_METRIC_CRITICAL = 0.9; // 单项指标阈值

	private long[] prevCpuTicks;

	// 滑动平均窗口
	private final MovingAverage cpuAvg = new MovingAverage(3);
	private final MovingAverage memAvg = new MovingAverage(3);
	private final MovingAverage diskAvg = new MovingAverage(3);
	private final MovingAverage queueAvg = new MovingAverage(3);
	private final MovingAverage workerAvg = new MovingAverage(3);

	// 状态枚举
	public enum LoadState {
		NORMAL, WARNING, CRITICAL
	}

	// 单例实例
	private static final LoadManager INSTANCE = new LoadManager();

	public static LoadManager getInstance() {
		return INSTANCE;
	}

	// 线程安全控制
	private final StampedLock lock = new StampedLock();
	private final AtomicReference<LoadState> currentState = new AtomicReference<>(LoadState.NORMAL);
	private final AtomicInteger currentScore = new AtomicInteger(0);

	// 系统信息
	private final SystemInfo systemInfo = new SystemInfo();
	private final CentralProcessor processor = systemInfo.getHardware().getProcessor();
	private MeterRegistry registry;

	// 性能指标缓存（volatile保证可见性）
	private volatile double lastCpuLoad = 0.0;
	private volatile double lastMemUsage = 0.0;
	private volatile double lastDiskIO = 0.0;
	private volatile double lastQueueSize = 0.0;
	private volatile double lastWorkerPoolUsage = 0.0;

	private LoadManager() {
	}

	public void init(Vertx vertx) {
		this.registry = BackendRegistries.getDefaultNow();
		registerMetrics();
		startSampling(vertx);
	}

	private void registerMetrics() {
		Gauge.builder("load.score", currentScore::get).register(registry);
		Gauge.builder("load.state", () -> currentState.get().ordinal()).register(registry);
		Gauge.builder("load.cpu", () -> lastCpuLoad).register(registry);
		Gauge.builder("load.memory", () -> lastMemUsage).register(registry);
		Gauge.builder("load.disk", () -> lastDiskIO).register(registry);
		Gauge.builder("load.queue", () -> lastQueueSize).register(registry);
		Gauge.builder("load.worker", () -> lastWorkerPoolUsage).register(registry);
	}

	private void initCpuTicks() {
		prevCpuTicks = processor.getSystemCpuLoadTicks();
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

	private void updateLoadState() {
		long stamp = lock.writeLock(); // 全程写锁保证原子性
		try {
			double[] metrics = collectMetrics();
			double score = calculateScore(metrics);
			LoadState newState = evaluateState(score, metrics);

			currentScore.set((int) (score * 100));
			LoadState oldState = currentState.getAndSet(newState);

			if (oldState != newState) {
				executeDegrade(newState);
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	private double[] collectMetrics() {
		// 1. 事件循环队列大小（标准化）
		double queueSize = registry.find("vertx.eventloop.queue.size").gauge().value();
		queueSize = Math.min(queueSize / 100.0, 1.0); // 假设队列超过100认为满载

		// 2. Worker线程池使用率
		double workerPoolUsage = calculateWorkerPoolUsage();

		// 3. 系统指标（使用oshi正确方法）
		// CPU使用率（已考虑多核）
		double cpuLoad = getCpuLoad();
		cpuLoad = (Double.isNaN(cpuLoad) || cpuLoad < 0) ? 0 : Math.min(cpuLoad, 1.0);

		// 内存使用率
		GlobalMemory memory = systemInfo.getHardware().getMemory();
		double memUsage = 1.0 - (memory.getAvailable() / (double) memory.getTotal());

		// 磁盘IO（标准化）
		double diskIo = systemInfo.getHardware().getDiskStores().stream().mapToDouble(HWDiskStore::getTransferTime).sum() / 10_000.0; // 假设10000ms为满载

		// 更新缓存
		lastCpuLoad = cpuLoad;
		lastMemUsage = memUsage;
		lastDiskIO = diskIo;
		lastQueueSize = queueSize;
		lastWorkerPoolUsage = workerPoolUsage;

		// 返回滑动平均后的指标
		return new double[] { queueAvg.next(queueSize), workerAvg.next(workerPoolUsage), cpuAvg.next(cpuLoad), memAvg.next(memUsage),
				diskAvg.next(diskIo) };
	}

	private double calculateWorkerPoolUsage() {
		double poolSize = registry.find("vertx.worker.pool.size").gauge().value();
		if (poolSize <= 0)
			return 0.0;

		double active = registry.find("vertx.worker.pool.active").gauge().value();
		double queued = registry.find("vertx.worker.queue.size").gauge().value();

		double threadUsage = active / poolSize;
		double queueFactor = Math.min(queued / poolSize, 1.0);
		return (threadUsage * 0.7) + (queueFactor * 0.3);
	}

	private double calculateScore(double[] metrics) {
		double weightedSum = 0;
		for (int i = 0; i < metrics.length; i++) {
			weightedSum += metrics[i] * WEIGHTS[i];
		}
		return Math.min(weightedSum, 1.0);
	}

	private LoadState evaluateState(double score, double[] metrics) {
		// 先检查单项指标是否超过紧急阈值
		for (double metric : metrics) {
			if (metric >= SINGLE_METRIC_CRITICAL) {
				return LoadState.CRITICAL;
			}
		}

		// 再根据综合评分判断
		if (score >= CRITICAL_THRESHOLD) {
			return LoadState.CRITICAL;
		} else if (score >= WARNING_THRESHOLD) {
			return LoadState.WARNING;
		} else {
			return LoadState.NORMAL;
		}
	}

	private void executeDegrade(LoadState state) {
		DegradeStrategy.recoverAll();
		switch (state) {
		case CRITICAL:
			DegradeStrategy.limit(LoadLimitTypeEnum.Login);
			DegradeStrategy.limit(LoadLimitTypeEnum.GlobalChat);
			DegradeStrategy.limit(LoadLimitTypeEnum.LeaderboardUpdate);
			break;
		case WARNING:
			DegradeStrategy.limit(LoadLimitTypeEnum.GlobalChat);
			break;
		default: // NORMAL不操作
		}
	}

	private double getCpuLoad() {
		long[] newTicks = processor.getSystemCpuLoadTicks();
		double load = processor.getSystemCpuLoadBetweenTicks(prevCpuTicks);
		prevCpuTicks = newTicks;
		return load;
	}

	public MovingAverage getCpuAvg() {
		return cpuAvg;
	}

	public MovingAverage getMemAvg() {
		return memAvg;
	}

	public MovingAverage getDiskAvg() {
		return diskAvg;
	}

	public MovingAverage getQueueAvg() {
		return queueAvg;
	}

	public MovingAverage getWorkerAvg() {
		return workerAvg;
	}

	public StampedLock getLock() {
		return lock;
	}

	public AtomicReference<LoadState> getCurrentState() {
		return currentState;
	}

	public AtomicInteger getCurrentScore() {
		return currentScore;
	}

	public SystemInfo getSystemInfo() {
		return systemInfo;
	}

	public CentralProcessor getProcessor() {
		return processor;
	}

	public MeterRegistry getRegistry() {
		return registry;
	}

	public double getLastCpuLoad() {
		return lastCpuLoad;
	}

	public double getLastMemUsage() {
		return lastMemUsage;
	}

	public double getLastDiskIO() {
		return lastDiskIO;
	}

	public double getLastQueueSize() {
		return lastQueueSize;
	}

	public double getLastWorkerPoolUsage() {
		return lastWorkerPoolUsage;
	}

}