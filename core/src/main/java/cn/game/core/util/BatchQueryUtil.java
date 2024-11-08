package cn.game.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.game.core.exception.BatchProcessException;
import cn.game.core.task.BatchProcessResult;
import cn.game.core.task.BatchProcessResult.BatchError;

/**    
 * 分页查询处理的工具类
 * 2024年11月8日 10:16:28
 * @author SYQ
 */
public class BatchQueryUtil {
	/** 
	 * 处理分页查询，单线程版本
	 * @param <T>
	 * @param batchQuery 分页查询
	 * @param processor 查询出来的数据处理器
	 */
    public static <T> void processBatch(BatchQuery<T> batchQuery, Consumer<T> processor) {
        processBatch(batchQuery, processor, 100);
    }
    public static <T> void processBatch(BatchQuery<T> batchQuery, Consumer<T> processor, int batchSize) {
        int offset = 0;
        while (true) {
            List<T> batch = batchQuery.query(offset, batchSize);
            if (batch.isEmpty()) {
                break;
            }
            
            for (T item : batch) {
                processor.accept(item);
            }
            offset += batchSize;
        }
    }

	/** 
	 * 使用多线程处理查询出来的数据，可以指定 ExecutorService
	 * @param <T>
	 * @param batchQuery
	 * @param processor
	 * @param continueOnError	异常时是否继续处理下一个数据
	 * @return
	 */
	public static <T> BatchProcessResult processBatchParallel(BatchQuery<T> batchQuery, Consumer<T> processor, boolean continueOnError) {
		return processBatchParallel(batchQuery, processor, null, 100, continueOnError);
	}

	public static <T> BatchProcessResult processBatchParallel(BatchQuery<T> batchQuery, Consumer<T> processor, ExecutorService executor,
			int batchSize, boolean continueOnError) {

		int totalProcessed = 0;
		List<BatchError> errors = Collections.synchronizedList(new ArrayList<>());

		int currentOffset = 0;
		while (true) {
			final int offset = currentOffset;

			List<T> batch;
			try {
				batch = batchQuery.query(offset, batchSize);
				if (batch.isEmpty()) {
					break;
				}
			} catch (Exception e) {
				BatchError error = new BatchError(offset, -1, e, "Query failed");
				if (!continueOnError) {
					throw new BatchProcessException("Query failed at offset: " + offset, e, new BatchProcessResult(totalProcessed, errors));
				}
				errors.add(error);
				currentOffset += batchSize;
				continue;
			}

			try {
				processBatch(batch, processor, executor, offset, continueOnError, errors, totalProcessed);
			} catch (BatchProcessException e) {
				throw e;
			} catch (Exception e) {
				BatchError error = new BatchError(offset, -1, e, "Batch processing failed");
				if (!continueOnError) {
					throw new BatchProcessException("Processing failed at offset: " + offset, e,
							new BatchProcessResult(totalProcessed, errors));
				}
				errors.add(error);
			}

			totalProcessed += batch.size();
			currentOffset += batchSize;
		}

		return new BatchProcessResult(totalProcessed, errors);
	}

	private static <T> void processBatch(List<T> batch, Consumer<T> processor, ExecutorService executor, int offset,
			boolean continueOnError, List<BatchError> errors, int totalProcessed) {

		AtomicInteger itemIndex = new AtomicInteger(0);

		// 创建任务处理器
		Function<T, CompletableFuture<Void>> taskProcessor = item -> {
			Runnable task = () -> {
				int index = itemIndex.getAndIncrement();
				try {
					processor.accept(item);
				} catch (Exception e) {
					BatchError error = new BatchError(offset, index, e, "Processing failed");
					errors.add(error);
					if (!continueOnError) {
						throw new CompletionException(e);
					}
				}
			};

			return executor == null ? CompletableFuture.runAsync(task) : CompletableFuture.runAsync(task, executor);
		};

		// 执行所有任务
		List<CompletableFuture<Void>> futures = batch.stream().map(taskProcessor).collect(Collectors.toList());

		try {
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		} catch (CompletionException e) {
			if (!continueOnError) {
				throw new BatchProcessException("Processing failed", e.getCause(), new BatchProcessResult(totalProcessed, errors));
			}
		}
	}


	public interface BatchQuery<T> {
		List<T> query(int offset, int limit);
	}
}
