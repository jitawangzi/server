package cn.game.login.cache.entity;

import java.io.Serializable;

public class PassportSession implements Serializable{

	private long sessionId;
	private long uid;
	
	public long getSessionId() {
		return sessionId;
	}
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	public long getUid() {
		return uid;
	}
	public void setUid(long uid) {
		this.uid = uid;
	}
	
	
}
