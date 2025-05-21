package cn.game.core.performance.metric.system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.game.core.performance.metric.AbstractMetricCollector;
import cn.game.core.performance.metric.MetricType;

/**
 * 磁盘I/O指标收集器
 */
public class DiskMetricCollector extends AbstractMetricCollector {

	// 缓存上次disk stats采集的时间和数据
	private Map<String, long[]> lastDiskStats = null;
	private long lastDiskStatsTime = 0;

	public DiskMetricCollector() {
		// 默认阈值配置
		super("disk", MetricType.SYSTEM, 3, 0.7, 0.8);
	}

	@Override
	protected double doCollect() {
		try {
			// 当前时间
			long currentTime = System.currentTimeMillis();

			// 获取当前样本
			Map<String, long[]> currentStats = readDiskStatsOptimized();

			// 如果没有上次样本或者时间太短，保存并返回0
			if (lastDiskStats == null || (currentTime - lastDiskStatsTime) < 200) {
				lastDiskStats = currentStats;
				lastDiskStatsTime = currentTime;
				return 0.0;
			}

			// 计算最大使用率
			double maxUtilization = 0.0;
			long timeElapsedMs = currentTime - lastDiskStatsTime;

			// 只处理最可能的几个设备，避免过多循环
			String[] commonDevices = { "sda", "vda", "nvme0n1", "xvda" };
			for (String device : commonDevices) {
				long[] current = currentStats.get(device);
				long[] previous = lastDiskStats.get(device);

				if (current != null && previous != null && current.length >= 10 && previous.length >= 10) {
					// io_ticks是第10个元素（索引9）
					long ticksDiff = current[9] - previous[9];
					// 计算使用率：ticksDiff毫秒中有多少毫秒在I/O上
					double utilization = (double) ticksDiff / timeElapsedMs;
					// 确保在0-1范围内
					utilization = Math.min(1.0, Math.max(0.0, utilization));
					maxUtilization = Math.max(maxUtilization, utilization);
				}
			}

			// 更新缓存
			lastDiskStats = currentStats;
			lastDiskStatsTime = currentTime;

			return maxUtilization;
		} catch (Exception e) {
			log.debug("Failed to get disk usage", e);
			return 0.0; // 出错时返回0，避免系统误判
		}
	}

	/**
	 * 优化版：读取/proc/diskstats文件
	 */
	private Map<String, long[]> readDiskStatsOptimized() {
		Map<String, long[]> result = new HashMap<>(4);

		try (BufferedReader reader = new BufferedReader(new FileReader("/proc/diskstats"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.trim().split("\\s+", 15);
				if (parts.length >= 14) {
					String deviceName = parts[2];
					// 只收集几个主要物理磁盘
					if ((deviceName.equals("sda") || deviceName.equals("vda") || deviceName.equals("nvme0n1")
							|| deviceName.equals("xvda"))) {
						long[] stats = new long[12];
						for (int i = 0; i < Math.min(12, parts.length - 3); i++) {
							stats[i] = Long.parseLong(parts[i + 3]);
						}
						result.put(deviceName, stats);
					}
				}
			}
		} catch (IOException e) {
			log.debug("Error reading disk stats", e);
		}

		return result;
	}
}