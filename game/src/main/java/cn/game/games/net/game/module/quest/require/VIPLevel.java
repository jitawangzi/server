package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.VIPLevel)
public class VIPLevel extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public VIPLevel() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int type = event.getIntParameter(0);
		return type == Asset.VIPExp.ID;
	}
}
