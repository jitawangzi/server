package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.ParticipateChapter)
public class ParticipateChapter extends AbstractCondition {

	public ParticipateChapter() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.BattleStart };
	}

//	@Override
//	public void updateRequireCount(GameEvent event) {
//		int count = event.getIntParameter(1);
//		finishCount += count;
//	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int id = event.getIntParameter(0);
		if (id == getRequireId()) {
			return true;
		}
		return false;
	}
}
