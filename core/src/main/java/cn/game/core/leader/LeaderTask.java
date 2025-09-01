package cn.game.core.leader;

import cn.game.core.leader.LeaderLeaseManager.LeaderTaskContext;

/**
 * 业务方需实现的 Leader 任务接口
 *
 * <p>建议实现形态：</p>
 * <pre>
 *   public void run(LeaderTaskContext ctx) throws Exception {
 *       while (ctx.canExecute()) {
 *           ctx.ensureLeaseHealthy();
 *           // 执行小批次工作
 *       }
 *   }
 * </pre>
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
     * 任务主逻辑，建议按“批次”执行并在每批前调用 ensureLeaseHealthy()
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