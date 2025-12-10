package cn.game.core.id;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import org.apache.zookeeper.CreateMode;
import org.ehcache.impl.internal.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.id.IdUtil.IdType;
import cn.game.util.ZkHelper;

/**
 * 号段模式服务，借助zookeeper
 */
public class SegmentIdService {
    private static final Logger log = LoggerFactory.getLogger(SegmentIdService.class);
    private static final String ZNODE_PATH = "/server/distributed-id";

    private final ConcurrentMap<IdType, SegmentGenerator> generators = new ConcurrentHashMap<>();

    public void init() {
        for (IdType type : IdType.values()) {
            generators.put(type, new SegmentGenerator(type));
        }
    }

    public long nextId(IdType type) {
        return generators.get(type).nextId();
    }

    private class SegmentGenerator {
        // 每个号段包含的ID数量
        private static final int ID_COUNT_PER_SEGMENT = 10000;
        private static final float CORDON_RATE = 0.5f;

        private final IdType idType;
        
        private int curIdSegment;
        private volatile int nextIdSegment;
        private long minId;
        private long maxId;
        private long cordon;
        private long currentId;

        public SegmentGenerator(IdType idType) {
            this.idType = idType;
            this.curIdSegment = allocateIdSegmentNode(idType);
            reset();
        }

        public synchronized long nextId() {
            long ret = currentId++;
            
            if (ret == cordon) {
                CompletableFuture.supplyAsync(() -> allocateIdSegmentNode(idType))
                    .whenCompleteAsync((segment, ex) -> {
                        if (ex != null) {
                            log.error("预加载号段失败: " + idType, ex);
                        } else {
                            this.nextIdSegment = segment;
                        }
                    });
            } 
            else if (ret == maxId) {
                changeSegment();
            }
            return ret;
        }

        private void changeSegment() {
            int retry = 0;
            while (nextIdSegment == 0 || nextIdSegment == curIdSegment) {
                if (retry++ > 1000) {
                    throw new RuntimeException("无法获取新的号段: " + idType);
                }
                try { Thread.sleep(10); } catch (InterruptedException e) {}
            }
            this.curIdSegment = nextIdSegment;
            reset();
        }

        private void reset() {
            // 公式：minId = (号段索引 - 1) * 步长 + 1
            // 这样号段1 对应 1-10000，号段2 对应 10001-20000
            minId = (long) (this.curIdSegment - 1) * ID_COUNT_PER_SEGMENT + 1;
            maxId = (long) this.curIdSegment * ID_COUNT_PER_SEGMENT;
            cordon = (long) (maxId - ID_COUNT_PER_SEGMENT * CORDON_RATE);
            currentId = minId;
            log.info("号段切换完成 [{}]: Segment={} Range={}-{}", idType, curIdSegment, minId, maxId);
        }

        /**
         * ZK 交互：申请下一个号段序号
         * 增加了初始值检查逻辑
         */
        private int allocateIdSegmentNode(IdType idType) {
            String pathBase = ZNODE_PATH + "/" + idType.name().toLowerCase() + "/id-";
            
            // 1. 计算目标最小号段索引
            // 例如 initialId=100000, size=10000. 
            // 我们希望生成的ID >= 100000。
            // (target - 1) * 10000 + 1 >= 100000  => target >= 10.99 => target = 11
            // 也就是第11个号段开始于 100001。
            long initialId = idType.getInitialId();
            int targetMinSegment = 0;
            if (initialId > 0) {
                 targetMinSegment = (int) ((initialId - 1) / ID_COUNT_PER_SEGMENT) + 1;
            }

            try {
                // 2. 创建节点获取当前序号
                int id = createNode(pathBase);

                // 3. 兼容性检查与初始化（追赶模式）
                // 如果当前ZK生成的序号 小于 目标序号，说明是新系统或者新配置了初始值
                // 我们需要快速消耗掉中间的号段，直到 ZK 序号追上目标值
                if (id < targetMinSegment) {
                    log.warn("[初始化] 类型 {} 当前号段 {} 小于初始配置号段 {}, 开始执行追赶逻辑...", 
                             idType, id, targetMinSegment);
                    
                    while (id < targetMinSegment) {
                        // 删除刚才创建的旧节点（清理垃圾）
                        deleteNode(pathBase, id);
                        
                        // 创建下一个
                        id = createNode(pathBase);
                    }
                    log.info("[初始化] 类型 {} 追赶完成，当前号段: {}", idType, id);
                }

                // 4. 清理上一个节点 (常规逻辑)
                if (id > 1) {
                    deleteNode(pathBase, id - 1);
                }
                
                return id;
            } catch (Exception e) {
                throw new RuntimeException("ZK号段申请失败: " + idType, e);
            }
        }

        private int createNode(String pathBase) throws Exception {
            String path = ZkHelper.curator.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(pathBase);
            return extractSequentialId(path);
        }

        private void deleteNode(String pathBase, int id) {
            try {
                String prevPath = pathBase + String.format("%010d", id);
                ZkHelper.curator.delete().forPath(prevPath);
            } catch (Exception e) {
                // 忽略删除失败，可能是已经被其他进程删了，或者节点不存在
            }
        }

        private int extractSequentialId(String nodePath) {
            String[] parts = nodePath.split("-");
            return Integer.parseInt(parts[parts.length - 1]);
        }
    }
}