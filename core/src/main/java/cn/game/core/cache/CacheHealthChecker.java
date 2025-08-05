package cn.game.core.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 缓存健康检查
 */
public class CacheHealthChecker {
    private final SimpleCacheManager cacheManager;
    private static final Logger logger = LoggerFactory.getLogger(CacheHealthChecker.class);
    
    // 健康阈值
    private static final double MIN_HIT_RATE = 0.8;           // 最低命中率
    private static final double MAX_LOAD_TIME_MS = 500.0;     // 最大平均加载时间
    private static final double MAX_FAILURE_RATE = 0.1;       // 最大失败率
    private static final long MAX_CACHE_SIZE = 40000;         // 最大缓存大小
    
    public CacheHealthChecker() {
        this.cacheManager = SimpleCacheManager.getInstance();
    }
    
    /**
     * 执行健康检查
     * @return true表示健康，false表示有问题
     */
    public boolean checkHealth() {
        var stats = cacheManager.getCache().stats();
        boolean healthy = true;
        
        // 检查命中率
        if (stats.requestCount() > 100 && stats.hitRate() < MIN_HIT_RATE) {
            logger.warn("缓存命中率过低: {:.2f}% (最低要求: {:.1f}%)", 
                    stats.hitRate() * 100, MIN_HIT_RATE * 100);
            healthy = false;
        }
        
        // 检查平均加载时间
        double avgLoadTimeMs = stats.averageLoadPenalty() / 1_000_000.0;
        if (stats.loadCount() > 10 && avgLoadTimeMs > MAX_LOAD_TIME_MS) {
            logger.warn("平均加载时间过长: {:.2f}ms (最大允许: {:.0f}ms)", 
                    avgLoadTimeMs, MAX_LOAD_TIME_MS);
            healthy = false;
        }
        
        // 检查失败率
        if (stats.loadCount() > 10 && stats.loadFailureRate() > MAX_FAILURE_RATE) {
            logger.warn("加载失败率过高: {:.2f}% (最大允许: {:.1f}%)", 
                    stats.loadFailureRate() * 100, MAX_FAILURE_RATE * 100);
            healthy = false;
        }
        
        // 检查缓存大小
        long cacheSize = cacheManager.getCacheSize();
        if (cacheSize > MAX_CACHE_SIZE) {
            logger.warn("缓存大小过大: {} (最大建议: {})", cacheSize, MAX_CACHE_SIZE);
            healthy = false;
        }
        
        if (healthy) {
            logger.info("缓存健康检查通过 ✅");
        } else {
            logger.warn("缓存健康检查发现问题 ❌");
        }
        
        return healthy;
    }
    
    /**
     * 获取健康状态报告
     */
    public String getHealthReport() {
        var stats = cacheManager.getCache().stats();
        
        return String.format(
            "缓存健康报告:\n" +
            "- 缓存大小: %d\n" +
            "- 命中率: %.2f%%\n" +
            "- 平均加载时间: %.2fms\n" +
            "- 加载失败率: %.2f%%\n" +
            "- 总请求数: %d\n" +
            "- 驱逐次数: %d",
            cacheManager.getCacheSize(),
            stats.hitRate() * 100,
            stats.averageLoadPenalty() / 1_000_000.0,
            stats.loadFailureRate() * 100,
            stats.requestCount(),
            stats.evictionCount()
        );
    }
}