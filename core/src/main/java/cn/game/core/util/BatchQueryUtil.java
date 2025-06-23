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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.async.AsyncProcessor;
import cn.game.core.exception.BatchProcessException;
import cn.game.core.process.OffsetBatchQuery;
import cn.game.core.task.BatchProcessResult;
import cn.game.core.task.BatchProcessResult.BatchError;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**    
 * 分页查询处理的工具类
 * 2024年11月8日 10:16:28
 * 
 * @link BatchProcessorUtil
 * 
 * @author SYQ
 */

@Deprecated
public class BatchQueryUtil {
	private static final Logger logger = LoggerFactory.getLogger(BatchQueryUtil.class);

	/** 
	 * 处理分页查询，单线程版本
	 * 注意processor这里需要同步逻辑
	 * 也就是查询出来数据后，处理完毕再查询下一批
	 * 查询和处理都是在当前线程。 
	 * 
	 * @param <T>
	 * @param batchQuery 分页查询
	 * @param processor 查询出来的数据处理器
	 * 
	 */
	public static <T> void processBatch(OffsetBatchQuery<T> batchQuery, Consumer<T> processor) {
		processBatch(batchQuery, processor, 100);
	}

	public static <T> void processBatch(OffsetBatchQuery<T> batchQuery, Consumer<T> processor, int batchSize) {
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
	 * 处理分页查询，处理完一批才加载下一批
	 * 使用异步处理器,异步逻辑需要返回Vert.x的Future
	 * @param <T>
	 * @param batchQuery
	 * @param processor
	 * @return
	 */
	public static <T> Future<Void> processBatchAsync(OffsetBatchQuery<T> batchQuery, AsyncProcessor<T> processor, boolean continueOnError) {
		return processBatchAsync(batchQuery, processor, 100, continueOnError);
	}

	public static <T> Future<Void> processBatchAsync(OffsetBatchQuery<T> batchQuery, AsyncProcessor<T> processor, int batchSize,
			boolean continueOnError) {
		Promise<Void> promise = Promise.promise();
		processNextBatchAsync(batchQuery, processor, batchSize, 0, promise, continueOnError);
		return promise.future();
	}

	private static <T> void processNextBatchAsync(OffsetBatchQuery<T> batchQuery, AsyncProcessor<T> processor, int batchSize, int offset,
			Promise<Void> finalPromise, boolean continueOnError) {

		List<T> batch;
		try {
			batch = batchQuery.query(offset, batchSize);
			if (batch.isEmpty()) {
				finalPromise.complete();
				return;
			}
		} catch (Exception e) {
			logger.error("Failed to query batch at offset " + offset, e);
			if (continueOnError) {
				processNextBatchAsync(batchQuery, processor, batchSize, offset + batchSize, finalPromise, continueOnError);
			} else {
				finalPromise.fail(e);
			}
			return;
		}

		// 处理当前批次
		List<Future<?>> futures = new ArrayList<>();
		for (T item : batch) {
			Future<?> itemFuture;
			try {
				itemFuture = Future.fromCompletionStage(processor.process(item));
			} catch (Exception e) {
				// 直接抛出的异常转换为失败的Future
				logger.error("Process: failed to process item at offset " + offset, e);
				if (continueOnError) {
					// 如果继续处理，创建一个成功的空Future
					itemFuture = Future.succeededFuture();
				} else {
					// 如果不继续处理，创建一个失败的Future
					itemFuture = Future.failedFuture(e);
				}
			}

			// 处理异步失败的情况
			itemFuture.onFailure(throwable -> {
				logger.error("Future: failed to process item at offset " + offset, throwable);
			});

			futures.add(itemFuture);
		}

		List<Future<?>> successfulFutures = new ArrayList<>();
		Future.all(futures).onComplete(ar -> {
			boolean hasErrors = false;
			// 检查所有future的结果
			for (Future<?> future : futures) {
				if (future.failed()) {
					hasErrors = true;
					if (!continueOnError) {
						finalPromise.fail(future.cause());
						return;
					}
				} else {
					successfulFutures.add(future);
				}
			}

			// 如果允许错误且至少有一个成功,或者全部成功,则继续处理下一批
			if ((continueOnError && !successfulFutures.isEmpty()) || !hasErrors) {
				processNextBatchAsync(batchQuery, processor, batchSize, offset + batchSize, finalPromise, continueOnError);
			} else {
				// 如果不允许错误且有错误发生,使用第一个失败的future的cause
				futures.stream().filter(Future::failed).findFirst().ifPresent(f -> finalPromise.fail(f.cause()));
			}
		});
	}

	/** 
	 * 使用多线程处理查询出来的数据，默认使用ForkJoinPool执行任务
	 * 注意处理逻辑需要是同步的。
	 * 也是处理完一批才加载下一批
	 * @param <T>
	 * @param batchQuery
	 * @param processor 		同步数据处理器
	 * @param continueOnError	异常时是否继续处理下一个数据
	 * @return
	 */
	public static <T> BatchProcessResult processBatchParallel(OffsetBatchQuery<T> batchQuery, Consumer<T> processor, boolean continueOnError) {
		return processBatchParallel(batchQuery, processor, null, 100, continueOnError);
	}

	/** 
	 * 使用多线程处理查询出来的数据，执行指定执行处理逻辑的线程池
	 * 注意处理逻辑需要是同步的。
	 * 也是处理完一批才加载下一批
	 * @param <T>
	 * @param batchQuery 查询器
	 * @param processor 数据处理器
	 * @param executor 处理任务的线程池
	 * @param batchSize 每次查询的数据量
	 * @param continueOnError 出错时是否继续处理下一个数据
	 * @return
	 */
	public static <T> BatchProcessResult processBatchParallel(OffsetBatchQuery<T> batchQuery, Consumer<T> processor, ExecutorService executor,
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

	/**
	 * 完全并行处理批量数据，所有批次同时加载和处理
	 * 适用于异步处理逻辑（返回CompletableFuture的处理器）
	 * 
	 * 谨慎使用，防止任务堆积
	 * 
	 * @param <T> 数据类型
	 * @param batchQuery 批量查询接口
	 * @param asyncProcessor 异步数据处理器，返回CompletableFuture
	 * @param continueOnError 出错时是否继续处理
	 * @return 处理结果，包含处理总数和错误信息
	 */
	public static <T> BatchProcessResult processBatchParallelAsync(OffsetBatchQuery<T> batchQuery,
			Function<T, CompletableFuture<Void>> asyncProcessor, boolean continueOnError) {
		return processBatchParallelAsync(batchQuery, asyncProcessor, null, 100, continueOnError);
	}

	/**
	 * 完全并行处理批量数据，所有批次同时加载和处理
	 * 适用于异步处理逻辑（返回CompletableFuture的处理器）
	 * 
	 * @param <T> 数据类型
	 * @param batchQuery 批量查询接口
	 * @param asyncProcessor 异步数据处理器，返回CompletableFuture
	 * @param executor 执行异步任务的线程池，如果为null则使用ForkJoinPool
	 * @param batchSize 每批数据量
	 * @param continueOnError 出错时是否继续处理
	 * @return 处理结果，包含处理总数和错误信息
	 */
	public static <T> BatchProcessResult processBatchParallelAsync(OffsetBatchQuery<T> batchQuery,
			Function<T, CompletableFuture<Void>> asyncProcessor, ExecutorService executor, int batchSize, boolean continueOnError) {

		// 用于收集所有批次的Future
		List<CompletableFuture<BatchProcessResult>> batchFutures = new ArrayList<>();
		AtomicInteger currentOffset = new AtomicInteger(0);

		// 不断尝试加载新批次，直到没有数据
		while (true) {
			final int offset = currentOffset.get();
			List<T> batch;

			try {
				batch = batchQuery.query(offset, batchSize);
				if (batch.isEmpty()) {
					break;
				}
			} catch (Exception e) {
				if (!continueOnError) {
					throw new BatchProcessException("Query failed at offset: " + offset, e,
							new BatchProcessResult(0, Collections.singletonList(new BatchError(offset, -1, e, "Query failed"))));
				}
				currentOffset.addAndGet(batchSize);
				continue;
			}

			// 处理当前批次
			CompletableFuture<BatchProcessResult> batchFuture = processOneBatchAsync(batch, asyncProcessor, executor, offset,
					continueOnError);
			batchFutures.add(batchFuture);

			currentOffset.addAndGet(batchSize);
		}

		// 等待所有批次处理完成
		try {
			CompletableFuture<Void> allFutures = CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]));

			if (!continueOnError) {
				// 如果不继续处理错误，等待所有完成或第一个异常
				allFutures.join();
			}

			// 收集所有批次的结果
			List<BatchError> allErrors = new ArrayList<>();
			int totalProcessed = 0;

			for (CompletableFuture<BatchProcessResult> future : batchFutures) {
				try {
					BatchProcessResult result = future.join();
					totalProcessed += result.getProcessedCount();
					allErrors.addAll(result.getErrors());
				} catch (Exception e) {
					if (!continueOnError) {
						throw e;
					}
					// 忽略错误，继续处理其他批次
				}
			}

			return new BatchProcessResult(totalProcessed, allErrors);

		} catch (CompletionException e) {
			throw new BatchProcessException("Processing failed", e.getCause(), new BatchProcessResult(0, Collections.emptyList()));
		}
	}

	private static <T> CompletableFuture<BatchProcessResult> processOneBatchAsync(List<T> batch,
			Function<T, CompletableFuture<Void>> asyncProcessor, ExecutorService executor, int offset, boolean continueOnError) {

		AtomicInteger itemIndex = new AtomicInteger(0);
		List<BatchError> errors = Collections.synchronizedList(new ArrayList<>());

		// 为批次中的每个元素创建处理任务
		List<CompletableFuture<Void>> itemFutures = batch.stream().map(item -> {
			int index = itemIndex.getAndIncrement();
			return asyncProcessor.apply(item).exceptionally(e -> {
				errors.add(new BatchError(offset, index, e, "Processing failed"));
				if (!continueOnError) {
					throw new CompletionException(e);
				}
				return null;
			});
		}).collect(Collectors.toList());

		// 合并批次中所有任务的结果
		return CompletableFuture.allOf(itemFutures.toArray(new CompletableFuture[0]))
				.thenApply(v -> new BatchProcessResult(batch.size(), errors))
				.exceptionally(e -> {
					if (continueOnError) {
						return new BatchProcessResult(batch.size(), errors);
					}
					throw new CompletionException(e);
				});
	}
}
