package cn.game.core.zookeeper;

/**
 * 从值对象中提取唯一 ID（键）。
 */
@FunctionalInterface
public interface IdExtractor<T> {
    Object getId(T value);
}

