package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.util.GameUtil;

@ConditionType(type = ConditionTypeEnum.Gacha)
public class Gacha extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.Draw };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public Gacha() {

	}
	public void updateRequireCount(PlayerEvent event) {
		finishCount += event.getIntParameter(0);
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		if (getExtParam().length > 0) {
			return GameUtil.contains(getExtParam(), event.getIntParameter(1));
		}
		return true;
	}

}
