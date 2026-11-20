package cn.game.games.net.game.gm.acceptance;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.game.gm.proto.VirtualGameClient;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.account.Account;
import cn.game.util.DateUtil;
import cn.game.util.ObjUtil;

/**
 * 沙盒执行守卫与隔离核心门禁 (DC-01, DC-02, DC-03)
 * 统一管理自动化验收沙盒玩家生命周期、副作用静默标记与条件破障开关。
 *
 * @author shuyongqiang
 */
public class SandboxExecutionGuard {

    private static final Logger log = LoggerFactory.getLogger(SandboxExecutionGuard.class);

    /** 线程级副作用静默上下文，用于阻断全局跑马灯广播、排行榜Redis写入与DAO分表写入 */
    private static final ThreadLocal<Boolean> SANDBOX_MUTED_SCOPE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** 处于条件豁免破障状态的沙盒玩家集合 (仅用于 MATRIX_SCAN 模式) */
    private static final Set<Long> CONDITION_BYPASS_PLAYERS = ConcurrentHashMap.newKeySet();

    /**
     * 判断当前线程是否处于沙盒副作用静默作用域
     *
     * @return true表示处于静默状态
     */
    public static boolean isSandboxThreadMuted() {
        return Boolean.TRUE.equals(SANDBOX_MUTED_SCOPE.get());
    }

    /**
     * 设置当前线程的沙盒副作用静默状态
     *
     * @param muted true开启静默，false恢复常规
     */
    public static void setSandboxThreadMuted(boolean muted) {
        if (muted) {
            SANDBOX_MUTED_SCOPE.set(Boolean.TRUE);
        } else {
            SANDBOX_MUTED_SCOPE.remove();
        }
    }

    /**
     * 判断指定玩家当前是否开启了全量配表扫描条件豁免破障
     *
     * @param playerId 玩家ID
     * @return true表示开启破障
     */
    public static boolean isConditionBypassEnabled(long playerId) {
        return PlayerSaveCoordinator.isSandboxPlayerId(playerId) && CONDITION_BYPASS_PLAYERS.contains(playerId);
    }

    /**
     * 开启或关闭指定玩家的配表扫描条件豁免破障
     *
     * @param playerId 玩家ID
     * @param enable true开启，false关闭
     */
    public static void enableConditionBypass(long playerId, boolean enable) {
        if (enable) {
            CONDITION_BYPASS_PLAYERS.add(playerId);
        } else {
            CONDITION_BYPASS_PLAYERS.remove(playerId);
        }
    }

    /**
     * 检查当前DAO写操作方法是否应该被静默旁路
     *
     * @param method 方法名
     * @return true表示应旁路直接返回空成功Future
     */
    public static boolean shouldBypassDaoMethod(String method) {
        if (!isSandboxThreadMuted() || method == null) {
            return false;
        }
        String lower = method.toLowerCase();
        // 允许只读查询，阻断所有插入/更新/删除操作
        return !lower.startsWith("select") && !lower.startsWith("get") && !lower.startsWith("find") && !lower.startsWith("count");
    }

    /**
     * 获取或创建专用的沙盒免落盘测试玩家（默认模块号为0）
     *
     * @param preferredPlayerId 期望的玩家ID
     * @return 沙盒角色
     */
    public static Player acquireSandboxPlayer(long preferredPlayerId) {
        return acquireSandboxPlayer(preferredPlayerId, 0);
    }

    /**
     * 获取或创建专用的沙盒免落盘测试玩家
     *
     * @param preferredPlayerId 期望的玩家ID，若小于沙盒起始号段则自动分配
     * @param moduleId 当前执行的模块号
     * @return 已完全初始化、挂载虚拟客户端并注册在内存管理器中的沙盒角色
     */
    public static Player acquireSandboxPlayer(long preferredPlayerId, int moduleId) {
        if (cn.game.games.net.game.gm.GmSecurityPolicy.currentRunMode().isProduction()) {
            throw new cn.game.core.exception.LogicException(cn.game.protocol.manual.ErrorMsgEnum.production_gm_not_allow.getId(), "生产环境严禁创建或获取沙盒测试角色");
        }
        long effectiveId = preferredPlayerId >= PlayerSaveCoordinator.SANDBOX_PLAYER_ID_MIN
                ? preferredPlayerId
                : PlayerSaveCoordinator.SANDBOX_PLAYER_ID_MIN + Math.max(1L, (long) moduleId);

        Player existing = PlayerManager.getInstance().getPlayer(effectiveId);
        if (existing != null) {
            existing.setOnline(true);
            existing.setIslogouting(false);
            if (existing.getGameClient() == null || !existing.getGameClient().isActive()) {
                existing.setGameClient(new VirtualGameClient(effectiveId));
            }
            cn.game.games.net.game.manager.GameClientManager.getInstance().addGameClientPlayer(existing.getGameClient());
            if (existing.getAccount() == null) {
                Account defaultAccount = new Account();
                defaultAccount.sdkPayChannel = "0010";
                defaultAccount.platform = 1;
                defaultAccount.version = "";
                defaultAccount.openid = "sandbox_openid_" + effectiveId;
                existing.setAccount(defaultAccount);
            }
            return existing;
        }

        PlayerData data = new PlayerData();
        ObjUtil.setDefaultValue(data);
        data.setPlayerId(effectiveId);
        data.setUid(effectiveId);
        data.setAccountId("sandbox_" + effectiveId);
        data.setName("Sandbox_" + moduleId);
        String serverId = ServerContext.getInstance().getServerId();
        data.setServerId(serverId != null ? serverId : "1");
        data.setLevel(100);
        data.setVipLevel(14);
        data.setRefreshDay(DateUtil.getDay());

        Player player = new Player(data);
        Account account = new Account();
        account.sdkPayChannel = "0010";
        account.platform = 1;
        account.version = "";
        account.openid = "sandbox_openid_" + effectiveId;
        player.setAccount(account);

        try {
            player.initPlayerModule();
        } catch (Throwable t) {
            log.warn("沙盒角色初始化模块警告: playerId={}", effectiveId, t);
        }

        player.setOnline(true);
        player.setIslogining(false);
        player.setIslogouting(false);
        VirtualGameClient vClient = new VirtualGameClient(effectiveId);
        player.setGameClient(vClient);
        cn.game.games.net.game.manager.GameClientManager.getInstance().addGameClientPlayer(vClient);

        PlayerManager.getInstance().initAdd(player);

        ConditionSatisfier.satisfyAllFunctionsAndBaseLevel(player, 100, 14, 100);

        return player;
    }

    /**
     * 释放并销毁沙盒玩家内存对象
     *
     * @param player 待清理的沙盒角色
     */
    public static void releaseSandboxPlayer(Player player) {
        if (player == null) {
            return;
        }
        long pid = player.getPlayerId();
        enableConditionBypass(pid, false);
        try {
            if (player.getGameClient() != null) {
                cn.game.games.net.game.manager.GameClientManager.getInstance().removeGameClient(player.getGameClient(), cn.game.core.net.client.LogoutType.ServerClose);
            }
        } catch (Throwable t) {
            log.warn("从客户端管理器删除沙盒连接失败: playerId={}", pid, t);
        }
        try {
            player.cancelAllTimer();
        } catch (Throwable t) {
            log.warn("取消沙盒角色定时器失败: playerId={}", pid, t);
        }
        try {
            PlayerManager.getInstance().deletePlayer(player);
        } catch (Throwable t) {
            log.warn("从玩家管理器删除沙盒角色失败: playerId={}", pid, t);
        }
    }
}
