package cn.game.core.process;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.async.AsyncProcessor;
import cn.game.core.exception.BatchProcessException;
import cn.game.core.task.BatchProcessResult;
import cn.game.core.task.BatchProcessResult.BatchError;

public class FlexibleDataProcessor<T> implements DataProcessor<T> {
	private static final Logger logger = LoggerFactory.getLogger(FlexibleDataProcessor.class);

	private final Consumer<T> syncProcessor;
	private final AsyncProcessor<T> asyncProcessor;
	private final Function<T, CompletableFuture<Void>> completableFutureProcessor;

	public FlexibleDataProcessor(Consumer<T> syncProcessor) {
		this.syncProcessor = syncProcessor;
		this.asyncProcessor = null;
		this.completableFutureProcessor = null;
	}

	public FlexibleDataProcessor(AsyncProcessor<T> asyncProcessor) {
		this.syncProcessor = null;
		this.asyncProcessor = asyncProcessor;
		this.completableFutureProcessor = null;
	}

	public FlexibleDataProcessor(Function<T, CompletableFuture<Void>> completableFutureProcessor) {
		this.syncProcessor = null;
		this.asyncProcessor = null;
		this.completableFutureProcessor = completableFutureProcessor;
	}

	@Override
	public BatchProcessResult process(DataProvider<T> dataProvider, ProcessingConfig config) {
		switch (config.getMode()) {
		case SEQUENTIAL:
			return processSequential(dataProvider, config);
		case PARALLEL_BATCH:
			return processParallelBatch(dataProvider, config);
		case PARALLEL_ALL:
			return processParallelAll(dataProvider, config);
		case ASYNC_BATCH:
			return processAsyncBatch(dataProvider, config);
		default:
			throw new IllegalArgumentException("Unsupported processing mode");
		}
	}
	private BatchProcessResult processSequential(DataProvider<T> dataProvider, ProcessingConfig config) {
		if (syncProcessor == null) {
			throw new IllegalStateException("Sync processor is required for sequential processing");
		}

		int totalProcessed = 0;
		List<BatchError> errors = new ArrayList<>();

		while (true) {
			List<T> batch;
			try {
				batch = dataProvider.nextBatch();
				if (batch.isEmpty()) {
					break;
				}
			} catch (Exception e) {
				logger.error("Failed to get next batch", e);
				if (!config.isContinueOnError()) {
					throw new BatchProcessException("Data fetch failed", e);
				}
				errors.add(new BatchError(totalProcessed, -1, e, "Data fetch failed"));
				continue;
			}

			for (int i = 0; i < batch.size(); i++) {
				T item = batch.get(i);
				try {
					syncProcessor.accept(item);
				} catch (Exception e) {
					logger.error("Processing failed at index: " + (totalProcessed + i), e);
					errors.add(new BatchError(totalProcessed + i, -1, e, "Processing failed"));
					if (!config.isContinueOnError()) {
						throw new BatchProcessException("Processing failed", e);
					}
				}
				// 无论处理成功还是失败，只要配置了继续处理，就增加计数
				totalProcessed++;
			}
		}

		return new BatchProcessResult(totalProcessed, errors);
	}

	private BatchProcessResult processParallelBatch(DataProvider<T> dataProvider, ProcessingConfig config) {
		if (syncProcessor == null) {
			throw new IllegalStateException("Sync processor is required for parallel batch processing");
		}

		ExecutorService executor = config.getExecutor() != null ? config.getExecutor() : ForkJoinPool.commonPool();

		AtomicInteger totalProcessed = new AtomicInteger(0);
		List<BatchError> errors = Collections.synchronizedList(new ArrayList<>());

		while (true) {
			List<T> batch;
			try {
				batch = dataProvider.nextBatch();
				if (batch.isEmpty()) {
					break;
				}
			} catch (Exception e) {
				logger.error("Failed to get next batch", e);
				if (!config.isContinueOnError()) {
					throw new BatchProcessException("Data fetch failed", e);
				}
				errors.add(new BatchError(totalProcessed.get(), -1, e, "Data fetch failed"));
				continue;
			}

			List<CompletableFuture<Void>> futures = new ArrayList<>();
			final int batchOffset = totalProcessed.get();
			AtomicInteger batchIndex = new AtomicInteger(0);

			for (T item : batch) {
				CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
					int index = batchIndex.getAndIncrement();
					try {
						syncProcessor.accept(item);
						totalProcessed.incrementAndGet();
					} catch (Exception e) {
						errors.add(new BatchError(batchOffset + index, -1, e, "Processing failed"));
						if (!config.isContinueOnError()) {
							throw new CompletionException(e);
						}
					}
				}, executor);
				futures.add(future);
			}

