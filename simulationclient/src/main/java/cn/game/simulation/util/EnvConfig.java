package cn.game.simulation.util;

import java.net.URL;
import java.util.Properties;

public class EnvConfig {
	public static String loginServerUrl;
	public static String gateServerIp;
	public static int gateServerPort;
	public static String passportUsername;
	public static String pwd;
	public static String logbackFile;
	public static String args;
	public static boolean login;
	public static boolean initLogback;
	public static String serverId;
	public static String version;

	public static int botIdStart;
	public static int botCount;
	public static int loginCountPerSecond;
	public static int sendInterval;

	public static void init(Properties initialProp) {

		passportUsername = initialProp.getProperty("user.name");
		pwd = initialProp.getProperty("user.pwd");
		gateServerIp = initialProp.getProperty("game.server.ip");
		gateServerPort = initialProp.getProperty("game.server.port") == null ? 0 : Integer.parseInt(initialProp.getProperty("game.server.port"));
		loginServerUrl = initialProp.getProperty("login.server.url");

		logbackFile = initialProp.getProperty("logbackFile");
		args = initialProp.getProperty("argsFile");
		login = Boolean.parseBoolean(initialProp.getProperty("login"));

		serverId = initialProp.getProperty("game.server.id");
		version = initialProp.getProperty("game.server.version");
		initLogback = Boolean.parseBoolean(initialProp.getProperty("initLogback"));
//		LogbackConfig.init(initLogback, logbackFile);
		URL resource = Thread.currentThread().getContextClassLoader().getResource(args);
		args = resource.getPath();

		botIdStart = Integer.parseInt("botIdStart");
		botCount = Integer.parseInt("botCount");
		loginCountPerSecond = Integer.parseInt("loginCountPerSecond");
		sendInterval = Integer.parseInt("sendInterval");

	}

}
