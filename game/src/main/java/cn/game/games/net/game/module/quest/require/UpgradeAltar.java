package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.UpgradeAltar)
public class UpgradeAltar extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.QianLi };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public UpgradeAltar() {

	}
	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}
}
