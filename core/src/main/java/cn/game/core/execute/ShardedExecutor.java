package cn.game.core.execute;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 分片执行器 - 基于ID将任务分配到固定的虚拟线程，支持动态扩缩容
 */
public class ShardedExecutor implements TaskExecutorCallback {
    // 默认配置
    private static final int DEFAULT_INITIAL_SHARDS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int DEFAULT_MIN_SHARDS = 4;
    private static final int DEFAULT_MAX_SHARDS = 128;
    private static final Duration DEFAULT_SCALE_CHECK_INTERVAL = Duration.ofSeconds(30);
    private static final int DEFAULT_SCALE_THRESHOLD_PERCENT = 70; // 70% 队列填充触发扩容
    private static final int DEFAULT_SCALE_DOWN_THRESHOLD_PERCENT = 30; // 30% 队列填充触发缩容
    private static final int DEFAULT_TASK_PER_SHARD_THRESHOLD = 100; // 每个分片积压任务阈值
    
    // 分片处理器数组，使用原子引用以支持原子更新
    private final AtomicReference<TaskProcessor[]> processorsRef;
    
    // 配置参数
    private final int minShards;
    private final int maxShards;
    private final Duration scaleCheckInterval;
    private final int scaleThresholdPercent;
    private final int scaleDownThresholdPercent;
    private final int tasksPerShardThreshold;
    private final boolean autoScaling;
    
    // 统计信息
    private final ShardedExecutorStats stats = new ShardedExecutorStats();
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final ConcurrentMap<String, AtomicInteger> objectTaskCounters = new ConcurrentHashMap<>();
    
    // 自动扩缩容控制
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ScheduledExecutorService scheduler;
    
    /**
     * 创建具有指定分片数的执行器（不支持动态扩缩容）
     * @param shardCount 分片数量
     */
    public ShardedExecutor(int shardCount) {
        this(new Builder().withInitialShards(shardCount).withAutoScaling(false));
    }
    
    /**
     * 创建具有默认配置的执行器
     */
    public ShardedExecutor() {
        this(new Builder());
    }
    
