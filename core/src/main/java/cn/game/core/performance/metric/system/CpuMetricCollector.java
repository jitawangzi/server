package cn.game.core.performance.metric.system;

import cn.game.core.performance.metric.AbstractMetricCollector;
import cn.game.core.performance.metric.MetricType;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

/**
 * CPU指标收集器
 */
public class CpuMetricCollector extends AbstractMetricCollector {

	private final SystemInfo systemInfo;
	private final CentralProcessor processor;
	private long[] prevCpuTicks;

	public CpuMetricCollector() {
		// 设置CPU指标的警告阈值和临界阈值
		super("cpu", MetricType.SYSTEM, 3, 0.7, 0.9);
		this.systemInfo = new SystemInfo();
		this.processor = systemInfo.getHardware().getProcessor();
		// 提前初始化CPU使用基准数据，避免首次采集数据失真
		this.prevCpuTicks = processor.getSystemCpuLoadTicks();
	}

	@Override
	protected double doCollect() {
		long[] newTicks = processor.getSystemCpuLoadTicks();
		double load = processor.getSystemCpuLoadBetweenTicks(prevCpuTicks);
		// 更新前一次的数据，供下次使用
		prevCpuTicks = newTicks;

		// 处理首次采集可能出现的NaN值
		if (Double.isNaN(load)) {
			return 0.0;
		}

		// 确保在0-1范围内
		return Math.min(1.0, Math.max(0.0, load));
	}

	@Override
	public void init() {
		// 初始化时预热一次，避免首次采集不准确
		prevCpuTicks = processor.getSystemCpuLoadTicks();
		try {
			Thread.sleep(100); // 短暂等待以获取初始差值
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}