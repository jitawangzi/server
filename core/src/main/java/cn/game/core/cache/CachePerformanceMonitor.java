package cn.game.core.cache;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachePerformanceMonitor {
    private final SimpleCacheManager cacheManager;
    private final ScheduledExecutorService scheduler;
    private static final Logger logger = LoggerFactory.getLogger(CachePerformanceMonitor.class);
    
    public CachePerformanceMonitor() {
        this.cacheManager = SimpleCacheManager.getInstance();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * 启动性能监控
     */
    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                var stats = cacheManager.getCache().stats();
                
                // 记录关键性能指标
                logger.info("Cache Performance - Size:{}, Hit:{:.1f}%, Load:{:.1f}ms, Fail:{:.1f}%, Evictions:{}", 
                        cacheManager.getCacheSize(),
                        stats.hitRate() * 100,
                        stats.averageLoadPenalty() / 1_000_000.0,
                        stats.loadFailureRate() * 100,
                        stats.evictionCount());
                        
                // 性能告警
                if (stats.hitRate() < 0.7) {
                    logger.warn("🚨 Cache hit rate is low: {:.1f}%", stats.hitRate() * 100);
                }
                
                if (stats.averageLoadPenalty() / 1_000_000.0 > 500) {
                    logger.warn("🚨 Average load time is high: {:.1f}ms", 
                            stats.averageLoadPenalty() / 1_000_000.0);
                }
                
                if (stats.loadFailureRate() > 0.1) {
                    logger.warn("🚨 Load failure rate is high: {:.1f}%", 
                            stats.loadFailureRate() * 100);
                }
                
                // 容量告警
                long cacheSize = cacheManager.getCacheSize();
                if (cacheSize > 40000) {
                    logger.warn("🚨 Cache size is large: {}", cacheSize);
                }
                
            } catch (Exception e) {
                logger.error("缓存监控异常", e);
            }
        }, 1, 5, TimeUnit.MINUTES);
    }
    
    public void stopMonitoring() {
        scheduler.shutdown();
    }
}