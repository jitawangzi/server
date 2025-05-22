package cn.game.core.event;

/**
 * 服务器级别的事件类型
 */
public enum ServerEventTypeEnum {

	PlayerEvent(100, "转发玩家事件"),

	/** 参数：老状态、新状态  */
	ServerLoad(101, "服务器负载状态变化"),

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
