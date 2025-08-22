package cn.game.core.event;

/**
 * 服务器级别的事件类型
 */
public enum ServerEventTypeEnum {

	PlayerEvent(100, "转发玩家事件"),

	/** 参数：老状态、新状态  */
	ServerLoad(101, "服务器负载状态变化"),
	/** 一般是gm指令修改了系统时间,一般只测试用 */
	SystemTimeChange(108, "系统时间改变"),
	NewDay(109, "过晚上12点，跨天"),
	/** 跨周 */
	NewWeek(110, "跨周"),
	/** 跨月 */
	NewMonth(111, "跨月"),
	
	/**  参数： VirtualServer id： server1、server2等等 */
	VirtualServerOpen(200, "某服务器到达开服时间，开启服务器"),
	/**  参数： 活动id */
	ActivityOpenTime(300, "某活动到达开启时间"),
	/**  参数： 活动id */
	ActivityShutDownTime(301, "某活动到达关闭时间"),
	/**  参数： 活动id */
	ActivityDestoryTime(302, "某活动到达销毁时间"),
	;
	private int id;
	private String desc;

	private ServerEventTypeEnum(int id, String desc) {
		this.id = id;
		this.desc = desc;
	}

	public int getId() {
		return this.id;
	}

	public String getDesc() {
		return this.desc;
	}
}
