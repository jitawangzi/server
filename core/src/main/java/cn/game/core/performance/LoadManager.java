package cn.game.core.performance;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.core.Vertx;
import io.vertx.micrometer.backends.BackendRegistries;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;

/**
 * 生产级负载管理器（线程安全、低开销）
 */
public class LoadManager {

	// 10秒采样
	private static final int SAMPLE_INTERVAL = 10_000;

	// 队列、CPU、内存、磁盘权重
	private static final double[] WEIGHTS = { 0.4, 0.3, 0.2, 0.1 };

	// 负载警告和危险阈值
	private static final double WARNING_THRESHOLD = 0.6;
	private static final double CRITICAL_THRESHOLD = 0.8;

	// 增加指标压缩器
	private final MovingAverage cpuAvg = new MovingAverage(5);
	private final MovingAverage memAvg = new MovingAverage(5);
	private final MovingAverage diskAvg = new MovingAverage(3);
	private final MovingAverage queueAvg = new MovingAverage(3);

	// 状态枚举
	public enum LoadState {
		NORMAL, WARNING, CRITICAL
	}

	// 单例模式
	private static final LoadManager INSTANCE = new LoadManager();

	public static LoadManager getInstance() {
		return INSTANCE;
	}

	// 指标缓存
	private final StampedLock lock = new StampedLock();
	private final AtomicReference<LoadState> currentState = new AtomicReference<>(LoadState.NORMAL);
	private final AtomicInteger currentScore = new AtomicInteger(0);
	private final SystemInfo systemInfo = new SystemInfo();

	private MeterRegistry registry;

	// 性能指标缓存
	private double lastCpuLoad = 0.0;
	private double lastMemUsage = 0.0;
	private double lastDiskIO = 0.0;
	private double lastQueueSize = 0.0;

	// 私有构造
	private LoadManager() {
	}

	public void init(Vertx vertx) {
		this.registry = BackendRegistries.getDefaultNow();
		registerMetrics();
		startSampling(vertx);
	}

	private void registerMetrics() {
		Gauge.builder("load.score", currentScore::get).description("综合负载评分").register(registry);

		Gauge.builder("load.state", () -> currentState.get().ordinal())
				.description("负载状态(0=NORMAL,1=WARNING,2=CRITICAL)")
				.register(registry);

		Gauge.builder("load.cpu", () -> lastCpuLoad).description("CPU负载").register(registry);

		Gauge.builder("load.memory", () -> lastMemUsage).description("内存使用率").register(registry);

		Gauge.builder("load.disk", () -> lastDiskIO).description("磁盘IO").register(registry);

		Gauge.builder("load.queue", () -> lastQueueSize).description("事件循环队列大小").register(registry);
	}

	private void startSampling(Vertx vertx) {
		vertx.setPeriodic(SAMPLE_INTERVAL, id -> {
			vertx.executeBlocking(promise -> {
				try {
					updateLoadState();
					promise.complete();
				} catch (Exception e) {
					promise.fail(e);
				}
			}, false, res -> {
				if (res.failed()) {
					// 可以添加日志记录
					System.err.println("Failed to update load state: " + res.cause().getMessage());
				}
			});
		});
	}

	private void updateLoadState() {
		long stamp = lock.tryOptimisticRead();
		double[] metrics = collectMetrics();

		if (!lock.validate(stamp)) {
			stamp = lock.readLock();
			try {
				metrics = collectMetrics();
			} finally {
				lock.unlockRead(stamp);
			}
		}

		double score = calculateScore(metrics);
		LoadState newState = evaluateState(score);

		long writeStamp = lock.writeLock();
		try {
			// 更新当前分数和状态
			currentScore.set((int) (score * 100));
			LoadState oldState = currentState.getAndSet(newState);

			// 只有状态变化时才执行降级策略
			if (oldState != newState) {
				executeDegrade(newState);
			}
		} finally {
			lock.unlockWrite(writeStamp);
		}
	}

	private double[] collectMetrics() {
		// 应用层指标 - 事件循环队列大小
		double queueSize = registry.find("vertx.eventloop.queue.size").gauge() != null
				? registry.find("vertx.eventloop.queue.size").gauge().value()
				: 0.0;

		// 系统层指标（OSHI）
		GlobalMemory memory = systemInfo.getHardware().getMemory();
		double cpuLoad = systemInfo.getHardware().getProcessor().getSystemLoadAverage(1)[0] / Runtime.getRuntime().availableProcessors(); // 标准化CPU负载

		double memUsage = 1 - (memory.getAvailable() / (double) memory.getTotal());

		// 计算磁盘IO（标准化到0-1范围）
		double diskIo = systemInfo.getHardware().getDiskStores().stream().mapToDouble(HWDiskStore::getTransferTime).sum();

		// 标准化磁盘IO (假设最大10000ms为1.0)
		diskIo = Math.min(diskIo / 10000.0, 1.0);

		// 更新缓存的指标值
		lastCpuLoad = cpuLoad;
		lastMemUsage = memUsage;
		lastDiskIO = diskIo;
		lastQueueSize = queueSize;

		// 更新并返回滑动平均值
		return new double[] { queueAvg.next(queueSize / 100.0), // 标准化队列大小
				cpuAvg.next(cpuLoad), memAvg.next(memUsage), diskAvg.next(diskIo) };
	}

	private double calculateScore(double[] metrics) {
		double score = 0;
		for (int i = 0; i < metrics.length; i++) {
			score += metrics[i] * WEIGHTS[i];
		}
		return Math.min(score, 1.0); // 确保分数不超过1.0
	}

	private LoadState evaluateState(double score) {
		if (score >= CRITICAL_THRESHOLD) {
			return LoadState.CRITICAL;
		} else if (score >= WARNING_THRESHOLD) {
			return LoadState.WARNING;
		} else {
			return LoadState.NORMAL;
		}
	}

	private void executeDegrade(LoadState state) {
		// 先恢复所有限制，然后根据当前状态应用新的限制
		DegradeStrategy.recoverAll();

		switch (state) {
		case CRITICAL:
			// 在危险状态时限制登录和全体聊天
			DegradeStrategy.limit(LoadLimitTypeEnum.Login);
			DegradeStrategy.limit(LoadLimitTypeEnum.GlobalChat);
			break;
		case WARNING:
			// 在警告状态只限制全体聊天
			DegradeStrategy.limit(LoadLimitTypeEnum.GlobalChat);
			break;
		default:
			// NORMAL状态不需要限制
			break;
		}
	}

	/**
	 * 获取当前负载状态
	 */
	public LoadState getCurrentState() {
		return currentState.get();
	}

	/**
	 * 获取当前负载分数（0-100）
	 */
	public int getCurrentScore() {
		return currentScore.get();
	}

	/**
	 * 获取当前CPU负载
	 */
	public double getCurrentCpuLoad() {
		return lastCpuLoad;
	}

	/**
	 * 获取当前内存使用率
	 */
	public double getCurrentMemUsage() {
		return lastMemUsage;
	}
}