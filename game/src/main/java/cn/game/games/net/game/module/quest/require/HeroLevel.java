package cn.game.games.net.game.module.quest.require;

import java.util.Collection;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.HeroLevel)
public class HeroLevel extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.HeroLevelUp };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public HeroLevel() {

	}

	@Override
	public long getFinishCount() {
		int count = 0;
		int level = getParam(0);
		Collection<Hero> list = player.getHeroModule().list();
		for (Hero hero : list) {
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
		if (level > 0) {
			return hero.getLevel() == level;
		}
		return false;
	}
}
