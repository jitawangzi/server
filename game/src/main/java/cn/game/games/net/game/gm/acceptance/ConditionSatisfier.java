package cn.game.games.net.game.gm.acceptance;

import java.lang.reflect.Field;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.GuildJoin;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.func.FuncModule;
import cn.game.games.net.game.module.guild.GuildModule;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.config.VIPConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.ConditionManager;
import cn.game.protocol.generated.manager.VIPManager;

/**
 * 通用条件与门槛破障器。
 * 负责在测试前为沙盒玩家开启全部功能、拉满等级与VIP、满足指定前置条件并按需构造临时公会上下文。
 *
 * @author shuyongqiang
 */
public class ConditionSatisfier {

    private static final Logger log = LoggerFactory.getLogger(ConditionSatisfier.class);

    /**
     * 满足全部功能开启与基础高等级门槛
     *
     * @param player 目标沙盒玩家
     * @param targetLevel 目标玩家等级
     * @param targetVipLevel 目标VIP等级
     * @param targetMainBattleId 目标主线关卡通关ID
     */
    public static void satisfyAllFunctionsAndBaseLevel(Player player, int targetLevel, int targetVipLevel, int targetMainBattleId) {
        if (player == null) {
            return;
        }
        if (targetLevel > 0) {
            player.getData().setLevel(targetLevel);
            if (player.getPlayerModule() != null && player.getPlayerModule().getExpLevelMap() != null) {
                player.getPlayerModule().getExpLevelMap().setValue(Asset.playerExp.ID, targetLevel);
            }
        }
        if (targetVipLevel > 0) {
            int effectiveVip = targetVipLevel;
            if (VIPManager.instance() != null && !VIPManager.instance().list().isEmpty()) {
                int maxVip = 0;
                for (VIPConfig vc : VIPManager.instance().list()) {
                    if (vc.ID > maxVip) {
                        maxVip = vc.ID;
                    }
                }
                if (effectiveVip > maxVip) {
                    effectiveVip = maxVip;
                }
            }
            player.getData().setVipLevel(effectiveVip);
            if (player.getPlayerModule() != null && player.getPlayerModule().getExpLevelMap() != null) {
                player.getPlayerModule().getExpLevelMap().setValue(Asset.VIPExp.ID, effectiveVip);
            }
        }
        if (targetMainBattleId > 0 && player.getBattleModule() != null) {
            player.getBattleModule().setMainBattleHighest(targetMainBattleId);
        }
        if (player.getFuncModule() != null) {
            try {
                Field field = FuncModule.class.getDeclaredField("funcOpenMap");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<InitialUI, Boolean> map = (Map<InitialUI, Boolean>) field.get(player.getFuncModule());
                if (map != null) {
                    for (InitialUI initialUI : InitialUI.values()) {
                        map.put(initialUI, true);
                    }
                }
            } catch (Exception t) {
                log.warn("设置全功能开启异常: playerId={}", player.getPlayerId(), t);
            }
        }
    }

    /**
     * 针对指定配置条件集合进行条件前置满足与破障
     *
     * @param player 目标沙盒玩家
     * @param conditionIds 条件ID列表
     */
    public static void satisfyConditions(Player player, int... conditionIds) {
        if (player == null) {
            return;
        }
        satisfyAllFunctionsAndBaseLevel(player, 100, 15, 100);
        if (conditionIds != null) {
            for (int conditionId : conditionIds) {
                if (conditionId == 0) {
                    continue;
                }
                ConditionConfig config = ConditionManager.instance().getNullable(conditionId);
                if (config != null) {
                    try {
                        ConditionTypeEnum typeEnum = ConditionTypeEnum.get(config.type);
                        if (typeEnum != null && typeEnum.countType == 2 && player.getCountingModule() != null) {
                            player.getCountingModule().addCount(typeEnum, config.numParam);
                        }
                    } catch (Exception t) {
                        log.warn("满足条件计数异常: conditionId={}", conditionId, t);
                    }
                }
            }
        }
        SandboxExecutionGuard.enableConditionBypass(player.getPlayerId(), true);
    }

    /**
     * 清理沙盒玩家的临时公会上下文
     *
     * @param player 目标沙盒玩家
     */
    public static void clearSandboxGuildContext(Player player) {
        setSandboxGuildContext(player, 0L);
    }

    /**
     * 设置或清理沙盒玩家的临时公会上下文
     *
     * @param player 目标沙盒玩家
     * @param virtualGuildId 虚拟公会ID（大于0挂载，小于等于0清空）
     */
    public static void setSandboxGuildContext(Player player, long virtualGuildId) {
        if (player == null || player.getGuildModule() == null) {
            return;
        }
        try {
            Field field = GuildModule.class.getDeclaredField("guildJoin");
            field.setAccessible(true);
            if (virtualGuildId > 0) {
                GuildJoin gj = new GuildJoin();
                gj.setPlayerId(player.getPlayerId());
                gj.setGuildId(virtualGuildId);
                field.set(player.getGuildModule(), gj);
                player.getData().setUnionId(virtualGuildId);
            } else {
                field.set(player.getGuildModule(), null);
                player.getData().setUnionId(0L);
            }
        } catch (Exception t) {
            log.warn("设置沙盒公会上下文异常: virtualGuildId={}", virtualGuildId, t);
        }
    }
}
