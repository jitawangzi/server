package cn.game.games.net.game.module.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.game.games.cache.entity.Player;

public abstract class MultiPlayerActivityBase extends ActivityBase {
	protected Map<Long, ActivityPlayerData> playerDataMap = new ConcurrentHashMap<>();

	@Override
	public void init(int id, Object owner, boolean isNew) {
		super.init(id, owner, isNew);
		this.isMultiPlayer = true;
		if (isNew) {
		}
	}

	// 玩家加入活动
	public void joinActivity(Player player) {
		if (canJoin(player)) {
			ActivityPlayerData data = new ActivityPlayerData();
			data.setPlayerId(player.getPlayerId());
			data.setJoinTime(System.currentTimeMillis());
			playerDataMap.put(player.getPlayerId(), data);
			onPlayerJoin(player);
		}
	}

	// 玩家离开活动
	public void leaveActivity(Player player) {
		playerDataMap.remove(player.getPlayerId());
		onPlayerLeave(player);
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
	protected abstract void onPlayerJoin(Player player);

	protected abstract void onPlayerLeave(Player player);


	public boolean shouldExpire(long now) {
		// TODO Auto-generated method stub
		return false;
	}

}