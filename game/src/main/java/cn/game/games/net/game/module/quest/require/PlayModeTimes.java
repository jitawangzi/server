package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;

@ConditionType(type = ConditionTypeEnum.PlayModeTimes)
public class PlayModeTimes extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleStart };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public PlayModeTimes() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int id = event.getIntParameter(0);
		BattleConfig battleConfig = BattleManager.instance().get(id); 
		return battleConfig.BattleType == getParam();
	}
}
