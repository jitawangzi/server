package cn.game.games.net.game.module.rank;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.BattleHelper;
import cn.game.games.net.game.module.develop.AttrModule;
import cn.game.games.net.game.module.develop.attr.AttrCalcType;
import cn.game.games.net.game.module.develop.attr.PlayerAttrCalc;
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
	public void handleEvent(GameEvent event) {
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

	@Override
	public void initFromDbAfter() {
		// TODO Auto-generated method stub

	}

	/** 
	 * 1、已上阵神将排行榜 = 已上阵5个神将养成（升级+突破+图鉴）+所有外围养成
	2、最强神将战力榜 = Max（最强神将，仅算升级+突破）
	3、所有神将战力榜=已拥有的所有神将（升级+突破+图鉴）之和
	 */
	public void updateHeroCombatRank() {
		// 神将属性
		Map<Long, IntMapWrapper> heroAttrs = new HashMap<Long, IntMapWrapper>();
		HeroModule heroModule = player.getHeroModule();
		AttrModule attrModule = player.getAttrModule();
		Collection<Hero> list = heroModule.list();
		for (Hero hero : list) {
			IntMapWrapper heroAttr = BattleHelper.makeHeroAttr(hero);
			heroAttrs.put(hero.getId(), heroAttr);
		}
		// 所有外围属性
		attrModule.calcAllAttr();
		IntMapWrapper playerAttrMap = attrModule.getPlayerAttrMap();

		// 单独的图鉴属性
		IntMapWrapper bookAttrMap = null;
		PlayerAttrCalc bookAttrCalc = attrModule.getPlayerAttrCalcMap().get(AttrCalcType.HeroBook);
		if (bookAttrCalc != null) {
			bookAttrMap = bookAttrCalc.getAttrMap();
		}
		//
		// 所有神将战力
		IntMapWrapper allHeroAttrMap = new IntMapWrapper();
		heroAttrs.forEach((id, attrMap) -> {
			allHeroAttrMap.addAll(attrMap.getMap());
		});
		if (bookAttrMap != null) {
			allHeroAttrMap.addAll(bookAttrMap.getMap());
		}
		float allHeroCombat = BattleHelper.calcCombat(allHeroAttrMap);
		RankService.getInstance().updateMaxValueAsync(player.getServerId(), RankType.AllHeroCombat, playerId, allHeroCombat);

		// 最强神将战力
		float maxHeroCombat = 0;
		for (IntMapWrapper attrMap : heroAttrs.values()) {
			float combat = BattleHelper.calcCombat(attrMap);
			if (combat > maxHeroCombat) {
				maxHeroCombat = combat;
			}
		}
		RankService.getInstance().updateMaxValueAsync(player.getServerId(), RankType.HeroCombat, playerId, maxHeroCombat);

		// 上阵神将战力
		float allBattleCombat = 0;
		Set<Long> battleHeroIds = heroModule.getBattleHeroIds();
		for (Long id : battleHeroIds) {
			IntMapWrapper attrMap = heroAttrs.get(id);
			if (attrMap == null) {
				continue;
			}
			allBattleCombat += BattleHelper.calcCombat(attrMap);
		}
		allBattleCombat += BattleHelper.calcCombat(playerAttrMap);
		attrModule.setPower((int) allBattleCombat);

		RankService.getInstance().updateMaxValueAsync(player.getServerId(), RankType.CurrentHeroCombat, playerId, allBattleCombat);

	}
}
