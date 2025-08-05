package cn.game.core.cache;

public enum CacheDataType {
	// 玩家相关数据 - 短TTL，保证数据一致性
	PLAYER_GUILD_LEVEL("player_guild_level", 15), // 15秒

	// 公会相关数据 - 中等TTL
	GUILD_MEMBERS("guild_members", 180), // 3分钟

	// 服务器级别数据 - 长TTL
	RANKING_DATA("ranking", 600), // 10分钟

	// Redis缓存数据
	REDIS_CACHE("redis_cache", 600); // 10分钟

	private final String prefix;
	private final long ttlSeconds;

	CacheDataType(String prefix, long ttlSeconds) {
		this.prefix = prefix;
		this.ttlSeconds = ttlSeconds;
	}

	public String getPrefix() {
		return prefix;
	}

	public long getTtlSeconds() {
		return ttlSeconds;
	}
}