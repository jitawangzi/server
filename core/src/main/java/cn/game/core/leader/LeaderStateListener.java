package cn.game.core.leader;

/**
 * 领导权与租约状态监听
 *
 * 业务侧可以订阅该事件，以“状态驱动”的方式决定是否接受触发任务、降级或拒绝。
 */
public interface LeaderStateListener {

    /**
     * 当领导权状态变化时回调（来自 ZK LeaderLatch）
     *
     * @param isLeader 当前是否为 ZK Leader
     */
    default void onLeaderChanged(boolean isLeader) {}

    /**
     * 当租约持有状态变化时回调（仅在 isLeader=true 的语义下有意义）
     *
     * @param leaseHeld 当前是否持有执行权租约
     * @param reason    变化原因（可用于日志与诊断）
     */
    default void onLeaseStateChanged(boolean leaseHeld, String reason) {}
}

