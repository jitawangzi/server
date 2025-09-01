package cn.game.core.leader;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.task.SchedulerService;
import cn.game.util.LockUtil;

/**
 * LeaderLeaseManager
 *
 * <p>职责与目标：</p>
 * <ul>
 *   <li>通过 Zookeeper 的 LeaderLatch 选主，决定“候选领导者”。</li>
 *   <li>通过 Redis 分布式锁实现“执行权租约”，作为严格的执行硬条件。</li>
 *   <li>双条件判定：只有 isLeader（ZK）且持有租约（Redis）时，才启动和执行 Leader 任务。</li>
 *   <li>连接状态为 SUSPENDED 时立即停止续约并暂停/停止任务，最坏重叠窗口上限约为锁 TTL。</li>
 *   <li>提供通用扩展点：可注册多个 Leader 任务，每个任务在执行逻辑中进行租约健康检查。</li>
 *   <li>内置续约健康度策略：短 TTL + 高频续约 + 安全余量检查 + 连续续约失败阈值。</li>
 * </ul>
 *
 * <p>使用方式：</p>
 * <pre>
 *   LeaderLeaseManager manager = new LeaderLeaseManager(curator, "/app/leader/global");
 *   manager.registerTask(new MyLeaderTask());
 *   manager.start();
 *   ...
 *   manager.stop();
 * </pre>
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>节点在成为 ZK Leader 后，不立即执行任务，而是先抢占 Redis 锁（租约）。</li>
 *   <li>执行过程中周期性检查剩余 TTL，若低于安全余量则尝试刷新；连续失败超过阈值，立即停止任务并释放锁。</li>
 *   <li>SUSPENDED 事件发生时，立刻停止续约并暂停/停止任务，以减少双主风险；LOST 等价于 notLeader。</li>
 *   <li>nodeId 为 final 且在构造函数最开始初始化，避免“变量可能尚未初始化”的提示。</li>
 * </ul>
 *
 * <p>参数建议：</p>
 * <ul>
 *   <li>leaseTtlSeconds：建议不大于 ZK sessionTimeout；一般 10~20 秒。</li>
 *   <li>renewPeriodSeconds：TTL 的 1/3~1/2，例如 5~10 秒。</li>
 *   <li>safetyMarginSeconds：TTL 的 20%~40%，用于批前健康检查。</li>
 *   <li>maxRenewFailures：2~3 次；超过则主动让出执行权。</li>
 * </ul>
 */
public class LeaderLeaseManager {

	private static final Logger log = LoggerFactory.getLogger(LeaderLeaseManager.class);

	/** 默认 Redis 租约 TTL（秒） */
	public static final int DEFAULT_LEASE_TTL_SECONDS = 20;
	/** 默认续约周期（秒），建议为 TTL 的 1/3~1/2 */
	public static final int DEFAULT_RENEW_PERIOD_SECONDS = 7;
	/** 默认安全余量（秒），执行前若剩余寿命小于该值则尝试刷新或让出 */
	public static final int DEFAULT_SAFETY_MARGIN_SECONDS = 4;
	/** 默认连续续约失败阈值，超过后立即停任务并释放锁 */
	public static final int DEFAULT_MAX_RENEW_FAILURES = 2;
	/** 抢锁/续约失败后的最小退避（毫秒） */
	public static final int DEFAULT_RETRY_MIN_BACKOFF_MS = 200;
	/** 抢锁/续约失败后的最大退避（毫秒） */
	public static final int DEFAULT_RETRY_MAX_BACKOFF_MS = 1500;

	/** Curator 客户端 */
	private final CuratorFramework curator;
	/** LeaderLatch 路径，例如：/app/leader/global */
	private final String latchPath;
	/** Curator LeaderLatch */
	private final LeaderLatch latch;

	/** 节点唯一标识，用于日志与锁值记录（若有需要） */
	private final String nodeId;

	/** 任务调度器（复用项目内 SchedulerService） */
	private final SchedulerService scheduler = SchedulerService.getInstance();

