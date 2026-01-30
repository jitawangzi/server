package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.DefencelineLevelLowerThan)
public class DefencelineLevelLowerThan extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.DefenceLevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public DefencelineLevelLowerThan() {

	}
	
	@Override
	protected boolean checkAchieve() {
		return getFinishCount() < getRequireCount() ; 
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int level = event.getIntParameter(0);
		return level < getRequireCount();
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		return player.getDevelopModule().getDefenceLevel();
	}
}
