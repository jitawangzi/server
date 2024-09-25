package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.ChallengeWin)

public class ChallengeWin extends AbstractCondition {
    @Override
    public EventTypeEnum[] getEventTypes() {
		return new EventTypeEnum[] { EventTypeEnum.BattleEnd };
    }

    @Override
    public boolean checkEventParam(GameEvent event) {
		boolean win = event.getBoolParameter(2);
		return win;
    }
}
