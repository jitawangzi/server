package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.ConsumesDiamonds)
public class ConsumesDiamonds extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.CostItem };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public ConsumesDiamonds() {

	}

	@Override
	public long getFinishCount() {
		return player.getQuestModule().getCumulativeCount(ConditionTypeEnum.ConsumesDiamonds);
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int id = event.getIntParameter(0);
		if (id == Asset.diamond.ID) {
			return true;
		}
		return false;
	}
}
