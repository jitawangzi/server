package cn.game.games.net.game.helper;

import cn.game.games.cache.entity.Player;

/** 离线加载已登记实例后的初始化失败，携带原实例身份，避免按玩家 ID 隔离后继登录对象。 */
public final class OfflinePlayerInitializationFailure extends IllegalStateException {
    /** 本次实际登记但未完成初始化的对象，不代表失败时 Manager 中的任意同 ID 对象。 */
    private final Player player;

    /** @param player 已登记的原实例 @param cause 同步初始化或异步加载的原始异常 */
    public OfflinePlayerInitializationFailure(Player player, Throwable cause) {
        super("OFFLINE_PLAYER_INITIALIZATION_FAILED playerId=" + player.getPlayerId(), cause);
        this.player = player;
    }

    /** @return 只能按对象身份核对和隔离的原实例 */
    public Player player() { return player; }
}
