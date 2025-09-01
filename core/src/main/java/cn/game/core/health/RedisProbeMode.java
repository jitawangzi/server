package cn.game.core.health;

/**
 * Redis 探测模式
 */
public enum RedisProbeMode {
    PING,       // 对任意节点执行 PING（通过脚本或 exists 兜底）
    SMALL_RW,   // set/get 小键
    SCRIPT      // 执行轻量脚本对小键操作
}

