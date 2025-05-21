package cn.game.core.performance.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.performance.MovingAverage;

/**
 * 通用指标收集器实现
 */
public abstract class AbstractMetricCollector implements MetricCollector {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected volatile double latestValue = 0.0;
	protected final MovingAverage movingAvg;
	protected final String name;
	protected final MetricType type;
	protected final double warningThreshold;
	protected final double criticalThreshold;

	protected AbstractMetricCollector(String name, MetricType type, int windowSize, double warningThreshold, double criticalThreshold) {
		this.name = name;
		this.type = type;
		this.movingAvg = new MovingAverage(windowSize);
		this.warningThreshold = warningThreshold;
		this.criticalThreshold = criticalThreshold;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public MetricType getType() {
		return type;
	}

	@Override
	public double collect() {
		try {
			double value = doCollect();
			latestValue = value;
			return movingAvg.next(value);
		} catch (Exception e) {
			log.error("Error collecting metric: " + name, e);
			return latestValue;
		}
	}

	@Override
	public double getLatestValue() {
		return latestValue;
	}

	@Override
	public double getWarningThreshold() {
		return warningThreshold;
	}

	@Override
	public double getCriticalThreshold() {
		return criticalThreshold;
	}

	@Override
	public void init() {
		// 默认空实现，子类可根据需要重写
	}

	@Override
	public void shutdown() {
		// 默认空实现，子类可根据需要重写
	}

	/**
	 * 执行实际的指标收集
	 * @return 收集到的原始值（0.0-1.0范围）
	 */
	protected abstract double doCollect();

	/**
	 * 标准化值到0-1范围
	 */
	protected double normalize(double value, double min, double max) {
		return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
	}
}