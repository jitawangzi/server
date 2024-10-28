package cn.game.core.net.client;

public enum LogoutType {
		/** 未知 */
    Unknown(0, "Unknown", "未知"),
	ClientRequest(1, "ClientRequest", "客户端请求退出"),
	Timeout(2, "Timeout", "客户端时间长没有发请求，超时被服务器退出"),
    /** 服务器关闭 */
    ServerClose(3, "ServerClose", "服务器关闭"),
	ClientLoginFail(4, "ClientLoginFail", "登录失败"),
    /** 服务器维护 */
    ServerMaintain(5, "ServerMaintain", "服务器维护"),
    /** 被GM踢 */
    GMKick(6, "GMKick", "被GM踢"),
    
    ForbidAccount(7, "ForbidAccount", "封号被踢"),
	Reconnect(8, "Reconnect", "重连时踢掉老的连接"),
	WrongReconnection(9, "WrongReconnection", "错误的重连，一般出现在，缓存已经清理了，客户端还重连"),
	LoginOtherServer(10, "LoginOtherServer", "登录了其他服务器,退出当前服务器"),
	;

	private int ID;
	private String Name;
	private String Desc;

	private LogoutType(int ID, String Name, String Desc) {
		this.ID = ID;
		this.Name = Name;
		this.Desc = Desc;
	}

	public int getID() {
		return ID;
	}

	public String getName() {
		return Name;
	}

	public String getDesc() {
		return Desc;
	}

}
