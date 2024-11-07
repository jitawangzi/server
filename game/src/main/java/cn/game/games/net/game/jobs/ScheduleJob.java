package cn.game.games.net.game.jobs;

import java.util.Collection;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016;

/**    
 * 定时任务的实现，对于轻量的定时任务，可以写在这里，比quartz更方便。
 * 例如，每分钟检查在线玩家，每天凌晨刷新数据等。
 * 适用于cron写死不变的情况。 
 * 如果cron是配置的，则使用 SchedulerService
 * 2024年11月7日 18:42:34
 * @author SYQ
 */
@Component
public class ScheduleJob {

	@Scheduled(cron = "0 * * * * ?")
	public void onlineCheck() {
		GameClientManager.getInstance().checkClient();
	}

	@Scheduled(cron = "1 0 0 * * ?")
	public void newDay() {
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
