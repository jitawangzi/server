package cn.game.games.net.game.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.net.game.manager.GameClientManager;

public class OnlineJob implements Job {

	private static Logger log = LoggerFactory.getLogger(OnlineJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		GameClientManager.getInstance().checkClient();
	}
}
