package cn.game.core.zookeeper;

/**
 * 将业务侧的键类型 K 映射为内部统一的字符串键。
 * ZK 路径本质为字符串，因此内部用 String 作为 Map key 与路径拼接。
 */
public interface KeyAdapter<K> {
    String toStringKey(K key);

    static KeyAdapter<String> stringKey() {
        return k -> k;
    }

    static KeyAdapter<Long> longKey() {
        return k -> k == null ? null : Long.toString(k);
    }

    static KeyAdapter<Integer> intKey() {
        return k -> k == null ? null : Integer.toString(k);
    }
}

