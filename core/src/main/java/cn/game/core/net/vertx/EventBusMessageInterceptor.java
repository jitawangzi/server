package cn.game.core.net.vertx;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

public class EventBusMessageInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(EventBusMessageInterceptor.class);
	private static final String HDR_TRACE_ID = "trace-id";
	private static final String HDR_ORIG_TIMEOUT = "orig-timeout";
	private static final String HDR_SENDER_THREAD = "sender-thread";
	private static final String HDR_SENDER_CTX = "sender-ctx";
	private static final String HDR_MSG_HASH = "msg-hash";
	private static final String HDR_MSG_CLASS = "msg-class";
	private static final String HDR_SEND_TIMESTAMP = "send-timestamp";
	private static final String HDR_SEND_CONFIRM = "send-confirm";

	// 记录回复消息的发送状态
	private static final ConcurrentHashMap<String, ReplyStatus> replyStatusMap = new ConcurrentHashMap<>();
	
	// 集群状态缓存
	private static volatile ClusterStatus lastClusterStatus = new ClusterStatus();
	private static Vertx vertxInstance;

	public static void register(Vertx vertx) {
		vertxInstance = vertx;
		// 初始化消息跟踪器
		MessageTracker.initialize(vertx);
		
		// 启动集群健康监控
		startClusterMonitoring(vertx);

		// Outbound 拦截器：记录发送消息并跟踪需要回复的消息
		vertx.eventBus().addOutboundInterceptor(dc -> {
			try {
				Message<?> message = dc.message();
				Object actualBody = extractMessageBody(message);
				Class<?> bodyClass = (actualBody != null) ? actualBody.getClass() : null;
				String address = message.address();

				String traceId = message.headers().get(HDR_TRACE_ID); 
				if (traceId == null) {
					traceId = java.util.UUID.randomUUID().toString();
					message.headers().add(HDR_TRACE_ID, traceId);
				}
				String traceIdFinal = traceId;
				// 添加发送时间戳
				message.headers().add(HDR_SEND_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
				
				// 由于无法访问 DeliveryOptions，这里用默认/透传的超时值
				String hdrTimeout = message.headers() != null ? message.headers().get("orig-timeout") : null;
				long timeout = 30000L; // 默认 30s
				if (hdrTimeout != null) {
					try {
						timeout = Long.parseLong(hdrTimeout);
					} catch (NumberFormatException ignore) {
					}
				} else {
					// 反射方式从 message 内部取 options（可能随版本变化失效）
					timeout = getMessageTimeout(message);
				}

				// 增强：对回复消息进行特殊处理
				if (address != null && address.startsWith("__vertx.reply.")) {
					// 记录回复状态
					ReplyStatus status = new ReplyStatus(address, traceId, System.currentTimeMillis());
					replyStatusMap.put(address, status);
					
					// 记录详细的发送上下文
					logger.info("准备发送回复消息: address={}, traceId={}, thread={}, context={}, eventLoop={}, 集群状态={}",
							address, traceId, Thread.currentThread().getName(), 
							Vertx.currentContext(), isOnEventLoopThread(), lastClusterStatus);
					
					// 设置验证定时器
					vertx.setTimer(100, id -> {
						ReplyStatus currentStatus = replyStatusMap.get(address);
						if (currentStatus != null) {
							currentStatus.setConfirmed(true);
							logger.info("回复消息发送确认(100ms后): address={}, traceId={}, confirmed={}", 
								address, traceIdFinal, currentStatus.isConfirmed());
						}
					});
					
					// 1秒后再次检查
					vertx.setTimer(1000, id -> {
						ReplyStatus finalStatus = replyStatusMap.remove(address);
						if (finalStatus != null && !finalStatus.isReceived()) {
							logger.error("警告：回复消息可能未被接收: address={}, traceId={}, 发送时间={}, 集群状态={}",
								address, traceIdFinal, formatTimestamp(finalStatus.getSendTime()), lastClusterStatus);
						}
					});
				}

				// 记录发送日志
				logger.info("发送消息到[{}] replyAddress[{}] traceId[{}] timeout[{}] messageClass[{}] messageBody[{}] headers[{}] sender[{}]",
						address, message.replyAddress(), traceId, timeout, bodyClass, limitBody(actualBody), message.headers(),
						extractMessageSender(message));

				// 跟踪需要回复的消息
				String replyAddress = message.replyAddress();
				if (replyAddress != null) {
					String bodyHash = safeBodyHash(actualBody);
					MessageTracker.trackMessage(replyAddress, new MessageInfo(address, message.headers(), actualBody,
							System.currentTimeMillis(), timeout, traceId, bodyClass == null ? null : bodyClass.getName(), bodyHash));
				}

				dc.next();
				
				// 发送后立即检查（仅对回复消息）
				if (address != null && address.startsWith("__vertx.reply.")) {
					logger.info("回复消息已通过拦截器: address={}, traceId={}", address, traceId);
				}
				
			} catch (Throwable t) {
				logger.error("Outbound 拦截器异常", t);
				try {
					dc.next();
				} catch (Throwable ignore) {
				}
			}
		});

		// Inbound 拦截器：记录接收消息并处理回复
		vertx.eventBus().addInboundInterceptor(event -> {
			try {
				Message<?> message = event.message();
				Object actualBody = extractMessageBody(message);
				Class<?> bodyClass = (actualBody != null) ? actualBody.getClass() : null;

				String traceId = message.headers() != null ? message.headers().get(HDR_TRACE_ID) : null;
				String origTimeout = message.headers() != null ? message.headers().get("orig-timeout") : null;
				String msgHash = message.headers() != null ? message.headers().get("msg-hash") : null;
				String sendTimestamp = message.headers() != null ? message.headers().get(HDR_SEND_TIMESTAMP) : null;

				// 计算传输延迟
				long transmissionDelay = -1;
				if (sendTimestamp != null) {
					try {
						long sendTime = Long.parseLong(sendTimestamp);
						transmissionDelay = System.currentTimeMillis() - sendTime;
					} catch (NumberFormatException ignore) {
					}
				}

				logger.info(
						"接收消息从[{}] replyAddress[{}] traceId[{}] origTimeout[{}] messageClass[{}] messageBody[{}] headers[{}] sender[{}] 传输延迟[{}ms]",
						message.address(), message.replyAddress(), traceId, origTimeout, bodyClass, limitBody(actualBody),
						message.headers(), extractMessageSender(message), transmissionDelay);

				String messageAddress = message.address();
				if (messageAddress != null && messageAddress.startsWith("__vertx.reply.")) {
					// 标记回复已被接收
					ReplyStatus status = replyStatusMap.get(messageAddress);
					if (status != null) {
						status.setReceived(true);
						logger.info("回复消息已被接收确认: address={}, traceId={}, 传输延迟={}ms", 
							messageAddress, traceId, transmissionDelay);
					}
					
					MessageInfo trackedInfo = MessageTracker.handleReply(messageAddress, traceId, msgHash);
					if (trackedInfo != null) {
						logger.debug("收到回复，取消超时跟踪: replyAddress={} traceId={} reqAddr={}", messageAddress, traceId,
								trackedInfo.getAddress());
					} else {
						logger.warn("收到回复，但未匹配到已跟踪请求: replyAddress={} traceId={} 可能是超时后到达的回复", 
							messageAddress, traceId);
					}
				}
			} catch (Throwable t) {
				logger.error("Inbound 拦截器异常", t);
			} finally {
				try {
					event.next();
				} catch (Throwable ignore) {
				}
			}
		});
		
		// 启动诊断定时器
		startDiagnostics(vertx);
	}

	// 启动集群监控
	private static void startClusterMonitoring(Vertx vertx) {
		vertx.setPeriodic(10000, id -> {
			try {
				updateClusterStatus(vertx);
			} catch (Exception e) {
				logger.error("更新集群状态失败", e);
			}
		});
	}
	
	// 更新集群状态
	private static void updateClusterStatus(Vertx vertx) {
		try {
			if (vertx instanceof VertxInternal) {
				ClusterManager cm = ((VertxInternal) vertx).clusterManager();
				if (cm != null) {
					ClusterStatus status = new ClusterStatus();
					status.setActive(cm.isActive());
					status.setNodeCount(cm.getNodes().size());
					status.setNodes(cm.getNodes());
					
					// 检查 ZooKeeper 特定状态
					if (cm instanceof ZookeeperClusterManager) {
						ZookeeperClusterManager zkCm = (ZookeeperClusterManager) cm;
						// 可以通过反射获取更多状态信息
						status.setClusterManagerType("ZooKeeper");
					}
					
					lastClusterStatus = status;
					
					// 如果集群状态异常，记录警告,节点数至少要有3个
					if (!status.isActive() || status.getNodeCount() < 3) {
						logger.warn("集群状态异常: {}", status);
					}else {
						logger.info("集群当前状态: {}", status);
					}
				}
			}
		} catch (Exception e) {
			logger.error("获取集群状态失败", e);
		}
	}
	
	// 启动诊断功能
	private static void startDiagnostics(Vertx vertx) {
		// 每30秒输出一次诊断信息
		vertx.setPeriodic(30000, id -> {
			int activeMessages = MessageTracker.getActiveMessageCount();
			int activeTimers = MessageTracker.getActiveTimerCount();
			int pendingReplies = replyStatusMap.size();
			
			if (activeMessages > 0 || activeTimers > 0 || pendingReplies > 0) {
				logger.info("EventBus 诊断信息: 活跃消息={}, 活跃定时器={}, 待确认回复={}, 集群状态={}",
					activeMessages, activeTimers, pendingReplies, lastClusterStatus);
			}
			
			// 清理过期的回复状态
			long now = System.currentTimeMillis();
			replyStatusMap.entrySet().removeIf(entry -> 
				now - entry.getValue().getSendTime() > 60000); // 60秒后清理
		});
	}

	private static boolean isOnEventLoopThread() {
		// 尽量不依赖内部类；简单以线程名判断
		return Thread.currentThread().getName().contains("vert.x-eventloop");
	}

	private static String headerSafe(MultiMap headers, String key) {
		try {
			return headers == null ? null : headers.get(key);
		} catch (Throwable ignore) {
			return null;
		}
	}

	private static Object extractMessageBody(Message<?> message) {
		try {
			// 优先获取 body
			Object body = message.body();
			if (body != null) {
				return body;
			}

			// 尝试通过反射获取 sentBody
			Class<?> messageClass = message.getClass();
			Field sentBodyField = null;

			// 遍历类层次查找 sentBody 字段
			while (messageClass != null && sentBodyField == null) {
				try {
					sentBodyField = messageClass.getDeclaredField("sentBody");
				} catch (NoSuchFieldException e) {
					messageClass = messageClass.getSuperclass();
				}
			}

			if (sentBodyField != null) {
				sentBodyField.setAccessible(true);
				return sentBodyField.get(message);
			}

			return null;
		} catch (Exception e) {
			logger.debug("无法提取消息体: {}", e.toString());
			return null;
		}
	}

	private static Object extractMessageSender(Message<?> message) {
		try {
			// 尝试通过反射获取
			Class<?> messageClass = message.getClass();
			Field senderField = null;

			// 遍历类层次查找 sender 字段
			while (messageClass != null && senderField == null) {
				try {
					senderField = messageClass.getDeclaredField("sender");
				} catch (NoSuchFieldException e) {
					messageClass = messageClass.getSuperclass();
				}
			}

			if (senderField != null) {
				senderField.setAccessible(true);
				return senderField.get(message);
			}

			return null;
		} catch (Exception e) {
			logger.debug("无法提取消息sender: {}", e.toString());
			return null;
		}
	}

	private static long getMessageTimeout(Message<?> message) {
		try {
			// 尝试获取 DeliveryOptions
			Field optionsField = message.getClass().getDeclaredField("options");
			optionsField.setAccessible(true);
			DeliveryOptions options = (DeliveryOptions) optionsField.get(message);
			return options != null ? options.getSendTimeout() : 30000L;
		} catch (Exception e) {
			return 30000L; // 默认30秒超时
		}
	}

	private static String limitBody(Object body) {
		if (body == null)
			return "null";
		String s = String.valueOf(body);
		if (s.length() > 1024) {
			return s.substring(0, 1024) + "...(truncated)";
		}
		return s;
	}

	private static String safeBodyHash(Object body) {
		try {
			if (body == null)
				return null;
			String s = String.valueOf(body);
			byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(bytes);
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Throwable t) {
			return null;
		}
	}

	private static String snapshotHeaders(MultiMap msgHeaders, MultiMap optionsHeaders) {
		try {
			return String.format("msgHeaders=%s, optionsHeaders=%s", String.valueOf(msgHeaders), String.valueOf(optionsHeaders));
		} catch (Throwable ignore) {
			return "headers[unavailable]";
		}
	}
	
	private static String formatTimestamp(long timestamp) {
		return Instant.ofEpochMilli(timestamp)
				.atZone(ZoneId.systemDefault())
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
	}
	
	// 内部类：回复状态
	static class ReplyStatus {
		private final String address;
		private final String traceId;
		private final long sendTime;
		private volatile boolean confirmed = false;
		private volatile boolean received = false;
		
		public ReplyStatus(String address, String traceId, long sendTime) {
			this.address = address;
			this.traceId = traceId;
			this.sendTime = sendTime;
		}
		
		// getter/setter
		public boolean isConfirmed() { return confirmed; }
		public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
		public boolean isReceived() { return received; }
		public void setReceived(boolean received) { this.received = received; }
		public long getSendTime() { return sendTime; }
	}
	
	// 内部类：集群状态
	static class ClusterStatus {
		private boolean active = false;
		private int nodeCount = 0;
		private List<String> nodes = new ArrayList<>();
		private String clusterManagerType = "Unknown";

		// getter/setter
		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public int getNodeCount() {
			return nodeCount;
		}

		public void setNodeCount(int nodeCount) {
			this.nodeCount = nodeCount;
		}

		public List<String> getNodes() {
			return nodes;
		}

		public void setNodes(List<String> nodes) {
			this.nodes = nodes;
		}

		public String getClusterManagerType() {
			return clusterManagerType;
		}

		public void setClusterManagerType(String type) {
			this.clusterManagerType = type;
		}

		@Override
		public String toString() {
			return String.format("ClusterStatus[active=%s, nodes=%d, type=%s, nodeList=%s]", active, nodeCount, clusterManagerType, nodes);
		}
	}
}

