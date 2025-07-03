package cn.game.core.execute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.game.core.execute.error.ErrorHandler;
import cn.game.core.execute.error.ErrorPolicy;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.AsyncUtils;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * TaskExecutorService 的单元测试类
 * 使用 JUnit 5
 */
@DisplayName("TaskExecutorService 测试")
class TaskExecutorServiceTest {

	// region Stubs and Mocks (为了让测试独立运行的桩代码)

	private static Vertx vertx;
	private TaskExecutorService taskExecutorService;

	@BeforeAll
	static void beforeAll() {
		// 全局初始化Vertx
		vertx = VxHolder.vertx;
//		vertx = Vertx.vertx();
	}

	@AfterAll
	static void afterAll() {
		// 全局关闭Vertx
		if (vertx != null) {
			vertx.close().toCompletionStage().toCompletableFuture().join();
		}
	}

	@BeforeEach
	void setUp() {
		// 每个测试前都创建一个新的 TaskExecutorService 实例
//		taskExecutorService = new TaskExecutorService(TaskExecutionConfig.getDefault(),
//				new SharedMailboxManager(100, 512));

		taskExecutorService = new TaskExecutorService();
	}

	@AfterEach
	void tearDown() {
		// 每个测试后都关闭 TaskExecutorService
		if (taskExecutorService != null) {
			taskExecutorService.close();
		}
	}

	@Test
	@DisplayName("确保相同 entityId 的任务串行执行")
	void testSerialExecution_forSameEntityId() throws Exception {
		final long entityId = 1L;
		final List<Integer> executionOrder = Collections.synchronizedList(new ArrayList<>());
		final int taskCount = 100;

		List<Future<Integer>> futures = new ArrayList<>();
		for (int i = 0; i < taskCount; i++) {
			final int taskNumber = i;
			futures.add(taskExecutorService.execute(entityId, () -> {
				// 模拟耗时操作
				Thread.sleep(5);
				executionOrder.add(taskNumber);
				return taskNumber;
			}, "Task " + i));
		}

		// 等待所有任务完成
		AsyncUtils.await(Future.all(futures), 5, TimeUnit.SECONDS);

		// 验证执行顺序
		assertEquals(taskCount, executionOrder.size());
		for (int i = 0; i < taskCount; i++) {
			assertEquals(i, executionOrder.get(i), "任务 " + i + " 未按序执行");
		}
	}

	@Test
	@DisplayName("确保 entityId=0 的任务并行执行")
	void testParallelExecution_forEntityIdZero() {
		final long entityId = 0L;
		final int taskCount = 10;
		final long sleepTimeMs = 200;
		final AtomicInteger completedTasks = new AtomicInteger(0);
		final CountDownLatch latch = new CountDownLatch(taskCount);

		Callable<Void> task = () -> {
			Thread.sleep(sleepTimeMs);
			completedTasks.incrementAndGet();
			latch.countDown();
			return null;
		};

		// 在限定时间内执行，如果任务是串行的，总耗时会远超 (taskCount * sleepTimeMs)
		// 如果是并行的，总耗时会约等于 sleepTimeMs
		assertTimeoutPreemptively(Duration.ofMillis(sleepTimeMs * 2), () -> {
			for (int i = 0; i < taskCount; i++) {
				taskExecutorService.execute(entityId, task);
			}
			assertTrue(latch.await(sleepTimeMs + 100, TimeUnit.MILLISECONDS), "任务未在预期时间内完成");
		}, "并行任务执行超时");

		assertEquals(taskCount, completedTasks.get());
	}

