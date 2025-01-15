package cn.game.core.process;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import cn.game.core.async.AsyncProcessor;
import cn.game.core.util.BatchQueryUtil.BatchQuery;

public class BatchProcessorUtil {
	private static final int DEFAULT_BATCH_SIZE = 100;

	// 创建数据提供者
	public static <T> DataProvider<T> createPageQueryProvider(BatchQuery<T> batchQuery) {
		return new PageQueryDataProvider<>(batchQuery, DEFAULT_BATCH_SIZE);
	}

	public static <T> DataProvider<T> createPageQueryProvider(BatchQuery<T> batchQuery, int batchSize) {
		validateBatchSize(batchSize);
		return new PageQueryDataProvider<>(batchQuery, batchSize);
	}

	public static <T> DataProvider<T> createListProvider(List<T> data, int batchSize) {
		validateInput(data, batchSize);
		return new ListDataProvider<>(data, batchSize);
	}

	private static void validateBatchSize(int batchSize) {
		if (batchSize <= 0) {
			throw new IllegalArgumentException("Batch size must be positive");
		}
	}

	private static <T> void validateInput(List<T> data, int batchSize) {
		if (data == null) {
			throw new IllegalArgumentException("Data list cannot be null");
		}
		validateBatchSize(batchSize);
	}

	/** 
	 * 同步处理器
	 * @param <T>
	 * @param processor
	 * @return
	 */
	public static <T> DataProcessor<T> createProcessor(Consumer<T> processor) {
		validateProcessor(processor);
		return new FlexibleDataProcessor<>(processor);
	}

	/** 
	 * 异步处理器
	 * @param <T>
	 * @param processor
	 * @return
	 */
	public static <T> DataProcessor<T> createProcessor(AsyncProcessor<T> processor) {
		validateProcessor(processor);
		return new FlexibleDataProcessor<>(processor);
	}

	/** 
	 * CompletableFuture处理器
	 * @param <T>
	 * @param processor
	 * @return
	 */
	public static <T> DataProcessor<T> createProcessor(Function<T, CompletableFuture<Void>> processor) {
		validateProcessor(processor);
		return new FlexibleDataProcessor<>(processor);
	}

	private static void validateProcessor(Object processor) {
		if (processor == null) {
			throw new IllegalArgumentException("Processor cannot be null");
		}
	}

}
