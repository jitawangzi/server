package cn.game.core.health;

import org.apache.curator.framework.CuratorFramework;
import org.redisson.api.RedissonClient;

/**
 * SelfDestructGuard（保守版，分片无关，任一探测目标连续失败达阈值即判定 Redis 不健康）
 *
 * 策略：
 * - ZooKeeper 或 Redis 任一在 decisionWindow 内连续失败达到阈值 => 触发自毁（System.exit）
 * - Redis 探测不依赖 hash tag，使用多个独立的“探测目标”键与多命令路径，任一目标连续失败达阈值即视为不健康
 * - 探测频率低（10-15s），窗口默认 5 分钟，满足“约 5 分钟内判定故障”的需求
 *
 * 兼容 Redisson 3.14.0：
 * - RScript.eval 签名为 eval(Mode mode, String luaScript, ReturnType returnType, List<Object> keys, Object... values)
 * - 因此移除了 codec 参数；PING 使用只读脚本 "return redis.call('PING')"，若失败则兜底 exists
 * - 业务脚本（SCRIPT 模式）同样使用上述 eval 签名
 */
public class SelfDestructGuard {

	private final HealthGuard guard;

	public SelfDestructGuard(SelfDestructGuardConfig cfg,RedissonClient redisson,CuratorFramework curator) {
		this.guard = new HealthGuard(cfg,redisson,curator);
	}

	public void start() {
		guard.start();
	}

	public void stop() {
		guard.stop();
	}
}
