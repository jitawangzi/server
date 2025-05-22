package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.DungeonTypeEnum;

@ConditionType(type = ConditionTypeEnum.PassLostScriptures)
public class PassLostScriptures extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ChapterWin };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	public PassLostScriptures() {

	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int battleId = event.getParameter(0);
		int subId = event.getParameter(1);
		BattleConfig battleConfig = BattleManager.instance().getNullable(battleId);
		if (battleConfig == null || battleConfig.BattleType != DungeonTypeEnum.ShiLuoZhenJing.getId()) {
			return false;
		}
		return battleId == getRequireId() && subId == getParam();
	}
}
