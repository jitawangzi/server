package cn.game.core.cache;

public enum CacheDataType {
	// 玩家相关数据 - 短TTL，保证更高的新鲜度

	// 公会相关数据(示例) - 中等TTL
	GUILD_MEMBERS("guild_members", 180, 50_000, CacheBackend.REMOTE, false),

	//已经开启的Game服务器列表
	SERVER_OPEN_LIST("server_open_list", 10, 1024, CacheBackend.OTHER, false),
	SERVER_OPEN_LATEST("server_open_latest", 5, 1, CacheBackend.OTHER, false),
	
	// 服务器级别数据(示例) - 较长TTL
	RANKING_DATA("ranking", 600, 20_000, CacheBackend.REDIS, false),

	GUILD_SIMPLE_INFO("guild_simple_info", 30, 50_000, CacheBackend.REMOTE, false),
	
	GUILD_NAME("guild_name", 60, 50_000, CacheBackend.REMOTE, false),
	
	// 直接使用 Redis 的通用数据 - 统一通过 REDIS 访问
	REDIS_CACHE("redis_cache", 600, 100_000, CacheBackend.REDIS, false);

	private final String prefix;
	private final long ttlSeconds;
	private final long maximumSize;
	private final CacheBackend backend;
	private final boolean playerScoped;

	CacheDataType(String prefix, long ttlSeconds, long maximumSize, CacheBackend backend, boolean playerScoped) {
		this.prefix = prefix;
		this.ttlSeconds = ttlSeconds;
		this.maximumSize = maximumSize;
		this.backend = backend;
		this.playerScoped = playerScoped;
	}

	public String getPrefix() {
		return prefix;
	}

	public long getTtlSeconds() {
		return ttlSeconds;
	}

	public long getMaximumSize() {
		return maximumSize;
	}

	public CacheBackend getBackend() {
		return backend;
	}

	public boolean isPlayerScoped() {
		return playerScoped;
	}

	public enum CacheBackend {
		REDIS,
		REMOTE,   // 跨服/远端服务
		MIXED ,    // 既可能来自 Redis，也可能来自其他服务（可据业务自定义）
		OTHER,   // 
		;
	}
}