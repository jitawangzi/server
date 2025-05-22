package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;

@ConditionType(type = ConditionTypeEnum.ParticipateJiDao)
public class ParticipateJiDao extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleStart };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public ParticipateJiDao() {

	}
	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int id = event.getIntParameter(0);
		BattleConfig battleConfig = BattleManager.instance().getNullable(id);
		if (battleConfig != null && battleConfig.BattleType == getParam()) {
			return true;
		}
		return false;
	}
}