/**
 * 消息信息类
 */
class MessageInfo {
	private final String address;
	private final MultiMap headers;
	private final Object body;
	private final long timestamp;
	private final long timeout;
	private final String traceId;
	private final String bodyClass;
	private final String bodyHash;

	public MessageInfo(String address, MultiMap headers, Object body, long timestamp, long timeout, String traceId, String bodyClass,
			String bodyHash) {
		this.address = address;
		this.headers = headers;
		this.body = body;
		this.timestamp = timestamp;
		this.timeout = timeout;
		this.traceId = traceId;
		this.bodyClass = bodyClass;
		this.bodyHash = bodyHash;
	}

	public String getAddress() {
		return address;
	}

	public Object getBody() {
		return body;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getTimeout() {
		return timeout;
	}

	public MultiMap getHeaders() {
		return headers;
	}

	public String getTraceId() {
		return traceId;
	}

	public String getBodyClass() {
		return bodyClass;
	}

	public String getBodyHash() {
		return bodyHash;
	}
}

/**
 * 消息跟踪器 - 使用 Vertx 内置定时器机制
 */
class MessageTracker {
	private static final Logger logger = LoggerFactory.getLogger(MessageTracker.class);
	private static final ConcurrentHashMap<String, MessageInfo> messageMap = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Long> timerMap = new ConcurrentHashMap<>();

