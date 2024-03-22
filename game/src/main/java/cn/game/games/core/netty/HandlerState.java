package cn.game.games.core.netty;

import java.util.concurrent.atomic.AtomicLong;

import io.netty.handler.traffic.TrafficCounter;

public class HandlerState implements HandlerStateMBean
{

	private TrafficCounter trafficCounter = WebSocketServerInitializer.trafficCounter;
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

	private long totalChannelActive;
	private long totalChannelInactive;
	private long totalChannelRegistered;
	private long totalChannelUnregistered;

	/**
	 * @return the totalMessagesReceived
	 */
	@Override
	public long getTotalPacketReceived()
	{
		return totalPacketReceived;
	}

	/**
	 * @return the totalMessageSend
	 */
	@Override
	public long getTotalPacketSend()
	{
		return totalPacketSend;
	}

	/**
	 * @return the totalExceptionCaught
	 */
	@Override
	public long getTotalExceptionCaught()
	{
		return totalExceptionCaught;
	}

	/**
	 * @return the totalReadIdle
	 */
	@Override
	public long getTotalReadIdle()
	{
		return totalReadIdle;
	}

	/**
	 * @return the totalWriteIdle
	 */
	@Override
	public long getTotalWriteIdle()
	{
		return totalWriteIdle;
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

	public void addTotalChannelActive()
	{
		this.totalChannelActive++;
	}

	public void addTotalChannelInactive()
	{
		this.totalChannelInactive++;
	}
	public void addTotalChannelRegistered() {
		this.totalChannelRegistered++;
	}
	public void addTotalChannelUnregistered() {
		this.totalChannelUnregistered++;
	}

	@Override
	public long lastReadBytes()
	{
		return trafficCounter.lastReadBytes();
	}

	@Override
	public long lastCumulativeTime()
	{
		return trafficCounter.lastCumulativeTime();
	}

	@Override
	public long lastReadThroughput()
	{
		return trafficCounter.lastReadThroughput();
	}

	@Override
	public double lastTime()
	{
		return trafficCounter.lastTime();
	}

	@Override
	public long lastWriteThroughput()
	{
		return trafficCounter.lastWriteThroughput();
	}

	@Override
	public double lastWrittenBytes()
	{
		return trafficCounter.lastWrittenBytes();
	}

	@Override
	public long getRealWriteThroughput()
	{
		return trafficCounter.getRealWriteThroughput();
	}

	@Override
	public AtomicLong getRealWrittenBytes()
	{
		return trafficCounter.getRealWrittenBytes();
	}

	@Override
	public long cumulativeReadBytes()
	{
		return trafficCounter.cumulativeReadBytes();
	}

	@Override
	public long cumulativeWrittenBytes()
	{
		return trafficCounter.cumulativeWrittenBytes();
	}

	@Override
	public long currentReadBytes()
	{
		return trafficCounter.currentReadBytes();
	}

	@Override
	public long currentWrittenBytes()
	{
		return trafficCounter.currentWrittenBytes();
	}


}
