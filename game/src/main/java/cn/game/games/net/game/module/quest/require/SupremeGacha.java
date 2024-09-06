package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * @ClassName SupremeGacha
 *
 * @description:
 * @author: ly
 * @create: 2024-09-06 11:25 @Version 1.0
 */
@ConditionType(type = ConditionTypeEnum.SupremeGacha)

public class SupremeGacha extends AbstractCumulativeCondition {
    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{EventTypeEnum.Draw};
    }

    @Override
    public boolean checkEventParam(GameEvent event) {
        return true;
    }
}
