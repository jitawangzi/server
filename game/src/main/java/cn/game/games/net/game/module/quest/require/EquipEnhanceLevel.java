package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.develop.equip.EquipModule;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.EquipEnhanceLevel)
public class EquipEnhanceLevel extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.EquipPartStrength };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public EquipEnhanceLevel() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int level = event.getIntParameter(1);
		return level >= getParam();
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		EquipModule module = player.getModule(EquipModule.class);
		return module.getEquipPartCountGTlevel(config.extParam[0]);
	}
}
