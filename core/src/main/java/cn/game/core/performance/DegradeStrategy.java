package cn.game.core.performance;

/**
 * 降级策略，当系统负载过高时，会限制某些功能或行为，以保护系统的稳定性和性能。
 */
public class DegradeStrategy {
	private static final boolean[] LIMIT_ENABLE = new boolean[LoadLimitTypeEnum.values().length];

	/** 
	 * 限制某种行为
	 * @param type
	 */
	public static void limit(LoadLimitTypeEnum type) {
		LIMIT_ENABLE[type.ordinal()] = true;
	}

	/** 
	 * 某行为是否被限制
	 * @param type
	 * @return
	 */
	public static boolean isLimited(LoadLimitTypeEnum type) {
		return LIMIT_ENABLE[type.ordinal()];
	}

	/** 
	 * 恢复所有限制
	 */
	public static void recoverAll() {
		for (int i = 0; i < LIMIT_ENABLE.length; i++) {
			LIMIT_ENABLE[i] = false;
		}
	}
}