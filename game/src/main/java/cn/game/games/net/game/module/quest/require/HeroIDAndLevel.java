package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * 英雄id 等级条件
 * 
 */
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
		return hero.getConfigId() == getRequireId();
	}
}
