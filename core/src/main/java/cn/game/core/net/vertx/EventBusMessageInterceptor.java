package cn.game.core.net.vertx;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

public class EventBusMessageInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(EventBusMessageInterceptor.class);

	public static void register(Vertx vertx) {
		// 初始化消息跟踪器
		MessageTracker.initialize(vertx);

		// Outbound 拦截器：记录发送消息并跟踪需要回复的消息
		vertx.eventBus().addOutboundInterceptor(dc -> {
			Message<?> message = dc.message();
			Object actualBody = extractMessageBody(message);
			Class<?> bodyClass = (actualBody != null) ? actualBody.getClass() : null;

			// 记录发送日志
			logger.info("发送消息到[{}]replyAddress[{}]messageClass[{}]messageBody[{}]sender[{}]", message.address(), message.replyAddress(),
					bodyClass, actualBody, extractMessageSender(message));

			// 跟踪需要回复的消息
			String replyAddress = message.replyAddress();
			if (replyAddress != null) {
				long timeout = getMessageTimeout(message);
				MessageTracker.trackMessage(replyAddress,
						new MessageInfo(message.address(), message.headers(), actualBody, System.currentTimeMillis(), timeout));
			}

			dc.next();
		});

		// Inbound 拦截器：记录接收消息并处理回复
		vertx.eventBus().addInboundInterceptor(event -> {
			Message<?> message = event.message();
			Object actualBody = extractMessageBody(message);
			Class<?> bodyClass = (actualBody != null) ? actualBody.getClass() : null;
			// 记录接收日志
			logger.info("接收消息从[{}]replyAddress[{}]messageClass[{}]messageBody[{}]sender[{}]", message.address(), message.replyAddress(),
					bodyClass, actualBody, extractMessageSender(message));

			// 处理回复消息
			String messageAddress = message.address();
			if (messageAddress != null && messageAddress.startsWith("__vertx.reply.")) {
				MessageInfo trackedInfo = MessageTracker.handleReply(messageAddress);
				if (trackedInfo != null) {
					logger.debug("收到回复，取消超时跟踪: {}", messageAddress);
				}
			}

			event.next();
		});
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
			logger.debug("无法提取消息体: {}", e.getMessage());
			return null;
		}
	}

	private static Object extractMessageSender(Message<?> message) {
		try {
			// 尝试通过反射获取
			Class<?> messageClass = message.getClass();
			Field sentBodyField = null;

			// 遍历类层次查找 sender 字段
			while (messageClass != null && sentBodyField == null) {
				try {
					sentBodyField = messageClass.getDeclaredField("sender");
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
			logger.debug("无法提取消息sender: {}", e.getMessage());
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

	public MessageInfo(String address, MultiMap headers, Object body, long timestamp, long timeout) {
		this.address = address;
		this.headers = headers;
		this.body = body;
		this.timestamp = timestamp;
		this.timeout = timeout;
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
}

/**
 * 消息跟踪器 - 使用 Vertx 内置定时器机制
 */
class MessageTracker {
	private static final Logger logger = LoggerFactory.getLogger(MessageTracker.class);
	private static final ConcurrentHashMap<String, MessageInfo> messageMap = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Long> timerMap = new ConcurrentHashMap<>();
	private static final AtomicLong timerIdGenerator = new AtomicLong(0);

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

		// 取消已存在的定时器
		cancelExistingTimer(replyAddress);

		// 存储消息信息
		messageMap.put(replyAddress, messageInfo);

		// 设置新的超时定时器
		scheduleTimeoutCheck(replyAddress, messageInfo.getTimeout());

		logger.debug("开始跟踪消息: {} -> {}, 超时: {}ms", messageInfo.getAddress(), replyAddress, messageInfo.getTimeout());
	}

	public static MessageInfo handleReply(String replyAddress) {
		// 取消定时器
		cancelExistingTimer(replyAddress);

		// 移除并返回消息信息
		MessageInfo messageInfo = messageMap.remove(replyAddress);
		if (messageInfo != null) {
			long responseTime = System.currentTimeMillis() - messageInfo.getTimestamp();
			logger.debug("消息回复成功: {} 响应时间: {}ms", replyAddress, responseTime);
		}

		return messageInfo;
	}

	private static void cancelExistingTimer(String replyAddress) {
		Long timerId = timerMap.remove(replyAddress);
		if (timerId != null) {
			vertx.cancelTimer(timerId);
			logger.debug("取消现有定时器: {} -> {}", replyAddress, timerId);
		}
	}

	private static void scheduleTimeoutCheck(String replyAddress, long timeout) {
		long timerId = vertx.setTimer(timeout, id -> {
			// 检查消息是否仍然存在（可能已经被回复处理了）
			MessageInfo timeoutInfo = messageMap.remove(replyAddress);
			timerMap.remove(replyAddress);

			if (timeoutInfo != null) {
				String sendTime = formatTimestamp(timeoutInfo.getTimestamp());
				logger.error("消息超时 {}ms. 目标地址: {}, 原始头信息: {}, 消息内容: {}, 发送时间: {}", timeoutInfo.getTimeout(), timeoutInfo.getAddress(),
						timeoutInfo.getHeaders(), timeoutInfo.getBody(), sendTime);
			}
		});

		timerMap.put(replyAddress, timerId);
		logger.debug("设置超时定时器: {} -> {}, 超时: {}ms", replyAddress, timerId, timeout);
	}

	private static void startPeriodicCleanup() {
		// 每分钟执行一次清理任务
		vertx.setPeriodic(60000, id -> {
			long now = System.currentTimeMillis();
			int cleanedCount = 0;

			// 使用迭代器安全地清理过期消息
			messageMap.entrySet().removeIf(entry -> {
				MessageInfo info = entry.getValue();
				if (now - info.getTimestamp() > info.getTimeout() + 5000) { // 额外5秒缓冲
					cancelExistingTimer(entry.getKey());
					return true;
				}
				return false;
			});

			if (cleanedCount > 0) {
				logger.debug("定期清理: 清理了 {} 个过期消息", cleanedCount);
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
}