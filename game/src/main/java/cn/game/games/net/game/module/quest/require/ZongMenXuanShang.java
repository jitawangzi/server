package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * @ClassName ZongMenXuanShang
 *
 * @description: 宗门悬赏
 * @author: ly
 * @create: 2025-02-13 17:23 @Version 1.0
 */
@ConditionType(type = ConditionTypeEnum.ZongMenXuanShang)
public class ZongMenXuanShang extends AbstractCondition {
    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[0];
    }

    @Override
    public boolean checkEventParam(PlayerEvent event) {
        return false;
    }
}
