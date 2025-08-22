package cn.game.core.zookeeper;

/**
 * 从值对象中提取唯一 ID（键）。
 * 返回的对象会交由 KeyAdapter 统一转换为字符串键。
 */
@FunctionalInterface
public interface IdExtractor<T> {
    Object getId(T value);
}

