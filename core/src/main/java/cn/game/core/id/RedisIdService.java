package cn.game.core.id;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.game.core.id.IdUtil.IdRangeConfig;
import cn.game.core.id.IdUtil.IdType;

public class RedisIdService {
    private static final Logger log = LoggerFactory.getLogger(RedisIdService.class);
    private static final String REDIS_KEY_TEMPLATE = "game:id:%s:%s"; // game:id:player:1001
    private static final String GLOBAL_SERVER_ID = "global";

    private final RedissonClient redissonClient;
    private final Map<IdType, Map<String, RAtomicLong>> generators = new ConcurrentHashMap<>();
    private final Map<IdType, Map<String, Long>> limits = new ConcurrentHashMap<>();

    public RedisIdService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void init(Map<IdType, List<IdRangeConfig>> rangeConfigs) {
        if (rangeConfigs == null) return;

        for (Map.Entry<IdType, List<IdRangeConfig>> entry : rangeConfigs.entrySet()) {
            IdType type = entry.getKey();
            for (IdRangeConfig config : entry.getValue()) {
                String redisKey = String.format(REDIS_KEY_TEMPLATE, type.name().toLowerCase(), config.logicalServerId);
                long startId = config.startId;
                long endId = startId + config.maxCount;

                RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
                initRedisValue(atomicLong, redisKey, startId);

                generators.computeIfAbsent(type, k -> new ConcurrentHashMap<>())
                          .put(config.logicalServerId, atomicLong);
                limits.computeIfAbsent(type, k -> new ConcurrentHashMap<>())
                      .put(config.logicalServerId, endId);
                
                log.info("Redis ID生成器就绪 | 类型: {} | 逻辑服: {} | 范围: {}-{}", type, config.logicalServerId, startId, endId);
            }
        }
    }

    public long nextId(String logicalServerId, IdType type) {
        Map<String, RAtomicLong> serverMap = generators.get(type);
        if (serverMap == null || !serverMap.containsKey(logicalServerId)) {
            throw new IllegalArgumentException(String.format("逻辑服 [%s] 未配置类型 [%s]", logicalServerId, type));
        }
        long nextId = serverMap.get(logicalServerId).incrementAndGet();
        
        Long limit = limits.get(type).get(logicalServerId);
        if (nextId > limit) {
            throw new RuntimeException(String.format("ID耗尽: [%s] - [%s]", logicalServerId, type));
        }
        return nextId;
    }

    public long nextGlobalId(IdType type) {
        return generators.computeIfAbsent(type, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(GLOBAL_SERVER_ID, k -> {
                    String key = String.format(REDIS_KEY_TEMPLATE, type.name().toLowerCase(), GLOBAL_SERVER_ID);
                    RAtomicLong gen = redissonClient.getAtomicLong(key);
                    initRedisValue(gen, key, 0);
                    return gen;
                }).incrementAndGet();
    }

    private void initRedisValue(RAtomicLong atomicLong, String key, long startId) {
        if (!atomicLong.isExists()) {
            atomicLong.set(startId);
        } else {
            long current = atomicLong.get();
            if (current < startId) {
                log.warn("Redis Key [{}] 快进: {} -> {}", key, current, startId);
                while (true) {
                    current = atomicLong.get();
                    if (current >= startId || atomicLong.compareAndSet(current, startId)) break;
                }
            }
        }
    }
}