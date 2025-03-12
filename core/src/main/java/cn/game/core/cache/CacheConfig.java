package cn.game.core.cache;

public class CacheConfig {
	private final int maxSize;
	private final int expireSeconds;
	private final CacheType cacheType;

	public CacheConfig(int maxSize, int expireSeconds, CacheType cacheType) {
		this.maxSize = maxSize;
		this.expireSeconds = expireSeconds;
		this.cacheType = cacheType;
	}

	public int getMaxSize() {
		return maxSize;
	}


	public int getExpireSeconds() {
		return expireSeconds;
	}

	public CacheType getCacheType() {
		return cacheType;
	}

}