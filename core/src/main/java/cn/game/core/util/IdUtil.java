package cn.game.core.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
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
	private static final String ZK_WORKER_ID_PATH = "/server/distributed-id-workers";
	
	private static ConcurrentMap<IdType, IdGenarator> idGenarators = new ConcurrentHashMap<IdType, IdGenarator>();

	// 记录号段，号段内使用自增的id
	public static enum IdType {

		PLAYER, HERO, ITEM, UNION, ORDER, GUILD;
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
		int workerId = allocateWorkerIdViaZk();
		worker = new IdWorker(workerId, datacenterId);

		for (IdType idType : IdType.values()) {
			idGenarators.put(idType, new IdGenarator(idType, allocateIdSegmentNode(idType)));
		}
	}

	/** 
	 * 借助redis实现id分配，不推荐了，需要使用redis锁
	 * @return
	 * @throws Exception
	 */
	private static int allocateWorkerIdViaRedis() throws Exception {

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
		return allocateWorkerIdViaRedis();
	}
	
	/**
     * 使用 ZK 临时节点“槽位”模式，分配一个可复用的 Worker ID。
     * 这种方式需要处理分布式下的脑裂风险。 
     * @return 分配到的 Worker ID
     */
    private static int allocateWorkerIdViaZkEphemeralNode() throws Exception {
        log.info("开始使用 ZK 分配 Worker ID (上限: {})...", maxCount);

        // 确保父路径存在
        ZkHelper.curator.checkExists().creatingParentContainersIfNeeded().forPath(ZK_WORKER_ID_PATH);

        // 从 0 开始循环，尝试抢占一个 ID 槽位
        for (int workerId = 0; workerId < maxCount; workerId++) {
            String zkNodePath = ZK_WORKER_ID_PATH + "/" + workerId;
            
            try {
                // 关键：创建“临时”节点 (EPHEMERAL)
                // 它的存在就代表了这个 ID 被占用
                ZkHelper.curator.create()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(zkNodePath); // 尝试创建 /server/distributed-id-workers/0, /1, /2...

                // --- 创建成功！---
                // 我们成功“抢占”到了这个 ID，进程生命周期内将持有它
                log.info("成功分配 Worker ID: {}", workerId);
                return workerId;

            } catch (NodeExistsException e) {
                // --- 节点已存在 (NodeExistsException) ---
                // log.debug("Worker ID [{}] 已被占用, 尝试下一个...", workerId);
                // 这个 ID (workerId) 被其他进程占用了，继续循环，尝试下一个 ID
                continue;
            }
        }

        // 如果循环结束都没找到
        throw new RuntimeException(String.format(
            "Worker ID 分配失败！所有槽位 (0-%d) 都已被占用！", maxCount - 1
        ));
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
	 * 使用 ZK 顺序节点分配一个可复用的 Worker ID
	 * 优先复用小的 ID，避免 ID 浪费
	 * 
	 * @return 分配到的 Worker ID
	 * @throws Exception 当所有 ID 都被占用时抛出异常
	 */
	private static int allocateWorkerIdViaZk() throws Exception {
	    log.info("开始使用 ZK 分配 Worker ID (上限: {})...", maxCount);

	    // 确保父路径存在（修改为 creatingParentsIfNeeded）
	    if (ZkHelper.curator.checkExists().forPath(ZK_WORKER_ID_PATH) == null) {
	        ZkHelper.curator.create()
	                .creatingParentsIfNeeded()
	                .withMode(CreateMode.PERSISTENT)
	                .forPath(ZK_WORKER_ID_PATH);
	        log.info("创建 Worker ID 父节点: {}", ZK_WORKER_ID_PATH);
	    }

	    // 第一步：尝试复用已有但未被占用的最小 Worker ID
	    int workerId = tryReuseMinAvailableWorkerId();
	    if (workerId != -1) {
	        log.info("复用 Worker ID: {}", workerId);
	        return workerId;
	    }

	    // 第二步：如果没有可复用的 ID，尝试创建新的顺序节点
	    workerId = tryAllocateNewWorkerId();
	    if (workerId != -1) {
	        log.info("成功分配新的 Worker ID: {}", workerId);
	        return workerId;
	    }

	    // 第三步：所有 ID 都已被占用，抛出异常
	    String errorMsg = String.format(
	        "Worker ID 分配失败！所有槽位 (0-%d) 都已被占用！\n" +
	        "请手动删除 ZooKeeper 节点: %s\n" +
	        "然后重置最小 ID 并重启服务。\n" +
	        "删除命令示例: zkCli.sh deleteall %s",
	        maxCount - 1, ZK_WORKER_ID_PATH, ZK_WORKER_ID_PATH
	    );
	    log.error(errorMsg);
	    throw new RuntimeException(errorMsg);
	}

	/**
	 * 尝试复用最小的可用 Worker ID
	 * 遍历 0 到 maxCount，找到第一个未被占用的 ID
	 * 
	 * @return 可用的 Worker ID，如果没有返回 -1
	 */
	private static int tryReuseMinAvailableWorkerId() throws Exception {
	    // 获取当前所有已占用的 Worker ID 节点
	    List<String> existingNodes = ZkHelper.curator.getChildren().forPath(ZK_WORKER_ID_PATH);
	    
	    // 将节点名转换为 Worker ID 集合
	    Set<Integer> occupiedIds = new HashSet<>();
	    for (String node : existingNodes) {
	        try {
	            // 节点名可能是 "0", "1", "2" 或 "worker-0000000000" 格式
	            int id;
	            if (node.contains("-")) {
	                // 顺序节点格式: worker-0000000001
	                id = extractSequentialId(ZK_WORKER_ID_PATH + "/" + node);
	            } else {
	                // 直接 ID 格式: 0, 1, 2
	                id = Integer.parseInt(node);
	            }
	            occupiedIds.add(id);
	        } catch (NumberFormatException e) {
	            log.warn("无法解析 Worker ID 节点: {}", node);
	        }
	    }

	    // 从 0 开始找第一个未被占用的 ID
	    for (int workerId = 0; workerId < maxCount; workerId++) {
	        if (!occupiedIds.contains(workerId)) {
	            String zkNodePath = ZK_WORKER_ID_PATH + "/" + workerId;
	            try {
	                // 尝试创建临时节点占用这个 ID
	                ZkHelper.curator.create()
	                        .withMode(CreateMode.EPHEMERAL)
	                        .forPath(zkNodePath);
	                return workerId;
	            } catch (NodeExistsException e) {
	                // 并发情况下，其他实例可能刚好占用了这个 ID
	                log.debug("Worker ID [{}] 在尝试占用时已被其他实例占用", workerId);
	                continue;
	            }
	        }
	    }
	    
	    return -1;
	}

	/**
	 * 尝试通过创建顺序节点分配新的 Worker ID
	 * 
	 * @return 分配的 Worker ID，如果超过上限返回 -1
	 */
	private static int tryAllocateNewWorkerId() throws Exception {
	    try {
	        // 创建临时顺序节点
	        String sequentialNodePath = ZkHelper.curator.create()
	                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
	                .forPath(ZK_WORKER_ID_PATH + "/worker-");
	        
	        // 从节点路径中提取顺序号作为 Worker ID
	        int workerId = extractSequentialId(sequentialNodePath);
	        
	        // 检查是否超过上限
	        if (workerId >= maxCount) {
	            // 超过上限，删除刚创建的节点
	            try {
	                ZkHelper.curator.delete().forPath(sequentialNodePath);
	            } catch (Exception e) {
	                log.warn("删除超限节点失败: {}", sequentialNodePath, e);
	            }
	            return -1;
	        }
	        
	        return workerId;
	        
	    } catch (Exception e) {
	        log.error("创建顺序节点失败", e);
	        throw e;
	    }
	}

	/**
	 * 从顺序节点路径中提取序号
	 * 
	 * @param nodePath 节点路径，例如: /server/distributed-id-workers/worker-0000000005
	 * @return 提取的序号，例如: 5
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
