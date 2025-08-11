package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * @ClassName GuildBoss
 *
 * @description: GuildBoss
 * @author: ly
 * @create: 2025-02-13 17:23 @Version 1.0
 */
@ConditionType(type = ConditionTypeEnum.GuildBOSS)
public class GuildBoss extends AbstractCondition {
    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[0];
    }

    @Override
    public boolean checkEventParam(PlayerEvent event) {
        return false;
    }
}
