package cn.game.core.redis;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class ShardedRedisMapContainerTest {
	// 创建网络，让容器之间可以通信
	private static final Network REDIS_NETWORK = Network.newNetwork();

	// Redis集群节点配置
	private static final int MASTER_COUNT = 3;
	private static final int REPLICA_COUNT = 1; // 每个master的副本数
	private static final String REDIS_IMAGE = "bitnami/redis-cluster:6.2";

	// 存储所有Redis容器
	private static final List<GenericContainer<?>> REDIS_CONTAINERS = createRedisContainers();

	private static RedissonClient redisClient;
	private RedisShardedMap<String, String> shardedMap;
	private static final String TEST_MAP_KEY = "test_map";
	private static final int SHARD_COUNT = 4;

	// 创建Redis容器列表
	private static List<GenericContainer<?>> createRedisContainers() {
		List<GenericContainer<?>> containers = new ArrayList<>();

		// 创建所有节点容器
		for (int i = 0; i < MASTER_COUNT * (REPLICA_COUNT + 1); i++) {
			String containerName = "redis-" + i;
			GenericContainer<?> container = new GenericContainer<>(REDIS_IMAGE).withNetwork(REDIS_NETWORK)
					.withNetworkAliases(containerName)
					.withExposedPorts(6379)
					.withEnv("REDIS_PASSWORD", "") // 不设置密码
					.withEnv("REDIS_CLUSTER_REPLICAS", String.valueOf(REPLICA_COUNT))
					.withEnv("REDIS_NODES", createRedisNodesConfig())
					.withEnv("REDIS_CLUSTER_ANNOUNCE_IP", containerName)
					.withEnv("REDIS_CLUSTER_DYNAMIC_IPS", "yes")
					.waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));

			containers.add(container);
		}

		return containers;
	}

	// 创建Redis节点配置
	private static String createRedisNodesConfig() {
		return IntStream.range(0, MASTER_COUNT * (REPLICA_COUNT + 1))
				.mapToObj(i -> "redis-" + i + ":6379")
				.collect(Collectors.joining(","));
	}

	@BeforeAll
	static void setUp() {
		// 启动所有容器
		REDIS_CONTAINERS.forEach(GenericContainer::start);

		// 等待集群就绪
		try {
			Thread.sleep(30000); // 给集群初始化一些时间
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// 配置Redisson客户端
		Config config = new Config();
		String nodes = REDIS_CONTAINERS.stream()
				.map(container -> String.format("redis://%s:%d", container.getHost(), container.getMappedPort(6379)))
				.collect(Collectors.joining(","));

		config.useClusterServers().setScanInterval(2000).addNodeAddress(nodes.split(","));

		redisClient = Redisson.create(config);
	}

	@Test
	void testBasicOperations() {
		shardedMap = new RedisShardedMap<>(redisClient, TEST_MAP_KEY, SHARD_COUNT);

		// 基本操作测试
		assertNull(shardedMap.put("key1", "value1"));
		assertEquals("value1", shardedMap.get("key1"));
		// ... 其他测试代码
	}

	// ... 其他测试方法
}