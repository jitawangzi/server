package cn.game.core.cache;

public class CacheConfig {
	private final int maxSize;
	private final int expireMinutes;
	private final CacheType cacheType;

	public CacheConfig(int maxSize, int expireMinutes, CacheType cacheType) {
		this.maxSize = maxSize;
		this.expireMinutes = expireMinutes;
		this.cacheType = cacheType;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public int getExpireMinutes() {
		return expireMinutes;
	}

	public CacheType getCacheType() {
		return cacheType;
	}

}