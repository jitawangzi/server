package cn.game.games.core.netty;

import java.util.concurrent.atomic.AtomicLong;

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

	public long lastReadBytes();

	public long lastCumulativeTime();

	public long lastReadThroughput();

	public double lastTime();

	public long lastWriteThroughput();

	public double lastWrittenBytes();

	public long getRealWriteThroughput();

	public AtomicLong getRealWrittenBytes();

	public long cumulativeReadBytes();

	public long cumulativeWrittenBytes();

	public long currentReadBytes();

	public long currentWrittenBytes();

}
