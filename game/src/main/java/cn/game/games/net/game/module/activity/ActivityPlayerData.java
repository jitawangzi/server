package cn.game.games.net.game.module.activity;

import java.util.HashMap;
import java.util.Map;

public class ActivityPlayerData {
	private long playerId;
	private long joinTime;
	private Map<String, Object> extraData = new HashMap<>(); // 额外数据

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public long getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(long joinTime) {
		this.joinTime = joinTime;
	}

	public Map<String, Object> getExtraData() {
		return extraData;
	}

	public void setExtraData(Map<String, Object> extraData) {
		this.extraData = extraData;
	}

}