package cn.game.games.core.cache;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.cache.CacheDataType;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.SimpleCacheManager;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.cross.guild.service.GuildServiceInterface;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.protobuf.GuildMsg.GuildShowInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildSimpleInfo;
import cn.game.util.RedisUtil;
import cn.game.util.ServerType;
import io.vertx.core.Future;
import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 游戏侧缓存门面：集中注册各 CacheDataType 的默认 loader，
 * 对外提供简化易用的 API。读取以显示为主、弱一致性（TTL 内允许旧值）。
 */
public class GameCacheService {
	private static final Logger log = LoggerFactory.getLogger(GameCacheService.class);

	private static final GameCacheService INSTANCE = new GameCacheService();
	private final SimpleCacheManager cache = SimpleCacheManager.getInstance();

	private GameCacheService() {
		registerLoaders();
	}

	public static GameCacheService getInstance() {
		return INSTANCE;
	}

	private void registerLoaders() {
		// 玩家工会等级：远端服务
		cache.registerLoader(CacheDataType.GUILD_SIMPLE_INFO,
				(key) -> CompletableFuture.supplyAsync(() -> fetchGuildSimpleInfo(Long.parseLong(key))), null // 可在将来增加跨服批量 RPC
		);
		cache.registerLoader(CacheDataType.GUILD_NAME,
				(key) -> CompletableFuture.supplyAsync(() -> fetchGuildName(Long.parseLong(key))), null
				);

		cache.registerLoader(CacheDataType.SERVER_OPEN_LIST,
				(key) -> CompletableFuture.supplyAsync(() -> ServerContext.getInstance().getValidGameService().getValidServers()), null);
		cache.registerLoader(CacheDataType.SERVER_OPEN_LATEST,
				(key) -> CompletableFuture.supplyAsync(() -> {
					VirtualServerView retServerView= null; 
					Map<String, VirtualServerView> validServers = ServerContext.getInstance().getValidGameService().getValidServers(); 
					if (validServers != null && validServers.size() > 0) {
						List<VirtualServerView> list = validServers.values()
								.stream()
								.sorted((a, b) -> b.getOpenTime().compareTo(a.getOpenTime()))
								.collect(Collectors.toList());
						retServerView = list.get(0);
					}
					return retServerView;
				}), null);

		//排行榜数据：存于 Redis（这里覆盖默认，演示 RBatch 批量）
		cache.registerLoader(CacheDataType.RANKING_DATA, (key) -> CompletableFuture.supplyAsync(() -> RedisUtil.get(key)), (keys) -> {
			var redisson = RedisUtil.getRedis();
			RBatch batch = redisson.createBatch();
			Map<String, RFuture<Object>> rf = new LinkedHashMap<>();
			for (String k : keys) {
				rf.put(k, batch.getBucket(k).getAsync());
			}
			return batch.executeAsync().toCompletableFuture().thenApply(br -> {
				Map<String, Object> m = new LinkedHashMap<>();
				rf.forEach((k, f) -> m.put(k, f.getNow()));
				return m;
			});
		});

		// 4) REDIS_CACHE：SimpleCacheManager 已内置默认 loader，无需重复注册
	}

	// ========================= 玩家相关便捷 API =========================

