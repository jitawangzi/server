package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.KillBossEach)
public class KillBossEach extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleEnd };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public KillBossEach() {

	}
	
	public void updateRequireCount(PlayerEvent event) {
		int killBossCount = event.getIntParameter(4); 
		finishCount += killBossCount;
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int killBossCount = event.getIntParameter(4); 
		return killBossCount > 0;
	}
}
