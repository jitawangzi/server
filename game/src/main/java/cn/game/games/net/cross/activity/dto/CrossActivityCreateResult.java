package cn.game.games.net.cross.activity.dto;

import java.io.Serializable;

/**
 * 跨服活动创建/自愈结果 DTO
 * 
 * @author shuyongqiang
 */
public class CrossActivityCreateResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean success;
	private long activityUid;
	private int configId;
	private String scopeKey;
	private String serverId;
	private String errorMsg;

	public CrossActivityCreateResult() {
	}

	public CrossActivityCreateResult(boolean success, long activityUid, int configId, String scopeKey, String serverId, String errorMsg) {
		this.success = success;
		this.activityUid = activityUid;
		this.configId = configId;
		this.scopeKey = scopeKey;
		this.serverId = serverId;
		this.errorMsg = errorMsg;
	}

	public static CrossActivityCreateResult success(long activityUid, int configId, String scopeKey, String serverId) {
		return new CrossActivityCreateResult(true, activityUid, configId, scopeKey, serverId, null);
	}

	public static CrossActivityCreateResult fail(int configId, String scopeKey, String errorMsg) {
		return new CrossActivityCreateResult(false, 0L, configId, scopeKey, null, errorMsg);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
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

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}
