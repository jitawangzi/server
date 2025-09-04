package cn.game.core.leader;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
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
 *   <li>提供通用扩展点：可注册多个 Leader 任务；同时新增 LeaderStateListener，供“事件驱动”业务订阅状态。</li>
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

    /** 节点唯一标识 */
    private String nodeId;

    /** 任务调度器 */
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
    /** 当前是否为 ZK Leader */
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

    // 状态监听器（新增）
    private final CopyOnWriteArrayList<LeaderStateListener> stateListeners = new CopyOnWriteArrayList<>();
    private volatile boolean leaseHeldLastNotified = false;

    /**
     * 构造函数（使用默认参数）
     */
    public LeaderLeaseManager(CuratorFramework curator, String latchPath) {
        this(curator, latchPath,
                DEFAULT_LEASE_TTL_SECONDS,
                DEFAULT_RENEW_PERIOD_SECONDS,
                DEFAULT_SAFETY_MARGIN_SECONDS,
                DEFAULT_MAX_RENEW_FAILURES,
                DEFAULT_RETRY_MIN_BACKOFF_MS,
                DEFAULT_RETRY_MAX_BACKOFF_MS);
    }

    /**
     * 构造函数（自定义参数）
     */
    public LeaderLeaseManager(CuratorFramework curator,
                              String latchPath,
                              int leaseTtlSeconds,
                              int renewPeriodSeconds,
                              int safetyMarginSeconds,
                              int maxRenewFailures,
                              int retryMinBackoffMs,
                              int retryMaxBackoffMs) {
        this.curator = Objects.requireNonNull(curator, "curator");
        this.latchPath = Objects.requireNonNull(latchPath, "latchPath");

        this.nodeId = System.getProperty("node.id", UUID.randomUUID().toString());

        this.leaseTtlSeconds = leaseTtlSeconds;
        this.renewPeriodSeconds = renewPeriodSeconds;
        this.safetyMarginSeconds = safetyMarginSeconds;
        this.maxRenewFailures = maxRenewFailures;
        this.retryMinBackoffMs = retryMinBackoffMs;
        this.retryMaxBackoffMs = retryMaxBackoffMs;

        this.latch = new LeaderLatch(this.curator, this.latchPath, this.nodeId);
    }

    // ================= 监听器管理（新增） =================
    public void addLeaderStateListener(LeaderStateListener l) {
        if (l != null) stateListeners.add(l);
    }

    public void removeLeaderStateListener(LeaderStateListener l) {
        stateListeners.remove(l);
    }

    private void notifyLeaderChanged(boolean leader) {
        for (LeaderStateListener l : stateListeners) {
            try { l.onLeaderChanged(leader); } catch (Throwable ignore) {}
        }
    }

    private void notifyLeaseHeld(boolean leaseHeld, String reason) {
        leaseHeldLastNotified = leaseHeld;
        for (LeaderStateListener l : stateListeners) {
            try { l.onLeaseStateChanged(leaseHeld, reason); } catch (Throwable ignore) {}
        }
    }

    // ================= 生命周期 =================
    public void registerTask(LeaderTask task) {
        taskRegistry.register(task);
    }

    public void start() throws Exception {
        if (running) return;
        running = true;

        curator.getConnectionStateListenable().addListener(connectionStateListener);
        latch.addListener(leaderLatchListener);
        latch.start();

        logInfo("manager started. nodeId=%s, latchPath=%s, leaseTtl=%ss, renew=%ss",
                nodeId, latchPath, leaseTtlSeconds, renewPeriodSeconds);
    }

    public void stop() {
        running = false;
        cancelAcquire();
        cancelRenew();

        stopAllTasksAndUnlock("stop()");
        try {
            latch.close();
        } catch (Exception e) {
            logWarn("close latch error", e);
        }
        logInfo("manager stopped. nodeId=%s", nodeId);
    }

    // ================= 监听器实现 =================
    private final LeaderLatchListener leaderLatchListener = new LeaderLatchListener() {
        @Override
        public void isLeader() {
            if (!running) return;
            logInfo("I am leader by ZK. nodeId=%s", nodeId);
            isLeader = true;
            notifyLeaderChanged(true);
            scheduleAcquireWithBackoff();
        }

        @Override
        public void notLeader() {
            logInfo("I am not leader by ZK. nodeId=%s", nodeId);
            isLeader = false;
            notifyLeaderChanged(false);
            stopAllTasksAndUnlock("notLeader");
            cancelAcquire();
            if (leaseHeldLastNotified) {
                notifyLeaseHeld(false, "notLeader");
            }
        }
    };

    private final ConnectionStateListener connectionStateListener = (client, newState) -> {
        if (!running) return;
        logInfo("ZK connection state -> %s. nodeId=%s", newState, nodeId);
        switch (newState) {
            case SUSPENDED:
                suspended = true;
                pauseTasksAndStopRenew("SUSPENDED");
                if (leaseHeldLastNotified) {
                    notifyLeaseHeld(false, "SUSPENDED");
                }
                break;
            case LOST:
                suspended = true;
                isLeader = false;
                notifyLeaderChanged(false);
                stopAllTasksAndUnlock("LOST");
                cancelAcquire();
                if (leaseHeldLastNotified) {
                    notifyLeaseHeld(false, "LOST");
                }
                break;
            case RECONNECTED:
            case READ_ONLY:
                suspended = false;
                if (isLeader) {
                    scheduleAcquireWithBackoff();
                }
                break;
            default:
                break;
        }
    };

    // ================= 抢占与续约 =================
    private String globalLeaseKey() {
        return "leader:global:" + latchPath;
    }

    private void scheduleAcquireWithBackoff() {
        if (!running || !isLeader) return;
        cancelAcquire();
        int delay = ThreadLocalRandom.current().nextInt(retryMinBackoffMs, retryMaxBackoffMs + 1);
        acquireFuture = scheduler.scheduleTask(this::acquireLeaseAndMaybeRun, delay, TimeUnit.MILLISECONDS);
    }

    private void cancelAcquire() {
        if (acquireFuture != null) {
            acquireFuture.cancel(false);
            acquireFuture = null;
        }
    }

    private void acquireLeaseAndMaybeRun() {
        if (!running || !isLeader || suspended) return;

        final String key = globalLeaseKey();
        try {
            RLock lock = LockUtil.tryLockNoWait(leaseTtlSeconds, key);
            if (lock != null) {
                currentLock = lock;
                currentLockKey = key;
                consecutiveRenewFailures = 0;

                logInfo("acquired lease. key=%s, ttl=%ss, nodeId=%s", key, leaseTtlSeconds, nodeId);
                notifyLeaseHeld(true, "acquired");

                scheduleRenewTask();
                taskRegistry.startAll(this);
            } else {
                logInfo("lease busy. key=%s, will retry", key);
                scheduleAcquireWithBackoff();
            }
        } catch (Throwable t) {
            logWarn("acquire lease error, will retry. key=" + key, t);
            scheduleAcquireWithBackoff();
        }
    }

    private void scheduleRenewTask() {
        cancelRenew();
        renewFuture = scheduler.scheduleAtFixedRate(this::safeRenewTick, renewPeriodSeconds, TimeUnit.SECONDS);
    }

    private void cancelRenew() {
        if (renewFuture != null) {
            renewFuture.cancel(false);
            renewFuture = null;
        }
    }

    private void safeRenewTick() {
        try {
            renewTick();
        } catch (Throwable t) {
            logWarn("renew tick error", t);
        }
    }

    private void renewTick() {
        if (!running) return;
        if (!isLeader || suspended) {
            logInfo("skip renew: isLeader=%s, suspended=%s", isLeader, suspended);
            return;
        }
        if (currentLock == null || currentLockKey == null) {
            logInfo("no current lease, skip renew.");
            return;
        }

        try {
            long remainMs = currentLock.remainTimeToLive();
            if (remainMs <= 0) {
                logWarn("lease expired or unknown. remainMs<=0, stopping tasks.",null);
                stopAllTasksAndUnlock("leaseExpired");
                notifyLeaseHeld(false, "leaseExpired");
                scheduleAcquireWithBackoff();
                return;
            }

            if (TimeUnit.MILLISECONDS.toSeconds(remainMs) <= safetyMarginSeconds) {
                boolean refreshed = false;
                try {
                    currentLock.lock(leaseTtlSeconds, TimeUnit.SECONDS);
                    currentLock.unlock();
                    refreshed = true;
                } catch (Throwable e) {
                    logWarn("refresh lease failed (re-enter). key=" + currentLockKey, e);
                }

                if (!refreshed) {
                    consecutiveRenewFailures++;
                    logWarn("lease near expiry, refresh failed. remainMs=" + remainMs + ", failures=" + consecutiveRenewFailures, null);
                    if (consecutiveRenewFailures >= maxRenewFailures) {
                        logWarn("renew failures exceed threshold, stop tasks and release lease.", null);
                        stopAllTasksAndUnlock("renewFailuresExceeded");
                        notifyLeaseHeld(false, "renewFailuresExceeded");
                        scheduleAcquireWithBackoff();
                        return;
                    }
                } else {
                    consecutiveRenewFailures = 0;
                    logDebug("lease refreshed OK. key=%s, remainMs(before)=%s", currentLockKey, remainMs);
                }
            } else {
                consecutiveRenewFailures = 0;
            }
        } catch (Throwable t) {
            consecutiveRenewFailures++;
            logWarn("renew check error. failures=" + consecutiveRenewFailures, t);
            if (consecutiveRenewFailures >= maxRenewFailures) {
                logWarn("renew failures exceed threshold (exception path), stop tasks.", null);
                stopAllTasksAndUnlock("renewExceptionsExceeded");
                notifyLeaseHeld(false, "renewExceptionsExceeded");
                scheduleAcquireWithBackoff();
            }
        }
    }

    private void pauseTasksAndStopRenew(String reason) {
        cancelRenew();
        taskRegistry.pauseAll(reason);
    }

    private void stopAllTasksAndUnlock(String reason) {
        cancelRenew();
        taskRegistry.stopAll(reason);
        if (currentLock != null) {
            try {
                currentLock.unlock();
            } catch (Throwable e) {
                logWarn("unlock error. key=" + currentLockKey, e);
            }
        }
        currentLock = null;
        currentLockKey = null;
        consecutiveRenewFailures = 0;
    }

    // ================= 供业务查询的快照 =================
    /**
     * 快照：是否满足“ZK 是 Leader 且持有租约”的执行条件
     */
    public boolean isLeaderAndLeaseHeld() {
        return running && isLeader && !suspended && currentLock != null;
    }

    long currentLeaseRemainMillis() {
        try {
            if (currentLock == null) return -1;
            return currentLock.remainTimeToLive();
        } catch (Throwable t) {
            return -1;
        }
    }

    int leaseTtlSeconds() { return leaseTtlSeconds; }
    int safetyMarginSeconds() { return safetyMarginSeconds; }
    String nodeId() { return nodeId; }

    // ================= 任务注册与运行（与原有一致） =================
    private static class LeaderTaskRegistry {
        private final java.util.concurrent.ConcurrentHashMap<String, LeaderTaskRunner> tasks = new java.util.concurrent.ConcurrentHashMap<>();

        void register(LeaderTask task) {
            Objects.requireNonNull(task, "task");
            if (tasks.putIfAbsent(task.name(), new LeaderTaskRunner(task)) != null) {
                throw new IllegalStateException("duplicated task name: " + task.name());
            }
        }

        void startAll(LeaderLeaseManager manager) {
            tasks.values().forEach(r -> r.startIfEligible(manager));
        }

        void pauseAll(String reason) {
            tasks.values().forEach(r -> r.pause(reason));
        }

        void stopAll(String reason) {
            tasks.values().forEach(r -> r.stop(reason));
        }
    }

    private static class LeaderTaskRunner {
        private final LeaderTask task;
        private volatile boolean running = false;

        LeaderTaskRunner(LeaderTask task) {
            this.task = task;
        }

        void startIfEligible(LeaderLeaseManager manager) {
            if (running) return;
            if (!manager.isLeaderAndLeaseHeld()) return;

            running = true;
            LeaderTaskContext ctx = manager.createTaskContext(task.name());
            try {
                task.onStart(ctx);
            } catch (Throwable t) {
                running = false;
                manager.logTaskError(task.name(), "onStart", t);
                return;
            }
            SchedulerService.getInstance().executeTask(() -> {
                try {
                    task.run(ctx);
                } catch (Throwable t) {
                    manager.logTaskError(task.name(), "run", t);
                } finally {
                    running = false;
                    try {
                        task.onStop("runFinished");
                    } catch (Throwable ignore) {}
                }
            });
        }

        void pause(String reason) {
            if (!running) return;
            try {
                task.onPause(reason);
            } catch (Throwable ignore) {}
        }

        void stop(String reason) {
            if (!running) return;
            running = false;
            try {
                task.onStop(reason);
            } catch (Throwable ignore) {}
        }
    }

    // ================= Leader 任务上下文（保留，以兼容原有任务） =================
    public static class LeaderTaskContext {
        private final LeaderLeaseManager manager;
        private final String taskName;

        LeaderTaskContext(LeaderLeaseManager manager, String taskName) {
            this.manager = manager;
            this.taskName = taskName;
        }

        public boolean canExecute() {
            return manager.isLeaderAndLeaseHeld();
        }

        public void ensureCanExecute() {
            if (!canExecute()) {
                throw new IllegalStateException("Not allowed to execute: not leader or lease missing. task=" + taskName);
            }
        }

        public void ensureLeaseHealthy() {
            long remainMs = manager.currentLeaseRemainMillis();
            if (remainMs <= 0) {
                throw new IllegalStateException("Lease expired or unknown. task=" + taskName);
            }
            long marginMs = TimeUnit.SECONDS.toMillis(manager.safetyMarginSeconds());
            if (remainMs <= marginMs) {
                sleepQuiet(Duration.ofMillis(200));
                long remain2 = manager.currentLeaseRemainMillis();
                if (remain2 <= marginMs) {
                    throw new IllegalStateException("Lease near expiry, margin violated. task=" + taskName + ", remainMs=" + remain2);
                }
            }
        }

        public String nodeId() {
            return manager.nodeId();
        }

        public String taskName() {
            return taskName;
        }

        private void sleepQuiet(Duration d) {
            try { Thread.sleep(d.toMillis()); } catch (InterruptedException ignored) {}
        }
    }

    // ================= 日志辅助 =================
    void logTaskError(String taskName, String phase, Throwable t) {
        log.error("[LeaderLease][task={}] {} error: {}", taskName, phase, t.getMessage(), t);
    }

    private void logInfo(String fmt, Object... args) {
        log.info("[LeaderLease] " + String.format(fmt, args));
    }

    private void logWarn(String msg, Throwable t) {
        if (t == null) log.warn("[LeaderLease] " + msg);
        else log.warn("[LeaderLease] " + msg, t);
    }

    private void logDebug(String fmt, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug("[LeaderLease] " + String.format(fmt, args));
        }
    }

    // 工厂方法：供 LeaderTaskRunner 创建上下文
    public LeaderTaskContext createTaskContext(String taskName) {
        return new LeaderTaskContext(this, taskName);
    }
}

