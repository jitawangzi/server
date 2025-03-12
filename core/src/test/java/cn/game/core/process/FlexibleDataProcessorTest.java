package cn.game.core.process;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.game.core.async.AsyncProcessor;
import cn.game.core.exception.BatchProcessException;
import cn.game.core.process.processor.DataProcessor;
import cn.game.core.process.provider.DataProvider;
import cn.game.core.task.BatchProcessResult;

class FlexibleDataProcessorTest {

	private List<String> testData;
	private DataProvider<String> listProvider;
	private AtomicInteger processedCount;

	@BeforeEach
	void setUp() {
		testData = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
		processedCount = new AtomicInteger(0);
	}

	@Test
	void testSequentialProcessing() {
		// 创建数据提供者
		listProvider = BatchProcessorUtil.createListProvider(testData, 3);

		// 创建处理器
		DataProcessor<String> processor = BatchProcessorUtil.createProcessor(item -> {
			processedCount.incrementAndGet();
			// 模拟处理
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});

		// 配置
		ProcessingConfig config = ProcessingConfig.defaultConfig().setMode(ProcessingMode.SEQUENTIAL).setContinueOnError(true);

		// 执行处理
		BatchProcessResult result = processor.process(listProvider, config);

		// 验证结果
		assertEquals(testData.size(), result.getProcessedCount());
		assertEquals(0, result.getErrors().size());
		assertEquals(testData.size(), processedCount.get());
	}

	@Test
	void testParallelBatchProcessing() {
		listProvider = BatchProcessorUtil.createListProvider(testData, 3);
		ExecutorService executor = Executors.newFixedThreadPool(3);

		DataProcessor<String> processor = BatchProcessorUtil.createProcessor(item -> {
			processedCount.incrementAndGet();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		ProcessingConfig config = ProcessingConfig.defaultConfig()
				.setMode(ProcessingMode.PARALLEL_BATCH)
				.setExecutor(executor)
				.setBatchCompletionMode(BatchCompletionMode.ALL_COMPLETE)
				.setContinueOnError(true);

		BatchProcessResult result = processor.process(listProvider, config);

		executor.shutdown();
		assertEquals(testData.size(), result.getProcessedCount());
		assertEquals(0, result.getErrors().size());
		assertEquals(testData.size(), processedCount.get());
	}

	@Test
	void testAsyncBatchProcessing() {
		listProvider = BatchProcessorUtil.createListProvider(testData, 3);

		DataProcessor<String> processor = BatchProcessorUtil.createProcessor((AsyncProcessor<String>) item -> {
			return CompletableFuture.runAsync(() -> {
				try {
					processedCount.incrementAndGet();
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new CompletionException(e);
				}
			});
		});

		ProcessingConfig config = ProcessingConfig.defaultConfig().setMode(ProcessingMode.ASYNC_BATCH).setContinueOnError(true);

		BatchProcessResult result = processor.process(listProvider, config);

		// 等待一段时间确保异步处理完成
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		assertEquals(testData.size(), result.getProcessedCount());
		assertEquals(0, result.getErrors().size());
		assertEquals(testData.size(), processedCount.get());
	}

	@Test
	void testParallelAllProcessing() {
		listProvider = BatchProcessorUtil.createListProvider(testData, 3);

		DataProcessor<String> processor = BatchProcessorUtil
				.createProcessor((Function<String, CompletableFuture<Void>>) item -> CompletableFuture.runAsync(() -> {
					processedCount.incrementAndGet();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}));

		ProcessingConfig config = ProcessingConfig.defaultConfig().setMode(ProcessingMode.PARALLEL_ALL).setContinueOnError(true);

		BatchProcessResult result = processor.process(listProvider, config);

		assertEquals(testData.size(), result.getProcessedCount());
		assertEquals(0, result.getErrors().size());
		assertEquals(testData.size(), processedCount.get());
	}

	@Test
	void testErrorHandling() {
		listProvider = BatchProcessorUtil.createListProvider(testData, 3);

		DataProcessor<String> processor = BatchProcessorUtil.createProcessor(item -> {
			processedCount.incrementAndGet();
			if (item.equals("5")) {
				throw new RuntimeException("Test error");
			}
		});

		ProcessingConfig config = ProcessingConfig.defaultConfig().setMode(ProcessingMode.SEQUENTIAL).setContinueOnError(true);

		BatchProcessResult result = processor.process(listProvider, config);

		assertEquals(testData.size(), result.getProcessedCount());
		assertEquals(1, result.getErrors().size());
		assertEquals(testData.size(), processedCount.get());
	}

	@Test
	void testErrorHandlingWithStopOnError() {
		listProvider = BatchProcessorUtil.createListProvider(testData, 3);

		DataProcessor<String> processor = BatchProcessorUtil.createProcessor(item -> {
			processedCount.incrementAndGet();
			if (item.equals("5")) {
				throw new RuntimeException("Test error");
			}
		});

		ProcessingConfig config = ProcessingConfig.defaultConfig().setMode(ProcessingMode.SEQUENTIAL).setContinueOnError(false);

		assertThrows(BatchProcessException.class, () -> {
			processor.process(listProvider, config);
		});

		assertTrue(processedCount.get() < testData.size());
	}
}