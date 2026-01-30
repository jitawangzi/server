package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.RemainMatiarialNow)
public class RemainMatiarialNow extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.CostItem };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public RemainMatiarialNow() {

	}
	@Override
	protected boolean checkAchieve() {
		return getFinishCount() <= getRequireCount() ; 
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return event.get(0) == getRequireId();
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		return player.getGoodsModule(config.idParam).getCount(config.idParam);
	}
}
