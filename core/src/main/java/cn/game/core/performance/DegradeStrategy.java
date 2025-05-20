package cn.game.core.performance;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 降级策略，当系统负载过高时，会限制某些功能或行为，以保护系统的稳定性和性能。
 */
public class DegradeStrategy {

	private static final AtomicBoolean[] LIMIT_ENABLE = new AtomicBoolean[LoadLimitTypeEnum.values().length];

	// 静态初始化数组
	static {
		for (int i = 0; i < LIMIT_ENABLE.length; i++) {
			LIMIT_ENABLE[i] = new AtomicBoolean(false);
		}
	}

	/**
	 * 限制某种行为
	 * @param type 限制类型
	 */
	public static void limit(LoadLimitTypeEnum type) {
		LIMIT_ENABLE[type.ordinal()].set(true);
	}

	/**
	 * 某行为是否被限制
	 * @param type 限制类型
	 * @return 是否被限制
	 */
	public static boolean isLimited(LoadLimitTypeEnum type) {
		return LIMIT_ENABLE[type.ordinal()].get();
	}

	/**
	 * 解除某种限制
	 * @param type 限制类型
	 */
	public static void recover(LoadLimitTypeEnum type) {
		LIMIT_ENABLE[type.ordinal()].set(false);
	}

	/**
	 * 恢复所有限制
	 */
	public static void recoverAll() {
		for (AtomicBoolean flag : LIMIT_ENABLE) {
			flag.set(false);
		}
	}
}