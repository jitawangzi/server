package cn.game.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.util.file.WatchDir;

/**
 * class 热更新 agent
 *  2020年9月10日 上午10:42:55
 * @author SYQ
 */
public class HotUpdateAgent {

	private static Instrumentation instrumentation;
//	private static Path userPath = Paths.get("D:/work/Game/bin");
	// 生产环境，从工作目录里的cn目录下加载class文件
	private static Path userPath = Paths.get(System.getProperty("user.dir") + File.separator + "cn");
	private static final Logger log = LoggerFactory.getLogger(HotUpdateAgent.class);

	public static void agentmain(String args, Instrumentation inst) {
		instrumentation = inst;
		log.info("class 热更新 agent 启动成功....");
		// class根目录
		try {
			WatchDir watchDir = new WatchDir(userPath, true, HotUpdateAgent::redefineClasses);
			watchDir.start();
//			WatchDir.main(new String[] { "-r", userPath.toString() });
		} catch (Exception e) {
			e.printStackTrace();
			log.error("热更新异常", e);
		}
	}

	public static void redefineClasses(Path replaceFile) {
		if (!replaceFile.toString().endsWith(".class")) {
			return;
		}

		log.info("准备 redefine  class 【{}】", replaceFile.getFileName());
		String rootString = userPath.toString();
		// 去掉绝对路径的工程前缀
		String classPath = replaceFile.toString().substring(rootString.length() + 1, replaceFile.toString().length());
		// 路径分隔符替换成.
		classPath = classPath.replace(File.separator, ".");
		// 去掉末尾的.class,最终包.类名称
		int index = classPath.lastIndexOf(".");
		classPath = classPath.substring(0, index);

		try {
			File file = replaceFile.toFile();
			byte[] bytes = fileToBytes(file);
			Class<?> replaceClass = Class.forName(classPath);
			ClassDefinition classDefinition = new ClassDefinition(replaceClass, bytes);
			instrumentation.redefineClasses(classDefinition);
			log.info("redefine class 【{}】 成功, 文件大小 【{}】： ", replaceFile.getFileName(), bytes.length);
		} catch (Throwable e) {
			e.printStackTrace();
			log.error("热更新class失败 ： " + replaceFile.getFileName(), e);
		}
	}

	public static byte[] fileToBytes(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		byte[] bytes = new byte[in.available()];
		in.read(bytes);
		in.close();
		return bytes;
	}
}
