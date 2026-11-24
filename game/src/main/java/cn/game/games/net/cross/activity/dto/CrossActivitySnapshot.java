package cn.game.games.net.cross.activity.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 跨服活动快照信息 DTO
 * 
 * @author shuyongqiang
 */
public class CrossActivitySnapshot implements Serializable {
	private static final long serialVersionUID = 1L;

	private long activityUid;
	private int configId;
	private String scopeKey;
	private int state;
	private long startTime;
	private long endTime;
	private long destroyTime;
	private String serverId;
	private Map<String, Object> extra = new HashMap<>();

	public CrossActivitySnapshot() {
	}

	public long getActivityUid() {
		return activityUid;
	}

	public void setActivityUid(long activityUid) {
		this.activityUid = activityUid;
	}

	public int getConfigId() {
		return configId;
	}

	public void setConfigId(int configId) {
		this.configId = configId;
	}

	public String getScopeKey() {
		return scopeKey;
	}

	public void setScopeKey(String scopeKey) {
		this.scopeKey = scopeKey;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getDestroyTime() {
		return destroyTime;
	}

	public void setDestroyTime(long destroyTime) {
		this.destroyTime = destroyTime;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public Map<String, Object> getExtra() {
		return extra;
	}

	public void setExtra(Map<String, Object> extra) {
		this.extra = extra;
	}
}
