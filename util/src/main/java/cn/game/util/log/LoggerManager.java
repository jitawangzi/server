package cn.game.util.log;

import java.io.File;
import java.net.URL;

import cn.game.util.config.ConfigUtil;

/**    
 * 基于本地文件的log4j2日志管理器
 * 2025年6月16日 11:39:12
 * @author SYQ
 */
public class LoggerManager {

	private static volatile boolean initialized = false;

    public static void init() throws Exception {

		if (initialized) {
			return;
		}
		String logPath = ConfigUtil.getConfig("SEVER_PATH");
		if (logPath == null) {
			logPath = "..";
		}
		System.setProperty("SEVER_PATH", logPath);

		String cylog = ConfigUtil.getConfig("CYLOG_PATH");
        if (cylog != null) {
            //畅游环境，直接用运维配置的地址
        } else {
            cylog = logPath + "/logs/cylog";
        }
		String serverid = ConfigUtil.getConfig("game.server.id");
		if (serverid != null) {
			cylog += "/" + serverid;
		}
        System.setProperty("SEVER_PATH_CYLOG", cylog);

		// 配置log4j2配置文件路径
		configureLog4j2();

		// 使用异步日志
		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

        flushAll();
		initialized = true;
		System.out.println("LoggerManager initialized successfully.");
    }

	/**
	 * 配置log4j2配置文件路径
	 * 优先级：系统变量指定路径 > classpath文件路径 > classpath资源
	 */
	private static void configureLog4j2() throws Exception {
		String configPath = null;
		String loadMethod = null;

		// 1. 优先检查系统变量指定的路径
		String customConfigPath = ConfigUtil.getConfig("log4j2.config.path");
		if (customConfigPath != null) {
			File customFile = new File(customConfigPath);
			if (customFile.exists() && customFile.isFile()) {
				configPath = customFile.getAbsolutePath();
				loadMethod = "系统变量指定路径（支持热更新）";
			} else {
				System.err.println("警告: 系统变量指定的log4j2配置文件不存在: " + customConfigPath);
			}
		}

		// 2. 如果系统变量未设置或文件不存在，尝试从classpath获取文件路径
		if (configPath == null) {
			URL configUrl = LoggerManager.class.getClassLoader().getResource("log4j2.xml");
			if (configUrl != null && "file".equals(configUrl.getProtocol())) {
				// 如果是文件协议，转换为文件路径（支持热更新）
				configPath = configUrl.getFile();
				// 处理URL编码的路径（如空格等特殊字符）
				configPath = java.net.URLDecoder.decode(configPath, "UTF-8");
				loadMethod = "classpath文件路径（支持热更新）";
			} else if (configUrl != null) {
				// 如果找到配置文件但不是文件协议（比如在jar包中），使用classpath方式
				configPath = "classpath:log4j2.xml";
				loadMethod = "classpath资源（不支持热更新）";
			} else {
				// 如果classpath中没有找到配置文件，抛出异常
				throw new Exception("log4j2.xml not found in classpath");
			}
		}

		// 设置配置文件路径
		System.setProperty("log4j.configurationFile", configPath);
		System.out.println("Log4j2配置加载方式: " + loadMethod);
		System.out.println("Log4j2配置文件路径: " + configPath);
	}

    private static void flushAll() {
        for (LoggerType loggerType : LoggerType.values()) {
			loggerType.logger.info("null");
        }
    }
}