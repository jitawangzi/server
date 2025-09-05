package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;

@ConditionType(type = ConditionTypeEnum.FunContinueFailTimes)
public class FunContinueFailTimes extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleEnd };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public FunContinueFailTimes() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		BattleConfig battleConfig = BattleManager.instance().get(event.get(0));
		return !event.getBoolParameter(2) && getParam() == 0 || getParam() > 0 && getParam() == battleConfig.BattleType;
	}
}
