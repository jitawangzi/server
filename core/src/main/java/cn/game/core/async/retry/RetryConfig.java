package cn.game.core.async.retry;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * 重试配置
 */
public class RetryConfig {

	public final int maxAttempts;
	public final Duration delay;
	public final Class<? extends Throwable>[] retryOn;
	public final Predicate<Object> resultPredicate;

	private RetryConfig(Builder builder) {
		this.maxAttempts = builder.maxAttempts;
		this.delay = builder.delay;
		this.retryOn = builder.retryOn;
		this.resultPredicate = builder.resultPredicate;
	}

	public static class Builder {
		private int maxAttempts = 3;
		private Duration delay = Duration.ZERO;
		private Class<? extends Throwable>[] retryOn;
		private Predicate<Object> resultPredicate = result -> true;

		public Builder maxAttempts(int maxAttempts) {
			this.maxAttempts = maxAttempts;
			return this;
		}

		public Builder delay(Duration delay) {
			this.delay = delay;
			return this;
		}

		@SafeVarargs
		public final Builder retryOn(Class<? extends Throwable>... retryOn) {
			this.retryOn = retryOn;
			return this;
		}

		public Builder retryIf(Predicate<Object> resultPredicate) {
			this.resultPredicate = resultPredicate;
			return this;
		}

		public RetryConfig build() {
			return new RetryConfig(this);
		}
	}

}
