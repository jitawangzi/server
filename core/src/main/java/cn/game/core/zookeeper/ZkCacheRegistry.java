package cn.game.core.zookeeper;

import java.io.Closeable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * 使用枚举键的 CacheRegistry，类型安全、IDE 友好。
 * E 必须是枚举类型。
 */
public class ZkCacheRegistry<E extends Enum<E>> implements Closeable {

    private final Map<E, ZkBackedCache<?, ?>> caches;

    public ZkCacheRegistry(Class<E> enumType) {
        this.caches = new EnumMap<>(Objects.requireNonNull(enumType, "enumType"));
    }

    public ZkCacheRegistry<E> register(E key, ZkBackedCache<?, ?> cache) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(cache, "cache");
        caches.put(key, cache);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <K, T> ZkBackedCache<K, T> get(E key) {
        return (ZkBackedCache<K, T>) caches.get(key);
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

