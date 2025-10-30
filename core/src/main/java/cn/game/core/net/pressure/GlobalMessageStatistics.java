package cn.game.core.net.pressure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.client.AbstractNetClient;

public class GlobalMessageStatistics {
	private static final Logger log = LoggerFactory.getLogger(GlobalMessageStatistics.class);
	private static final GlobalMessageStatistics instance = new GlobalMessageStatistics();

	// 消息统计窗口大小(毫秒)
	private static long STATISTICS_WINDOW = 5 * 60 * 1000;

	// 消息记录：消息名 -> 时间窗口 -> 统计数据
	private final Map<String, NavigableMap<Long, MessageStats>> messageStatsMap = new ConcurrentHashMap<>();

	public static GlobalMessageStatistics getInstance() {
		return instance;
	}

	// 单个消息的统计数据
	private static class MessageStats {
		List<Double> responseTimes = new ArrayList<>();
		long requestCount = 0;
		long windowStartTime;

		MessageStats(long windowStartTime) {
			this.windowStartTime = windowStartTime;
		}
	}

	public void recordMessage(String msgName, long sendTime, long recvTime) {
		long windowKey = sendTime / STATISTICS_WINDOW * STATISTICS_WINDOW;

		messageStatsMap.computeIfAbsent(msgName, k -> new TreeMap<>()).computeIfAbsent(windowKey, k -> new MessageStats(k)).requestCount++;

		if (recvTime > 0) {
			double responseTime = (recvTime - sendTime) / 1_000_000.0;
			if (responseTime >= 0) {
				messageStatsMap.get(msgName).get(windowKey).responseTimes.add(responseTime);
			} else {
				log.error("响应时间小于0，消息名：{}，发送时间：{}，接收时间：{}", msgName, sendTime, recvTime);
			}
		}
	}

	public void mergeClientData(Collection<? extends AbstractNetClient> clients) {
		for (AbstractNetClient client : clients) {
			client.sendMessages.forEach((seq, sendPair) -> {
				String msgName = sendPair.getLeft();
				long sendTime = sendPair.getRight();

				Pair<String, Long> recvPair = client.recvMessages.get(seq);
				long recvTime = recvPair != null ? recvPair.getRight() : 0;

				recordMessage(msgName, sendTime, recvTime);
			});

			// 清理客户端数据
			client.sendMessages.clear();
			client.recvMessages.clear();
		}

		// 清理过期数据
		cleanExpiredData();
	}

	private void cleanExpiredData() {
		long currentTime = System.currentTimeMillis();
		long expireTime = currentTime - STATISTICS_WINDOW;

		messageStatsMap.values().forEach(windowMap -> {
			windowMap.headMap(expireTime).clear();
		});
	}

	public void calculateStatisticsAndSaveResult(Collection<? extends AbstractNetClient> clients)
			throws IOException {
		Calendar c = Calendar.getInstance();
		String fileName = String.format("%s_%02d-%02d", System.getProperty("botIdStart"), c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE));

		String dirPath = System.getProperty("user.dir") + "/statistics/";
		new File(dirPath).mkdirs();

		String filePath = dirPath + fileName + ".csv";
		String slowFilePath = dirPath + fileName + "-slowMessage.csv";

		mergeClientData(clients);
		calculateStatistics(filePath, slowFilePath);
	}

	private void calculateStatistics(String outputFilePath, String slowFilePath) throws IOException {
		boolean pressureDev = Boolean.getBoolean("pressureDev");
		String[] headers = { "消息号", "请求数量", "响应数量", "响应率", "最小响应", "最大响应", "平均响应", "50%分位", "75%分位", "90%分位" };

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8);
				OutputStreamWriter writerSlow = pressureDev
						? new OutputStreamWriter(new FileOutputStream(slowFilePath), StandardCharsets.UTF_8)
						: null) {

			writer.write('\ufeff');
			if (writerSlow != null) {
				writerSlow.write('\ufeff');
			}

			try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers));
					CSVPrinter printerSlow = writerSlow != null ? new CSVPrinter(writerSlow, CSVFormat.DEFAULT.withHeader(headers))
							: null) {
				// 按 key 的自然顺序（String 的字典序）遍历
				messageStatsMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
					String messageName = entry.getKey();
					NavigableMap<Long, MessageStats> timeWindowMap = entry.getValue();
					List<Double> allTimes = new ArrayList<>();
					long totalRequests = 0;

					// 合并所有时间窗口的数据
					for (MessageStats stats : timeWindowMap.values()) {
						allTimes.addAll(stats.responseTimes);
						totalRequests += stats.requestCount;
					}
					if (!allTimes.isEmpty()) {
						try {
							printMessageStats(messageName, totalRequests, allTimes, printer, printerSlow);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}

	private void printMessageStats(String msgName, long requestCount, List<Double> times, CSVPrinter printer, CSVPrinter printerSlow)
			throws IOException {
		long responseCount = times.size();
		double responseRate = (responseCount / (double) requestCount) * 100;

		Collections.sort(times);
		double minTime = times.get(0);
		double maxTime = times.get(times.size() - 1);
		double avgTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		double p50 = percentile(times, 50);
		double p75 = percentile(times, 75);
		double p90 = percentile(times, 90);

		printer.printRecord(msgName, requestCount, responseCount, String.format("%.2f%%", responseRate), formatDecimal(minTime),
				formatDecimal(maxTime), formatDecimal(avgTime), p50, p75, p90);

		if (printerSlow != null && (p50 > 10 || responseRate < 100)) {
			printerSlow.printRecord(msgName, requestCount, responseCount, String.format("%.2f%%", responseRate), formatDecimal(minTime),
					formatDecimal(maxTime), formatDecimal(avgTime), p50, p75, p90);
		}
	}

	private double percentile(List<Double> times, double percentile) {
		int index = (int) Math.ceil(times.size() * percentile / 100.0) - 1;
		return Double.parseDouble(formatDecimal(times.get(index)));
	}

	private String formatDecimal(double value) {
		return new BigDecimal(Double.toString(value)).setScale(2, RoundingMode.HALF_UP).toString();
	}
}