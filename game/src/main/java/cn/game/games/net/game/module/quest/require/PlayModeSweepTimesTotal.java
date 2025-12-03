package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.PlayModeSweepTimesTotal)
public class PlayModeSweepTimesTotal extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleSweep };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public PlayModeSweepTimesTotal() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true; 
	}
}
