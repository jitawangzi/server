package cn.game.games.net.game.gm.acceptance.suites;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.acceptance.AcceptanceContext;
import cn.game.games.net.game.gm.acceptance.AcceptanceFixtureException;
import cn.game.games.net.game.gm.acceptance.InProcessAcceptanceKit;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.SandboxResetter;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.item.ItemHandler;
import cn.game.games.net.game.module.item.ItemUse;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;

/**
 * 道具模块双轨自动化验收套件 (0x0b)。
 * 覆盖任选宝箱、随机礼包、手操卡多态使用分支与全量道具分解出售配表扫描。
 *
 * @author shuyongqiang
 */
public class ItemModuleAcceptanceSuite implements ModuleAcceptanceSuite {

    private static final Logger log = LoggerFactory.getLogger(ItemModuleAcceptanceSuite.class);

    public static final int MODULE_ID = 0x0b;
    public static final String MODULE_KEY = "item";
    public static final String TITLE = "道具使用与出售分解";

    private static final List<String> DECLARED_BRANCHES = Collections.unmodifiableList(Arrays.asList(
            "ITEM_USE_TYPE_4_HeroChoose",
            "ITEM_USE_TYPE_7_RandomItem",
            "ITEM_USE_TYPE_13_ShouCaoKa",
            "ITEM_SELL_DECOMPOSE"
    ));

    private static final List<String> RELATED_TABLES = Collections.singletonList("ItemConfig");

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
        InProcessAcceptanceKit.ensureHandlerRegistered(MODULE_ID, ItemHandler::new);
        Player player = ctx.getSandboxPlayer();

