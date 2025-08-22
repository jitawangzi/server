package cn.game.core.zookeeper.codec;

/**
 * 通用编解码接口：定义如何在 byte[] 与强类型 T 之间转换。
 */
public interface ValueCodec<T> {
    byte[] encode(T value);
    T decode(byte[] bytes);
}