	@Test
	@DisplayName("从平台线程提交任务后，应在虚拟线程中执行")
	void testExecutionOnVirtualThread_whenSubmittedFromPlatformThread() throws Exception {
		final long entityId = 2L;
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<Boolean> isVirtualThread = new AtomicReference<>();
		final AtomicReference<String> threadName = new AtomicReference<>();

		// 创建一个平台线程
		Thread platformThread = new Thread(() -> {
			taskExecutorService.submitTask(entityId, () -> {
				isVirtualThread.set(Thread.currentThread().isVirtual());
				threadName.set(Thread.currentThread().getName());
				latch.countDown();
			});
		});

		platformThread.start();
		platformThread.join(); // 等待平台线程完成任务提交

		// 等待任务在虚拟线程中执行完毕
		assertTrue(latch.await(2, TimeUnit.SECONDS), "任务执行超时");

		assertTrue(isVirtualThread.get(), "任务未在虚拟线程中执行");
		assertTrue(threadName.get().startsWith("vt-"), "虚拟线程名称不符合预期");
	}

	@Test
	@DisplayName("模拟两个 entityId 互相等待导致的死锁场景")
	void testDeadlock_whenEntitiesWaitForEachOther() {
		final long entityId1 = 101L;
		final long entityId2 = 102L;
		final long taskTimeout = 500; // 设置一个较短的超时时间来检测死锁

		// Task A 在 entityId1 上运行, 它会调用并等待 entityId2 上的一个任务
		Callable<String> taskA = () -> {
			// System.out.println("Task A on " + entityId1 + " started. Waiting for task on
			// " + entityId2);
			String result = "Task A result";
			// 先执行自己的一个任务
			Thread.sleep(50); // 模拟耗时

			// 这里会阻塞当前虚拟线程，直到entityId2上的任务完成
			String resultFromB = null;
//			try {
				resultFromB = taskExecutorService.executeAndAwait(entityId2, () -> {
					System.out.println("Executing subtask on " + entityId2);
					Thread.sleep(100); // 模拟耗时
					return "Result from subtask on 2";
				}, "Subtask from A", 0, taskTimeout);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			return result + " and " + resultFromB;
		};

		// Task B 在 entityId2 上运行, 它会调用并等待 entityId1 上的一个任务
		Callable<String> taskB = () -> {
			// System.out.println("Task B on " + entityId2 + " started. Waiting for task on
			// " + entityId1);
			String result = "Task B result";

			// 先执行自己的一个任务
			Thread.sleep(50); // 模拟耗时

			// 这里会阻塞当前虚拟线程，直到entityId1上的任务完成
			String resultFromA = taskExecutorService.executeAndAwait(entityId1, () -> {
				System.out.println("Executing subtask on " + entityId1);
				Thread.sleep(100); // 模拟耗时
				return "Result from subtask on 1";
			}, "Subtask from B", 0, taskTimeout);

			return result + " and " + resultFromA;
		};

		// 同时提交两个任务，制造死锁条件
		// 1. Task A 在 entityId1 上开始执行，并提交一个子任务到 entityId2 的队列，然后等待子任务结果。
		// 2. Task B 在 entityId2 上开始执行，并提交一个子任务到 entityId1 的队列，然后等待子任务结果。
		// 结果: A 占用了 entity1 的执行权，等待 B 完成。B 占用了 entity2 的执行权，等待 A 完成。
		// A 的子任务和 B 的子任务都在对方队列里排队，无法执行。
		Future<String> futureA = taskExecutorService.execute(entityId1, taskA);
		Future<String> futureB = taskExecutorService.execute(entityId2, taskB);
//		Future<String> futureB = Future.succeededFuture("Task B result");

		// 验证两个任务最终都因为等待对方而超时失败
		assertThrows(RuntimeException.class, () -> AsyncUtils.await(futureA, taskTimeout + 5000, TimeUnit.MILLISECONDS), "任务A应该超时");
		assertThrows(RuntimeException.class, () -> AsyncUtils.await(futureB, taskTimeout + 5000, TimeUnit.MILLISECONDS), "任务B应该超时");

		futureA.onComplete(res -> {
			if (res.failed()) {
				// 任务A失败，打印异常信息
				System.err.println("Task A failed: " + res.cause().getMessage());
			} else {
				System.out.println("Task A completed with result: " + res.result());
			}
		});
		futureB.onComplete(res -> {
			if (res.failed()) {
				// 任务B失败，打印异常信息
				System.err.println("Task B failed: " + res.cause().getMessage());
			} else {
				System.out.println("Task B completed with result: " + res.result());
			}
		});
	}

