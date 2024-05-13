package cn.game.games.core.log;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;


/**
 * <pre>
 *     日志模块使用log4j2，且全部使用异步日志输出方式，提高效率，这里有几个坑，做一下说明，帮助以后再次理解：
 *
 *     1.因为使用异步输出，所以记录一次的内容可能不会立刻输出到文件内，需要有下一次的记录才会触发。
 *
 *     2.暂时还没有找到该库里关于自动进行日志切分的方式，所以还是采用flush的方式去主动向日志文件中记录一个null来使日志文件进行切分
 *
 *     3.日志切分都是按天的，以后如果需要按更细粒度的周期进行切分，再做处理
 *
 *     4.结合1和2，就出现一个很奇怪的现象，如果开服以后某个日志一直没有内容记录，则到了日志切分的时候会记录一个null（第2点），但因为
 *     异步输出的原因，这个null不会立刻输出到文件（第1点），就会导致日志切分的操作也不能触发，本次切分就失败了，只有等到再来一个记录
 *     的时候才会触发。针对这个问题，解决策略就是每次开服的时候对每个日志记录一个null（等同于切分日志的操作），促使真正切分日志的操作
 *     能够顺利完成
 *
 * </pre>
 *
 * @author pangjiawei - [Created on 2018/1/30 20:56]
 */
public class LoggerManager {

    private static final LoggerManager INSTANCE = new LoggerManager();

    private LoggerManager() {
    }

    public static LoggerManager getInstance() {
        return INSTANCE;
    }

    public static void init() throws Exception {
//        ServerEventManager.registerEventHandler(INSTANCE);

        String logPath = "..";
//        if (Configuration.startupMode == Configuration.StartupMode.docker) {
//            logPath = "";
//        }
        System.setProperty("SEVER_PATH", logPath);

        String cylog = System.getenv("CYLOG_PATH");
        if (cylog != null) {
            //畅游环境，直接用运维配置的地址
        } else {
            cylog = logPath + "/logs/cylog";
        }
        System.setProperty("SEVER_PATH_CYLOG", cylog);


        String fileName = "log4j2.xml";
//        File file = new File(Configuration.contextPath + fileName);
		String contextPath = System.getProperty("user.dir") + "/";
		File file = new File(contextPath + fileName);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        ConfigurationSource source = new ConfigurationSource(in);
        Configurator.initialize(null, source);

        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.trace, SystemLogger::trace);
        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.debug, SystemLogger::debug);
        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.info, SystemLogger::info);
        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.warn, SystemLogger::warn);
        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.error, SystemLogger::error);
        EmbeddedLogger.setLevelLogger(EmbeddedLogger.Level.fatal, SystemLogger::fatal);

        flushAll();
    }

//    @Override
//    public ServerEventType[] getEventTypes() {
//        return EVENT_TYPES;
//    }

//    @Override
//    public void handle(ServerEvent event) {
//        switch (event.getEventType()) {
//            case newDay:
//                //日志配置切分策略可自行生效，无需主动向日志输出内容
////                flushAll();
//                break;
//            default:
//                break;
//        }
//    }

    private static void flushAll() {
        for (LoggerType loggerType : LoggerType.values()) {
            loggerType.logger.error("null");
        }
    }
}
