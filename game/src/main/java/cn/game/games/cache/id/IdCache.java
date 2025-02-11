package cn.game.games.cache.id;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.game.core.cache.CacheConfig;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.GenericDistributedIDManager;

public class IdCache {
	private static final Map<DistributedObjectType, GenericDistributedIDManager> managers = new ConcurrentHashMap<>();

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
		GenericDistributedIDManager manager = getManager(DistributedObjectType.PLAYER);
		return manager.getServerId(playerId);
	}

}
