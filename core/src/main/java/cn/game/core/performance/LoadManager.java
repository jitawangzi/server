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
	private static final int SAMPLE_INTERVAL = 1_000; // 1秒采样
	private static final double[] WEIGHTS = { 0.4, 0.3, 0.2, 0.1 }; // 队列、CPU、内存、磁盘权重
	// 增加指标压缩器
	private final MovingAverage cpuAvg = new MovingAverage(5);
	private final MovingAverage memAvg = new MovingAverage(5);

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
			currentScore.set((int) (score * 100));
			currentState.set(newState);
			executeDegrade(newState);
		} finally {
			lock.unlockWrite(writeStamp);
		}
	}

	private double[] collectMetrics() {
		// 应用层指标
		double queueSize = registry.find("vertx.eventloop.queue.size").gauge().value();

		// 系统层指标（OSHI）
		GlobalMemory memory = systemInfo.getHardware().getMemory();
		double cpuLoad = systemInfo.getHardware().getProcessor().getSystemLoadAverage(1)[0];
		double memUsage = 1 - (memory.getAvailable() / (double) memory.getTotal());
		double diskIo = systemInfo.getHardware().getDiskStores().stream().mapToDouble(HWDiskStore::getTransferTime).sum();
		// 应用容器感知
		double containerCpu = ContainerAwareMetrics.getContainerCpuUsage(cpuLoad);
		double containerMem = ContainerAwareMetrics.getContainerMemUsage((long) (memUsage * 1e6));

		// 应用滑动平均
		return new double[] { queueSize, cpuAvg.next(containerCpu), memAvg.next(containerMem), diskIo };
	}

	private double calculateScore(double[] metrics) {
		double score = 0;
		for (int i = 0; i < metrics.length; i++) {
			score += metrics[i] * WEIGHTS[i];
		}
		return score;
	}

	private LoadState evaluateState(double score) {
		// 动态基线调整（示例：基于历史数据）
//		double baseline = BaselineManager.getBaseline();
		double baseline = 0.5; // 示例基线值,待完善
		if (score > baseline * 1.3)
			return LoadState.CRITICAL;
		if (score > baseline * 1.1)
			return LoadState.WARNING;
		return LoadState.NORMAL;
	}
	private void executeDegrade(LoadState state) {
		switch (state) {
		case CRITICAL:
			DegradeStrategy.limit(LoadLimitTypeEnum.Login);
			break;
		case WARNING:
			DegradeStrategy.limit(LoadLimitTypeEnum.GlobalChat);
			break;
		default:
			DegradeStrategy.recoverAll();
		}
	}

	public LoadState getCurrentState() {
		return currentState.get();
	}
}