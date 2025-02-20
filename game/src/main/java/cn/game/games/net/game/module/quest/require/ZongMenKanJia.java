package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * @ClassName ZongMenKanJia
 *
 * @description: 参与砍价
 * @author: ly
 * @create: 2025-02-13 17:23 @Version 1.0
 */
@ConditionType(type = ConditionTypeEnum.ZongMenKanJia)
public class ZongMenKanJia extends AbstractCondition {
    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{EventTypeEnum.ZongMenBargain};
    }

    @Override
    public boolean checkEventParam(GameEvent event) {
        return true;
    }
}
