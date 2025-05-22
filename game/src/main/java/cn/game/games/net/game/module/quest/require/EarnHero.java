package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.HeroManager;

@ConditionType(type = ConditionTypeEnum.EarnHero)
public class EarnHero extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.Hero };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public EarnHero() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int id = event.getIntParameter(0);
		HeroConfig heroConfig = HeroManager.instance().get(id);
		int param = getParam(0);
		if (param > 0) {
			return heroConfig.InitialQuality == param;
		}
		return true;
	}
}
