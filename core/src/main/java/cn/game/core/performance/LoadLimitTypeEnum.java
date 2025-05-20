package cn.game.core.performance;

/**
 * 负载限制类型枚举，定义系统在高负载情况下可以限制的功能
 */
public enum LoadLimitTypeEnum {

	/**
	 * 登录功能限制
	 * 在系统负载过高时，限制新用户登录，保证已登录用户的体验
	 */
	Login,

	/**
	 * 全体聊天限制
	 * 在系统负载较高时，限制全服聊天功能，减少消息广播带来的压力
	 */
	GlobalChat,

	/**
	 * 排行榜刷新限制
	 * 在系统负载过高时，降低排行榜刷新频率，减少计算
	 */
	LeaderboardUpdate,

	/**
	 * 阻塞操作限制
	 * vertx的worker线程池超载了，减少阻塞操作，例如玩家在线保存频率
	 */
	BlockingOperation,
	/**
	 * 非阻塞操作限制
	 * vertx的eventloop线程超载了
	 */
	NoBlockingOperation,

}