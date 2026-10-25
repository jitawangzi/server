package cn.game.core.execute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.util.DateUtil;

/**
 * 执行监控，收集和报告执行统计信息
 */
public class ExecutionMonitor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionMonitor.class.getName());
    
    private final TaskExecutorService executorService;
    private final TaskExecutionConfig config;
    
    // 全局统计
    private final AtomicInteger totalActiveMailboxes = new AtomicInteger(0);
    private final AtomicLong totalCompletedTasks = new AtomicLong(0);
    private final AtomicLong totalFailedTasks = new AtomicLong(0);
    private final AtomicLong totalTimeoutTasks = new AtomicLong(0);
    /** 清理与阻塞诊断使用同一时钟，测试通过推进时钟进入真实扫描分支。 */
    private final LongSupplier clock;
    /** 上次清理开始时间，单位为毫秒。 */
    private final AtomicLong lastCleanupTime;
    /** 本监控器创建的后台线程，服务关闭时结束，避免遗留扫描旧邮箱。 */
    private volatile Thread monitorThread;
    
    // 监听器
    private final List<Consumer<MonitorSnapshot>> snapshotListeners = new ArrayList<>();
    
    /**
     * 使用项目时间源创建监控器。
     * @param executorService 被监控的执行服务
     * @param config 清理周期与告警阈值
     */
    public ExecutionMonitor(TaskExecutorService executorService, TaskExecutionConfig config) {
        this(executorService, config, DateUtil::currentTimeMillis);
    }

    /**
     * 注入监控时钟，不改变业务任务的执行方式。
     * @param executorService 被监控的执行服务
     * @param config 扫描与告警配置
     * @param clock 返回毫秒时间的时钟
     */
    ExecutionMonitor(TaskExecutorService executorService, TaskExecutionConfig config, LongSupplier clock) {
        this.executorService = executorService;
        this.config = config;
        this.clock = clock;
        this.lastCleanupTime = new AtomicLong(clock.getAsLong());
    }
    
    /**
     * 开始定期监控
     * @param intervalMs 监控间隔（毫秒）
     */
    public void startPeriodicMonitoring(long intervalMs) {
        monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 检查邮箱状态并清理闲置邮箱
                    checkMailboxes();
                    
                    // 收集并报告统计数据
                    MonitorSnapshot snapshot = collectStatistics();
                    reportStatistics(snapshot);
                    
                    // 通知监听器
                    notifyListeners(snapshot);
                    
                    // 等待下一个监控周期
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    LOGGER.warn("Execution monitor interrupted; stopping monitoring", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
					LOGGER.error("Error during execution monitor cycle", e);
                }
            }
        }, "ExecutionMonitor-Thread");
        
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /** 停止本实例的后台监控，不影响共享调度器或其他服务。 */
    public void stopPeriodicMonitoring() {
        Thread thread = monitorThread;
        if (thread != null) { thread.interrupt(); }
    }
    
    /**
     * 检查邮箱状态，清理闲置邮箱，检测死锁
     */
    public void checkMailboxes() {
        long now = clock.getAsLong();
        long idleTimeout = config.getIdleTimeoutMs();
        long deadlockThreshold = config.getDeadlockDetectionThresholdMs();
        
        // 检查是否应该执行清理
        if (now - lastCleanupTime.get() >= config.getMailboxCleanupIntervalMs()) {
            lastCleanupTime.set(now);
            
            // 获取所有邮箱
            ConcurrentMap<Long, ActorMailbox> mailboxes = executorService.getMailboxes();
            List<ActorMailbox> toRemove = new ArrayList<>();
            
            // 检查每个邮箱
            for (ActorMailbox mailbox : mailboxes.values()) {
                long idleTime = now - mailbox.getLastAccessTime();
                long processingTime = now - mailbox.processingStartedAt();
                
                // 检测闲置邮箱
                if (mailbox.isEmpty() && !mailbox.isProcessing() && idleTime > idleTimeout) {
                    toRemove.add(mailbox);
                }
                
                // 检测潜在死锁
                Thread activeThread = mailbox.processingThread();
                if (mailbox.isProcessing() && activeThread != null && processingTime > deadlockThreshold) {
					LOGGER.warn(String.format(
                        "Potential deadlock detected for entity %d - processing for %d ms with %d tasks in queue",
                        mailbox.getEntityId(), processingTime, mailbox.getQueueSize()
                    ));
                    
                    // 超时只说明需要诊断，不能证明原任务已经终止或允许另一消费者进入。
                    LOGGER.warn("Mailbox {} blocked on thread {}: {}", mailbox.getEntityId(), activeThread.getName(),
                            Arrays.toString(activeThread.getStackTrace()));
                }
            }
            
            // 移除闲置邮箱
            for (ActorMailbox mailbox : toRemove) {
                if (executorService.getMailboxManager().removeMailbox(mailbox, idleTimeout)) {
					LOGGER.info("Removed idle mailbox for entity " + mailbox.getEntityId());
                }
            }
        }
    }
    
    /**
     * 收集统计数据
     * @return 监控快照
     */
    public MonitorSnapshot collectStatistics() {
        MonitorSnapshot snapshot = new MonitorSnapshot();
        ConcurrentMap<Long, ActorMailbox> mailboxes = executorService.getMailboxes();
        
        // 更新全局统计
        totalActiveMailboxes.set(mailboxes.size());
        
        // 收集各邮箱的统计信息
        for (ActorMailbox mailbox : mailboxes.values()) {
            MailboxStats stats = mailbox.getStats();
            
            // 更新全局计数器
            totalCompletedTasks.set(totalCompletedTasks.get() + stats.completedTasks.get());
            totalFailedTasks.set(totalFailedTasks.get() + stats.failedTasks.get());
            totalTimeoutTasks.set(totalTimeoutTasks.get() + stats.timeoutTasks.get());
            
            // 添加到快照
            snapshot.mailboxStats.put(mailbox.getEntityId(), new MailboxStatSnapshot(mailbox));
        }
        
        // 设置全局统计
        snapshot.totalMailboxes = totalActiveMailboxes.get();
        snapshot.totalCompletedTasks = totalCompletedTasks.get();
        snapshot.totalFailedTasks = totalFailedTasks.get();
        snapshot.totalTimeoutTasks = totalTimeoutTasks.get();
        snapshot.timestamp = System.currentTimeMillis();
        
        return snapshot;
    }
    
    /**
     * 报告统计数据
     * @param snapshot 监控快照
     */
    private void reportStatistics(MonitorSnapshot snapshot) {
		if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format(
                "Execution stats: mailboxes=%d, completed=%d, failed=%d, timeout=%d",
                snapshot.totalMailboxes, snapshot.totalCompletedTasks,
                snapshot.totalFailedTasks, snapshot.totalTimeoutTasks
            ));
            
            // 找出最繁忙的邮箱
            List<Map.Entry<Long, MailboxStatSnapshot>> busyMailboxes = snapshot.mailboxStats.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().queueSize, a.getValue().queueSize))
                .limit(5)
                .collect(Collectors.toList());
            
            if (!busyMailboxes.isEmpty()) {
                LOGGER.info("Top 5 busiest mailboxes:");
                for (Map.Entry<Long, MailboxStatSnapshot> entry : busyMailboxes) {
                    MailboxStatSnapshot stat = entry.getValue();
                    LOGGER.info(String.format(
                        "Entity %d: queue=%d, processing=%s, completed=%d, failed=%d, avg=%.2f ms",
                        entry.getKey(), stat.queueSize, stat.processing,
                        stat.completedTasks, stat.failedTasks,
                        stat.averageProcessingTimeNs / 1_000_000.0
                    ));
                }
            }
        }
    }
    
    /**
     * 添加监控快照监听器
     * @param listener 监听器
     */
    public void addSnapshotListener(Consumer<MonitorSnapshot> listener) {
        snapshotListeners.add(listener);
    }
    
    /**
     * 移除监控快照监听器
     * @param listener 监听器
     */
    public void removeSnapshotListener(Consumer<MonitorSnapshot> listener) {
        snapshotListeners.remove(listener);
    }
    
    /**
     * 通知所有监听器
     * @param snapshot 监控快照
     */
    private void notifyListeners(MonitorSnapshot snapshot) {
        for (Consumer<MonitorSnapshot> listener : snapshotListeners) {
            try {
                listener.accept(snapshot);
            } catch (Exception e) {
				LOGGER.error("Error notifying monitor listener", e);
            }
        }
    }
    
    /**
     * 监控快照，包含某一时刻的统计信息
     */
    public static class MonitorSnapshot {
        public long timestamp;
        public int totalMailboxes;
        public long totalCompletedTasks;
        public long totalFailedTasks;
        public long totalTimeoutTasks;
        public Map<Long, MailboxStatSnapshot> mailboxStats = new HashMap<>();
    }
    
    /**
     * 邮箱统计快照
     */
    public static class MailboxStatSnapshot {
        public final long entityId;
        public final int queueSize;
        public final boolean processing;
        public final int completedTasks;
        public final int failedTasks;
        public final int timeoutTasks;
        public final double averageProcessingTimeNs;
        public final long maxProcessingTimeNs;
        public final String slowestTaskDescription;
        public final Throwable lastError;
        
        public MailboxStatSnapshot(ActorMailbox mailbox) {
            MailboxStats stats = mailbox.getStats();
            this.entityId = mailbox.getEntityId();
            this.queueSize = mailbox.getQueueSize();
            this.processing = mailbox.isProcessing();
            this.completedTasks = stats.completedTasks.get();
            this.failedTasks = stats.failedTasks.get();
            this.timeoutTasks = stats.timeoutTasks.get();
            this.averageProcessingTimeNs = stats.getAverageProcessingTimeNs();
            this.maxProcessingTimeNs = stats.getMaxProcessingTimeNs();
            this.slowestTaskDescription = stats.getSlowestTaskDescription();
            this.lastError = mailbox.getLastError();
        }
    }
}

