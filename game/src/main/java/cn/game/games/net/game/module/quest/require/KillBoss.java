package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**    
 * 累计杀boss
 * @date 2024年5月8日 下午6:36:31
 * @author SYQ
 */
@ConditionType(type = ConditionTypeEnum.KillBoss)
public class KillBoss extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleEnd };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public KillBoss() {

	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		return true;
	}
}
