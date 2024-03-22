package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;

/**
 * 玩家等级条件
 * 
 */
@ConditionType(type = OldConditionTypeEnum.LimitLevel)
public class PlayerLevelCondition extends AbstractCondition {

	public PlayerLevelCondition() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.LevelUp };
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int level = event.getIntParameter(0);
		if (level >= getRequireCount()) {
			setAchieve();
			return true;
		}
		return false;
	}
}
