package cn.game.games.net.game.jobs;

import java.util.Collection;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;

public class StoreStaminaJob implements Job
{

	private static Logger	log	= LoggerFactory.getLogger(StoreStaminaJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		long now = System.currentTimeMillis();
		Collection<GameClient> gameClients = GameClientManager.getInstance().getGameClients();
		for (GameClient gameClient : gameClients) {
			Player player = PlayerManager.getInstance().getPlayer(gameClient.getPlayerId());
			PlayerHelper.addTask(gameClient.getPlayerId(), r -> {
				player.getBattleModule().addStoreStaminas((int) (now / 1000));
			});
		}
	}
}
