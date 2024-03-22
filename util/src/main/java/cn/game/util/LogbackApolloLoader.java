package cn.game.util;

import java.io.ByteArrayInputStream;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * 通过spring初始化logback
 * 
 * @date 2021年11月9日 下午5:40:42
 * @author SYQ
 */
@Component
public class LogbackApolloLoader extends ApolloLoader {

	private static final String fileName = "logback.xml";

	@Override
	public void load(String externalConfigFileLocation) throws Exception {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		ConfigFile configFile = ConfigService.getConfigFile("logback", ConfigFileFormat.XML);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(configFile.getContent().getBytes());
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(lc);
//		lc.reset();
		configurator.doConfigure(byteArrayInputStream);
		StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

	}

	@PostConstruct
	private void initLog() throws Exception {
		load(null);
	}

	// 监听配置文件更新，同时也会先把这个缓存到本地
	@ApolloConfigChangeListener(fileName)
	private void anotherOnChange(ConfigChangeEvent changeEvent) throws Exception {
		//当logback.xml文件改变的时候动态更新
		load(null);
	}

	@Override
	protected String getFileName() {
		return fileName;
	}
}
