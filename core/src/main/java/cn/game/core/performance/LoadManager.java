package cn.game.core.performance;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;

import cn.game.core.performance.evaluation.LoadEvaluator;
import cn.game.core.performance.evaluation.LoadState;
import cn.game.core.performance.metric.MetricCollector;
import cn.game.core.performance.metric.MetricRegistry;
import cn.game.core.performance.metric.system.CpuMetricCollector;
import cn.game.core.performance.metric.system.DiskMetricCollector;
import cn.game.core.performance.metric.system.MemoryMetricCollector;
import cn.game.core.performance.metric.system.NetworkMetricCollector;
import cn.game.core.performance.metric.vertx.VertxEventLoopMetricCollector;
import cn.game.core.performance.metric.vertx.VertxWorkerPoolMetricCollector;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.core.Vertx;
import io.vertx.micrometer.backends.BackendRegistries;

/**
 * 负载管理器，作为系统监控的主协调类
 */
public class LoadManager {

	private final Logger log = LoggerFactory.getLogger(LoadManager.class);

	// 采样间隔
	private static final int SAMPLE_INTERVAL = 5_000;

	// 负载状态和评分
	private final AtomicReference<LoadState> currentState = new AtomicReference<>(LoadState.NORMAL);
	private final AtomicInteger currentScore = new AtomicInteger(0);

	// 监控相关组件
	private final MetricRegistry metricRegistry = MetricRegistry.getInstance();
	private final LoadEvaluator evaluator;
	private MeterRegistry registry;

	// 专用监控线程
	private Thread monitorThread;
	private volatile boolean running = false;

	// 状态变化监听器
	private final List<LoadStateChangeListener> stateChangeListeners = new CopyOnWriteArrayList<>();

	// 单例实例
	private static final LoadManager INSTANCE = new LoadManager();

	private LoadManager() {
		// 初始化评估器
		this.evaluator = new LoadEvaluator(0.6, 0.8);
	}

	public static LoadManager getInstance() {
		return INSTANCE;
	}

	/**
	 * 负载状态变化监听器接口
	 */
	public interface LoadStateChangeListener {
		/**
		 * 当负载状态变化时调用
		 * @param oldState 旧状态
		 * @param newState 新状态
		 * @param currentScore 当前得分
		 */
		void onStateChange(LoadState oldState, LoadState newState, int currentScore);
	}

	/**
	 * 注册负载状态变化监听器
	 * @param listener 状态变化监听器
	 */
	public void addStateChangeListener(LoadStateChangeListener listener) {
		if (listener != null) {
			stateChangeListeners.add(listener);
		}
	}

	/**
	 * 取消注册负载状态变化监听器
	 * @param listener 要移除的监听器
	 * @return 是否成功移除
	 */
	public boolean removeStateChangeListener(LoadStateChangeListener listener) {
		return stateChangeListeners.remove(listener);
	}

	/**
	 * 初始化负载管理器
	 * @param vertx Vertx实例
	 */
	public void init(Vertx vertx) {
		this.registry = BackendRegistries.getDefaultNow();

		// 注册基础指标收集器
		registerDefaultCollectors(vertx);

		// 设置指标权重
		configureWeights();

		// 注册Micrometer监控指标
		registerMetrics();

		// 启动专用监控线程
		startMonitorThread();
	}

	/**
	 * 注册默认的指标收集器
	 */
	private void registerDefaultCollectors(Vertx vertx) {
		// 系统基础指标
		metricRegistry.register(new CpuMetricCollector());
		metricRegistry.register(new MemoryMetricCollector());
		metricRegistry.register(new DiskMetricCollector());
		metricRegistry.register(new NetworkMetricCollector());

		// Vertx特定指标
		if (vertx != null) {
			metricRegistry.register(new VertxEventLoopMetricCollector(vertx));
			metricRegistry.register(new VertxWorkerPoolMetricCollector(vertx));
		}
	}

	/**
	 * 配置各指标权重
	 */
	private void configureWeights() {
		evaluator.setWeight("cpu", 0.15)
				.setWeight("memory", 0.15)
				.setWeight("disk", 0.15)
				.setWeight("network", 0.15)
				.setWeight("vertx.eventloop", 0.25)
				.setWeight("vertx.workerpool", 0.15);
	}

	/**
	 * 注册监控指标到Micrometer
	 */
	private void registerMetrics() {
		Gauge.builder("load.score", currentScore::get).register(registry);
		Gauge.builder("load.state", () -> currentState.get().ordinal()).register(registry);

		// 注册所有指标收集器的最新值
		for (MetricCollector collector : metricRegistry.getCollectors().values()) {
			Gauge.builder("metrics." + collector.getName(), collector::getLatestValue).register(registry);
		}
	}

