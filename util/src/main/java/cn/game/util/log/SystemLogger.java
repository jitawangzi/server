package cn.game.util.log;

import org.apache.commons.lang3.StringUtils;


/**
 * @author pangjiawei - [Created on 2018/1/30 22:02]
 */
public class SystemLogger extends DeprecatedLogger {

    public static void trace(final Object... objects) {
        String log = LoggerType.splice(objects);
        if (!StringUtils.isEmpty(log)) {
            LoggerType.System.logger.trace(log);
        }
        catching(objects);
    }

    public static void debug(final Object... objects) {
        String log = LoggerType.splice(objects);
        if (!StringUtils.isEmpty(log)) {
            LoggerType.System.logger.debug(log);
        }
        catching(objects);
    }

    public static void info(final Object... objects) {
        String log = LoggerType.splice(objects);
        if (!StringUtils.isEmpty(log)) {
            LoggerType.System.logger.info(log);
        }
        catching(objects);
    }

    public static void warn(final Object... objects) {
        String log = LoggerType.splice(objects);
        if (!StringUtils.isEmpty(log)) {
            LoggerType.System.logger.warn(log);
        }
        catching(objects);
    }

    public static void error(final Object... objects) {
        String log = LoggerType.splice(objects);
        if (!StringUtils.isEmpty(log)) {
            LoggerType.System.logger.error(log);
        }
        catching(objects);
    }

    public static void fatal(final Object... objects) {
        String log = LoggerType.splice(objects);
        if (!StringUtils.isEmpty(log)) {
            LoggerType.System.logger.fatal(log);
        }
        catching(objects);
    }
}
