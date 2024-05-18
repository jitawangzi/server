package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.LvCondition)
public class LvCondition extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public LvCondition() {

	}

	@Override
	public long getFinishCount() {
		return player.getPlayerModule().getExpLevelMap().getValue(getRequireId());
	}
	@Override
	public boolean checkEventParam(GameEvent event) {
		int expId = event.getIntParameter(0);
		int level = event.getIntParameter(1);

		return expId == getRequireId() && level >= getRequireCount();
	}
}
