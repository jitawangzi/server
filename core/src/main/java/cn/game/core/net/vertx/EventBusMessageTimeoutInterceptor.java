package cn.game.core.net.vertx;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

public class EventBusMessageTimeoutInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(EventBusMessageTimeoutInterceptor.class);

	public static void register(Vertx vertx) {
		vertx.eventBus().addOutboundInterceptor(dc -> {
			Message<?> message = dc.message();
			String replyAddress = message.replyAddress();
			if (replyAddress != null) {
				long timeout = getMessageTimeout(message);
				Object actualBody = extractMessageBody(message);

				MessageTracker.track(replyAddress, new MessageInfo(message.address(), actualBody, System.currentTimeMillis(), timeout));
			}
			dc.next();
		});
	}

	private static Object extractMessageBody(Message<?> message) {
		try {
			// 先尝试直接获取body
			Object body = message.body();
			if (body != null) {
				return body;
			}

			// 如果body为null，尝试从父类获取sentBody
			Field sentBodyField = message.getClass().getSuperclass().getDeclaredField("sentBody");
			sentBodyField.setAccessible(true);
			Object sentBody = sentBodyField.get(message);

			if (sentBody != null) {
				return sentBody;
			}

			// 如果都获取不到，返回null
			return null;
		} catch (Exception e) {
			logger.error("Failed to extract message body", e);
			return null;
		}
	}

	private static long getMessageTimeout(Message<?> message) {
		try {
			Field optionsField = message.getClass().getDeclaredField("options");
			optionsField.setAccessible(true);
			DeliveryOptions options = (DeliveryOptions) optionsField.get(message);
			return options != null ? options.getSendTimeout() : 30000;
		} catch (Exception e) {
			return 30000;
		}
	}
}

class MessageInfo {
	private final String address;
	private final Object body;
	private final long timestamp;
	private final long timeout;

	public MessageInfo(String address, Object body, long timestamp, long timeout) {
		this.address = address;
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

}

class MessageTracker {
	private static final Map<String, MessageInfo> messageMap = new ConcurrentHashMap<>();
	private static final Map<String, Timer> timerMap = new ConcurrentHashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(MessageTracker.class);

	public static void track(String replyAddress, MessageInfo messageInfo) {
		messageMap.put(replyAddress, messageInfo);
		scheduleTimeoutCheck(replyAddress);
	}

	private static void scheduleTimeoutCheck(String replyAddress) {
		// 取消已存在的定时器
		Timer existingTimer = timerMap.remove(replyAddress);
		if (existingTimer != null) {
			existingTimer.cancel();
		}

		MessageInfo info = messageMap.get(replyAddress);
		if (info != null) {
			Timer timer = new Timer(true);
			timerMap.put(replyAddress, timer);

			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					// 检查消息是否仍然存在
					MessageInfo timeoutInfo = messageMap.remove(replyAddress);
					if (timeoutInfo != null) {
						logger.error("Message timeout after {}ms. Remote address: {}, Original message: {}, Send time: {}",
								timeoutInfo.getTimeout(), timeoutInfo.getAddress(), timeoutInfo.getBody(),
								new Date(timeoutInfo.getTimestamp()));
					}
					timerMap.remove(replyAddress);
					timer.cancel();
				}
			}, info.getTimeout());
		}
	}

	public static MessageInfo getMessageInfo(String replyAddress) {
		// 取消对应的定时器
		Timer timer = timerMap.remove(replyAddress);
		if (timer != null) {
			timer.cancel();
		}
		return messageMap.remove(replyAddress);
	}

	// 定期清理过期的消息和定时器
	static {
		Timer cleanupTimer = new Timer(true);
		cleanupTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				// 清理过期的消息
				messageMap.entrySet().removeIf(entry -> {
					MessageInfo info = entry.getValue();
					if (now - info.getTimestamp() > info.getTimeout()) {
						Timer timer = timerMap.remove(entry.getKey());
						if (timer != null) {
							timer.cancel();
						}
						return true;
					}
					return false;
				});
			}
		}, 60000, 60000); // 每分钟清理一次
	}
}