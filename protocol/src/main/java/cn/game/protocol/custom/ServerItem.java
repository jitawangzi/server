package cn.game.protocol.custom;

import net.sf.json.JSONObject;

public class ServerItem {

	public static final byte STATUS_NORMAL_NEW = 1; //正常运行-新区
	public static final byte STATUS_NORMAL_FAST = 2; //正常运行-流畅
	public static final byte STATUS_NORMAL_HOT = 3; //正常运行-火爆
	public static final byte STATUS_NORMAL_FULL = 4; //正常运行-拥挤
	public static final byte STATUS_MAINTANCE = 5; //维护
	public static final byte STATUS_NEW_SERVER = 6; //开新服
	public static final byte STATUS_NEW_STOP = 7; //停服

	private String serverId;
	private String name;
	private String ip;
	private String internalIp;
	private int port;
	private int internalPort;
	private int status;
	private int visible;
	private int canEnter;
	private int nameColor;

	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		o.put("server_id", serverId);
		o.put("name", name);
		o.put("ip", ip);
//		o.put("internalIp", internalIp);
		o.put("port", port);
//		o.put("internalPort", internalPort);
		o.put("status", status);
//		o.put("visible", visible);
//		o.put("canEnter", canEnter);
//		o.put("nameColor", nameColor);
		return o;
	}

	public void fromJSON(JSONObject jr) {
		serverId = jr.getString("server_id");
		name = jr.getString("name");
		ip = jr.getString("ip");
//		internalIp = jr.getString("internalIp");
		port = jr.getInt("port");
//		internalPort = jr.getInt("internalPort");
		status = jr.getInt("status");
//		visible = jr.getInt("visible");
//		canEnter = jr.getInt("canEnter");
//		nameColor = jr.getInt("nameColor");
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getVisible() {
		return visible;
	}
	public void setVisible(int visible) {
		this.visible = visible;
	}
	public int getCanEnter() {
		return canEnter;
	}
	public void setCanEnter(int canEnter) {
		this.canEnter = canEnter;
	}
	public int getNameColor() {
		return nameColor;
	}
	public void setNameColor(int nameColor) {
		this.nameColor = nameColor;
	}

	public String getInternalIp() {
		return internalIp;
	}

	public void setInternalIp(String internalIp) {
		this.internalIp = internalIp;
	}

	public int getInternalPort() {
		return internalPort;
	}

	public void setInternalPort(int internalPort) {
		this.internalPort = internalPort;
	}

}
