package cn.game.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * 支持本地/分布式异步链式业务流程编排
 * 支持本地then、远程thenRemote、批量thenSequential等
 * 适配 Vert.x 5.x Future API
 */
public class FuturePipeline<T> {
	private final Future<T> future;

	private FuturePipeline(Future<T> future) {
		this.future = future;
	}

	/** 创建链式起点 */
	public static <T> FuturePipeline<T> start(Future<T> fut) {
		return new FuturePipeline<>(fut);
	}

	/** 用于无初始值起点 */
	public static FuturePipeline<Void> empty() {
		return new FuturePipeline<>(Future.succeededFuture());
	}

	/** 链式：本地异步步骤（返回Future） */
	public <R> FuturePipeline<R> then(Function<T, Future<R>> next) {
		return new FuturePipeline<>(future.compose(next));
	}

	/** 链式：本地同步步骤（返回普通值） */
	public <R> FuturePipeline<R> thenSync(Function<T, R> next) {
		return new FuturePipeline<>(future.map(next));
	}

	/** 链式：本地异步步骤（不依赖上一步结果） */
	public <R> FuturePipeline<R> thenPure(Supplier<Future<R>> next) {
		return new FuturePipeline<>(future.compose(ignored -> next.get()));
	}

	/** 链式分支（例如日志、监控等，原值透传） */
	public FuturePipeline<T> peek(Handler<? super T> handler) {
		future.onSuccess(handler);
		return this;
	}

	/** 链式异常恢复步骤，同步。 相当于异常中的catch逻辑 */
	public FuturePipeline<T> exceptionally(Function<Throwable, T> recover) {
		return new FuturePipeline<T>(future.recover(err -> {
			try {
				T apply = recover.apply(err);
				return Future.succeededFuture(apply);
			} catch (Throwable t) {
				return Future.failedFuture(t);
			}
		}));
	}

	/** 链式异常恢复步骤，异步 相当于异常中的catch逻辑*/
	public FuturePipeline<T> exceptionallyAsync(Function<Throwable, Future<T>> recover) {
		return new FuturePipeline<>(future.recover(recover));
	}

	/** 终止链：成功时 */
	public FuturePipeline<T> onSuccess(Handler<? super T> handler) {
		future.onSuccess(handler);
		return this;
	}

	/** 终止链：失败时 */
	public FuturePipeline<T> onFailure(Handler<Throwable> handler) {
		future.onFailure(handler);
		return this;
	}

	/** 终止链：完成时 */
	public FuturePipeline<T> onComplete(Handler<AsyncResult<T>> handler) {
		future.onComplete(handler);
		return this;
	}

	/** 获取底层Future，可与原生Vert.x生态无缝对接 */
	public Future<T> future() {
		return future;
	}

	// ===================== 分布式调用能力 =========================

	/**
	 * 分布式单步RPC调用
	 * @param rpcCall 传入上一步结果，返回Future
	 */
	public <R> FuturePipeline<R> thenRemote(Function<T, Future<R>> rpcCall) {
		return then(rpcCall);
	}

	/**
	 * 分布式批量顺序异步收集（集成AsyncUtils.sequentialCollect）
	 * 适合：跨服/跨节点批量RPC、分布式聚合、直到满足条件为止
	 *
	 * @param remotes 远程接口实例列表
	 * @param callExecutor (上一步结果, 单个远程实例) -> Future<单次结果>
	 * @param accumulator (单次结果, 累加结果) -> 新累加结果
	 * @param stopPredicate 累加结果是否满足提前终止
	 * @param initial 初始累加值
	 */
	public <E, R, A> FuturePipeline<A> thenSequential(List<E> remotes, BiFunction<T, E, Future<R>> callExecutor,
			BiFunction<R, A, A> accumulator, Predicate<A> stopPredicate, A initial) {
		Future<A> chained = future.compose(prev -> AsyncUtils.sequentialCollect(remotes, remote -> callExecutor.apply(prev, remote),
				accumulator, stopPredicate, initial));
		return new FuturePipeline<>(chained);
	}

	/**
	 * 并行批量异步收集（可选，适合全量分布式聚合）
	 * 适合结果顺序不敏感、所有节点都要访问的场景
	 */
	public <E, R, A> FuturePipeline<A> thenParallel(List<E> remotes, BiFunction<T, E, Future<R>> callExecutor,
			BiFunction<R, A, A> accumulator, A initial) {
		Future<A> chained = future.compose(prev -> {
			List<Future<R>> futures = new ArrayList<>();
			for (E remote : remotes) {
				futures.add(callExecutor.apply(prev, remote));
			}
			return Future.all(futures).map(cf -> {
				List<R> results = cf.list();
				A acc = initial;
				for (R r : results) {
					acc = accumulator.apply(r, acc);
				}
				return acc;
			});
		});
		return new FuturePipeline<>(chained);
	}

	// ========== 可扩展的链式分支、条件、超时、过滤等 =============

	/** 条件判断，只有满足条件才执行下一步，否则直接完成 */
	public FuturePipeline<T> filter(Predicate<T> predicate) {
		Future<T> filtered = future.compose(val -> {
			if (predicate.test(val)) {
				return Future.succeededFuture(val);
			} else {
				return Future.failedFuture(new IllegalStateException("filter predicate failed: " + val));
			}
		});
		return new FuturePipeline<>(filtered);
	}

	public static <T> FuturePipeline<T> of(Future<T> future) {
		return new FuturePipeline<>(future);
	}

	/** 超时控制 */
	public FuturePipeline<T> timeout(long timeoutMs) {
		return new FuturePipeline<>(future.timeout(timeoutMs, TimeUnit.MILLISECONDS));
	}
}