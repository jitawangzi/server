package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.battle.LingShanWenChanBattle;
import cn.game.games.net.game.module.quest.AbstractCumulativeCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;

@ConditionType(type = ConditionTypeEnum.LingShanLevel)
public class LingShanLevel extends AbstractCumulativeCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleEnd ,EventTypeEnum.LingShanSkipFloor};
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public LingShanLevel() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		if (event.getType() == EventTypeEnum.BattleEnd) {
			int id = event.getIntParameter(0);
			BattleConfig battleConfig = BattleManager.instance().get(id);
			if (battleConfig.BattleType == DungeonTypeEnum.LingShanWenChan.getId()) {
//			int subId = event.getIntParameter(1);
				boolean win = event.getBoolParameter(2);
				return win; 
			}
		}else if (event.getType() == EventTypeEnum.LingShanSkipFloor) {
			return true; 
		}
		return false;
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		BattleModule module = player.getModule(BattleModule.class);
		LingShanWenChanBattle battle = module.getBattle(DungeonTypeEnum.LingShanWenChan);
		return battle == null ? 0 : battle.getLastCompleteFloor() >= config.extParam[0] ? 1 : 0;
	}
}
