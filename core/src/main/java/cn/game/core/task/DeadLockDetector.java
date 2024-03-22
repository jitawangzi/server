package cn.game.core.task;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.util.Config;


public class DeadLockDetector extends Thread
{
	private static Logger		log			= LoggerFactory.getLogger(DeadLockDetector.class);

	private static final int	sleepTime	= Config.DEADLOCK_CHECK_INTERVAL * 1000;

	private final ThreadMXBean	tmx;

	public DeadLockDetector()
	{
		super("DeadLockDetector");
		tmx = ManagementFactory.getThreadMXBean();
	}

	@Override
	public final void run()
	{
		boolean deadlock = false;
		while (!deadlock)
		{
			try
			{
				long[] ids = tmx.findDeadlockedThreads();

				if (ids != null)
				{
					deadlock = true;
					ThreadInfo[] tis = tmx.getThreadInfo(ids, true, true);
					String info = "DeadLock Found!\n";
					for (ThreadInfo ti : tis)
					{
						info += ti.toString();
					}

					for (ThreadInfo ti : tis)
					{
						LockInfo[] locks = ti.getLockedSynchronizers();
						MonitorInfo[] monitors = ti.getLockedMonitors();
						if (locks.length == 0 && monitors.length == 0)
						{
							continue;
						}

						ThreadInfo dl = ti;
						info += "Java-level deadlock:\n";
						info += "\t" + dl.getThreadName() + " is waiting to lock " + dl.getLockInfo().toString()
								+ " which is held by " + dl.getLockOwnerName() + "\n";
						while ((dl = tmx.getThreadInfo(new long[]
						{ dl.getLockOwnerId() }, true, true)[0]).getThreadId() != ti.getThreadId())
						{
							info += "\t" + dl.getThreadName() + " is waiting to lock " + dl.getLockInfo().toString()
									+ " which is held by " + dl.getLockOwnerName() + "\n";
						}
					}
					log.warn(info);

					// TODO 死锁发生时的处理机制
				}
				Thread.sleep(sleepTime);
			} catch (Exception e)
			{
				log.warn("DeadLockDetector: ", e);
			}
		}
	}
}
