package cn.game.core.execute;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 任务执行器工厂，创建和配置执行器
 */
public class TaskExecutorFactory {
    private static volatile ConcurrentMap<String, TaskExecutorService> executorServices = new ConcurrentHashMap<>();
    
    /**
     * 创建任务执行服务
     * @param config 执行配置
     * @return 任务执行服务
     */
	public static TaskExecutorService createExecutor(TaskExecutionConfig config) {
        return new TaskExecutorService(config);
    }
    
    /**
     * 创建任务执行服务（使用默认配置）
     * @return 任务执行服务
     */
	public static TaskExecutorService createExecutor() {
		return new TaskExecutorService(TaskExecutionConfig.getDefault());
    }
    
    /**
     * 获取或创建共享的任务执行服务实例
     * @param name 执行器名称
     * @return 共享的任务执行服务
     */
	public static TaskExecutorService getOrCreateExecutor(String name) {
		return executorServices.computeIfAbsent(name, k -> createExecutor());
    }
    
    /**
     * 获取或创建共享的任务执行服务实例
     * @param name 执行器名称
     * @param config 执行配置
     * @return 共享的任务执行服务
     */
	public static TaskExecutorService getOrCreateExecutor(String name, TaskExecutionConfig config) {
		return executorServices.computeIfAbsent(name, k -> createExecutor(config));
    }
    
    /**
     * 关闭执行器
     * @param name 执行器名称
     */
    public static void closeExecutor(String name) {
        TaskExecutorService executor = executorServices.remove(name);
        if (executor != null) {
            executor.close();
        }
    }
    
    /**
     * 关闭所有执行器
     */
    public static void closeAllExecutors() {
        for (TaskExecutorService executor : executorServices.values()) {
            executor.close();
        }
        executorServices.clear();
    }
}

