package cn.game.games.net.game.gm.acceptance.suites;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.ShopItem;
import cn.game.games.net.game.gm.acceptance.AcceptanceContext;
import cn.game.games.net.game.gm.acceptance.CostProvisioner;
import cn.game.games.net.game.gm.acceptance.ConditionSatisfier;
import cn.game.games.net.game.gm.acceptance.InProcessAcceptanceKit;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.SandboxResetter;
import cn.game.games.net.game.module.shop.ShopHandler;
import cn.game.protocol.generated.config.ChapterPacksConfig;
import cn.game.protocol.generated.config.FundPassConfig;
import cn.game.protocol.generated.config.MonthCardConfig;
import cn.game.protocol.generated.config.RechargeConfig;
import cn.game.protocol.generated.config.ShopConfig;
import cn.game.protocol.generated.config.ShopItemConfig;
import cn.game.protocol.generated.manager.ChapterPacksManager;
import cn.game.protocol.generated.manager.FundPassManager;
import cn.game.protocol.generated.manager.MonthCardManager;
import cn.game.protocol.generated.manager.RechargeManager;
import cn.game.protocol.generated.manager.ShopItemManager;
import cn.game.protocol.generated.manager.ShopManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;

/**
 * 商店模块双轨自动化验收套件 (0x15)。
 * 覆盖全服商店购买契约、配表商品全集扫描、月卡、章节礼包与直购充值档位。
 *
 * @author shuyongqiang
 */
public class ShopModuleAcceptanceSuite implements ModuleAcceptanceSuite {

    private static final Logger log = LoggerFactory.getLogger(ShopModuleAcceptanceSuite.class);

    public static final int MODULE_ID = 0x15;
    public static final String MODULE_KEY = "shop";
    public static final String TITLE = "全服商店、黑市、月卡与礼包";

    private static final List<String> DECLARED_BRANCHES = Collections.unmodifiableList(Arrays.asList(
            "SHOP_ITEM_FREE",
            "SHOP_ITEM_RESOURCE_PAY",
            "SHOP_ITEM_RMB_VOUCHER_PAY",
            "SHOP_ITEM_AD_PAY",
            "MONTH_CARD_BUY",
            "CHAPTER_PACK_BUY",
            "RECHARGE_BUY"
    ));

    private static final List<String> RELATED_TABLES = Collections.unmodifiableList(Arrays.asList(
            "ShopItemConfig",
            "ShopConfig",
            "MonthCardConfig",
            "ChapterPacksConfig",
            "RechargeConfig"
    ));

    @Override
    public int getModuleId() {
        return MODULE_ID;
    }

