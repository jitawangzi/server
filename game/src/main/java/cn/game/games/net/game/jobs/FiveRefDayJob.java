package cn.game.games.net.game.jobs;

import java.util.concurrent.ConcurrentHashMap;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.game.core.task.TaskManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.cache.entity.Player;

public class FiveRefDayJob implements Job {

	@Override
	/**
	 * 每日5点刷新逻辑
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		TaskManager.getInstance().addMainTask(() -> {

			ConcurrentHashMap<Long, Player> allPlayer = PlayerManager.getInstance().getAllPlayer();
			for (Player player : allPlayer.values()) {

//				PlayerHelper.refreshFiveClock(player);
			}
		});
	}

}
