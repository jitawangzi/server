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
import cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016;

/**    
 * 跨天等等的定时任务，还是使用quartz，spring的定时任务，改时间后不生效。 
 * 2024年11月17日 00:09:41
 * @author SYQ
 */
public class NewDayJob implements Job
{

	private static Logger	log	= LoggerFactory.getLogger(NewDayJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		Collection<GameClient> gameClients = GameClientManager.getInstance().getGameClients();
		for (GameClient gameClient : gameClients) {
			Player player = PlayerManager.getInstance().getPlayer(gameClient.getPlayerId());
			PlayerHelper.addTask(gameClient.getPlayerId(), r -> {
				PlayerHelper.refresh(player);
				// 通知客户端跨天了， 使用登陆来刷新所有数据。
				gameClient.sendProtocol(PlayerResetPush_01100016.getDefaultInstance());
			});
		}
	}
}
