package cn.game.core.zookeeper;

public enum ZkCacheType {
	/**
	* 虚拟分服列表
	*/
	VIRTUAL_SERVER_LIST,

	/**
	 * 真实的服务器列表（进程id）
	 */
	SERVER_LIST,

	/**
	 * 其他自定义缓存
	 */
	CUSTOM_CACHE;
}
