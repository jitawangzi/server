package cn.game.games.net.game.gm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import cn.game.core.exception.LogicException;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.util.DateUtil;

/**
 * 非生产请求 Trace 录制器 (DC-13, BR-12, EX-09, AC-11, DR-07)
 * 维护有界环形缓冲、请求期资源变动流水及 Log4j2 ERROR/异常双通道捕获。
 * 生产环境下硬阻断并快速短路，零内存分配与零锁竞争。
 *
 * @author shuyongqiang
 */
public class RequestTraceService {

	private static final RequestTraceService INSTANCE = new RequestTraceService();

	public static RequestTraceService getInstance() {
		return INSTANCE;
	}

	private volatile boolean enabled = false;
	private volatile long filterPlayerId = 0L;
	private final AtomicInteger maxRecords = new AtomicInteger(500);
	private final AtomicLong totalRecorded = new AtomicLong(0L);
	private final ConcurrentLinkedDeque<TraceRecord> buffer = new ConcurrentLinkedDeque<>();
	private final ThreadLocal<TraceBuilder> currentBuilder = new ThreadLocal<>();
	private final AtomicBoolean appenderRegistered = new AtomicBoolean(false);

	private RequestTraceService() {}

	/**
	 * 判断当前玩家或请求是否应当录制。
	 * 生产环境下恒返回 false 快速短路。
	 */
	public boolean isRecording(long playerId) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			return false;
		}
		if (!enabled) {
			return false;
		}
		long target = filterPlayerId;
		return target == 0L || target == playerId;
	}

	/**
	 * 开启 Trace 录制
	 *
	 * @param playerId 目标玩家ID (0表示全部玩家)
	 * @param maxEntries 环形缓冲区最大容量 (10 ~ 2000，默认500)
	 * @param clearExisting 是否清空已有缓冲区
	 */
	public void start(long playerId, int maxEntries, boolean clearExisting) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID, "生产环境禁止开启Trace录制");
		}
		if (clearExisting) {
			buffer.clear();
			totalRecorded.set(0L);
		}
		if (maxEntries > 0) {
			int clamped = Math.min(2000, Math.max(10, maxEntries));
			maxRecords.set(clamped);
		}
		this.filterPlayerId = playerId;
		ensureLogAppenderAttached();
		this.enabled = true;
	}

	/**
	 * 停止 Trace 录制 (保留当前缓冲区供检索)
	 */
	public void stop() {
		this.enabled = false;
	}

	/**
	 * 查询录制器状态
	 */
	public Map<String, Object> status() {
		Map<String, Object> res = new LinkedHashMap<>();
		res.put("enabled", enabled);
		res.put("filterPlayerId", filterPlayerId);
		res.put("maxRecords", maxRecords.get());
		res.put("bufferedCount", buffer.size());
		res.put("totalRecorded", totalRecorded.get());
		return res;
	}

	/**
	 * 清空录制缓冲区
	 */
	public Map<String, Object> clear() {
		int count = buffer.size();
		buffer.clear();
		totalRecorded.set(0L);
		Map<String, Object> res = new LinkedHashMap<>();
		res.put("clearedCount", count);
		return res;
	}

	/**
	 * 多维条件倒序检索录制记录
	 */
	public List<TraceRecord> query(long playerId, int msgId, String msgName, int seq, String traceId,
			boolean onlyErrors, int limit) {
		if (limit <= 0) {
			limit = 50;
		}
		if (limit > 200) {
			limit = 200;
		}

		List<TraceRecord> result = new ArrayList<>();
		// Deque 逆序遍历 (最新的排在前面)
		Iterator<TraceRecord> it = buffer.descendingIterator();
		while (it.hasNext() && result.size() < limit) {
			TraceRecord rec = it.next();
			if (playerId > 0 && rec.getPlayerId() != playerId) {
				continue;
			}
			if (msgId > 0 && rec.getMsgId() != msgId) {
				continue;
			}
			if (msgName != null && !msgName.isBlank() && !rec.getMsgName().equalsIgnoreCase(msgName)) {
				continue;
			}
			if (seq >= 0 && rec.getSeq() != seq) {
				continue;
			}
			if (traceId != null && !traceId.isBlank() && !rec.getTraceId().equals(traceId)) {
				continue;
			}
			if (onlyErrors && !rec.hasErrors()) {
				continue;
			}
			result.add(rec);
		}
		return result;
	}

	/**
	 * 开始记录一次请求
	 */
	public TraceBuilder beginTrace(long playerId, int msgId, String msgName, int seq) {
		if (!isRecording(playerId)) {
			return null;
		}
		TraceBuilder builder = new TraceBuilder(playerId, msgId, msgName, seq);
		currentBuilder.set(builder);
		return builder;
	}

	/**
	 * 结束一次请求记录并推入环形缓冲
	 */
	public void endTrace(TraceBuilder builder, int errorCode) {
		try {
			if (builder == null) {
				return;
			}
			TraceRecord record = builder.build(errorCode);
			buffer.addLast(record);
			totalRecorded.incrementAndGet();

			int cap = maxRecords.get();
			while (buffer.size() > cap) {
				buffer.pollFirst();
			}
		} finally {
			currentBuilder.remove();
		}
	}

	public TraceBuilder getCurrentBuilder() {
		return currentBuilder.get();
	}

	/**
	 * 非生产环境开启单请求隔离强制录制 (DC-05, BR-06, EX-06)
	 * 不修改全局 enabled 开关，确保 LogAppender 已挂载，并将 TraceBuilder 绑定至 ThreadLocal。
	 *
	 * @param playerId 目标玩家ID
	 * @param msgId    协议ID
	 * @param msgName  协议名称
	 * @param seq      请求序号
	 * @return 构造器，生产模式下返回 null
	 */
	public TraceBuilder beginForcedTrace(long playerId, int msgId, String msgName, int seq) {
		if (GmSecurityPolicy.currentRunMode().isProduction()) {
			return null;
		}
		ensureLogAppenderAttached();
		TraceBuilder builder = new TraceBuilder(playerId, msgId, msgName, seq);
		currentBuilder.set(builder);
		return builder;
	}

	/**
	 * 结束单请求隔离强制录制并返回 TraceRecord
	 *
	 * @param builder   由 beginForcedTrace 创建的构造器
	 * @param errorCode 最终错误码
	 * @return 构建的 TraceRecord，若原全局正开启录制则顺带推入环形缓冲
	 */
	public TraceRecord endForcedTrace(TraceBuilder builder, int errorCode) {
		try {
			if (builder == null) {
				return null;
			}
			TraceRecord record = builder.build(errorCode);
			if (isRecording(builder.playerId)) {
				buffer.addLast(record);
				totalRecorded.incrementAndGet();
				int cap = maxRecords.get();
				while (buffer.size() > cap) {
					buffer.pollFirst();
				}
			}
			return record;
		} finally {
			currentBuilder.remove();
		}
	}

	/**
	 * 记录资源增减变动流水
	 */
	public static void recordResourceEvent(long playerId, int id, long count, boolean isAdd, String opType) {
		TraceBuilder b = INSTANCE.currentBuilder.get();
		if (b != null && (b.playerId == 0 || b.playerId == playerId)) {
			b.recordResource(id, count, isAdd, opType);
		}
	}

	/**
	 * 记录 Log4j2 ERROR 日志事件
	 */
	public static void recordErrorEvent(String loggerName, String message, String exceptionClass) {
		TraceBuilder b = INSTANCE.currentBuilder.get();
		if (b != null) {
			b.recordError(loggerName, message, exceptionClass, DateUtil.currentTimeMillis());
		}
	}

	/**
	 * 记录捕获的异常
	 */
	public static void recordException(Throwable t) {
		TraceBuilder b = INSTANCE.currentBuilder.get();
		if (b != null && t != null) {
			b.recordException(t);
		}
	}

	private void ensureLogAppenderAttached() {
		if (appenderRegistered.compareAndSet(false, true)) {
			try {
				org.apache.logging.log4j.spi.LoggerContext ctx = LogManager.getContext(false);
				if (ctx instanceof LoggerContext loggerContext) {
					Configuration config = loggerContext.getConfiguration();
					TraceLog4jAppender appender = new TraceLog4jAppender();
					appender.start();
					config.addAppender(appender);
					config.getRootLogger().addAppender(appender, Level.ERROR, null);
					loggerContext.updateLoggers();
				}
			} catch (Throwable t) {
				org.slf4j.LoggerFactory.getLogger(RequestTraceService.class).warn("动态挂载 Log4j2 TraceAppender 异常 (可能非生产或轻量测试环境)", t);
			}
		}
	}

	private static class TraceLog4jAppender extends AbstractAppender {
		protected TraceLog4jAppender() {
			super("TraceErrorAppender", null, PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY);
		}

		@Override
		public void append(LogEvent event) {
			if (event == null) {
				return;
			}
			Level level = event.getLevel();
			if (level == Level.ERROR || level == Level.FATAL) {
				TraceBuilder builder = INSTANCE.currentBuilder.get();
				if (builder != null) {
					String loggerName = event.getLoggerName();
					String msg = event.getMessage() != null ? event.getMessage().getFormattedMessage() : "";
					String thrownClass = event.getThrown() != null ? event.getThrown().getClass().getName() : null;
					builder.recordError(loggerName, msg, thrownClass, event.getTimeMillis());
				}
			}
		}
	}

	public static class TraceBuilder {
		final String traceId;
		final long playerId;
		final int msgId;
		final String msgName;
		final int seq;
		final long startTimeMs;
		final long startNanos;
		final List<ResourceEvent> resourceEvents = new CopyOnWriteArrayList<>();
		final List<ErrorEvent> errorEvents = new CopyOnWriteArrayList<>();

		TraceBuilder(long playerId, int msgId, String msgName, int seq) {
			this.traceId = UUID.randomUUID().toString();
			this.playerId = playerId;
			this.msgId = msgId;
			this.msgName = msgName != null ? msgName : "";
			this.seq = seq;
			this.startTimeMs = DateUtil.currentTimeMillis();
			this.startNanos = System.nanoTime();
		}

		public void recordResource(int id, long count, boolean isAdd, String opType) {
			resourceEvents.add(new ResourceEvent(id, count, isAdd, opType, DateUtil.currentTimeMillis()));
		}

		public void recordError(String loggerName, String message, String exceptionClass, long timestampMs) {
			errorEvents.add(new ErrorEvent(loggerName, message, exceptionClass, timestampMs));
		}

		public void recordException(Throwable t) {
			if (t != null) {
				String msg = t.getMessage() != null ? t.getMessage() : t.toString();
				errorEvents.add(new ErrorEvent(t.getClass().getSimpleName(), msg, t.getClass().getName(), DateUtil.currentTimeMillis()));
			}
		}

		TraceRecord build(int errorCode) {
			long elapsedMs = Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);
			return new TraceRecord(traceId, playerId, msgId, msgName, seq, startTimeMs, elapsedMs, errorCode,
					new ArrayList<>(resourceEvents), new ArrayList<>(errorEvents));
		}
	}

	public static class TraceRecord implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String traceId;
		private final long playerId;
		private final int msgId;
		private final String msgName;
		private final int seq;
		private final long startTimeMs;
		private final long elapsedMs;
		private final int errorCode;
		private final List<ResourceEvent> resourceEvents;
		private final List<ErrorEvent> errorEvents;

		public TraceRecord(String traceId, long playerId, int msgId, String msgName, int seq, long startTimeMs,
				long elapsedMs, int errorCode, List<ResourceEvent> resourceEvents, List<ErrorEvent> errorEvents) {
			this.traceId = traceId;
			this.playerId = playerId;
			this.msgId = msgId;
			this.msgName = msgName;
			this.seq = seq;
			this.startTimeMs = startTimeMs;
			this.elapsedMs = elapsedMs;
			this.errorCode = errorCode;
			this.resourceEvents = resourceEvents != null ? resourceEvents : Collections.emptyList();
			this.errorEvents = errorEvents != null ? errorEvents : Collections.emptyList();
		}

		public String getTraceId() { return traceId; }
		public long getPlayerId() { return playerId; }
		public int getMsgId() { return msgId; }
		public String getMsgName() { return msgName; }
		public int getSeq() { return seq; }
		public long getStartTimeMs() { return startTimeMs; }
		public long getElapsedMs() { return elapsedMs; }
		public int getErrorCode() { return errorCode; }
		public List<ResourceEvent> getResourceEvents() { return resourceEvents; }
		public List<ResourceEvent> getResourceDeltas() { return resourceEvents; }
		public List<ErrorEvent> getErrorEvents() { return errorEvents; }
		public List<ErrorEvent> getErrors() { return errorEvents; }
		public boolean hasErrors() {
			return (errorCode != 0 && errorCode != ErrorMsgEnum.ok.getId()) || !errorEvents.isEmpty();
		}
	}

	public static class ResourceEvent implements Serializable {
		private static final long serialVersionUID = 1L;

		private final int id;
		private final long count;
		private final boolean isAdd;
		private final String opType;
		private final long timestampMs;

		public ResourceEvent(int id, long count, boolean isAdd, String opType, long timestampMs) {
			this.id = id;
			this.count = count;
			this.isAdd = isAdd;
			this.opType = opType != null ? opType : "None";
			this.timestampMs = timestampMs;
		}

		public int getId() { return id; }
		public long getCount() { return count; }
		public boolean isAdd() { return isAdd; }
		public String getOpType() { return opType; }
		public long getTimestampMs() { return timestampMs; }
	}

	public static class ErrorEvent implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String loggerName;
		private final String message;
		private final String exceptionClass;
		private final long timestampMs;

		public ErrorEvent(String loggerName, String message, String exceptionClass, long timestampMs) {
			this.loggerName = loggerName != null ? loggerName : "";
			this.message = message != null ? message : "";
			this.exceptionClass = exceptionClass != null ? exceptionClass : "";
			this.timestampMs = timestampMs;
		}

		public String getLoggerName() { return loggerName; }
		public String getMessage() { return message; }
		public String getExceptionClass() { return exceptionClass; }
		public long getTimestampMs() { return timestampMs; }
	}
}
