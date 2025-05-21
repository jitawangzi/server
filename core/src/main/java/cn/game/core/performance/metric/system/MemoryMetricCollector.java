package cn.game.core.performance.metric.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import cn.game.core.performance.metric.AbstractMetricCollector;
import cn.game.core.performance.metric.MetricType;

/**
 * 内存指标收集器
 */
public class MemoryMetricCollector extends AbstractMetricCollector {

	private final MemoryMXBean memoryMxBean;

	public MemoryMetricCollector() {
		// 设置内存指标的警告阈值和临界阈值
		super("memory", MetricType.SYSTEM, 5, 0.7, 0.8);
		this.memoryMxBean = ManagementFactory.getMemoryMXBean();
	}

	@Override
	protected double doCollect() {
		// 获取堆内存使用情况
		MemoryUsage heapUsage = memoryMxBean.getHeapMemoryUsage();

		// 如果最大值未定义或为负，使用已提交内存作为基准
		long max = heapUsage.getMax();
		if (max <= 0) {
			max = heapUsage.getCommitted();
		}

		// 计算使用率
		if (max > 0) {
			return (double) heapUsage.getUsed() / max;
		}

		return 0.0;
	}

	/**
	 * 获取非堆内存使用率（可选扩展）
	 * @return 非堆内存使用率
	 */
	public double getNonHeapUsage() {
		MemoryUsage nonHeapUsage = memoryMxBean.getNonHeapMemoryUsage();
		long committed = nonHeapUsage.getCommitted();
		if (committed > 0) {
			return (double) nonHeapUsage.getUsed() / committed;
		}
		return 0.0;
	}
}