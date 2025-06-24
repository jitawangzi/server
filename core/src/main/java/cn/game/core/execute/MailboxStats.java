package cn.game.core.execute;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 邮箱统计信息，用于监控和调试
 */
public class MailboxStats {
    public final AtomicInteger submittedTasks = new AtomicInteger(0);
    public final AtomicInteger completedTasks = new AtomicInteger(0);
    public final AtomicInteger failedTasks = new AtomicInteger(0);
    public final AtomicInteger timeoutTasks = new AtomicInteger(0);
    public final AtomicInteger rejectedTasks = new AtomicInteger(0);
    
    private final AtomicLong totalProcessingTimeNs = new AtomicLong(0);
    private final AtomicLong maxProcessingTimeNs = new AtomicLong(0);
    private final AtomicReference<String> slowestTaskDescription = new AtomicReference<>("");
    
    /**
     * 记录任务处理时间
     * @param durationNs 处理时间（纳秒）
     * @param taskDescription 任务描述
     */
    public void recordProcessingTime(long durationNs, String taskDescription) {
        totalProcessingTimeNs.addAndGet(durationNs);
        
        // 更新最长处理时间
        long currentMax = maxProcessingTimeNs.get();
        while (durationNs > currentMax) {
            if (maxProcessingTimeNs.compareAndSet(currentMax, durationNs)) {
                slowestTaskDescription.set(taskDescription);
                break;
            }
            currentMax = maxProcessingTimeNs.get();
        }
    }
    
    /**
     * 获取平均处理时间（纳秒）
     * @return 平均处理时间
     */
    public double getAverageProcessingTimeNs() {
        int completed = completedTasks.get();
        if (completed == 0) return 0.0;
        return (double) totalProcessingTimeNs.get() / completed;
    }
    
    /**
     * 获取最长处理时间（纳秒）
     * @return 最长处理时间
     */
    public long getMaxProcessingTimeNs() {
        return maxProcessingTimeNs.get();
    }
    
    /**
     * 获取处理最慢的任务描述
     * @return 任务描述
     */
    public String getSlowestTaskDescription() {
        return slowestTaskDescription.get();
    }
    
    /**
     * 获取任务成功率
     * @return 成功率（0.0-1.0）
     */
    public double getSuccessRate() {
        int total = completedTasks.get() + failedTasks.get() + timeoutTasks.get();
        if (total == 0) return 1.0;
        return (double) completedTasks.get() / total;
    }
    
    /**
     * 获取当前等待中的任务数量
     * @return 等待中的任务数量
     */
    public int getPendingTasks() {
        return submittedTasks.get() - completedTasks.get() - failedTasks.get() - timeoutTasks.get();
    }
    
    /**
     * 重置统计信息
     */
    public void reset() {
        submittedTasks.set(0);
        completedTasks.set(0);
        failedTasks.set(0);
        timeoutTasks.set(0);
        rejectedTasks.set(0);
        totalProcessingTimeNs.set(0);
        maxProcessingTimeNs.set(0);
        slowestTaskDescription.set("");
    }
    
    @Override
    public String toString() {
        return String.format(
            "MailboxStats{submitted=%d, completed=%d, failed=%d, timeout=%d, rejected=%d, " +
            "avgTime=%.2f ms, maxTime=%.2f ms, slowestTask='%s', successRate=%.2f%%}",
            submittedTasks.get(), completedTasks.get(), failedTasks.get(), timeoutTasks.get(), rejectedTasks.get(),
            getAverageProcessingTimeNs() / 1_000_000.0, maxProcessingTimeNs.get() / 1_000_000.0,
            slowestTaskDescription.get(), getSuccessRate() * 100
        );
    }
}

