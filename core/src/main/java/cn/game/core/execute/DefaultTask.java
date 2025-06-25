package cn.game.core.execute;

import java.util.concurrent.Callable;

/**
 * Task接口的默认实现
 */
public class DefaultTask<T> implements Task<T>, Comparable<Task<?>> {
    private final Callable<T> action;
    private final String description;
	@Deprecated
    private final int priority;
	@Deprecated
    private final long timeoutMs;
    
    public DefaultTask(Callable<T> action, String description, int priority, long timeoutMs) {
        this.action = action;
        this.description = description;
        this.priority = priority;
        this.timeoutMs = timeoutMs;
    }
    
    @Override
    public T execute() throws Exception {
        return action.call();
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    @Override
    public long getTimeoutMs() {
        return timeoutMs;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public int compareTo(Task<?> other) {
        // 高优先级先执行
        return Integer.compare(other.getPriority(), this.priority);
    }
    
    /**
     * 创建任务构建器
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    /**
     * 任务构建器，用于简化任务创建
     */
    public static class Builder<T> {
        private Callable<T> action;
        private String description = "Unknown Task";
        private int priority = 0;
        private long timeoutMs = 0;
        
        public Builder<T> action(Callable<T> action) {
            this.action = action;
            return this;
        }
        
        public Builder<T> description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder<T> priority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder<T> timeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }
        
        public DefaultTask<T> build() {
            if (action == null) {
                throw new IllegalStateException("Task action cannot be null");
            }
            return new DefaultTask<>(action, description, priority, timeoutMs);
        }
    }
}

