package cn.game.core.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SimpleCacheManager {
	private static final Logger logger = LoggerFactory.getLogger(SimpleCacheManager.class);

	// 单个Cache实例处理所有数据
	private final Cache<String, Object> cache;

	// 单例实例
	private static volatile SimpleCacheManager instance;
	private static final Object lock = new Object();

	private SimpleCacheManager() {
		this.cache = Caffeine.newBuilder()
				.maximumSize(50000) // 总缓存条目上限
				.recordStats() // 启用统计
				.expireAfter(new DynamicTTLExpiry()) // 动态TTL
				.build();

		logger.info("Caffeine缓存管理器初始化完成，最大容量: 50000");
	}

	public static SimpleCacheManager getInstance() {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new SimpleCacheManager();
				}
			}
		}
		return instance;
	}

	/**
	 * 动态TTL策略 - 根据key前缀确定过期时间
	 */
	private static class DynamicTTLExpiry implements Expiry<String, Object> {
		@Override
		public long expireAfterCreate(String key, Object value, long currentTime) {
			return getTTLByKey(key);
		}

		@Override
		public long expireAfterUpdate(String key, Object value, long currentTime, long currentDuration) {
			return getTTLByKey(key); // 更新时重新计算TTL
		}

		@Override
		public long expireAfterRead(String key, Object value, long currentTime, long currentDuration) {
			return currentDuration; // 读取时保持原TTL
		}

		private long getTTLByKey(String key) {
			// 根据key前缀确定TTL
			for (CacheDataType dataType : CacheDataType.values()) {
				if (key.startsWith(dataType.getPrefix() + ":")) {
					return TimeUnit.SECONDS.toNanos(dataType.getTtlSeconds());
				}
			}
			// 默认TTL：10分钟
			return TimeUnit.SECONDS.toNanos(600);
		}
	}

	/**
	 * 获取缓存数据 - 核心方法
	 */
	public <T> T get(CacheDataType dataType, String key, Function<String, T> loader) {
		String fullKey = buildKey(dataType, key);

		try {
			@SuppressWarnings("unchecked")
			T result = (T) cache.get(fullKey, k -> {
				logger.debug("缓存MISS: type={}, key={}", dataType.getPrefix(), key);
				return loader.apply(key);
			});

			logger.debug("缓存操作: type={}, key={}", dataType.getPrefix(), key);
			return result;

		} catch (Exception e) {
			logger.error("缓存获取失败: type={}, key={}", dataType.getPrefix(), key, e);
			return loader.apply(key);
		}
	}

	/**
	 * 获取玩家相关数据的便捷方法
	 */
	public <T> T getPlayerData(CacheDataType dataType, long playerId, Function<Long, T> loader) {
		String key = String.valueOf(playerId);
		return get(dataType, key, k -> loader.apply(playerId));
	}

	/**
	 * 手动设置缓存
	 */
	public void put(CacheDataType dataType, String key, Object value) {
		String fullKey = buildKey(dataType, key);
		cache.put(fullKey, value);
		logger.debug("手动设置缓存: type={}, key={}", dataType.getPrefix(), key);
	}

	/**
	 * 设置玩家数据缓存的便捷方法
	 */
	public void putPlayerData(CacheDataType dataType, long playerId, Object value) {
		put(dataType, String.valueOf(playerId), value);
	}

	/**
	 * 删除特定缓存
	 */
	public void evict(CacheDataType dataType, String key) {
		String fullKey = buildKey(dataType, key);
		cache.invalidate(fullKey);
		logger.debug("删除缓存: type={}, key={}", dataType.getPrefix(), key);
	}

	/**
	 * 删除玩家特定类型缓存
	 */
	public void evictPlayerData(CacheDataType dataType, long playerId) {
		evict(dataType, String.valueOf(playerId));
	}

	/**
	 * 删除玩家所有相关缓存
	 */
	public void evictAllPlayerCache(long playerId) {
		String playerSuffix = ":" + playerId;
		cache.asMap().keySet().removeIf(key -> {
			// 检查是否是玩家相关的缓存key
			return (key.startsWith("player_") && key.endsWith(playerSuffix)) || (key.startsWith("guild_") && key.endsWith(playerSuffix));
		});
		logger.info("清除玩家所有缓存: playerId={}", playerId);
	}

	/**
	 * 删除特定类型的所有缓存
	 */
	public void evictByType(CacheDataType dataType) {
		String prefix = dataType.getPrefix() + ":";
		cache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
		logger.info("清除类型缓存: type={}", dataType.getPrefix());
	}

	/**
	 * 构建完整的缓存key
	 */
	private String buildKey(CacheDataType dataType, String key) {
		return dataType.getPrefix() + ":" + key;
	}

	/**
	 * 获取缓存统计信息
	 */
	public void printCacheStats() {
		var stats = cache.stats();
		logger.info("=== 缓存统计信息 ===");
		logger.info("缓存大小: {}", cache.estimatedSize());
		logger.info("命中率: {:.2f}%", stats.hitRate() * 100);
		logger.info("命中次数: {}", stats.hitCount());
		logger.info("未命中次数: {}", stats.missCount());
		logger.info("未命中率: {:.2f}%", stats.missRate() * 100);
		logger.info("总请求次数: {}", stats.requestCount());
		logger.info("加载次数: {}", stats.loadCount());
		logger.info("加载成功次数: {}", stats.loadSuccessCount());
		logger.info("加载失败次数: {}", stats.loadFailureCount());
		logger.info("加载失败率: {:.2f}%", stats.loadFailureRate() * 100);
		logger.info("平均加载时间: {:.2f}ms", stats.averageLoadPenalty() / 1_000_000.0);
		logger.info("总加载时间: {:.2f}ms", stats.totalLoadTime() / 1_000_000.0);
		logger.info("驱逐次数: {}", stats.evictionCount());
		logger.info("驱逐权重: {}", stats.evictionWeight());

		// 告警检查
		if (stats.hitRate() < 0.8) {
			logger.warn("⚠️  命中率过低，建议检查缓存策略");
		}
		if (stats.loadFailureRate() > 0.1) {
			logger.warn("⚠️  加载失败率过高: {:.2f}%", stats.loadFailureRate() * 100);
		}
	}

	/**
	 * 获取详细的缓存统计信息（修正版）
	 */
	public void printDetailedCacheStats() {
		var stats = cache.stats();

		logger.info("=== 详细缓存统计信息 ===");

		// 基础信息
		logger.info("缓存大小: {}", cache.estimatedSize());

		// 命中统计
		logger.info("--- 命中统计 ---");
		logger.info("总请求次数: {}", stats.requestCount());
		logger.info("命中次数: {}", stats.hitCount());
		logger.info("未命中次数: {}", stats.missCount());
		logger.info("命中率: {:.2f}%", stats.hitRate() * 100);
		logger.info("未命中率: {:.2f}%", stats.missRate() * 100);

		// 加载统计
		logger.info("--- 加载统计 ---");
		logger.info("总加载次数: {}", stats.loadCount());
		logger.info("加载成功次数: {}", stats.loadSuccessCount());
		logger.info("加载失败次数: {}", stats.loadFailureCount());
		logger.info("加载失败率: {:.2f}%", stats.loadFailureRate() * 100);
		if (stats.loadCount() > 0) {
			logger.info("加载成功率: {:.2f}%", (double) stats.loadSuccessCount() / stats.loadCount() * 100);
		}

		// 时间统计
		logger.info("--- 时间统计 ---");
		logger.info("总加载时间: {:.2f}ms", stats.totalLoadTime() / 1_000_000.0);
		logger.info("平均加载时间: {:.2f}ms", stats.averageLoadPenalty() / 1_000_000.0);

		// 驱逐统计
		logger.info("--- 驱逐统计 ---");
		logger.info("驱逐次数: {}", stats.evictionCount());
		logger.info("驱逐权重: {}", stats.evictionWeight());

		// 性能分析
		logger.info("--- 性能分析 ---");
		if (stats.requestCount() > 0) {
			// 计算每个请求的平均处理时间（包括缓存命中和加载）
			double avgResponseTime = stats.averageLoadPenalty() / 1_000_000.0 * stats.missRate();
			logger.info("平均响应时间: {:.2f}ms", avgResponseTime);
		}

		// 缓存效率分析
		if (stats.requestCount() > 100) { // 至少有100个请求才进行分析
			logger.info("--- 缓存效率分析 ---");
			logger.info("缓存节省时间: {:.2f}ms", stats.hitCount() * stats.averageLoadPenalty() / 1_000_000.0);
			logger.info("每命中节省: {:.2f}ms", stats.averageLoadPenalty() / 1_000_000.0);
		}

		// 告警检查
		if (stats.hitRate() < 0.8) {
			logger.warn("⚠️  命中率过低: {:.2f}%，建议检查缓存策略", stats.hitRate() * 100);
		}
		if (stats.loadFailureRate() > 0.1) {
			logger.warn("⚠️  加载失败率过高: {:.2f}%", stats.loadFailureRate() * 100);
		}
		if (stats.averageLoadPenalty() / 1_000_000.0 > 500) {
			logger.warn("⚠️  平均加载时间过长: {:.2f}ms", stats.averageLoadPenalty() / 1_000_000.0);
		}
	}

	/**
	 * 简化的缓存统计信息
	 */
	public void printSimpleCacheStats() {
		var stats = cache.stats();
		logger.info("缓存统计 - 大小:{}, 命中率:{:.1f}%, 平均加载:{:.1f}ms, 失败率:{:.1f}%", cache.estimatedSize(), stats.hitRate() * 100,
				stats.averageLoadPenalty() / 1_000_000.0, stats.loadFailureRate() * 100);
	}

	/**
	 * 获取缓存大小
	 */
	public long getCacheSize() {
		return cache.estimatedSize();
	}

	/**
	 * 清空所有缓存
	 */
	public void clear() {
		cache.invalidateAll();
		logger.info("清空所有缓存");
	}

	public Cache<String, Object> getCache() {
		return cache;
	}
	
}