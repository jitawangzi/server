package cn.game.core.leader;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.util.ZkHelper;

/**
 * LeaderFramework
 *
 * <p>便捷入口：组合 Curator（ZK） + Redisson（Redis）以构建“选主 + 租约”的通用执行框架。</p>
 *
 * <p>示例：</p>
 * <pre>
 *   LeaderFramework framework = LeaderFramework.newDefault("/app/leader/global");
 *   framework.registerTask(new MyLeaderTask());
 *   framework.start();
 *   // 服务停止
 *   framework.stop();
 * </pre>
 */
public class LeaderFramework {

    private static final Logger log = LoggerFactory.getLogger(LeaderFramework.class);

    /** Curator 客户端（由 ZkHelper 管理生命周期） */
    private final CuratorFramework curator;
    /** 具体管理逻辑由 LeaderLeaseManager 承担 */
    private final LeaderLeaseManager manager;

    private LeaderFramework(CuratorFramework curator, LeaderLeaseManager manager) {
        this.curator = curator;
        this.manager = manager;
    }

    /**
     * 使用默认参数创建框架实例
     *
     * @param latchPath ZK LeaderLatch 路径
     * @return LeaderFramework
     */
    public static LeaderFramework newDefault(String latchPath) {
        CuratorFramework client = ZkHelper.curator;
        LeaderLeaseManager manager = new LeaderLeaseManager(client, latchPath);
        return new LeaderFramework(client, manager);
    }

    /**
     * 使用自定义参数创建框架实例
     *
     * @param latchPath           ZK LeaderLatch 路径
     * @param leaseTtlSeconds     租约 TTL（秒）
     * @param renewPeriodSeconds  续约周期（秒）
     * @param safetyMarginSeconds 安全余量（秒）
     * @param maxRenewFailures    连续续约失败阈值
     * @return LeaderFramework
     */
    public static LeaderFramework newWithParams(String latchPath,
                                                int leaseTtlSeconds,
                                                int renewPeriodSeconds,
                                                int safetyMarginSeconds,
                                                int maxRenewFailures) {
        CuratorFramework client = ZkHelper.curator;
        LeaderLeaseManager manager = new LeaderLeaseManager(
                client, latchPath,
                leaseTtlSeconds, renewPeriodSeconds, safetyMarginSeconds, maxRenewFailures,
                LeaderLeaseManager.DEFAULT_RETRY_MIN_BACKOFF_MS,
                LeaderLeaseManager.DEFAULT_RETRY_MAX_BACKOFF_MS
        );
        return new LeaderFramework(client, manager);
    }

    /**
     * 注册一个 Leader 任务
     *
     * @param task 任务实现
     */
    public void registerTask(LeaderTask task) {
        manager.registerTask(task);
    }

    /**
     * 启动框架
     *
     * @throws Exception LeaderLatch 启动异常
     */
    public void start() throws Exception {
        manager.start();
        log.info("[LeaderFramework] started.");
    }

    /**
     * 停止框架
     */
    public void stop() {
        manager.stop();
        log.info("[LeaderFramework] stopped.");
    }
}


