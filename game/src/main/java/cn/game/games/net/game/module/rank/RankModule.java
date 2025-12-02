package cn.game.games.net.game.module.rank;

import java.util.Collection;
import java.util.Map;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.battle.EquipTowerBattle;
import cn.game.games.net.game.module.battle.IBattleHandler;
import cn.game.games.net.game.module.battle.LingShanWenChanBattle;
import cn.game.games.net.game.module.battle.TowerBattle;
import cn.game.games.net.game.module.battle.XiangYaoFuMoBattle;
import cn.game.games.net.game.module.develop.AttrModule;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.protocol.generated.config.RankConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.RankManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.IntMapWrapper;

public class RankModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ChapterFirstWin, EventTypeEnum.LevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {

		case LevelUp: {
			int type = event.getIntParameter(0);
			int level = event.getIntParameter(1);
			if (type == Asset.playerExp.ID) {
				RankConfig rankConfig = RankManager.instance().get(RankType.Level.ID);
				if (level >= rankConfig.Request) {
					RankService.getInstance().setScoreAsync(player.getServerId(), RankType.Level, playerId, level);
				}
			}
			break;
		}
		case ChapterFirstWin: {
			RankConfig rankConfig = RankManager.instance().get(RankType.Battle.ID);
			int battleId = event.getIntParameter(0);

			if (BattleHelper.isPreBattle(battleId, rankConfig.Request)) {
				RankService.getInstance().setScoreAsync(player.getServerId(), RankType.Battle, playerId, battleId);
			}
			break;
		}
		}
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	/** 
	 * 1、已上阵神将排行榜 = 已上阵5个神将养成（升级+突破+图鉴）+所有外围养成
	2、最强神将战力榜 = Max（最强神将，仅算升级+突破）--先算上阵的。 
	 */
	public void updateHeroCombatRank() {
		AttrModule attrModule = player.getAttrModule();
		long calcPower = attrModule.calcPower(); 
//		
//		RankConfig rankConfig = RankManager.instance().get(RankType.HeroCombat.ID);
//		if (maxHeroCombat >= rankConfig.Request) {
//			RankService.getInstance().updateMaxValueAsync(player.getServerId(), RankType.HeroCombat, playerId, maxHeroCombat);
//		}
		RankConfig rankConfig = RankManager.instance().get(RankType.CurrentHeroCombat.ID);
		if (calcPower >= rankConfig.Request) {
			RankService.getInstance().updateMaxValueAsync(player.getServerId(), RankType.CurrentHeroCombat, playerId, calcPower);
		}
	}

	/** 
	 * 获取某排行榜的当前排行分值
	 * 如果当前分值不是直接保存在排行榜，在玩家身上的话
	 * 需要在这里计算获取
	 * @param rankType
	 * @return
	 */
	public String getScore(RankType rankType) {
		long score = 0;
		switch (rankType) {
		case Battle: 
		case BattleServerOpenActivity: 
		{
			score  = player.getBattleModule().getMainBattleHighest(); 
			break;
		}
		case Level: 
		case LevelServerOpenActivity: 
		{
			score = player.getLevel();
			break;
		}
		case LingShanWenChan:
		case LingShanWenChanServerOpenActivity:
		{
			LingShanWenChanBattle battle = player.getBattleModule().getBattle(DungeonTypeEnum.LingShanWenChan); 
			score = battle.getLastCompleteFloor(); 
			break;
		}
		case GemTowerMain: 
		case GemTowerMainServerOpenActivity: 
		{
			BattleModule module = player.getModule(BattleModule.class);
			TowerBattle battle = module.getBattle(DungeonTypeEnum.GemTower);
			score = battle.getCurFloor().get(DungeonTypeEnum.GemTower.getId()); 
			break;
		}
		case GemTowerIce: {
			BattleModule module = player.getModule(BattleModule.class);
			TowerBattle battle = module.getBattle(DungeonTypeEnum.GemTower);
			score = battle.getCurFloor().get(DungeonTypeEnum.GemTowerIce.getId()); 
			break;
		}
		case GemTowerThunder: {
			BattleModule module = player.getModule(BattleModule.class);
			TowerBattle battle = module.getBattle(DungeonTypeEnum.GemTower);
			score = battle.getCurFloor().get(DungeonTypeEnum.GemTowerThunder.getId()); 
			break;
		}
		case GemTowerFire: {
			BattleModule module = player.getModule(BattleModule.class);
			TowerBattle battle = module.getBattle(DungeonTypeEnum.GemTower);
			score = battle.getCurFloor().get(DungeonTypeEnum.GemTowerFire.getId()); 
			break;
		}
		case GemTowerPoison: {
			BattleModule module = player.getModule(BattleModule.class);
			TowerBattle battle = module.getBattle(DungeonTypeEnum.GemTower);
			score = battle.getCurFloor().get(DungeonTypeEnum.GemTowerPoison.getId()); 
			break;
		}
		case EquipTower:
		case EquipTowerServerOpenActivity:
		{
			break;
		}
		case XiangYaoFuMo:
		case XiangYaoFuMoServerOpenActivity:
		{
			XiangYaoFuMoBattle battle = player.getBattleModule().getBattle(DungeonTypeEnum.XiangYaoFuMo); 
			score =  battle.getLastCompleteBattleId(); 
			break;
		}
		case DaShengLeiTaiDay: {
			
			break;
		}
		case DaShengLeiTaiSeason: 
		case DaShengLeiTaiServerOpenActivity: 
		{
			
			break;
		}
		case TotalServerOpenActivity: 
		{
			
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + rankType);
		}
		return score +  "";
	}
}
