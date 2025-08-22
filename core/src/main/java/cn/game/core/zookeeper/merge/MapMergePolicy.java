package cn.game.core.zookeeper.merge;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 适用于 T = Map<K,V> 的合并策略：
 * - 默认：key 冲突时以 newValue 覆盖 oldValue
 * - 可传入 conflictResolver 以自定义冲突合并规则： (oldV, newV) -> mergedV
 */
@SuppressWarnings("unchecked")
public class MapMergePolicy<K, V> implements MergePolicy<Map<K, V>> {

    private final BiFunction<V, V, V> conflictResolver;

    public MapMergePolicy() {
        this((oldV, newV) -> newV); // 默认新值覆盖
    }

    public MapMergePolicy(BiFunction<V, V, V> conflictResolver) {
        this.conflictResolver = conflictResolver;
    }

    @Override
    public Map<K, V> merge(Map<K, V> oldValue, Map<K, V> newValue) {
        if (oldValue == null && newValue == null) return null;
        Map<K, V> out = new HashMap<>();
        if (oldValue != null) out.putAll(oldValue);
        if (newValue != null) {
            for (Map.Entry<K, V> e : newValue.entrySet()) {
                K k = e.getKey();
                V nv = e.getValue();
                if (out.containsKey(k)) {
                    V ov = out.get(k);
                    V mv = conflictResolver.apply(ov, nv);
                    out.put(k, mv);
                } else {
                    out.put(k, nv);
                }
            }
        }
        return out;
    }
}

