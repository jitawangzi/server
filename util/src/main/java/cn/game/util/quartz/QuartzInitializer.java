package cn.game.util.quartz;

import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuartzInitializer
{
	private static Logger		log				= LoggerFactory.getLogger(QuartzInitializer.class);

	private Properties configFile;
	private StdSchedulerFactory	factory;
	private boolean				performShutdown	= true;
	private Scheduler			scheduler		= null;
	private int					startDelay		= 0;

	public QuartzInitializer(Properties properties)
	{
		this.configFile = properties;
	}

	public void Initializer()
	{
		try
		{
			if (configFile != null)
			{
				factory = new StdSchedulerFactory(configFile);
			} else
			{
				factory = new StdSchedulerFactory();
			}

			scheduler = factory.getScheduler();

			if (startDelay <= 0)
			{
				// Start now
				scheduler.start();
				log.info("Scheduler has been started...");
			} else
			{
				// Start delayed
//				scheduler.startDelayed(startDelay);
				log.info("Scheduler will start in " + startDelay + " seconds.");
			}
		} catch (SchedulerException e)
		{

			e.printStackTrace();
		}
	}

	public void destroyed()
	{
		if (!performShutdown) { return; }

		try
		{
			if (scheduler != null)
			{
				scheduler.shutdown();
			}
		} catch (Exception e)
		{
			log.info("Quartz Scheduler failed to shutdown cleanly: " + e.toString());
			e.printStackTrace();
		}

		log.info("Quartz Scheduler successful shutdown.");
	}
}