	private static Vertx vertx;
	private static volatile boolean initialized = false;

	public static void initialize(Vertx vertxInstance) {
		if (!initialized) {
			synchronized (MessageTracker.class) {
				if (!initialized) {
					vertx = vertxInstance;
					startPeriodicCleanup();
					initialized = true;
				}
			}
		}
	}

	public static void trackMessage(String replyAddress, MessageInfo messageInfo) {
		if (!initialized) {
			logger.warn("MessageTracker 未初始化，无法跟踪消息");
			return;
		}
		// 先存消息，再设置定时器（确保 timerMap 与 messageMap 一致）
		messageMap.put(replyAddress, messageInfo);
		scheduleTimeoutCheck(replyAddress, messageInfo.getTimeout());
		if (logger.isDebugEnabled()) {
			logger.debug("开始跟踪消息: reqAddr={} -> replyAddr={} traceId={} 超时:{}ms", messageInfo.getAddress(), replyAddress,
					messageInfo.getTraceId(), messageInfo.getTimeout());
		}
	}

	// 增加 traceId/bodyHash 用于匹配与日志
	public static MessageInfo handleReply(String replyAddress, String traceId, String bodyHash) {
		// 取消定时器
		cancelExistingTimer(replyAddress);
		// 移除并返回消息信息
		MessageInfo messageInfo = messageMap.remove(replyAddress);
		if (messageInfo != null) {
			long responseTime = System.currentTimeMillis() - messageInfo.getTimestamp();
			logger.info("消息回复成功: replyAddr={} traceId={} reqAddr={} 耗时={}ms bodyClass={} reqHash={} rspHash={}", replyAddress,
					safe(traceId), messageInfo.getAddress(), responseTime, messageInfo.getBodyClass(), messageInfo.getBodyHash(),
					safe(bodyHash));
		} else {
			logger.warn("收到回复但未找到跟踪项: replyAddr={} traceId={}", replyAddress, safe(traceId));
		}
		return messageInfo;
	}

