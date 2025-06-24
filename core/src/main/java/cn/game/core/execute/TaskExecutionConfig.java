package cn.game.core.execute;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 任务执行配置
 */
public class TaskExecutionConfig {
    // 默认配置值
    private static final int DEFAULT_MAX_QUEUE_SIZE = 1000;
    private static final long DEFAULT_IDLE_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(10);
    private static final long DEFAULT_MAILBOX_CLEANUP_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5);
    private static final int DEFAULT_ERROR_RETRY_LIMIT = 3;
    private static final long DEFAULT_DEFAULT_TASK_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(30);
    private static final long DEFAULT_DEADLOCK_DETECTION_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(1);
    
    // 配置属性
    private final int maxQueueSize;
    private final long idleTimeoutMs;
    private final long mailboxCleanupIntervalMs;
    private final int errorRetryLimit;
    private final long defaultTaskTimeoutMs;
    private final long deadlockDetectionThresholdMs;
    
    private TaskExecutionConfig(Builder builder) {
        this.maxQueueSize = builder.maxQueueSize;
        this.idleTimeoutMs = builder.idleTimeoutMs;
        this.mailboxCleanupIntervalMs = builder.mailboxCleanupIntervalMs;
        this.errorRetryLimit = builder.errorRetryLimit;
        this.defaultTaskTimeoutMs = builder.defaultTaskTimeoutMs;
        this.deadlockDetectionThresholdMs = builder.deadlockDetectionThresholdMs;
    }
    
    public int getMaxQueueSize() {
        return maxQueueSize;
    }
    
    public long getIdleTimeoutMs() {
        return idleTimeoutMs;
    }
    
    public long getMailboxCleanupIntervalMs() {
        return mailboxCleanupIntervalMs;
    }
    
    public int getErrorRetryLimit() {
        return errorRetryLimit;
    }
    
    public long getDefaultTaskTimeoutMs() {
        return defaultTaskTimeoutMs;
    }
    
    public long getDeadlockDetectionThresholdMs() {
        return deadlockDetectionThresholdMs;
    }
    
    /**
     * 创建默认配置
     */
    public static TaskExecutionConfig getDefault() {
        return new Builder().build();
    }
    
    /**
     * 创建配置构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 配置构建器类
     */
    public static class Builder {
        private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
        private long idleTimeoutMs = DEFAULT_IDLE_TIMEOUT_MS;
        private long mailboxCleanupIntervalMs = DEFAULT_MAILBOX_CLEANUP_INTERVAL_MS;
        private int errorRetryLimit = DEFAULT_ERROR_RETRY_LIMIT;
        private long defaultTaskTimeoutMs = DEFAULT_DEFAULT_TASK_TIMEOUT_MS;
        private long deadlockDetectionThresholdMs = DEFAULT_DEADLOCK_DETECTION_THRESHOLD_MS;
        
        public Builder maxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
            return this;
        }
        
        public Builder idleTimeout(Duration timeout) {
            this.idleTimeoutMs = timeout.toMillis();
            return this;
        }
        
        public Builder idleTimeoutMs(long timeoutMs) {
            this.idleTimeoutMs = timeoutMs;
            return this;
        }
        
        public Builder mailboxCleanupInterval(Duration interval) {
            this.mailboxCleanupIntervalMs = interval.toMillis();
            return this;
        }
        
        public Builder mailboxCleanupIntervalMs(long intervalMs) {
            this.mailboxCleanupIntervalMs = intervalMs;
            return this;
        }
        
        public Builder errorRetryLimit(int limit) {
            this.errorRetryLimit = limit;
            return this;
        }
        
        public Builder defaultTaskTimeout(Duration timeout) {
            this.defaultTaskTimeoutMs = timeout.toMillis();
            return this;
        }
        
        public Builder defaultTaskTimeoutMs(long timeoutMs) {
            this.defaultTaskTimeoutMs = timeoutMs;
            return this;
        }
        
        public Builder deadlockDetectionThreshold(Duration threshold) {
            this.deadlockDetectionThresholdMs = threshold.toMillis();
            return this;
        }
        
        public Builder deadlockDetectionThresholdMs(long thresholdMs) {
            this.deadlockDetectionThresholdMs = thresholdMs;
            return this;
        }
        
        public TaskExecutionConfig build() {
            return new TaskExecutionConfig(this);
        }
    }
}

