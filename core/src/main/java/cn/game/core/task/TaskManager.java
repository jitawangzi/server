package cn.game.core.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.MoreExecutors;

import cn.game.util.Config;
import cn.game.util.PriorityThreadFactory;
import cn.game.util.Rnd;
import cn.game.util.SingleThreadFactory;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import io.vertx.core.Context;

/**
 * @Description 服务器端任务
 * @date 2018年11月5日 上午10:23:27
 * @author SYQ
 * @param <R>
 */
public class TaskManager {
	private static Logger log = LoggerFactory.getLogger(TaskManager.class);

	/** 游戏主逻辑线程，修改玩家在内存中的数据，应该都在这个线程中执行 */
	private ExecutorService mainExecutor;
	/** 定时执行线程池 */
	private ScheduledThreadPoolExecutor scheduledThreadPool;
	/** 执行阻塞或者耗时的任务 */
	private ExecutorService workerExecutor;
	/** 非阻塞异步线程池 */
	private ExecutorService asyncExecutor;
	private static final long MAX_DELAY = Long.MAX_VALUE / 1000000 / 2;

	private static final int PLAYER_EXECUTOR_SIZE = Runtime.getRuntime().availableProcessors() * 2;
	private ExecutorService[] playerExecutors = new ExecutorService[PLAYER_EXECUTOR_SIZE];

	private static TaskManager instance = new TaskManager();

	public static TaskManager getInstance() {
		return instance;
	}

	/**
	 * @Description 任务类型
	 * @date 2021年3月15日 下午2:40:41
	 * @author SYQ
	 */
	public enum TaskType {
		MAIN, // 主线程任务
		PLAYER, //玩家任务
		ASYNC, //非IO异步
		BLOCK, //阻塞耗时的任务
		NULL//
	}

//	@FunctionalInterface
//	public interface CallBackTask {
//		public abstract void run(Object arg);
//	}

	private TaskManager() {
//		 mainExecutor = Executors.newSingleThreadExecutor();
		mainExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(
				Config.generalPacketWorkQueueSize),
				new SingleThreadFactory("逻辑主线程", Thread.MAX_PRIORITY), new AbortPolicy());
		
		scheduledThreadPool = new ScheduledThreadPoolExecutor(Config.generalScheduled, new PriorityThreadFactory(
				"ScheduledThreadPool", Thread.NORM_PRIORITY));

		workerExecutor = new ThreadPoolExecutor(Config.generalScheduled, Config.generalScheduled * 5, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(Config.generalPacketWorkQueueSize),
				new SingleThreadFactory("worker-thread", Thread.NORM_PRIORITY), new AbortPolicy());

