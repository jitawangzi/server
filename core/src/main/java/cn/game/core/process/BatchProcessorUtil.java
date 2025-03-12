package cn.game.core.process;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import cn.game.core.async.AsyncProcessor;
import cn.game.core.process.processor.DataProcessor;
import cn.game.core.process.processor.FlexibleDataProcessor;
import cn.game.core.process.provider.CursorQueryDataProvider;
import cn.game.core.process.provider.DataProvider;
import cn.game.core.process.provider.ListDataProvider;
import cn.game.core.process.provider.OffsetQueryDataProvider;

public class BatchProcessorUtil {
	private static final int DEFAULT_BATCH_SIZE = 100;

	/** 
	 * 创建基于offset分页的数据提供者，带默认每批数量
	 * @param <T>
	 * @param batchQuery
	 * @return
	 */
	public static <T> DataProvider<T> createOffsetProvider(OffsetBatchQuery<T> batchQuery) {
		return new OffsetQueryDataProvider<>(batchQuery, DEFAULT_BATCH_SIZE);
	}

	/** 
	 * 创建基于offset分页的数据提供者
	 * @param <T>
	 * @param batchQuery
	 * @param batchSize
	 * @return
	 */
	public static <T> DataProvider<T> createOffsetProvider(OffsetBatchQuery<T> batchQuery, int batchSize) {
		validateBatchSize(batchSize);
		return new OffsetQueryDataProvider<>(batchQuery, batchSize);
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

	/** 
	 * 单字段游标分页的数据提供器
	 * @param <T> 数据类型
	 * @param <C> 游标类型
	 * @param queryFunction  查询函数，第一个参数为游标数组(例如数据库中的主键)，第二个参数为每批数据的大小
	 * @param extractor		游标提供函数
	 * @param batchSize	每批数据的大小
	 * @param initialValue 初始游标数组 
	 * @return
	 */
	public static <T, C> DataProvider<T> createSingleCursorProvider(BiFunction<C, Integer, List<T>> queryFunction,
			Function<T, C> extractor, int batchSize, C initialValue) {
		BiFunction<Object[], Integer, List<T>> queryFunctionWrapper = (cursors, size) -> queryFunction.apply((C) cursors[0], size);
		Function<T, Object[]> extractorWrapper = t -> new Object[] { extractor.apply(t) };
		return new CursorQueryDataProvider<>(queryFunctionWrapper, extractorWrapper, batchSize, initialValue);
	}

	/** 
	 * 多字段游标分页的数据提供器
	 * @param <T>
	 * @param queryFunction  查询函数，第一个参数为游标数组(例如数据库中的主键)，第二个参数为每批数据的大小
	 * @param extractor		游标提供函数
	 * @param batchSize	每批数据的大小
	 * @param initialValue 初始游标数组 
	 * @return
	 */
	public static <T> DataProvider<T> createMultipleCursorProvider(BiFunction<Object[], Integer, List<T>> queryFunction,
			Function<T, Object[]> extractor,
			int batchSize, Object... initialValue) {
		return new CursorQueryDataProvider<>(queryFunction, extractor, batchSize, initialValue);
	}

	private static void validateProcessor(Object processor) {
		if (processor == null) {
			throw new IllegalArgumentException("Processor cannot be null");
		}
	}

}
