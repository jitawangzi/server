package cn.game.core.zookeeper;

/**
 * 本地缓存更新完成后的通知回调。
 * 注意：保证在回调前，缓存已被更新（最终一致的“后置回调”）。
 */
@FunctionalInterface
public interface CacheChangeListener<T> {
    void onChange(NodeChangeType type, Object key, T newValue);
}

