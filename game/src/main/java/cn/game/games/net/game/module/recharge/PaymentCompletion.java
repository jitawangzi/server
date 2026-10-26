package cn.game.games.net.game.module.recharge;

import java.util.function.Supplier;
import cn.game.core.util.AsyncUtils;
import cn.game.games.cache.entity.Player;
import io.vertx.core.Future;

/**
 * 现金支付将奖励执行与保存确认分开：奖励在发货邮箱同步等待，购买结果只在保存确认后发布。
 * 奖励 Future 必须代表业务完成，且不能依赖同一邮箱的后继任务；异常不提供自动回滚或重放。
 */
public final class PaymentCompletion<T> {
    /** 在发货任务内执行的完整奖励步骤。 */
    private final Supplier<Future<T>> reward;
    /** 已执行结果只暂存于内存，不作为跨重启的恢复凭据。 */
    private T value;
    /** 防止同一绑定被多次执行。 */
    private boolean executed;

    /** @param reward 完整奖励步骤，不能包含最终购买成功响应 */
    private PaymentCompletion(Supplier<Future<T>> reward) { this.reward = reward; }

    /** @param player 所属玩家 @param payment 支付结果 @param reward 同步业务奖励 @return 保存确认后的奖励结果 */
    public static <T> Future<T> reward(Player player, Future<Boolean> payment, Supplier<T> reward) {
        return async(player, payment, () -> AsyncUtils.fromSync(() -> Future.succeededFuture(reward.get())));
    }

    /** @param player 所属玩家 @param payment 支付结果 @param reward 同步奖励 @param declined 原入口支付拒绝错误码
     * @return 保存确认后的结果；只转换明确拒绝，保留奖励异常的原始原因 */
    public static <T> Future<T> reward(Player player, Future<Boolean> payment, Supplier<T> reward,
            cn.game.protocol.manual.ErrorMsgEnum declined) {
        return reward(player, payment, reward).recover(failure -> Future.failedFuture(
                "PAYMENT_NOT_SUCCESSFUL".equals(failure.getMessage())
                ? new cn.game.core.exception.LogicException(declined) : failure));
    }

    /** @param player 所属玩家 @param payment 支付结果 @param reward 完整异步奖励 @return 保存确认后的奖励结果 */
    public static <T> Future<T> async(Player player, Future<Boolean> payment, Supplier<Future<T>> reward) {
        if (payment.isComplete()) {
            return payment.compose(paid -> Boolean.TRUE.equals(paid) ? AsyncUtils.fromSync(reward)
                    : Future.failedFuture("PAYMENT_NOT_SUCCESSFUL"));
        }
        PaymentCompletion<T> completion = new PaymentCompletion<>(reward);
        player.getPlayerModule().registerPayReward(payment, completion);
        return payment.map(paid -> {
            if (!Boolean.TRUE.equals(paid)) { throw new IllegalStateException("PAYMENT_NOT_SUCCESSFUL"); }
            if (!completion.executed) {
                throw new IllegalStateException("PAYMENT_REWARD_NOT_CONFIRMED");
            }
            return completion.value;
        });
    }

    /** 只在所属发货邮箱调用；同步异常和异步失败均传回发货入口，不完成购买响应。 */
    public void execute() {
        if (executed) { throw new IllegalStateException("PAYMENT_REWARD_ALREADY_EXECUTED"); }
        executed = true;
        Future<T> pending = reward.get();
        if (pending == null) { throw new IllegalStateException("PAYMENT_REWARD_FUTURE_MISSING"); }
        value = AsyncUtils.await(pending);
    }
}
