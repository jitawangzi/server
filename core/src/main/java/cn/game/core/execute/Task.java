package cn.game.core.execute;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 任务 - 封装要执行的操作和结果处理
 */
class Task<T> {
    private final String objectId;
    private final Supplier<T> action;
    private final CompletableFuture<T> resultFuture;
    private final long creationTime;
    private long startTime;
    private long endTime;
    
    /**
     * 创建任务
     * @param objectId 对象ID
     * @param action 要执行的操作
     * @param resultFuture 存放结果的Future
     */
    Task(String objectId, Supplier<T> action, CompletableFuture<T> resultFuture) {
        this.objectId = objectId;
        this.action = action;
        this.resultFuture = resultFuture;
        this.creationTime = System.currentTimeMillis();
    }
    
    /**
     * 获取对象ID
     */
    String getObjectId() {
        return objectId;
    }
    
    /**
     * 执行任务
     */
    void execute() {
        if (resultFuture.isDone()) {
            return; // 任务已完成或取消
        }
        
        startTime = System.currentTimeMillis();
        try {
            T result = action.get();
            resultFuture.complete(result);
        } catch (Throwable t) {
            resultFuture.completeExceptionally(
                new ObjectExecutionException("Error executing task for object: " + objectId, t));
        } finally {
            endTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 获取任务等待时间（毫秒）
     */
    long getWaitTime() {
        return startTime > 0 ? startTime - creationTime : System.currentTimeMillis() - creationTime;
    }
    
    /**
     * 获取任务执行时间（毫秒）
     */
    long getExecutionTime() {
        return endTime > 0 && startTime > 0 ? endTime - startTime : 0;
    }
    
    /**
     * 获取任务总时间（毫秒）
     */
    long getTotalTime() {
        long end = endTime > 0 ? endTime : System.currentTimeMillis();
        return end - creationTime;
    }
}

