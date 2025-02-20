package cn.game.core.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import org.apache.zookeeper.CreateMode;
import org.ehcache.impl.internal.concurrent.ConcurrentHashMap;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.cache.CacheType;
import cn.game.util.IdWorker;
import cn.game.util.LockUtil;
import cn.game.util.RedisUtil;
import cn.game.util.ZkHelper;

/**    
 * 分布式id生成
 * 2024年2月5日 上午10:31:04
 * @author SYQ
 */
public class IdUtil {
	private static final Logger log = LoggerFactory.getLogger(IdUtil.class);

	public static IdWorker worker;
//	public static final int workerId = 1;
	public static final int datacenterId = 0;
	private static final long maxCount = IdWorker.maxWorkerId;
	private static final String ZNODE_PATH = "/server/distributed-id";

	private static ConcurrentMap<IdType, IdGenarator> idGenarators = new ConcurrentHashMap<IdType, IdGenarator>();

	// 记录号段，号段内使用自增的id
	public static enum IdType {

		PLAYER, HERO, ITEM, UNION, ORDER, ZONGMEN;
	}

	/** 
	 * 获取按号段自增id
	 * @param idType
	 * @return
	 */
	public static long getIdAutoIncrease(IdType idType) {
		IdGenarator idGenarator = idGenarators.get(idType);
		return idGenarator.nextId();
	}

	/** 
	 * 获取基于雪花算法的分布式唯一id，数字比较大。 
	 * @return
	 */
	public static long getId() {
		return worker.nextId();
	}
	/** 
	 * 生成一个唯一的订单id，把playerId拼进去
	 * @param playerId
	 * @return
	 */
	public static long genOrderId(long playerId) {
		return playerId << 33|getIdAutoIncrease(IdType.ORDER);
	}

	public static void init() throws Exception {
		int workerId = allocateWorkerId();
		worker = new IdWorker(workerId, datacenterId);

		for (IdType idType : IdType.values()) {
			idGenarators.put(idType, new IdGenarator(idType, allocateIdSegmentNode(idType)));
		}
	}

	private static int allocateWorkerId() throws Exception {

		// 获取计数器对象
		RAtomicLong counter = RedisUtil.getRedis().getAtomicLong(CacheType.DISTRIBUTED_WORKER_COUNTER.name());

		// 递增计数器
		int seq = (int) counter.incrementAndGet();

		if (seq == maxCount) {
			// 重新设置计数器为1
			counter.set(1);
		} else if (seq > maxCount) {
			for (int i = 0; i < maxCount; i++) {
				counter.set(1);
				seq = (int) counter.incrementAndGet();
				if (seq < maxCount) {
					break;
				}
				Thread.sleep(100);
			}
		}
		RLock lock = LockUtil.tryLockNoExpiredNoWaitSync(CacheType.SERVER_SEQUENCE.key(seq));
		if (lock != null) {
			return seq;
		}
		return allocateWorkerId();
	}

	private static int allocateIdSegmentNode(IdType idType) {

		// 创建顺序临时节点
		String sequentialNodePath = null;
		int id = 0;
		try {
			sequentialNodePath = ZkHelper.curator.create().creatingParentsIfNeeded()
					.withMode(CreateMode.PERSISTENT_SEQUENTIAL)
					.forPath(ZNODE_PATH + "/" + idType.name().toLowerCase() + "/id-");
			// 从节点路径中提取顺序号，作为唯一 ID
			id = extractSequentialId(sequentialNodePath);
			// 删除前一个节点
			if (id > 1) {
				ZkHelper.curator.delete().forPath(decrementId(sequentialNodePath));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return id;
	}

	/** 
	 * 抽取出顺序号
	 * @param nodePath
	 * @return
	 */
	private static int extractSequentialId(String nodePath) {
		String[] parts = nodePath.split("-");
		return Integer.parseInt(parts[parts.length - 1]);
	}

	/** 
	 * 获取前一个顺序id
	 * @param nodePath
	 * @return
	 */
	private static String decrementId(String nodePath) {
		int index = nodePath.lastIndexOf("-");
		String path = nodePath.substring(0, index);
		String idString = nodePath.substring(index + 1, nodePath.length());
		int number = Integer.parseInt(idString);
		number--;
		return String.format(path + "-%010d", number);
	}

	private static class IdGenarator {

		/** 一次申请多少个id */
		private static final int idCountPerSegment = 10000;
		private static final float cordonRate = 0.5f;

		private int curIdSegment;
		private volatile int nextIdSegment;

		private long minId;
		private long maxId;
		private long cordon;
		private long id;
		private IdType idType;

		private synchronized long nextId() {
			long ret = id++;
			if (ret == cordon) {
				// 请求新的stage
				CompletableFuture.supplyAsync(() -> allocateIdSegmentNode(idType)).whenCompleteAsync((v, throwable) -> {
					this.nextIdSegment = v;
					if (throwable != null) {
						log.error("", throwable);
					}
				});
			} else if (ret == maxId) {
				// 切换
				changeSegment();
			}
			return ret;
		}

		private void changeSegment() {

			if (nextIdSegment == 0 || nextIdSegment == curIdSegment) {
				// WAIT
				int count = 1;
				while (nextIdSegment == 0 || nextIdSegment == curIdSegment) {
					if (count++ >= 1000) {
						throw new RuntimeException("changeSegment error ：" + Thread.currentThread().getName());
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			this.curIdSegment = nextIdSegment;
			reset();
		}

		private IdGenarator(IdType idType, int idSegment) {
			this.curIdSegment = idSegment;
			this.idType = idType;
			reset();
		}

		private void reset() {
			minId = (this.curIdSegment - 1) * idCountPerSegment + 1;
			maxId = this.curIdSegment * idCountPerSegment;
			cordon = (long) (maxId - idCountPerSegment * cordonRate);
			id = minId;
		}
	}
}
