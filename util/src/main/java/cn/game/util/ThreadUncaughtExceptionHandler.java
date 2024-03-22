package cn.game.util;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUncaughtExceptionHandler implements UncaughtExceptionHandler
{
	private static final Logger	log	= LoggerFactory.getLogger(ThreadUncaughtExceptionHandler.class);

	public void uncaughtException(Thread t, Throwable e)
	{
		log.error("Critical Error - Thread:"+t.getName()+" terminated abnormaly:"+e.getMessage(), e);
		if (e instanceof OutOfMemoryError)
		{
			log.error("Out of memory! You should get more memory!");
		}
	}

}
