package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;

/**
 * 获取指定数量的物品
 * 
 */
@ConditionType(type = OldConditionTypeEnum.GetItemNumbers)
public class GetItemNumbers extends AbstractCondition {

	public GetItemNumbers() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.GetItem };
	}

	@Override
	public void updateRequireCount(GameEvent event) {
		int count = event.getIntParameter(1);
		finishCount += count;
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int id = event.getIntParameter(0);
		if (id == getRequireId())
			return true;
		return false;
	}
}
