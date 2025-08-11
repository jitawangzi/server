package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.GuildKanJia)
public class GuildBargain extends AbstractCondition {
	private static final EventTypeEnum[] events = {EventTypeEnum.GuildBargain};
    @Override
    public EventTypeEnum[] getEventTypes() {
        return events;
    }
	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true;
	}
}
