package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;

@ConditionType(type = ConditionTypeEnum.EliteFinish)
public class EliteFinish extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ChapterWin };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public EliteFinish() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int id = event.getIntParameter(0);
		BattleConfig battleConfig = BattleManager.instance().get(id);
		if (battleConfig.BattleType == 2) {
			return true;
		}
		return false;
	}
}
