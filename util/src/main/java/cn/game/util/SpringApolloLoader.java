package cn.game.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

/**
 * 初始化spring
 * 
 * 2021年11月9日 下午5:42:23
 * @author SYQ
 */
public class SpringApolloLoader extends ApolloLoader {

	private static Logger logger = LoggerFactory.getLogger(SpringApolloLoader.class);
	private String fileName;

	@Override
	public void init() throws Exception {
		String[] args = ConfigService.getAppConfig().getArrayProperty("springArgs", ",", null);
		if (args == null) { 
			throw new IllegalArgumentException("spring 配置文件没有指定"); 
		}
		for (int i = 0; i < args.length; i++) {
			fileName = args[i];
			// 先把配置文件缓存到本地
			ConfigFile configFile = ConfigService.getConfigFile(fileName.substring(0, fileName.indexOf(".")), ConfigFileFormat.XML);
			String content = configFile.getContent();
			logger.debug(content);
			String pathName = getPathName();
			String xmlPath = prop2Xml(pathName);
//			args[i] = "file:" + xmlPath; // 转绝对路径
			args[i] = xmlPath; // 转绝对路径
		}
		// 通过本地文件初始化spring
		SpringContextLoader.main(args);
	}

	@Override
	public void load(String externalConfigFileLocation) throws Exception {
	}
	@Override
	protected String getFileName() {
		return fileName;
	}
}
