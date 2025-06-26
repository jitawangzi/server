package cn.game.core.util;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import io.vertx.core.Future;
import io.vertx.core.Promise;

class FuturePipelineTest {
	/**
	 * 典型用法：同步 thenSync/then
	 */
	@Test
	void testThenSyncAndThen() {
		// 初始Future，返回数字42
		Future<Integer> base = Future.succeededFuture(42);

		// thenSync: 变换类型为字符串
		FuturePipeline<String> pipeline = FuturePipeline.start(base).thenSync(i -> "value:" + i);

		// then: 异步变换，拼接后缀
		pipeline = pipeline.then(str -> Future.succeededFuture(str + "-done"));

		// 检查链式结果
		String result = pipeline.future().result();
		assertEquals("value:42-done", result);
	}

	/**
	 * 测试 thenPure: 不依赖前一步结果
	 */
	@Test
	void testThenPure() {
		AtomicInteger supplierCalled = new AtomicInteger(0);

		FuturePipeline<String> pipeline = FuturePipeline.empty().thenPure(() -> {
			supplierCalled.incrementAndGet();
			return Future.succeededFuture("abc");
		});

		assertEquals("abc", pipeline.future().result());
		assertEquals(1, supplierCalled.get(), "thenPure的Supplier应被调用一次");
	}

	/**
	 * 测试 peek: 副作用分支
	 */
	@Test
	void testPeek() {
		AtomicReference<String> ref = new AtomicReference<>();

		FuturePipeline<String> pipeline = FuturePipeline.start(Future.succeededFuture("peek-test")).peek(ref::set);

		// peek不影响主流程
		assertEquals("peek-test", pipeline.future().result());
		assertEquals("peek-test", ref.get(), "peek应能观察值");
	}

	/**
	 * 测试异常同步恢复 exceptionally
	 */
	@Test
	void testExceptionally() {
		RuntimeException exception = new RuntimeException("fail!");
		Future<String> failFuture = Future.failedFuture(exception);

		String recovered = FuturePipeline.start(failFuture).exceptionally(e -> "recovered:" + e.getMessage()).future().result();

		assertEquals("recovered:fail!", recovered);
	}

	/**
	 * 测试异常异步恢复 exceptionallyAsync
	 */
	@Test
	void testExceptionallyAsync() {
		Future<String> failFuture = Future.failedFuture("X");
		String result = FuturePipeline.start(failFuture)
				.exceptionallyAsync(e -> Future.succeededFuture("asyncFix:" + e.getMessage()))
				.future()
				.result();

		assertEquals("asyncFix:X", result);
	}

	/**
	 * 测试终止链 onSuccess/onFailure/onComplete
	 */
	@Test
	void testOnSuccessOnFailureOnComplete() {
		// 成功流
		AtomicReference<Integer> success = new AtomicReference<>();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		AtomicReference<Integer> completed = new AtomicReference<>();

		FuturePipeline<Integer> pipeline = FuturePipeline.start(Future.succeededFuture(100))
				.onSuccess(success::set)
				.onFailure(failure::set)
				.onComplete(res -> completed.set(res.result()));

		assertEquals(100, pipeline.future().result());
		assertEquals(100, success.get());
		assertNull(failure.get());
		assertEquals(100, completed.get());

		// 失败流
		AtomicReference<Integer> failedCompleted = new AtomicReference<>();
		AtomicReference<Throwable> failedFailure = new AtomicReference<>();

		FuturePipeline<Integer> failPipeline = FuturePipeline.<Integer>start(Future.failedFuture(new RuntimeException("fail")))
				.onSuccess(val -> fail("不应成功"))
				.onFailure(failedFailure::set)
				.onComplete(res -> failedCompleted.set(res.result()));

		assertNull(failPipeline.future().result());
		assertNotNull(failedFailure.get());
		assertNull(failedCompleted.get());
	}

