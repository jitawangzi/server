package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ConditionTypeEnum;

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
		return PlayerHelper.getConditionCount(player, condition);
	}

	@Override
	public boolean checkEventParam(PlayerEvent event) {
		int id = event.getIntParameter(0);
		if (id == getRequireId()) {
			return true;
		}
		return false;
	}
}
