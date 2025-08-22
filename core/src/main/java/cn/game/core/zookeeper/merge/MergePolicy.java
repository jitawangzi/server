package cn.game.core.zookeeper.merge;

import cn.game.core.zookeeper.ValueCodec;

/**
 * 定义“新值如何与旧值合并”的策略。
 * - merge：合并内存中的旧值与新值，返回要存入缓存（以及可能写回 ZK）的最终值。
 * - encodeForWrite：如需定制写回 ZK 的编码，可覆盖；默认直接使用 codec.encode(merged)。
 */
public interface MergePolicy<T> {

    T merge(T oldValue, T newValue);

    default byte[] encodeForWrite(T merged, ValueCodec<T> codec) {
        return codec.encode(merged);
    }
}

