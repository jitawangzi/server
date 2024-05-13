package cn.game.games.core.log;

import java.io.ByteArrayInputStream;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import cn.game.util.ApolloLoader;
import cn.game.util.log.EmbeddedLogger;
import cn.game.util.log.LoggerType;
import cn.game.util.log.SystemLogger;

/**
 * 通过spring初始化log4j
 * 
 * @date 2021年11月9日 下午5:40:42
 * @author SYQ
 */
//@Component
public class Log4jApolloLoader extends ApolloLoader {

	private static final String fileName = "log4j.xml";

	@Override
	public void load(String externalConfigFileLocation) throws Exception {
//		ServerEventManager.registerEventHandler(INSTANCE);

		String logPath = "..";
//		if (Configuration.startupMode == Configuration.StartupMode.docker) {
//			logPath = "";
//		}
		System.setProperty("SEVER_PATH", logPath);

		String cylog = System.getenv("CYLOG_PATH");
		if (cylog != null) {
			// 畅游环境，直接用运维配置的地址
		} else {
			cylog = logPath + "/logs/cylog";
		}
		System.setProperty("SEVER_PATH_CYLOG", cylog);

		ConfigFile configFile = ConfigService.getConfigFile("logback", ConfigFileFormat.XML);

//		String fileName = "log4j2.xml";
//		File file = new File(Configuration.contextPath + fileName);
//		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		ByteArrayInputStream in = new ByteArrayInputStream(configFile.getContent().getBytes());

		ConfigurationSource source = new ConfigurationSource(in);
		Configurator.initialize(null, source);

		EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.trace, SystemLogger::trace);
		EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.debug, SystemLogger::debug);
		EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.info, SystemLogger::info);
		EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.warn, SystemLogger::warn);
		EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.error, SystemLogger::error);
		EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.fatal, SystemLogger::fatal);

		flushAll();
	}

	@PostConstruct
	private void initLog() throws Exception {
		load(null);
	}

	// 监听配置文件更新，同时也会先把这个缓存到本地
	@ApolloConfigChangeListener(fileName)
	private void anotherOnChange(ConfigChangeEvent changeEvent) throws Exception {
		// 当log4j.xml文件改变的时候动态更新
		load(null);
	}

	@Override
	protected String getFileName() {
		return fileName;
	}

	private static void flushAll() {
		for (LoggerType loggerType : LoggerType.values()) {
			loggerType.logger.error("null");
		}
	}
}
