package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * 消耗体力
 * 
 */
@ConditionType(type = ConditionTypeEnum.ExertsStamina)
public class ExertsStamina extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.CostItem };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public ExertsStamina() {

	}

	@Override
	public void updateRequireCount(GameEvent event) {
		int count = event.getIntParameter(1);
		finishCount += count;
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int id = event.getIntParameter(0);
		if (id == Asset.playerEnergy.ID) {
			return true;
		}
		return false;
	}
}
