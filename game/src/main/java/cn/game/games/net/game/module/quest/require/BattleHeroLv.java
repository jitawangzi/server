package cn.game.games.net.game.module.quest.require;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.manual.DungeonTypeEnum;

@ConditionType(type = ConditionTypeEnum.BattleHeroLv)
public class BattleHeroLv extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.HeroBattle, EventTypeEnum.HeroLevelUp ,EventTypeEnum.LineupUpdate};

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public BattleHeroLv() {

	}

	@Override
	public long getFinishCount() {
		int level = getParam(0);
		int count = 0;
		HeroModule heroModule = player.getHeroModule(); 
		Map<Integer, List<String>> lineups = player.getBattleModule().getLineups(DungeonTypeEnum.BattleChapter.getId()); 
		// 从所有阵容里面，找出符合等级条件的 最多数量英雄
		Iterator<Entry<Integer, List<String>>> iterator = lineups.entrySet().iterator(); 
		while (iterator.hasNext()) {
			Map.Entry<java.lang.Integer,java.util.List<java.lang.String>> entry = (Map.Entry<java.lang.Integer,java.util.List<java.lang.String>>) iterator
					.next();
			List<String> value = entry.getValue(); 
			int countTmp = 0;

			for (String string : value) {
				Hero hero = heroModule.get(Long.parseLong(string));
				if (hero.getLevel() >= level) {
					countTmp++;
				}
			}
			if (countTmp > count) {
				count = countTmp; 
			}
		}
		return count;
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
//		Hero hero = event.getParameter(0);
//		int level = getParam(0);
//		return hero.getLevel() >= level;
		//分多阵容了，数据变化就检查
		return true ; 
	}
}
