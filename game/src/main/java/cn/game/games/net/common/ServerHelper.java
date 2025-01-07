package cn.game.games.net.common;

import cn.game.util.ServerType;

public class ServerHelper {

	/** 
	 * 解析server id，使用程序运行时参数或者环境变量设置的server id
	 * @param args
	 * @param serverType
	 * @return
	 */
	public static String parseServerId(String[] args, ServerType serverType) {
		String serverId = null;
		String serverIdKey = serverType.getServerIdKey();
		if (args.length == 0) {
			serverId = System.getProperty(serverIdKey);
			if (serverId == null) {
				serverId = System.getenv(serverIdKey);
			}
		} else {
			serverId = args[0];
		}
		if (serverId == null) {
			throw new IllegalArgumentException(serverType.name() + "没有设置 serverId, 请使用参数或者环境变量设置。");
		}
		System.setProperty(serverIdKey, serverId);
		return serverId;
	}
}
