package cn.game.games.net.game.module.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MultiPlayerActivityBase extends ActivityBase {
	protected Map<Long, ActivityPlayerData> playerDataMap = new ConcurrentHashMap<>();

	// 玩家加入活动
	public void joinActivity(long playerId) {
		if (canJoin(playerId)) {
			ActivityPlayerData data = new ActivityPlayerData();
			data.setPlayerId(playerId);
			data.setJoinTime(System.currentTimeMillis());
			playerDataMap.put(playerId, data);
			onPlayerJoin(playerId);
		}
	}

	// 玩家离开活动
	public void leaveActivity(long playerId) {
		playerDataMap.remove(playerId);
		onPlayerLeave(playerId);
	}

	// 获取活动内玩家数据
	public ActivityPlayerData getPlayerData(long playerId) {
		return playerDataMap.get(playerId);
	}

	@Override
	public List<Long> getParticipants() {
		return new ArrayList<>(playerDataMap.keySet());
	}

	// 子类实现的钩子方法
	protected abstract void onPlayerJoin(long playerId);

	protected abstract void onPlayerLeave(long playerId);

}