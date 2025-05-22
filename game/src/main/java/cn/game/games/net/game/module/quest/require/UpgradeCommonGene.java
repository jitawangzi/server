package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.UpgradeCommonGene)
public class UpgradeCommonGene extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] {};

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public UpgradeCommonGene() {

	}

	@Override
	public long getFinishCount() {
		int count = 0;
		int level = getParam(0);
		return count;
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		Hero hero = event.getParameter(0);
		int level = getParam(0);
		return hero.getLevel() >= level;
	}
}
