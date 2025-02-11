package cn.game.games.cache.id;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheConfig;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.GenericDistributedIDManager;
import cn.game.core.task.SchedulerService;
import cn.game.util.RedisUtil;

public class IdCache {
	private static final Logger log = LoggerFactory.getLogger(IdCache.class);
	private static final Map<DistributedObjectType, GenericDistributedIDManager> managers = new ConcurrentHashMap<>();

	private static final int DEFAULT_EXPIRE_MINUTES = 10;

	static {
		// 玩家id缓存配置
		managers.put(DistributedObjectType.PLAYER,
				new DistributedIDManager(DistributedObjectType.PLAYER, new CacheConfig(8192, 10, CacheType.PLAYER_SERVER_ID)));
		// 工会id缓存配置
		managers.put(DistributedObjectType.ZONGMEN,
				new DistributedIDManager(DistributedObjectType.ZONGMEN, new CacheConfig(1024, 60, CacheType.ZONG_MEN_SERVER_ID)));
	}

	public static GenericDistributedIDManager getManager(DistributedObjectType type) {
		return managers.get(type);
	}

	/** 
	 * 辅助方法，方便获取玩家所在服务器id
	 * @param playerId
	 * @return
	 */
	public static String getPlayerServerId(long playerId) {
		GenericDistributedIDManager manager = getManager(DistributedObjectType.PLAYER);
		return manager.getServerId(playerId);
	}

	public static String getZongMenServerId(long playerId) {
		GenericDistributedIDManager manager = getManager(DistributedObjectType.ZONGMEN);
		return manager.getServerId(playerId);
	}

	/** 
	 * 延长 id--serverId 的过期时间
	 */
	public static void setAllCurrentServerId() {
		for (DistributedObjectType distributedObjectType : DistributedObjectType.values()) {
			GenericDistributedIDManager manager = getManager(distributedObjectType);
			Collection<Long> allIds = manager.getAllIds();
			for (Long id : allIds) {
				RFuture<Void> idFuture = setServerId(manager.generateRedisKey(id));
				idFuture.onComplete((v, throwable) -> {
					if (throwable != null) {
						log.error(distributedObjectType + " " + id + " setServerId error ", throwable);
					}
				});
			}
		}
	}

	public static RFuture<Void> setServerId(DistributedObjectType objectType, long id) {
		GenericDistributedIDManager manager = getManager(objectType);
		String redisKey = manager.generateRedisKey(id);
		return setServerId(redisKey);
	}

	private static RFuture<Void> setServerId(String key) {
		return RedisUtil.setAsync(key, ServerContext.getInstance().getServerId(), DEFAULT_EXPIRE_MINUTES,
				TimeUnit.MINUTES);
	}

	/**
	 * 尝试设置某个id的的服务器id
	 * @param playerId
	 * @return
	 */
	public static RFuture<Boolean> trySetServerId(DistributedObjectType objectType, long id) {
		GenericDistributedIDManager manager = getManager(objectType);
		String redisKey = manager.generateRedisKey(id);
		return RedisUtil.trySetAsync(redisKey,
				ServerContext.getInstance().getServerId(), DEFAULT_EXPIRE_MINUTES, TimeUnit.MINUTES);
	}

	public static void init() {
		SchedulerService.getInstance().scheduleWithFixedDelay(() -> {
			setAllCurrentServerId();
		}, DEFAULT_EXPIRE_MINUTES / 2, DEFAULT_EXPIRE_MINUTES / 2, TimeUnit.MINUTES);
	}

}
