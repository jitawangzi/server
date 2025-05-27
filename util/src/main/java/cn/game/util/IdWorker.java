package cn.game.util;

/**
 * 64位ID (42(毫秒)+5(机器ID)+5(业务编码)+12(重复累加))
 * 
 * @author Polim
 */
public class IdWorker {

//	private final static long twepoch = 1608866809197L;
	private final static long twepoch = 1747977902003L;

	// 机器标识位数，一般10位 1024差不多够了
	private final static long workerIdBits = 15L;
	// 数据中心标识位数，和上面的机器标识位数一起，来区分不同的机器，看情况设置
	private final static long datacenterIdBits = 3L;
	// 机器ID最大值
	public final static long maxWorkerId = -1L ^ (-1L << workerIdBits);
	// 数据中心ID最大值
	private final static long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
	// 毫秒内自增位，每毫秒内，可以产生多少个id
	private final static long sequenceBits = 8L;
	// 机器ID偏左移位数
	private final static long workerIdShift = sequenceBits;
	// 数据中心ID左移位数
	private final static long datacenterIdShift = sequenceBits + workerIdBits;
	// 时间毫秒左移位数
	private final static long timestampLeftShift = sequenceBits + workerIdBits
			+ datacenterIdBits;

	private final static long sequenceMask = -1L ^ (-1L << sequenceBits);

	private static long lastTimestamp = -1L;

	private long sequence = 0L;
	private final long workerId;
	private final long datacenterId;

	public IdWorker(long workerId, long datacenterId) {
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(
					"worker Id can't be greater than %d or less than 0");
		}
		if (datacenterId > maxDatacenterId || datacenterId < 0) {
			throw new IllegalArgumentException(
					"datacenter Id can't be greater than %d or less than 0");
		}
		this.workerId = workerId;
		this.datacenterId = datacenterId;
	}

	public synchronized long nextId() {
		long timestamp = timeGen();
		if (timestamp < lastTimestamp) {
			try {
				throw new Exception(
						"Clock moved backwards.  Refusing to generate id for "
								+ (lastTimestamp - timestamp) + " milliseconds");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (lastTimestamp == timestamp) {
			// 当前毫秒内，则+1
			sequence = (sequence + 1) & sequenceMask;
			if (sequence == 0) {
				// 当前毫秒内计数满了，则等待下一毫秒
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
//			sequence = 0;
			// 根据时间戳奇偶性设置初始序列号
			// 奇数时间戳从1开始，偶数时间戳从0开始,使得生成的id末尾数均匀分布。否则低并发下，多为偶数
			sequence = (timestamp & 1);
		}
		lastTimestamp = timestamp;
		// ID偏移组合生成最终的ID，并返回ID
		long nextId = ((timestamp - twepoch) << timestampLeftShift)
				| (datacenterId << datacenterIdShift)
				| (workerId << workerIdShift) | sequence;

		return nextId;
	}

	private long tilNextMillis(final long lastTimestamp) {
		long timestamp = this.timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = this.timeGen();
		}
		return timestamp;
	}

	private long timeGen() {
		return System.currentTimeMillis();
	}
}
