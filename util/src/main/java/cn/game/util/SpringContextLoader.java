package cn.game.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 系统启动的主类，使用spring 的FileSystemXmlApplicationContext进行配置文件加载和解析，
 * 你可以通过设定输入 参数来指定启动时需要加载的配置文件
 */
public class SpringContextLoader extends ThreadGroup
{
	private static final Logger						log	= LoggerFactory.getLogger(SpringContextLoader.class);
	// ApplicationContext 实例，也就是spring实例
	private static FileSystemXmlApplicationContext appContext;

	public SpringContextLoader()
	{
		super("SpringContextLoader");
	}

	public static void main(final String[] args) throws Exception
	{
		Runnable addStarter = new Runnable()
		{
			@Override
			public void run()
			{
				// 在这里调用我们自己的程序的入口函数
				try
				{
					start(args);
				} catch (Throwable e)
				{
					log.error("spring 初始化异常"+Arrays.toString(args), e) ; 
					e.printStackTrace();
					System.exit(0);
				}
			}
		};
		// 把我们自己的程序当作这个线程组的一个线程来运行
		Thread thread = new Thread(new SpringContextLoader(), addStarter);
		thread.start();
		thread.join();
	}

	/**
	 * 启动服务器
	 * 
	 * @param args
	 *            运行参数,使用文件绝对路径
	 * @throws Exception
	 *             异常
	 */
	public static void start(String[] args) throws Exception {
		// 如果spring窗口实例为null，则讲明构建过程出错了，直接退出
		if (appContext != null)
			return;
		// 这里使用文件的绝对路径：
		boolean isWin = System.getProperty("os.name").toLowerCase().contains("win")  ; 
		// 这个list实于存放应用程序参数，在这里是配置文件
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].indexOf(".xml") == -1)
				continue;
			list.add("file:"+(isWin?"//":"")+  args[i]);
		}
		appContext = new FileSystemXmlApplicationContext(list.toArray(new String[0]));
		log.info("SERVER START COMPLETE. ");
	}

	/**
	 * 关闭服务器
	 * 
	 * @throws Exception
	 *             异常
	 */
	public static void close() throws Exception
	{
		if (appContext == null) return;
		appContext.close();
	}

	/**
	 * 重新启动服务器
	 * 
	 * @param args
	 *            运行参数
	 * @throws Exception
	 *             异常
	 */
	public static void restart(String[] args) throws Exception
	{
		close();
		start(args);
	}

	/**
	 * 获取Spring的配置文件，应用加载上下文对象
	 * 
	 * @return 上下文对象
	 */
	public static FileSystemXmlApplicationContext getContext()
	{
		return appContext;
	}

	/**
	 * 处理发生的异常
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e)
	{

		log.error(t.getName(), e);
	}
}
