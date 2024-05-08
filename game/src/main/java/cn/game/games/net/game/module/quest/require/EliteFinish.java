package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;

@ConditionType(type = ConditionTypeEnum.EliteFinish)
public class EliteFinish extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ChapterWin };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public EliteFinish() {

	}

	@Override
	public long getFinishCount() {
		return player.getQuestModule().getCumulativeCount(ConditionTypeEnum.EliteFinish);
	}
	@Override
	public boolean checkEventParam(GameEvent event) {
		int id = event.getIntParameter(0);
		BattleConfig battleConfig = BattleManager.instance().get(id);
		if (battleConfig.BattleType == 2) {
			return true;
		}
		return false;
	}
}
