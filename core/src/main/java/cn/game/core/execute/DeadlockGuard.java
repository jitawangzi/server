package cn.game.core.execute;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;

/**    
 * 使用邮箱任务的死锁检测
 */
public class DeadlockGuard {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeadlockGuard.class);

	/**
	 * 是否启用死锁检测
	 */
	private static volatile boolean enabled = ServerContext.getInstance().getRunMode().isTest();

	/**
	 * 每个线程的ID调用栈
	 */
	private static final ThreadLocal<Deque<Long>> idStack = ThreadLocal.withInitial(ArrayDeque::new);

	/** 开启检测 */
	public static void enable() {
		enabled = true;
		LOGGER.info("DeadlockGuard enabled");
	}

	/** 关闭检测 */
	public static void disable() {
		enabled = false;
		LOGGER.info("DeadlockGuard disabled");
	}

	public static boolean isEnabled() {
		return enabled;
	}

	/** 在处理某id前调用 */
	public static void enter(long entityId) {
		if (!enabled)
			return;
		idStack.get().push(entityId);
	}

	/** 处理完毕后调用 */
	public static void exit(long entityId) {
		if (!enabled)
			return;
		Deque<Long> stack = idStack.get();
		if (!stack.isEmpty() && stack.peek().equals(entityId)) {
			stack.pop();
		}
	}

	/** 获取当前线程的ID调度栈 */
	public static Deque<Long> getCurrentStack() {
		return idStack.get();
	}

	/** 获取当前正在处理的ID（栈顶） */
	public static Long getCurrentId() {
		Deque<Long> stack = idStack.get();
		return stack.isEmpty() ? null : stack.peek();
	}

	/**
	 * 检查同进程内是否存在跨ID同步等待风险
	 * 在同步调用executeAndAwait等API前调用
	 * @param targetId 你即将同步等待的目标ID
	 */
	public static void checkCrossIdSyncWait(long targetId, String description) {
		if (!enabled)
			return;
		Deque<Long> stack = idStack.get();
		Long currentId = getCurrentId();
		if (currentId != null && !currentId.equals(targetId)) {
			String stackTrace = getStackTraceString(new Exception());
			if (stack.contains(targetId)) {
				String msg = "Potential deadlock: entity " + currentId + " description " + description + " synchronously waiting for "
						+ targetId
						+ ", targetId is already in call stack: " + stack + "\nJava Call Stack:\n" + stackTrace;
				// 先不抛出异常，某些情况下允许跨id同步。 
				LOGGER.warn(msg);
//				throw new IllegalStateException(msg);
			} else {
				LOGGER.warn(
						"Possibly unsafe cross-id sync wait: entity {} description {} synchronously waiting for {}, call stack: {}\nJava Call Stack:\n{}",
						currentId, description, targetId, stack, stackTrace);
				// 先不抛出异常，某些情况下允许跨id同步
//				throw new CrossIdSyncWaitException(targetId, description);
			}
		}
	}

	private static String getStackTraceString(Exception e) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append("\tat ").append(element).append('\n');
		}
		return sb.toString();
	}
}