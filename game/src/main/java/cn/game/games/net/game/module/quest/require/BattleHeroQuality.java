package cn.game.games.net.game.module.quest.require;

import java.util.Map;

import cn.game.games.cache.entity.Hero;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

@ConditionType(type = ConditionTypeEnum.BattleHeroQuality)
public class BattleHeroQuality extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.HeroBattle };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public BattleHeroQuality() {

	}

	@Override
	public long getFinishCount() {
		int count = 0;
		Map<Long, Integer> battleHeros = player.getHeroModule().getBattleHeros();
		for (Long id : battleHeros.keySet()) {
			Hero hero = player.getHeroModule().get(id);
//			HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
			if (hero.getQuality() >= getParam(0)) {
				count++;
			}
		}
		return count;
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		Hero hero = event.getParameter(0);
		int quality = getParam(0);
//		HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
		return hero.getQuality() >= quality;
	}
}
