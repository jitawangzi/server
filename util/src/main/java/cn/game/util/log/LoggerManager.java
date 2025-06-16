package cn.game.util.log;

import java.io.File;

public class LoggerManager {

    private static final LoggerManager INSTANCE = new LoggerManager();

    private LoggerManager() {
    }

    public static LoggerManager getInstance() {
        return INSTANCE;
    }

    public static void init() throws Exception {

		String logPath = System.getProperty("SEVER_PATH", System.getenv("SEVER_PATH"));
		if (logPath == null) {
			logPath = "..";
		}
		System.setProperty("SEVER_PATH", logPath);

		String cylog = System.getProperty("CYLOG_PATH", System.getenv("CYLOG_PATH"));
        if (cylog != null) {
            //畅游环境，直接用运维配置的地址
        } else {
            cylog = logPath + "/logs/cylog";
        }
		String serverid = System.getProperty("game.server.id");
		if (serverid != null) {
			cylog += "/" + serverid;
		}
        System.setProperty("SEVER_PATH_CYLOG", cylog);

        String fileName = "log4j2.xml";
		String contextPath = System.getProperty("user.dir") + "/";
		File file = new File(contextPath + fileName);
		// 设置配置文件路径
		System.setProperty("log4j.configurationFile", file.getAbsolutePath());
		// 使用异步日志
		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.trace, SystemLogger::trace);
        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.debug, SystemLogger::debug);
        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.info, SystemLogger::info);
        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.warn, SystemLogger::warn);
        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.error, SystemLogger::error);
        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.fatal, SystemLogger::fatal);

        flushAll();

    }

    private static void flushAll() {
        for (LoggerType loggerType : LoggerType.values()) {
			loggerType.logger.info("null");
        }
    }
}
