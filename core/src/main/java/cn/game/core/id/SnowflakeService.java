package cn.game.core.id;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.game.util.IdWorker;
import cn.game.util.ZkHelper;

/**
 * 雪花算法服务
 * 职责：
 * 1. 在 ZK 上分配唯一的 WorkerID (复用/新建/抢占)
 * 2. 维持 WorkerID 的心跳
 * 3. 提供 nextId() 接口
 */
public class SnowflakeService {
    private static final Logger log = LoggerFactory.getLogger(SnowflakeService.class);

    private static final String ZK_WORKER_ID_PATH = "/server/distributed-id-workers";
    private static final long ZOMBIE_THRESHOLD_MS = 60 * 60 * 1000L; // 1小时
    private static final long HEARTBEAT_INTERVAL_MS = 3000L;
    private static final int DATACENTER_ID = 0;

    // 唯一id
    private final String serverId;
    private IdWorker idWorker;
    private int workerId;
    
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "snowflake-heartbeat");
        t.setDaemon(true);
        return t;
    });

    public SnowflakeService(String serverId) {
        this.serverId = serverId;
    }

    public void init() throws Exception {
        // 1. 确保父路径
        if (ZkHelper.curator.checkExists().forPath(ZK_WORKER_ID_PATH) == null) {
            try {
                ZkHelper.curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZK_WORKER_ID_PATH);
            } catch (Exception ignored) {}
        }

        // 2. 分配 ID
        this.workerId = allocateWorkerId();
        
        // 3. 实例化算法
        this.idWorker = new IdWorker(workerId, DATACENTER_ID);
        
        // 4. 启动心跳
        startHeartbeat();
        
        log.info("SnowflakeService 启动成功. ServerId: [{}], WorkerId: [{}]", serverId, workerId);
    }

    public long nextId() {
        return idWorker.nextId();
    }

    private int allocateWorkerId() throws Exception {
        List<String> children = ZkHelper.curator.getChildren().forPath(ZK_WORKER_ID_PATH);

        // 1. 尝试复用
        for (String child : children) {
            int id = Integer.parseInt(child);
            WorkerNodeInfo info = getNodeData(id);
            if (info != null && serverId.equals(info.getServerId())) {
                updateNodeData(id); // 激活
                return id;
            }
        }

        // 2. 尝试新建
        for (int i = 0; i <= IdWorker.maxWorkerId; i++) {
            String path = ZK_WORKER_ID_PATH + "/" + i;
            if (ZkHelper.curator.checkExists().forPath(path) == null) {
                try {
                    createNode(i);
                    return i;
                } catch (Exception ignored) {} // 并发冲突，重试下一个
            }
        }

        // 3. 尝试抢占僵尸节点
        long now = System.currentTimeMillis();
        for (int i = 0; i <= IdWorker.maxWorkerId; i++) {
            WorkerNodeInfo info = getNodeData(i);
            if (info != null && (info.getLastUpdateTime() + ZOMBIE_THRESHOLD_MS < now)) {
                log.warn("抢占僵尸 WorkerID: {}", i);
                try {
                    updateNodeData(i);
                    return i;
                } catch (Exception ignored) {}
            }
        }

        throw new RuntimeException("无法分配 WorkerID");
    }

    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                WorkerNodeInfo info = getNodeData(workerId);
                if (info != null && !serverId.equals(info.getServerId())) {
                    log.error("WorkerID 被非法抢占！ServerId: {}", info.getServerId());
                    return; // 此时应考虑熔断或报警
                }
                updateNodeData(workerId);
            } catch (Exception e) {
                log.error("心跳失败", e);
            }
        }, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    // --- ZK Utils ---
    
    private void createNode(int id) throws Exception {
        byte[] data = JSON.toJSONString(new WorkerNodeInfo(serverId, System.currentTimeMillis())).getBytes(StandardCharsets.UTF_8);
        ZkHelper.curator.create().withMode(CreateMode.PERSISTENT).forPath(ZK_WORKER_ID_PATH + "/" + id, data);
    }

    private void updateNodeData(int id) throws Exception {
        byte[] data = JSON.toJSONString(new WorkerNodeInfo(serverId, System.currentTimeMillis())).getBytes(StandardCharsets.UTF_8);
        ZkHelper.curator.setData().forPath(ZK_WORKER_ID_PATH + "/" + id, data);
    }

    private WorkerNodeInfo getNodeData(int id) {
        try {
            byte[] bytes = ZkHelper.curator.getData().forPath(ZK_WORKER_ID_PATH + "/" + id);
            return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), WorkerNodeInfo.class);
        } catch (Exception e) {
            return null;
        }
    }

    // DTO
    public static class WorkerNodeInfo {
        private String serverId;
        private Long lastUpdateTime;
        public WorkerNodeInfo() {}
        public WorkerNodeInfo(String s, Long t) { this.serverId = s; this.lastUpdateTime = t; }
        public String getServerId() { return serverId; }
        public void setServerId(String serverId) { this.serverId = serverId; }
        public Long getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(Long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    }
}
