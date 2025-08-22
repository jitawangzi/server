package cn.game.core.zookeeper.merge;

import java.util.HashSet;
import java.util.Set;

/**
 * 适用于 T = Set<E> 的并集策略：
 * - old 为 null 时返回 new 的拷贝
 * - new 为 null 时返回 old 的拷贝（也可返回 old 原对象，但为避免外部引用，统一拷贝）
 * - 都不为 null：返回 old ∪ new 的新集合
 */
@SuppressWarnings("unchecked")
public class SetUnionPolicy<E> implements MergePolicy<Set<E>> {

    @Override
    public Set<E> merge(Set<E> oldValue, Set<E> newValue) {
        if (oldValue == null && newValue == null) return null;
        Set<E> out = new HashSet<>();
        if (oldValue != null) out.addAll(oldValue);
        if (newValue != null) out.addAll(newValue);
        return out;
    }
}

