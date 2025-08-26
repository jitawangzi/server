package cn.game.games.net.game.module.activity;

public class ActivityHelper {
	// 按玩家开启的
	/** 活动开启类型： 创建玩家 */
	public static final int OPENTYPE_PLAYER_CREATE = 1;
	/** 活动开启类型： 玩家创建角色后多少天 */
	public static final int OPENTYPE_PLAYER_CREATE_DAYS = 2;
	/** 活动开启类型： 玩家到达指定等级 */
	public static final int OPENTYPE_PLAYER_LEVEL = 3;
	
	// 按服务器开启的
	/** 活动开启类型： 开服后xx天 */
	public static final int OPENTYPE_SERVER_OPEN_DAY = 10;
	// 按时间开启的，可以是玩家的，也可以是服务器的
	/** 活动开启类型： 按时间开启 */
	public static final int OPENTYPE_DATE = 0;
	
	

	/** 普通的七日狂欢 */
	public static final int SEVENDAYS_CARNIVAL = 1;
}
