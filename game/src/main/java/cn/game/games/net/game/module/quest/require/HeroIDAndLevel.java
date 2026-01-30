package cn.game.games.net.game.module.quest.require;

import java.util.Collection;

import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.HeroIDAndLevel)
public class HeroIDAndLevel extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.HeroLevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public HeroIDAndLevel() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		Hero hero = event.getParameter(0); 
		return hero.getConfigId() == getRequireId() && hero.getLevel() >= getParam(0) && hero.getLevel() <= getParam(1); 
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		boolean ret = false;
		Collection<Hero> byConfigId = player.getHeroModule().getByConfigId(config.idParam); 
		for (Hero hero : byConfigId) {
			if (hero.getLevel() >= config.extParam[0] && hero.getLevel() <= config.extParam[1]) {
				ret = true; 
				break; 
			}
		}
		return ret ? 1 : 0;
	}
}
