package cn.game.games.core.mina;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.integration.jmx.IoServiceMBean;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import cn.game.util.MBeanManager;

/**   
 * @Description 
 * @date 2017年4月1日 上午10:30:00
 * @author SYQ
 */
public class NioAcceptorFactoryBean extends AbstractFactoryBean
{
	private static final Logger		log				= LoggerFactory.getLogger(NioAcceptorFactoryBean.class);
	/**
	 * mina的acceptor实例
	 */
	public static NioSocketAcceptor	acceptor;
	/**
	 * 逻辑处理器
	 */
	private IoHandler				handler;
	/**
	 * 线程过虑器，用于执行数转发
	 */
	private IoFilter				executorFilter	= null;

	/**
	 * 线程过虑器中开启的线程的核心数量
	 */
	private int						corePoolSize	= 10;
	/**
	 * 线程过虑器最多可开启的线程数量
	 */
	private int						maximumPoolSize	= 20;
	/**
	 * 线程空闲时间
	 */
	private int						keepAliveTime	= 5;
	/**
	 * 过虑器列表
	 */
	private Map<String, IoFilter>	filterList;
	/**
	 * socket关的配置
	 */
	private SocketSessionConfig		sessionConfig;
	/**
	 * 服务器要监听的socket列表
	 */
	private List<InetSocketAddress>	socketAddress;
	/***/
	private int						backlog;

	public NioAcceptorFactoryBean()
	{
		super();
	}

	@Override
	protected Object createInstance() throws Exception
	{

		if (acceptor == null)
		{
			// 得到acceptor，这个acceptor直接new出来
			acceptor = newAcceptor();
			acceptor.setReuseAddress(true);
		}

		if (backlog > 0)
		{
			acceptor.setBacklog(backlog);
		}

		IoServiceMBean acceptorMBean = new IoServiceMBean(acceptor);
		// 注册JMX
		MBeanManager.registerMBean(acceptorMBean, acceptor.getClass().getPackage().getName() + ":type=acceptor,name="
				+ acceptor.getClass().getSimpleName());

		if (sessionConfig != null)
		{
			// 设置会话的配置信息，这个配置从spring中注入
			SocketSessionConfig config = acceptor.getSessionConfig();
			config.setAll(sessionConfig);
		}
		// 为acceptor设置handler，handler用于接收这个acceptor接收到的数据
		if (handler != null)
		{
			acceptor.setHandler(handler);
		}
		// 装配acceptor的过虑器
		if (!(filterList == null || filterList.isEmpty()))
		{
			// 从acceptor拿到默认的过虑器列表
			DefaultIoFilterChainBuilder filterChainBuilder = acceptor.getFilterChain();
			// 取得所有从spring中注入的过虑器
			Iterator<Entry<String, IoFilter>> iterator = filterList.entrySet().iterator();
			Entry<String, IoFilter> entry = null;
			while (iterator.hasNext())
			{
				entry = iterator.next();
				// 每次都往最后加入过虑器
				filterChainBuilder.addLast(entry.getKey(), entry.getValue());
			}
		}

		if (executorFilter == null)
		{
			// 当前线程过虑器为null，则使用默认的构建方法构建一个
			acceptor.getFilterChain().addLast("executorFilter", builderExecutorFilter());
		} else
		{
			acceptor.getFilterChain().addLast("executorFilter", executorFilter);
		}

		// 根据给写的ip的个数，调用不同的bind方法
		if (!(socketAddress == null || socketAddress.isEmpty()))
		{
			// 端口的个数
			int size = socketAddress.size();
			if (size == 1)
			{
				// 如果只有一个端口，直接按正常的方法bind到相应的端口上，当前都是这种情况
				InetSocketAddress isa = socketAddress.iterator().next();
				acceptor.bind(isa);
			} else if (size == 2)
			{
				// 如果有两个端口
				Iterator<InetSocketAddress> iterator = socketAddress.iterator();
				acceptor.bind(iterator.next());
				acceptor.bind(iterator.next());
			} else
			{
				acceptor.bind(socketAddress);
			}
		} else
		{
			acceptor.bind();
		}

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			// 服务器停止或出错时放弃端口
			public void run()
			{
				log.info("关闭游戏服务器端口");
				acceptor.unbind();
			}
		});
		return acceptor;
	}

	@Override
	public Class getObjectType()
	{

		return NioSocketAcceptor.class;
	}

	public NioSocketAcceptor newAcceptor()
	{
		return new NioSocketAcceptor();
	}

	/**
	 * 构建默认的线程过虑器
	 * 
	 * @return
	 */
	private IoFilter builderExecutorFilter()
	{
		IoEventType[] eventType =
		{
		// IoEventType.EXCEPTION_CAUGHT,
		IoEventType.MESSAGE_RECEIVED,
		/*
		 * IoEventType.MESSAGE_SENT,
		 * IoEventType.SESSION_CLOSED,
		 * IoEventType.SESSION_IDLE,
		 * IoEventType.SESSION_OPENED,
		 * IoEventType.WRITE
		 */
		};
		IoFilter executorFilter = new ExecutorFilter(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
				eventType);
		return executorFilter;

	}

	public void setAcceptor(NioSocketAcceptor acceptor)
	{
		this.acceptor = acceptor;
	}

	public void setHandler(IoHandler handler)
	{
		this.handler = handler;
	}

	public void setFilterList(Map<String, IoFilter> filterList)
	{
		this.filterList = filterList;
	}

	public void setExecutorFilter(IoFilter executorFilter)
	{
		this.executorFilter = executorFilter;
	}

	public void setCorePoolSize(int corePoolSize)
	{
		this.corePoolSize = corePoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize)
	{
		this.maximumPoolSize = maximumPoolSize;
	}

	public void setKeepAliveTime(int keepAliveTime)
	{
		this.keepAliveTime = keepAliveTime;
	}

	public void setSessionConfig(SocketSessionConfig sessionConfig)
	{
		this.sessionConfig = sessionConfig;
	}

	public void setSocketAddress(List<InetSocketAddress> socketAddress)
	{
		this.socketAddress = socketAddress;
	}

	public void setBacklog(int backlog)
	{
		this.backlog = backlog;
	}

}
