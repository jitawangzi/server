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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.client.AbstractNetClient;

public class GlobalMessageStatistics {
	private static final Logger log = LoggerFactory.getLogger(GlobalMessageStatistics.class);
	private static final GlobalMessageStatistics instance = new GlobalMessageStatistics();

	public MultiKeyMap<Long, Pair<String, Long>> globalSendMessages = new MultiKeyMap<Long, Pair<String, Long>>();
	public MultiKeyMap<Long, Pair<String, Long>> globalRecvMessages = new MultiKeyMap<Long, Pair<String, Long>>();
	// 全局消息发送记录： (客户端ID, 消息序号) => 消息名, 消息发送时间
//	public Map<String, Pair<String, Long>> globalSendMessages = new ConcurrentHashMap<>();
	// 全局消息接收记录： (客户端ID, 消息序号) => 消息名, 消息接收时间
//	public Map<String, Pair<String, Long>> globalRecvMessages = new ConcurrentHashMap<>();

	public static GlobalMessageStatistics getInstance() {
		return instance;
	}

	private void mergeAllClientData(Collection<? extends AbstractNetClient> clients) {
		for (AbstractNetClient client : clients) {
			client.sendMessages.forEach((k, v) -> {
				globalSendMessages.put(client.getPlayerId(), k.longValue(), v);
			});
			client.recvMessages.forEach((k, v) -> {
				globalRecvMessages.put(client.getPlayerId(), k.longValue(), v);
			});
		}
	}

//	private void mergeClientData(Map<String, Pair<String, Long>> sendMessages, Map<String, Pair<String, Long>> recvMessages) {
//		globalSendMessages.putAll(sendMessages);
//		globalRecvMessages.putAll(recvMessages);
//	}

	public void calculateStatisticsAndSaveResult(Collection<? extends AbstractNetClient> clients) throws IOException {
		Calendar c = Calendar.getInstance();
		int h = c.get(Calendar.HOUR_OF_DAY);
		int m = c.get(Calendar.MINUTE);
//		int s = c.get(Calendar.SECOND);

		String fileName = System.getProperty("botIdStart") + "_" + h + "-" + m;
		String dirPath = System.getProperty("user.dir") + "/statistics/";
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		String filePath = dirPath + fileName + ".csv";
		String slowFilePath = dirPath + h + "-" + m + "-slowMessage.csv";

		mergeAllClientData(clients);
		calculateStatistics(filePath, slowFilePath);
		// 清理下消息记录
		for (AbstractNetClient abstractNetClient : clients) {
			abstractNetClient.recvMessages.clear();
			abstractNetClient.sendMessages.clear();
		}
	}

	private void calculateStatistics(String outputFilePath, String slowFilePath) throws IOException {
		// 开发模式
		boolean pressureDev = Boolean.getBoolean("pressureDev");
		// 每个消息的所有响应时间
		Map<String, List<Double>> responseTimes = new TreeMap<>();
		// 每个消息的请求数量
		Map<String, Long> requestCountMap = new HashMap<>();

		// 每个人发的消息统计，
//		Map<Long, Integer>

		// 匹配发送接收消息，计算响应时间
		for (Entry<MultiKey<? extends Long>, Pair<String, Long>> sendEntry : globalSendMessages.entrySet()) {
			MultiKey<? extends Long> key = sendEntry.getKey();
			Long playerId = key.getKey(0);
			Long seq = key.getKey(1);
			String sendMsgName = sendEntry.getValue().getLeft();
			long sendTime = sendEntry.getValue().getRight();

			Pair<String, Long> recvEntry = globalRecvMessages.get(key);
			if (recvEntry != null) {
				long recvTime = recvEntry.getRight();
				double responseTime = Double.parseDouble(String.format("%.2f", (recvTime - sendTime) / 1_000_000.0));
				if (responseTime < 0) {
					log.error("响应时间小于0，playerId：{} seq ：{} 消息名：{}，发送时间：{}，接收时间：{}", playerId, seq, sendMsgName, sendTime, recvTime);
				}
				responseTimes.computeIfAbsent(sendMsgName, k -> new ArrayList<>()).add(responseTime);
			}
			requestCountMap.compute(sendMsgName, (k, v) -> v == null ? 1 : v + 1);
		}

		OutputStreamWriter writer = null;
		CSVPrinter printer = null;

		OutputStreamWriter writerSlow = null;
		CSVPrinter printerSlow = null;
		try {
			String[] headers = new String[] { "消息号", "请求数量", "响应数量", "响应率", "最小响应", "最大响应", "平均响应", "50%分位", "75%分位", "90%分位" };
			writer = new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8);
			// 写入UTF-8 BOM，避免Excel打开csv文件时乱码,注意CSVPrinter的初始化要在这个之后
			writer.write('\ufeff');
			printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers));

			if (pressureDev) {
				writerSlow = new OutputStreamWriter(new FileOutputStream(slowFilePath), StandardCharsets.UTF_8);
				writerSlow.write('\ufeff');
				printerSlow = new CSVPrinter(writerSlow, CSVFormat.DEFAULT.withHeader(headers));
			}
			// 每个消息的统计
			for (Map.Entry<String, List<Double>> entry : responseTimes.entrySet()) {
				String msgName = entry.getKey();
				List<Double> times = entry.getValue();

				if (!times.isEmpty()) {
					long requestCount = requestCountMap.get(msgName);
					long responseCount = times.size();
					double responseRate = (responseCount / (double) requestCount) * 100;
					double minResponseTime = Collections.min(times);
					double maxResponseTime = Collections.max(times);
					double avgResponseTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

					Collections.sort(times);
					double p50 = percentile(times, 50);
					double p75 = percentile(times, 75);
					double p90 = percentile(times, 90);

					printer.printRecord(msgName, requestCount, responseCount, String.format("%.2f%%", responseRate), minResponseTime, maxResponseTime,
							String.format("%.2f", avgResponseTime), p50, p75, p90);
					// 如果有没返回的，或者时间中位数大于10ms的，额外记录下来。
					if (writerSlow != null && (p50 > 10 || responseRate < 100)) {
						printerSlow.printRecord(msgName, requestCount, responseCount, String.format("%.2f%%", responseRate), minResponseTime, maxResponseTime,
								String.format("%.2f", avgResponseTime), p50, p75, p90, times.toString());
					}

				}
			}
//			globalSendMessages.clear();
//			globalRecvMessages.clear();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (writerSlow != null) {
				writerSlow.close();
			}
			if (printer != null) {
				printer.close();
			}
			if (printerSlow != null) {
				printerSlow.close();
			}
		}

	}

	private double percentile(List<Double> times, double percentile) {
		List<Double> sortedTimes = times.stream().sorted().collect(Collectors.toList());
		int n = sortedTimes.size();
		double index = (n - 1) * (percentile / 100.0);
		int lowerIndex = (int) Math.floor(index);
		int upperIndex = (int) Math.ceil(index);

		if (lowerIndex == upperIndex) {
			return sortedTimes.get(lowerIndex);
		}

		double lowerValue = sortedTimes.get(lowerIndex);
		double upperValue = sortedTimes.get(upperIndex);
		double ret = lowerValue + (upperValue - lowerValue) * (index - lowerIndex);
		return Double.parseDouble(formatDecimal(ret));
	}

	private String formatDecimal(double value) {
		BigDecimal bd = new BigDecimal(Double.toString(value));
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.toString();
	}
}
