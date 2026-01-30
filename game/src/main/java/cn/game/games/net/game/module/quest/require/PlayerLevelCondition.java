package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**
 * 玩家等级条件
 * 
 */
@ConditionType(type = ConditionTypeEnum.PlayerLevel)
public class PlayerLevelCondition extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.LevelUp };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public PlayerLevelCondition() {

	}

	@Override
	public long getFinishCount() {
		return getValue(player, null);
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {

		int type = event.getIntParameter(0);
		int level = event.getIntParameter(1);
		if (type == Asset.playerExp.ID) {
			return true;
		}
		return false;
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		return player.getLevel();
	}
}
