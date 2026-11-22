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
     * @param args DAO调用参数（用于在跨服或异步工作线程上识别沙盒实体）
     * @return true表示应旁路直接返回空成功Future
     */
    public static boolean shouldBypassDaoMethod(String method, Object... args) {
        if (method == null) {
            return false;
        }
        String lower = method.toLowerCase();
        // 允许只读查询，阻断所有插入/更新/删除操作
        boolean isWriteMethod = !lower.startsWith("select") && !lower.startsWith("get") && !lower.startsWith("find") && !lower.startsWith("count");
        if (!isWriteMethod) {
            return false;
        }
        if (isSandboxThreadMuted()) {
            return true;
        }
        if (args != null) {
            for (Object arg : args) {
                if (isSandboxEntity(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 记录由沙盒玩家创建的跨服沙盒公会ID，确保即便公会改名或触发定时保存也能被 DAO 旁路识别 */
    private static final Set<Long> SANDBOX_GUILD_IDS = ConcurrentHashMap.newKeySet();

    private static boolean isSandboxEntity(Object arg) {
        if (arg == null) {
            return false;
        }
        if (arg instanceof Number num) {
            long val = num.longValue();
            return PlayerSaveCoordinator.isSandboxPlayerId(val) || SANDBOX_GUILD_IDS.contains(val);
        }
        if (arg instanceof PlayerData pd) {
            return PlayerSaveCoordinator.isSandboxPlayerId(pd.getPlayerId());
        }
        if (arg instanceof cn.game.games.cache.entity.GuildJoin gj) {
            if (PlayerSaveCoordinator.isSandboxPlayerId(gj.getPlayerId())) {
                if (gj.getGuildId() > 0) {
                    SANDBOX_GUILD_IDS.add(gj.getGuildId());
                }
                return true;
            }
            return false;
        }
        if (arg instanceof cn.game.games.cache.entity.GuildData gd) {
            if (gd.getId() > 0 && SANDBOX_GUILD_IDS.contains(gd.getId())) {
                return true;
            }
            if (gd.getName() != null && gd.getName().startsWith("沙盒")) {
                if (gd.getId() > 0) {
                    SANDBOX_GUILD_IDS.add(gd.getId());
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private static void registerLiveClusterRouting(Player player) {
        if (player == null) {
            return;
        }
        long pid = player.getPlayerId();
        try {
            String serverId = ServerContext.getInstance().getServerId();
            if (cn.game.core.net.vertx.VxHolder.vertx != null
                    && cn.game.util.RedisUtil.getRedis() != null
                    && serverId != null && !serverId.isBlank()) {
                String idKey = cn.game.core.cache.id.IdCache.generateRedisKey(cn.game.core.cache.id.DistributedObjectType.PLAYER, pid);
                // 强制覆盖刷新当前沙盒玩家所在 GameServer 节点路由，防止跨节点重启后 initServerId 因旧键存在而静默失败
                cn.game.util.RedisUtil.set(idKey, serverId);
                cn.game.core.cache.id.IdCache.initServerId(cn.game.core.cache.id.DistributedObjectType.PLAYER, pid);
                cn.game.games.core.SimplePlayer simplePlayer = new cn.game.games.core.SimplePlayer(player);
                cn.game.core.cache.RedisLocalCache.getInstance().put(cn.game.core.cache.CacheType.PLAYER_SIMPLE.key(pid), simplePlayer);
            }
        } catch (Throwable t) {
            log.warn("注册沙盒玩家跨服路由与SimplePlayer缓存警告: playerId={}", pid, t);
        }
    }

    /**
     * 清理沙盒玩家在真实集群 Redis 中的跨服路由与 SimplePlayer 缓存
     *
     * @param pid 沙盒玩家ID
     */
    public static void unregisterLiveClusterRouting(long pid) {
        try {
            if (cn.game.core.net.vertx.VxHolder.vertx != null
                    && cn.game.util.RedisUtil.getRedis() != null
                    && ServerContext.getInstance().getServerId() != null) {
                String redisKey = cn.game.core.cache.id.IdCache.generateRedisKey(cn.game.core.cache.id.DistributedObjectType.PLAYER, pid);
                cn.game.util.RedisUtil.delete(redisKey);
                String simpleKey = cn.game.core.cache.CacheType.PLAYER_SIMPLE.key(pid);
                cn.game.core.cache.RedisLocalCache.getInstance().deleteAsync(simpleKey);
                cn.game.util.RedisUtil.delete(simpleKey);
            }
        } catch (Throwable t) {
            log.warn("清理沙盒玩家跨服路由缓存失败: playerId={}", pid, t);
        }
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
            registerLiveClusterRouting(existing);
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
        String nowStr = DateUtil.getTimeByPattern(new java.util.Date(), DateUtil.pattern_en);
        data.setCreateDate(nowStr);
        data.setLoginDate(nowStr);

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
        registerLiveClusterRouting(player);

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
        unregisterLiveClusterRouting(pid);
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
