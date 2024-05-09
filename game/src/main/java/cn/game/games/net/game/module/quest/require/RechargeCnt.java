package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.RechargeCnt)
public class RechargeCnt extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.Charge };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public RechargeCnt() {

	}
	@Override
	public long getFinishCount() {
		return player.getQuestModule().getCumulativeCount(ConditionTypeEnum.RechargeCnt);
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}
}
