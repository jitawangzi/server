package cn.game.core.execute;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* SharedMailboxManager
* 
* 共享邮箱管理器，允许多个实体ID共享同一个邮箱
* 通过将实体ID取模映射到桶（邮箱）中，减少邮箱数量
* 适用于需要高并发和资源节省的场景
* 
* 似乎在数据量不大的情况下优化的作用不明显，10w个邮箱对象，也就50M左右内存
* 
*/
public class SharedMailboxManager extends OneToOneMailboxManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SharedMailboxManager.class);

	// 配置
	private final int bucketCount;

	/**
	
	创建共享邮箱管理器
	@param maxQueueSize 每个邮箱的最大队列大小
	@param bucketCount 邮箱数量（桶数）
	*/
	public SharedMailboxManager(int maxQueueSize, int bucketCount) {
		super(maxQueueSize);
		this.bucketCount = Math.max(1, bucketCount); // 至少1个桶
		LOGGER.info("Created SharedMailboxManager with {} buckets", this.bucketCount);
	}

	/**
	
	将实体ID映射到邮箱ID
	重写父类方法，实现多个实体ID映射到同一个邮箱
	@param entityId 实体ID
	@return 邮箱ID
	*/
	@Override
	protected long mapToMailboxId(long entityId) {
		if (entityId == 0) {
			return mapToMailboxId0();
		}
		// 其他ID映射到共享邮箱
		return Math.abs(entityId % bucketCount);
	}

	/**
	
	获取总邮箱数量
	@return 邮箱数量
	*/
	public int getBucketCount() {
		return bucketCount;
	}
}
