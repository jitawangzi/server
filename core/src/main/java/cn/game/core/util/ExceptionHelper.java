package cn.game.core.util;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.exception.LogicException;

public class ExceptionHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHelper.class);
	
	public static <T extends Throwable> T findCause(Throwable throwable, Class<T> targetType) {
		if (throwable == null || targetType == null)
			return null;
		@SuppressWarnings("unchecked")
		java.util.List<Throwable> list = ExceptionUtils.getThrowableList(throwable);
		for (Throwable t : list) {
			if (targetType.isInstance(t)) {
				return targetType.cast(t);
			}
		}
		return null;
	}

	 /**
     * 统一异常处理策略：
     * - 如果包含 LogicException：仅打印轻量业务日志（不带异常对象），原样抛出 LogicException（无堆栈）。
     * - 否则：打印完整堆栈，抛出保留堆栈的异常（原 RuntimeException 或包一层 RuntimeException）。
     *
     * 该方法内部直接抛出异常，不返回。
     */
    public static void rethrowWithPolicy(Throwable throwable, Logger logger, String scene) {
        final Logger log = (logger != null) ? logger : LOGGER;
        final String prefix = (scene != null && !scene.isEmpty()) ? scene : "异常";

        // 识别业务异常（包含 throwable 本身和其 cause 链）
        LogicException le = (throwable instanceof LogicException)
                ? (LogicException) throwable
                : findCause(throwable, LogicException.class);

        if (le != null) {
            // 轻量日志：不传异常对象，避免堆栈
            log.warn("{} 业务异常 code={}, msg={}", prefix, le.getErrorCode(), le.getErrorMessage());
            throw le; // 无堆栈异常，原样抛出
        }
        if (throwable instanceof Error) {
            log.error("{} 系统异常(Error)", prefix, throwable);
            throw new RuntimeException("wrapped error: " + throwable.getClass().getName(), throwable);
        }
        // 非业务异常：打印完整堆栈
        log.error("{} 系统异常", prefix, throwable);
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        throw new RuntimeException(throwable);
    }
}
