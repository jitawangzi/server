package cn.game.core.execute;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 执行监控，收集和报告执行统计信息
 */
public class ExecutionMonitor {
    private static final Logger LOGGER = Logger.getLogger(ExecutionMonitor.class.getName());
    
    private final TaskExecutorService executorService;
    private final TaskExecutionConfig config;
    
    // 全局统计
    private final AtomicInteger totalActiveMailboxes = new AtomicInteger(0);
    private final AtomicLong totalCompletedTasks = new AtomicLong(0);
    private final AtomicLong totalFailedTasks = new AtomicLong(0);
    private final AtomicLong totalTimeoutTasks = new AtomicLong(0);
    private final AtomicLong lastCleanupTime = new AtomicLong(System.currentTimeMillis());
    
    // 监听器
    private final List<Consumer<MonitorSnapshot>> snapshotListeners = new ArrayList<>();
    
    public ExecutionMonitor(TaskExecutorService executorService, TaskExecutionConfig config) {
        this.executorService = executorService;
        this.config = config;
    }
    
    /**
     * 开始定期监控
     * @param intervalMs 监控间隔（毫秒）
     */
    public void startPeriodicMonitoring(long intervalMs) {
        Thread monitorThread = new Thread(() -> {
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
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in monitor thread", e);
                }
            }
        }, "ExecutionMonitor-Thread");
        
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
    
    /**
     * 检查邮箱状态，清理闲置邮箱，检测死锁
     */
    public void checkMailboxes() {
        long now = System.currentTimeMillis();
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
                long lastAccess = mailbox.getLastAccessTime();
                long processingTime = now - lastAccess;
                
                // 检测闲置邮箱
                if (mailbox.isEmpty() && !mailbox.isProcessing() && processingTime > idleTimeout) {
                    toRemove.add(mailbox);
                }
                
                // 检测潜在死锁
                if (mailbox.isProcessing() && processingTime > deadlockThreshold) {
                    LOGGER.warning(() -> String.format(
                        "Potential deadlock detected for entity %d - processing for %d ms with %d tasks in queue",
                        mailbox.getEntityId(), processingTime, mailbox.getQueueSize()
                    ));
                    
                    // 重置处理状态，允许其他线程尝试处理
                    if (mailbox.compareAndSetProcessing(true, false)) {
                        LOGGER.info(() -> "Reset processing state for potentially deadlocked entity " + mailbox.getEntityId());
                        
                        // 尝试重新提交处理器
                        if (!mailbox.isEmpty() && mailbox.compareAndSetProcessing(false, true)) {
                            executorService.submitProcessor(mailbox);
                        }
                    }
                }
            }
            
            // 移除闲置邮箱
            for (ActorMailbox mailbox : toRemove) {
                if (executorService.removeMailbox(mailbox.getEntityId())) {
                    LOGGER.info(() -> "Removed idle mailbox for entity " + mailbox.getEntityId());
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
        if (LOGGER.isLoggable(Level.INFO)) {
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
                LOGGER.log(Level.WARNING, "Error notifying monitor listener", e);
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