	/**
	 * 测试 filter: 满足条件与不满足条件
	 */
	@Test
	void testFilter() {
		// 满足条件
		Future<Integer> base = Future.succeededFuture(20);
		FuturePipeline<Integer> ok = FuturePipeline.start(base).filter(i -> i > 10);
		assertEquals(20, ok.future().result());

		// 不满足条件
		FuturePipeline<Integer> fail = FuturePipeline.start(base).filter(i -> i > 100);
		assertTrue(fail.future().failed());
		assertTrue(fail.future().cause() instanceof IllegalStateException);
	}

	/**
	 * 测试 thenRemote: 实际即为then
	 */
	@Test
	void testThenRemote() {
		FuturePipeline<String> pipeline = FuturePipeline.start(Future.succeededFuture("in"))
				.thenRemote(str -> Future.succeededFuture(str + "-remote"));

		assertEquals("in-remote", pipeline.future().result());
	}

	/**
	 * 测试 thenSequential（模拟分布式、聚合逻辑）
	 */
	@Test
	void testThenSequential() {
		List<String> nodes = Arrays.asList("A", "B", "C");
		// callExecutor: 拼接节点
		BiFunction<String, String, Future<String>> call = (input, node) -> Future.succeededFuture(input + "-" + node);

		// accumulator: 累加到List
		BiFunction<String, List<String>, List<String>> acc = (r, acc0) -> {
			List<String> copy = new ArrayList<>(acc0);
			copy.add(r);
			return copy;
		};

		Predicate<List<String>> stop = acc0 -> false; // 不提前终止

		FuturePipeline<List<String>> pipeline = FuturePipeline.start(Future.succeededFuture("INIT"))
				.thenSequential(nodes, call, acc, stop, new ArrayList<>());

		List<String> res = pipeline.future().result();
		assertEquals(3, res.size());
		assertTrue(res.get(0).contains("A") && res.get(1).contains("B") && res.get(2).contains("C"));
	}

	/**
	 * 测试 thenParallel（模拟并发聚合）
	 */
	@Test
	void testThenParallel() {
		List<Integer> remotes = Arrays.asList(1, 2, 3);
		BiFunction<String, Integer, Future<Integer>> call = (input, remote) -> Future.succeededFuture(remote * 2);

		// 累加总和
		BiFunction<Integer, Integer, Integer> acc = Integer::sum;

		FuturePipeline<Integer> pipeline = FuturePipeline.start(Future.succeededFuture("foo")).thenParallel(remotes, call, acc, 0);

		Integer result = AsyncUtils.await(pipeline.future());
		assertEquals(12, result); // 1*2+2*2+3*2=12
	}

	/**
	 * 测试 timeout（超时控制）
	 * 需要异步场景下才有意义
	 */
	@Test
	void testTimeout() {
		// 慢Future（2秒后完成）
		Promise<String> promise = Promise.promise();
		FuturePipeline<String> pipeline = FuturePipeline.start(promise.future()).timeout(100); // 0.1秒超时

		// 用Awaitility等待异步结果
		await().atMost(500, TimeUnit.MILLISECONDS).until(() -> pipeline.future().isComplete());

		assertTrue(pipeline.future().failed());
		assertTrue(pipeline.future().cause().getMessage().toLowerCase().contains("timeout"), "应为timeout异常");

		// 再测快完成场景
		FuturePipeline<String> fast = FuturePipeline.start(Future.succeededFuture("fast")).timeout(1000);

		assertEquals("fast", fast.future().result());
	}

	/**
	 * 测试 of方法
	 */
	@Test
	void testOf() {
		Future<String> f = Future.succeededFuture("of");
		FuturePipeline<String> pipeline = FuturePipeline.of(f);
		assertEquals("of", pipeline.future().result());
	}

	/**
	 * 演示链式编排的常用写法
	 */
	@Test
	void testChainingUsageDemo() {
		List<String> log = new ArrayList<>();
		String result = FuturePipeline.empty()
				.thenPure(() -> Future.succeededFuture("A"))
				.thenSync(s -> s + "B")
				.peek(log::add)
				.then(str -> Future.succeededFuture(str + "C"))
				.future()
				.result();

		assertEquals("ABC", result);
		assertEquals(1, log.size());
		assertEquals("AB", log.get(0));
	}
}