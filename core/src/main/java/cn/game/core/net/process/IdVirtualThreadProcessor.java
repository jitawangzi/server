package cn.game.core.net.process;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

import cn.game.core.execute.TaskExecutorService;
import cn.game.core.execute.TaskSubmission;
import cn.game.core.execute.TaskNotExecutedException;
import cn.game.core.execute.RequestTiming;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.protocol.manual.ErrorMsgEnum;
import io.vertx.core.Future;

/** 将网络与 RPC 任务送入实体邮箱，并将开始前拒绝明确反馈到原调用者。 */
public class IdVirtualThreadProcessor extends AbstractProcessor {

	/** 保持生产单例延迟获取；测试或独立服务可注入自有执行器。 */
	private final Supplier<TaskExecutorService> executor;

	/** 使用现有全局执行服务，构造时不启动其后台资源。 */
	public IdVirtualThreadProcessor() {
		this.executor = TaskExecutorService::getInstance;
	}

	/** @param executor 本处理器使用的执行服务，close 时沿用原关闭契约 */
	public IdVirtualThreadProcessor(TaskExecutorService executor) {
		this.executor = () -> executor;
	}

	@Override
	public void process(final NetClient netClient, final IProtocol<?> protocol) {
		process(netClient.getPlayerId(), netClient, protocol);
	}

	@Override
	public void process(long objectId, Runnable task) {
		process(objectId, task, false);
	}
	
	@Override
	public void process(long objectId, Runnable task,boolean fast) {
		processAsync(objectId, task, fast).onFailure(failure -> log.error("Task failed for {}", objectId, failure));
	}

	@Override
	public Future<Void> processAsync(long objectId, Runnable task, boolean fast) {
		TaskSubmission<Void> submission = executor.get().submitTaskWithResult(objectId, task, fast, "processor task");
		return submission.result().recover(failure -> Future.failedFuture(submission.hasStarted()
				? Processor.executionFailure(objectId, failure) : new TaskNotExecutedException(failure)));
	}

	@Override
	public void process(long objectId, NetClient netClient, IProtocol<?> protocol) {
		if (!netClient.beginProtocolSubmission(protocol)) { return; }
		RequestTiming timing = RequestTiming.start(protocol.getMsgID());
		Future<Void> execution;
		try {
			execution = processAsync(objectId, () -> {
				try { timing.run(netClient, protocol.getSeq(), () -> super.process(netClient, protocol)); }
				finally { timing.finished(false); }
			}, false);
		} catch (RuntimeException | Error failure) {
			log.error("Network submission failed before returning a result for {}", objectId, failure);
			netClient.endProtocolSubmission(protocol);
			timing.finished(true);
			throw failure;
		}
		execution.onComplete(result -> {
			try {
				if (result.failed()) {
					Throwable failure = result.cause();
					log.warn("Network task failed for {}, message {}", objectId, protocol.getMsgID(), failure);
					if (failure instanceof TaskNotExecutedException) {
						timing.finished(true);
						// 登记保留至拒绝发送结束，防止同序号重入先执行成功再收到旧拒绝。
						netClient.rejectProtocol(protocol, ErrorMsgEnum.system_overload.getId());
					}
				}
			} catch (RuntimeException replyFailure) {
				log.error("Network rejection reply failed for {}, message {}", objectId, protocol.getMsgID(), replyFailure);
			} finally {
				netClient.endProtocolSubmission(protocol);
			}
		});
	}

	@Override
	public <T> T process(long objectId, Callable<T> supplier) {
		try {
			return executor.get().executeAndAwait(objectId, supplier,false,"", 10000);
		} catch (Exception e) {
			log.error("Callable failed for {}", objectId, e);
			throw new RuntimeException("Error processing callable for objectId: " + objectId, e);
		}
	}
	

	@Override
	public <T, R> Future<T> process(long objectId, Callable<R> supplier, Function<R, Future<T>> mapper) {
		Future<R> execute = executor.get().execute(objectId, supplier,false,"", 10000);
		if (mapper != null) {
			return execute.compose(mapper);
		}
		return execute.map(r -> (T) r) ; 
	}
	
	@Override
	public void close() {
		executor.get().close();
	}
}
