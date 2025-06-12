package cn.game.games.net.game.init;

import cn.game.core.cache.CacheConfig;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.games.cache.id.DistributedIDManager;

public class GameIdManagerInitializer {
	public static void initialize() {
		// 注册玩家ID管理器
		IdCache.registerManager(new DistributedIDManager(DistributedObjectType.PLAYER,
				new CacheConfig(8192, cn.game.util.Config.DEFAULT_REDIS_DISTRIBUTED_OBJECT_EXPIRE_SECONDS, CacheType.PLAYER_SERVER_ID)));

		// 注册工会ID管理器
		IdCache.registerManager(new DistributedIDManager(DistributedObjectType.ZONGMEN,
				new CacheConfig(1024, cn.game.util.Config.DEFAULT_REDIS_DISTRIBUTED_OBJECT_EXPIRE_SECONDS, CacheType.ZONG_MEN_SERVER_ID)));

		// 初始化定时任务
		IdCache.init();
	}
}