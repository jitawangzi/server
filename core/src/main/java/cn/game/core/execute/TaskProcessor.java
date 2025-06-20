package cn.game.core.execute;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 任务处理器 - 在单个虚拟线程上处理任务
 */
class TaskProcessor {
    private final int shardId;
	private final TaskExecutorCallback executor;
    private final BlockingQueue<Task<?>> taskQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Thread processorThread;
    
    /**
	 * 创建任务处理器
	 * @param shardId 分片ID
	 * @param executor 所属执行器回调
	 */
	TaskProcessor(int shardId, TaskExecutorCallback executor) {
        this.shardId = shardId;
        this.executor = executor;
        
        // 创建虚拟线程并启动
        this.processorThread = Thread.startVirtualThread(this::processLoop);
    }
    
    /**
     * 提交任务
     */
    <T> void submit(Task<T> task) {
        taskQueue.add(task);
    }
    
    /**
     * 获取当前队列大小
     */
    int getQueueSize() {
        return taskQueue.size();
    }
    
    /**
     * 主处理循环
     */
    private void processLoop() {
        Thread.currentThread().setName("TaskProcessor-" + shardId);
        
        while (running.get()) {
            try {
                Task<?> task = taskQueue.take();
                
                // 执行任务
                task.execute();
                
                // 通知执行器任务已完成
                executor.taskCompleted(task);
                
            } catch (InterruptedException e) {
                if (!running.get()) {
                    break;
                }
            } catch (Exception e) {
                // 记录异常，但继续处理下一个任务
                System.err.println("Error processing task in shard " + shardId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 关闭处理器
     */
    void shutdown() {
        if (running.compareAndSet(true, false)) {
            processorThread.interrupt();
        }
    }
}

