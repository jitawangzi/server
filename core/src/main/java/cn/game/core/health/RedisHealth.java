package cn.game.core.health;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;

/**
 * Redis 健康探测器（保守版，任一探测目标连续失败达阈值即判定 Redis 不健康）
 *
 * 策略：
 * - 使用多个独立的“探测目标”键与多命令路径，任一目标连续失败达阈值即视为不健康
 * - 探测频率低（10-15s），满足“约 5 分钟内判定故障”的需求
 *
 * 兼容 Redisson 3.14.0：
 * - RScript.eval 签名为 eval(Mode mode, String luaScript, ReturnType returnType, List<Object> keys, Object... values)
 * - 因此移除了 codec 参数；PING 使用只读脚本 "return redis.call('PING')"，若失败则兜底 exists
 * - 业务脚本（SCRIPT 模式）同样使用上述 eval 签名
 */
public class RedisHealth {

	private final RedissonClient redisson;
	private final SelfDestructGuardConfig cfg;
	private final int threshold;

	private final List<ProbeTarget> targets = new ArrayList<>();
	private int roundRobin = 0;

	static final class ProbeTarget {
		final String key; // e.g., sdg:test:rand123-0
		int consecutiveFail = 0;
		boolean lastHealthy = true;

		ProbeTarget(String key) {
			this.key = key;
		}
	}

	RedisHealth(RedissonClient redisson, SelfDestructGuardConfig cfg, int threshold) {
		this.redisson = Objects.requireNonNull(redisson);
		this.cfg = Objects.requireNonNull(cfg);
		this.threshold = threshold;
		initTargets();
	}

	private void initTargets() {
		String salt = Long.toHexString(ThreadLocalRandom.current().nextLong());
		int n = Math.max(1, cfg.redisProbeKeysCount);
		for (int i = 0; i < n; i++) {
			String key = "sdg:test:" + salt + "-" + i; // 不使用 hash tag，平均散布
			targets.add(new ProbeTarget(key));
		}
	}

	// 轮询执行 1 个探测（降低压力）
	boolean probeOnce() {
		if (targets.isEmpty())
			return false;
		ProbeTarget t = targets.get(roundRobin++ % targets.size());
		boolean ok = doProbe(t.key);
		if (ok) {
			t.consecutiveFail = 0;
			t.lastHealthy = true;
		} else {
			t.consecutiveFail++;
			t.lastHealthy = false;
		}
		return ok;
	}

	boolean breached() {
		for (ProbeTarget t : targets) {
			if (t.consecutiveFail >= threshold)
				return true;
		}
		return false;
	}

	String debugState() {
		StringBuilder sb = new StringBuilder();
		for (ProbeTarget t : targets) {
			sb.append(t.key).append(":").append(t.consecutiveFail).append(" ");
		}
		return sb.toString().trim();
	}

	private boolean doProbe(String key) {
		Callable<Boolean> task = () -> {
			try {
				switch (cfg.redisProbeMode) {
				case PING: {
					// 使用只读脚本执行 PING（老版 Redisson 3.14.0 的 eval 签名）
					try {
						Object res = redisson.getScript()
								.eval(RScript.Mode.READ_ONLY, "return redis.call('PING')", RScript.ReturnType.STATUS,
										Collections.emptyList());
						return res != null;
					} catch (Throwable ignore) {
						// 兜底：exists（轻量、读路径）
						try {
							return redisson.getBucket(key).isExists();
						} catch (Throwable t2) {
							return false;
						}
					}
				}
				case SMALL_RW: {
					String val = "ok-" + System.nanoTime();
					redisson.getBucket(key).set(val, cfg.redisProbeKeyTtl.toSeconds(), TimeUnit.SECONDS);
					Object got = redisson.getBucket(key).get();
					return Objects.equals(val, got);
				}
				case SCRIPT: {
					Object res = redisson.getScript()
							.eval(RScript.Mode.READ_WRITE, cfg.scriptBody, RScript.ReturnType.STATUS,
									java.util.Collections.singletonList((Object) key), String.valueOf(cfg.redisProbeKeyTtl.toSeconds()),
									"ok");
					return res != null;
				}
				default:
					return false;
				}
			} catch (Throwable t) {
				return false;
			}
		};
		Boolean r = runWithSoftTimeout(task, cfg.redisProbeSoftTimeout);
		return Boolean.TRUE.equals(r);
	}

	private static <T> T runWithSoftTimeout(Callable<T> task, Duration timeout) {
		ExecutorService es = Executors.newSingleThreadExecutor(r -> {
			Thread th = new Thread(r, "sdg-redis-probe");
			th.setDaemon(true);
			return th;
		});
		Future<T> f = es.submit(task);
		es.shutdown();
		try {
			if (timeout == null || timeout.isZero() || timeout.isNegative()) {
				return f.get();
			}
			return f.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (Throwable e) {
			f.cancel(true);
			return null;
		}
	}

}
