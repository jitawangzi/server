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
 * <p>本版本支持 LeaderStateListener，业务可以通过监听器订阅“领导权/租约”变化事件，
 * 结合你已有的 HealthGuard 健康状态来做“状态驱动”的执行决策。</p>
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
     * 注册一个 Leader 任务（可选；如果你改为事件驱动模型，也可以不注册任务）
     *
     * @param task 任务实现
     */
    public void registerTask(LeaderTask task) {
        manager.registerTask(task);
    }

    /**
     * 订阅领导权/租约状态变化
     */
    public void addLeaderStateListener(LeaderStateListener listener) {
        manager.addLeaderStateListener(listener);
    }

    /**
     * 取消订阅领导权/租约状态变化
     */
    public void removeLeaderStateListener(LeaderStateListener listener) {
        manager.removeLeaderStateListener(listener);
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

