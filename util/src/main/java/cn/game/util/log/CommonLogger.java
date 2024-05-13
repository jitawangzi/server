package cn.game.util.log;

import org.apache.commons.lang3.StringUtils;

/**
 * @author pangjiawei - [Created on 2018/1/30 22:03]
 */
public class CommonLogger extends Logger {

    public static void info(final Object... objects) {
        String log = LoggerType.splice(objects);
        if (!StringUtils.isEmpty(log)) {
            LoggerType.Info.logger.info(log);
        }
        catching(objects);
    }

    public static void warn(final Object... objects) {
        String log = LoggerType.splice(objects);
        if (!StringUtils.isEmpty(log)) {
            LoggerType.Warn.logger.warn(log);
        }
        catching(objects);
    }

    public static void error(final Object... objects) {
        String log = LoggerType.splice(objects);
        if (!StringUtils.isEmpty(log)) {
            LoggerType.Error.logger.error(log);
        }
        catching(objects);
    }

    public static void net(final String info) {
        LoggerType.Net.logger.info(info);
    }

    public static void elapsed(final String info) {
        LoggerType.Elapsed.logger.info(info);
    }

    public static void netCheck(String ip, long cost, long pid) {
        Object[] array = new Object[]{getCurrentTimeLogText(), pid, cost, ip};
        LoggerType.NetCheck.logger.info(LoggerType.splice(array));
    }
}
