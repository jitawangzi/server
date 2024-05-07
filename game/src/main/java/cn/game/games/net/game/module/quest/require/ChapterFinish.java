package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.ChapterFinish)
public class ChapterFinish extends AbstractCondition {

	public ChapterFinish() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.Chapter };
	}

	@Override
	public void updateRequireCount(GameEvent event) {
		int count = event.getIntParameter(1);
		finishCount += count;
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int id = event.getIntParameter(0);
		int count = event.getIntParameter(1);
		if (id == getRequireId()) {
			finishCount += count;
			return true;
		}
		return false;
	}
}
