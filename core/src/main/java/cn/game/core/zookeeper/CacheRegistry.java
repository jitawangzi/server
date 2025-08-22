package cn.game.core.zookeeper;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 多缓存实例的统一注册与生命周期管理。
 * - 支持链式注册：register(name, cache)
 * - 统一启动与预热：startAllAndWarmup()
 * - 统一关闭：close()
 */
@Deprecated
public class CacheRegistry implements Closeable {

    private final Map<ZkCacheType, ZkBackedCache<?, ?>> caches = new LinkedHashMap<>();

    public CacheRegistry register(ZkCacheType type, ZkBackedCache<?, ?> cache) {
        Objects.requireNonNull(type, "name");
        Objects.requireNonNull(cache, "cache");
        caches.put(type, cache);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <K, T> ZkBackedCache<K, T> get(ZkCacheType type) {
        return (ZkBackedCache<K, T>) caches.get(type);
    }

    public void startAllAndWarmup() throws Exception {
        for (ZkBackedCache<?, ?> c : caches.values()) {
            c.startAndWarmup();
        }
    }

    @Override
    public void close() {
        for (ZkBackedCache<?, ?> c : caches.values()) {
            try { c.close(); } catch (Exception ignore) {}
        }
        caches.clear();
    }
}

