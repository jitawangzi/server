package cn.game.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redisson分布式锁
 * 2020年12月10日 下午4:11:36
 * @author SYQ
 */
public class LockUtil {

	private static final Logger log = LoggerFactory.getLogger(LockUtil.class);
	/** 获取锁时的等待时间 */
	public static final int waitTime = 5;
	/** 锁最长持有时间 */
	public static final int leaseTime = 30;

	/**
	 * 同步获取锁，如果正常获取到锁则直接锁定,处理完业务后需要手动释放锁
	 * @param locks
	 * @return lock，成功获取到锁，只能在获取锁成功情况下才能进行后续处理； null，获取锁失败
	 */
	public static RLock tryLockSync(String... locks) {
		RLock lock = initLock(locks);
		try {
			boolean tryLock = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
			if (tryLock) {
				return lock;
			}
		} catch (Exception e) {
			log.warn("try lock failed [{}]", Arrays.toString(locks));
		}
		log.warn("Thread[{}] lock failed, locks[{}] : ", Thread.currentThread().getName(), locks);
		return null;
	}

	/** 
	 * 获取一个不过期的锁，如果获取不到立刻返回。{@link #tryLockSync(String...)}
	 * @param locks
	 * @return 获取到的锁，没获取到返回null
	 */
	public static RLock tryLockNoExpiredNoWaitSync(String... locks) {
		RLock lock = initLock(locks);
		try {
			boolean tryLock = lock.tryLock(0, -1, TimeUnit.SECONDS);
			if (tryLock) {
				return lock;
			}
		} catch (Exception e) {
			log.warn("try lock failed [{}]", Arrays.toString(locks));
		}
		log.warn("Thread[{}] lock failed, locks[{}] : ", Thread.currentThread().getName(), locks);
		return null;
	}
	/**
	 * 异步获取锁，建议使用
	 *              {@link LockUtil#lockAndRunAsync(Consumer, String...)}代替
	 * @param locks
	 * @return
	 */
	public static RFuture<Boolean> lockAsync(String... locks) {

		RLock lock = initLock(locks);
		return lock.tryLockAsync(waitTime, leaseTime, TimeUnit.SECONDS);
	}
	/**
	 * 获取锁之后 直接执行任务,不用释放锁,但是执行任务之前，需要判断是否获取到锁
	 * @param action
	 * @param locks
	 */
	public static void lockAndRunAsync(Consumer<? super Boolean> action, String... locks) {

		RLock lock = initLock(locks);
		long threadId = Thread.currentThread().getId();
		RFuture<Boolean> future = lock.tryLockAsync(waitTime, leaseTime, TimeUnit.SECONDS);

		future.onComplete((res, e) -> {
			try {
				if (e != null) {
					log.error("", e);
				}
				action.accept(res == null ? false : res);
			} catch (Exception e1) {
				log.error("", e1);
			} finally {
				if (res != null && res == true) {
					lock.unlockAsync(threadId).onComplete((r, ee) -> {
						if (ee != null) {
							log.warn("unlock error,locks {} ", locks, ee);
						}
					});
				}
			}
		});
	}
	/**
	 * 对key获取锁之后，在处理数据
	 * @param action
	 *            获取数据后的操作
	 * @param failAction
	 *            失败操作
	 * @param keys
	 *            键集合
	 */
	public static void lockAndRunAsync(Consumer<List<?>> action, Consumer<?> failAction, String... keys) {

		String[] locks = getLockKey(keys);
		RLock lock = initLock(locks);
		long threadId = Thread.currentThread().getId();
		RFuture<Boolean> future = lock.tryLockAsync(waitTime, leaseTime, TimeUnit.SECONDS);
		future.onComplete((res, e) -> {
			try {
				if (e != null) {
					log.error("", e);
				}
				if (res == null || res == false) {
					failAction.accept(null);
				} else {
					RedissonUtil.getAndWaitAsyncBatch(action, keys);
				}
			} catch (Exception e1) {
				log.error("", e1);
			} finally {
				if (res != null && res == true) {
					lock.unlockAsync(threadId).onComplete((r, ee) -> {
						if (ee != null) {
							log.warn("unlock error,locks {} ", locks, ee);
						}
					});
				}
			}
		});
	}

	/**
	 * 把key转化为对应的锁字符串
	 * @param key
	 * @return
	 */
	public static String getLockKey(String key) {
		return "lock_" + key;
	}
	/**
	 * 把key转化为对应的锁字符串
	 * @param keys
	 * @return
	 */
	public static String[] getLockKey(String... keys) {
		String[] ret = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			ret[i] = getLockKey(keys[i]);
		}
		return ret;
	}

	/**
	 * 释放锁，解锁和加锁应该在同一个线程
	 * @param lock
	 */
	public static void unlock(RLock lock) {
		if (lock == null) {
			return ; 
		}
		lock.unlockAsync().onComplete((r, e) -> {
			if (e != null) {
				log.warn("unlock error,lock {} {}", lock, e);
			}
		});
	}

	private static RLock initLock(String... locks) {
		if (locks == null || locks.length == 0) { 
			throw new NullPointerException(" locks is null");
		}
		RLock lock;
		if (locks.length == 1) {
			lock = RedissonUtil.getRedis().getLock(locks[0]);
		} else {
			RLock[] lockArray = new RLock[locks.length];
			Arrays.sort(locks);

			for (int i = 0; i < locks.length; i++) {
				lockArray[i] = RedissonUtil.getRedis().getLock(locks[i]);
			}

			lock = new RedissonMultiLock(lockArray);
		}

		return lock;
	}
	public static void main(String[] args) throws InterruptedException {

		RedissonUtil.set("a", "a");
		RedissonUtil.set("b", "b");
		RedissonUtil.set("c", "c");
		RedissonUtil.set("d", "d");
		// 同步
		RLock lock = tryLockSync("fff");
		if (lock != null) { // 获取到锁
			try {
				System.err.println("执行任务");
				Thread.currentThread().sleep(10 * 1000);
				System.err.println("执行任务完毕");
			} finally {
				unlock(lock);
			}
		} else {

		}
		Consumer<?> failConsumer = r -> {
			System.err.println("获取锁失败");
		};
		new Thread(() -> {
			// 异步
			lockAndRunAsync(r -> {
				for (Object object : r) {
					System.out.println("第二次消费数据： " + object);
				}
			}, failConsumer, "a", "b");
		}).start();
		// 异步
		lockAndRunAsync(r -> {
			for (Object object : r) {
				System.out.println("消费数据： " + object);
			}
		}, failConsumer, "a", "b");
	
	}
}
