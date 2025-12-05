package cn.game.core.id;

import java.util.List;
import java.util.Map;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分布式ID生成器外观类 (Facade)
 * 整合了：雪花算法、Redis分服自增、ZK号段模式
 */
public class IdUtil {
    private static final Logger log = LoggerFactory.getLogger(IdUtil.class);

    // 三大核心服务
    private static SnowflakeService snowflakeService;
    private static RedisIdService redisIdService;
    private static SegmentIdService segmentService;

    /**
     * ID类型定义
     * 可以在这里配置号段模式的初始值
     */
    public static enum IdType {
        PLAYER(340200000L), 
        HERO(0), 
        ITEM(0), 
        UNION(0), 
        ORDER(0), 
        GUILD(890000001L);

        private final long initialId;

        IdType(long initialId) {
            this.initialId = initialId;
        }

        public long getInitialId() {
            return initialId;
        }
    }

    /**
     * ID 范围配置 (用于 Redis 分服模式)
     */
    public static class IdRangeConfig {
        String logicalServerId; // 逻辑服务器ID
        long startId;           // 起始ID
        int maxCount;           // 最大数量

        public IdRangeConfig(String logicalServerId, long startId, int maxCount) {
            this.logicalServerId = logicalServerId;
            this.startId = startId;
            this.maxCount = maxCount;
        }
    }

    /**
     * 初始化所有 ID 服务
     * 
     * @param serverId          进程唯一标识 (用于雪花算法)
     * @param redissonClient    Redisson客户端 (用于Redis模式)
     * @param redisConfigs      Redis分服配置 (用于Redis模式)
     */
    public static void init(String serverId, 
                            RedissonClient redissonClient, 
                            Map<IdType, List<IdRangeConfig>> redisConfigs) throws Exception {
        
        log.info("开始初始化分布式 ID 服务 | 进程ID: {}", serverId);

        // 1. 初始化雪花算法
        snowflakeService = new SnowflakeService(serverId);
        snowflakeService.init();

        // 2. 初始化 Redis 多逻辑服模式,看情况使用
		if (redissonClient != null && redisConfigs != null) {
			log.info("Redis ID 服务启用，配置逻辑服数量: {}", redisConfigs.values().stream().mapToInt(List::size).sum());
			redisIdService = new RedisIdService(redissonClient);
			redisIdService.init(redisConfigs);
		}
        // 3. 初始化 ZK 号段模式 (依赖 ZkHelper)
        segmentService = new SegmentIdService();
        segmentService.init();
        
        log.info("分布式 ID 服务初始化完成.");
    }
    public static void init(String serverId) throws Exception {
    	init(serverId, null, null);
    }

    // ==========================================
    // 1. 雪花算法 (全局唯一，不连续，趋势递增)
    //	没有特殊需求可以使用这个，不涉及到持久化
    // ==========================================
    public static long getId() {
        checkInit();
        return snowflakeService.nextId();
    }

    // ==========================================
    // 2. Redis 模式 (指定逻辑服，连续)
    // 适用于：玩家ID、英雄ID等必须归属于某个逻辑服的数据
    // ==========================================
    public static long getIdByRedis(String logicalServerId, IdType idType) {
        checkInit();
        return redisIdService.nextId(logicalServerId, idType);
    }
    
    /**
     * Redis 全局模式 (备用)
     */
    public static long getIdGlobalByRedis(IdType idType) {
    	checkRedisInit();
        return redisIdService.nextGlobalId(idType);
    }

    // ==========================================
    // 3. 号段模式 (全局唯一，连续，最高性能，可能有空洞)
    // ==========================================
    public static long getIdBySegment(IdType idType) {
        checkInit();
        return segmentService.nextId(idType);
    }

    /**
     * 混合业务逻辑示例：生成订单ID
     */
    public static long genOrderId(long playerId) {
        // 示例：高位是玩家ID，低位是全局自增序列
        return playerId << 33 | getIdBySegment(IdType.ORDER);
    }

    private static void checkInit() {
        if (snowflakeService == null || segmentService == null) {
            throw new IllegalStateException("IdUtil 尚未初始化");
        }
    }
    private static void checkRedisInit() {
    	if (redisIdService == null) {
    		throw new IllegalStateException("IdUtil redis类型 尚未初始化");
    	}
    }
}