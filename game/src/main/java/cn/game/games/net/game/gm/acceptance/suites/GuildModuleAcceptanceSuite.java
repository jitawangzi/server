package cn.game.games.net.game.gm.acceptance.suites;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.cross.guild.Guild;
import cn.game.games.net.cross.guild.GuildManager;
import cn.game.games.net.cross.guild.service.GuildService;
import cn.game.games.net.game.gm.acceptance.AcceptanceContext;
import cn.game.games.net.game.gm.acceptance.AcceptanceFixtureException;
import cn.game.games.net.game.gm.acceptance.ConditionSatisfier;
import cn.game.games.net.game.gm.acceptance.CostProvisioner;
import cn.game.games.net.game.gm.acceptance.InProcessAcceptanceKit;
import cn.game.games.net.game.gm.acceptance.ModuleAcceptanceSuite;
import cn.game.games.net.game.gm.acceptance.SandboxResetter;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.guild.GuildHandler;
import cn.game.games.net.game.module.shop.ShopHelper;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.GuildBargainConfig;
import cn.game.protocol.generated.config.GuildDonateConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.GuildBargainManager;
import cn.game.protocol.generated.manager.GuildDonateManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.GuildMsg.GuildServiceInfo;
import cn.game.protocol.protobuf.PbProtocol;

/**
 * 公会模块双轨自动化验收套件 (0x40)。
 * 覆盖公会创建、捐献、砍价与管理相关契约用例及全量配表矩阵扫描。
 *
 * @author shuyongqiang
 */
public class GuildModuleAcceptanceSuite implements ModuleAcceptanceSuite {

    private static final Logger log = LoggerFactory.getLogger(GuildModuleAcceptanceSuite.class);

    public static final int MODULE_ID = 0x40;
    public static final String MODULE_KEY = "guild";
    public static final String TITLE = "公会创建、捐献、砍价与管理验收";

    private static final AtomicLong GUILD_NAME_SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private static final List<String> DECLARED_BRANCHES = Collections.unmodifiableList(Arrays.asList(
            "GUILD_DONATE_FREE",
            "GUILD_DONATE_GOLD",
            "GUILD_DONATE_DIAMOND",
            "GUILD_DONATE_RESOURCE",
            "GUILD_BARGAIN_PERFORM",
            "GUILD_BARGAIN_BUY",
            "GUILD_CREATE_NORMAL",
            "GUILD_SETTING_UPDATE"
    ));

    private static final List<String> RELATED_TABLES = Collections.unmodifiableList(Arrays.asList(
            "GuildDonateConfig",
            "GuildBargainConfig",
            "GuildBasicConfig"
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
        InProcessAcceptanceKit.ensureHandlerRegistered(MODULE_ID, GuildHandler::new);
        Player player = ctx.getSandboxPlayer();
        ConditionSatisfier.satisfyAllFunctionsAndBaseLevel(player, 100, 14, 100);

        // 1. 契约用例：未在公会中的玩家发起捐献拦截
        ctx.executeContractCase("CONTRACT:guild_donate_not_in_guild", "未加入公会的玩家进行捐献应返回非法请求拦截", () -> {
            clearPlayerGuild(player);
            Map<String, Object> req = Map.of("id", 1);
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildDonateRequest_40000067, req);
            InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.illegal_request.getId());
        });

