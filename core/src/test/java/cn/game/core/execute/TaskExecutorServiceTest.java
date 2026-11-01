package cn.game.core.execute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.Assertions;
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
	@DisplayName("确保 entityId=0 分配到不同邮箱时并行执行")
	void testParallelExecution_forEntityIdZero() throws Exception {
        // 生产在128个邮箱中随机选择，碰撞时允许串行；固定随机来源才能单独验证并行能力。
        final AtomicLong mailboxIds = new AtomicLong(Long.MAX_VALUE - 128);
        OneToOneMailboxManager mailboxes = new OneToOneMailboxManager(32) {
            @Override protected long mapToMailboxId0() { return mailboxIds.incrementAndGet(); }
        };
        taskExecutorService.close();
        taskExecutorService = new TaskExecutorService(TaskExecutionConfig.getDefault(), mailboxes);
        final int taskCount = 10;
        final CountDownLatch entered = new CountDownLatch(taskCount);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicInteger completed = new AtomicInteger();
        final List<Future<Void>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < taskCount; i++) {
                futures.add(taskExecutorService.execute(0L, () -> {
                    entered.countDown();
                    // 释放前所有任务都须进入；串行执行时第一条任务会挡住其余任务。
                    Assertions.assertTrue(release.await(10, TimeUnit.SECONDS), "测试未释放并行任务");
                    completed.incrementAndGet();
                    return null;
                }, false, "并行栅栏验证", 15_000, null));
            }
            Assertions.assertTrue(entered.await(5, TimeUnit.SECONDS), "entityId=0任务未同时进入执行区");
            Assertions.assertEquals(0, completed.get(), "栅栏释放前不得完成任务");
        } finally {
            // 断言失败也释放真实任务，避免测试清理被自身栅栏阻塞。
            release.countDown();
        }
        AsyncUtils.await(Future.all(futures), 5, TimeUnit.SECONDS);
        Assertions.assertEquals(taskCount, completed.get());
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
				},false, "Subtask from A", taskTimeout);
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
			}, false,"Subtask from B", taskTimeout);

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

	/** 当前检测契约只告警；同时验证消费者仍建立正确的 ID 上下文，不改成仅检查成功返回。 */
	@Test
	@DisplayName("跨 ID 同步等待沿用只告警策略，并保留消费者上下文")
	void testDeadlockCheck_whenEntitiesWaitForEachOther() throws Exception {
		final long entityId1 = 101L;
		final long entityId2 = 102L;
		boolean previouslyEnabled = DeadlockGuard.isEnabled();
		List<String> warnings = Collections.synchronizedList(new ArrayList<>());
		Logger logger = (Logger) LogManager.getLogger(DeadlockGuard.class);
		Level previousLevel = logger.getLevel();
		AbstractAppender appender = new AbstractAppender("guard-contract-test", null,
				PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY) {
			@Override
			public void append(LogEvent event) {
				if (event.getLevel().isMoreSpecificThan(Level.WARN)) {
					warnings.add(event.getMessage().getFormattedMessage());
				}
			}
		};
		appender.start();
		logger.addAppender(appender);
		try {
			// 独立测试没有业务日志配置，默认 ERROR 会过滤待验证的 WARN。
			logger.setLevel(Level.WARN);
			DeadlockGuard.enable();
			Future<String> future = taskExecutorService.execute(entityId1, () -> {
				Assertions.assertEquals(entityId1, DeadlockGuard.getCurrentId());
				return taskExecutorService.executeAndAwait(entityId2, () -> {
					Assertions.assertEquals(entityId2, DeadlockGuard.getCurrentId());
					return "cross-id completed";
				}, false, "guard contract", 5000);
			});
			Assertions.assertEquals("cross-id completed", AsyncUtils.awaitWithException(future, 5, TimeUnit.SECONDS));
			Assertions.assertTrue(warnings.stream().anyMatch(message -> message.contains("cross-id sync wait")
					&& message.contains("101") && message.contains("102")), "必须记录跨 ID 等待诊断");
		} finally {
			logger.setLevel(previousLevel);
			logger.removeAppender(appender);
			appender.stop();
			if (!previouslyEnabled) { DeadlockGuard.disable(); }
		}
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
		},false, "Timeout Test", timeoutMs);

		// 验证 future 因超时而失败
		Exception ex = assertThrows(Exception.class, () -> AsyncUtils.await(future, timeoutMs + 50, TimeUnit.MILLISECONDS));
		assertTrue(ex instanceof TimeoutException || (ex.getCause() != null && ex.getCause() instanceof TimeoutException));
	}

	/** 保留真实生产调度链路，按单调时间验证两段最低延迟及最终丢弃，不要求恰好整秒完成。 */
	@Test
	@DisplayName("发生异常时，应该进行重试")
	void testRetry_whenException() {
		List<Long> attemptTimes = Collections.synchronizedList(new ArrayList<>());
		List<Long> failureTimes = Collections.synchronizedList(new ArrayList<>());
		List<Integer> attempts = Collections.synchronizedList(new ArrayList<>());
		int maxRetries = 3;
		int retryDelaySeconds = 3;
		RuntimeException failure = new RuntimeException("Test exception for retry");
		ErrorHandler errorHandler = context -> {
			if (context.getCause() instanceof SQLException) {
				return ErrorPolicy.discard();
			}
			failureTimes.add(System.nanoTime());
			attempts.add(context.getAttemptCount());
			return context.getAttemptCount() >= maxRetries ? ErrorPolicy.discard()
					: ErrorPolicy.retryHeadWithDelay(retryDelaySeconds, TimeUnit.SECONDS);
		};

		Future<String> future = taskExecutorService.execute(20, () -> {
			attemptTimes.add(System.nanoTime());
			throw failure;
		}, false, "Test exception for retry", 0, errorHandler);
		Assertions.assertSame(failure, Assertions.assertThrows(RuntimeException.class,
				() -> AsyncUtils.awaitWithException(future, 15, TimeUnit.SECONDS)));
		Assertions.assertEquals(List.of(1, 2, 3), attempts);
		Assertions.assertEquals(maxRetries, attemptTimes.size());
		for (int i = 1; i < maxRetries; i++) {
			long elapsedNanos = attemptTimes.get(i) - failureTimes.get(i - 1);
			Assertions.assertTrue(elapsedNanos >= TimeUnit.SECONDS.toNanos(retryDelaySeconds),
					"重试不得早于三秒延迟，实际纳秒=" + elapsedNanos);
		}
		org.slf4j.LoggerFactory.getLogger(getClass()).info("真实重试间隔纳秒: {}, {}",
				attemptTimes.get(1) - failureTimes.get(0), attemptTimes.get(2) - failureTimes.get(1));

		// 直接丢弃由原因身份和一次调用证明，不能用平台调度速度的任意上限代替行为断言。
		AtomicInteger sqlCalls = new AtomicInteger();
		SQLException sqlFailure = new SQLException("sql exception");
		Future<String> future2 = taskExecutorService.execute(21, () -> {
			sqlCalls.incrementAndGet();
			throw sqlFailure;
		}, false, "sql exception for discard", 0, errorHandler);
		Assertions.assertSame(sqlFailure, Assertions.assertThrows(SQLException.class,
				() -> AsyncUtils.awaitWithException(future2, 5, TimeUnit.SECONDS)));
		Assertions.assertEquals(1, sqlCalls.get());
		Assertions.assertEquals(List.of(1, 2, 3), attempts);
	}
}