			try {
				switch (config.getBatchCompletionMode()) {
				case ALL_COMPLETE:
					CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
					break;
				case ANY_COMPLETE:
					CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0])).join();
					break;
				case MAJORITY_COMPLETE:
					waitForMajority(futures);
					break;
				}
			} catch (Exception e) {
				if (!config.isContinueOnError()) {
					return new BatchProcessResult(totalProcessed.get(), errors);
				}
			}
		}

		return new BatchProcessResult(totalProcessed.get(), errors);
	}

	private BatchProcessResult processParallelAll(DataProvider<T> dataProvider, ProcessingConfig config) {
		if (completableFutureProcessor == null) {
			throw new IllegalStateException("CompletableFuture processor is required for parallel all processing");
		}

		List<CompletableFuture<BatchProcessResult>> batchFutures = new ArrayList<>();
		AtomicInteger totalOffset = new AtomicInteger(0);

		while (true) {
			List<T> batch;
			try {
				batch = dataProvider.nextBatch();
				if (batch.isEmpty()) {
					break;
				}
			} catch (Exception e) {
				if (!config.isContinueOnError()) {
					throw new BatchProcessException("Data fetch failed", e);
				}
				continue;
			}

			CompletableFuture<BatchProcessResult> batchFuture = processOneBatchParallel(batch, totalOffset.get(), config);
			batchFutures.add(batchFuture);
			totalOffset.addAndGet(batch.size());
		}

		try {
			CompletableFuture<Void> allFutures = CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]));

			if (!config.isContinueOnError()) {
				allFutures.join();
			}

			List<BatchError> allErrors = new ArrayList<>();
			AtomicInteger totalProcessed = new AtomicInteger(0);

			for (CompletableFuture<BatchProcessResult> future : batchFutures) {
				try {
					BatchProcessResult result = future.join();
					totalProcessed.addAndGet(result.getProcessedCount());
					allErrors.addAll(result.getErrors());
				} catch (Exception e) {
					if (!config.isContinueOnError()) {
						throw e;
					}
				}
			}

			return new BatchProcessResult(totalProcessed.get(), allErrors);

		} catch (Exception e) {
			throw new BatchProcessException("Processing failed", e);
		}
	}

	private BatchProcessResult processAsyncBatch(DataProvider<T> dataProvider, ProcessingConfig config) {
		if (asyncProcessor == null) {
			throw new IllegalStateException("Async processor is required for async batch processing");
		}

		AtomicInteger totalProcessed = new AtomicInteger(0);
		List<BatchError> errors = Collections.synchronizedList(new ArrayList<>());
		CompletableFuture<BatchProcessResult> resultFuture = new CompletableFuture<>();

		processNextAsyncBatch(dataProvider, config, errors, totalProcessed, resultFuture);

		try {
			return resultFuture.get(); // 等待所有处理完成
		} catch (Exception e) {
			throw new BatchProcessException("Async processing failed", e);
		}
	}

	private void processNextAsyncBatch(DataProvider<T> dataProvider, ProcessingConfig config, List<BatchError> errors,
			AtomicInteger totalProcessed, CompletableFuture<BatchProcessResult> finalResult) {

		List<T> batch;
		try {
			batch = dataProvider.nextBatch();
			if (batch.isEmpty()) {
				finalResult.complete(new BatchProcessResult(totalProcessed.get(), errors));
				return;
			}
		} catch (Exception e) {
			logger.error("Failed to get next batch", e);
			if (config.isContinueOnError()) {
				processNextAsyncBatch(dataProvider, config, errors, totalProcessed, finalResult);
			} else {
				finalResult.completeExceptionally(new BatchProcessException("Data fetch failed", e));
			}
			return;
		}

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int i = 0; i < batch.size(); i++) {
			final int index = i;
			T item = batch.get(i);
			CompletableFuture<Void> future = asyncProcessor.process(item)
					.thenRun(() -> totalProcessed.incrementAndGet())
					.exceptionally(e -> {
						errors.add(new BatchError(totalProcessed.get(), index, e, "Processing failed"));
						if (!config.isContinueOnError()) {
							throw new CompletionException(e);
						}
						return null;
					});
			futures.add(future);
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((v, e) -> {
			if (e != null && !config.isContinueOnError()) {
				finalResult.completeExceptionally(new BatchProcessException("Processing failed", e));
			} else {
				processNextAsyncBatch(dataProvider, config, errors, totalProcessed, finalResult);
			}
		});
	}

	private CompletableFuture<BatchProcessResult> processOneBatchParallel(List<T> batch, int offset, ProcessingConfig config) {

		List<BatchError> errors = Collections.synchronizedList(new ArrayList<>());
		List<CompletableFuture<Void>> itemFutures = new ArrayList<>();
		AtomicInteger itemIndex = new AtomicInteger(0);

		for (T item : batch) {
			CompletableFuture<Void> itemFuture = completableFutureProcessor.apply(item).exceptionally(e -> {
				int index = itemIndex.getAndIncrement();
				errors.add(new BatchError(offset + index, -1, e, "Processing failed"));
				if (!config.isContinueOnError()) {
					throw new CompletionException(e);
				}
				return null;
			});
			itemFutures.add(itemFuture);
		}

		return CompletableFuture.allOf(itemFutures.toArray(new CompletableFuture[0]))
				.thenApply(v -> new BatchProcessResult(batch.size(), errors))
				.exceptionally(e -> {
					if (config.isContinueOnError()) {
						return new BatchProcessResult(batch.size(), errors);
					}
					throw new CompletionException(e);
				});
	}

	private void waitForMajority(List<CompletableFuture<Void>> futures) {
		int majority = (futures.size() / 2) + 1;
		AtomicInteger completedCount = new AtomicInteger(0);
		CountDownLatch latch = new CountDownLatch(majority);

		for (CompletableFuture<Void> future : futures) {
			future.whenComplete((result, ex) -> {
				if (completedCount.incrementAndGet() <= majority) {
					latch.countDown();
				}
			});
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CompletionException(e);
		}
	}
}