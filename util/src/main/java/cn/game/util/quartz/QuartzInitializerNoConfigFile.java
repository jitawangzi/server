package cn.game.util.quartz;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuartzInitializerNoConfigFile {
	private static Logger log = LoggerFactory.getLogger(QuartzInitializerNoConfigFile.class);

	private String configFile;
	private StdSchedulerFactory factory;
	private boolean performShutdown = true;
	private Scheduler scheduler = null;
	private int startDelay = 0;

	public QuartzInitializerNoConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public void Initializer() {
		try {
			if (configFile != null) {
				factory = new StdSchedulerFactory(configFile);
			} else {
				factory = new StdSchedulerFactory();
			}

			scheduler = factory.getScheduler();

			if (startDelay <= 0) {
				// Start now
				scheduler.start();
				log.info("Scheduler has been started...");
			} else {
				// Start delayed
				// scheduler.startDelayed(startDelay);
				log.info("Scheduler will start in " + startDelay + " seconds.");
			}

		} catch (SchedulerException e) {

			e.printStackTrace();
		}
	}

	public void addJob(JobDetail jobDetail, CronTrigger trig) throws SchedulerException {
		scheduler.scheduleJob(jobDetail, trig);
	}

	public void addJob(String name, String group, Class<?> clazz, String cron) throws SchedulerException {

//		JobDetail jobDetail = new JobDetail(name, group, clazz);
//
//		CronTrigger trig = new CronTrigger();
//		try {
//			trig.setCronExpression(new CronExpression(cron));
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		scheduler.scheduleJob(jobDetail, trig);
	}

	public void destroyed() {
		if (!performShutdown) {
			return;
		}

		try {
			if (scheduler != null) {
				scheduler.shutdown();
			}
		} catch (Exception e) {
			log.info("Quartz Scheduler failed to shutdown cleanly: " + e.toString());
			e.printStackTrace();
		}

		log.info("Quartz Scheduler successful shutdown.");
	}
}
