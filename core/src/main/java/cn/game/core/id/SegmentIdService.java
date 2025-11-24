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
 * 号段模式服务
 * 职责：
 * 1. 管理所有 IdType 的生成器实例
 * 2. 处理 ZK 号段申请逻辑
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

    /**
     * 单个类型的号段生成器 (原 IdUtil.IdGenarator)
     */
    private class SegmentGenerator {
        private static final int ID_COUNT_PER_SEGMENT = 10000;
        private static final float CORDON_RATE = 0.5f;

        private final IdType idType;
        
        // 运行时状态
        private int curIdSegment;
        private volatile int nextIdSegment;
        private long minId;
        private long maxId;
        private long cordon;
        private long currentId;

        public SegmentGenerator(IdType idType) {
            this.idType = idType;
            // 初始化时同步获取第一个号段
            this.curIdSegment = allocateIdSegmentNode(idType);
            reset();
        }

        public synchronized long nextId() {
            long ret = currentId++;
            
            // 达到警戒线，异步预加载下一段
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
            // 达到最大值，切换号段
            else if (ret == maxId) {
                changeSegment();
                // 切换后，ret 应该是新号段的第一个值，而不是旧号段的 maxId
                // 注意：原代码逻辑 ret++ 后返回 ret，如果 ret==maxId，返回的是 maxId (旧段最后值)
                // 下一次调用才会用到新段。这里逻辑保持原样，但在高并发下 changeSegment 需要非常小心。
            }
            return ret;
        }

        private void changeSegment() {
            // 自旋等待异步加载完成
            int retry = 0;
            while (nextIdSegment == 0 || nextIdSegment == curIdSegment) {
                if (retry++ > 1000) {
                    // 极端情况：ZK挂了或者网络断了，这里可以抛异常或降级
                    throw new RuntimeException("无法获取新的号段: " + idType);
                }
                try { Thread.sleep(10); } catch (InterruptedException e) {}
            }
            
            this.curIdSegment = nextIdSegment;
            reset();
        }

		private void reset() {
			minId = (long) (this.curIdSegment - 1) * ID_COUNT_PER_SEGMENT + 1;
			maxId = (long) this.curIdSegment * ID_COUNT_PER_SEGMENT;
			cordon = (long) (maxId - ID_COUNT_PER_SEGMENT * CORDON_RATE);
			currentId = minId;
			log.info("号段切换完成 [{}]: {}-{}", idType, minId, maxId);
		}
    }

    /**
     * ZK 交互：申请下一个号段序号
     */
    private int allocateIdSegmentNode(IdType idType) {
        String pathBase = ZNODE_PATH + "/" + idType.name().toLowerCase() + "/id-";
        try {
            // 创建持久顺序节点
            String path = ZkHelper.curator.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(pathBase);
            
            int id = extractSequentialId(path);
            
            // 删除旧节点 (清理垃圾)
            if (id > 1) {
                try {
                    String prevPath = pathBase + String.format("%010d", id - 1);
                    ZkHelper.curator.delete().forPath(prevPath);
                } catch (Exception e) {
                    // 删除失败不影响主流程
                    log.warn("清理旧号段节点失败", e);
                }
            }
            return id;
        } catch (Exception e) {
            throw new RuntimeException("ZK号段申请失败", e);
        }
    }

    private int extractSequentialId(String nodePath) {
        String[] parts = nodePath.split("-");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}