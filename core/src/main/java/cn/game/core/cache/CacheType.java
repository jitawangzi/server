package cn.game.core.cache;

/**
 * 缓存类型，作为缓存key的前缀
 * @author abc
 *
 */
public enum CacheType {
	
	//通行证用户session
	PASSPORT_SESSION(), // passportSessionId => PassportSession
	
	/** 玩家在哪个服务器 */
	PLAYER_SERVER_ID,
	
	//实体字段
	F_USER_NAME_ID(), // DbUser.username => DbUser.id
	
	//其他
	SERVER_LIST(), RECENT_SERVER_LIST(),

	/** 服务器序列号 */
	SERVER_SEQUENCE,
	/** 服务器序列号最大值 */
	DISTRIBUTED_WORKER_COUNTER,
	
	/** 玩家简单信息 */
	PLAYER_SIMPLE,
	/** 自增起始id */
	Player_MAX_ID,

	/** 所有用户名 */
	SET_ALL_NAME,

	/** 玩家name--id */
	MAP_PLAYER_NAME_ID,

	/** 排行榜 */
	SET_RANK,
	// 一些分布式锁定义
	// 服务器id分布式锁，防止同时多个同样id的服务器运行
	SERVER_LOCK,
	/** SimplePlayer初始化锁 */
	SERVER_SIMPLE_PLAYER_INIT,
	IOS_OPENID_ORDER_DATA;

	public String key(Object... ks) {

		if (ks == null || ks.length == 0) {
			return name();
		}
		StringBuilder b = new StringBuilder();
		b.append(name());
		for (Object k : ks) {
			b.append("_").append(k);
		}
		return b.toString();
	}
	
	private CacheType(){
	}
}