        // 2. 契约用例：付费捐献资源不足拦截
        ctx.executeContractCase("CONTRACT:guild_donate_resource_not_enough", "资源不足时发起付费捐献应返回资源不足拦截", () -> {
            SandboxResetter.resetModules(player, "currency");
            ensureSandboxGuild(player);
            GuildDonateConfig paidConfig = findPaidDonateConfig();
            if (paidConfig != null) {
                player.getGuildModule().getDonateMap().remove(paidConfig.ID);
                Map<String, Object> req = Map.of("id", paidConfig.ID);
                Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildDonateRequest_40000067, req);
                InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.resource_not_enough.getId());
            }
        });

        // 3. 契约用例：达到每日捐献上限拦截
        ctx.executeContractCase("CONTRACT:guild_donate_times_limit", "超过每日捐献上限时应返回次数超限拦截", () -> {
            ensureSandboxGuild(player);
            GuildDonateConfig config = findFirstDonateConfig();
            if (config != null) {
                player.getGuildModule().getDonateMap().add(config.ID, config.DayCount);
                Map<String, Object> req = Map.of("id", config.ID);
                Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildDonateRequest_40000067, req);
                InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.times_limit.getId());
            }
        });

        // 4. 契约用例：VIP 等级不足创建公会拦截
        ctx.executeContractCase("CONTRACT:guild_create_vip_limit", "VIP 等级不足时创建公会应返回等级不足拦截", () -> {
            clearPlayerGuild(player);
            if (GlobalConst.GuildCreationVIP <= 0) {
                GlobalConst.GuildCreationVIP = 1;
            }
            player.getPlayerModule().getExpLevelMap().setValue(Asset.VIPExp.ID, 0);
            Map<String, Object> req = Map.of("name", "测试VIP拦截公会", "notice", "公告", "declaration", "宣言", "icon", 1, "autoJoin", 1);
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildCreateRequest_40000005, req);
            InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.level_not_enough.getId());
            player.getPlayerModule().getExpLevelMap().setValue(Asset.VIPExp.ID, 14);
        });

        // 5. 契约用例：已有公会玩家重复创建拦截
        ctx.executeContractCase("CONTRACT:guild_create_already_in_guild", "已有公会的玩家创建公会应返回宗门已存在拦截", () -> {
            ensureSandboxGuild(player);
            player.getPlayerModule().getExpLevelMap().setValue(Asset.VIPExp.ID, 14);
            Map<String, Object> req = Map.of("name", "重复创建测试公会", "notice", "公告", "declaration", "宣言", "icon", 1, "autoJoin", 1);
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildCreateRequest_40000005, req);
            InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.zong_men_exist.getId());
        });

        // 6. 契约用例：公会信息详情查询正常返回
        ctx.executeContractCase("CONTRACT:guild_info_query_success", "已在公会中的玩家查询公会信息应正常返回详情", () -> {
            ensureSandboxGuild(player);
            Map<String, Object> req = Collections.emptyMap();
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildInfoRequest_40000021, req);
            InProcessAcceptanceKit.assertProtocolSuccess(res);
        });

        // 7. 契约用例：未在公会中的玩家发起砍价拦截
        ctx.executeContractCase("CONTRACT:guild_bargain_not_in_guild", "未在公会中的玩家发起砍价应返回宗门不存在拦截", () -> {
            clearPlayerGuild(player);
            Map<String, Object> req = Collections.emptyMap();
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildBargainRequest_40000060, req);
            InProcessAcceptanceKit.assertErrorCode(res, ErrorMsgEnum.zong_men_not_exist.getId());
        });

        // 8. 契约用例：宗主修改公会设置成功
        ctx.executeContractCase("CONTRACT:guild_setting_update_success", "宗主修改公会设置（公告、宣言与图标）应成功生效", () -> {
            ensureSandboxGuild(player);
            Map<String, Object> req = Map.of(
                    "notice", "验收更新公告",
                    "declaration", "验收更新宣言",
                    "icon", 2,
                    "autoJoin", 1
            );
            Map<String, Object> res = InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildSettingRequest_40000013, req);
            InProcessAcceptanceKit.assertProtocolSuccess(res);
        });
    }

    @Override
    public void runMatrixScan(AcceptanceContext ctx) throws Exception {
        InProcessAcceptanceKit.ensureHandlerRegistered(MODULE_ID, GuildHandler::new);
        Player player = ctx.getSandboxPlayer();
        ConditionSatisfier.satisfyAllFunctionsAndBaseLevel(player, 100, 14, 100);

        // 1. 公会捐献全量配表扫描 (GuildDonateConfig)
        if (GuildDonateManager.instance() != null) {
            Collection<GuildDonateConfig> donateConfigs = GuildDonateManager.instance().list();
            for (GuildDonateConfig config : donateConfigs) {
                if (ctx.isCircuitBroken()) {
                    break;
                }

                String branchKey = determineDonateBranch(config);
                String caseId = "MATRIX_GUILD_DONATE_" + config.ID;

                ctx.executeMatrixRow(caseId, "GuildDonateConfig", config.ID, branchKey,
                        () -> {
                            ensureSandboxGuild(player);
                            player.getGuildModule().getDonateMap().remove(config.ID);
                            if (config.Price != null && config.Price.length >= 3 && config.Price[0] == ShopHelper.COST_TYPE_RESOURCE) {
                                CostProvisioner.provisionShopPayment(player, config.Price, 1);
                                if (!player.isEnough(config.Price[1], config.Price[2])) {
                                    throw new AcceptanceFixtureException("造数后捐献消耗资源仍未满足: resId=" + config.Price[1], false);
                                }
                            }
                        },
                        () -> {
                            Map<String, Object> req = Map.of("id", config.ID);
                            return InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildDonateRequest_40000067, req);
                        },
                        result -> {
                            InProcessAcceptanceKit.assertProtocolSuccess(result);
                            if (config.Price != null && config.Price.length >= 3 && config.Price[0] == ShopHelper.COST_TYPE_RESOURCE && config.Price[2] > 0) {
                                InProcessAcceptanceKit.assertResourceDelta(result, config.Price[1], -config.Price[2]);
                            }
                        }
                );
            }
        }

        // 2. 公会砍价多态商品全集扫描 (GuildBargainConfig)
        if (GuildBargainManager.instance() != null) {
            Collection<GuildBargainConfig> bargainConfigs = GuildBargainManager.instance().list();
            for (GuildBargainConfig config : bargainConfigs) {
                if (ctx.isCircuitBroken()) {
                    break;
                }

                // 2.1 砍价动作扫描
                String caseIdPerform = "MATRIX_GUILD_BARGAIN_PERFORM_" + config.ID;
                ctx.executeMatrixRow(caseIdPerform, "GuildBargainConfig", config.ID, "GUILD_BARGAIN_PERFORM",
                        () -> {
                            Guild guild = ensureSandboxGuild(player);
                            guild.getModule().getBargain().setBargainItemId(config.ID);
                            guild.getModule().getBargain().setBargainTotalNum(0);
                            player.getGuildModule().setBargainCount(0);
                            player.getGuildModule().setDisbandCount(0);
                            PlayerHelper.addResources(player, Asset.GuildBargain.ID, 5, OpType.GM, false);
                            if (!player.isEnough(Asset.GuildBargain.ID, 1)) {
                                throw new AcceptanceFixtureException("造数后砍价次数资源不足", false);
                            }
                        },
                        () -> {
                            Map<String, Object> req = Collections.emptyMap();
                            return InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildBargainRequest_40000060, req);
                        },
                        result -> {
                            InProcessAcceptanceKit.assertProtocolSuccess(result);
                        }
                );

                // 2.2 砍价购买动作扫描
                String caseIdBuy = "MATRIX_GUILD_BARGAIN_BUY_" + config.ID;
                ctx.executeMatrixRow(caseIdBuy, "GuildBargainConfig", config.ID, "GUILD_BARGAIN_BUY",
                        () -> {
                            Guild guild = ensureSandboxGuild(player);
                            guild.getModule().getBargain().setBargainItemId(config.ID);
                            player.getGuildModule().setBargainBuy(false);
                            if (config.Price != null && config.Price.length >= 2) {
                                CostProvisioner.provisionCostArray(player, config.Price, 1);
                                if (!player.isEnough(config.Price[0], config.Price[1])) {
                                    throw new AcceptanceFixtureException("造数后购买砍价商品货币不足", false);
                                }
                            }
                        },
                        () -> {
                            Map<String, Object> req = Collections.emptyMap();
                            return InProcessAcceptanceKit.invokeProto(player.getPlayerId(), PbProtocol.GuildBargainBuyRequest_40000062, req);
                        },
                        result -> {
                            InProcessAcceptanceKit.assertProtocolSuccess(result);
                        }
                );
            }
        }
    }

    /**
     * 根据捐献配置的价格结构识别多态分支
     */
    private String determineDonateBranch(GuildDonateConfig config) {
        if (config.Price == null || config.Price.length == 0 || (config.Price.length == 1 && config.Price[0] == 0)) {
            return "GUILD_DONATE_FREE";
        }
        int costType = config.Price[0];
        if (costType == ShopHelper.COST_TYPE_ADVERTISE) {
            return "GUILD_DONATE_FREE";
        }
        if (config.Price.length >= 3 && costType == ShopHelper.COST_TYPE_RESOURCE) {
            int assetId = config.Price[1];
            if (assetId == Asset.gold.ID) {
                return "GUILD_DONATE_GOLD";
            } else if (assetId == Asset.diamond.ID) {
                return "GUILD_DONATE_DIAMOND";
            }
        }
        return "GUILD_DONATE_RESOURCE";
    }

    private GuildDonateConfig findFirstDonateConfig() {
        if (GuildDonateManager.instance() != null && !GuildDonateManager.instance().list().isEmpty()) {
            return GuildDonateManager.instance().list().iterator().next();
        }
        return null;
    }

    private GuildDonateConfig findPaidDonateConfig() {
        if (GuildDonateManager.instance() != null) {
            for (GuildDonateConfig cfg : GuildDonateManager.instance().list()) {
                if (cfg.Price != null && cfg.Price.length >= 3 && cfg.Price[0] == ShopHelper.COST_TYPE_RESOURCE && cfg.Price[2] > 0) {
                    return cfg;
                }
            }
        }
        return null;
    }

    /**
     * 确保沙盒玩家处于一个有效的公会中（若无则创建）
     */
    private Guild ensureSandboxGuild(Player player) {
        long currentGuildId = player.getGuildId();
        if (currentGuildId > 0) {
            Guild existing = GuildManager.getInstance().getGuild(currentGuildId);
            if (existing != null) {
                return existing;
            }
        }
        player.getGuildModule().setDisbandCount(0);
        player.getGuildModule().setNextJoinTimer(0);
        String guildName = "沙盒宗门" + GUILD_NAME_SEQ.incrementAndGet();
        GuildServiceInfo serviceInfo = GuildService.getInstance().createGuild(
                player.getPlayerId(), guildName, "沙盒测试公告", "沙盒测试宣言", 1, 1);
        long newGuildId = serviceInfo.getShowInfo().getSimpleInfo().getId();
        player.getGuildModule().join(newGuildId);
        return GuildManager.getInstance().getGuild(newGuildId);
    }

    /**
     * 清理沙盒玩家的公会挂载
     */
    private void clearPlayerGuild(Player player) {
        if (player.getGuildId() > 0) {
            player.getGuildModule().quit(0);
        }
        player.getGuildModule().setDisbandCount(0);
        player.getGuildModule().setNextJoinTimer(0);
        ConditionSatisfier.clearSandboxGuildContext(player);
    }
}
