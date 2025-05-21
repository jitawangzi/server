package cn.game.core.performance.metric.system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import cn.game.core.performance.metric.AbstractMetricCollector;
import cn.game.core.performance.metric.MetricType;

/**
 * 网络指标收集器
 */
public class NetworkMetricCollector extends AbstractMetricCollector {

	// 缓存上次网络采集的时间和数据
	private Map<String, long[]> lastNetworkStats = null;
	private long lastNetworkStatsTime = 0;

	// 默认接口最大速率 (单位: bytes/sec)
	private static final long DEFAULT_NET_MAX_SPEED = 1_000_000_000L; // 1 Gbps
	private Map<String, Long> interfaceMaxSpeeds = new HashMap<>();

	public NetworkMetricCollector() {
		// 设置网络指标的警告阈值和临界阈值
		super("network", MetricType.NETWORK, 3, 0.7, 0.8);
	}

	@Override
	public void init() {
		// 初始化网络接口速率
		initNetworkSpeeds();
	}

	@Override
	protected double doCollect() {
		try {
			// 当前时间
			long currentTime = System.currentTimeMillis();

			// 获取当前样本
			Map<String, long[]> currentStats = readNetworkStats();

			// 如果没有上次样本或者时间太短，保存并返回0
			if (lastNetworkStats == null || (currentTime - lastNetworkStatsTime) < 200) {
				lastNetworkStats = currentStats;
				lastNetworkStatsTime = currentTime;
				return 0.0;
			}

			// 计算最大使用率
			double maxUtilization = 0.0;
			long timeElapsedMs = currentTime - lastNetworkStatsTime;

			for (Map.Entry<String, long[]> entry : currentStats.entrySet()) {
				String iface = entry.getKey();

				// 跳过lo回环接口和虚拟接口
				if (iface.equals("lo") || iface.startsWith("docker") || iface.startsWith("veth") || iface.startsWith("virbr")) {
					continue;
				}

				long[] current = entry.getValue();
				long[] previous = lastNetworkStats.get(iface);

				if (previous != null && current.length >= 2 && previous.length >= 2) {
					// 计算收发的字节数差异 (rx_bytes, tx_bytes)
					long rxDiff = current[0] - previous[0];
					long txDiff = current[1] - previous[1];

					// 总流量 (bytes/sec)
					long totalBytesDiff = rxDiff + txDiff;
					double bytesPerSec = totalBytesDiff * 1000.0 / timeElapsedMs;

					// 获取接口最大速率
					Long maxSpeed = interfaceMaxSpeeds.getOrDefault(iface, DEFAULT_NET_MAX_SPEED);

					// 计算使用率
					double utilization = bytesPerSec / maxSpeed;

					// 确保在0-1范围内
					utilization = Math.min(1.0, Math.max(0.0, utilization));
					maxUtilization = Math.max(maxUtilization, utilization);
				}
			}

			// 更新缓存
			lastNetworkStats = currentStats;
			lastNetworkStatsTime = currentTime;

			return maxUtilization;
		} catch (Exception e) {
			log.debug("Failed to get network usage", e);
			return 0.0; // 出错时返回0，避免系统误判
		}
	}

	/**
	 * 读取网络接口统计信息
	 * @return 网络接口统计数据映射，每个接口对应一个数组[rx_bytes, tx_bytes]
	 */
	private Map<String, long[]> readNetworkStats() {
		Map<String, long[]> result = new HashMap<>(8);

		try (BufferedReader reader = new BufferedReader(new FileReader("/proc/net/dev"))) {
			// 跳过前两行（标题行）
			reader.readLine();
			reader.readLine();

			String line;
			while ((line = reader.readLine()) != null) {
				// 格式: Interface: rx_bytes packets errs drop fifo frame compressed multicast
				// tx_bytes packets...
				String[] parts = line.trim().split(":");
				if (parts.length >= 2) {
					String iface = parts[0].trim();
					String[] stats = parts[1].trim().split("\\s+");

					if (stats.length >= 10) {
						// 只保存接收和发送的字节数
						long[] netStats = new long[2];
						netStats[0] = Long.parseLong(stats[0]); // rx_bytes
						netStats[1] = Long.parseLong(stats[8]); // tx_bytes
						result.put(iface, netStats);
					}
				}
			}
		} catch (IOException e) {
			log.debug("Error reading network stats", e);
		}

		return result;
	}

	/**
	 * 初始化网络接口的最大速率
	 * 读取/sys/class/net/[interface]/speed获取网络接口速率
	 */
	private void initNetworkSpeeds() {
		// 常见的网络接口名称
		String[] commonInterfaces = { "eth0", "eth1", "ens3", "ens4", "enp0s3", "enp0s8" };

		for (String iface : commonInterfaces) {
			try {
				// 尝试从系统文件读取速率 (Mbits/sec)
				Path speedPath = Paths.get("/sys/class/net/", iface, "speed");
				if (Files.exists(speedPath)) {
					String speed = new String(Files.readAllBytes(speedPath)).trim();
					// 转换为bytes/sec (1 Mbit = 125000 bytes)
					long maxSpeed = Long.parseLong(speed) * 125000L;
					interfaceMaxSpeeds.put(iface, maxSpeed);
				}
			} catch (Exception e) {
				// 如果读取失败，使用默认值
				log.debug("Could not read speed for interface " + iface, e);
			}
		}
	}
}