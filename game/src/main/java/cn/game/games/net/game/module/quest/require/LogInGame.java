package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.LogInGame)
public class LogInGame extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.NewDay };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public LogInGame() {
		if (this.finishCount == 0) {
			this.finishCount = 1;
		}
	}
	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}

}
