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
