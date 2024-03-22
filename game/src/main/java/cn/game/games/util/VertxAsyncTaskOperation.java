package cn.game.games.util;

import cn.game.core.async.AsyncTask;
import cn.game.core.task.TaskManager;
import cn.game.core.task.TaskManager.TaskType;

/**
 * 异步任务操作,需指定任务类型，也就是执行任务的线程
 */
public class VertxAsyncTaskOperation {
	
	/** 当前的状态 */
	private volatile int stage;
	/** 异步任务 */
	private final AsyncTask task;
	/** 最终回调线程 */
	private final TaskType finallyExecutor;
	/** 异步处理线程 */
	private final TaskType asyncExecutor;
	/** 按id分配任务时使用 */
	private long asyncId;
	private long finallyId;
	
	public VertxAsyncTaskOperation(AsyncTask task, TaskType asyncExecutor, TaskType finallyExecutor) {
		stage = AsyncTask.STAGE_INITAILZED;
		this.task = task;
		this.asyncExecutor = asyncExecutor;
		this.finallyExecutor = finallyExecutor;
		
	}
	public VertxAsyncTaskOperation(AsyncTask task, TaskType asyncExecutor, long asyncId, TaskType finallyExecutor, long finallyId) {
		this(task, asyncExecutor, finallyExecutor);
		this.asyncId = asyncId;
		this.finallyId = finallyId;

	}
	
	/**
	 * 根据当前所处的状态来执行相应的操作
	 * 
	 * 框架根据返回值来决定调用的方法
	 * 实现者的状态如果更加复杂，可以根据内部状态来进一步决定doStartStep/doIoStep/doStopStep的执行内容
	 * 
	 * 这里无需指定参数，因为参数在其它步骤都已经获得了
	 */
	public void execute() {
		switch (stage) {
			case AsyncTask.STAGE_INITAILZED: {
				stage = task.doStart();
				if (stage == AsyncTask.STAGE_START_DONE) {
					// 指定线程执行IOStep
					TaskManager.getInstance().addTask(this::execute, asyncExecutor, asyncId);
				} else if (stage == AsyncTask.STAGE_IO_DONE) {
					stage = this.task.doStop();
				}
				break;
			}
			case AsyncTask.STAGE_START_DONE: {
				stage = task.doIo();
				if (stage == AsyncTask.STAGE_IO_DONE) {
					// 指定线程执行STOPStep
					TaskManager.getInstance().addTask(this::execute, finallyExecutor, finallyId);
				} else {
					// 可能已经完成，就是一个单纯的异步调用，不需要通知主线程
				}
				break;
			}
			case AsyncTask.STAGE_IO_DONE: {
				stage = this.task.doStop();
				break;
			}
		}
	}

}