	private static String safe(String v) {
		return v == null ? "null" : v;
	}

	private static void cancelExistingTimer(String replyAddress) {
		Long timerId = timerMap.remove(replyAddress);
		if (timerId != null) {
			try {
				vertx.cancelTimer(timerId);
				if (logger.isDebugEnabled())
					logger.debug("取消定时器: {} -> {}", replyAddress, timerId);
			} catch (Throwable t) {
				logger.warn("取消定时器异常: replyAddr={} timerId={} err={}", replyAddress, timerId, t.toString());
			}
		}
	}

	private static void scheduleTimeoutCheck(String replyAddress, long timeout) {
		long timerId = vertx.setTimer(timeout, id -> {
			// 只在消息仍存在时报告超时
			MessageInfo timeoutInfo = messageMap.remove(replyAddress);
			timerMap.remove(replyAddress);

			if (timeoutInfo != null) {
				String sendTime = formatTimestamp(timeoutInfo.getTimestamp());
				logger.error("消息超时 {}ms. replyAddr: {}, 目标地址: {}, traceId: {}, 原始头信息: {}, 消息内容: {}, 发送时间: {}", timeoutInfo.getTimeout(),
						replyAddress, timeoutInfo.getAddress(), timeoutInfo.getTraceId(), timeoutInfo.getHeaders(),
						limitBody(timeoutInfo.getBody()), sendTime);
			}
		});
		timerMap.put(replyAddress, timerId);
		if (logger.isDebugEnabled())
			logger.debug("设置超时定时器: {} -> {}, 超时: {}ms", replyAddress, timerId, timeout);
	}