		asyncExecutor = new ThreadPoolExecutor(Config.generalScheduled, Config.generalScheduled * 5, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(Config.generalPacketWorkQueueSize), new SingleThreadFactory("async-thread",
						Thread.NORM_PRIORITY), new AbortPolicy());

//		for (int i = 0; i < PLAYER_EXECUTOR_SIZE; i++) {
//			playerExecutors[i] = new DefaultEventExecutor();
//		}
	}

	/**
	 * @Description 向主线程中添加一个任务
	 * @param runnable
	 */
	public void addMainTask(Runnable runnable) {

		try {
			mainExecutor.execute(runnable);
		} catch (Throwable e) {
			e.printStackTrace();
			log.error("taskManager main  task  run error ", e);
		}
	}
	/**
	 * @Description 向工作线程池中添加一个任务，用来执行耗时或者阻塞的操作
	 * @param runnable
	 */
	public void addWorkerTask(Runnable runnable) {

		try {
			workerExecutor.execute(runnable);
		} catch (Throwable e) {
			e.printStackTrace();
			log.error("taskManager worker  task  run error ", e);
		}
	}
	/**
	 * @Description 向异步线程池中添加一个任务，执行非阻塞任务
	 * @param runnable
	 */
	public void addAsyncTask(Runnable runnable) {

		try {
			asyncExecutor.execute(runnable);
		} catch (Throwable e) {
			e.printStackTrace();
			log.error("taskManager worker  task  run error ", e);
		}
	}

	public void addPlayerTask(Runnable runnable, long playerId) {

//		ExecutorService executorService = playerExecutors[(int) (playerId % PLAYER_EXECUTOR_SIZE)];
//		try {
//			executorService.execute(runnable);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			log.error("taskManager worker  task  run error ", e);
//		}
//		NetClient netClient = clientmana
	}
	public void addPlayerTask(Runnable runnable, Context context) {

//		ExecutorService executorService = playerExecutors[(int) (playerId % PLAYER_EXECUTOR_SIZE)];
//		try {
//			executorService.execute(runnable);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			log.error("taskManager worker  task  run error ", e);
//		}
//		NetClient netClient = clientmana
	}

	public void addTask(Runnable runnable, TaskType taskType) {

		addTask(runnable, taskType, 0);
	}
	public void addTask(Runnable runnable, TaskType taskType, long id) {

		switch (taskType) {
			case MAIN:
				addMainTask(runnable);
				break;
			case PLAYER:
				addPlayerTask(runnable, id);
				break;
			case ASYNC:
				addAsyncTask(runnable);
				break;
			case BLOCK:
				addWorkerTask(runnable);
				break;
			default:
				break;
		}
	}

	/**
	 * @Description 增加阻塞的任务
	 * @param callbackTask
	 *            回调任务,默认执行在逻辑主线程中
	 * @param blockTask
	 *            当前要执行的阻塞任务
	 */
	public <T> CompletableFuture<T> addBlockTask(Consumer<T> callbackTask, Supplier<T> blockTask) {

		CompletableFuture<T> future = CompletableFuture.supplyAsync(blockTask);
		return future.whenCompleteAsync((t, h) -> {
			try {
				callbackTask.accept(t);
			} catch (Exception e) {
				log.error("", e);
			}
		}, mainExecutor);
	}
	/**
	 * @Description 增加阻塞的任务
	 * @param blockTask
	 *            当前要执行的阻塞任务
	 */
	public <T> CompletableFuture<T> addBlockTask(Supplier<T> blockTask) {

		CompletableFuture<T> future = CompletableFuture.supplyAsync(blockTask);
		return future.whenCompleteAsync((r, e) -> {
			if (e != null) {
				log.error("", e);
			}
		});
	}
	/**
	 * @Description 增加阻塞的任务
	 * @param runnable
	 *            当前要执行的阻塞任务
	 */
	public CompletableFuture<Void> addBlockTask(Runnable runnable) {

		CompletableFuture<Void> future = CompletableFuture.runAsync(runnable);
		return future.whenCompleteAsync((r, e) -> {
			if (e != null) {
				log.error("", e);
			}
		});
	}

	/**
	 * @Description 增加阻塞的任务
	 * @param task   回调任务
	 * @param suppliers 所有任务完成后，获取所有的返回结果，再执行callBack（任务间没有依赖关系）
	 */
	public CompletableFuture<Void> addBlockTaskNoOrder(Consumer<Object[]> callbackTask, Supplier<?>... suppliers) {

		CompletableFuture<?>[] futures = new CompletableFuture[suppliers.length];
		for (int i = 0; i < suppliers.length; i++) {
			futures[i] = CompletableFuture.supplyAsync(suppliers[i]);
		}
		return CompletableFuture.allOf(futures).whenCompleteAsync((v, th) -> {
			Object[] ret = new Object[futures.length];
			for (int j = 0; j < futures.length; j++) {
//				ret[j] = futures[j].getNow(null);
				try {
					ret[j] = futures[j].get();
				} catch (Exception e) {
					log.error("", e);
				}
			}
			try {
				callbackTask.accept(ret); // 需要处理Consumer的异常，Function不用，异常会返回null
			} catch (Exception e) {
				log.error("", e);
			}

		}, mainExecutor);
	}

	/**
	 * @Description 增加阻塞的任务
	 * @param task   回调任务
	 * @param functions 依次在同一线程完成任务，后一个任务会等待前一个任务的返回值，
	 * 都执行完后再执行callBack,最后的任务在默认的Executor中执行
	 */
	public CompletableFuture addBlockTaskOrder(Consumer task, Function... functions) {

		CompletableFuture<Object> first = null;
		for (Function f : functions) {
			if (first == null) {
				first = CompletableFuture.supplyAsync(() -> f.apply(null));
			} else {
				first = first.thenApply(f);
			}
		}
		return first.whenCompleteAsync((t, h) -> {
			try {
				task.accept(t); // 需要处理Consumer的异常，Function不用，异常会返回null
			} catch (Exception e) {
				log.error("", e);
			}
		}, mainExecutor);
	}

	/**
	 * @Description 增加阻塞的任务,依次在同一线程完成任务，后一个任务会等待前一个任务的返回值 第一个正常应该是
	 *              Supplier 类型，最后一个是Consumer类型，不过可以都简化为Function
	 *              这里所有的任务都是在ForkJoinPool池中执行的，需注意线程安全
	 * @param functions
	 */
	public void addBlockTaskOrder(Function... functions) {

		CompletableFuture<Object> first = null;
		for (Function f : functions) {
			if (first == null) {
				first = CompletableFuture.supplyAsync(() -> f.apply(null));
			} else {
				first = first.thenApply(f);
			}
		}
	}

	/**
	 * 延时执行
	 * 
	 * @param r
	 *            待执行的任务
	 * @param delay
	 *            (ms)
	 *            延时时间
	 * @return
	 */
	public ScheduledFuture<?> scheduleGeneral(Runnable r, long delay) {
		try {
			delay = validateDelay(delay);
			return scheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) {
			return null; /* shutdown, ignore */
		}
	}

	/**
	 * @Description 给定的延迟事件执行后，按照固定的间隔周期执行
	 * @param command
	 * @param initialDelay
	 *            首次执行任务的等待时间（毫秒）
	 * @param period
	 *            周期（毫秒）
	 * @return
	 */
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable command, long initialDelay, long period) {
		try {
			period = validateDelay(period);
			initialDelay = validateDelay(initialDelay);
			return scheduledThreadPool.scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) {
			return null; /* shutdown, ignore */
		}
	}
	public long validateDelay(long delay) {
		if (delay < 0) {
			delay = 0;
		} else if (delay > MAX_DELAY) {
			delay = MAX_DELAY;
		}
		return delay;
	}
	/**
	 * 移除定时任务
	 * 
	 * @param task
	 */
	public boolean removeScheduleTask(Runnable task) {
		return scheduledThreadPool.remove(task);
	}

	public void shutdown() {
		// 不需要执行定时任务了，否则会等待
//		MoreExecutors.shutdownAndAwaitTermination(scheduledThreadPool, 60, TimeUnit.SECONDS);
		MoreExecutors.shutdownAndAwaitTermination(mainExecutor, 600, TimeUnit.SECONDS);
		MoreExecutors.shutdownAndAwaitTermination(workerExecutor, 600, TimeUnit.SECONDS);
		MoreExecutors.shutdownAndAwaitTermination(asyncExecutor, 600, TimeUnit.SECONDS);
//		shutdownPlayerExecutors();
	}

	private void shutdownPlayerExecutors() {
		CountDownLatch latch = new CountDownLatch(playerExecutors.length);
		for (int i = 0; i < playerExecutors.length; i++) {
			ExecutorService executorService = playerExecutors[i];
			if (executorService instanceof SingleThreadEventExecutor) {
				SingleThreadEventExecutor executor = (SingleThreadEventExecutor) executorService;
				executor.shutdownGracefully().addListener(f -> {
					latch.countDown();
				});
			}
		}
		try {
			latch.await(600, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws Exception {

//		TaskManager.getInstance().addBlockTaskNoOrder(r -> {
//
//			System.out.println(r.toString());
//		}, () -> {
//			return 0;
//		}, () -> {
//			return 5 / 0;
//		});
//		TaskManager.getInstance().testMutiTaskOrder();
		// TaskManager.getInstance().testMutiTaskNoOrder();
//		TaskManager.getInstance().testBlock();
//		TaskManager.getInstance().testFuncOrder();
		TaskManager.getInstance().testFuture();

		Thread.currentThread().join();

	}

	public void testMutiTaskNoOrder() {

		Consumer task = new Consumer() {
			@Override
			public void accept(Object arg) {
				Object[] args = (Object[]) arg;
				System.out.println("汇总结果，线程　：　" + Thread.currentThread().getName());

				for (int i = 0; i < args.length; i++) {
					System.out.println(args[i]);
				}
			}
		};

		Supplier<?> s1 = () -> delayedUpperCase("abc");
		Supplier<?> s2 = () -> delayedUpperCase("def");
		Supplier<?> s3 = () -> delayedUpperCase("ghi");
		TaskManager.getInstance().addBlockTaskNoOrder(task, new Supplier[] { s1, s2, s3 });

		System.out.println("结束，线程　：　" + Thread.currentThread().getName());

	}

	public void testMutiTaskOrder() {

		Consumer task = new Consumer() {
			@Override
			public void accept(Object arg) {
				System.out.println("汇总结果，线程　：　" + Thread.currentThread().getName());
				System.out.println(arg);
			}
		};

		Function<String, String> s3 = s -> delayedUpperCase(s + "ghi");
		Function<String, String> s2 = s -> delayedUpperCase(s + "def");
		Function<String, String> s1 = s -> delayedUpperCase("abc");

		TaskManager.getInstance().addBlockTaskOrder(task, new Function[] { s1, s2, s3 });

		System.out.println("结束，线程　：　" + Thread.currentThread().getName());
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void testFuncOrder() {

		Function task = new Function() {
			@Override
			public Object apply(Object arg) {
				System.out.println("汇总结果，线程　：　" + Thread.currentThread().getName());
				System.out.println(arg);
				return null;
			}
		};

		Function<String, String> s3 = s -> delayedUpperCase(s + "ghi");
		Function<String, String> s2 = s -> delayedUpperCase(s + "def");
		Function<String, String> s1 = s -> delayedUpperCase("abc");
		TaskManager.getInstance().addBlockTaskOrder(new Function[] { s1, s2, s3, task });

		System.out.println("结束，线程　：　" + Thread.currentThread().getName());

	}

	private static String delayedUpperCase(String now) {
		System.out.println("延时任务准备执行，线程　：　" + Thread.currentThread().getName());
		try {
			Thread.sleep(Rnd.get(500, 2000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("延时任务执行完毕，线程　：　" + Thread.currentThread().getName());
		return now.toUpperCase();
	}

	public void testBlock() {

		int x = 3;
		TaskManager.getInstance().addMainTask(() -> {
			System.out.println("开始执行任务 :" + Thread.currentThread().getName());
		});
		Consumer<String> callBackTask = r -> {
			System.out.println("数据库执行任务后，回掉任务执行 ： " + r.toString() + Thread.currentThread().getName());
			System.out.println("执行逻辑");
		};

		TaskManager.getInstance().addBlockTask(callBackTask, () -> {
			System.out.println("开始执行数据库阻塞任务： " + Thread.currentThread().getName());
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("执行数据库阻塞任务结束： " + Thread.currentThread().getName());
			return " 数据库结果 ";
		});

		TaskManager.getInstance().addMainTask(() -> {
			System.out.println("任务执行完毕： " + Thread.currentThread().getName());
		});
		System.out.println("主线程执行完毕： " + Thread.currentThread().getName());

	}
	public void testFuture() {

		CompletableFuture<String> ret1 = TaskManager.getInstance().addBlockTask(() -> {
			System.out.println("开始执行数据库阻塞任务1： " + Thread.currentThread().getName());
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("执行数据库阻塞任务1结束： " + Thread.currentThread().getName());
			return " 数据库结果1 ";
		});

		CompletableFuture<String> ret2 = TaskManager.getInstance().addBlockTask(() -> {
			System.out.println("开始执行数据库阻塞任务2： " + Thread.currentThread().getName());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("执行数据库阻塞任务2结束： " + Thread.currentThread().getName());
			return " 数据库结果2 ";
		});
		CompletableFuture<String> ret3 = TaskManager.getInstance().addBlockTask(() -> {
			System.out.println("开始执行数据库阻塞任务3： " + Thread.currentThread().getName());
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("执行数据库阻塞任务3结束： " + Thread.currentThread().getName());
			return " 数据库结果3 ";
		});
		CompletableFuture.allOf(ret1, ret2, ret3).whenCompleteAsync((r, e) -> {
			if (e != null) {
				System.out.println(r);
			}
			System.out.println(ret1.getNow("default"));
			System.out.println(ret2.getNow("default"));
			System.out.println(ret3.getNow("default"));
		}, mainExecutor);

	}

}
