package cn.game.core.net.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

public class EventLoopBlockingDetector {
    private static final Logger logger = LoggerFactory.getLogger(EventLoopBlockingDetector.class);
    
    public static void detectBlocking(Vertx vertx) {
        // 每秒检查一次
        vertx.setPeriodic(1000, id -> {
            Thread.getAllStackTraces().forEach((thread, stack) -> {
                if (thread.getName().startsWith("vert.x-eventloop-thread")) {
                    // 检查是否有阻塞调用
                    for (StackTraceElement element : stack) {
                        String method = element.getMethodName();
                        if (method.equals("join") || 
                            method.equals("get") || 
                            method.equals("await") ||
                            method.equals("waitingGet")) {
                            
                            logger.error("⚠️ EventLoop 线程 {} 正在执行阻塞操作！", thread.getName());
                            logger.error("阻塞位置: {}", element);
                            
                            // 打印完整调用栈
                            for (StackTraceElement e : stack) {
                                logger.error("  at {}", e);
                            }
                        }
                    }
                }
            });
        });
    }
}