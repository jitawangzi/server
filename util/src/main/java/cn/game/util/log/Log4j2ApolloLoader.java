package cn.game.util.log;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;

import cn.game.util.ApolloLoader;
import cn.game.util.ServerType;
import cn.game.util.config.ConfigUtil;

/**
 * 通过Apollo初始化log4j2
 */
public class Log4j2ApolloLoader extends ApolloLoader {

	private static final Logger logger = LoggerFactory.getLogger(Log4j2ApolloLoader.class);
	private static final String NAMESPACE = "log4j2";
	private static final String FILE_NAME = NAMESPACE + ".xml";

	private static Log4j2ApolloLoader instance = new Log4j2ApolloLoader();

	private Log4j2ApolloLoader() {
		// 私有构造函数，确保单例
	}

	public static Log4j2ApolloLoader getInstance() {
		return instance;
	}

	@Override
	protected void load(String externalConfigFileLocation) throws Exception {
		// 从Apollo获取log4j2.xml
		ConfigFile configFile = ConfigService.getConfigFile(NAMESPACE, ConfigFileFormat.XML);
		String configContent = configFile.getContent();

		if (configContent == null || configContent.trim().isEmpty()) {
			logger.warn("Empty log4j2 configuration from Apollo, keeping base configuration");
			return;
		}

		// 加载配置到Log4j2
		ByteArrayInputStream configInputStream = new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8));

		// 获取当前LoggerContext并重新配置
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		ConfigurationSource source = new ConfigurationSource(configInputStream);
		Configuration newConfig = new XmlConfiguration(context, source);
		context.start(newConfig);

		logger.info("Log4j2 configuration updated from Apollo");
	}

	@Override
	public void init() throws Exception {
		// 设置必要的系统属性
		setupLogProperties();
		// 先缓存配置文件到本地，后续加载
		ConfigService.getConfigFile(NAMESPACE, ConfigFileFormat.XML);

		super.init();
		// 手动注册配置变更监听器
		registerConfigChangeListener();

		// 刷新所有日志
		flushAll();
	}

	private void setupLogProperties() {

		String logPath = ConfigUtil.getConfig("SEVER_PATH");
		if (logPath == null) {
			logPath = "..";
		}
		System.setProperty("SEVER_PATH", logPath);

		String cylog = ConfigUtil.getConfig("CYLOG_PATH");
		if (cylog != null) {
			// 畅游环境，直接用运维配置的地址
		} else {
			cylog = logPath + "/logs/cylog";
		}
		String serverid = ConfigUtil.getConfig(ServerType.Game.getServerIdKey());
		if (serverid != null) {
			cylog += "/" + serverid;
		}
		System.setProperty("SEVER_PATH_CYLOG", cylog);

		// 使用异步日志
		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

		flushAll();
	}

	private void flushAll() {
		for (LoggerType loggerType : LoggerType.values()) {
			loggerType.logger.info("null");
		}
	}

	private void registerConfigChangeListener() {
		Config config = ConfigService.getConfig(NAMESPACE);
		config.addChangeListener(new ConfigChangeListener() {
			@Override
			public void onChange(ConfigChangeEvent changeEvent) {
				try {
					logger.info("Detected log4j2.xml changes from Apollo, reloading configuration...");
					load(null);
				} catch (Exception e) {
					logger.error("Failed to reload log4j2 configuration", e);
				}
			}
		});
	}

	@Override
	protected String getFileName() {
		return FILE_NAME;
	}
}