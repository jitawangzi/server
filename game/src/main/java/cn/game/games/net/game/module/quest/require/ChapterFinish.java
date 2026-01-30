package cn.game.games.net.game.module.quest.require;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.ConditionManager;

/**    
 * 通关某章节次数
 * 2024年5月8日 下午5:19:23
 * @author SYQ
 */
@ConditionType(type = ConditionTypeEnum.ChapterFinish)
public class ChapterFinish extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.ChapterWin };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public ChapterFinish() {

	}

	@Override
	public long getFinishCount() {
		return getValue(player, ConditionManager.instance().get(condition));
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int id = event.getIntParameter(0);
		if (id == getRequireId()) {
			return true;
		}
		return false;
	}

	@Override
	public long getValue(Player player, ConditionConfig config) {
		BattleModule battleModule = player.getModule(BattleModule.class);
		return battleModule.isBattlePass(config.idParam) ? 1 : 0;
	}
}