	public int getPlayerGuildLevel(long playerId) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		if (player == null || player.getGuildId() <= 0) {
			return 0;
		}
		String key = String.valueOf(player.getGuildId());
		GuildSimpleInfo guildSimpleInfo = cache.get(CacheDataType.GUILD_SIMPLE_INFO, key);
		return guildSimpleInfo == null ? 0 : guildSimpleInfo.getLevel();
	}
	public String getGuildName(long guildId) {
		String key = String.valueOf(guildId);
		String name = cache.get(CacheDataType.GUILD_NAME, key);
		return name == null ? "" : name;
	}


	// ========================= 服务器级别数据（排行/Redis） =========================

	public GuildSimpleInfo getGuildSimpleInfo(long guildId) {
		if (guildId<=0) {
			return null ; 
		}
		String key = String.valueOf(guildId);
		GuildSimpleInfo guildSimpleInfo = cache.get(CacheDataType.GUILD_SIMPLE_INFO, key);
		return guildSimpleInfo;
	}

	
	public Map<String, VirtualServerView> getOpenServerMap() {
		return cache.get(CacheDataType.SERVER_OPEN_LIST, CacheDataType.SERVER_OPEN_LIST.name());
	}
	public VirtualServerView getOpenServerLatest() {
		return cache.get(CacheDataType.SERVER_OPEN_LATEST, CacheDataType.SERVER_OPEN_LATEST.name());
	}
	public Object getRankingData(RankType rankingType) {
		String redisKey = rankingType.Name;
		return cache.get(CacheDataType.RANKING_DATA, redisKey, RedisUtil::get);
	}

	public Future<Object> getRankingDataAsync(RankType rankingType) {
		String redisKey = rankingType.Name;
		return cache.getAsync(CacheDataType.RANKING_DATA, redisKey, RedisUtil::get);
	}

	public String getRedisData(String redisKey) {
		return cache.get(CacheDataType.REDIS_CACHE, redisKey, RedisUtil::get);
	}

	public Future<String> getRedisDataAsync(String redisKey) {
		return cache.getAsync(CacheDataType.REDIS_CACHE, redisKey, RedisUtil::get);
	}

	public <T> void putRedisData(String redisKey, T value) {
		cache.put(CacheDataType.REDIS_CACHE, redisKey, value);
	}

	public <T> Future<Void> putRedisDataAsync(String redisKey, T value) {
		return cache.putAsync(CacheDataType.REDIS_CACHE, redisKey, value);
	}

	public Future<Boolean> deleteRedisKeyAsync(String redisKey) {
		return cache.deleteRemoteAsync(CacheDataType.REDIS_CACHE, redisKey);
	}

	public boolean existsRedisKey(String redisKey) {
		return cache.existsRemote(CacheDataType.REDIS_CACHE, redisKey);
	}

	// ========================= 批量 API 示例 =========================

	public <T> List<T> multiGetRedis(List<String> redisKeys) {
		return cache.multiGetList(CacheDataType.REDIS_CACHE, redisKeys, keys -> {
			// 同步批量：RBatch
			var redisson = RedisUtil.getRedis();
			RBatch batch = redisson.createBatch();
			List<RFuture<Object>> rf = new ArrayList<>();
			for (String k : keys) {
				rf.add(batch.getBucket(k).getAsync());
			}
			batch.execute();
			List<T> out = new ArrayList<>(keys.size());
			for (RFuture<Object> f : rf) {
				@SuppressWarnings("unchecked")
				T v = (T) f.getNow();
				out.add(v);
			}
			return out;
		}, RedisUtil::get);
	}
	

	/** 
	 * 可变参数的同步批量获取方法
	 * @param keys
	 * @return
	 */
	public <T> List<T> multiGetRedis(CacheType cacheType, String... keys) {
		if (keys == null || keys.length == 0) {
			return Collections.EMPTY_LIST;
		}
		List<String> list = new ArrayList<>(keys.length);
		for (String key : keys) {
			list.add(cacheType.key(key));
		}
		return multiGetRedis(list);
	}

	public <T> Future<List<T>> multiGetRedisAsync(List<String> redisKeys) {
		return cache.multiGetAsync(CacheDataType.REDIS_CACHE, redisKeys, keys -> {
			// 同步批量 loader（给缓存层异步包装）
			var redisson = RedisUtil.getRedis();
			RBatch batch = redisson.createBatch();
			Map<String, RFuture<Object>> rf = new LinkedHashMap<>();
			for (String k : keys) {
				rf.put(k, batch.getBucket(k).getAsync());
			}
			batch.execute();
			Map<String, T> out = new LinkedHashMap<>();
			for (Map.Entry<String, RFuture<Object>> e : rf.entrySet()) {
				@SuppressWarnings("unchecked")
				T v = (T) e.getValue().getNow();
				out.put(e.getKey(), v);
			}
			return out;
		}, RedisUtil::get);
	}
	
	/** 
	 * 可变参数的同步批量获取方法
	 * @param keys
	 * @return
	 */
	public <T> Future<List<T>> multiGetRedisAsync(CacheType cacheType, String... keys) {
		if (keys == null || keys.length == 0) {
			return Future.succeededFuture(Collections.emptyList());
		}
		List<String> list = new ArrayList<>(keys.length);
		for (String key : keys) {
			list.add(cacheType.key(key));
		}
		return multiGetRedisAsync(list);
	}

	// ========================= 缓存失效事件 =========================

	public void onPlayerDataUpdated(long playerId, CacheDataType... dataTypes) {
		if (dataTypes == null || dataTypes.length == 0) {
			cache.evictAllPlayerCache(playerId);
			log.info("Player data updated -> evict all caches for playerId={}", playerId);
		} else {
			for (CacheDataType type : dataTypes) {
				cache.evictPlayerData(type, playerId);
			}
			log.info("Player data updated -> evicted types={} for playerId={}", Arrays.toString(dataTypes), playerId);
		}
	}

	public void onPlayerLogout(long playerId) {
		cache.evictAllPlayerCache(playerId);
		log.info("Player logout -> evict all caches: playerId={}", playerId);
	}

	public void clearAll() {
		cache.clearAll();
	}

	public void printStats() {
		cache.printStats();
	}

	// ========================= 具体加载逻辑 =========================

	private GuildSimpleInfo fetchGuildSimpleInfo(long guildId) {
		if (guildId <= 0) {
			return null;
		}
		Set<String> serverSet = ActiveServerListManager.getInstance().getServerSet(ServerType.Cross); 
		if (serverSet.isEmpty()) {
			return null;
		}
		GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(guildId);
		GuildShowInfo info = guildProxy.getGuildShowInfo(guildId);
		if (info == null || !info.hasSimpleInfo()) {
			return null;
		}
		return info.getSimpleInfo();
	}
	private String fetchGuildName(long guildId) {
		if (guildId <= 0) {
			return null;
		}
		Set<String> serverSet = ActiveServerListManager.getInstance().getServerSet(ServerType.Cross); 
		if (serverSet.isEmpty()) {
			return null;
		}
		GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(guildId);
		String name = guildProxy.getGuildName(guildId);
		return name;
	}

	private List<Long> fetchGuildMembersFromRemote(long guildId) {
		return Collections.emptyList();
	}
}