package cn.game.core.async.retry;

import java.util.function.Function;

/**
 * 通用的尝试结果封装类
 * @param <T> 结果数据类型
 */
public class AttemptResult<T> {
	private final T data;
	private final boolean success;
	private final String message;
	private final int attemptCount;
	private final Throwable error;

	private AttemptResult(Builder<T> builder) {
		this.data = builder.data;
		this.success = builder.success;
		this.message = builder.message;
		this.attemptCount = builder.attemptCount;
		this.error = builder.error;
	}

	public T getData() {
		return data;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public Throwable getError() {
		return error;
	}

	// 实用方法
	public <R> AttemptResult<R> map(Function<T, R> mapper) {
		if (!success || data == null) {
			return new Builder<R>().success(false).message(this.message).attemptCount(this.attemptCount).error(this.error).build();
		}
		return new Builder<R>().success(true).data(mapper.apply(data)).attemptCount(this.attemptCount).build();
	}

	public static class Builder<T> {
		private T data;
		private boolean success;
		private String message;
		private int attemptCount;
		private Throwable error;

		public Builder<T> data(T data) {
			this.data = data;
			return this;
		}

		public Builder<T> success(boolean success) {
			this.success = success;
			return this;
		}

		public Builder<T> message(String message) {
			this.message = message;
			return this;
		}

		public Builder<T> attemptCount(int attemptCount) {
			this.attemptCount = attemptCount;
			return this;
		}

		public Builder<T> error(Throwable error) {
			this.error = error;
			return this;
		}

		public AttemptResult<T> build() {
			return new AttemptResult<>(this);
		}
	}

	// 便捷的静态工厂方法
	public static <T> AttemptResult<T> success(T data) {
		return new Builder<T>().success(true).data(data).attemptCount(1).build();
	}

	public static <T> AttemptResult<T> failure(String message) {
		return new Builder<T>().success(false).message(message).build();
	}

	public static <T> AttemptResult<T> failure(String message, Throwable error) {
		return new Builder<T>().success(false).message(message).error(error).build();
	}
}