	@Test
	@DisplayName("检查某个id的任务，阻塞等待另外一个id的结果")
	void testDeadlockCheck_whenEntitiesWaitForEachOther() {
		final long entityId1 = 101L;
		final long entityId2 = 102L;
		DeadlockGuard.enable();

		final long taskTimeout = 5000; // 设置一个较短的超时时间来检测死锁
		// Task A 在 entityId1 上运行, 它会调用并等待 entityId2 上的一个任务
		Callable<String> taskA = () -> {
			String result = "Task A result";
			// 先执行自己的一个任务
			// 这里会阻塞当前虚拟线程，直到entityId2上的任务完成
			// 非法用法，直接抛出异常
			String resultFromB = taskExecutorService.executeAndAwait(entityId2, () -> {
				System.out.println("Executing subtask on " + entityId2);
				Thread.sleep(100); // 模拟耗时
				return "Result from subtask on 2";
			}, "Subtask from A", 0, taskTimeout);
			return result + " and " + resultFromB;
		};

		Future<String> futureA = taskExecutorService.execute(entityId1, taskA);
		TaskExecutionException exception = assertThrows(TaskExecutionException.class, () -> AsyncUtils
				.awaitWithException(futureA, taskTimeout + 5000, TimeUnit.MILLISECONDS),
				"应该抛出 TaskExecutionException");

		assertTrue(ExceptionUtils.indexOfThrowable(exception, CrossIdSyncWaitException.class) > -1, "Cause 应为 CrossIdSyncWaitException");

	}

	@Test
	@DisplayName("当邮箱队列满时，应抛出 QueueFullException")
	void testQueueFullException_whenMailboxIsFull() throws InterruptedException {
		// 使用自定义配置，设置一个很小的队列容量
		TaskExecutionConfig config = TaskExecutionConfig.builder().maxQueueSize(1).build();
		TaskExecutorService smallQueueService = new TaskExecutorService(config);

		final long entityId = 3L;
		final CountDownLatch longTaskStarted = new CountDownLatch(1);

		// 提交一个长时间运行的任务，占满队列
		smallQueueService.execute(entityId, () -> {
			longTaskStarted.countDown();
			Thread.sleep(500);
			return null;
		});

		// 等待长任务开始执行
		assertTrue(longTaskStarted.await(1, TimeUnit.SECONDS));

		// 尝试提交第二个任务，此时队列应该已满 (因为有一个正在处理，所以可以再放一个进队列)
		// 我们需要提交第三个任务来触发异常
		smallQueueService.execute(entityId, () -> "Task 2");

		// 提交第三个任务，预期会失败
		Future<String> future3 = smallQueueService.execute(entityId, () -> "Task 3");

		// 验证 future 失败并且原因是 QueueFullException
		Exception ex = assertThrows(Exception.class, () -> AsyncUtils.await(future3, 1, TimeUnit.SECONDS));
		assertTrue(ex instanceof QueueFullException || ex.getCause() != null && ex.getCause() instanceof QueueFullException,
				"异常类型应为 QueueFullException");

		smallQueueService.close();
	}

	@Test
	@DisplayName("服务关闭后应拒绝新任务")
	void testTaskRejection_afterServiceShutdown() {
		taskExecutorService.close(); // 关闭服务

		assertTrue(taskExecutorService.isShutdown()); // 假设有 isShutdown 方法

		Future<String> future = taskExecutorService.execute(1L, () -> "should not run");

		// 验证 future 失败并且原因是 IllegalStateException
		Exception ex = assertThrows(Exception.class, () -> AsyncUtils.await(future, 1, TimeUnit.SECONDS));
		assertEquals(IllegalStateException.class, ex.getCause().getClass());
		assertEquals("Task executor service is shutting down", ex.getCause().getMessage());
	}

