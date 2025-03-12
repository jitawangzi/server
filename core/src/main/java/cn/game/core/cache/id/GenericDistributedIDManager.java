package cn.game.core.cache.id;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheConfig;
import cn.game.util.RedisUtil;

/**    
 * 分布式对象服务器映射管理器
 * 保存对象ID与服务器ID的映射关系
 * 一般靠时间自动过期，某些映射关系变化频繁的，需要手动更新
 * 一般只保存非本服的对象ID，本服对象ID直接返回本服ID
 * 
 * 2025年2月11日 10:01:15
 * @author SYQ
 * @param <T>
 */
public abstract class GenericDistributedIDManager {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final DistributedObjectType objectType;
	protected final Cache<Long, String> serverCache;
	protected final CacheConfig config;

	public GenericDistributedIDManager(DistributedObjectType objectType, CacheConfig cacheConfig) {
		this.objectType = objectType;
		this.config = cacheConfig;
		this.serverCache = CacheBuilder.newBuilder()
				.maximumSize(cacheConfig.getMaxSize())
				.expireAfterWrite(cacheConfig.getExpireSeconds(), TimeUnit.SECONDS)
				.build();
	}

	/** 
	 * 获取对象所在服务器ID
	 * 如果对象不存在，返回空字符串，不会每次查询redis
	 * 大多数情况下使用这个方法来查询对象所在服务器ID
	 * @param objectId  对象id
	 * @return
	 */
	public String getServerId(long objectId) {
		return getServerIdInternal(objectId, true);
	}

	/** 
	 * 获取对象所在服务器ID，只是查询，不会使用缓存
	 * @param objectId
	 * @return
	 */
	public String selectServerId(long objectId) {
		return getServerIdInternal(objectId, false);
	}

	/** 
	 * 获取对象所在服务器ID
	 * @param objectId  对象id
	 * @param useCacheIfAbsent  如果对象不存在，是否使用缓存，使用缓存则直接返回空字符串，不会再次查询redis
	 * @return
	 */
	public String getServerIdInternal(long objectId, boolean useCacheIfAbsent) {
		try {
			// 如果对象在本服务器，直接返回本服务器ID
			if (isObjectInCurrentServer(objectId)) {
				return ServerContext.getInstance().getServerId();
			}
			return serverCache.get(objectId, () -> {
				String redisKey = generateRedisKey(objectId);
				String serverId = RedisUtil.get(redisKey);
				if (serverId == null) {
					if (useCacheIfAbsent) {
						serverId = "";
					}
				}
				return serverId;
			});
		} catch (Exception e) {
			logger.error("Failed to get server id for {} with id {}", objectType, objectId, e);
			return "";
		}
	}

	/**
	 * 更新对象id->服务器id 映射关系
	 */
	public void setServerId(long objectId, String serverId) {
		serverCache.put(objectId, serverId);
	}

	/**
	 * 清除对象id->服务器id 映射关系
	 */
	public void clearServerId(long objectId) {
		serverCache.invalidate(objectId);
	}

	/**
	 * 重置缓存
	 */
	public void invalidateServerId(long objectId) {
		serverCache.invalidate(objectId);
	}

	public String generateRedisKey(long objectId) {
		return config.getCacheType().key(objectId);
	}

	public abstract boolean isObjectInCurrentServer(long objectId);

	/** 
	 * 获取所有当前进程中管理的id
	 * 一般在延长serverId时使用
	 * @return
	 */
	public abstract Collection<Long> getAllIds();
}
