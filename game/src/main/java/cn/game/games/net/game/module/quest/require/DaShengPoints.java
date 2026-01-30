package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.battle.PVEVPBattle;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.manual.DungeonTypeEnum;

@ConditionType(type = ConditionTypeEnum.DaShengPoints)
public class DaShengPoints extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.DaShengPointsAdd };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public DaShengPoints() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		return true;
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		BattleModule module = player.getModule(BattleModule.class);
		PVEVPBattle battle = module.getBattle(DungeonTypeEnum.PVEVPBattle);
		return battle != null ? (int) battle.getMyRank().getScore() : 0;
	}
}