    /**
     * 使用构建器创建执行器
     */
    private ShardedExecutor(Builder builder) {
        this.minShards = builder.minShards;
        this.maxShards = builder.maxShards;
        this.scaleCheckInterval = builder.scaleCheckInterval;
        this.scaleThresholdPercent = builder.scaleThresholdPercent;
        this.scaleDownThresholdPercent = builder.scaleDownThresholdPercent;
        this.tasksPerShardThreshold = builder.tasksPerShardThreshold;
        this.autoScaling = builder.autoScaling;
        
        // 初始化处理器
        TaskProcessor[] initialProcessors = createProcessors(builder.initialShards);
        this.processorsRef = new AtomicReference<>(initialProcessors);
        
        // 创建调度器（如果启用自动扩缩容）
        if (autoScaling) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor(
                r -> {
                    Thread t = new Thread(r, "ShardScalingMonitor");
                    t.setDaemon(true);
                    return t;
                });
            startScalingMonitor();
        } else {
            this.scheduler = null;
        }
    }
    
    /**
     * 创建指定数量的处理器
     */
    private TaskProcessor[] createProcessors(int count) {
        TaskProcessor[] processors = new TaskProcessor[count];
        for (int i = 0; i < count; i++) {
            processors[i] = new TaskProcessor(i, this);
        }
        return processors;
    }
    
    /**
     * 启动自动扩缩容监控
     */
    private void startScalingMonitor() {
        scheduler.scheduleAtFixedRate(
                this::checkAndAdjustCapacity,
                scaleCheckInterval.toMillis(),
                scaleCheckInterval.toMillis(),
                TimeUnit.MILLISECONDS);
    }
    
    /**
     * 检查负载并调整容量
     */
    private void checkAndAdjustCapacity() {
        try {
            TaskProcessor[] currentProcessors = processorsRef.get();
            int currentShardCount = currentProcessors.length;
            
            // 计算当前负载
            int totalQueueSize = 0;
            int[] queueSizes = new int[currentShardCount];
            
            for (int i = 0; i < currentShardCount; i++) {
                int size = currentProcessors[i].getQueueSize();
                queueSizes[i] = size;
                totalQueueSize += size;
            }
            
            // 计算平均每个分片的队列大小
            double avgQueueSize = (double) totalQueueSize / currentShardCount;
            int maxQueueSize = 0;
            for (int size : queueSizes) {
                maxQueueSize = Math.max(maxQueueSize, size);
            }
            
            // 决定是否需要扩容
            boolean needScaleUp = maxQueueSize > tasksPerShardThreshold ||
                    avgQueueSize > (tasksPerShardThreshold * scaleThresholdPercent / 100.0);
            
            // 决定是否需要缩容
            boolean needScaleDown = currentShardCount > minShards &&
                    maxQueueSize < (tasksPerShardThreshold * scaleDownThresholdPercent / 100.0);
            
            if (needScaleUp && currentShardCount < maxShards) {
                // 扩容：增加50%的分片，但不超过最大限制
                int newCount = Math.min(maxShards, currentShardCount + (currentShardCount / 2));
                if (newCount > currentShardCount) {
                    adjustShardCount(newCount);
                    System.out.println("Scaled up: " + currentShardCount + " -> " + newCount + 
                            " shards (avg queue: " + String.format("%.1f", avgQueueSize) + 
                            ", max queue: " + maxQueueSize + ")");
                }
            } else if (needScaleDown) {
                // 缩容：减少25%的分片，但不低于最小限制
                int newCount = Math.max(minShards, currentShardCount - (currentShardCount / 4));
                if (newCount < currentShardCount) {
                    adjustShardCount(newCount);
                    System.out.println("Scaled down: " + currentShardCount + " -> " + newCount + 
                            " shards (avg queue: " + String.format("%.1f", avgQueueSize) + 
                            ", max queue: " + maxQueueSize + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Error in scaling monitor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 调整分片数量
     */
    private void adjustShardCount(int newCount) {
        TaskProcessor[] currentProcessors = processorsRef.get();
        int currentCount = currentProcessors.length;
        
        if (newCount == currentCount) {
            return;
        }
        
        TaskProcessor[] newProcessors = new TaskProcessor[newCount];
        
        if (newCount > currentCount) {
            // 扩容：保留所有现有处理器，添加新的
            System.arraycopy(currentProcessors, 0, newProcessors, 0, currentCount);
            for (int i = currentCount; i < newCount; i++) {
                newProcessors[i] = new TaskProcessor(i, this);
            }
        } else {
            // 缩容：保留部分处理器，关闭多余的
            System.arraycopy(currentProcessors, 0, newProcessors, 0, newCount);
            for (int i = newCount; i < currentCount; i++) {
                currentProcessors[i].shutdown();
            }
        }
        
        // 原子更新处理器数组
        processorsRef.set(newProcessors);
    }
    
    /**
     * 在指定对象的虚拟线程上执行任务
     * @param objectId 对象ID
     * @param task 要执行的任务
     * @return 任务结果的Future
     */
    public <T> CompletableFuture<T> execute(long objectId, Supplier<T> task) {
        return execute(String.valueOf(objectId), task);
    }
    
    /**
     * 在指定对象的虚拟线程上执行任务（使用字符串ID）
     * @param objectId 对象ID
     * @param task 要执行的任务
     * @return 任务结果的Future
     */
    public <T> CompletableFuture<T> execute(String objectId, Supplier<T> task) {
        if (objectId == null || task == null) {
            throw new IllegalArgumentException("ObjectId and task cannot be null");
        }
        
        // 计算分片索引
        TaskProcessor[] processors = processorsRef.get();
        int shardIndex = getShardIndex(objectId, processors.length);
        TaskProcessor processor = processors[shardIndex];
        
        // 创建任务并提交
        CompletableFuture<T> resultFuture = new CompletableFuture<>();
        Task<T> taskObj = new Task<>(objectId, task, resultFuture);
        
        // 记录活跃任务数
        activeTaskCount.incrementAndGet();
        objectTaskCounters.computeIfAbsent(objectId, k -> new AtomicInteger(0))
                         .incrementAndGet();
        
        stats.taskSubmitted();
        processor.submit(taskObj);
        
        return resultFuture;
    }
    
    /**
     * 计算对象ID对应的分片索引
     */
    private int getShardIndex(String objectId, int shardCount) {
        return Math.abs(objectId.hashCode() % shardCount);
    }
    
    /**
     * 获取当前分片数量
     */
    public int getShardCount() {
        return processorsRef.get().length;
    }
    
    /**
     * 获取当前活跃任务数
     */
    public int getActiveTaskCount() {
        return activeTaskCount.get();
    }
    
    /**
     * 获取特定对象的活跃任务数
     */
    public int getObjectTaskCount(String objectId) {
        AtomicInteger counter = objectTaskCounters.get(objectId);
        return counter != null ? counter.get() : 0;
    }
    
    /**
     * 获取执行器统计信息
     */
    public ShardedExecutorStats getStats() {
        return stats;
    }
    
    /**
     * 手动设置分片数量
     */
    public void setShardCount(int count) {
        if (count < minShards || count > maxShards) {
            throw new IllegalArgumentException(
                    "Shard count must be between " + minShards + " and " + maxShards);
        }
        adjustShardCount(count);
    }
    
    /**
     * 任务完成时的回调
     */
	@Override
	public void taskCompleted(Task<?> task) {
        activeTaskCount.decrementAndGet();
        
        AtomicInteger counter = objectTaskCounters.get(task.getObjectId());
        if (counter != null && counter.decrementAndGet() <= 0) {
            objectTaskCounters.remove(task.getObjectId());
        }
        
        stats.taskCompleted(task.getExecutionTime());
    }
    
    /**
     * 关闭执行器，停止所有处理器
     */
    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            if (scheduler != null) {
                scheduler.shutdown();
            }
            
            TaskProcessor[] processors = processorsRef.get();
            for (TaskProcessor processor : processors) {
                processor.shutdown();
            }
        }
    }
    
    /**
     * 分片执行器构建器
     */
    public static class Builder {
        private int initialShards = DEFAULT_INITIAL_SHARDS;
        private int minShards = DEFAULT_MIN_SHARDS;
        private int maxShards = DEFAULT_MAX_SHARDS;
        private Duration scaleCheckInterval = DEFAULT_SCALE_CHECK_INTERVAL;
        private int scaleThresholdPercent = DEFAULT_SCALE_THRESHOLD_PERCENT;
        private int scaleDownThresholdPercent = DEFAULT_SCALE_DOWN_THRESHOLD_PERCENT;
        private int tasksPerShardThreshold = DEFAULT_TASK_PER_SHARD_THRESHOLD;
        private boolean autoScaling = true;
        
        /**
         * 设置初始分片数
         */
        public Builder withInitialShards(int count) {
            this.initialShards = count;
            return this;
        }
        
        /**
         * 设置最小分片数
         */
        public Builder withMinShards(int count) {
            this.minShards = count;
            return this;
        }
        
        /**
         * 设置最大分片数
         */
        public Builder withMaxShards(int count) {
            this.maxShards = count;
            return this;
        }
        
        /**
         * 设置扩缩容检查间隔
         */
        public Builder withScaleCheckInterval(Duration interval) {
            this.scaleCheckInterval = interval;
            return this;
        }
        
        /**
         * 设置扩容阈值百分比
         */
        public Builder withScaleThresholdPercent(int percent) {
            this.scaleThresholdPercent = percent;
            return this;
        }
        
        /**
         * 设置缩容阈值百分比
         */
        public Builder withScaleDownThresholdPercent(int percent) {
            this.scaleDownThresholdPercent = percent;
            return this;
        }
        
        /**
         * 设置每个分片的任务阈值
         */
        public Builder withTasksPerShardThreshold(int count) {
            this.tasksPerShardThreshold = count;
            return this;
        }
        
        /**
         * 设置是否启用自动扩缩容
         */
        public Builder withAutoScaling(boolean enabled) {
            this.autoScaling = enabled;
            return this;
        }
        
        /**
         * 构建分片执行器
         */
        public ShardedExecutor build() {
            return new ShardedExecutor(this);
        }
    }
    
    /**
     * 创建新的构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }
}