	/** 租约 TTL（秒） */
	private final int leaseTtlSeconds;
	/** 续约周期（秒） */
	private final int renewPeriodSeconds;
	/** 安全余量（秒） */
	private final int safetyMarginSeconds;
	/** 连续续约失败阈值 */
	private final int maxRenewFailures;
	/** 抢锁最小/最大退避 */
	private final int retryMinBackoffMs;
	private final int retryMaxBackoffMs;

	/** Manager 是否已启动 */
	private volatile boolean running = false;
	/** 当前是否为 ZK Leader（从 LeaderLatch 视角） */
	private volatile boolean isLeader = false;
	/** 当前是否处于 SUSPENDED 等“可疑”状态 */
	private volatile boolean suspended = false;

	/** 续约调度句柄 */
	private volatile ScheduledFuture<?> renewFuture;
	/** 抢锁重试调度句柄 */
	private volatile ScheduledFuture<?> acquireFuture;

	/** 任务注册中心 */
	private final LeaderTaskRegistry taskRegistry = new LeaderTaskRegistry();

	/** 当前持有的 Redis 锁（全局执行权租约） */
	private volatile RLock currentLock;
	/** 当前锁 key */
	private volatile String currentLockKey;
	/** 连续续约失败次数计数器 */
	private volatile int consecutiveRenewFailures = 0;

	/**
	 * 构造函数（使用默认参数）
	 *
	 * @param curator   CuratorFramework 实例
	 * @param latchPath LeaderLatch 路径（ZK）
	 */
	public LeaderLeaseManager(CuratorFramework curator, String latchPath) {
		this(curator, latchPath, DEFAULT_LEASE_TTL_SECONDS, DEFAULT_RENEW_PERIOD_SECONDS, DEFAULT_SAFETY_MARGIN_SECONDS,
				DEFAULT_MAX_RENEW_FAILURES, DEFAULT_RETRY_MIN_BACKOFF_MS, DEFAULT_RETRY_MAX_BACKOFF_MS);
	}

	/**
	 * 构造函数（自定义参数）
	 *
	 * @param curator              CuratorFramework 实例
	 * @param latchPath            LeaderLatch 路径（ZK）
	 * @param leaseTtlSeconds      Redis 锁租约 TTL（秒）
	 * @param renewPeriodSeconds   续约周期（秒）
	 * @param safetyMarginSeconds  安全余量（秒）
	 * @param maxRenewFailures     连续续约失败阈值
	 * @param retryMinBackoffMs    抢锁/续约失败后的最小退避（毫秒）
	 * @param retryMaxBackoffMs    抢锁/续约失败后的最大退避（毫秒）
	 */
	public LeaderLeaseManager(CuratorFramework curator, String latchPath, int leaseTtlSeconds, int renewPeriodSeconds,
			int safetyMarginSeconds, int maxRenewFailures, int retryMinBackoffMs, int retryMaxBackoffMs) {
		this.curator = Objects.requireNonNull(curator, "curator");
		this.latchPath = Objects.requireNonNull(latchPath, "latchPath");

		// 注意：尽早初始化 nodeId，避免监听器捕获到“未初始化”的字段
		this.nodeId = System.getProperty("node.id", UUID.randomUUID().toString());

		this.leaseTtlSeconds = leaseTtlSeconds;
		this.renewPeriodSeconds = renewPeriodSeconds;
		this.safetyMarginSeconds = safetyMarginSeconds;
		this.maxRenewFailures = maxRenewFailures;
		this.retryMinBackoffMs = retryMinBackoffMs;
		this.retryMaxBackoffMs = retryMaxBackoffMs;

		// LeaderLatch 使用 nodeId 作为参与者 ID
		this.latch = new LeaderLatch(this.curator, this.latchPath, this.nodeId);
	}

	/**
	 * 注册一个 Leader 任务（线程安全，多次调用可注册多个任务）
	 *
	 * @param task LeaderTask 实现
	 */
	public void registerTask(LeaderTask task) {
		taskRegistry.register(task);
	}

	/**
	 * 启动框架：开始监听 ZK 状态、参与选主、尝试获取执行权并调度任务
	 *
	 * @throws Exception LeaderLatch 启动异常
	 */
	public void start() throws Exception {
		if (running)
			return;
		running = true;

		// 注册 ZK 连接状态监听
		curator.getConnectionStateListenable().addListener(connectionStateListener);
		// 注册 Leader 切换监听
		latch.addListener(leaderLatchListener);
		// 参与选主
		latch.start();

		log.info("[LeaderLease] manager started. nodeId={}, latchPath={}, leaseTtl={}s, renew={}s", nodeId, latchPath, leaseTtlSeconds,
				renewPeriodSeconds);
	}