	private static void startPeriodicCleanup() {
		// 每分钟执行一次清理任务
		vertx.setPeriodic(60000, id -> {
			long now = System.currentTimeMillis();
			AtomicInteger cleanedCount = new AtomicInteger(0);

			// 使用迭代器安全地清理过期消息
			Iterator<Map.Entry<String, MessageInfo>> it = messageMap.entrySet().iterator();
			List<MessageInfo> expiredMessages = new java.util.ArrayList<>();
			while (it.hasNext()) {
				Map.Entry<String, MessageInfo> entry = it.next();
				MessageInfo info = entry.getValue();
				if (now - info.getTimestamp() > info.getTimeout() + 5000) { // 额外5秒缓冲
					expiredMessages.add(info); 
					it.remove();
					cancelExistingTimer(entry.getKey());
					cleanedCount.incrementAndGet();
				}
			}

			int cc = cleanedCount.get();
			if (cc > 0) {
				logger.warn("定期清理: 清理了 {} 个过期消息", cc);
				logger.warn("过期消息详情:" + expiredMessages.stream()
						.map(info -> String.format("reqAddr=%s, traceId=%s, bodyClass=%s, bodyHash=%s, sentAt=%s, timeout=%dms, headers=%s",
								info.getAddress(), info.getTraceId(), info.getBodyClass(), info.getBodyHash(),
								formatTimestamp(info.getTimestamp()), info.getTimeout(), info.getHeaders()))
						.reduce((a, b) -> a + "\n" + b)
						.orElse("无"));
			}

			// 记录当前跟踪状态
			int activeMessages = messageMap.size();
			int activeTimers = timerMap.size();
			if (activeMessages > 0 || activeTimers > 0) {
				logger.debug("当前跟踪状态: {} 个活跃消息, {} 个活跃定时器", activeMessages, activeTimers);
			}
		});
	}

	private static String formatTimestamp(long timestamp) {
		return Instant.ofEpochMilli(timestamp)
				.atZone(ZoneId.systemDefault())
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
	}

	// 提供状态查询方法（用于监控和调试）
	public static int getActiveMessageCount() {
		return messageMap.size();
	}

	public static int getActiveTimerCount() {
		return timerMap.size();
	}

	private static String limitBody(Object body) {
		if (body == null)
			return "null";
		String s = String.valueOf(body);
		if (s.length() > 1024) {
			return s.substring(0, 1024) + "...(truncated)";
		}
		return s;
	}
}