package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * @ClassName SupremeGacha
 *
 * @description: 40这个是只算至尊请神的
 * @author: ly
 * @create: 2024-09-06 11:25 @Version 1.0
 */
@ConditionType(type = ConditionTypeEnum.SupremeGacha)

public class SupremeGacha extends AbstractCondition {
    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{EventTypeEnum.Draw};
    }

    @Override
    public boolean checkEventParam(PlayerEvent event) {
        int typeId = event.getIntParameter(1);
        if (typeId != 2){
            return false;
        }
        return true;
    }

    @Override
    public void updateRequireCount(PlayerEvent event) {
        int count = event.getIntParameter(0);
        addCount(count);
    }
}
