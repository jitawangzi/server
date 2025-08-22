package cn.game.core.zookeeper.codec;

import cn.game.util.JsonUtil;

import java.nio.charset.StandardCharsets;

/**
 * 适配 Vert.x 集群节点在 ZK 中的节点数据格式：
 * - 节点值不是纯 JSON，示例：`10.12.0.221�(.{"serverId":"login_test","serverType":"Login"}`
 * - 简单策略：从字节序列中提取第一个 '{' 到最后一个 '}' 之间的子串，按 JSON 反序列化。
 */
public class VertxNodeValueCodec implements ValueCodec<ActiveServerNode> {

    @Override
    public byte[] encode(ActiveServerNode value) {
        String json = JsonUtil.toJsonString(value);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ActiveServerNode decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        String s = new String(bytes, StandardCharsets.UTF_8);
        int b = s.indexOf('{');
        int e = s.lastIndexOf('}');
        if (b >= 0 && e >= b) {
            String json = s.substring(b, e + 1);
            try {
                return JsonUtil.parseObject(json, ActiveServerNode.class);
            } catch (RuntimeException ex) {
                try {
                    return JsonUtil.parseObject(s, ActiveServerNode.class);
                } catch (RuntimeException ignore) {}
            }
        } else {
            try {
                return JsonUtil.parseObject(s, ActiveServerNode.class);
            } catch (RuntimeException ignore) {}
        }
        return null;
    }
}

