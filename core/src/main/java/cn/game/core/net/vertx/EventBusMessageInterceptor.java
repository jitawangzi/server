package cn.game.core.net.vertx;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

public class EventBusMessageInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(EventBusMessageInterceptor.class);
	private static final String HDR_TRACE_ID = "trace-id";
	private static final String HDR_ORIG_TIMEOUT = "orig-timeout";
	private static final String HDR_SENDER_THREAD = "sender-thread";
	private static final String HDR_SENDER_CTX = "sender-ctx";
	private static final String HDR_MSG_HASH = "msg-hash";
	private static final String HDR_MSG_CLASS = "msg-class";

	public static void register(Vertx vertx) {
		// 初始化消息跟踪器
		MessageTracker.initialize(vertx);

		// Outbound 拦截器：记录发送消息并跟踪需要回复的消息
		vertx.eventBus().addOutboundInterceptor(dc -> {
			try {
				Message<?> message = dc.message();
				Object actualBody = extractMessageBody(message);
				Class<?> bodyClass = (actualBody != null) ? actualBody.getClass() : null;

				// 从已存在的 headers 里拿 trace-id（如果在业务 send 时透传），没有就生成一个仅用于日志
				String traceId = message.headers() != null ? message.headers().get("trace-id") : null;
				if (traceId == null) {
					traceId = java.util.UUID.randomUUID().toString();
				}

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

				// 记录发送日志
				logger.info("发送消息到[{}] replyAddress[{}] traceId[{}] timeout[{}] messageClass[{}] messageBody[{}] headers[{}] sender[{}]",
						message.address(), message.replyAddress(), traceId, timeout, bodyClass, limitBody(actualBody), message.headers(),
						extractMessageSender(message));

				// 跟踪需要回复的消息
				String replyAddress = message.replyAddress();
				if (replyAddress != null) {
					String bodyHash = safeBodyHash(actualBody);
					MessageTracker.trackMessage(replyAddress, new MessageInfo(message.address(), message.headers(), actualBody,
							System.currentTimeMillis(), timeout, traceId, bodyClass == null ? null : bodyClass.getName(), bodyHash));
				}

				dc.next();
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

				String traceId = message.headers() != null ? message.headers().get("trace-id") : null;
				String origTimeout = message.headers() != null ? message.headers().get("orig-timeout") : null;
				String msgHash = message.headers() != null ? message.headers().get("msg-hash") : null;

				logger.info(
						"接收消息从[{}] replyAddress[{}] traceId[{}] origTimeout[{}] messageClass[{}] messageBody[{}] headers[{}] sender[{}]",
						message.address(), message.replyAddress(), traceId, origTimeout, bodyClass, limitBody(actualBody),
						message.headers(), extractMessageSender(message));

				String messageAddress = message.address();
				if (messageAddress != null && messageAddress.startsWith("__vertx.reply.")) {
					MessageInfo trackedInfo = MessageTracker.handleReply(messageAddress, traceId, msgHash);
					if (trackedInfo != null) {
						logger.debug("收到回复，取消超时跟踪: replyAddress={} traceId={} reqAddr={}", messageAddress, traceId,
								trackedInfo.getAddress());
					} else {
						logger.debug("收到回复，但未匹配到已跟踪请求: replyAddress={} traceId={}", messageAddress, traceId);
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
			while (it.hasNext()) {
				Map.Entry<String, MessageInfo> entry = it.next();
				MessageInfo info = entry.getValue();
				if (now - info.getTimestamp() > info.getTimeout() + 5000) { // 额外5秒缓冲
					it.remove();
					cancelExistingTimer(entry.getKey());
					cleanedCount.incrementAndGet();
				}
			}

			int cc = cleanedCount.get();
			if (cc > 0) {
				logger.warn("定期清理: 清理了 {} 个过期消息", cc);
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