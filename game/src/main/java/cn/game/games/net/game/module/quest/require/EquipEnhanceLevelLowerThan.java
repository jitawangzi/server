package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.develop.equip.EquipModule;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.EquipEnhanceLevelLowerThan)
public class EquipEnhanceLevelLowerThan extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.EquipPartStrength };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public EquipEnhanceLevelLowerThan() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true; 
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		EquipModule module = player.getModule(EquipModule.class);
		return module.getEquipPartCountLTlevel(config.extParam[0]);
	}
}
