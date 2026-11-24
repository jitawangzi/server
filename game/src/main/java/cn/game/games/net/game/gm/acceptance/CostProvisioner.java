package cn.game.games.net.game.gm.acceptance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.shop.ShopHelper;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.manual.OpType;

/**
 * 模块验收通用消耗与代金券造数补齐器 (DC-04, BR-03)
 * 自动识别货币/材料二元组、二维矩阵及商店三元组，为RMB直购自动注入足额代金券走同步抵扣。
 *
 * @author shuyongqiang
 */
public class CostProvisioner {

    private static final Logger log = LoggerFactory.getLogger(CostProvisioner.class);

    /**
     * 解析并补齐扁平消耗数组 {id, count, id2, count2, ...}
     *
     * @param player 目标玩家
     * @param cost 消耗数组
     * @param multiplier 数量倍数
     */
    public static void provisionCostArray(Player player, int[] cost, int multiplier) {
        if (player == null || cost == null || cost.length < 2) {
            return;
        }
        int effectiveMultiplier = Math.max(1, multiplier);
        for (int i = 0; i + 1 < cost.length; i += 2) {
            int resourceId = cost[i];
            int amount = cost[i + 1];
            if (resourceId > 0 && amount > 0) {
                try {
                    PlayerHelper.addResources(player, resourceId, (int) Math.min(Integer.MAX_VALUE, (long) amount * effectiveMultiplier), OpType.GM, false);
                } catch (Throwable t) {
                    log.warn("补齐资源消耗异常: playerId={} id={} count={}", player.getPlayerId(), resourceId, amount, t);
                    throw new AcceptanceFixtureException("造数补齐资源失败: resourceId=" + resourceId + " count=" + amount, t, true);
                }
            }
        }
    }

    /**
     * 解析并补齐二维消耗矩阵 {{id, count}, {id2, count2}}
     *
     * @param player 目标玩家
     * @param costs 二维消耗矩阵
     * @param multiplier 数量倍数
     */
    public static void provisionCostMatrix(Player player, int[][] costs, int multiplier) {
        if (player == null || costs == null || costs.length == 0) {
            return;
        }
        for (int[] cost : costs) {
            provisionCostArray(player, cost, multiplier);
        }
    }

    /**
     * 专门适配商店购买三元组 {costType, param1, param2}
     *
     * @param player 目标玩家
     * @param purchaseParameter 购买参数
     * @param count 购买次数
     */
    public static void provisionShopPayment(Player player, int[] purchaseParameter, int count) {
        if (player == null || purchaseParameter == null || purchaseParameter.length == 0 || purchaseParameter[0] == 0) {
            return;
        }
        int effectiveCount = Math.max(1, count);
        int costType = purchaseParameter[0];

        try {
            if (costType == ShopHelper.COST_TYPE_RESOURCE && purchaseParameter.length >= 3) {
                int resourceId = purchaseParameter[1];
                int price = purchaseParameter[2];
                if (resourceId > 0 && price > 0) {
                    PlayerHelper.addResources(player, resourceId, (int) Math.min(Integer.MAX_VALUE, (long) price * effectiveCount), OpType.GM, false);
                }
            } else if (costType == ShopHelper.COST_TYPE_RECHARGE && purchaseParameter.length >= 2) {
                int rmbCost = purchaseParameter[1];
                if (rmbCost > 0) {
                    // 注入足额代金券 (Asset.Vouchers ID = 100008)，触发 Player#pay 同步代金券抵扣
                    PlayerHelper.addResources(player, Asset.Vouchers.ID, (int) Math.min(Integer.MAX_VALUE, (long) rmbCost * effectiveCount), OpType.GM, false);
                }
            } else if (costType == ShopHelper.COST_TYPE_ADVERTISE) {
                // 广告类型支付无需额外补货币
            }
        } catch (Throwable t) {
            log.warn("商店支付造数失败: playerId={} costType={}", player.getPlayerId(), costType, t);
            throw new AcceptanceFixtureException("商店支付造数失败: costType=" + costType, t, true);
        }
    }
}
