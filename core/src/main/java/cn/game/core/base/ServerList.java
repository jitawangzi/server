package cn.game.core.base;

import java.io.Serializable;

public class ServerList  implements Serializable,Comparable<ServerList>{

	/**  */
	private static final long serialVersionUID = -5416232579959359423L;
	public static final int STATUS_RUN = 1;		//正常运行
	public static final int STATUS_MAINTANCE = 2;//维护
	public static final int STATUS_NEW_SERVER = 3;//开新服
	public static final int STATUS_SHUTDOWN = 4;//停服
	
    private String serverId;

    private String name;

    private Integer status;

    private String ip;

    private Integer port;

    private String internalIp;

    private Integer internalPort;

	private String ServerOpenTime;

    private Integer seq;
	private Integer type;
	private String version;
	private Integer priorty;

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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getInternalIp() {
		return internalIp;
	}

	public void setInternalIp(String internalIp) {
		this.internalIp = internalIp;
	}

	public Integer getInternalPort() {
		return internalPort;
	}

	public void setInternalPort(Integer internalPort) {
		this.internalPort = internalPort;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Integer getPriorty() {
		return priorty;
	}

	public void setPriorty(Integer priorty) {
		this.priorty = priorty;
	}

	public String getServerOpenTime() {
		return ServerOpenTime;
	}

	public void setServerOpenTime(String serverOpenTime) {
		ServerOpenTime = serverOpenTime;
	}

	@Override
	public int compareTo(ServerList o) {

		if (this.seq>o.seq)
		{
			return -1  ; 
		}else if (this.seq<o.seq)
		{
			return 1 ;
		}
		return 0;
	}

	@Override
	public String toString() {
		return "ServerList [serverId=" + serverId + ", name=" + name + ", status=" + status + ", ip=" + ip + ", port="
				+ port + ", internalIp=" + internalIp + ", internalPort=" + internalPort + ", seq=" + seq + ", type="
				+ type + ", version=" + version + ", priorty=" + priorty + "]";
	}

}