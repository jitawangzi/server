package cn.game.games.core.mina;

import org.apache.mina.core.service.IoServiceStatistics;


public class HandlerState implements HandlerStateMBean
{

	private IoServiceStatistics	ioServiceStatistics;

	/**
	 * 总接收信息数
	 */
	private long				totalPacketReceived;

	/**
	 * 总发送信息数
	 */
	private long				totalPacketSend;

	/**
	 * 总异常数
	 */
	private long				totalExceptionCaught;

	/**
	 * 总读取空闲等待超时次数
	 */
	private long				totalReadIdle;

	/**
	 * 总写出信息空闲等待超时次数
	 */
	private long				totalWriteIdle;

	/**
	 * 总创建通信会话次数
	 */
	private long				totalCreateSession;

	/**
	 * 总打开通信会话次数
	 */
	private long				totalOpendSession;

	/**
	 * 总关闭通信会话次数
	 */
	private long				totalCloseSession;

	/**
	 * @return the totalMessagesReceived
	 */
	public long getTotalPacketReceived()
	{
		return totalPacketReceived;
	}

	/**
	 * @return the totalMessageSend
	 */
	public long getTotalPacketSend()
	{
		return totalPacketSend;
	}

	/**
	 * @return the totalExceptionCaught
	 */
	public long getTotalExceptionCaught()
	{
		return totalExceptionCaught;
	}

	/**
	 * @return the totalReadIdle
	 */
	public long getTotalReadIdle()
	{
		return totalReadIdle;
	}

	/**
	 * @return the totalWriteIdle
	 */
	public long getTotalWriteIdle()
	{
		return totalWriteIdle;
	}

	/**
	 * @return the totalCreateSession
	 */
	public long getTotalCreateSession()
	{
		return totalCreateSession;
	}

	/**
	 * @return the totalOpendSession
	 */
	public long getTotalOpendSession()
	{
		return totalOpendSession;
	}

	/**
	 * @return the totalCloseSession
	 */
	public long getTotalCloseSession()
	{
		return totalCloseSession;
	}

	/**
	 * 接收的数据包的总数
	 */
	public void addTotalPacketReceived()
	{
		this.totalPacketReceived++;
	}

	/**
	 * 发送出数据包的总数
	 */
	public void addTotalPacketSend()
	{
		this.totalPacketSend++;
	}

	/**
	 * 发生的异常总数
	 */
	public void addTotalExceptionCaught()
	{
		this.totalExceptionCaught++;
	}

	/**
	 * 写超时总数
	 */
	public void addTotalReadIdle()
	{
		this.totalReadIdle++;
	}

	/**
	 * 发生的写超时总数
	 */
	public void addTotalWriteIdle()
	{
		this.totalWriteIdle++;
	}

	/**
	 * 创建的session总数
	 */
	public void addTotalCreateSession()
	{
		this.totalCreateSession++;
	}

	/**
	 * 打开的session总数
	 */
	public void addTotalOpendSession()
	{
		this.totalOpendSession++;
	}

	/**
	 * 关闭的session总数
	 */
	public void addTotalCloseSession()
	{
		this.totalCloseSession++;
	}

	public IoServiceStatistics getIoServiceStatistics()
	{
		if (ioServiceStatistics == null)
		{
			ioServiceStatistics = NioAcceptorFactoryBean.acceptor != null ? NioAcceptorFactoryBean.acceptor
					.getStatistics() : null;
		}
		return ioServiceStatistics;
	}

	@Override
	public long getCumulativeManagedSessionCount()
	{
		return getIoServiceStatistics().getCumulativeManagedSessionCount();
	}

	@Override
	public int getLargestManagedSessionCount()
	{
		return getIoServiceStatistics().getLargestManagedSessionCount();
	}

	@Override
	public double getLargestReadBytesThroughput()
	{
		return getIoServiceStatistics().getLargestReadBytesThroughput();
	}

	@Override
	public double getLargestReadMessagesThroughput()
	{
		return getIoServiceStatistics().getLargestReadMessagesThroughput();
	}

	@Override
	public double getLargestWrittenBytesThroughput()
	{
		return getIoServiceStatistics().getLargestWrittenBytesThroughput();
	}

	@Override
	public double getLargestWrittenMessagesThroughput()
	{
		return getIoServiceStatistics().getLargestWrittenMessagesThroughput();
	}

	@Override
	public long getLastIoTime()
	{
		return getIoServiceStatistics().getLastIoTime();
	}

	@Override
	public long getLastReadTime()
	{
		return getIoServiceStatistics().getLastReadTime();
	}

	@Override
	public long getLastWriteTime()
	{
		return getIoServiceStatistics().getLastWriteTime();
	}

	@Override
	public long getReadBytes()
	{
		return getIoServiceStatistics().getReadBytes();
	}

	@Override
	public double getReadBytesThroughput()
	{
		return getIoServiceStatistics().getReadBytesThroughput();
	}

	@Override
	public long getReadMessages()
	{
		return getIoServiceStatistics().getReadMessages();
	}

	@Override
	public double getReadMessagesThroughput()
	{
		return getIoServiceStatistics().getReadMessagesThroughput();
	}

	@Override
	public int getScheduledWriteBytes()
	{
		return getIoServiceStatistics().getScheduledWriteBytes();
	}

	@Override
	public int getScheduledWriteMessages()
	{
		return getIoServiceStatistics().getScheduledWriteMessages();
	}

	@Override
	public int getThroughputCalculationInterval()
	{
		return getIoServiceStatistics().getThroughputCalculationInterval();
	}

	@Override
	public long getThroughputCalculationIntervalInMillis()
	{
		return getIoServiceStatistics().getThroughputCalculationIntervalInMillis();
	}

	@Override
	public long getWrittenBytes()
	{
		return getIoServiceStatistics().getWrittenBytes();
	}

	@Override
	public double getWrittenBytesThroughput()
	{
		return getIoServiceStatistics().getWrittenBytesThroughput();
	}

	@Override
	public long getWrittenMessages()
	{
		return getIoServiceStatistics().getWrittenMessages();
	}

	@Override
	public double getWrittenMessagesThroughput()
	{
		return getIoServiceStatistics().getWrittenMessagesThroughput();
	}

}
