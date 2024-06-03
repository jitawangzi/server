package cn.game.games.net.game.module.quest.require;

import java.util.Set;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.PassLostScriptures)
public class PassLostScriptures extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.HeroBattle, EventTypeEnum.HeroLevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public PassLostScriptures() {

	}

	@Override
	public long getFinishCount() {
		int count = 0;
		int level = getParam(0);
		Set<Long> battleHeros = player.getHeroModule().getBattleHeros();
		for (Long id : battleHeros) {
			Hero hero = player.getHeroModule().get(id);
			if (hero.getLevel() >= level) {
				count++;
			}
		}
		return count;
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		Hero hero = event.getParameter(0);
		int level = getParam(0);
		return hero.getLevel() >= level;
	}
}
