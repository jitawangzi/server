package cn.game.core.id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**    
 * 分布式ID生成器外观类 (Facade)
 * 业务层只与此通过此类交互
 */
public class IdUtil {
    private static final Logger log = LoggerFactory.getLogger(IdUtil.class);

    // 核心服务组件
    private static SnowflakeService snowflakeService;
    private static SegmentIdService segmentService;

    /**
     * 业务ID类型定义
     */
    public static enum IdType {
        PLAYER, HERO, ITEM, UNION, ORDER, GUILD;
    }

    /**
     * 初始化所有 ID 生成服务
     * @param serverId 当前进程唯一标识
     */
    public static void init(String serverId) throws Exception {
        log.info("开始初始化分布式 ID 服务...");
        
        // 1. 初始化雪花算法服务 (分配 WorkerID + 启动心跳)
        snowflakeService = new SnowflakeService(serverId);
        snowflakeService.init();

        // 2. 初始化号段模式服务 (预加载号段)
        segmentService = new SegmentIdService();
        segmentService.init();
        
        log.info("分布式 ID 服务初始化完成.");
    }

    /** 
     * 获取基于雪花算法的分布式唯一 ID
     */
    public static long getId() {
        checkInit();
        return snowflakeService.nextId();
    }

    /** 
     * 获取按号段自增 ID
     */
    public static long getIdAutoIncrease(IdType idType) {
        checkInit();
        return segmentService.nextId(idType);
    }

    /** 
     * 生成订单 ID (业务组合逻辑)
     */
    public static long genOrderId(long playerId) {
        return playerId << 33 | getIdAutoIncrease(IdType.ORDER);
    }
    
    private static void checkInit() {
        if (snowflakeService == null || segmentService == null) {
            throw new IllegalStateException("IdUtil 尚未初始化，请先调用 IdUtil.init(serverId)");
        }
    }
}