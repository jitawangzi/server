package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;

/**
 * 拥有指定数量的物品
 * 
 */
@ConditionType(type = OldConditionTypeEnum.GetItems)
public class OwnItemNumbers extends AbstractCondition {

	public OwnItemNumbers() {

	}

	@Override
	public void setEvents() {
		super.events = new EventTypeEnum[] { EventTypeEnum.GetItem };
	}

	@Override
	public long getFinishCount() {

		return ItemHelper.getCount(PlayerManager.getInstance().getPlayer(playerId), getRequireId());
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int id = event.getIntParameter(0);
		if (id == getRequireId()) {
			return true;
		}
		return false;
	}
}
