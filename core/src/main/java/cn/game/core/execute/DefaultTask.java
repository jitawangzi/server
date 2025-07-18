package cn.game.core.execute;

import cn.game.core.execute.error.ErrorHandler;

import java.util.concurrent.Callable;

/**
 * Task接口的默认实现
 */
public class DefaultTask<T> implements Task<T> {
    private final Callable<T> action;
    private final String description;
	boolean fast;
	@Deprecated
    private final long timeoutMs;
    private final ErrorHandler errorHandler;

    public DefaultTask(Callable<T> action, String description, boolean fast, long timeoutMs, ErrorHandler errorHandler) {
        this.action = action;
        this.description = description;
        this.fast = fast;
        this.timeoutMs = timeoutMs;
        this.errorHandler = errorHandler != null ? errorHandler : ErrorHandler.DISCARD_HANDLER;
    }

    @Override
    public T execute() throws Exception {
        return action.call();
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
    public ErrorHandler getErrorHandler() {
        return errorHandler;
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
        private boolean fast ; 
        private long timeoutMs = 0;
        private ErrorHandler errorHandler = ErrorHandler.DISCARD_HANDLER;

        public Builder<T> action(Callable<T> action) {
            this.action = action;
            return this;
        }

        public Builder<T> description(String description) {
            this.description = description;
            return this;
        }

        public Builder<T> fast(boolean fast) {
            this.fast = fast;
            return this;
        }

        public Builder<T> timeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder<T> errorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public DefaultTask<T> build() {
            if (action == null) {
                throw new IllegalStateException("Task action cannot be null");
            }
            return new DefaultTask<>(action, description, fast, timeoutMs, errorHandler);
        }
    }

	@Override
	public boolean isFast() {
		return fast;
	}
}

