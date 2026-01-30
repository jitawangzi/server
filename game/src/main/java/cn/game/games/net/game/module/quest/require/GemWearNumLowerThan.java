package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.develop.gem.GemModule;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.GemWearNumLowerThan)
public class GemWearNumLowerThan extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.GemWear };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public GemWearNumLowerThan() {

	}
	
	@Override
	protected boolean checkAchieve() {
		return getFinishCount() < getRequireCount() ; 
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true; 
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		GemModule module = player.getModule(GemModule.class);
		return module.getCountGTQualityWearCount(config.extParam[0], false);
	}
}
