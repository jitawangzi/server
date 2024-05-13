package cn.game.games.core.log;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 工具组件的日志由使用者设置，若不设置则使用控制台输出
 *
 * @author pangjiawei - [Created on 2018/1/26 18:31]
 */
public class EmbeddedLogger {

	private static final DateTimeFormatter formatterMill = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z");

	public interface LevelLogger {

		void log(final Object... objects);

	}

	public enum Level {
		trace, debug, info, warn, error, fatal, monitoring
	}

	private static final char DELIMITER = '|';

	private static final EmbeddedLogger.LevelLogger[] LEVEL_LOGGERS = new EmbeddedLogger.LevelLogger[EmbeddedLogger.Level.values().length];

	public static void setLevelLogger(final EmbeddedLogger.Level level, final EmbeddedLogger.LevelLogger levelLogger) {
		LEVEL_LOGGERS[level.ordinal()] = levelLogger;
	}

	private static void log(final EmbeddedLogger.Level level, final Object... objects) {
		if (LEVEL_LOGGERS[level.ordinal()] != null) {
			LEVEL_LOGGERS[level.ordinal()].log(objects);
		} else {
			// 如果没有设置的话,就使用这样的方式打印.
			System.err.println(String.format("NOT EmbeddedLogger FOR [%s], PLEASE SET IT.", level.name()));
			if (ArrayUtils.isNotEmpty(objects)) {
				System.err.println(StringUtils.join(objects, DELIMITER));
				for (Object object : objects) {
					if (object instanceof Throwable) {
						((Throwable) object).printStackTrace();
					}
				}
			}
		}
	}

	public static void trace(final Object... objects) {
		log(EmbeddedLogger.Level.trace, objects);
	}

	public static void debug(final Object... objects) {
		log(EmbeddedLogger.Level.debug, objects);
	}

	public static void info(final Object... objects) {
		log(EmbeddedLogger.Level.info, objects);
	}

	public static void warn(final Object... objects) {
		log(EmbeddedLogger.Level.warn, objects);
	}

	public static void error(final Object... objects) {
		log(EmbeddedLogger.Level.error, objects);
	}

	public static void fatal(final Object... objects) {
		log(EmbeddedLogger.Level.fatal, objects);
	}

	public static void monitoringInter(final Object... objects) {
		log(Level.monitoring, objects);
	}

	/**
	 * 监控日志
	 * @param nodeType 节点类型
	 * @param nodeId  节点id，没有或者还没分配填写0
	 * @param level 日志重要性级别
	 * @param module 错误分类 LOGIC NET MYSQL REDIS
	 * @param detail 详细信息
	 */
	public static void monitoring(String nodeType, int nodeId, org.apache.logging.log4j.Level level, String module, String detail) {
		Object[] infos = new Object[] { getCurrentMillTimeLogText(), nodeType, nodeId, level.toString(), module, detail };
		monitoringInter(infos);
	}

	/**
	 * 监控日志-超时监控
	 * @param nodeType 节点类型
	 * @param nodeId  节点id，没有或者还没分配填写0
	 * @param level 日志重要性级别
	 * @param module 错误分类 LOGIC NET MYSQL REDIS
	 * @param detail 详细信息
	 * @param start 开始时间
	 * @param waterMark 超过该毫秒数，则输出log
	 */
	public static void monitoringCost(String nodeType, int nodeId, org.apache.logging.log4j.Level level, String module, String detail, long start,
			long waterMark) {
		long cost = System.currentTimeMillis() - start;
		if (cost > waterMark) {

			Object[] infos = new Object[] { getCurrentMillTimeLogText(), nodeType, nodeId, level.toString(), module, detail + cost };
			monitoringInter(infos);
		}
	}

	/**
	 * 监控日志-超量监控
	 * @param nodeType 节点类型
	 * @param nodeId  节点id，没有或者还没分配填写0
	 * @param level 日志重要性级别
	 * @param module 错误分类 LOGIC NET MYSQL REDIS
	 * @param detail 详细信息
	 * @param count 当前数量
	 * @param waterMark 超过该数量，则输出log
	 */
	public static void monitoringCount(String nodeType, int nodeId, org.apache.logging.log4j.Level level, String module, String detail, int count,
			int waterMark) {
		if (count > waterMark) {

			Object[] infos = new Object[] { getCurrentMillTimeLogText(), nodeType, nodeId, level.toString(), module, detail + count };
			monitoringInter(infos);
		}
	}

	protected static String getCurrentMillTimeLogText() {
		return LocalDateTime.now().atZone(ZoneId.systemDefault()).format(formatterMill);
	}

}
