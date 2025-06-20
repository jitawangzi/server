package cn.game.core.execute;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 动态分片执行器使用示例
 */
public class DynamicExecutorExample {
    
    public static void main(String[] args) throws Exception {
        // 创建动态分片执行器
        DynamicShardedExecutor executor = DynamicShardedExecutor.builder()
                .withInitialShards(8)           // 初始8个分片
                .withMinShards(4)               // 最少4个分片
                .withMaxShards(32)              // 最多32个分片
                .withScaleCheckInterval(Duration.ofSeconds(5))  // 5秒检查一次
                .withTasksPerShardThreshold(50) // 每个分片50个任务触发扩容
                .build();
                
        // 模拟负载变化
        for (int cycle = 0; cycle < 5; cycle++) {
            System.out.println("\n===== 负载周期 " + (cycle + 1) + " =====");
            System.out.println("当前分片数: " + executor.getShardCount());
            
            // 负载强度随周期变化
            int objectCount = 10 + cycle * 5;
            int tasksPerObject = 20 + cycle * 10;
            
            System.out.println("提交 " + objectCount + " 个对象，每个 " + tasksPerObject + " 个任务");
            
            // 提交大量任务
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < objectCount; i++) {
                String objectId = "object:" + i;
                
                for (int j = 0; j < tasksPerObject; j++) {
                    CompletableFuture<String> future = executor.execute(objectId, () -> {
                        // 模拟工作负载
                        try {
                            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(10, 30));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "Processed";
                    });
                    
                    futures.add(future.thenAccept(result -> {}));
                }
            }
            
            // 等待本周期任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            System.out.println("周期完成，当前分片数: " + executor.getShardCount());
            System.out.println("统计: " + executor.getStats());
            
            // 暂停一下，让系统稳定
            TimeUnit.SECONDS.sleep(2);
        }
        
        // 关闭执行器
        executor.shutdown();
    }
}
