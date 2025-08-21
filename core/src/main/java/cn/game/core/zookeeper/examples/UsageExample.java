package cn.game.core.zookeeper.examples;

import cn.game.core.zookeeper.*;
import cn.game.util.ZkHelper;
import org.apache.curator.framework.CuratorFramework;

import java.util.Arrays;
import java.util.List;

/**
 * 用法示例（演示多种键类型与统一注册管理）。
 */
public class UsageExample {

    public static void main(String[] args) throws Exception {
        CuratorFramework curator = ZkHelper.curator;

        // servers: String 键
        PathPolicy serversPath = PathPolicy.simple("/server/login/game/virtual-servers");
        ValueCodec<ExampleModels.VirtualServerView> serversCodec = new JsonValueCodec<>(ExampleModels.VirtualServerView.class);
        IdExtractor<ExampleModels.VirtualServerView> serversId = v -> v.ID;
        ZkBackedCache<String, ExampleModels.VirtualServerView> serversCache =
                ZkBackedCache.<String, ExampleModels.VirtualServerView>builder(String.class, ExampleModels.VirtualServerView.class)
                        .client(curator)
                        .pathPolicy(serversPath)
                        .codec(serversCodec)
                        .idExtractor(serversId)
                        .keyAdapter(KeyAdapter.stringKey())
                        .build();

        serversCache.addListener((type, key, val) -> {
            System.out.println("[servers] " + type + " id=" + key);
        });

        // players: long 键
        PathPolicy playersPath = PathPolicy.simple("/game/players");
        ValueCodec<ExampleModels.PlayerInfo> playersCodec = new JsonValueCodec<>(ExampleModels.PlayerInfo.class);
        IdExtractor<ExampleModels.PlayerInfo> playersId = v -> v.id; // long
        ZkBackedCache<Long, ExampleModels.PlayerInfo> playersCache =
                ZkBackedCache.<Long, ExampleModels.PlayerInfo>builder(Long.class, ExampleModels.PlayerInfo.class)
                        .client(curator)
                        .pathPolicy(playersPath)
                        .codec(playersCodec)
                        .idExtractor(playersId)
                        .keyAdapter(KeyAdapter.longKey())
                        .build();

        playersCache.addListener((type, key, val) -> {
            System.out.println("[players] " + type + " id=" + key);
        });

        // 统一注册并启动
        CacheRegistry registry = new CacheRegistry()
                .register(ZkCacheType.VIRTUAL_SERVER_LIST, serversCache)
                .register(ZkCacheType.SERVER_LIST, playersCache);

        registry.startAllAndWarmup();

        // 运维初始化（以 servers 为例）
        ZkInitializer<String, ExampleModels.VirtualServerView> serverInit =
                new ZkInitializer<>(curator, serversPath, serversCodec, serversId, KeyAdapter.stringKey());
        List<ExampleModels.VirtualServerView> initData = Arrays.asList(
                new ExampleModels.VirtualServerView("s1", "Server-1", 2000, 1, "2025-01-01T00:00:00"),
                new ExampleModels.VirtualServerView("s2", "Server-2", 2000, 2, "2025-02-01T00:00:00")
        );
        serverInit.initMissing(initData);

        // 业务读取：从本地缓存
        serversCache.getByKey("s1").ifPresent(v -> System.out.println("s1 name=" + v.name));
        playersCache.getByKey(10001L).ifPresent(p -> System.out.println("player 10001 level=" + p.level));

        registry.close();
    }
}
