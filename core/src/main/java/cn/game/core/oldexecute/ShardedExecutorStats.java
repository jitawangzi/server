package cn.game.core.oldexecute;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 分片执行器统计信息
 */
public class ShardedExecutorStats {
    private final LongAdder submittedTasks = new LongAdder();
    private final LongAdder completedTasks = new LongAdder();
    private final LongAdder totalExecutionTime = new LongAdder();
    private final AtomicLong maxExecutionTime = new AtomicLong(0);
    private final long startTime = System.currentTimeMillis();
    
    /**
     * 记录任务提交
     */
    void taskSubmitted() {
        submittedTasks.increment();
    }
    
    /**
     * 记录任务完成
     * @param executionTime 执行时间（毫秒）
     */
    void taskCompleted(long executionTime) {
        completedTasks.increment();
        totalExecutionTime.add(executionTime);
        
        // 更新最大执行时间
        while (true) {
            long current = maxExecutionTime.get();
            if (executionTime <= current || 
                maxExecutionTime.compareAndSet(current, executionTime)) {
                break;
            }
        }
    }
    
    /**
     * 获取提交的任务总数
     */
    public long getSubmittedTaskCount() {
        return submittedTasks.sum();
    }
    
    /**
     * 获取已完成的任务总数
     */
    public long getCompletedTaskCount() {
        return completedTasks.sum();
    }
    
    /**
     * 获取平均执行时间（毫秒）
     */
    public double getAverageExecutionTime() {
        long completed = completedTasks.sum();
        return completed > 0 ? (double) totalExecutionTime.sum() / completed : 0;
    }
    
    /**
     * 获取最大执行时间（毫秒）
     */
    public long getMaxExecutionTime() {
        return maxExecutionTime.get();
    }
    
    /**
     * 获取运行时间（毫秒）
     */
    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * 计算每秒处理的任务数
     */
    public double getTasksPerSecond() {
        long uptime = getUptime() / 1000;
        return uptime > 0 ? (double) completedTasks.sum() / uptime : 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "ShardedExecutorStats{submitted=%d, completed=%d, avgTime=%.2fms, maxTime=%dms, tps=%.2f}",
            getSubmittedTaskCount(),
            getCompletedTaskCount(),
            getAverageExecutionTime(),
            getMaxExecutionTime(),
            getTasksPerSecond()
        );
    }
}

