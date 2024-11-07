package cn.game.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通过apollo获取配置后，进行后续的载入
 * 
 * 2021年11月9日 下午5:39:13
 * @author SYQ
 */
public abstract class ApolloLoader {

	private static Logger logger = LoggerFactory.getLogger(ApolloLoader.class);

	protected static String appId = System.getProperty("app.id", System.getenv("APP_ID"));
	static {
		if (appId == null) {
			Properties properties = new Properties();
			// 使用 ClassLoader 加载文件
			try (InputStream inputStream = ApolloLoader.class.getClassLoader().getResourceAsStream("META-INF/app.properties")) {
				if (inputStream == null) {
					throw new IOException(" unable to find META-INF/app.properties");
				}
				// 加载属性
				properties.load(inputStream);
				appId = properties.getProperty("app.id");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 载入某种配置
	 * 
	 * @param externalConfigFileLocation
	 * @throws Exception
	 */
	protected abstract void load(String externalConfigFileLocation) throws Exception;

	protected void checkLoad(String externalConfigFileLocation) throws Exception {

		File externalConfigFile = new File(externalConfigFileLocation);
		if (!externalConfigFile.exists()) {
			throw new IOException("配置文件不存在，path ：" + externalConfigFileLocation);
		} else {
			if (!externalConfigFile.isFile()) {
				throw new IOException("配置文件不正确，path ：" + externalConfigFileLocation);
			} else {
				if (!externalConfigFile.canRead()) {
					throw new IOException("配置文件不能被读取，path ：" + externalConfigFileLocation);
				}
			}
		}
	}

	/**
	 * 默认的初始化方法
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		String pathName = getPathName();
		String xmlPath = prop2Xml(pathName);
		checkLoad(xmlPath);
		load(xmlPath);
	}

	protected String getPathName() {
		String pathName;
		String system = System.getProperty("os.name");
		if (system.toLowerCase().startsWith("win")) {
			pathName = "C:/opt/data/" + appId + "/config-cache/";
		} else {
			//除了win其他系统路径一样
			pathName = "/opt/data/" + appId + "/config-cache/";
		}

		String cluster = "default";
		List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
		for (String in : inputArgs) {
			if (in.contains("Dapollo") && in.contains("cluster")) {
				String[] clusters = in.split("=");
				cluster = clusters[1].replaceAll(" ", "");
			}
		}
		pathName += appId + "+" + cluster + "+" + getFileName() + ".properties";
		return pathName;
	}
	/**
	 * 获取配置文件名,带文件后缀
	 * 
	 * @return
	 */
	protected abstract String getFileName();

	protected String prop2Xml(String path) throws Exception {
		StringBuffer fileContent = new StringBuffer();
		File filename = new File(path);
		InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
		BufferedReader br = new BufferedReader(reader);
		int f = 0;
		String line = "";
		line = br.readLine();
		while (line != null) {
			if (f > 1) {
				//前两行注释不要
				fileContent.append(line);
			}
			line = br.readLine();
			f++;
		}

		//去掉content=
		fileContent.replace(0, 8, "");
		//java反转义
		String outContent = StringEscapeUtils.unescapeJava(fileContent.toString());

		//生成xml文件
		String outPath = path.replaceAll(".properties", "");
		File file = new File(outPath);
		if (!file.exists()) {
			file.createNewFile();
		} else {
			// 先删除再重新创建不然会报错
			file.delete();
			file.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(file, true);
		out.write(outContent.getBytes("utf-8"));
		out.close();
		return outPath;
	}
}
