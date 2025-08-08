package cn.game.games.core.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.cache.CacheDataType;
import cn.game.core.cache.SimpleCacheManager;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.cross.zongmen.service.ZongmenServiceInterface;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenShowInfo;
import cn.game.util.RedisUtil;

public class GameCacheService {
	private static final Logger logger = LoggerFactory.getLogger(GameCacheService.class);
	private final SimpleCacheManager cacheManager = SimpleCacheManager.getInstance();
	private static final GameCacheService instance = new GameCacheService() ; 
	private GameCacheService() {
	}
	public static GameCacheService getInstance() {
		return instance;
	}
	
	// ============ 玩家数据相关 ============

	/** 
	 * 获取玩家工会等级
	 * @param playerId
	 * @return
	 */
	public int getPlayerGuildLevel(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId); 
		return cacheManager.getPlayerData(CacheDataType.PLAYER_GUILD_LEVEL, playerId, r -> {
			if (player == null || player.getGuildId() <= 0) {
				return 0; // 玩家未加入公会
			}
			return fetchPlayeGuildLevelFromRemote(player.getGuildId());
		});
	}
	// ============ 服务器级别数据 ============

	/**
	 * 获取排行榜数据（10分钟缓存）
	 */
	public Object getRankingData(RankType rankingType) {
		return cacheManager.get(CacheDataType.RANKING_DATA, rankingType.Name, this::fetchFromRedis);
	}

	/**
	 * 获取Redis缓存数据（10分钟缓存）
	 */
	public String getRedisData(String redisKey) {
		return cacheManager.get(CacheDataType.REDIS_CACHE, redisKey, this::fetchFromRedis);
	}

	// ============ 缓存管理方法 ============

	/**
	 * 玩家数据更新后，清除相关缓存
	 */
	public void onPlayerDataUpdated(long playerId, CacheDataType... dataTypes) {
		if (dataTypes.length == 0) {
			// 清除该玩家所有缓存
			cacheManager.evictAllPlayerCache(playerId);
		} else {
			// 清除指定类型的缓存
			for (CacheDataType dataType : dataTypes) {
				cacheManager.evictPlayerData(dataType, playerId);
			}
		}
		logger.info("玩家数据更新，清除缓存: playerId={}", playerId);
	}

	/**
	 * 玩家下线，清除所有相关缓存
	 */
	public void onPlayerLogout(long playerId) {
		cacheManager.evictAllPlayerCache(playerId);
		logger.info("玩家下线，清除缓存: playerId={}", playerId);
	}

	private <T> T fetchFromRedis(String redisKey) {
		return RedisUtil.get(redisKey);
	}

	private int fetchPlayeGuildLevelFromRemote(long zongmenId) {
		if (zongmenId <= 0) {
			return 0;
		}
		ZongmenServiceInterface zongmenProxy = ServerHelper.getZongmenProxy(zongmenId);
		ZongMenShowInfo zongmenShowInfo = zongmenProxy.getZongmenShowInfo(zongmenId);
		if (zongmenShowInfo == null) {
			return 0;
		}
		return zongmenShowInfo.getSimpleInfo().getLevel();
	}

}