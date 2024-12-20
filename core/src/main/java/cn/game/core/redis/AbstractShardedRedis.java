package cn.game.core.redis;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redis分片操作的基础抽象类
 */
public abstract class AbstractShardedRedis {
	protected Logger log = LoggerFactory.getLogger(this.getClass());

	protected int shardCount;
	protected final RedissonClient redisClient;
	protected final String baseKey;

	protected AbstractShardedRedis(RedissonClient redisClient, String baseKey, int shardCount) {
		this.redisClient = redisClient;
		this.baseKey = baseKey;
		this.shardCount = shardCount;
	}

	/**
	 * 根据对象计算分片键
	 *
	 * @param element 需要计算分片的对象
	 * @return 分片键名
	 */
	public String getShardKey(Object element) {
		int shard = Math.abs(element.hashCode()) % shardCount;
		return baseKey + ":" + shard;
	}

	/**
	 * 根据分片索引获取分片键
	 *
	 * @param shardIndex 分片索引
	 * @return 分片键名
	 */
	protected String getShardKey(int shardIndex) {
		return baseKey + ":" + shardIndex;
	}

	/**
	 * 获取分片数量
	 *
	 * @return 分片数量
	 */
	public int getShardCount() {
		return shardCount;
	}
}