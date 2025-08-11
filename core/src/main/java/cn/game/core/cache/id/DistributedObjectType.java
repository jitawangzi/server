package cn.game.core.cache.id;

import cn.game.util.ServerType;

/**
 * 分布式对象类型枚举
 */
public enum DistributedObjectType {
	PLAYER(ServerType.Game), GUILD(ServerType.Cross);

	/** 这个对象保存在哪种服务器里 */
	private ServerType serverType;

	public ServerType getServerType() {
		return serverType;
	}

	private DistributedObjectType(ServerType serverType) {
		this.serverType = serverType;
	}
}