package cn.game.util;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class MBeanManager
{

	public static void registerMBean(Object instance, String key)
	{
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		try
		{
			mBeanServer.registerMBean(instance, new ObjectName(key));
		} catch (Exception e)
		{
		}
	}

	public static void unRegisterMBean(String key)
	{
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		try
		{
			mBeanServer.unregisterMBean(new ObjectName(key));
		} catch (Exception e)
		{
		}
	}
}
