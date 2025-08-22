package cn.game.core.zookeeper.codec;

import cn.game.util.JsonUtil;
import java.nio.charset.StandardCharsets;

/**
 * 使用 JsonUtil 的 JSON 编解码器（适用于纯 JSON 的节点值）。
 * 需保证 JsonUtil 提供：
 *  - static String toJsonString(Object o)
 *  - static <T> T parseObject(String json, Class<T> type)
 */
public class JsonValueCodec<T> implements ValueCodec<T> {
    private final Class<T> type;

    public JsonValueCodec(Class<T> type) {
        this.type = type;
    }

    @Override
    public byte[] encode(T value) {
        String json = JsonUtil.toJsonString(value);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public T decode(byte[] bytes) {
        String json = new String(bytes, StandardCharsets.UTF_8);
        return JsonUtil.parseObject(json, type);
    }
}

