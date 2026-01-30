package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.RSGTreeLevel)
public class RSGTreeLevel extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public RSGTreeLevel() {

	}

	@Override
	public long getFinishCount() {
		return getValue(player, null);
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return event.getIntParameter(0) == Asset.RSGTreeExp.ID; 
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		return player.getLevel(Asset.RSGTreeExp);
	}
}
