package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.KillMonstersEach)
public class KillMonstersEach extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleEnd };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public KillMonstersEach() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
//		int id = event.getIntParameter(0);
//		EquipConfig equipConfig = EquipManager.instance().get(id); 
//		return equipConfig.quality >= getParam();
		
		return false;
	}
}
