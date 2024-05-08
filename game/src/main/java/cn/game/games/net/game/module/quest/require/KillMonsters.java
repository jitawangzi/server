package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**    
 * 累计杀怪
 * @date 2024年5月8日 下午6:36:31
 * @author SYQ
 */
@ConditionType(type = ConditionTypeEnum.KillMonsters)
public class KillMonsters extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleEnd };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public KillMonsters() {

	}

	@Override
	public long getFinishCount() {
		return player.getQuestModule().getCumulativeCount(ConditionTypeEnum.KillMonsters);
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}
}
