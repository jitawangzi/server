package cn.game.core.execute;

import java.util.concurrent.ConcurrentMap;

/**

邮箱管理器接口，定义邮箱分配和管理的策略
*/
public interface MailboxManager {

	/**
	
	获取或创建邮箱
	@param entityId 实体ID
	@return 邮箱
	*/
	ActorMailbox getOrCreateMailbox(long entityId);

	/**
	 * 按实际邮箱键协调获取和入队，业务执行不得进入协调区域。
	 * @param entityId 业务实体 ID，仅在本次提交开始时映射一次
	 * @param task 要受理的任务
	 * @param fast 是否插入队列头部
	 * @return 已入队的邮箱；容量不足时返回 null
	 */
	ActorMailbox enqueueTask(long entityId, TaskWrapper<?> task, boolean fast);

	/**
	 * 仅移除仍为当前实例的空闲邮箱，不再次映射实际邮箱 ID。
	 * @param mailbox 清理扫描捕获的实例
	 * @param idleTimeoutMs 空闲门槛，负数表示不检查时长
	 * @return 是否成功退役该实例
	 */
	boolean removeMailbox(ActorMailbox mailbox, long idleTimeoutMs);

	/**
	
	获取邮箱
	@param entityId 实体ID
	@return 邮箱，如果不存在返回null
	*/
	ActorMailbox getMailbox(long entityId);

	/**
	
	移除邮箱
	@param entityId 实体ID
	@return 如果邮箱存在并被移除返回true
	*/
	boolean removeMailbox(long entityId);

	/**
	
	获取所有邮箱
	@return 邮箱映射
	*/
	ConcurrentMap<Long, ActorMailbox> getMailboxes();

	/**
	
	清理空闲邮箱
	@param idleTimeoutMs 空闲超时时间（毫秒）
	@return 清理的邮箱数量
	*/
	int cleanIdleMailboxes(long idleTimeoutMs);

	/**
	
	关闭邮箱管理器
	*/
	void close();
}
