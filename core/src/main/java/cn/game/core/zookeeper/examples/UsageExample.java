package cn.game.core.zookeeper.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;

import cn.game.core.zookeeper.IdExtractor;
import cn.game.core.zookeeper.KeyAdapter;
import cn.game.core.zookeeper.NodeChangeType;
import cn.game.core.zookeeper.PathPolicy;
import cn.game.core.zookeeper.ZkBackedCache;
import cn.game.core.zookeeper.ZkCacheRegistry;
import cn.game.core.zookeeper.ZkCacheType;
import cn.game.core.zookeeper.codec.ActiveServerNode;
import cn.game.core.zookeeper.codec.JsonValueCodec;
import cn.game.core.zookeeper.codec.ValueCodec;
import cn.game.core.zookeeper.codec.VertxNodeValueCodec;
import cn.game.core.zookeeper.merge.MapMergePolicy;
import cn.game.util.ZkHelper;

/**
 * 用法示例：
 * - 示例 A：每实例一个节点（T=ActiveServerNode），按需分组（不使用合并策略或使用 Replace）。
 * - 示例 B：按组为值（T=Set<String> 或 T=Map<String,ActiveServerNode>），通过合并策略实现 append/union 语义。
 */
public class UsageExample {

    public static void main(String[] args) throws Exception {
        CuratorFramework curator = ZkHelper.curator;

        // 示例 A：Vert.x 活跃节点缓存（每个节点一个子节点；值包含服务元数据，但可能带前缀）
        PathPolicy activeNodesPath = PathPolicy.simple("/cluster/active-nodes");
        ValueCodec<ActiveServerNode> vertxCodec = new VertxNodeValueCodec();
        IdExtractor<ActiveServerNode> nodeId = n -> n.serverId; // 用 serverId 作为键
        ZkBackedCache<String, ActiveServerNode> activeNodesCache =
                ZkBackedCache.<String, ActiveServerNode>builder(String.class, ActiveServerNode.class)
                        .client(curator)
                        .pathPolicy(activeNodesPath)
                        .codec(vertxCodec)
                        .idExtractor(nodeId)
                        .keyAdapter(KeyAdapter.stringKey())
                        // 未显式传策略 -> 默认 Replace（监听也会把 ZK 值直接放入）
                        .build();

        activeNodesCache.addListener((type, key, node) -> {
            if (type == NodeChangeType.NODE_CREATED) {
                System.out.println("[active] JOIN id=" + key + " type=" + safeType(node));
            } else if (type == NodeChangeType.NODE_DELETED) {
                System.out.println("[active] LEAVE id=" + key);
            } else if (type == NodeChangeType.NODE_CHANGED) {
                System.out.println("[active] UPDATE id=" + key + " type=" + safeType(node));
            }
        });

        // 示例 B1：分组为值（T = Set<String>），key = groupType（如 "Login"/"Game"）
        // - 上游若写入“增量集合”，我们通过 SetUnionPolicy 合并到本地并写回（当你调用 upsert 时）。
        PathPolicy groupSetPath = PathPolicy.simple("/cluster/active-node-groups/set");
        ValueCodec<Set<String>> setCodec = new ValueCodec<Set<String>>() {
            @Override public byte[] encode(Set<String> value) { return new JsonValueCodec<>(Object.class).encode(value); }
            @Override public Set<String> decode(byte[] bytes) {
                // 这里简化：使用 JsonUtil 的通用反序列化为 Set（实现依赖你的 JsonUtil 是否支持）
                // 若不支持，请改为 List 再 new HashSet<>(list)
                String json = new String(bytes);
                // 假设 JsonUtil 支持：parseArray(json, String.class)
                try {
//                    List<String> list = cn.game.util.JsonUtil.parseArray(json, String.class);
                    List<String> list = new ArrayList<>();
                    return new LinkedHashSet<>(list);
                } catch (Exception e) {
                    return new LinkedHashSet<>();
                }
            }
        };
        IdExtractor<Set<String>> groupId = set -> {
            // 这里无法从值反推出 groupKey，实际使用中请在 upsert 时传入一个带 groupKey 的包装类型，或不要用 createMissing/upsert 由运维统一写。
            // 为保持示例可编译，这里返回 null（不建议生产这样做）。
            return null;
        };
        // 注意：由于 IdExtractor 需要从值中提取 id，这种“值不含 id”的形态不适合使用 createMissing/upsert，
        // 更适合只读监听；或者定义一个包装类型 GroupSet { String group; Set<String> members; }。
        // 下方我们改用“更合理”的 B2 示例。

        // 示例 B2：分组为值（T = Map<String, ActiveServerNode>），key = groupType
        // - 合并策略：MapMergePolicy，新值覆盖同 key 旧值。
        class GroupMap {
            public String group; // e.g., "Login"/"Game"
            public Map<String, ActiveServerNode> members;
        }
        PathPolicy groupMapPath = PathPolicy.simple("/cluster/active-node-groups/map");
        ValueCodec<GroupMap> groupMapCodec = new JsonValueCodec<>(GroupMap.class);
        IdExtractor<GroupMap> groupMapId = gm -> gm.group;
        ZkBackedCache<String, GroupMap> groupMapCache =
                ZkBackedCache.<String, GroupMap>builder(String.class, GroupMap.class)
                        .client(curator)
                        .pathPolicy(groupMapPath)
                        .codec(groupMapCodec)
                        .idExtractor(groupMapId)
                        .keyAdapter(KeyAdapter.stringKey())
                        // 显式传入策略：对 GroupMap 本身使用 Replace，但对内部 members 的更新可由上游完成
                        // 若你希望在 upsert 传入“仅新增/局部更新”的 GroupMap，且想在本地合并，那么请让 MergePolicy 针对 GroupMap 自定义：
                        .mergePolicy((oldV, newV) -> {
                            if (oldV == null) return newV;
                            if (newV == null) return oldV;
                            GroupMap out = new GroupMap();
                            out.group = newV.group != null ? newV.group : oldV.group;
                            MapMergePolicy<String, ActiveServerNode> inner = new MapMergePolicy<>();
                            Map<String, ActiveServerNode> mergedMembers =
                                    inner.merge(oldV.members, newV.members);
                            out.members = mergedMembers;
                            return out;
                        })
                        .build();

        groupMapCache.addListener((type, key, gm) -> {
            int size = (gm == null || gm.members == null) ? 0 : gm.members.size();
            System.out.println("[group-map] " + type + " group=" + key + " size=" + size);
        });

        // 统一管理
        ZkCacheRegistry<ZkCacheType> registry = new ZkCacheRegistry<>(ZkCacheType.class)
                .register(ZkCacheType.VIRTUAL_SERVER_LIST, activeNodesCache)
                .register(ZkCacheType.SERVER_LIST, groupMapCache);

        registry.startAllAndWarmup();

        // 使用点：对 A 的“随用随分组”
        Map<String, List<ActiveServerNode>> byType = activeNodesCache.getAll().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(n -> n.serverType));
        Set<String> gameIds = byType.getOrDefault("Game", Collections.emptyList()).stream()
                .map(n -> n.serverId).collect(Collectors.toCollection(LinkedHashSet::new));
        System.out.println("Current Game servers: " + gameIds);

        // 使用点：对 B2 的“按组直接获取一张 Map”
        groupMapCache.getByKey("Game").ifPresent(gm -> {
            Set<String> ids = gm.members == null ? Set.of() : gm.members.keySet();
            System.out.println("Group=Game, members=" + ids);
        });

        registry.close();
    }

    private static String safeType(ActiveServerNode n) {
        return n == null ? "?" : n.serverType;
    }
}
