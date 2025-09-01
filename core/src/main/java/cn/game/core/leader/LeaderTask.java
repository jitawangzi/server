package cn.game.core.leader;

import cn.game.core.leader.LeaderLeaseManager.LeaderTaskContext;

/**
 * 业务方需实现的 Leader 任务接口
 *
 * 本版本兼容两种用法：
 * 1) 事件驱动（推荐）：业务侧通过 OperationalGate + 监听器控制是否执行触发型任务；
 *    此时可以不实现一个“长循环”的 run，而是让 run 内部按你的线程模型自行管理。
 * 2) 批循环（旧用法）：如果你仍希望采用批循环，可在 run 内部 while(ctx.canExecute()) 并在批前 ensureLeaseHealthy()。
 */
public interface LeaderTask {
    /**
     * 任务名（全局唯一）
     *
     * @return 任务名
     */
    String name();

    /**
     * 任务启动前回调（已满足 canExecute）
     *
     * @param ctx 上下文
     */
    default void onStart(LeaderTaskContext ctx) {}

    /**
     * 任务主逻辑：
     * - 事件驱动模型：可在此启动你自己的工作线程、注册触发入口，内部自行使用 OperationalGate 判断是否允许执行；
     * - 批循环模型：可 while (ctx.canExecute()) { ctx.ensureLeaseHealthy(); ... }。
     *
     * @param ctx 上下文
     * @throws Exception 任意异常将被记录并结束任务
     */
    void run(LeaderTaskContext ctx) throws Exception;

    /**
     * 暂停回调（SUSPENDED）
     *
     * @param reason 原因
     */
    default void onPause(String reason) {}

    /**
     * 停止回调（notLeader/LOST/续约失败等）
     *
     * @param reason 原因
     */
    default void onStop(String reason) {}
}

