package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.EquipEnhanceLevel)
public class EquipEnhanceLevel extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.EquipPartStrength };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public EquipEnhanceLevel() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int level = event.getIntParameter(1);
		return level >= getParam();
	}
}
