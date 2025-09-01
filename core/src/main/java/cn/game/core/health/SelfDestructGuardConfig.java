package cn.game.core.health;

import java.time.Duration;

/**
 * 自毁保护配置（独立文件）
 */
public class SelfDestructGuardConfig {
    // 健康检查频率
    public Duration checkInterval = Duration.ofSeconds(10);
    // 故障判定窗口（约 5 分钟）
    public Duration decisionWindow = Duration.ofMinutes(5);

    // Redis 探测模式：PING/SMALL_RW/SCRIPT
    public RedisProbeMode redisProbeMode = RedisProbeMode.SMALL_RW;

    // 探测目标键的数量（越多覆盖越广；默认 9）
    public int redisProbeKeysCount = 9;

    // Redis 读写键的过期时间，避免残留
    public Duration redisProbeKeyTtl = Duration.ofSeconds(30);

    // 软超时（多数由底层 Redisson 配置掌控，这里只是保护）
    public Duration redisProbeSoftTimeout = Duration.ofMillis(500);

    // 连续失败阈值（<=0 则用 decisionWindow/checkInterval 推导）
    public int consecutiveFailThreshold = 0;

    // 退出码
    public int exitCode = 42;

    public boolean logDetails = true;

    // 若使用 SCRIPT，提供轻量脚本（可保持与续约路径一致）
    // 示例：return redis.call('setex', KEYS[1], ARGV[1], ARGV[2])
    public String scriptBody = "return redis.call('setex', KEYS[1], ARGV[1], ARGV[2])";
}

