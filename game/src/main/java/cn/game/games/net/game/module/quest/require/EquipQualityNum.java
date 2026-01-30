package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.develop.equip.EquipModule;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.config.EquipConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.EquipManager;

@ConditionType(type = ConditionTypeEnum.EquipQualityNum)
public class EquipQualityNum extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.EquipWear };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public EquipQualityNum() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int id = event.getIntParameter(0);
		EquipConfig equipConfig = EquipManager.instance().get(id); 
		return equipConfig.quality >= getParam();
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		EquipModule module = player.getModule(EquipModule.class);
		return module.getEquipCountGTQuality(config.extParam[0]);
	}
}