	@Test
	@DisplayName("任务内异常应正确传递给 Future")
	void testTaskFailure_propagatesToFuture() {
		final String errorMessage = "This is a test exception";
		Future<Object> future = taskExecutorService.execute(4L, () -> {
			throw new RuntimeException(errorMessage);
		});

		// 验证 future 失败并且异常信息匹配
		Exception ex = assertThrows(Exception.class, () -> AsyncUtils.awaitWithException(future, 1, TimeUnit.SECONDS));
		assertEquals(RuntimeException.class, ex.getClass());
		assertEquals(errorMessage, ex.getMessage());
	}

	@Test
	@DisplayName("执行超时的任务应被正确处理")
	void testTaskTimeout_whenExecutionExceedsLimit() {
		long timeoutMs = 100;
		Future<Object> future = taskExecutorService.execute(5L, () -> {
			Thread.sleep(timeoutMs + 200); // 确保执行时间超过超时设置
			return "done";
		}, "Timeout Test", 0, timeoutMs);

		// 验证 future 因超时而失败
		Exception ex = assertThrows(Exception.class, () -> AsyncUtils.await(future, timeoutMs + 50, TimeUnit.MILLISECONDS));
		assertTrue(ex instanceof TimeoutException || (ex.getCause() != null && ex.getCause() instanceof TimeoutException));
	}

	@Test
	@DisplayName("发生异常时，应该进行重试")
	void testRetry_whenException() {

		long startTime = System.currentTimeMillis();
		AtomicInteger retryCount = new AtomicInteger(0);
		int maxRetries = 3;
		int retryDelaySeconds = 3;
		ErrorHandler errorHandler = context -> {
			Throwable cause = context.getCause();
			if (cause instanceof SQLException) {
				return ErrorPolicy.discard();
			} else {
				retryCount.incrementAndGet();
				// 重试三次
				// 超过重试次数后，丢弃任务
				System.err.println("重试次数: " + context.getAttemptCount() + ", 错误: " + cause.getMessage());
				if (context.getAttemptCount() >= maxRetries) {
					return ErrorPolicy.discard();
				}
				return ErrorPolicy.retryHeadWithDelay(retryDelaySeconds, TimeUnit.SECONDS);
			}
		};

		Future<String> future = taskExecutorService.execute(20, () -> {
			throw new RuntimeException("Test exception for retry");
		}, "Test exception for retry", 0, 30000, errorHandler); // 超时时间应该比重试的时间长

		Exception ex = assertThrows(Exception.class, () -> AsyncUtils.await(future, 50, TimeUnit.SECONDS));
		assertTrue(ex instanceof RuntimeException || (ex.getCause() != null && ex.getCause() instanceof RuntimeException));

		System.out.println("重试" + maxRetries + "次，耗时: " + (System.currentTimeMillis() - startTime) / 1000 + "秒");
		assertTrue(retryCount.get() == maxRetries, "重试次数应为" + maxRetries + "次");
		// 第三次直接失败了，所以是2个时间间隔
		assertTrue((System.currentTimeMillis() - startTime) / 1000 == (maxRetries - 1) * retryDelaySeconds,
				"总耗时应为 " + (maxRetries - 1) * retryDelaySeconds + " 秒");

		startTime = System.currentTimeMillis();

		Future<String> future2 = taskExecutorService.execute(21, () -> {
			throw new SQLException("sql exception");
		}, "sql exception for discard", 0, 3000, errorHandler);

		ex = assertThrows(Exception.class, () -> AsyncUtils.await(future2, 50, TimeUnit.SECONDS));
		assertTrue(ex instanceof SQLException || (ex.getCause() != null && ex.getCause() instanceof SQLException));
		// 直接失败，不重试，执行时间应该很短
		assertTrue(System.currentTimeMillis() - startTime <= 100);

	}
}
