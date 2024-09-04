package cn.game.simulation.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;

public class MessageStatistics {
	// 消息发送记录： 消息序号，消息名，消息发送时间
	public Map<Integer, Pair<String, Long>> sendMessages = new ConcurrentHashMap<>();
	// 消息接收记录： 消息序号，消息名，消息接收时间
	public Map<Integer, Pair<String, Long>> recvMessages = new ConcurrentHashMap<>();

	public static void main(String[] args) {
		MessageStatistics stats = new MessageStatistics();

		// 示例数据（实际使用时请填入真实数据）
		stats.sendMessages.put(1, Pair.of("LoginBySessionC2S", 1000L));
		stats.recvMessages.put(1, Pair.of("LoginBySessionS2C", 1500L));
		stats.sendMessages.put(2, Pair.of("AuthResVersionC2S", 2000L));
		stats.recvMessages.put(2, Pair.of("AuthResVersionS2C", 2100L));
		stats.sendMessages.put(3, Pair.of("AuthUseRoleC2S", 3000L));
		stats.recvMessages.put(3, Pair.of("AuthUseRoleS2C", 3100L));
		stats.sendMessages.put(4, Pair.of("LoginBySessionC2S", 1000L));
		stats.recvMessages.put(4, Pair.of("LoginBySessionS2C", 1001L));

		stats.calculateStatistics();
	}

	public void calculateStatistics() {
		// Map to store the response times for each message name
		Map<String, List<Long>> responseTimes = new HashMap<>();

		// Match send and receive messages to calculate response times
		for (Map.Entry<Integer, Pair<String, Long>> sendEntry : sendMessages.entrySet()) {
			int msgId = sendEntry.getKey();
			String sendMsgName = sendEntry.getValue().getLeft();
			long sendTime = sendEntry.getValue().getRight();

			Pair<String, Long> recvEntry = recvMessages.get(msgId);
			if (recvEntry != null) {
				String recvMsgName = recvEntry.getLeft();
				long recvTime = recvEntry.getRight();
				String msgName = sendMsgName + "_" + recvMsgName;
				long responseTime = recvTime - sendTime;

				responseTimes.computeIfAbsent(msgName, k -> new ArrayList<>()).add(responseTime);
			}
		}

		// Calculate statistics for each message name
		for (Map.Entry<String, List<Long>> entry : responseTimes.entrySet()) {
			String msgName = entry.getKey();
			List<Long> times = entry.getValue();

			if (!times.isEmpty()) {
				long requestCount = sendMessages.entrySet().stream()
						.filter(e -> (e.getValue().getLeft() + "_" + recvMessages.get(e.getKey()).getLeft()).equals(msgName)).count();
				long responseCount = times.size();
				double responseRate = (responseCount / (double) requestCount) * 100;
				double minResponseTime = Collections.min(times);
				double maxResponseTime = Collections.max(times);
				double avgResponseTime = times.stream().mapToLong(Long::longValue).average().orElse(0.0);

				Collections.sort(times);
				double p50 = times.get((int) (times.size() * 0.50));
				double p75 = times.get((int) (times.size() * 0.75));
				double p90 = times.get((int) (times.size() * 0.90));

				System.out.printf("%s\t%d\t%d\t%.2f%%\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f%n", msgName, requestCount, responseCount, responseRate,
						minResponseTime, maxResponseTime, avgResponseTime, p50, p75, p90);
			}
		}
	}
}
