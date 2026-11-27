package cn.game.games.net.game.gm.command;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.battle.LingShanWenChanBattle;
import cn.game.games.net.game.module.battle.MengYanMiJingBattle;
import cn.game.games.net.game.module.battle.ShiLuoZhenJingBattle;
import cn.game.games.net.game.module.battle.TowerBattle;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * 战斗与关卡调试GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "battle", description = "战斗与副本调试指令")
public class BattleGm extends AbstractGm {

	@GmAction(name = "ly", description = "跳关宝石塔(灵玉塔)", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void ly(Player player,
			@GmParam(description = "目标关卡ID", required = false, defaultValue = "0") int id) {
		BattleModule battleModule = player.getBattleModule();
		TowerBattle towerBattle = battleModule.getBattle(DungeonTypeEnum.GemTower);
		towerBattle.gmJump(id);
	}

	@GmAction(name = "zxgk", description = "设置主线通关进度", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void zxgk(Player player,
			@GmParam(description = "主线关卡ID或第几关序号") int id) {
		int p1 = id;
		if (p1 <= 0) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID, "主线关卡参数必须大于0: " + p1);
		}
		if (p1 < 1000) {
			List<BattleConfig> battleTypeList = BattleManager.instance().getBattleTypeList(DungeonTypeEnum.BattleChapter.getId());
			if (battleTypeList == null || battleTypeList.isEmpty()) {
				throw new LogicException(ErrorMsgEnum.config_data_not_found.ID, "主线章节配置为空");
			}
			if (p1 > battleTypeList.size()) {
				throw new LogicException(ErrorMsgEnum.request_parameter_error.ID,
						"主线关卡序号超出范围: " + p1 + "，当前主线总关数=" + battleTypeList.size());
			}
			p1 = battleTypeList.get(p1 - 1).ID;
		}
		BattleConfig battleConfig = BattleManager.instance().getNullable(p1);
		if (battleConfig == null) {
			throw new LogicException(ErrorMsgEnum.config_data_not_found.ID, "未找到主线关卡配置ID: " + p1);
		}
		BattleModule battleModule = player.getBattleModule();
		battleModule.setMainBattleHighest(p1);
		while (battleConfig != null) {
			battleModule.addChapter(battleConfig.ID);
			Chapter chapter = battleModule.getChapter(battleConfig.ID);
			chapter.setBattleTime(30);
			chapter.setPass(true);
			battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
		}
		// 刷新一下功能开启
		if (player.getFuncModule() != null) {
			player.getFuncModule().refreshFuncOpen();
		}
	}

	@GmAction(name = "lingshan", description = "设置灵山问禅通关层数", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void lingshan(Player player,
			@GmParam(description = "层数") int floor) {
		if (floor == 0) {
			throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
		}
		BattleModule battleModule = player.getBattleModule();
		LingShanWenChanBattle battle = battleModule.getBattle(DungeonTypeEnum.LingShanWenChan);
		battle.setLastCompleteFloor(floor);
	}

	@GmAction(name = "slzj", description = "设置失落真经关卡进度", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void slzj(Player player,
			@GmParam(description = "关卡ID", required = false, defaultValue = "0") int id,
			@GmParam(description = "阶段", required = false, defaultValue = "0") int stage) {
		BattleModule battleModule = player.getBattleModule();
		ShiLuoZhenJingBattle battle = battleModule.getBattle(DungeonTypeEnum.ShiLuoZhenJing);
		if (battle != null) {
			BattleConfig battleConfig = BattleManager.instance().get(id);
			if (battleConfig.preBattle > 0) {
				battle.setCompleteBattleId(battleConfig.preBattle);
			}
			battle.setStartBattleId(id);
			if (stage > 0 && stage <= 10) {
				battle.setBattleStage(stage);
			}
		}
	}

	@GmAction(name = "mymj", description = "设置梦魇秘境关卡进度", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void mymj(Player player,
			@GmParam(description = "关卡ID", required = false, defaultValue = "0") int id) {
		BattleModule battleModule = player.getBattleModule();
		MengYanMiJingBattle battle = battleModule.getBattle(DungeonTypeEnum.MengYanMiJing);
		if (battle != null) {
			BattleConfig battleConfig = BattleManager.instance().get(id);
			if (battleConfig.preBattle > 0) {
				battle.setCompleteBattleId(battleConfig.preBattle);
				battle.setMaxBattleId(battleConfig.preBattle);
			}
			battle.setStartBattleId(id);
		}
	}

	@GmAction(name = "slzjsd", description = "设置失落真经手动关卡", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void slzjsd(Player player,
			@GmParam(description = "关卡ID", required = false, defaultValue = "0") int id,
			@GmParam(description = "阶段", required = false, defaultValue = "0") int stage) {
		BattleModule battleModule = player.getBattleModule();
		ShiLuoZhenJingBattle battle = battleModule.getBattle(DungeonTypeEnum.ShiLuoZhenJing);
		if (battle != null) {
			if (id > 0) {
				BattleConfig battleConfig = BattleManager.instance().get(id);
				if (battleConfig.preBattle > 0) {
					battle.setCompleteBattleId(battleConfig.preBattle);
					battle.setHistoryMaxBattleId(battleConfig.preBattle);
				}
				battle.setStartBattleId(id);
			}
			if (stage > 0 && stage < 10) {
				battle.setBattleStage(stage);
			} else {
				battle.setBattleStage(10);
			}
			battle.setZhijieshoudong(true);
		}
	}
}