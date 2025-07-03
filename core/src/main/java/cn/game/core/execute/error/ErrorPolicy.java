package cn.game.core.execute.error;

import java.util.concurrent.TimeUnit;

/**
 * 任务执行失败后的处理策略
 */
public class ErrorPolicy {
	/** 创建一个放弃任务的策略,默认使用 */
	private static ErrorPolicy DISCARD = new ErrorPolicy(PolicyType.DISCARD, 0, null);

    /**
     * 策略类型枚举
     */
    public enum PolicyType {
        /**
         * 放弃任务
         */
        DISCARD,
        /**
         * 立即将任务放回队列头部重试
         */
        RETRY_HEAD_IMMEDIATELY,
        /**
         * 延迟后将任务放回队列头部重试
         */
        RETRY_HEAD_WITH_DELAY,
    }

    private final PolicyType type;
    private final long delay;
    private final TimeUnit timeUnit;

    private ErrorPolicy(PolicyType type, long delay, TimeUnit timeUnit) {
        this.type = type;
        this.delay = delay;
        this.timeUnit = timeUnit;
    }

    /**
     * @return 策略类型
     */
    public PolicyType getType() {
        return type;
    }

    /**
     * @return 延迟时间，仅当策略为 RETRY_HEAD_WITH_DELAY 时有效
     */
    public long getDelay() {
        return delay;
    }

    /**
     * @return 时间单位，仅当策略为 RETRY_HEAD_WITH_DELAY 时有效
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
	 * 默认放弃策略
	 * @return 放弃策略
	 */
    public static ErrorPolicy discard() {
		return DISCARD;
    }

    /**
     * 创建一个立即重试的策略（放回队列头）
     * @return 立即重试策略
     */
    public static ErrorPolicy retryHeadImmediately() {
        return new ErrorPolicy(PolicyType.RETRY_HEAD_IMMEDIATELY, 0, null);
    }

    /**
     * 创建一个延迟重试的策略（放回队列头）
     * @param delay 延迟时间
     * @param timeUnit 时间单位
     * @return 延迟重试策略
     */
    public static ErrorPolicy retryHeadWithDelay(long delay, TimeUnit timeUnit) {
        if (delay <= 0) {
            return retryHeadImmediately();
        }
        return new ErrorPolicy(PolicyType.RETRY_HEAD_WITH_DELAY, delay, timeUnit);
    }
}

