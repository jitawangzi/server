package cn.game.games.net.game.manager;

import java.io.IOException;
import java.util.Collection;

import cn.game.core.base.ServerContext;
import cn.game.core.net.pressure.GlobalMessageStatistics;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.net.client.GameClient;
import cn.game.util.Config;

public class PressureTestManager {
	private static PressureTestManager instance = new PressureTestManager();

	private PressureTestManager() {
	};

	public static PressureTestManager getInstance() {
		return instance;
	}

	/** 初始化一些数据 */
	public void init() {
		if (ServerContext.getInstance().getRunMode().isPressure() && ServerContext.getInstance().isPressureDev()) {
			VxHolder.vertx.setPeriodic(Config.messageStatisticsInterval * 60 * 1000, r -> {
				Collection<GameClient> gameClients = GameClientManager.getInstance().getGameClients();
				try {
					GlobalMessageStatistics.getInstance().calculateStatisticsAndSaveResult(gameClients);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}
}