	/**
	 * 停止框架：停止续约与抢锁、停止任务、释放锁、退出选主
	 */
	public void stop() {
		running = false;
		cancelAcquire();
		cancelRenew();

		stopAllTasksAndUnlock("stop()");
		try {
			latch.close();
		} catch (Exception e) {
			log.warn("[LeaderLease] close latch error", e);
		}
		log.info("[LeaderLease] manager stopped. nodeId={}", nodeId);
	}

	/**
	 * ZK Leader 切换监听器
	 */
	private final LeaderLatchListener leaderLatchListener = new LeaderLatchListener() {
		@Override
		public void isLeader() {
			if (!running)
				return;
			log.info("[LeaderLease] I am leader by ZK. nodeId={}", nodeId);
			isLeader = true;
			// 成为 ZK Leader 后，尝试获取 Redis 租约（执行权），成功才会启动任务
			scheduleAcquireWithBackoff();
		}

		@Override
		public void notLeader() {
			log.info("[LeaderLease] I am not leader by ZK. nodeId={}", nodeId);
			isLeader = false;
			// 丢失领导权：停止续约、停止任务、释放锁、停止抢占动作
			stopAllTasksAndUnlock("notLeader");
			cancelAcquire();
		}
	};

	/**
	 * ZK 连接状态监听：
	 * <ul>
	 *   <li>SUSPENDED：网络可疑，立即暂停/停止任务并停止续约（不立即释放锁，最坏让权窗口由 TTL 限制）。</li>
	 *   <li>LOST：会话丢失，等价于 notLeader，停止任务并释放锁。</li>
	 *   <li>RECONNECTED/READ_ONLY：恢复连接；若仍自认是 Leader，则重新尝试确保租约后再恢复任务。</li>
	 * </ul>
	 */
	private final ConnectionStateListener connectionStateListener = new ConnectionStateListener() {
		@Override
		public void stateChanged(CuratorFramework client, ConnectionState newState) {
			if (!running)
				return;
			log.info("[LeaderLease] ZK connection state -> {}. nodeId={}", newState, nodeId);
			switch (newState) {
			case SUSPENDED:
				suspended = true;
				pauseTasksAndStopRenew("SUSPENDED");
				break;
			case LOST:
				suspended = true;
				isLeader = false;
				stopAllTasksAndUnlock("LOST");
				cancelAcquire();
				break;
			case RECONNECTED:
			case READ_ONLY:
				suspended = false;
				if (isLeader) {
					// 如果仍是 Leader，确保租约在手（或重新获取）
					scheduleAcquireWithBackoff();
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 生成全局执行权租约的 Redis 锁 key
	 *
	 * @return 锁 key
	 */
	private String globalLeaseKey() {
		return "leader:global:" + latchPath;
	}

	/**
	 * 带退避的获取租约调度：避免在租约到期瞬间所有候选打爆 Redis
	 */
	private void scheduleAcquireWithBackoff() {
		if (!running || !isLeader)
			return;
		cancelAcquire();
		int delay = ThreadLocalRandom.current().nextInt(retryMinBackoffMs, retryMaxBackoffMs + 1);
		acquireFuture = scheduler.scheduleTask(this::acquireLeaseAndMaybeRun, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * 取消抢锁重试任务
	 */
	private void cancelAcquire() {
		if (acquireFuture != null) {
			acquireFuture.cancel(false);
			acquireFuture = null;
		}
	}

	/**
	 * 尝试获取租约并在成功时启动任务
	 */
	private void acquireLeaseAndMaybeRun() {
		if (!running || !isLeader || suspended)
			return;

		final String key = globalLeaseKey();
		try {
			// 尝试以 leaseTtlSeconds 获取锁（不等待）。获取成功才启动任务。
			RLock lock = LockUtil.tryLockNoWait(leaseTtlSeconds, key);
			if (lock != null) {
				currentLock = lock;
				currentLockKey = key;
				consecutiveRenewFailures = 0;

				log.info("[LeaderLease] acquired lease. key={}, ttl={}s, nodeId={}", key, leaseTtlSeconds, nodeId);

				// 启动续约任务
				scheduleRenewTask();
				// 启动所有注册的 Leader 任务（各任务内需批间调用 ensureLeaseHealthy）
				taskRegistry.startAll(this);
			} else {
				log.info("[LeaderLease] lease busy. key={}, will retry", key);
				scheduleAcquireWithBackoff();
			}
		} catch (Throwable t) {
			log.warn("[LeaderLease] acquire lease error, will retry. key={}", key, t);
			scheduleAcquireWithBackoff();
		}
	}

	/**
	 * 创建并启动续约定时任务
	 */
	private void scheduleRenewTask() {
		cancelRenew();
		// 采用固定频率执行续约检查
		renewFuture = scheduler.scheduleAtFixedRate(this::safeRenewTick, renewPeriodSeconds, TimeUnit.SECONDS);
	}

	/**
	 * 取消续约定时任务
	 */
	private void cancelRenew() {
		if (renewFuture != null) {
			renewFuture.cancel(false);
			renewFuture = null;
		}
	}

	/**
	 * 续约定时任务的安全包装
	 */
	private void safeRenewTick() {
		try {
			renewTick();
		} catch (Throwable t) {
			log.warn("[LeaderLease] renew tick error", t);
		}
	}

	/**
	 * 续约逻辑：
	 * <ul>
	 *   <li>非 Leader 或处于 SUSPENDED 时，跳过续约（由上层状态机决定暂停）。</li>
	 *   <li>remainTimeToLive <= 0 表示租约已过期或不可用，立即停止任务并尝试重新获取。</li>
	 *   <li>若剩余 TTL 小于安全余量，尝试通过“同线程重入一次并释放重入”来刷新 TTL（Redisson 常见技巧）。</li>
	 *   <li>连续刷新失败计数超过阈值，立即停止任务并释放锁，以尽快让出执行权。</li>
	 * </ul>
	 */
	private void renewTick() {
		if (!running)
			return;
		if (!isLeader || suspended) {
			log.info("[LeaderLease] skip renew: isLeader={}, suspended={}", isLeader, suspended);
			return;
		}
		if (currentLock == null || currentLockKey == null) {
			log.info("[LeaderLease] no current lease, skip renew.");
			return;
		}

		try {
			long remainMs = currentLock.remainTimeToLive();
			if (remainMs <= 0) {
				log.warn("[LeaderLease] lease expired or unknown. remainMs<=0, stopping tasks.");
				stopAllTasksAndUnlock("leaseExpired");
				scheduleAcquireWithBackoff();
				return;
			}

			// 接近安全余量：尝试刷新 TTL
			if (TimeUnit.MILLISECONDS.toSeconds(remainMs) <= safetyMarginSeconds) {
				boolean refreshed = false;
				try {
					// 重入 -> 释放重入层，借此刷新 TTL
					currentLock.lock(leaseTtlSeconds, TimeUnit.SECONDS);
					currentLock.unlock();
					refreshed = true;
				} catch (Throwable e) {
					log.warn("[LeaderLease] refresh lease failed (re-enter). key={}", currentLockKey, e);
				}

				if (!refreshed) {
					consecutiveRenewFailures++;
					log.warn("[LeaderLease] lease near expiry, refresh failed. remainMs={}, failures={}", remainMs,
							consecutiveRenewFailures);
					if (consecutiveRenewFailures >= maxRenewFailures) {
						log.error("[LeaderLease] renew failures exceed threshold, stop tasks and release lease.");
						stopAllTasksAndUnlock("renewFailuresExceeded");
						scheduleAcquireWithBackoff();
						return;
					}
				} else {
					consecutiveRenewFailures = 0;
					log.debug("[LeaderLease] lease refreshed OK. key={}, remainMs(before)={}", currentLockKey, remainMs);
				}
			} else {
				// 正常路径：清零失败计数
				consecutiveRenewFailures = 0;
			}
		} catch (Throwable t) {
			consecutiveRenewFailures++;
			log.warn("[LeaderLease] renew check error. failures={}", consecutiveRenewFailures, t);
			if (consecutiveRenewFailures >= maxRenewFailures) {
				log.error("[LeaderLease] renew failures exceed threshold (exception path), stop tasks.");
				stopAllTasksAndUnlock("renewExceptionsExceeded");
				scheduleAcquireWithBackoff();
			}
		}
	}

	/**
	 * 暂停任务并停止续约（响应 SUSPENDED）
	 *
	 * @param reason 原因描述
	 */
	private void pauseTasksAndStopRenew(String reason) {
		cancelRenew();
		taskRegistry.pauseAll(reason);
		// 此处不强制释放锁，让 TTL 作为最坏让权上界；如需更激进，可在此解锁以缩短无主窗口。
	}

	/**
	 * 停止所有任务并释放当前持有的锁（响应 notLeader/LOST/续约失败等）
	 *
	 * @param reason 原因描述
	 */
	private void stopAllTasksAndUnlock(String reason) {
		cancelRenew();
		taskRegistry.stopAll(reason);
		if (currentLock != null) {
			try {
				currentLock.unlock();
			} catch (Throwable e) {
				log.warn("[LeaderLease] unlock error. key={}", currentLockKey, e);
			}
		}
		currentLock = null;
		currentLockKey = null;
		consecutiveRenewFailures = 0;
	}

	/**
	 * 创建任务上下文
	 *
	 * @param taskName 任务名
	 * @return LeaderTaskContext
	 */
	public LeaderTaskContext createTaskContext(String taskName) {
		return new LeaderTaskContext(this, taskName);
	}

	/**
	 * 是否满足“ZK 是 Leader 且持有租约”的执行条件
	 *
	 * @return true 表示可以执行
	 */
	boolean isLeaderAndLeaseHeld() {
		return running && isLeader && !suspended && currentLock != null;
	}

	/**
	 * 查询当前租约剩余寿命（毫秒）
	 *
	 * @return 剩余毫秒；异常或无租约返回 -1
	 */
	long currentLeaseRemainMillis() {
		try {
			if (currentLock == null)
				return -1;
			return currentLock.remainTimeToLive();
		} catch (Throwable t) {
			return -1;
		}
	}

	/** @return 租约 TTL（秒） */
	int leaseTtlSeconds() {
		return leaseTtlSeconds;
	}

	/** @return 安全余量（秒） */
	int safetyMarginSeconds() {
		return safetyMarginSeconds;
	}

	/** @return 当前节点 ID */
	String nodeId() {
		return nodeId;
	}

	/**
	 * 任务注册中心：负责保存、启动、暂停、停止任务
	 */
	private static class LeaderTaskRegistry {
		private final java.util.concurrent.ConcurrentHashMap<String, LeaderTaskRunner> tasks = new java.util.concurrent.ConcurrentHashMap<>();

		/**
		 * 注册任务
		 *
		 * @param task 任务实现
		 */
		void register(LeaderTask task) {
			Objects.requireNonNull(task, "task");
			if (tasks.putIfAbsent(task.name(), new LeaderTaskRunner(task)) != null) {
				throw new IllegalStateException("duplicated task name: " + task.name());
			}
		}

		/**
		 * 启动所有任务（前提：当前实例满足 canExecute）
		 *
		 * @param manager LeaderLeaseManager
		 */
		void startAll(LeaderLeaseManager manager) {
			tasks.values().forEach(r -> r.startIfEligible(manager));
		}

		/**
		 * 暂停所有任务
		 *
		 * @param reason 原因
		 */
		void pauseAll(String reason) {
			tasks.values().forEach(r -> r.pause(reason));
		}

		/**
		 * 停止所有任务
		 *
		 * @param reason 原因
		 */
		void stopAll(String reason) {
			tasks.values().forEach(r -> r.stop(reason));
		}
	}

	/**
	 * 任务运行包装：负责状态机与与 Scheduler 的衔接
	 */
	private static class LeaderTaskRunner {
		private final LeaderTask task;
		private volatile boolean running = false;

		LeaderTaskRunner(LeaderTask task) {
			this.task = task;
		}

		/**
		 * 符合条件则启动任务（仅启动一次）
		 *
		 * @param manager LeaderLeaseManager
		 */
		void startIfEligible(LeaderLeaseManager manager) {
			if (running)
				return;
			if (!manager.isLeaderAndLeaseHeld())
				return;

			running = true;
			LeaderTaskContext ctx = manager.createTaskContext(task.name());
			try {
				task.onStart(ctx);
			} catch (Throwable t) {
				running = false;
				manager.logTaskError(task.name(), "onStart", t);
				return;
			}
			// 交给全局 Scheduler 执行主循环
			SchedulerService.getInstance().executeTask(() -> {
				try {
					task.run(ctx);
				} catch (Throwable t) {
					manager.logTaskError(task.name(), "run", t);
				} finally {
					running = false;
					try {
						task.onStop("runFinished");
					} catch (Throwable ignore) {
					}
				}
			});
		}

		/**
		 * 暂停任务（可由任务自行决定如何暂停，例如仅打标记）
		 *
		 * @param reason 原因
		 */
		void pause(String reason) {
			if (!running)
				return;
			try {
				task.onPause(reason);
			} catch (Throwable ignore) {
			}
		}

		/**
		 * 停止任务（幂等）
		 *
		 * @param reason 原因
		 */
		void stop(String reason) {
			if (!running)
				return;
			running = false;
			try {
				task.onStop(reason);
			} catch (Throwable ignore) {
			}
		}
	}

	/**
	 * 任务日志辅助
	 *
	 * @param taskName 任务名
	 * @param phase    阶段（onStart/run 等）
	 * @param t        异常
	 */
	void logTaskError(String taskName, String phase, Throwable t) {
		log.error("[LeaderLease][task={}] {} error: {}", taskName, phase, t.getMessage(), t);
	}

	/**
	 * Leader 任务上下文：
	 * <ul>
	 *   <li>提供 canExecute() 判断，要求“ZK 是 Leader 且持有租约”。</li>
	 *   <li>提供 ensureLeaseHealthy()，在每个批次前校验剩余 TTL 是否高于安全余量。</li>
	 *   <li>暴露 nodeId 与 taskName 方便日志与诊断。</li>
	 * </ul>
	 */
	public static class LeaderTaskContext {
		private final LeaderLeaseManager manager;
		private final String taskName;

		LeaderTaskContext(LeaderLeaseManager manager, String taskName) {
			this.manager = manager;
			this.taskName = taskName;
		}

		/**
		 * 是否允许执行（双条件）
		 *
		 * @return true 允许，false 不允许
		 */
		public boolean canExecute() {
			return manager.isLeaderAndLeaseHeld();
		}

		/**
		 * 若不允许执行则抛出异常
		 *
		 * @throws IllegalStateException 非 Leader 或未持有租约
		 */
		public void ensureCanExecute() {
			if (!canExecute()) {
				throw new IllegalStateException("Not allowed to execute: not leader or lease missing. task=" + taskName);
			}
		}

		/**
		 * 校验租约健康度：剩余 TTL 必须大于安全余量
		 *
		 * @throws IllegalStateException 租约已过期或剩余寿命低于安全余量且无法恢复
		 */
		public void ensureLeaseHealthy() {
			long remainMs = manager.currentLeaseRemainMillis();
			if (remainMs <= 0) {
				throw new IllegalStateException("Lease expired or unknown. task=" + taskName);
			}
			long marginMs = TimeUnit.SECONDS.toMillis(manager.safetyMarginSeconds());
			if (remainMs <= marginMs) {
				// 给续约线程一点时间（轻微让步），再检查一次
				sleepQuiet(Duration.ofMillis(200));
				long remain2 = manager.currentLeaseRemainMillis();
				if (remain2 <= marginMs) {
					throw new IllegalStateException("Lease near expiry, margin violated. task=" + taskName + ", remainMs=" + remain2);
				}
			}
		}

		/**
		 * @return 当前节点 ID
		 */
		public String nodeId() {
			return manager.nodeId();
		}

		/**
		 * @return 任务名
		 */
		public String taskName() {
			return taskName;
		}

		private void sleepQuiet(Duration d) {
			try {
				Thread.sleep(d.toMillis());
			} catch (InterruptedException ignored) {
			}
		}
	}
}
