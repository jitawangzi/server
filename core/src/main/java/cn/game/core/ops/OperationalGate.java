package cn.game.core.ops;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import cn.game.core.leader.LeaderStateListener;
import cn.game.core.health.HealthGuard; // 仅说明：业务在装配层把 HealthGuard 的状态变化映射到本门面；如你已有监听可直接调用 updatePhase(...)
import cn.game.core.health.SelfDestructGuardConfig; // 此引用非必须，仅为说明可根据配置自定义策略

/**
 * OperationalGate
 *
 * 面向“被动触发”的业务入口门面：
 * - 由业务在装配时注册为 LeaderStateListener（领导权/租约）；
 * - 同时由业务在 HealthGuard 的 tick 状态变化处调用 updatePhase(...)（或通过你已有的健康监听器桥接）；
 * - 触发型任务在真正执行前调用 allowExecute()/reason() 决策是否执行。
 *
 * 注意：这里不直接依赖 HealthGuard 的类与监听器实现，避免硬耦合；业务装配层负责把健康状态映射到 setProtect()/setSevere()/setNormal()。
 */
public class OperationalGate implements LeaderStateListener {

    public enum RunPhase {
        NORMAL,     // 允许执行
        PROTECT,    // 降级/拒绝新触发（外部依赖不稳）
        SEVERE      // 严重，待自毁
    }

    private final AtomicReference<RunPhase> phase = new AtomicReference<>(RunPhase.NORMAL);
    private final AtomicBoolean leaderAndLease = new AtomicBoolean(false);
    private final AtomicReference<String> reasonRef = new AtomicReference<>("init");

    // ========== 对外：由健康监测装配层调用的状态切换 ==========
    public void setNormal(String reason) {
        phase.set(RunPhase.NORMAL);
        reasonRef.set("health:NORMAL(" + reason + ")");
    }

    public void setProtect(String reason) {
        phase.set(RunPhase.PROTECT);
        reasonRef.set("health:PROTECT(" + reason + ")");
    }

    public void setSevere(String reason) {
        phase.set(RunPhase.SEVERE);
        reasonRef.set("health:SEVERE(" + reason + ")");
    }

    // ========== LeaderStateListener 实现 ==========
    @Override
    public void onLeaderChanged(boolean isLeader) {
        // 仅记录领导权变化；最终 gate 由“领导权 + 租约”共同决定
        // 这里不直接设置 leaderAndLease，由租约回调统一覆盖，避免竞态
        reasonRef.set("leaderChanged:" + isLeader);
        if (!isLeader) {
            leaderAndLease.set(false);
        }
    }

    @Override
    public void onLeaseStateChanged(boolean leaseHeld, String reason) {
        leaderAndLease.set(leaseHeld);
        reasonRef.set("lease:" + leaseHeld + "(" + reason + ")");
    }

    // ========== 被动触发入口检查 ==========
    /**
     * 是否允许执行触发型任务：要求处于 NORMAL 且持有“领导权+租约”
     */
    public boolean allowExecute() {
        return phase.get() == RunPhase.NORMAL && leaderAndLease.get();
    }

    /**
     * 当前不允许执行的原因（用于日志/指标）
     */
    public String reason() {
        return reasonRef.get();
    }

    /**
     * 当前运行阶段（NORMAL/PROTECT/SEVERE），便于业务做更细粒度的降级。
     */
    public RunPhase currentPhase() {
        return phase.get();
    }
}

