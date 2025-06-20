package cn.game.core.performance.evaluation;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.performance.metric.MetricCollector;
import cn.game.core.performance.metric.MetricRegistry;

/**
 * 负载评估器，根据指标值评估系统状态
 */
public class LoadEvaluator {
	private static final Logger log = LoggerFactory.getLogger(LoadEvaluator.class);

	private final Map<String, Double> metricWeights = new HashMap<>();
	private final double warningThreshold;
	private final double criticalThreshold;

	public LoadEvaluator(double warningThreshold, double criticalThreshold) {
		this.warningThreshold = warningThreshold;
		this.criticalThreshold = criticalThreshold;
	}

	/**
	 * 设置指标权重
	 * @param metricName 指标名称
	 * @param weight 权重值
	 * @return 自身，支持链式调用
	 */
	public LoadEvaluator setWeight(String metricName, double weight) {
		metricWeights.put(metricName, weight);
		return this;
	}

	public double getWeight(String metricName) {
		return metricWeights.getOrDefault(metricName, 0.0);
	}

	/**
	 * 评估系统负载状态
	 * @param metricValues 指标值映射
	 * @return 负载状态
	 */
	public LoadState evaluate(Map<String, Double> metricValues) {
		// 首先检查是否有指标达到临界值
		for (Map.Entry<String, Double> entry : metricValues.entrySet()) {
			String name = entry.getKey();
			double value = entry.getValue();

			MetricCollector collector = MetricRegistry.getInstance().getCollector(name);
			if (collector != null && value >= collector.getCriticalThreshold()) {
				// 如果任何指标超过其临界值，则系统处于临界状态
				log.warn("Metric [{}] value [{}] exceeds critical threshold [{}]", name, value, collector.getCriticalThreshold());
				return LoadState.CRITICAL;
			}
			if (collector != null && value >= collector.getWarningThreshold()) {
				// 如果任何指标超过其临界值，则系统处于临界状态
				log.warn("Metric [{}] value [{}] exceeds warning threshold [{}]", name, value, collector.getWarningThreshold());
				return LoadState.WARNING;
			}
		}

		// 计算加权得分
		double totalScore = 0.0;
		double totalWeight = 0.0;

		for (Map.Entry<String, Double> entry : metricValues.entrySet()) {
			String name = entry.getKey();
			double value = entry.getValue();
			double weight = metricWeights.getOrDefault(name, 0.0);

			totalScore += value * weight;
			totalWeight += weight;
		}

		double score = totalWeight > 0 ? totalScore / totalWeight : 0;

		// 根据得分确定状态
		if (score >= criticalThreshold) {
			return LoadState.CRITICAL;
		} else if (score >= warningThreshold) {
			return LoadState.WARNING;
		} else {
			return LoadState.NORMAL;
		}
	}
}