package cn.game.core.net.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

public class EventLoopHealthChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventLoopHealthChecker.class);
    
    public static void startChecking(Vertx vertx) {
        // 每秒检查一次 EventLoop 响应性
        vertx.setPeriodic(1000, id -> {
            for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
                final int index = i;
                long start = System.currentTimeMillis();
                
                vertx.runOnContext(v -> {
                    long delay = System.currentTimeMillis() - start;
                    if (delay > 100) {
                    	LOGGER.warn("EventLoop-{} 响应延迟: {}ms", index, delay);
                        
                        // 检查是否有同步 RPC 调用
                        Thread.getAllStackTraces().forEach((thread, stack) -> {
                            if (thread.getName().contains("eventloop-thread-" + index)) {
                                for (StackTraceElement element : stack) {
                                    if (element.getClassName().contains("RpcClient") &&
                                        element.getMethodName().contains("handleSyncCall")) {
                                    	LOGGER.error("发现 EventLoop 上的同步 RPC 调用！");
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}