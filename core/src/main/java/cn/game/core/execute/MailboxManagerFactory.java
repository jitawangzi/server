package cn.game.core.execute;

/**

邮箱管理器工厂，用于创建不同类型的邮箱管理器
*/
public class MailboxManagerFactory {

	/**
	
	创建一对一邮箱管理器，每个ID一个邮箱
	@param maxQueueSize 最大队列大小
	@return 一对一邮箱管理器
	*/
	public static MailboxManager createOneToOneManager(int maxQueueSize) {
		return new OneToOneMailboxManager(maxQueueSize);
	}

	/**
	
	创建共享邮箱管理器，多个ID共享一个邮箱
	@param maxQueueSize 最大队列大小
	@param bucketCount 邮箱数量（桶数）
	@return 共享邮箱管理器
	*/
	public static MailboxManager createSharedManager(int maxQueueSize, int bucketCount) {
		return new SharedMailboxManager(maxQueueSize, bucketCount);
	}

	/**
	
	创建自定义共享邮箱管理器，提供自定义邮箱映射算法
	@param maxQueueSize 最大队列大小
	@param bucketCount 邮箱数量（桶数）
	@param mappingStrategy 自定义ID映射策略
	@return 自定义共享邮箱管理器
	*/
	public static MailboxManager createCustomSharedManager(int maxQueueSize, int bucketCount, IdMappingStrategy mappingStrategy) {
		return new CustomSharedMailboxManager(maxQueueSize, bucketCount, mappingStrategy);
	}

	/**
	
	ID映射策略接口，定义实体ID到邮箱ID的映射方法
	*/
	@FunctionalInterface
	public interface IdMappingStrategy {

		/** 
		 * 
		将实体ID映射到邮箱ID
		@param entityId 实体ID
		@param bucketCount 总桶数
		@return 邮箱ID
		 */
		long mapToMailboxId(long entityId, int bucketCount);
	}

	/**
	
	自定义共享邮箱管理器，允许指定ID映射策略
	*/
	private static class CustomSharedMailboxManager extends SharedMailboxManager {
		private final IdMappingStrategy mappingStrategy;

		public CustomSharedMailboxManager(int maxQueueSize, int bucketCount, IdMappingStrategy mappingStrategy) {
			super(maxQueueSize, bucketCount);
			this.mappingStrategy = mappingStrategy;
		}

		@Override
		protected long mapToMailboxId(long entityId) {
// 特殊情况：entityId==0 保持独立邮箱
			if (entityId == 0) {
				return 0L;
			}

			// 使用自定义映射策略
			return mappingStrategy.mapToMailboxId(entityId, getBucketCount());
		}
	}
}
