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
	SERVER_ID_LOCK,
	/** 只能有一个GameServer执行某段逻辑时，使用这个锁。  */
	GAME_SERVER_LOCK,
	/**ios 微信 access_token 请求分布式锁*/
	IOS_WE_CHAT_ACCESS_TOKEN_REFRESH_LOCK,
	/** SimplePlayer初始化锁 */
	SERVER_SIMPLE_PLAYER_INIT,
	/**ios 平台支付订单的信息 key openid, val： pid_orderId */
	IOS_OPENID_ORDER_DATA,
	/** ios 平台支付订单的锁 */
	IOS_OPENID_ORDER_DATA_LOCK,

	//************宗门相关***********
	/**** 宗门简单数据 */
	ZONG_MEN_SIMPLE_DATA,
	/**** 玩家加入宗门全局锁 */
	ZONG_MEN_JOIN_PLAYER_LOCK,
	/**** 宗门名称--id */
	ZONG_MEN_NAME_ID,
	/**** 宗门创建锁 */
	ZONG_MEN_CREATE_LOCK,
	/** 宗门在哪个服务器 */
	ZONG_MEN_SERVER_ID,
	//************宗门相关***********

	;

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
