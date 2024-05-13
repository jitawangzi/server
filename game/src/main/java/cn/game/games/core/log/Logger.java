package cn.game.games.core.log;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.Level;

/**
 * @author pangjiawei - [Created on 2018/1/31 10:35]
 */
public class Logger {

    private static final DateTimeFormatter formatterSecond = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z");

    static void catching(final Object... objects) {
        for (Object object : objects) {
            if (object instanceof Throwable) {
                LoggerType.Error.logger.catching(Level.ERROR, (Throwable) object);
            }
        }
    }

    public static String getCurrentTimeLogText() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).format(formatterSecond);
    }

    public static String getCurrentTimeLogText1() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).format(formatter);
    }

}
