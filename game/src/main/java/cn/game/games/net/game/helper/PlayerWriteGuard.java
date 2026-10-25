package cn.game.games.net.game.helper;

import java.util.function.Supplier;
import cn.game.games.cache.entity.Player;
import cn.game.core.base.ServerContext;
import io.vertx.core.Future;

/** 玩家聚合表的保守写作用域；异步回调必须在实际写入时重新进入玩家邮箱。 */
public final class PlayerWriteGuard {
    private PlayerWriteGuard() {}
    /** 可用于 try-with-resources 的同步范围，不跨外部 Future 生命周期。 */
    public record Scope(PlayerSaveCoordinator.Mutation mutation) implements AutoCloseable {
        @Override
        public void close() { if (mutation != null) { mutation.close(); } }
    }
    /** 独立表模式沿用原机制，不把主表确认冒充独立表已持久化。 */
    public static Scope begin(Player player) {
        return new Scope(ServerContext.getInstance().isSinglePlayerTable()
                ? PlayerSaveCoordinator.shared().beginMutation(player.getPlayerId(), player) : null);
    }
    /** 在已有玩家任务内执行同步变化，异常也关闭作用域。 */
    public static <T> T call(Player player, Supplier<T> action) {
        try (Scope ignored = begin(player)) { return action.get(); }
    }
    /** 同步无返回值业务入口。 */
    public static void run(Player player, Runnable action) { call(player, () -> { action.run(); return null; }); }
    /** 迟到回调重新排队，在实际执行时核对原对象身份与风险预算。 */
    public static Future<Void> submit(Player player, Runnable action) {
        return submit(player, action, ServerContext.getInstance().getProcessor(), () -> begin(player));
    }
    /** 生产提交路径的依赖接缝，用真实邮箱测试排队后的门禁。 */
    static Future<Void> submit(Player player, Runnable action, cn.game.core.net.process.Processor processor, Supplier<Scope> scope) {
        return processor.processAsync(player.getPlayerId(), () -> { try (Scope ignored = scope.get()) { action.run(); } }, false);
    }
    /** 一次性timer只有开始前保护拒绝可延期；执行后的异常不自动重放。 */
    public static void runTimer(Player player, Runnable action) {
        if (ServerContext.getInstance().isSinglePlayerTable()) {
            PlayerSaveCoordinator.shared().runOrDeferTimer(player.getPlayerId(), player, action);
        } else { action.run(); }
    }
    /** 带结果的迟到回调；调用者应传播返回 Future 的失败。 */
    public static <T> Future<T> submitValue(Player player, Supplier<T> action) {
        return ServerContext.getInstance().getProcessor().process(player.getPlayerId(), () -> call(player, action), null);
    }
}