    @Override
    public String getModuleKey() {
        return MODULE_KEY;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public List<String> getDeclaredBranches() {
        return DECLARED_BRANCHES;
    }

    @Override
    public List<String> getRelatedTables() {
        return RELATED_TABLES;
    }

    @Override
    public void runContractCases(AcceptanceContext ctx) throws Exception {
        InProcessAcceptanceKit.ensureHandlerRegistered(MODULE_ID, ShopHandler::new);
        Player player = ctx.getSandboxPlayer();
        ConditionSatisfier.satisfyAllFunctionsAndBaseLevel(player, 100, 14, 100);

        // 1. 契约用例：商店列表查询
        ctx.executeContractCase("CONTRACT:shop_item_list_query", "查询商店配置商品列表返回成功", () -> {
            Map<String, Object> req = Map.of("shopId", 1);
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopItemListRequest_15000001, req);
            InProcessAcceptanceKit.assertProtocolSuccess(res);
        });

        // 2. 契约用例：购买不存在商品拦截
        ctx.executeContractCase("CONTRACT:shop_buy_item_not_exist", "购买不存在的商品ID拦截", () -> {
            Map<String, Object> req = Map.of("shopId", 1, "itemId", 99999999, "count", 1);
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopItemBuyRequest_15000003, req);
            InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.shop_item_not_exist.getId());
        });

        // 3. 契约用例：货币不足拦截
        ctx.executeContractCase("CONTRACT:shop_buy_resource_not_enough", "货币不足时购买商品拦截", () -> {
            ShopItemConfig target = null;
            if (ShopItemManager.instance() != null) {
                for (ShopItemConfig cfg : ShopItemManager.instance().list()) {
                    if (cfg.PurchaseParameter != null && cfg.PurchaseParameter.length >= 3 && cfg.PurchaseParameter[0] == 1) {
                        target = cfg;
                        break;
                    }
                }
            }
            if (target != null) {
                SandboxResetter.resetModules(player, "shop", "currency");
                List<ShopItem> items = player.getShopModule().getShopItems(1);
                items.clear();
                items.add(new ShopItem(target.ID));

                Map<String, Object> req = Map.of("shopId", 1, "itemId", target.ID, "count", 1);
                Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopItemBuyRequest_15000003, req);
                InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.resource_not_enough.getId());
            }
        });

        // 4. 契约用例：限购上限拦截与重置购买
        ctx.executeContractCase("CONTRACT:shop_buy_quota_limit_and_reset", "限购满额拦截与重置后正常购买", () -> {
            ShopItemConfig target = null;
            if (ShopItemManager.instance() != null) {
                for (ShopItemConfig cfg : ShopItemManager.instance().list()) {
                    if (cfg.ShopItemQuota > 0) {
                        target = cfg;
                        break;
                    }
                }
            }
            if (target != null) {
                SandboxResetter.resetModules(player, "shop", "currency");
                List<ShopItem> items = player.getShopModule().getShopItems(1);
                items.clear();
                ShopItem si = new ShopItem(target.ID);
                si.setItemBuyTimes(target.ShopItemQuota);
                items.add(si);

                CostProvisioner.provisionShopPayment(player, target.PurchaseParameter, 1);
                Map<String, Object> req = Map.of("shopId", 1, "itemId", target.ID, "count", 1);
                Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopItemBuyRequest_15000003, req);
                InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.shop_item_buy_count_max.getId());

                // 重置限购次数后再次购买
                si.setItemBuyTimes(0);
                CostProvisioner.provisionShopPayment(player, target.PurchaseParameter, 1);
                Map<String, Object> res2 = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopItemBuyRequest_15000003, req);
                InProcessAcceptanceKit.assertProtocolSuccess(res2);
                if (si.getItemBuyTimes() != 1) {
                    throw new AssertionError("重置后购买次数未正常递增: " + si.getItemBuyTimes());
                }
            }
        });

        // 5. 契约用例：黑市刷新正常执行与满额拦截
        ctx.executeContractCase("CONTRACT:shop_heishi_refresh_limit", "黑市刷新正常执行与满额拦截", () -> {
            ShopConfig target = null;
            if (ShopManager.instance() != null) {
                for (ShopConfig sc : ShopManager.instance().list()) {
                    if (sc.Type == 2) {
                        target = sc;
                        break;
                    }
                }
            }
            if (target != null) {
                SandboxResetter.resetModules(player, "shop");
                Map<String, Object> req = Map.of("shopId", target.ID);
                Map<String, Object> res1 = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopHeishiRefreshRequest_15000005, req);
                InProcessAcceptanceKit.assertProtocolSuccess(res1);

                player.getShopModule().getHeishiRefreshTimesMap().setValue(target.ID, 9999);
                Map<String, Object> res2 = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopHeishiRefreshRequest_15000005, req);
                InProcessAcceptanceKit.assertErrorCode(res2, ErrorMsgEnum.times_limit.getId());
            }
        });

        // 6. 契约用例：月卡购买与重复购买防护
        ctx.executeContractCase("CONTRACT:month_card_buy_and_repeat_guard", "月卡购买与重复购买防刷拦截", () -> {
            MonthCardConfig target = null;
            if (MonthCardManager.instance() != null && !MonthCardManager.instance().list().isEmpty()) {
                target = MonthCardManager.instance().list().iterator().next();
            }
            if (target != null) {
                SandboxResetter.resetModules(player, "monthCard", "currency");
                CostProvisioner.provisionShopPayment(player, target.Price, 1);

                Map<String, Object> req = Map.of("id", target.ID);
                Map<String, Object> res1 = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.MonthCardBuyRequest_15000010, req);
                InProcessAcceptanceKit.assertProtocolSuccess(res1);

                CostProvisioner.provisionShopPayment(player, target.Price, 1);
                Map<String, Object> res2 = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.MonthCardBuyRequest_15000010, req);
                InProcessAcceptanceKit.assertErrorCode(res2, ErrorMsgEnum.month_card_repeated.getId());
            }
        });

        // 6. 契约用例：章节礼包购买与重复购买防护
        ctx.executeContractCase("CONTRACT:chapter_pack_buy_and_repeat_guard", "章节礼包购买与重复购买防刷拦截", () -> {
            ChapterPacksConfig target = null;
            if (ChapterPacksManager.instance() != null && !ChapterPacksManager.instance().list().isEmpty()) {
                target = ChapterPacksManager.instance().list().iterator().next();
            }
            if (target != null) {
                ConditionSatisfier.satisfyConditions(player, target.Condition);
                CostProvisioner.provisionShopPayment(player, target.PurchaseParameter, 1);

                Map<String, Object> req = Map.of("id", target.ID);
                Map<String, Object> res1 = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopChapterPacksBuyRequest_15000020, req);
                InProcessAcceptanceKit.assertProtocolSuccess(res1);

                CostProvisioner.provisionShopPayment(player, target.PurchaseParameter, 1);
                Map<String, Object> res2 = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopChapterPacksBuyRequest_15000020, req);
                InProcessAcceptanceKit.assertErrorCode(res2, ErrorMsgEnum.shop_gift_repeated.getId());
            }
        });

        // 7. 契约用例：通行证购买与重复购买防护
        ctx.executeContractCase("CONTRACT:fund_pass_buy_and_repeat_guard", "通行证购买与重复购买防刷拦截", () -> {
            FundPassConfig target = null;
            if (FundPassManager.instance() != null) {
                for (FundPassConfig cfg : FundPassManager.instance().list()) {
                    if (cfg.Price != null && cfg.Price.length > 0) {
                        target = cfg;
                        break;
                    }
                }
            }
            if (target != null) {
                SandboxResetter.resetModules(player, "shop", "currency");
                ConditionSatisfier.satisfyAllFunctionsAndBaseLevel(player, 100, 15, 100);
                CostProvisioner.provisionShopPayment(player, target.Price, 1);

                Map<String, Object> req = Map.of("id", target.ID);
                Map<String, Object> res1 = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopFundPassBuyRequest_15000030, req);
                InProcessAcceptanceKit.assertProtocolSuccess(res1);

                Map<String, Object> res2 = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopFundPassBuyRequest_15000030, req);
                InProcessAcceptanceKit.assertErrorCode(res2, ErrorMsgEnum.repeat_request.getId());
            }
        });
    }

    @Override
    public void runMatrixScan(AcceptanceContext ctx) throws Exception {
        InProcessAcceptanceKit.ensureHandlerRegistered(MODULE_ID, ShopHandler::new);
        Player player = ctx.getSandboxPlayer();

        // 1. 全量商品 ShopItemConfig 扫描
        if (ShopItemManager.instance() != null) {
            Collection<ShopItemConfig> allShopItems = ShopItemManager.instance().list();
            for (ShopItemConfig config : allShopItems) {
                if (ctx.isCircuitBroken()) {
                    break;
                }

                String branchKey = "SHOP_ITEM_RESOURCE_PAY";
                if (config.PurchaseParameter == null || config.PurchaseParameter.length == 0 || config.PurchaseParameter[0] == 0) {
                    branchKey = "SHOP_ITEM_FREE";
                } else if (config.PurchaseParameter[0] == 1) {
                    branchKey = "SHOP_ITEM_RESOURCE_PAY";
                } else if (config.PurchaseParameter[0] == 2) {
                    branchKey = "SHOP_ITEM_RMB_VOUCHER_PAY";
                } else if (config.PurchaseParameter[0] == 3) {
                    branchKey = "SHOP_ITEM_AD_PAY";
                }

                String caseId = "MATRIX_SHOP_ITEM_" + config.ID;
                int hostShopId = 1;

                ctx.executeMatrixRow(caseId, "ShopItemConfig", config.ID, branchKey,
                        () -> {
                            SandboxResetter.resetModules(player, "shop", "currency", "item");
                            ConditionSatisfier.satisfyConditions(player, config.UnlockCondition, config.BuyCondition);
                            CostProvisioner.provisionShopPayment(player, config.PurchaseParameter, 1);

                            List<ShopItem> items = player.getShopModule().getShopItems(hostShopId);
                            items.clear();
                            ShopItem si = new ShopItem(config.ID);
                            si.setItemBuyTimes(0);
                            items.add(si);
                        },
                        () -> {
                            Map<String, Object> req = Map.of("shopId", hostShopId, "itemId", config.ID, "count", 1);
                            return InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopItemBuyRequest_15000003, req);
                        },
                        result -> {
                            InProcessAcceptanceKit.assertProtocolSuccess(result);
                            ShopItem itemInModule = player.getShopModule().getShopItem(hostShopId, config.ID);
                            if (itemInModule != null && itemInModule.getItemBuyTimes() != 1) {
                                throw new AssertionError("购买后计数异常: " + itemInModule.getItemBuyTimes());
                            }
                        }
                );
            }
        }

        // 2. 全量月卡 MonthCardConfig 扫描
        if (MonthCardManager.instance() != null) {
            for (MonthCardConfig config : MonthCardManager.instance().list()) {
                if (ctx.isCircuitBroken()) {
                    break;
                }
                String branchKey = "MONTH_CARD_BUY";
                String caseId = "MATRIX_MONTH_CARD_" + config.ID;
                ctx.executeMatrixRow(caseId, "MonthCardConfig", config.ID, branchKey,
                        () -> {
                            SandboxResetter.resetModules(player, "monthCard", "currency");
                            CostProvisioner.provisionShopPayment(player, config.Price, 1);
                        },
                        () -> {
                            Map<String, Object> req = Map.of("id", config.ID);
                            return InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.MonthCardBuyRequest_15000010, req);
                        },
                        result -> {
                            InProcessAcceptanceKit.assertProtocolSuccess(result);
                        }
                );
            }
        }

        // 3. 全量章节礼包 ChapterPacksConfig 扫描
        if (ChapterPacksManager.instance() != null) {
            for (ChapterPacksConfig config : ChapterPacksManager.instance().list()) {
                if (ctx.isCircuitBroken()) {
                    break;
                }
                String branchKey = "CHAPTER_PACK_BUY";
                String caseId = "MATRIX_CHAPTER_PACK_" + config.ID;
                ctx.executeMatrixRow(caseId, "ChapterPacksConfig", config.ID, branchKey,
                        () -> {
                            SandboxResetter.resetModules(player, "player", "currency");
                            ConditionSatisfier.satisfyConditions(player, config.Condition);
                            CostProvisioner.provisionShopPayment(player, config.PurchaseParameter, 1);
                        },
                        () -> {
                            Map<String, Object> req = Map.of("id", config.ID);
                            return InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopChapterPacksBuyRequest_15000020, req);
                        },
                        result -> {
                            InProcessAcceptanceKit.assertProtocolSuccess(result);
                        }
                );
            }
        }

        // 4. 全量直购充值 RechargeConfig 扫描
        if (RechargeManager.instance() != null) {
            for (RechargeConfig config : RechargeManager.instance().list()) {
                if (ctx.isCircuitBroken()) {
                    break;
                }
                String branchKey = "RECHARGE_BUY";
                String caseId = "MATRIX_RECHARGE_" + config.ID;
                ctx.executeMatrixRow(caseId, "RechargeConfig", config.ID, branchKey,
                        () -> {
                            SandboxResetter.resetModules(player, "currency");
                            CostProvisioner.provisionShopPayment(player, config.PurchaseParameter, 1);
                        },
                        () -> {
                            Map<String, Object> req = Map.of("id", config.ID);
                            return InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ShopRechargeRequest_15000022, req);
                        },
                        result -> {
                            InProcessAcceptanceKit.assertProtocolSuccess(result);
                        }
                );
            }
        }
    }
}
