package cn.game.games.net.game.helper;

import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.util.JsonUtil;

/**
 * 封存一次玩家聚合表写入的全部 Mapper 参数，避免异步读取混入后续在线状态。
 * 当前映射值仅含基本类型和 String；模块图通过完整带类型 JSON 隔离。
 * 新增映射字段或可变类型须同步复制策略及 Mapper 覆盖测试，不负责跨次写入保序。
 */
final class PlayerDataSnapshot {
    /** 只随本次 Mapper 参数传递观测标量，不改变 PlayerData 持久字段或模块 JSON 格式。 */
    private static final class MeasuredData extends PlayerData {
        /** 封存时读取；回调线程不再访问在线模块。 */
        @com.fasterxml.jackson.annotation.JsonIgnore
        private PaymentCapacityMonitor.Sample capacity;
    }
    /** 工具类不创建实例。 */
    private PlayerDataSnapshot() {}

    /**
     * 在玩家所属串行执行上下文捕获参数；调用期间不得并发修改在线数据或模块。
     * 成功捕获后同步释放在线对象的旧 JSON，返回对象仅交给本次 DAO，提交后不得改写。
     * @param player 模块已初始化、当前由调用方持有执行权的玩家
     * @return 与在线对象分离的 Mapper 参数，不表示数据库已提交
     */
    static PlayerData capture(Player player) {
        PlayerData source = player.getData();
        source.beforeSave();
        String modules = JsonUtil.toJsonStringWithType(player.getModules());

        // 显式覆盖 insert 与 updateByPrimaryKey 实际读取的字段，不克隆整个玩家对象。
        MeasuredData snapshot = new MeasuredData();
        snapshot.setPlayerId(source.getPlayerId());
        snapshot.setUid(source.getUid());
        snapshot.setChannelId(source.getChannelId());
        snapshot.setDeviceId(source.getDeviceId());
        snapshot.setAccountId(source.getAccountId());
        snapshot.setIsGm(source.getIsGm());
        snapshot.setServerId(source.getServerId());
        snapshot.setGender(source.getGender());
        snapshot.setName(source.getName());
        snapshot.setRegion(source.getRegion());
        snapshot.setExp(source.getExp());
        snapshot.setLevel(source.getLevel());
        snapshot.setVipExpTotal(source.getVipExpTotal());
        snapshot.setVipLevel(source.getVipLevel());
        snapshot.setFightPower(source.getFightPower());
        snapshot.setHead(source.getHead());
        snapshot.setHeadFrame(source.getHeadFrame());
        snapshot.setImage(source.getImage());
        snapshot.setCreateDate(source.getCreateDate());
        snapshot.setLoginDate(source.getLoginDate());
        snapshot.setOfflineTime(source.getOfflineTime());
        snapshot.setRefreshMonth(source.getRefreshMonth());
        snapshot.setRefreshWeek(source.getRefreshWeek());
        snapshot.setRefreshDay(source.getRefreshDay());
        snapshot.setRefreshFiveDay(source.getRefreshFiveDay());
        snapshot.setTitle(source.getTitle());
        snapshot.setUnionId(source.getUnionId());
        snapshot.setUnionName(source.getUnionName());
        snapshot.setCombat(source.getCombat());
        snapshot.setDay(source.getDay());
        snapshot.setGameTime(source.getGameTime());
        snapshot.setModules(modules);

        // 复用唯一序列化结果，只在保存边界读 HashMap.size，不在支付请求上全扫订单。
        try {
            var module = player.getPlayerModule();
            if (module != null) {
                snapshot.capacity = new PaymentCapacityMonitor.Sample(source.getPlayerId(), module.retainedPaymentOrderCount(),
                        PaymentCapacityMonitor.utf8Bytes(modules));
            }
        } catch (Throwable failure) {
            PaymentCapacityMonitor.reportFailure(failure);
        }

        // JSON 已由本次参数独占；异步完成或超时后都不能再清理任何提交对象。
        source.setModules("{}");
        return snapshot;
    }

    /** @param data 原封存参数 @return 捕获标量；手工或非聚合保存没有容量观测 */
    static PaymentCapacityMonitor.Sample capacity(PlayerData data) {
        return data instanceof MeasuredData measured ? measured.capacity : null;
    }
}
