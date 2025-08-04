package cn.game.games.net.game.module.rank;

import java.util.Collection;
import java.util.Map;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.module.develop.AttrModule;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.protocol.generated.config.RankConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.RankManager;
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
		HeroModule heroModule = player.getHeroModule();
		AttrModule attrModule = player.getAttrModule();
		Collection<Hero> battleHeros = heroModule.getBattleHeroList();
		// 计算所有所有属性
		attrModule.calcAllAttr();

		// 外围属性
		IntMapWrapper playerAttrMap = attrModule.getPlayerAttrMap();
		// 外围战力
		float playerAttrCombat = BattleHelper.calcCombat(playerAttrMap);
		// 神将属性
		Map<Long, IntMapWrapper> heroAttrs = attrModule.getHeroAttrs();
		float allHeroCombat = 0;
		// 最强神将战力
		float maxHeroCombat = 0;
		for (IntMapWrapper attrMap : heroAttrs.values()) {
			float combat = BattleHelper.calcCombat(attrMap);
			if (combat > maxHeroCombat) {
				maxHeroCombat = combat;
			}
			allHeroCombat += combat;
		}
		RankConfig rankConfig = RankManager.instance().get(RankType.HeroCombat.ID);
		if (maxHeroCombat >= rankConfig.Request) {
			RankService.getInstance().updateMaxValueAsync(player.getServerId(), RankType.HeroCombat, playerId, maxHeroCombat);
		}
		rankConfig = RankManager.instance().get(RankType.CurrentHeroCombat.ID);
		float allCombat = allHeroCombat + playerAttrCombat * battleHeros.size();
		if (allCombat >= rankConfig.Request) {
			RankService.getInstance().updateMaxValueAsync(player.getServerId(), RankType.CurrentHeroCombat, playerId, allCombat);
		}
		attrModule.setPower((int) allCombat);

	}

	public String getScore(RankType rankType) {
		switch (rankType) {
		case Battle: {

			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + rankType);
		}

		return "";

	}
}
