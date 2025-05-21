package cn.game.games.core.collector;

import cn.game.core.performance.metric.AbstractMetricCollector;
import cn.game.core.performance.metric.MetricType;
import cn.game.games.net.game.manager.PlayerManager;

public class PlayerConcurrencyCollector extends AbstractMetricCollector {

	public PlayerConcurrencyCollector() {
		super("players.concurrency", MetricType.BUSINESS, 3, 0.7, 0.9);
	}

	@Override
	protected double doCollect() {
		int onlinePlayers = PlayerManager.getInstance().getOnlineCount();
		int maxPlayers = 10000; // 假设最大玩家数为10000;
		return maxPlayers > 0 ? (double) onlinePlayers / maxPlayers : 0;
	}

}
