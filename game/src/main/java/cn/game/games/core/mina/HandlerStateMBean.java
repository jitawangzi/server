package cn.game.games.core.mina;

public interface HandlerStateMBean
{
	/**
	 * @return the totalMessagesReceived
	 */
	public long getTotalPacketReceived();

	/**
	 * @return the totalMessageSend
	 */
	public long getTotalPacketSend();

	/**
	 * @return the totalExceptionCaught
	 */
	public long getTotalExceptionCaught();

	/**
	 * @return the totalReadIdle
	 */
	public long getTotalReadIdle();

	/**
	 * @return the totalWriteIdle
	 */
	public long getTotalWriteIdle();

	/**
	 * @return the totalCreateSession
	 */
	public long getTotalCreateSession();

	/**
	 * @return the totalOpendSession
	 */
	public long getTotalOpendSession();

	/**
	 * @return the totalCloseSession
	 */
	public long getTotalCloseSession();

	public long getCumulativeManagedSessionCount();

	public int getLargestManagedSessionCount();

	public double getLargestReadBytesThroughput();

	public double getLargestReadMessagesThroughput();

	public double getLargestWrittenBytesThroughput();

	public double getLargestWrittenMessagesThroughput();

	public long getLastIoTime();

	public long getLastReadTime();

	public long getLastWriteTime();

	public long getReadBytes();

	public double getReadBytesThroughput();

	public long getReadMessages();

	public double getReadMessagesThroughput();

	public int getScheduledWriteBytes();

	public int getScheduledWriteMessages();

	public int getThroughputCalculationInterval();

	public long getThroughputCalculationIntervalInMillis();

	public long getWrittenBytes();

	public double getWrittenBytesThroughput();

	public long getWrittenMessages();

	public double getWrittenMessagesThroughput();

}