        // 1. 契约用例：背包不足拦截
        ctx.executeContractCase("CONTRACT:item_use_not_enough", "使用未拥有的道具应返回余额不足拦截", () -> {
            SandboxResetter.resetModules(player, "item");
            int existingItemId = 1001;
            if (ItemManager.instance() != null && !ItemManager.instance().list().isEmpty()) {
                existingItemId = ItemManager.instance().list().iterator().next().ID;
            }
            Map<String, Object> req = Map.of("itemUse", List.of(Map.of("id", existingItemId, "count", 1, "param", 0)));
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ItemUseRequest_0b000003, req);
            InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.resource_not_enough.getId());
        });

        // 2. 契约用例：非法数量拦截
        ctx.executeContractCase("CONTRACT:item_use_invalid_count", "使用数量非法时应返回参数错误", () -> {
            Map<String, Object> req = Map.of("itemUse", List.of(Map.of("id", 1, "count", -1, "param", 0)));
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ItemUseRequest_0b000003, req);
            InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.request_parameter_error.getId());
        });

        // 3. 契约用例：任选宝箱正向使用
        ctx.executeContractCase("CONTRACT:item_use_type4_hero_choose", "任选宝箱道具使用与奖励正确产出", () -> {
            ItemConfig target = null;
            if (ItemManager.instance() != null) {
                for (ItemConfig item : ItemManager.instance().list()) {
                    if (item.ItemType == 4 && item.Para1 != null && item.Para1.length > 0) {
                        target = item;
                        break;
                    }
                }
            }
            if (target != null) {
                SandboxResetter.resetModules(player, "item", "currency");
                PlayerHelper.addResources(player, target.ID, 1, OpType.GM, false);
                Map<String, Object> req = Map.of("itemUse", List.of(Map.of("id", target.ID, "count", 1, "param", 0)));
                Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ItemUseRequest_0b000003, req);
                InProcessAcceptanceKit.assertProtocolSuccess(res);
                InProcessAcceptanceKit.assertResourceDelta(res, target.Para1[0][0], target.Para1[0][1]);
            }
        });

        // 4. 契约用例：分解出售正向流程
        ctx.executeContractCase("CONTRACT:item_sell_decompose_normal", "道具正常出售分解产出对应资源", () -> {
            ItemConfig target = null;
            if (ItemManager.instance() != null) {
                for (ItemConfig item : ItemManager.instance().list()) {
                    if (item.Decompose != null && item.Decompose.length == 2) {
                        target = item;
                        break;
                    }
                }
            }
            if (target != null) {
                SandboxResetter.resetModules(player, "item", "currency");
                PlayerHelper.addResources(player, target.ID, 2, OpType.GM, false);
                Map<String, Object> req = Map.of("id", List.of(target.ID), "count", List.of(2));
                Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ItemSellRequest_0b000007, req);
                InProcessAcceptanceKit.assertProtocolSuccess(res);
                InProcessAcceptanceKit.assertResourceDelta(res, target.Decompose[0], (long) target.Decompose[1] * 2);
            }
        });

        // 5. 契约用例：背包无该道具时出售拦截
        ctx.executeContractCase("CONTRACT:item_sell_not_enough", "未拥有道具出售应返回资源不足拦截", () -> {
            SandboxResetter.resetModules(player, "item");
            int existingItemId = 1001;
            if (ItemManager.instance() != null && !ItemManager.instance().list().isEmpty()) {
                existingItemId = ItemManager.instance().list().iterator().next().ID;
            }
            Map<String, Object> req = Map.of("id", List.of(existingItemId), "count", List.of(1));
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ItemSellRequest_0b000007, req);
            InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.resource_not_enough.getId());
        });
    }

    @Override
    public void runMatrixScan(AcceptanceContext ctx) throws Exception {
        InProcessAcceptanceKit.ensureHandlerRegistered(MODULE_ID, ItemHandler::new);
        Player player = ctx.getSandboxPlayer();

        if (ItemManager.instance() == null) {
            log.warn("ItemManager 实例为空，跳过多态配表扫描");
            return;
        }

        Collection<ItemConfig> allItems = ItemManager.instance().list();
        for (ItemConfig item : allItems) {
            if (ctx.isCircuitBroken()) {
                break;
            }

            // 1. 多态使用分支扫描
            ItemUse itemUse = ItemUse.valueOf(item.ItemType);
            if (itemUse != null) {
                String branchKey = "ITEM_USE_TYPE_" + item.ItemType + "_" + itemUse.name();
                String caseId = "MATRIX_ITEM_USE_" + item.ID;
                ctx.executeMatrixRow(caseId, "ItemConfig", item.ID, branchKey,
                        () -> {
                            SandboxResetter.resetModules(player, "item", "currency");
                            PlayerHelper.addResources(player, item.ID, 1, OpType.GM, false);
                            if (!PlayerHelper.isEnough(player, item.ID, 1)) {
                                throw new AcceptanceFixtureException("造数后道具数量仍未满足", false);
                            }
                        },
                        () -> {
                            Map<String, Object> req = Map.of("itemUse", List.of(Map.of("id", item.ID, "count", 1, "param", 0)));
                            return InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ItemUseRequest_0b000003, req);
                        },
                        result -> {
                            InProcessAcceptanceKit.assertProtocolSuccess(result);
                        }
                );
            }

            // 2. 出售分解全量扫描
            if (item.Decompose != null && item.Decompose.length >= 2 && item.Decompose[0] > 0 && item.Decompose[1] > 0) {
                String branchKey = "ITEM_SELL_DECOMPOSE";
                String caseId = "MATRIX_ITEM_SELL_" + item.ID;
                ctx.executeMatrixRow(caseId, "ItemConfig", item.ID, branchKey,
                        () -> {
                            SandboxResetter.resetModules(player, "item", "currency");
                            PlayerHelper.addResources(player, item.ID, 1, OpType.GM, false);
                            if (!PlayerHelper.isEnough(player, item.ID, 1)) {
                                throw new AcceptanceFixtureException("造数后道具数量仍未满足", false);
                            }
                        },
                        () -> {
                            Map<String, Object> req = Map.of("id", List.of(item.ID), "count", List.of(1));
                            return InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.ItemSellRequest_0b000007, req);
                        },
                        result -> {
                            InProcessAcceptanceKit.assertProtocolSuccess(result);
                            if (item.Decompose[0] != item.ID) {
                                InProcessAcceptanceKit.assertResourceDelta(result, item.Decompose[0], (long) item.Decompose[1]);
                            }
                        }
                );
            }
        }
    }
}
