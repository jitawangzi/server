package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.battle.ChapterModule;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

/**    
 * 通关某章节次数
 * @date 2024年5月8日 下午5:19:23
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
		ChapterModule chapterModule = player.getModule(ChapterModule.class);
		return chapterModule.isBattlePass(getRequireId()) ? 1 : 0;
	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int id = event.getIntParameter(0);
		if (id == getRequireId()) {
			return true;
		}
		return false;
	}
}