	/**
	 * 启动专用监控线程
	 */
	private void startMonitorThread() {
		running = true;
		monitorThread = new Thread(() -> {
			Thread.currentThread().setName("system-monitor-thread");
			// 设置低优先级，避免影响业务处理
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

			while (running) {
				try {
					// 收集并更新系统指标
					collectAndUpdateMetrics();
					// 指定的采样间隔
					Thread.sleep(SAMPLE_INTERVAL);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				} catch (Exception e) {
					log.error("Error in monitor thread", e);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		});

		monitorThread.setDaemon(true);
		monitorThread.start();

		log.info("System monitor thread started");
	}

	/**
	 * 收集并更新系统指标
	 */
	private void collectAndUpdateMetrics() {
		// 收集所有指标
		Map<String, Double> metrics = metricRegistry.collectAll();

		// 评估系统状态
		LoadState newState = evaluator.evaluate(metrics);

		// 计算综合评分
		double score = calculateScore(metrics);

		final LoadState finalNewState = newState;
		final double finalScore = score;

		// 更新评分
		currentScore.set((int) (finalScore * 100));
		// 获取旧状态并设置新状态
		LoadState oldState = currentState.getAndSet(finalNewState);
		// 如果状态发生变化，执行降级策略并通知监听器
		if (oldState != finalNewState) {
			executeDegrade(finalNewState);
			notifyStateChangeListeners(oldState, finalNewState, currentScore.get());
		}
	}

	/**
	 * 通知所有负载状态变化监听器
	 */
	private void notifyStateChangeListeners(LoadState oldState, LoadState newState, int score) {
		for (LoadStateChangeListener listener : stateChangeListeners) {
			try {
				listener.onStateChange(oldState, newState, score);
			} catch (Exception e) {
				log.error("Error notifying load state change listener", e);
			}
		}
	}

	/**
	 * 计算综合评分
	 */
	private double calculateScore(Map<String, Double> metrics) {
		double totalScore = 0.0;
		double totalWeight = 0.0;

		for (Map.Entry<String, Double> entry : metrics.entrySet()) {
			String name = entry.getKey();
			double value = entry.getValue();
			double weight = evaluator.getWeight(name);

			totalScore += value * weight;
			totalWeight += weight;
		}

		return totalWeight > 0 ? totalScore / totalWeight : 0;
	}

	/**
	 * 执行降级策略
	 */
	private void executeDegrade(LoadState state) {
		DegradeStrategy.recoverAll();

		// 根据状态执行降级策略
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

		// 基于特定指标的单独限制
		MetricCollector eventLoopCollector = metricRegistry.getCollector("vertx.eventloop");
		if (eventLoopCollector != null && eventLoopCollector.getLatestValue() >= eventLoopCollector.getCriticalThreshold()) {
			DegradeStrategy.limit(LoadLimitTypeEnum.NoBlockingOperation);
		}

		MetricCollector workerPoolCollector = metricRegistry.getCollector("vertx.workerpool");
		if (workerPoolCollector != null && workerPoolCollector.getLatestValue() >= workerPoolCollector.getCriticalThreshold()) {
			DegradeStrategy.limit(LoadLimitTypeEnum.BlockingOperation);
		}
	}

	/**
	 * 关闭负载管理器
	 */
	public void shutdown() {
		running = false;
		if (monitorThread != null) {
			monitorThread.interrupt();
			try {
				monitorThread.join(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		metricRegistry.shutdownAll();
	}

	/**
	 * 注册自定义指标收集器
	 */
	public void registerCollector(MetricCollector collector) {
		metricRegistry.register(collector);
		evaluator.setWeight(collector.getName(), 0.1); // 默认权重
		// 更新Micrometer指标
		Gauge.builder("metrics." + collector.getName(), collector::getLatestValue).register(registry);
	}

	/**
	 * 设置指标权重
	 */
	public void setMetricWeight(String metricName, double weight) {
		evaluator.setWeight(metricName, weight);
	}

	/**
	 * 获取当前系统负载状态
	 */
	public LoadState getCurrentState() {
		return currentState.get();
	}

	/**
	 * 获取当前系统负载评分
	 */
	public int getCurrentScore() {
		return currentScore.get();
	}

	/** 
	 * 获取所有监控指标，最新获取到的数值
	 * @return
	 */
	public String showMetrics() {
		JSONObject sb = new JSONObject();
		Map<String, MetricCollector> collectors = metricRegistry.getCollectors();
		TreeSet<String> treeSet = new TreeSet<String>(collectors.keySet());
		for (String key : treeSet) {
			double value = Math.round(collectors.get(key).getLatestValue() * 100.0) / 100.0;
//			sb.append(key).append(": ").append(Math.round(value.getLatestValue() * 100.0) / 100.0).append(" ");
			sb.put(key, value);
		}
		return sb.toString();
	}
}