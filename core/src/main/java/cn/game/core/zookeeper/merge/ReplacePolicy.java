package cn.game.core.zookeeper.merge;

/**
 * 覆盖策略：新值直接取代旧值。
 */
public class ReplacePolicy<T> implements MergePolicy<T> {
    @Override
    public T merge(T oldValue, T newValue) {
        return newValue;
    }
}

