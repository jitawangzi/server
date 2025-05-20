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
	 * 玩家数据定时保存延长
	 * 在系统负载过高时，降低玩家数据保存频率，减少IO操作
	 */
	PlayerDataSave,

}