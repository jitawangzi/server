package cn.game.core.health;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.curator.framework.CuratorFramework;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.util.RedisUtil;
import cn.game.util.ZkHelper;

public class HealthGuard {
	private static final Logger LOGGER = LoggerFactory.getLogger(HealthGuard.class);
	
	private final SelfDestructGuardConfig cfg;
	private final RedisHealth redisHealth;
	private final ZkHealth zkHealth ;
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "sdg-health-guard");
		t.setDaemon(true);
		return t;
	});
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final int threshold; // 连续失败阈值
	private volatile Instant unhealthySince = null;

	public HealthGuard(SelfDestructGuardConfig cfg,RedissonClient redisson,CuratorFramework curator) {
		this.cfg = Objects.requireNonNull(cfg);
		if (redisson == null)
			throw new IllegalStateException("Redisson not initialized");
		if (curator == null)
			throw new IllegalStateException("Curator not initialized");

		int derived = cfg.consecutiveFailThreshold > 0 ? cfg.consecutiveFailThreshold
				: Math.max(1, (int) (cfg.decisionWindow.toMillis() / Math.max(1, cfg.checkInterval.toMillis())));
		this.threshold = derived;

		this.redisHealth = new RedisHealth(redisson, cfg, threshold);
		this.zkHealth = new ZkHealth(curator);
	}

	public void start() {
		if (!running.compareAndSet(false, true))
			return;
		zkHealth.attach();
		scheduler.scheduleAtFixedRate(this::tick, 0, cfg.checkInterval.toMillis(), TimeUnit.MILLISECONDS);
		log("START", "interval=" + cfg.checkInterval + ", window=" + cfg.decisionWindow + ", threshold=" + threshold);
	}

	public void stop() {
		if (!running.compareAndSet(true, false))
			return;
		scheduler.shutdownNow();
		log("STOP", "stopped");
	}

	private void tick() {
		try {
			boolean zkOk = zkHealth.probeOnce();
			boolean redisOk = redisHealth.probeOnce();

			boolean unhealthy = !zkOk || !redisOk;

			if (unhealthy) {
				if (unhealthySince == null) {
					unhealthySince = Instant.now();
					log("ENTER_UNHEALTHY", "zk=" + zkHealth.stateName() + ", zkFail=" + zkHealth.consecutiveFail() + ", redisOk="
							+ redisOk + ", redisTargets=" + redisHealth.debugState());
				}

				// 任一依赖达到阈值则自毁（保守）
				boolean zkBreach = zkHealth.breached(threshold);
				boolean redisBreach = redisHealth.breached();

				if (zkBreach || redisBreach) {
					Duration el = Duration.between(unhealthySince, Instant.now());
					log("SELF_DESTRUCT", "elapsed=" + el.toSeconds() + "s" + ", zk=" + zkHealth.stateName() + ", zkFail="
							+ zkHealth.consecutiveFail() + ", redisTargets=" + redisHealth.debugState());
					System.exit(cfg.exitCode);
				} else {
					log("UNHEALTHY_PROGRESS", "zkFail=" + zkHealth.consecutiveFail() + ", threshold=" + threshold + ", redisTargets="
							+ redisHealth.debugState());
				}
			} else {
				if (unhealthySince != null) {
					log("RECOVERED", "zk=" + zkHealth.stateName() + ", redisTargets=" + redisHealth.debugState());
					unhealthySince = null;
				}
			}
		} catch (Throwable t) {
			log("TICK_ERROR", t.toString());
		}
	}

	private void log(String event, String msg) {
		if (!cfg.logDetails)
			return;
		LOGGER.error(Instant.now() + " [SelfDestructGuard." + event + "] " + msg);
	}

}
