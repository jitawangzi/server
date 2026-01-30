package cn.game.games.net.game.gm.command;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.battle.LingShanWenChanBattle;
import cn.game.games.net.game.module.battle.MengYanMiJingBattle;
import cn.game.games.net.game.module.battle.ShiLuoZhenJingBattle;
import cn.game.games.net.game.module.battle.TowerBattle;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.core.exception.LogicException;

@Component
public class BattleGm extends AbstractGm {

    @Override
    public void init() {
        register("ly", this::ly);
        register("zxgk", this::zxgk);
        register("lingshan", this::lingshan);
        register("slzj", this::slzj);
        register("mymj", this::mymj);
        register("slzjsd", this::slzjsd);
    }

    private void ly(Player player, String[] params) {
        // 设置关卡id
        int p1 = getInt(params, 1);
        BattleModule battleModule = player.getBattleModule();
        TowerBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.GemTower);
        towerBattle.gmJump(p1);
    }

    private void zxgk(Player player, String[] params) {
        // 设置主线关卡id
        int p1 = getInt(params, 1);
        if (p1 == 0) {
            throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
        }
        if (p1 < 1000) {
            List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(DungeonTypeEnum.BattleChapter.getId());
            p1 = battleTypeList.get(p1 - 1).ID;
        }
        BattleModule battleModule = player.getBattleModule();
        battleModule.setMainBattleHighest(p1);
        BattleConfig battleConfig = BattleManager.instance().getNullable(p1);
        while (battleConfig != null) {
            battleModule.addChapter(battleConfig.ID);
            Chapter chapter = battleModule.getChapter(battleConfig.ID);
            chapter.setBattleTime(30);
            chapter.setPass(true);
            battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
        }
        // 刷新一下功能开启
        player.getFuncModule().refreshFuncOpen();
    }

    private void lingshan(Player player, String[] params) {
        // 设置灵山问禅层数
        int p1 = getInt(params, 1);
        if (p1 == 0) {
            throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
        }
        BattleModule battleModule = player.getBattleModule();
        LingShanWenChanBattle battle = battleModule.getBattle(DungeonTypeEnum.LingShanWenChan);
        battle.setLastCompleteFloor(p1);
    }

    private void slzj(Player player, String[] params) {
        // 设置失落真经关卡id
        int p1 = getInt(params, 1);
        int p2 = getInt(params, 2);
        BattleModule battleModule = player.getBattleModule();
        ShiLuoZhenJingBattle battle = battleModule.getBattle(DungeonTypeEnum.ShiLuoZhenJing);
        if (battle != null) {
            BattleConfig battleConfig = BattleManager.instance().get(p1);
            if (battleConfig.preBattle > 0) {
                battle.setCompleteBattleId(battleConfig.preBattle);
            }
            battle.setStartBattleId(p1);
            if (p2 > 0 && p2 <= 10) {
                battle.setBattleStage(p2);
            }
        }
    }

    private void mymj(Player player, String[] params) {
        // 设置梦魇秘境关卡id
        int p1 = getInt(params, 1);
        BattleModule battleModule = player.getBattleModule();
        MengYanMiJingBattle battle = battleModule.getBattle(DungeonTypeEnum.MengYanMiJing);
        if (battle != null) {
            BattleConfig battleConfig = BattleManager.instance().get(p1);
            if (battleConfig.preBattle > 0) {
                battle.setCompleteBattleId(battleConfig.preBattle);
                battle.setMaxBattleId(battleConfig.preBattle);
            }
            battle.setStartBattleId(p1);
        }
    }

    private void slzjsd(Player player, String[] params) {
        // 设置失落真经手动关卡。
        int p1 = getInt(params, 1);
        int p2 = getInt(params, 2);
        BattleModule battleModule = player.getBattleModule();
        ShiLuoZhenJingBattle battle = battleModule.getBattle(DungeonTypeEnum.ShiLuoZhenJing);
        if (battle != null) {
            if (p1 > 0) {
                BattleConfig battleConfig = BattleManager.instance().get(p1);
                if (battleConfig.preBattle > 0) {
                    battle.setCompleteBattleId(battleConfig.preBattle);
                    battle.setHistoryMaxBattleId(battleConfig.preBattle);
                }
                battle.setStartBattleId(p1);
            }
            if (p2 > 0 && p2 < 10) {
                battle.setBattleStage(p2);
            } else {
                battle.setBattleStage(10);
            }
            battle.setZhijieshoudong(true);
        }
    }
}
