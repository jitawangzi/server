package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * 修炼
 * 
 */
@ConditionType(type = ConditionTypeEnum.Practice)
public class Practice extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.QiangYuan, EventTypeEnum.QianLi };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public Practice() {

	}


	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true;
	}
}
