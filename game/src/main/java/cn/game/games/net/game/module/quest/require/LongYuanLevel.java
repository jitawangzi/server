package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;

@ConditionType(type = ConditionTypeEnum.LongYuanLevel)
public class LongYuanLevel extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleEnd };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public LongYuanLevel() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int id = event.getIntParameter(0);
		BattleConfig battleConfig = BattleManager.instance().get(id);
		if (battleConfig.BattleType == DungeonTypeEnum.GemTower.getId()) {
			boolean win = event.getBoolParameter(2);
			return win; 
		}
		return false;
	}
}
