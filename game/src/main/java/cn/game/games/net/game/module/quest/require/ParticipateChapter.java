package cn.game.games.net.game.module.quest.require;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.manager.BattleManager;

/**    
 * 参与主线章节次数
 * @date 2024年5月8日 下午5:49:16
 * @author SYQ
 */
@ConditionType(type = ConditionTypeEnum.ParticipateChapter)
public class ParticipateChapter extends AbstractCondition {
	private static final EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.BattleStart };

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	public ParticipateChapter() {

	}

	@Override
	public boolean checkEventParam(GameEvent event) {
		int id = event.getIntParameter(0);
		BattleConfig battleConfig = BattleManager.instance().get(id);
		if (battleConfig.BattleType == 1) {
			return true;
		}
		return false;
	}
}
