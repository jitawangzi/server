package cn.game.core.execute;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * 执行工具类，提供一些实用方法，集成Vertx Future
 */
public class ExecutionUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionUtils.class.getName());
    
    /**
     * 检查当前线程是否为虚拟线程
     * @return 如果当前线程是虚拟线程返回true
     */
    public static boolean isVirtualThread() {
        return Thread.currentThread().isVirtual();
    }
    
    /**
     * 确保代码在虚拟线程中执行
     * @param action 要执行的操作
     * @param <T> 结果类型
     * @return 操作结果
     * @throws Exception 如果操作执行失败
     */
    public static <T> T ensureVirtualThread(Callable<T> action) throws Exception {
        if (isVirtualThread()) {
            // 已经在虚拟线程中，直接执行
            return action.call();
        } else {
            // 创建一个新的虚拟线程执行操作
            CompletableFuture<T> future = new CompletableFuture<>();
            Thread.startVirtualThread(() -> {
                try {
                    future.complete(action.call());
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
            
            try {
                return future.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                }
                throw new RuntimeException(e.getCause());
            }
        }
    }
    
    /**
     * 在虚拟线程中运行操作
     * @param action 要执行的操作
     * @param <T> 结果类型
     * @return 包含结果的CompletableFuture
     */
    public static <T> CompletableFuture<T> runInVirtualThread(Callable<T> action) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            try {
                future.complete(action.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }
    
//    /**
//	 * 使用结构化并发执行多个任务，等待所有任务完成
//	 * @param tasks 任务列表，每个任务返回T
//	 * @param <T>  结果类型
//	 * @return 包含所有任务结果的List
//	 * @throws ExecutionException 如果任务执行失败
//	 * @throws InterruptedException 如果线程被中断
//	 */
//    
//    public static <T> List<T> executeAll(List<Callable<? extends T>> tasks) throws ExecutionException, InterruptedException {
//        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
//			List<StructuredTaskScope.Subtask<? extends T>> futures = new ArrayList<>(tasks.size());
//
//            // 提交所有任务
//            for (Callable<? extends T> task : tasks) {
//                futures.add(scope.fork(task));
//            }
//
//            // 等待所有任务完成
//            scope.join();
//            scope.throwIfFailed();
//
//            // 收集结果
//            List<T> results = new ArrayList<>(tasks.size());
//			for (var future : futures) {
//				results.add(future.get());
//            }
//
//            return results;
//        }
//    }
//    
//    /**
//     * 使用结构化并发执行多个任务，只要有一个任务完成就返回
//     * @param tasks 任务列表
//     * @param <T> 结果类型
//     * @return 第一个完成的任务的结果
//     * @throws ExecutionException 如果所有任务执行失败
//     * @throws InterruptedException 如果线程被中断
//     */
//    public static <T> T executeAny(List<Callable<? extends T>> tasks) throws ExecutionException, InterruptedException {
//        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<T>()) {
//            // 提交所有任务
//            for (Callable<? extends T> task : tasks) {
//                scope.fork(task);
//            }
//            
//            // 等待第一个任务成功完成
//            scope.join();
//            
//            // 返回第一个完成的任务的结果
//            return scope.result();
//        }
//    }
    
    /**
     * 重试执行任务直到成功或达到最大重试次数
     * @param task 任务
     * @param maxRetries 最大重试次数
     * @param retryDelayMs 重试延迟（毫秒）
     * @param <T> 任务结果类型
     * @return 任务结果
     * @throws Exception 如果所有重试都失败
     */
    public static <T> T retryUntilSuccess(Callable<T> task, int maxRetries, long retryDelayMs) throws Exception {
        int retries = 0;
        Exception lastException = null;
        
        while (retries <= maxRetries) {
            try {
                return task.call();
            } catch (Exception e) {
                lastException = e;
                retries++;
                
                if (retries > maxRetries) {
                    break;
                }
                
				LOGGER.warn("Retry " + retries + "/" + maxRetries + " after error: " + e.getMessage(), e);
                
                if (retryDelayMs > 0) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ie;
                    }
                }
            }
        }
        
        throw new Exception("Failed after " + maxRetries + " retries", lastException);
    }
    
    /**
     * 带重试的Future执行
     * @param vertx Vertx实例
     * @param operation 操作
     * @param maxRetries 最大重试次数
     * @param retryDelayMs 重试延迟（毫秒）
     * @param <T> 结果类型
     * @return 包含结果的Future
     */
    public static <T> Future<T> retryFuture(Vertx vertx, Supplier<Future<T>> operation, int maxRetries, long retryDelayMs) {
        return retryFuture(vertx, operation, maxRetries, retryDelayMs, 0);
    }

    private static <T> Future<T> retryFuture(Vertx vertx, Supplier<Future<T>> operation, int maxRetries, long retryDelayMs, int attempt) {
        return operation.get().recover(failure -> {
            if (attempt < maxRetries) {
				LOGGER.warn("Retry " + (attempt + 1) + "/" + maxRetries + " after error: " + failure.getMessage());
				Promise<Void> delayPromise = Promise.promise();
				vertx.setTimer(retryDelayMs, id -> delayPromise.complete());

				return delayPromise.future()
                    .compose(v -> retryFuture(vertx, operation, maxRetries, retryDelayMs, attempt + 1));
            } else {
                // 超过重试次数，返回失败
                return Future.failedFuture(failure);
            }
        });
    }
    
    /**
     * 超时Future，如果原始Future在指定时间内未完成，则失败
     * @param vertx Vertx实例
     * @param future 原始Future
     * @param timeoutMs 超时时间（毫秒）
     * @param <T> 结果类型
     * @return 带超时的Future
     */
    public static <T> Future<T> withTimeout(Vertx vertx, Future<T> future, long timeoutMs) {
		return future.timeout(timeoutMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 将CompletableFuture转换为Vertx Future
     * @param vertx Vertx实例
     * @param completableFuture CompletableFuture
     * @param <T> 结果类型
     * @return Vertx Future
     */
    public static <T> Future<T> fromCompletableFuture(Vertx vertx, CompletableFuture<T> completableFuture) {
        Promise<T> promise = Promise.promise();
        completableFuture.whenComplete((result, error) -> {
            if (error != null) {
                promise.fail(error);
            } else {
                promise.complete(result);
            }
        });
        return promise.future();
    }
    
    /**
     * 将Vertx Future转换为CompletableFuture
     * @param future Vertx Future
     * @param <T> 结果类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> toCompletableFuture(Future<T> future) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        future.onComplete(ar -> {
            if (ar.succeeded()) {
                completableFuture.complete(ar.result());
            } else {
                completableFuture.completeExceptionally(ar.cause());
            }
        });
        return completableFuture;
    }
    
    /**
     * 在虚拟线程中等待Future完成（同步等待）
     * @param future Vertx Future
     * @param <T> 结果类型
     * @return Future结果
     * @throws Exception 如果Future执行失败
     */
    public static <T> T awaitFuture(Future<T> future) throws Exception {
        return ensureVirtualThread(() -> future.await());
    }
    
    /**
     * 批量处理列表数据，每批并行处理
     * @param items 要处理的数据列表
     * @param batchSize 每批大小
     * @param processor 处理函数
     * @param <T> 输入类型
     * @param <R> 结果类型
     * @return 包含所有处理结果的Future
     */
    public static <T, R> Future<List<R>> processBatches(
            Vertx vertx, List<T> items, int batchSize, 
            Function<T, Future<R>> processor) {
        
        // 将列表分批
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            batches.add(new ArrayList<>(
                items.subList(i, Math.min(i + batchSize, items.size()))
            ));
        }
        
        // 每批处理完成后继续下一批
        Future<List<R>> result = Future.succeededFuture(new ArrayList<>());
        
        for (List<T> batch : batches) {
            result = result.compose(results -> {
                // 创建此批次所有项目的Future
                List<Future<R>> batchFutures = new ArrayList<>();
                for (T item : batch) {
                    batchFutures.add(processor.apply(item));
                }
                
                // 等待此批次全部完成
                return Future.all(batchFutures).map(compositeFuture -> {
                    // 收集此批次的结果
                    for (int i = 0; i < batchFutures.size(); i++) {
                        results.add(batchFutures.get(i).result());
                    }
                    return results;
                });
            });
        }
        
        return result;
    }
}

